import React, { useState, useRef, useEffect, useCallback } from 'react';
import {
  StyleSheet, Text, View, TouchableOpacity, ScrollView,
  Alert, Animated, Dimensions, StatusBar, Modal, Pressable,
  ActivityIndicator, AppState, AppStateStatus,
} from 'react-native';
import { Audio } from 'expo-av';
import * as FileSystem from 'expo-file-system';
import * as Haptics from 'expo-haptics';
import { useKeepAwake } from 'expo-keep-awake';
import { MaterialIcons } from '@expo/vector-icons';

const { width: SCREEN_WIDTH } = Dimensions.get('window');
const SERVER_URL = 'https://caroline-server-v2-production.up.railway.app';
const WS_URL = 'wss://caroline-server-v2-production.up.railway.app/ws/voice';

// Silence detection thresholds
const SILENCE_THRESHOLD_DB = -40;   // dB level below which we consider silence
const SILENCE_DURATION_MS  = 800;   // ms of silence before we send the clip
const MIN_SPEECH_MS        = 300;   // ignore clips shorter than this (noise bursts)

type Message = { id: string; role: 'user' | 'assistant'; content: string; timestamp: Date };
type AppState2 = 'idle' | 'connecting' | 'listening' | 'recording' | 'processing' | 'speaking' | 'error';

const RECORDING_OPTIONS: Audio.RecordingOptions = {
  isMeteringEnabled: true,
  android: {
    extension: '.m4a',
    outputFormat: Audio.AndroidOutputFormat.MPEG_4,
    audioEncoder: Audio.AndroidAudioEncoder.AAC,
    sampleRate: 16000,
    numberOfChannels: 1,
    bitRate: 32000,
  },
  ios: {
    extension: '.m4a',
    outputFormat: Audio.IOSOutputFormat.MPEG4AAC,
    audioQuality: Audio.IOSAudioQuality.MEDIUM,
    sampleRate: 16000,
    numberOfChannels: 1,
    bitRate: 32000,
    linearPCMBitDepth: 16,
    linearPCMIsBigEndian: false,
    linearPCMIsFloat: false,
  },
  web: { mimeType: 'audio/webm', bitsPerSecond: 32000 },
};

