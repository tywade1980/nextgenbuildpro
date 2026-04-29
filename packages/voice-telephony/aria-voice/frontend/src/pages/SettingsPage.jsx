import { useState, useEffect } from "react";
import { Save, Server, Mic, Volume2, Key, RefreshCw } from "lucide-react";
import axios from "axios";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Label } from "@/components/ui/label";
import { Slider } from "@/components/ui/slider";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Separator } from "@/components/ui/separator";

const API = `${process.env.REACT_APP_BACKEND_URL}/api`;

const voiceOptions = [
  { value: "alloy", label: "Alloy - Neutral (OpenAI)" },
  { value: "ash", label: "Ash - Clear (OpenAI)" },
  { value: "coral", label: "Coral - Warm (OpenAI)" },
  { value: "echo", label: "Echo - Calm (OpenAI)" },
  { value: "fable", label: "Fable - Expressive (OpenAI)" },
  { value: "nova", label: "Nova - Energetic (OpenAI)" },
  { value: "onyx", label: "Onyx - Deep (OpenAI)" },
  { value: "sage", label: "Sage - Wise (OpenAI)" },
  { value: "shimmer", label: "Shimmer - Cheerful (OpenAI)" },
];

const ttsEngineOptions = [
  { value: "openai", label: "OpenAI TTS" },
  { value: "xtts", label: "XTTS v2 (Runpod)" },
  { value: "fish", label: "Fish Speech 1.5 (Runpod)" },
  { value: "styletts2", label: "StyleTTS2 (Runpod)" },
];

const llmBackendOptions = [
  { value: "openai", label: "OpenAI GPT-5.2 (Emergent)" },
  { value: "runpod", label: "Mistral 7B (Runpod)" },
];

const modelOptions = [
  { value: "gpt-5.2", label: "GPT-5.2 (Default)" },
  { value: "gpt-4o", label: "GPT-4o" },
  { value: "mistral-runpod", label: "Mistral (Runpod)" },
  { value: "claude-sonnet-4-5", label: "Claude Sonnet 4.5" },
];

