import { useState, useEffect, useRef, useCallback } from "react";
import { Send, Trash2, Plus, Volume2, VolumeX } from "lucide-react";
import axios from "axios";
import { toast } from "sonner";
import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { ScrollArea } from "@/components/ui/scroll-area";
import VoiceOrb from "@/components/VoiceOrb";

const API = `${process.env.REACT_APP_BACKEND_URL}/api`;

export default function ConversationPage() {
  const [sessions, setSessions] = useState([]);
  const [currentSession, setCurrentSession] = useState(null);
  const [messages, setMessages] = useState([]);
  const [inputText, setInputText] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [voiceState, setVoiceState] = useState("idle");
  const [ttsEnabled, setTtsEnabled] = useState(true);
  const messagesEndRef = useRef(null);
  const mediaRecorderRef = useRef(null);
  const audioChunksRef = useRef([]);

  // Fetch sessions on mount
  useEffect(() => {
    fetchSessions();
  }, []);

  // Fetch messages when session changes
  useEffect(() => {
    if (currentSession) {
      fetchMessages(currentSession.id);
    } else {
      setMessages([]);
    }
  }, [currentSession]);

  // Scroll to bottom on new messages
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  const fetchSessions = async () => {
    try {
      const { data } = await axios.get(`${API}/sessions`);
      setSessions(data);
      if (data.length > 0 && !currentSession) {
        setCurrentSession(data[0]);
      }
    } catch (error) {
      console.error("Failed to fetch sessions:", error);
    }
  };

  const fetchMessages = async (sessionId) => {
    try {
      const { data } = await axios.get(`${API}/messages/${sessionId}`);
      setMessages(data);
    } catch (error) {
      console.error("Failed to fetch messages:", error);
    }
  };

  const createSession = async () => {
    try {
      const { data } = await axios.post(`${API}/sessions`);
      setSessions([data, ...sessions]);
      setCurrentSession(data);
      setMessages([]);
      toast.success("New conversation started");
    } catch (error) {
      toast.error("Failed to create conversation");
    }
  };

  const deleteSession = async (sessionId) => {
    try {
      await axios.delete(`${API}/sessions/${sessionId}`);
      setSessions(sessions.filter(s => s.id !== sessionId));
      if (currentSession?.id === sessionId) {
        setCurrentSession(sessions[0] || null);
      }
      toast.success("Conversation deleted");
    } catch (error) {
      toast.error("Failed to delete conversation");
    }
  };

  const sendMessage = async (text) => {
    if (!text.trim()) return;

    const userMessage = {
      id: Date.now().toString(),
      role: "user",
      content: text,
      timestamp: new Date().toISOString()
    };

    setMessages(prev => [...prev, userMessage]);
    setInputText("");
    setIsLoading(true);

    try {
      const { data } = await axios.post(`${API}/chat`, {
        content: text,
        session_id: currentSession?.id
      });

      if (!currentSession) {
        await fetchSessions();
      }

      setMessages(prev => [...prev, data]);

      // Play TTS if enabled
      if (ttsEnabled) {
        await playTTS(data.content);
      }
    } catch (error) {
      toast.error("Failed to send message");
      console.error(error);
    } finally {
      setIsLoading(false);
    }
  };

  const playTTS = async (text) => {
    try {
      setVoiceState("speaking");
      const { data } = await axios.post(`${API}/tts/base64`, {
        text: text.slice(0, 4000),
        voice: "nova",
        speed: 1.0
      });

      const audio = new Audio(`data:audio/mp3;base64,${data.audio}`);
      audio.onended = () => setVoiceState("idle");
      audio.onerror = () => setVoiceState("idle");
      await audio.play();
    } catch (error) {
      console.error("TTS error:", error);
      setVoiceState("idle");
    }
  };

  const startVoiceInput = useCallback(async () => {
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      const mediaRecorder = new MediaRecorder(stream, { mimeType: "audio/webm" });
      mediaRecorderRef.current = mediaRecorder;
      audioChunksRef.current = [];

      mediaRecorder.ondataavailable = (event) => {
        audioChunksRef.current.push(event.data);
      };

      mediaRecorder.onstop = async () => {
        const audioBlob = new Blob(audioChunksRef.current, { type: "audio/webm" });
        stream.getTracks().forEach(track => track.stop());

        try {
          const formData = new FormData();
          formData.append("file", audioBlob, "recording.webm");

          const { data } = await axios.post(`${API}/stt`, formData, {
            headers: { "Content-Type": "multipart/form-data" }
          });

          if (data.text) {
            setInputText(data.text);
            await sendMessage(data.text);
          }
        } catch (error) {
          toast.error("Failed to transcribe audio");
          console.error(error);
        }

        setVoiceState("idle");
      };

      mediaRecorder.start();
      setVoiceState("listening");
      toast.info("Listening...");
    } catch (error) {
      toast.error("Microphone access denied");
      console.error(error);
    }
  }, []);

  const stopVoiceInput = useCallback(() => {
    if (mediaRecorderRef.current && voiceState === "listening") {
      mediaRecorderRef.current.stop();
      toast.info("Processing...");
    }
  }, [voiceState]);

  const handleVoiceClick = () => {
    if (voiceState === "listening") {
      stopVoiceInput();
    } else if (voiceState === "idle") {
      startVoiceInput();
    }
  };

  const handleKeyPress = (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      sendMessage(inputText);
    }
  };

  return (
    <div className="h-[calc(100vh-4rem)] lg:h-[calc(100vh-2rem)] flex flex-col lg:flex-row gap-4">
      {/* Sessions Sidebar */}
      <Card className="lg:w-64 flex-shrink-0 glass border-border/50" data-testid="sessions-panel">
        <CardHeader className="p-4 border-b border-border/50">
          <div className="flex items-center justify-between">
            <CardTitle className="text-sm font-heading">Conversations</CardTitle>
            <Button
              size="icon"
              variant="ghost"
              onClick={createSession}
              className="h-8 w-8"
              data-testid="new-conversation-btn"
            >
              <Plus className="w-4 h-4" />
            </Button>
          </div>
        </CardHeader>
        <ScrollArea className="h-32 lg:h-[calc(100%-4rem)]">
          <div className="p-2 space-y-1">
            {sessions.map((session) => (
              <div
                key={session.id}
                onClick={() => setCurrentSession(session)}
                className={cn(
                  "flex items-center justify-between p-2 rounded-lg cursor-pointer transition-colors",
                  currentSession?.id === session.id
                    ? "bg-primary/10 border border-primary/30"
                    : "hover:bg-muted/50"
                )}
                data-testid={`session-${session.id}`}
              >
                <span className="text-sm truncate">{session.name}</span>
                <Button
                  size="icon"
                  variant="ghost"
                  onClick={(e) => {
                    e.stopPropagation();
                    deleteSession(session.id);
                  }}
                  className="h-6 w-6 opacity-50 hover:opacity-100"
                  data-testid={`delete-session-${session.id}`}
                >
                  <Trash2 className="w-3 h-3" />
                </Button>
              </div>
            ))}
            {sessions.length === 0 && (
              <p className="text-sm text-muted-foreground text-center py-4">
                No conversations yet
              </p>
            )}
          </div>
        </ScrollArea>
      </Card>

      {/* Main Chat Area */}
      <Card className="flex-1 flex flex-col glass border-border/50" data-testid="chat-panel">
        {/* Messages */}
        <ScrollArea className="flex-1 p-4">
          <div className="space-y-4">
            {messages.length === 0 && (
              <div className="flex flex-col items-center justify-center h-full py-12 text-center">
                <VoiceOrb state="idle" onClick={handleVoiceClick} size="xl" />
                <h2 className="mt-6 text-xl font-heading font-medium neon-text-cyan">
                  Hello, I'm ARIA
                </h2>
                <p className="mt-2 text-muted-foreground max-w-md">
                  Your AI voice assistant and agent orchestrator. 
                  Tap the orb or type below to start.
                </p>
              </div>
            )}

            {messages.map((msg, idx) => (
              <div
                key={msg.id || idx}
                className={cn(
                  "message-animate flex",
                  msg.role === "user" ? "justify-end" : "justify-start"
                )}
                data-testid={`message-${idx}`}
              >
                <div
                  className={cn(
                    "max-w-[80%] rounded-2xl px-4 py-3",
                    msg.role === "user"
                      ? "bg-primary text-primary-foreground rounded-br-md"
                      : "bg-card border border-border/50 rounded-bl-md"
                  )}
                >
                  <p className="text-sm whitespace-pre-wrap">{msg.content}</p>
                </div>
              </div>
            ))}

            {isLoading && (
              <div className="flex justify-start">
                <div className="bg-card border border-border/50 rounded-2xl rounded-bl-md px-4 py-3">
                  <div className="flex gap-1">
                    <span className="w-2 h-2 bg-primary rounded-full typing-dot" />
                    <span className="w-2 h-2 bg-primary rounded-full typing-dot" />
                    <span className="w-2 h-2 bg-primary rounded-full typing-dot" />
                  </div>
                </div>
              </div>
            )}
            <div ref={messagesEndRef} />
          </div>
        </ScrollArea>

        {/* Input Area */}
        <div className="p-4 border-t border-border/50">
          <div className="flex items-center gap-3">
            <VoiceOrb
              state={voiceState}
              onClick={handleVoiceClick}
              size="sm"
            />
            <div className="flex-1 flex gap-2">
              <Input
                value={inputText}
                onChange={(e) => setInputText(e.target.value)}
                onKeyPress={handleKeyPress}
                placeholder="Type a message..."
                className="flex-1 bg-background/50"
                disabled={isLoading}
                data-testid="chat-input"
              />
              <Button
                onClick={() => sendMessage(inputText)}
                disabled={!inputText.trim() || isLoading}
                className="bg-primary hover:bg-primary/90"
                data-testid="send-btn"
              >
                <Send className="w-4 h-4" />
              </Button>
            </div>
            <Button
              size="icon"
              variant="ghost"
              onClick={() => setTtsEnabled(!ttsEnabled)}
              className={cn(ttsEnabled ? "text-primary" : "text-muted-foreground")}
              data-testid="tts-toggle-btn"
            >
              {ttsEnabled ? <Volume2 className="w-5 h-5" /> : <VolumeX className="w-5 h-5" />}
            </Button>
          </div>
        </div>
      </Card>
    </div>
  );
}
