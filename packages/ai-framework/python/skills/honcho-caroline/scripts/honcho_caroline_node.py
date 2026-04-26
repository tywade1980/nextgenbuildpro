"""
honcho_caroline_node.py
========================
Centauri OS — Interlock Standard Node
Node ID: honcho_caroline_node

Purpose:
    Bridges Caroline AI's memory system to Honcho's persistent, reasoned
    user-representation platform. This Node replaces the flat
    caroline_neuro_memory.json context with a live, evolving psychological
    model of Wade maintained by Honcho across all sessions.

Capabilities:
    - ingest:   Write a new conversation message into Honcho (user or assistant)
    - query:    Ask Honcho what it knows about Wade right now
    - sync:     Pull Honcho's latest reasoning into caroline_neuro_memory.json
                so the rest of the Centauri stack can consume it
    - status:   Verify the Honcho connection and workspace health

Usage:
    python3 honcho_caroline_node.py --action ingest --role user --content "Hello"
    python3 honcho_caroline_node.py --action query  --question "What motivates Wade?"
    python3 honcho_caroline_node.py --action sync
    python3 honcho_caroline_node.py --action status
"""

import argparse
import json
import os
import sys
from datetime import datetime

# ── Centauri Interlock Standard ──────────────────────────────────────────────
STATE_FILE = os.environ.get(
    "CAROLINE_STATE_FILE",
    os.path.expanduser("~/wade-global-state/caroline_neuro_memory.json"),
)

# ── Honcho Configuration ─────────────────────────────────────────────────────
HONCHO_API_KEY   = os.environ.get("HONCHO_API_KEY", os.environ.get("HONCHO_API_KEY", ""))
HONCHO_WORKSPACE = os.environ.get("HONCHO_WORKSPACE", "centauri-os")

# Peer identifiers — stable across all sessions
WADE_PEER_ID      = "wade"
CAROLINE_PEER_ID  = "caroline"

# Default session used when no explicit session is provided
DEFAULT_SESSION   = "caroline-memory-main"

NODE_ID = "honcho_caroline_node"


# ── Interlock Helpers ─────────────────────────────────────────────────────────

def read_system_state() -> dict:
    """
    Mandatory State Check (Centauri Interlock Standard).
    Reads caroline_neuro_memory.json before executing primary logic.
    """
    if not os.path.exists(STATE_FILE):
        return {"status": "initialized", "context": "fresh_start", "NeuroRank": 0}
    try:
        with open(STATE_FILE, "r") as f:
            return json.load(f)
    except (json.JSONDecodeError, OSError):
        return {"status": "error_reading_state", "context": "unknown", "NeuroRank": 0}


def broadcast_result(status: str, payload: dict = None, error: str = None) -> dict:
    """
    Closed-Loop Broadcast (Centauri Interlock Standard).
    Emits a standardised JSON result to stdout for the Command_Router.
    """
    result = {
        "node_id":   NODE_ID,
        "timestamp": datetime.now().isoformat(),
        "status":    status,
        "payload":   payload or {},
        "error":     error,
    }
    print(json.dumps(result, indent=2))
    return result


def write_state_update(context_summary: str, honcho_data: dict):
    """
    Persists Honcho's latest reasoning back into caroline_neuro_memory.json
    so the full Centauri stack (NeuroRank, Command_Router, Caroline bridge)
    can consume it without knowing about Honcho directly.
    """
    state = read_system_state()
    state["honcho_context"]    = context_summary
    state["honcho_raw"]        = honcho_data
    state["honcho_synced_at"]  = datetime.now().isoformat()
    state["context"]           = context_summary  # overwrite top-level context

    os.makedirs(os.path.dirname(STATE_FILE), exist_ok=True)
    with open(STATE_FILE, "w") as f:
        json.dump(state, f, indent=2)


# ── Honcho Client Factory ─────────────────────────────────────────────────────

def get_honcho_client():
    """Returns an initialised Honcho client pointed at the Centauri workspace."""
    try:
        from honcho import Honcho
    except ImportError:
        raise RuntimeError(
            "honcho-ai package not found. Run: sudo pip3 install honcho-ai"
        )
    return Honcho(workspace_id=HONCHO_WORKSPACE, api_key=HONCHO_API_KEY)


# ── Node Actions ──────────────────────────────────────────────────────────────

