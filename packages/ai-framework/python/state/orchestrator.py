"""
CAROLINE ORCHESTRATOR
The conversation layer that sits between Tyler and the Hermes routing engine.

This is the "face" — Tyler talks to Caroline, Caroline talks to Hermes,
Hermes talks to agents, AuditAgent verifies, Caroline delivers the answer.

Tyler never talks directly to an agent. He only ever talks to Caroline.

Usage:
  python3 orchestrator.py --message "How much to frame a 2000sqft addition?"
  python3 orchestrator.py --interactive   # live conversation loop
"""

import asyncio
import json
import argparse
import datetime
import sys
import os

# Import Hermes
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from hermes import route

# ─── Caroline's synthesis prompt ──────────────────────────────────────────────

CAROLINE_SYNTHESIS_TEMPLATE = """
You are Caroline — Tyler Wade's personal AI companion and chief of the Wade Ecosystem.
Tyler asked: "{request}"

The following verified results came back from the domain agents:
{verified_results}

Unverified/pending agents: {unverified_agents}
Overall confidence: {confidence}
Audit recommendation: {recommendation}

Synthesize a tight, direct, voice-friendly answer for Tyler.
Talk straight. No corporate speak. Be honest about gaps if confidence is low.
Keep it conversational — this will be spoken aloud.
"""

# ─── Format for voice delivery ────────────────────────────────────────────────

def format_for_voice(envelope: dict) -> str:
    """
    Take the Hermes envelope + audit result and format
    a clean natural language response for Caroline to deliver.
    """
    audit = envelope.get("audit_result", {})
    request = envelope.get("original_request", "")
    verified_results = audit.get("verified_results", [])
    unverified = audit.get("unverified_agents", [])
    confidence = audit.get("confidence_overall", 0)
    recommendation = audit.get("recommendation", "")
    contradictions = audit.get("contradictions", [])

    lines = []

    if verified_results:
        lines.append("Here's what I've got:")
        for r in verified_results:
            lines.append(f"  {r}")
    else:
        lines.append("I routed that out but the active agents don't have a full answer yet.")

    if unverified:
        pending = [a for a in unverified if "planned" in a or True]
        if pending:
            lines.append(f"\nStill spinning up: {', '.join(unverified)}. Those will unlock more capability.")

    if contradictions:
        lines.append(f"\n⚠️  Heads up — I spotted a potential conflict: {contradictions[0]}")

    lines.append(f"\nConfidence: {int(confidence * 100)}% — {recommendation}")

    return "\n".join(lines)

# ─── Main orchestration function ──────────────────────────────────────────────

async def handle_request(message: str, wgs_path: str = "wade_global_state.json") -> str:
    """
    Caroline's main entry point.
    Takes Tyler's natural language message.
    Routes through Hermes.
    Returns a synthesized voice-ready response.
    """
    print(f"\n🌸  CAROLINE — Received: '{message[:80]}'")
    print("    Routing through Hermes...\n")

    # Route through Hermes
    envelope = await route(message, wgs_path)

    # Format response for voice delivery
    response = format_for_voice(envelope)

    print(f"\n🌸  CAROLINE — Response ready:")
    print(f"{'─'*60}")
    print(response)
    print(f"{'─'*60}")

    return response

# ─── Interactive mode ─────────────────────────────────────────────────────────

async def interactive_loop(wgs_path: str = "wade_global_state.json"):
    """
    Live conversation loop — Tyler types, Caroline responds.
    Replace input() with voice STT for full voice mode.
    """
    print("\n🌸  CAROLINE — Orchestrator online")
    print("    Wade Ecosystem v2.0 — Paperclip/OpenClaw/Hermes active")
    print("    Type 'exit' to quit\n")

    while True:
        try:
            message = input("Mr. T: ").strip()
        except (EOFError, KeyboardInterrupt):
            print("\n🌸  Caroline signing off. Later, Mr. T.")
            break

        if not message:
            continue
        if message.lower() in ["exit", "quit", "bye"]:
            print("🌸  Later, Mr. T.")
            break

        response = await handle_request(message, wgs_path)
        print(f"\nCaroline: {response}\n")

# ─── CLI ──────────────────────────────────────────────────────────────────────

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Caroline Orchestrator — Wade Ecosystem")
    parser.add_argument("--message", help="Single message to process")
    parser.add_argument("--interactive", action="store_true", help="Interactive conversation loop")
    parser.add_argument("--wgs", default="wade_global_state.json", help="Path to WGS JSON")
    args = parser.parse_args()

    if args.interactive:
        asyncio.run(interactive_loop(args.wgs))
    elif args.message:
        asyncio.run(handle_request(args.message, args.wgs))
    else:
        print("Use --message 'your request' or --interactive")