export default function App() {
  useKeepAwake();

  const [appState, setAppState]         = useState<AppState2>('idle');
  const [messages, setMessages]         = useState<Message[]>([]);
  const [statusText, setStatusText]     = useState('Tap to talk to Caroline');
  const [serverOnline, setServerOnline] = useState<boolean | null>(null);
  const [settingsVisible, setSettingsVisible] = useState(false);

  const wsRef            = useRef<WebSocket | null>(null);
  const soundRef         = useRef<Audio.Sound | null>(null);
  const recordingRef     = useRef<Audio.Recording | null>(null);
  const scrollRef        = useRef<ScrollView>(null);
  const pulseAnim        = useRef(new Animated.Value(1)).current;
  const appStateRef      = useRef(AppState.currentState);

  // VAD state refs (not React state — updated in callbacks without re-render)
  const silenceTimerRef  = useRef<ReturnType<typeof setTimeout> | null>(null);
  const speechStartedRef = useRef<boolean>(false);   // true once user has spoken above threshold
  const isPlayingRef     = useRef<boolean>(false);   // true while Caroline is speaking
  const isListeningRef   = useRef<boolean>(false);   // true while mic is open

  // ─── Pulse animation ──────────────────────────────────────────────────────
  const startPulse = useCallback(() => {
    Animated.loop(
      Animated.sequence([
        Animated.timing(pulseAnim, { toValue: 1.18, duration: 500, useNativeDriver: true }),
        Animated.timing(pulseAnim, { toValue: 1,    duration: 500, useNativeDriver: true }),
      ])
    ).start();
  }, [pulseAnim]);

  const stopPulse = useCallback(() => {
    pulseAnim.stopAnimation();
    pulseAnim.setValue(1);
  }, [pulseAnim]);

  // ─── Server health check ──────────────────────────────────────────────────
  const checkServer = useCallback(async () => {
    try {
      const controller = new AbortController();
      const timer = setTimeout(() => controller.abort(), 8000);
      const res = await fetch(`${SERVER_URL}/health`, { signal: controller.signal });
      clearTimeout(timer);
      setServerOnline(res.ok);
      return res.ok;
    } catch {
      setServerOnline(false);
      return false;
    }
  }, []);

  useEffect(() => {
    checkServer();
    const sub = AppState.addEventListener('change', (next: AppStateStatus) => {
      if (appStateRef.current.match(/inactive|background/) && next === 'active') checkServer();
      if (next.match(/inactive|background/)) disconnect();
      appStateRef.current = next;
    });
    return () => sub.remove();
  }, []);

  useEffect(() => {
    setTimeout(() => scrollRef.current?.scrollToEnd({ animated: true }), 100);
  }, [messages]);

  const addMessage = useCallback((role: 'user' | 'assistant', content: string) => {
    setMessages(prev => [...prev, {
      id: Date.now().toString() + Math.random(),
      role, content, timestamp: new Date(),
    }]);
  }, []);

  // ─── Stop mic completely ──────────────────────────────────────────────────
  const stopMic = useCallback(async () => {
    isListeningRef.current = false;
    if (silenceTimerRef.current) {
      clearTimeout(silenceTimerRef.current);
      silenceTimerRef.current = null;
    }
    speechStartedRef.current = false;
    if (recordingRef.current) {
      try { await recordingRef.current.stopAndUnloadAsync(); } catch {}
      recordingRef.current = null;
    }
  }, []);

  // ─── Send recorded clip to server ────────────────────────────────────────
  const sendClip = useCallback(async (recording: Audio.Recording) => {
    const uri = recording.getURI();
    if (!uri || !wsRef.current || wsRef.current.readyState !== WebSocket.OPEN) return;
    try {
      const base64 = await FileSystem.readAsStringAsync(uri, {
        encoding: FileSystem.EncodingType.Base64,
      });
      wsRef.current.send(JSON.stringify({ type: 'audio_chunk', data: base64 }));
      console.log('Sent clip, size:', base64.length);
      setAppState('processing');
      setStatusText('Processing...');
      stopPulse();
    } catch (e) {
      console.error('Send clip error:', e);
    } finally {
      try { await FileSystem.deleteAsync(uri, { idempotent: true }); } catch {}
    }
  }, [stopPulse]);

  // ─── Start listening (open mic with metering VAD) ─────────────────────────
  const startListening = useCallback(async () => {
    if (isListeningRef.current || isPlayingRef.current) return;
    if (!wsRef.current || wsRef.current.readyState !== WebSocket.OPEN) return;

    try {
      await Audio.setAudioModeAsync({
        allowsRecordingIOS: true,
        playsInSilentModeIOS: true,
        staysActiveInBackground: false,
      });

      const { recording } = await Audio.Recording.createAsync(RECORDING_OPTIONS);
      recordingRef.current = recording;
      isListeningRef.current = true;
      speechStartedRef.current = false;
      const recordingStartTime = Date.now();

      setAppState('listening');
      setStatusText('Listening...');
      startPulse();

      // ── Metering callback — fires every ~100ms ──────────────────────────
      recording.setOnRecordingStatusUpdate(async (status) => {
        if (!status.isRecording) return;
        if (!isListeningRef.current) return;

        const db = status.metering ?? -160;
        const isSpeaking = db > SILENCE_THRESHOLD_DB;

        if (isSpeaking) {
          // User is speaking — clear any pending silence timer
          speechStartedRef.current = true;
          if (silenceTimerRef.current) {
            clearTimeout(silenceTimerRef.current);
            silenceTimerRef.current = null;
          }
          setAppState('recording');
          setStatusText('Listening... (speaking)');
        } else if (speechStartedRef.current && !silenceTimerRef.current) {
          // User just went quiet after speaking — start 800ms silence timer
          silenceTimerRef.current = setTimeout(async () => {
            silenceTimerRef.current = null;
            const elapsed = Date.now() - recordingStartTime;

            // Only send if clip is long enough to be real speech
            if (elapsed < MIN_SPEECH_MS || !isListeningRef.current) return;

            // Stop mic and send the clip
            isListeningRef.current = false;
            speechStartedRef.current = false;
            const rec = recordingRef.current;
            recordingRef.current = null;
            if (rec) {
              try { await rec.stopAndUnloadAsync(); } catch {}
              await sendClip(rec);
            }
          }, SILENCE_DURATION_MS);
        }
      });

      // Set metering interval to 100ms
      await recording.setProgressUpdateInterval(100);

    } catch (e) {
      console.error('Start listening error:', e);
      isListeningRef.current = false;
    }
  }, [startPulse, sendClip]);

  // ─── Play MP3 audio from base64 ───────────────────────────────────────────
  const playAudio = useCallback(async (base64Mp3: string) => {
    // Kill mic BEFORE playing — no echo
    await stopMic();
    isPlayingRef.current = true;

    try {
      if (soundRef.current) {
        await soundRef.current.unloadAsync();
        soundRef.current = null;
      }
      const uri = `${FileSystem.cacheDirectory}caroline_${Date.now()}.mp3`;
      await FileSystem.writeAsStringAsync(uri, base64Mp3, {
        encoding: FileSystem.EncodingType.Base64,
      });
      await Audio.setAudioModeAsync({
        allowsRecordingIOS: false,
        playsInSilentModeIOS: true,
        staysActiveInBackground: false,
      });

      const { sound } = await Audio.Sound.createAsync({ uri }, { shouldPlay: true });
      soundRef.current = sound;
      setAppState('speaking');
      setStatusText('Caroline is speaking...');
      stopPulse();

      sound.setOnPlaybackStatusUpdate(async (status) => {
        if (status.isLoaded && status.didJustFinish) {
          isPlayingRef.current = false;
          try { await FileSystem.deleteAsync(uri, { idempotent: true }); } catch {}
          // Resume listening after Caroline finishes
          if (wsRef.current?.readyState === WebSocket.OPEN) {
            await startListening();
          }
        }
      });
    } catch (e) {
      console.error('Playback error:', e);
      isPlayingRef.current = false;
      if (wsRef.current?.readyState === WebSocket.OPEN) {
        await startListening();
      }
    }
  }, [stopMic, stopPulse, startListening]);

  // ─── Connect to server WebSocket ─────────────────────────────────────────
  const connect = useCallback(async () => {
    if (appState !== 'idle' && appState !== 'error') return;
    Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Medium);
    setAppState('connecting');
    setStatusText('Connecting to Caroline...');

    const { status } = await Audio.requestPermissionsAsync();
    if (status !== 'granted') {
      setAppState('error');
      setStatusText('Microphone permission denied. Tap to retry.');
      Alert.alert('Permission Required', 'Caroline needs microphone access to hear you.');
      return;
    }

    const ws = new WebSocket(WS_URL);
    wsRef.current = ws;

    ws.onopen = async () => {
      console.log('WebSocket connected');
      addMessage('assistant', "Hey! I'm Caroline. What do you need?");
      await startListening();
    };

    ws.onmessage = async (event) => {
      try {
        const msg = JSON.parse(event.data);
        console.log('WS message:', msg.type);

        if (msg.type === 'transcript_user' && msg.text) {
          addMessage('user', msg.text);
        } else if (msg.type === 'transcript_assistant' && msg.text) {
          addMessage('assistant', msg.text);
        } else if (msg.type === 'audio' && msg.data) {
          // Caroline's voice — play it (mic is killed inside playAudio)
          await playAudio(msg.data);
        } else if (msg.type === 'speaking_start') {
          // Belt-and-suspenders: also kill mic here in case playAudio hasn't fired yet
          await stopMic();
          isPlayingRef.current = true;
          setAppState('speaking');
          setStatusText('Caroline is speaking...');
          stopPulse();
        } else if (msg.type === 'speech_started') {
          // xAI VAD detected user speaking — interrupt Caroline if she's talking
          if (soundRef.current) {
            try { await soundRef.current.stopAsync(); } catch {}
          }
          isPlayingRef.current = false;
        } else if (msg.type === 'error') {
          console.error('Server error:', msg.message);
          setStatusText(`Error: ${msg.message}`);
          setAppState('error');
          stopPulse();
        }
      } catch (e) {
        console.error('WS parse error:', e);
      }
    };

    ws.onerror = (e) => {
      console.error('WS error:', e);
      setAppState('error');
      setStatusText('Connection error. Tap to reconnect.');
      stopPulse();
    };

    ws.onclose = async () => {
      console.log('WS closed');
      await stopMic();
      isPlayingRef.current = false;
      setAppState('idle');
      setStatusText('Tap to talk to Caroline');
      stopPulse();
    };
  }, [appState, addMessage, playAudio, startListening, stopMic, stopPulse]);

  // ─── Disconnect ───────────────────────────────────────────────────────────
  const disconnect = useCallback(async () => {
    await stopMic();
    isPlayingRef.current = false;
    if (soundRef.current) {
      try { await soundRef.current.unloadAsync(); } catch {}
      soundRef.current = null;
    }
    if (wsRef.current) {
      wsRef.current.close();
      wsRef.current = null;
    }
    setAppState('idle');
    setStatusText('Tap to talk to Caroline');
    stopPulse();
    Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Light);
  }, [stopMic, stopPulse]);

  const handleMainButton = useCallback(() => {
    if (appState === 'idle' || appState === 'error') connect();
    else disconnect();
  }, [appState, connect, disconnect]);

  const getButtonColor = () => {
    switch (appState) {
      case 'connecting':  return '#F59E0B';
      case 'listening':   return '#10B981';
      case 'recording':   return '#EF4444';
      case 'processing':  return '#F59E0B';
      case 'speaking':    return '#8B5CF6';
      case 'error':       return '#EF4444';
      default:            return '#6366F1';
    }
  };

  const getButtonIcon = (): any => {
    switch (appState) {
      case 'connecting':  return 'hourglass-empty';
      case 'listening':   return 'mic';
      case 'recording':   return 'mic';
      case 'processing':  return 'hourglass-empty';
      case 'speaking':    return 'volume-up';
      case 'error':       return 'refresh';
      default:            return 'mic-none';
    }
  };

  return (
    <View style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor="#0F0F1A" />
      <View style={styles.header}>
        <View style={styles.headerLeft}>
          <Text style={styles.headerTitle}>Caroline</Text>
          <View style={[styles.statusDot, {
            backgroundColor: serverOnline === true ? '#10B981' : serverOnline === false ? '#EF4444' : '#6B7280',
          }]} />
        </View>
        <TouchableOpacity onPress={() => setSettingsVisible(true)} style={styles.settingsBtn}>
          <MaterialIcons name="settings" size={22} color="#9CA3AF" />
        </TouchableOpacity>
      </View>

      <ScrollView
        ref={scrollRef}
        style={styles.messages}
        contentContainerStyle={styles.messagesContent}
        showsVerticalScrollIndicator={false}
      >
        {messages.length === 0 && (
          <View style={styles.emptyState}>
            <MaterialIcons name="chat-bubble-outline" size={48} color="#374151" />
            <Text style={styles.emptyText}>Start a conversation with Caroline</Text>
          </View>
        )}
        {messages.map(msg => (
          <View key={msg.id} style={[styles.bubble, msg.role === 'user' ? styles.userBubble : styles.assistantBubble]}>
            <Text style={[styles.bubbleText, msg.role === 'user' ? styles.userText : styles.assistantText]}>
              {msg.content}
            </Text>
            <Text style={styles.timestamp}>
              {msg.timestamp.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
            </Text>
          </View>
        ))}
      </ScrollView>

      <Text style={styles.statusText}>{statusText}</Text>

      <View style={styles.buttonContainer}>
        <Animated.View style={{ transform: [{ scale: pulseAnim }] }}>
          <TouchableOpacity
            style={[styles.mainButton, { backgroundColor: getButtonColor() }]}
            onPress={handleMainButton}
            activeOpacity={0.8}
          >
            {(appState === 'connecting' || appState === 'processing') ? (
              <ActivityIndicator size="large" color="#fff" />
            ) : (
              <MaterialIcons name={getButtonIcon()} size={40} color="#fff" />
            )}
          </TouchableOpacity>
        </Animated.View>
        {(appState === 'listening' || appState === 'recording' || appState === 'speaking') && (
          <Text style={styles.tapToStop}>Tap to stop</Text>
        )}
      </View>

      <Modal visible={settingsVisible} transparent animationType="slide" onRequestClose={() => setSettingsVisible(false)}>
        <Pressable style={styles.modalOverlay} onPress={() => setSettingsVisible(false)}>
          <View style={styles.modalContent}>
            <Text style={styles.modalTitle}>Caroline Settings</Text>
            <View style={styles.settingRow}>
              <Text style={styles.settingLabel}>Server</Text>
              <Text style={styles.settingValue}>Railway (always-on)</Text>
            </View>
            <View style={styles.settingRow}>
              <Text style={styles.settingLabel}>Status</Text>
              <Text style={[styles.settingValue, { color: serverOnline ? '#10B981' : '#EF4444' }]}>
                {serverOnline === null ? 'Checking...' : serverOnline ? 'Online' : 'Offline'}
              </Text>
            </View>
            <View style={styles.settingRow}>
              <Text style={styles.settingLabel}>Voice Engine</Text>
              <Text style={styles.settingValue}>xAI Realtime (Ara)</Text>
            </View>
            <View style={styles.settingRow}>
              <Text style={styles.settingLabel}>VAD Silence</Text>
              <Text style={styles.settingValue}>800ms</Text>
            </View>
            <TouchableOpacity style={styles.modalButton} onPress={() => { checkServer(); setSettingsVisible(false); }}>
              <Text style={styles.modalButtonText}>Refresh Server Status</Text>
            </TouchableOpacity>
            <TouchableOpacity style={[styles.modalButton, { backgroundColor: '#374151', marginTop: 4 }]} onPress={() => setSettingsVisible(false)}>
              <Text style={styles.modalButtonText}>Close</Text>
            </TouchableOpacity>
          </View>
        </Pressable>
      </Modal>
    </View>
  );
}