def action_status() -> dict:
    """Verify Honcho connectivity and workspace health."""
    honcho = get_honcho_client()
    wade      = honcho.peer(WADE_PEER_ID)
    caroline  = honcho.peer(CAROLINE_PEER_ID)
    return {
        "workspace":       HONCHO_WORKSPACE,
        "wade_peer":       WADE_PEER_ID,
        "caroline_peer":   CAROLINE_PEER_ID,
        "sdk_version":     _honcho_version(),
        "connection":      "ok",
    }


def action_ingest(role: str, content: str, session_id: str = DEFAULT_SESSION) -> dict:
    """
    Write a single message into Honcho under the given session.

    role    : 'user' (Wade) or 'assistant' (Caroline)
    content : the message text
    """
    honcho   = get_honcho_client()
    wade     = honcho.peer(WADE_PEER_ID)
    caroline = honcho.peer(CAROLINE_PEER_ID)
    session  = honcho.session(session_id)
    session.add_peers([wade, caroline])

    if role == "user":
        msg = wade.message(content)
    elif role == "assistant":
        msg = caroline.message(content)
    else:
        raise ValueError(f"Unknown role '{role}'. Must be 'user' or 'assistant'.")

    session.add_messages([msg])
    return {
        "ingested": True,
        "role":     role,
        "session":  session_id,
        "preview":  content[:120],
    }


def action_query(question: str = "What should I know about Wade right now? 3 sentences max.") -> dict:
    """
    Ask Honcho's reasoning engine what it currently knows about Wade.
    Returns a synthesised natural-language summary.
    """
    honcho = get_honcho_client()
    wade   = honcho.peer(WADE_PEER_ID)
    response = wade.chat(question)
    return {
        "question": question,
        "answer":   str(response),
    }


def action_sync() -> dict:
    """
    Pull Honcho's latest Wade-model into caroline_neuro_memory.json.
    This is the primary integration point — after this call, the entire
    Centauri stack has fresh, reasoned context about Wade.
    """
    honcho = get_honcho_client()
    wade   = honcho.peer(WADE_PEER_ID)

    summary = wade.chat(
        "Summarise everything you know about Wade in 5 sentences. "
        "Focus on his personality, current priorities, projects, and emotional state."
    )
    summary_str = str(summary)

    raw_payload = {
        "workspace":  HONCHO_WORKSPACE,
        "peer":       WADE_PEER_ID,
        "summary":    summary_str,
        "synced_at":  datetime.now().isoformat(),
    }

    write_state_update(summary_str, raw_payload)

    return {
        "synced":         True,
        "state_file":     STATE_FILE,
        "summary_preview": summary_str[:200],
    }


# ── Utilities ─────────────────────────────────────────────────────────────────

def _honcho_version() -> str:
    try:
        import honcho
        return honcho.__version__
    except Exception:
        return "unknown"


# ── CLI Entry Point ───────────────────────────────────────────────────────────

def main():
    parser = argparse.ArgumentParser(
        description="Honcho-Caroline Centauri Interlock Node"
    )
    parser.add_argument(
        "--action",
        required=True,
        choices=["status", "ingest", "query", "sync"],
        help="Node action to execute",
    )
    parser.add_argument("--role",     default="user",   help="Message role: user|assistant (ingest only)")
    parser.add_argument("--content",  default="",       help="Message content (ingest only)")
    parser.add_argument("--question", default="",       help="Question to ask Honcho (query only)")
    parser.add_argument("--session",  default=DEFAULT_SESSION, help="Honcho session ID")
    args = parser.parse_args()

    # ── Interlock Step 1: State Check ─────────────────────────────────────────
    state    = read_system_state()
    priority = state.get("NeuroRank", 0)

    # ── Interlock Step 2: Primary Logic ──────────────────────────────────────
    try:
        if args.action == "status":
            result = action_status()

        elif args.action == "ingest":
            if not args.content:
                raise ValueError("--content is required for ingest action")
            result = action_ingest(
                role=args.role,
                content=args.content,
                session_id=args.session,
            )

        elif args.action == "query":
            question = args.question or "What should I know about Wade right now? 3 sentences max."
            result = action_query(question)

        elif args.action == "sync":
            result = action_sync()

        # ── Interlock Step 3: Closed-Loop Broadcast ───────────────────────────
        broadcast_result("SUCCESS", payload={**result, "neuro_rank_at_execution": priority})

    except Exception as e:
        broadcast_result("ERROR", error=str(e))
        sys.exit(1)


if __name__ == "__main__":
    main()
