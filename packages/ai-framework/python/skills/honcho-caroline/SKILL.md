---
name: honcho-caroline
description: "Honcho-powered persistent memory and reasoning layer for Caroline AI. Use for: ingesting conversation messages into Honcho, querying Honcho's synthesised model of Wade, syncing Honcho's reasoning into caroline_neuro_memory.json, and verifying Honcho connection health. This Node follows the Centauri Interlock Standard."
---

# Honcho-Caroline Memory Node

This skill wires [Honcho](https://honcho.dev) — Plastic Labs' AI-native memory and reasoning platform — directly into Caroline's memory system as a **Centauri Interlock Node**. Instead of storing flat facts, Honcho builds and maintains a continuously evolving psychological model of Wade across every session.

## Architecture

```
Caroline AI (RunPod)
        │
        ▼
honcho_caroline_node.py   ◄──── caroline_neuro_memory.json
        │                              ▲
        ▼                              │
  Honcho API                    action_sync()
  (Workspace: centauri-os)
  ├── Peer: wade
  └── Peer: caroline
```

## Core Actions

| Action | What It Does |
|---|---|
| `status` | Verify Honcho connection and workspace health |
| `ingest` | Write a conversation message (user or assistant) into Honcho |
| `query` | Ask Honcho's reasoning engine what it knows about Wade |
| `sync` | Pull Honcho's latest Wade-model into `caroline_neuro_memory.json` |

## Usage

### Check Connection
```bash
python3 /home/ubuntu/skills/honcho-caroline/scripts/honcho_caroline_node.py --action status
```

### Ingest a Message (Wade speaking)
```bash
python3 /home/ubuntu/skills/honcho-caroline/scripts/honcho_caroline_node.py \
  --action ingest --role user --content "I'm focused on finishing the Centauri OS today"
```

### Ingest a Message (Caroline responding)
```bash
python3 /home/ubuntu/skills/honcho-caroline/scripts/honcho_caroline_node.py \
  --action ingest --role assistant --content "Got it. I'll keep context tight on Centauri today."
```

### Query Honcho About Wade
```bash
python3 /home/ubuntu/skills/honcho-caroline/scripts/honcho_caroline_node.py \
  --action query --question "What are Wade's top priorities right now?"
```

### Sync Honcho → caroline_neuro_memory.json
```bash
python3 /home/ubuntu/skills/honcho-caroline/scripts/honcho_caroline_node.py --action sync
```

## Environment Variables

| Variable | Default | Description |
|---|---|---|
| `HONCHO_API_KEY` | *(set in script)* | Your Honcho API key |
| `HONCHO_WORKSPACE` | `centauri-os` | Honcho workspace ID |
| `CAROLINE_STATE_FILE` | `~/wade-global-state/caroline_neuro_memory.json` | Path to Centauri state file |

## Integration with Centauri Stack

This Node follows the **Centauri Interlock Standard**:

1. **State Check** — reads `caroline_neuro_memory.json` before executing
2. **Modular Logic** — all Honcho operations are self-contained
3. **Closed-Loop Broadcast** — all outputs are JSON-formatted and routed to `Command_Router`

### Recommended Session Workflow

```bash
# 1. Start of session — sync Honcho's latest Wade model into state
python3 honcho_caroline_node.py --action sync

# 2. During session — ingest key exchanges
python3 honcho_caroline_node.py --action ingest --role user --content "..."
python3 honcho_caroline_node.py --action ingest --role assistant --content "..."

# 3. On demand — query for context-aware responses
python3 honcho_caroline_node.py --action query --question "What does Wade need right now?"
```

## Honcho Workspace

- **Workspace**: `centauri-os`
- **Peers**: `wade` (user), `caroline` (assistant)
- **Default Session**: `caroline-memory-main`
- **Dashboard**: [app.honcho.dev](https://app.honcho.dev)