export default function SettingsPage() {
  const [settings, setSettings] = useState({
    runpod_endpoint: "",
    runpod_api_key: "",
    default_model: "gpt-5.2",
    default_voice: "nova",
    tts_speed: 1.0,
    tts_engine: "openai",
    llm_backend: "openai"
  });
  const [isSaving, setIsSaving] = useState(false);
  const [isTesting, setIsTesting] = useState(false);

  useEffect(() => {
    fetchSettings();
  }, []);

  const fetchSettings = async () => {
    try {
      const { data } = await axios.get(`${API}/settings`);
      setSettings({
        runpod_endpoint: data.runpod_endpoint || "",
        runpod_api_key: data.runpod_api_key || "",
        default_model: data.default_model || "gpt-5.2",
        default_voice: data.default_voice || "nova",
        tts_speed: data.tts_speed || 1.0,
        tts_engine: data.tts_engine || "openai",
        llm_backend: data.llm_backend || "openai"
      });
    } catch (error) {
      console.error("Failed to fetch settings:", error);
    }
  };

  const saveSettings = async () => {
    setIsSaving(true);
    try {
      await axios.put(`${API}/settings`, settings);
      toast.success("Settings saved successfully");
    } catch (error) {
      toast.error("Failed to save settings");
    } finally {
      setIsSaving(false);
    }
  };

  const testTTS = async () => {
    setIsTesting(true);
    try {
      const { data } = await axios.post(`${API}/tts/base64`, {
        text: "Hello! This is ARIA, your AI voice assistant. Settings test successful.",
        voice: settings.default_voice,
        speed: settings.tts_speed,
        engine: settings.tts_engine
      });

      const mimeType = data.format === "wav" ? "audio/wav" : "audio/mpeg";
      const audio = new Audio(`data:${mimeType};base64,${data.audio}`);
      audio.onended = () => setIsTesting(false);
      audio.onerror = () => {
        setIsTesting(false);
        toast.error("Audio playback failed");
      };
      await audio.play();
      toast.success(`TTS working (${data.engine})`);
    } catch (error) {
      toast.error("TTS test failed");
      setIsTesting(false);
    }
  };

  const testAPI = async () => {
    try {
      const { data } = await axios.get(`${API}/`);
      toast.success(`API connected: ${data.message}`);
    } catch (error) {
      toast.error("API connection failed");
    }
  };

  return (
    <div className="space-y-6 max-w-3xl">
      {/* Header */}
      <div>
        <h1 className="text-2xl lg:text-3xl font-heading font-semibold">
          Settings
        </h1>
        <p className="text-muted-foreground mt-1">
          Configure ARIA's behavior and integrations
        </p>
      </div>

      {/* Runpod Configuration */}
      <Card className="glass border-border/50" data-testid="runpod-settings">
        <CardHeader>
          <CardTitle className="font-heading flex items-center gap-2">
            <Server className="w-5 h-5 text-primary" />
            Runpod Configuration
          </CardTitle>
          <CardDescription>
            Configure your Runpod pod endpoint for TTS engines and Mistral LLM
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-2">
            <Label>Runpod Pod URL</Label>
            <Input
              value={settings.runpod_endpoint}
              onChange={(e) => setSettings({ ...settings, runpod_endpoint: e.target.value })}
              placeholder="http://your-pod-ip:8000"
              className="font-mono text-sm"
              data-testid="runpod-endpoint-input"
            />
            <p className="text-xs text-muted-foreground">
              URL of your ARIA container running on Runpod (Fish Speech, XTTS, StyleTTS2, Mistral)
            </p>
          </div>
          <div className="space-y-2">
            <Label>Runpod API Key (Optional)</Label>
            <Input
              type="password"
              value={settings.runpod_api_key}
              onChange={(e) => setSettings({ ...settings, runpod_api_key: e.target.value })}
              placeholder="Enter your Runpod API key"
              data-testid="runpod-key-input"
            />
          </div>
        </CardContent>
      </Card>

      {/* LLM Backend */}
      <Card className="glass border-border/50" data-testid="llm-settings">
        <CardHeader>
          <CardTitle className="font-heading flex items-center gap-2">
            <Key className="w-5 h-5 text-secondary" />
            LLM Backend
          </CardTitle>
          <CardDescription>
            Choose between OpenAI (Emergent) or Mistral (Runpod)
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <Select
            value={settings.llm_backend}
            onValueChange={(value) => setSettings({ ...settings, llm_backend: value })}
          >
            <SelectTrigger data-testid="llm-backend-select">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {llmBackendOptions.map((opt) => (
                <SelectItem key={opt.value} value={opt.value}>
                  {opt.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          {settings.llm_backend === "runpod" && !settings.runpod_endpoint && (
            <p className="text-xs text-orange-500">
              Configure Runpod endpoint above to use Mistral
            </p>
          )}
        </CardContent>
      </Card>

      {/* TTS Engine */}
      <Card className="glass border-border/50" data-testid="tts-engine-settings">
        <CardHeader>
          <CardTitle className="font-heading flex items-center gap-2">
            <Volume2 className="w-5 h-5 text-green-500" />
            TTS Engine
          </CardTitle>
          <CardDescription>
            Select text-to-speech engine
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <Select
            value={settings.tts_engine}
            onValueChange={(value) => setSettings({ ...settings, tts_engine: value })}
          >
            <SelectTrigger data-testid="tts-engine-select">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {ttsEngineOptions.map((opt) => (
                <SelectItem key={opt.value} value={opt.value}>
                  {opt.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          {settings.tts_engine !== "openai" && !settings.runpod_endpoint && (
            <p className="text-xs text-orange-500">
              Configure Runpod endpoint above to use {settings.tts_engine.toUpperCase()}
            </p>
          )}
        </CardContent>
      </Card>

      {/* Model Settings */}
      <Card className="glass border-border/50" data-testid="model-settings">
        <CardHeader>
          <CardTitle className="font-heading flex items-center gap-2">
            <Key className="w-5 h-5 text-secondary" />
            Default Model
          </CardTitle>
          <CardDescription>
            Select the default LLM for conversations
          </CardDescription>
        </CardHeader>
        <CardContent>
          <Select
            value={settings.default_model}
            onValueChange={(value) => setSettings({ ...settings, default_model: value })}
          >
            <SelectTrigger data-testid="model-select">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {modelOptions.map((opt) => (
                <SelectItem key={opt.value} value={opt.value}>
                  {opt.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </CardContent>
      </Card>

      {/* Voice Settings */}
      <Card className="glass border-border/50" data-testid="voice-settings">
        <CardHeader>
          <CardTitle className="font-heading flex items-center gap-2">
            <Volume2 className="w-5 h-5 text-green-500" />
            Voice Settings
          </CardTitle>
          <CardDescription>
            Configure text-to-speech voice and speed
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-6">
          <div className="space-y-2">
            <Label>Voice</Label>
            <Select
              value={settings.default_voice}
              onValueChange={(value) => setSettings({ ...settings, default_voice: value })}
            >
              <SelectTrigger data-testid="voice-select">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {voiceOptions.map((opt) => (
                  <SelectItem key={opt.value} value={opt.value}>
                    {opt.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          <div className="space-y-3">
            <div className="flex items-center justify-between">
              <Label>Speech Speed</Label>
              <span className="text-sm text-muted-foreground font-mono">
                {settings.tts_speed.toFixed(1)}x
              </span>
            </div>
            <Slider
              value={[settings.tts_speed]}
              onValueChange={([value]) => setSettings({ ...settings, tts_speed: value })}
              min={0.5}
              max={2.0}
              step={0.1}
              data-testid="speed-slider"
            />
            <div className="flex justify-between text-xs text-muted-foreground">
              <span>Slow (0.5x)</span>
              <span>Normal (1.0x)</span>
              <span>Fast (2.0x)</span>
            </div>
          </div>

          <Button
            variant="outline"
            onClick={testTTS}
            disabled={isTesting}
            className="w-full"
            data-testid="test-tts-btn"
          >
            <Mic className="w-4 h-4 mr-2" />
            {isTesting ? "Playing..." : "Test Voice"}
          </Button>
        </CardContent>
      </Card>

      {/* Actions */}
      <div className="flex gap-3">
        <Button
          onClick={saveSettings}
          disabled={isSaving}
          className="flex-1 bg-primary hover:bg-primary/90"
          data-testid="save-settings-btn"
        >
          <Save className="w-4 h-4 mr-2" />
          {isSaving ? "Saving..." : "Save Settings"}
        </Button>
        <Button
          variant="outline"
          onClick={testAPI}
          data-testid="test-api-btn"
        >
          <RefreshCw className="w-4 h-4 mr-2" />
          Test API
        </Button>
      </div>

      {/* System Info */}
      <Separator className="my-6" />
      
      <Card className="glass border-border/50">
        <CardContent className="pt-6">
          <div className="space-y-2 text-sm">
            <div className="flex justify-between">
              <span className="text-muted-foreground">Version</span>
              <span className="font-mono">1.0.0</span>
            </div>
            <div className="flex justify-between">
              <span className="text-muted-foreground">API Endpoint</span>
              <span className="font-mono text-xs truncate max-w-[200px]">
                {process.env.REACT_APP_BACKEND_URL}
              </span>
            </div>
            <div className="flex justify-between">
              <span className="text-muted-foreground">LLM Provider</span>
              <span className="font-mono">OpenAI (Emergent)</span>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
