deep gram vopice agent and sdk
import { createClient } from "@deepgram/sdk";
import { AgentEvents } from "@deepgram/sdk";

const deepgram = createClient(DEEPGRAM_API_KEY);

// Create an agent connection
const agent = deepgram.agent();

// Set up event handlers
agent.on(AgentEvents.Open, () => {
  console.log("Connection opened");

  // Configure the agent once connection is established
  agent.configure({
    audio: {
      input: {
        encoding: "linear16",
        sampleRate: 24000,
      },
      output: {
        encoding: "mp3",
        sample_rate: 24000,
        bitrate: 48000,
        container: "none",
      },
    },
    agent: {
      language: "en",
      listen: {
        provider: {
          type: "deepgram",
          model: "nova-3",
        },
      },
      think: {
        provider: {
          type: "open_ai",
          model: "gpt-4-mini",
          temperature: 0.7,
        },
        prompt: "You are a helpful AI assistant. Keep responses brief and friendly.",
      },
      speak: {
        provider: {
          type: "deepgram",
          model: "aura-2-thalia-en",
        },
      },
    },
  });
});

// Handle agent responses
agent.on(AgentEvents.AgentStartedSpeaking, (data) => {
  console.log("Agent started speaking:", data["total_latency"]);
});

agent.on(AgentEvents.ConversationText, (message) => {
  console.log(`${message.role} said: ${message.content}`);
});

agent.on(AgentEvents.Audio, (audio) => {
  // Handle audio data from the agent
  playAudio(audio); // Your audio playback implementation
});

agent.on(AgentEvents.Error, (error) => {
  console.error("Error:", error);
});

agent.on(AgentEvents.Close, () => {
  console.log("Connection closed");
});

// Send audio data
function sendAudioData(audioData) {
  agent.send(audioData);
}

// Keep the connection alive
setInterval(() => {
  agent.keepAlive();
}, 8000);
