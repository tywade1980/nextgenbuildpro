# Caroline AI API Reference

The Caroline AI server runs on RunPod pod `dmed1ybt9cju4h` on port `8000`.

**Base URL:** `http://dmed1ybt9cju4h.runpod.net:8000`

## Endpoints

### `POST /ping`
Health check endpoint.
- **Request:** `{}`
- **Response:** `{"status": "ok", "model": "dolphin-mistral"}`

### `POST /chat`
Send a message to Caroline and receive a response.
- **Request:**
  ```json
  {
    "prompt": "Your message here",
    "system_prompt": "Optional system override",
    "temperature": 0.7,
    "max_tokens": 512
  }
  ```
- **Response:**
  ```json
  {
    "response": "Caroline's reply",
    "tokens_used": 128
  }
  ```

### `POST /sync`
Sync the Wade Global State to Caroline's context window.
- **Request:** Full WGS JSON object
- **Response:** `{"status": "synced", "context_tokens": 1024}`

## RunPod Server Setup

If Caroline is offline, SSH into the RunPod pod and run:

```bash
bash /workspace/setup.sh
```

Then start the server:

```bash
bash /workspace/start.sh
```

The server will be available on port `8000`.

## ElevenLabs Voice

- **Voice ID:** `wvVfSeWpAEhEqciDp1gK`
- **Recommended Model:** `eleven_turbo_v2_5` (low latency, ideal for real-time voice)
- **API Key:** Set as `ELEVENLABS_API_KEY` environment variable
