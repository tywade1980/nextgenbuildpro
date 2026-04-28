/**
 * Caroline xAI Realtime WebSocket Handler
 *
 * Proxies client WebSocket connections to the xAI Realtime API.
 * Ported from packages/caroline-ai/server/main.py WebSocket endpoint.
 *
 * Flow: client ← (ws) → this handler ← (ws) → xAI Realtime API
 */

const WebSocket = require('ws')

const XAI_REALTIME_URL = 'wss://api.x.ai/v1/realtime'

const CAROLINE_INSTRUCTIONS =
  'You are Caroline, a sharp, warm AI assistant for Wade — a master carpenter in Columbus OH. ' +
  'Voice-first: keep responses concise and natural for speech. No bullet points, no markdown. ' +
  'Be direct, confident, and a little light-hearted.'

/**
 * @param {WebSocket} clientWs
 * @param {import('http').IncomingMessage} req
 */
function handleCarolineWebSocket(clientWs, req) {
  const apiKey = process.env.XAI_API_KEY
  if (!apiKey) {
    clientWs.close(1011, 'XAI_API_KEY not configured')
    return
  }

  const upstreamWs = new WebSocket(XAI_REALTIME_URL, {
    headers: {
      Authorization: `Bearer ${apiKey}`,
      'Content-Type': 'application/json',
    },
  })

  upstreamWs.on('open', () => {
    // Send session config with Caroline's instructions
    const sessionUpdate = {
      type: 'session.update',
      session: {
        modalities: ['text', 'audio'],
        instructions: CAROLINE_INSTRUCTIONS,
        voice: 'alloy',
        input_audio_format: 'pcm16',
        output_audio_format: 'pcm16',
        input_audio_transcription: { model: 'whisper-1' },
        turn_detection: {
          type: 'server_vad',
          threshold: 0.5,
          prefix_padding_ms: 300,
          silence_duration_ms: 800,
        },
      },
    }
    upstreamWs.send(JSON.stringify(sessionUpdate))
  })

  // Relay upstream → client
  upstreamWs.on('message', (data) => {
    if (clientWs.readyState === WebSocket.OPEN) {
      clientWs.send(data)
    }
  })

  // Relay client → upstream
  clientWs.on('message', (data) => {
    if (upstreamWs.readyState === WebSocket.OPEN) {
      upstreamWs.send(data)
    }
  })

  upstreamWs.on('error', (err) => {
    console.error('[Caroline WS] upstream error:', err.message)
    if (clientWs.readyState === WebSocket.OPEN) {
      clientWs.close(1011, 'upstream error')
    }
  })

  clientWs.on('close', () => {
    if (upstreamWs.readyState === WebSocket.OPEN) {
      upstreamWs.close()
    }
  })

  upstreamWs.on('close', () => {
    if (clientWs.readyState === WebSocket.OPEN) {
      clientWs.close()
    }
  })

  clientWs.on('error', (err) => {
    console.error('[Caroline WS] client error:', err.message)
    if (upstreamWs.readyState === WebSocket.OPEN) {
      upstreamWs.close()
    }
  })
}

module.exports = { handleCarolineWebSocket }