const styles = StyleSheet.create({
  container:       { flex: 1, backgroundColor: '#0F0F1A' },
  header:          { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', paddingHorizontal: 20, paddingTop: 56, paddingBottom: 12, borderBottomWidth: 1, borderBottomColor: '#1F2937' },
  headerLeft:      { flexDirection: 'row', alignItems: 'center', gap: 8 },
  headerTitle:     { fontSize: 24, fontWeight: '700', color: '#F9FAFB', letterSpacing: -0.5 },
  statusDot:       { width: 10, height: 10, borderRadius: 5 },
  settingsBtn:     { padding: 4 },
  messages:        { flex: 1 },
  messagesContent: { padding: 16, gap: 8 },
  emptyState:      { flex: 1, alignItems: 'center', justifyContent: 'center', paddingTop: 80, gap: 12 },
  emptyText:       { color: '#6B7280', fontSize: 15, textAlign: 'center' },
  bubble:          { maxWidth: SCREEN_WIDTH * 0.78, borderRadius: 16, padding: 12, marginBottom: 4 },
  userBubble:      { alignSelf: 'flex-end', backgroundColor: '#1D4ED8' },
  assistantBubble: { alignSelf: 'flex-start', backgroundColor: '#1F2937' },
  bubbleText:      { fontSize: 15, lineHeight: 22 },
  userText:        { color: '#F9FAFB' },
  assistantText:   { color: '#E5E7EB' },
  timestamp:       { fontSize: 11, marginTop: 4, opacity: 0.5, color: '#9CA3AF' },
  statusText:      { textAlign: 'center', color: '#9CA3AF', fontSize: 14, paddingHorizontal: 20, paddingVertical: 8 },
  buttonContainer: { alignItems: 'center', paddingBottom: 48, paddingTop: 8 },
  mainButton:      { width: 80, height: 80, borderRadius: 40, justifyContent: 'center', alignItems: 'center', elevation: 8, shadowColor: '#000', shadowOffset: { width: 0, height: 4 }, shadowOpacity: 0.3, shadowRadius: 8 },
  tapToStop:       { color: '#6B7280', fontSize: 13, marginTop: 10 },
  modalOverlay:    { flex: 1, backgroundColor: 'rgba(0,0,0,0.6)', justifyContent: 'flex-end' },
  modalContent:    { backgroundColor: '#1F2937', borderTopLeftRadius: 20, borderTopRightRadius: 20, padding: 24, gap: 12 },
  modalTitle:      { fontSize: 18, fontWeight: '700', color: '#F9FAFB', marginBottom: 4 },
  settingRow:      { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', paddingVertical: 8, borderBottomWidth: 1, borderBottomColor: '#374151' },
  settingLabel:    { color: '#9CA3AF', fontSize: 14 },
  settingValue:    { color: '#F9FAFB', fontSize: 14, fontWeight: '500' },
  modalButton:     { backgroundColor: '#6366F1', borderRadius: 10, padding: 14, alignItems: 'center', marginTop: 8 },
  modalButtonText: { color: '#fff', fontWeight: '600', fontSize: 15 },
});
