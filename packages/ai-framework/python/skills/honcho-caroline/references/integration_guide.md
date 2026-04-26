# Honcho-Caroline Integration Guide

## Why Honcho Replaces Static Memory

The previous Caroline memory system stored context as a flat JSON file (`caroline_neuro_memory.json`). This approach has three limitations:

1. **No reasoning** — it stores what was said, not what it means about Wade
2. **No persistence across sessions** — context resets unless manually synced
3. **No synthesis** — it cannot connect patterns across conversations

Honcho solves all three. It acts as a *detective* — reasoning about new and existing evidence to form conclusions about Wade that go beyond what was explicitly stated.

## Data Flow

```
Wade speaks to Caroline
        │
        ▼
honcho_caroline_node.py --action ingest --role user
        │
        ▼
Honcho API (workspace: centauri-os)
  Peer: wade ──► Honcho reasons continuously in background
  Peer: caroline
        │
        ▼ (on --action sync)
caroline_neuro_memory.json ◄── Honcho's synthesised Wade model
        │
        ▼
Centauri Command_Router / NeuroRank / Caroline Bridge
```

## NeuroRank Alignment

Honcho's internal architecture maps directly to NeuroRank's cognitive regions:

| NeuroRank Region | Honcho Equivalent |
|---|---|
| Logic Core (35%) | Factual conclusions about Wade's projects and decisions |
| Emotional Engine (25%) | Tone and emotional state inference from conversation patterns |
| Memory Bank (20%) | Cross-session persistent peer representations |
| Intuition Layer (10%) | Pattern recognition across sessions (e.g. Wade's side-project enthusiasm) |
| Error Correction (10%) | Honcho's self-scoring and re-reasoning on contradictory signals |

## caroline_neuro_memory.json Schema After Sync

```json
{
  "status": "...",
  "context": "<Honcho 5-sentence Wade summary>",
  "NeuroRank": 0,
  "honcho_context": "<same summary>",
  "honcho_raw": {
    "workspace": "centauri-os",
    "peer": "wade",
    "summary": "...",
    "synced_at": "2026-04-06T..."
  },
  "honcho_synced_at": "2026-04-06T..."
}
```

## Recommended Automation

Add the sync call to Caroline's startup routine in `caroline_bridge.py` or the Centauri boot sequence:

```python
import subprocess
subprocess.run([
    "python3",
    "/home/ubuntu/skills/honcho-caroline/scripts/honcho_caroline_node.py",
    "--action", "sync"
])
```

This ensures every Caroline session starts with the freshest possible model of Wade.
