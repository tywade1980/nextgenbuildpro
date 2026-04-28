/**
 * Voice & Telephony Unified Gateway
 *
 * Consolidates all voice/telephony services from:
 *   - nextgentele  (SIP/WebRTC/IVR/carrier backend)
 *   - caroline-server-v2  (xAI Realtime WebSocket proxy)
 *   - Aria-voice backend  (FastAPI routes ported to Node.js bridge)
 *   - DG-voice  (Deepgram Voice Agent SDK)
 *
 * Routing:
 *   /api/telephony/*   → nextgentele SIP/WebRTC stack
 *   /ws/voice          → xAI Realtime proxy (Caroline)
 *   /api/aria/*        → Aria-voice Python sidecar
 *   /api/deepgram/*    → Deepgram Voice Agent
 */

const express = require('express')
const { createServer } = require('http')
const { WebSocketServer } = require('ws')
const { createProxyMiddleware } = require('http-proxy-middleware')
const path = require('path')

const app = express()
const server = createServer(app)

app.use(express.json())

// ─── Health & version ───────────────────────────────────────────────────────

app.get('/health', (req, res) => {
  res.json({
    status: 'ok',
    version: '1.0.0',
    services: {
      sip: process.env.SIP_ENABLED === 'true',
      webrtc: process.env.WEBRTC_ENABLED === 'true',
      caroline_realtime: !!process.env.XAI_API_KEY,
      aria: !!process.env.ARIA_BACKEND_URL,
      deepgram: !!process.env.DEEPGRAM_API_KEY,
    }
  })
})

// ─── Nextgentele SIP/WebRTC routes ──────────────────────────────────────────

const telephonyRoutes = require('./src/routes')
app.use('/api/telephony', telephonyRoutes)

// ─── Aria-voice Python sidecar proxy ────────────────────────────────────────

if (process.env.ARIA_BACKEND_URL) {
  app.use('/api/aria', createProxyMiddleware({
    target: process.env.ARIA_BACKEND_URL,
    changeOrigin: true,
    pathRewrite: { '^/api/aria': '' },
  }))
}

// ─── Deepgram Voice Agent endpoint ──────────────────────────────────────────

if (process.env.DEEPGRAM_API_KEY) {
  const { createClient } = require('@deepgram/sdk')

  app.post('/api/deepgram/agent', async (req, res) => {
    const { systemPrompt, voice = 'aura-2-thalia-en', model = 'nova-3' } = req.body
    // Returns connection config — actual audio is WebSocket-based
    res.json({
      agent_config: {
        listen: { provider: { type: 'deepgram', model } },
        think: {
          provider: { type: 'open_ai', model: 'gpt-4o-mini' },
          instructions: systemPrompt || DEFAULT_DEEPGRAM_SYSTEM_PROMPT
        },
        speak: { provider: { type: 'deepgram', model: voice } }
      }
    })
  })
}

// ─── Caroline xAI Realtime WebSocket proxy ──────────────────────────────────

const { handleCarolineWebSocket } = require('./caroline-ws-handler')

const wss = new WebSocketServer({ server, path: '/ws/voice' })
wss.on('connection', handleCarolineWebSocket)

// ─── Caroline token endpoint ─────────────────────────────────────────────────

app.post('/token', async (req, res) => {
  try {
    const response = await fetch('https://api.x.ai/v1/realtime/client_secrets', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${process.env.XAI_API_KEY}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ ttl: 300 })
    })
    const data = await response.json()
    res.json(data)
  } catch (err) {
    res.status(500).json({ error: err.message })
  }
})

// ─── LLM relay (OpenRouter) ──────────────────────────────────────────────────

app.post('/llm', async (req, res) => {
  try {
    const response = await fetch('https://openrouter.ai/api/v1/chat/completions', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${process.env.OPENROUTER_API_KEY}`,
        'Content-Type': 'application/json',
        'HTTP-Referer': 'https://caroline-ai.app'
      },
      body: JSON.stringify(req.body)
    })
    const data = await response.json()
    res.json(data)
  } catch (err) {
    res.status(500).json({ error: err.message })
  }
})

// ─── TTS relay (ElevenLabs) ───────────────────────────────────────────────────

app.post('/tts', async (req, res) => {
  const { voice_id, text } = req.body
  try {
    const response = await fetch(
      `https://api.elevenlabs.io/v1/text-to-speech/${voice_id}`,
      {
        method: 'POST',
        headers: {
          'xi-api-key': process.env.ELEVENLABS_API_KEY,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ text, model_id: 'eleven_turbo_v2_5' })
      }
    )
    const audio = await response.arrayBuffer()
    res.set('Content-Type', 'audio/mpeg')
    res.send(Buffer.from(audio))
  } catch (err) {
    res.status(500).json({ error: err.message })
  }
})

// ─── Start ────────────────────────────────────────────────────────────────────

const PORT = process.env.PORT || 3001
server.listen(PORT, () => {
  console.log(`[VoiceTelephony Gateway] listening on port ${PORT}`)
})

module.exports = { app, server }

const DEFAULT_DEEPGRAM_SYSTEM_PROMPT = `You are an intelligent AI receptionist and voice assistant for NextGen BuildPro.
Handle calls professionally. Route construction inquiries appropriately.
Be concise — this is a voice interface, so avoid long responses.`
