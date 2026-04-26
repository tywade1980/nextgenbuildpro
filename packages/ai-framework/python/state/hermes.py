"""
HERMES — Wade Ecosystem Routing & Message Bus
The delivery layer between Caroline (orchestrator) and all domain agents.

Flow:
  1. Caroline calls hermes.route(request) with a natural language request
  2. Hermes runs OpenClaw chunking — splits request into domain tasks
  3. Tasks fan out to domain agents in parallel
  4. Results return to AuditAgent for verification
  5. Audit agent synthesizes verified answer
  6. Caroline delivers final answer to Tyler

Usage:
  python3 hermes.py --request "How much would it cost to frame a 2000sqft addition?"
  python3 hermes.py --request "Fix the websocket bug in voice-ai-app"
"""

import json
import re
import asyncio
import argparse
import datetime
import os
import sys
from typing import List, Dict, Any

# ─── Load WGS ─────────────────────────────────────────────────────────────────

def load_wgs(path: str = "wade_global_state.json") -> Dict:
    with open(path, "r") as f:
        return json.load(f)

def save_wgs(data: Dict, path: str = "wade_global_state.json"):
    data["last_updated"] = datetime.datetime.now().isoformat()
    with open(path, "w") as f:
        json.dump(data, f, indent=2)

# ─── OpenClaw: Chunker ────────────────────────────────────────────────────────

def openclaw_chunk(request: str, wgs: Dict) -> List[Dict]:
    """
    OpenClaw — splits a natural language request into domain task chunks.
    Returns a list of tasks, each assigned to a domain agent.
    """
    rules = wgs["chunking_rules"]["rules"]
    always_run = wgs["chunking_rules"].get("always_run", [])
    request_lower = request.lower()

    matched_agents = set()
    chunks = []

    for rule in rules:
        if rule["weight"] == "fallback":
            continue
        if re.search(rule["pattern"], request_lower):
            for agent_id in rule["route_to"]:
                matched_agents.add(agent_id)

    # Always fall back to caroline if nothing matched
    if not matched_agents:
        matched_agents.add("caroline")

    # Always run these regardless
    for agent_id in always_run:
        matched_agents.add(agent_id)

    # Build task chunks
    registry = {a["id"]: a for a in wgs["agent_registry"]["agents"]}
    for agent_id in matched_agents:
        agent = registry.get(agent_id)
        if not agent:
            continue
        chunks.append({
            "task_id": f"{agent_id}-{datetime.datetime.now().strftime('%H%M%S%f')}",
            "agent_id": agent_id,
            "agent_name": agent["name"],
            "agent_role": agent["role"],
            "status": agent["status"],
            "request": request,
            "result": None,
            "confidence": None,
            "timestamp_assigned": datetime.datetime.now().isoformat()
        })

    return chunks

# ─── Request envelope ─────────────────────────────────────────────────────────

def build_request_envelope(request: str, chunks: List[Dict]) -> Dict:
    return {
        "request_id": f"req-{datetime.datetime.now().strftime('%Y%m%d-%H%M%S%f')}",
        "original_request": request,
        "timestamp": datetime.datetime.now().isoformat(),
        "status": "pending",
        "chunks": chunks,
        "audit_result": None,
        "final_answer": None
    }

# ─── Simulated agent execution (replace with real calls as agents come online) ──

async def execute_chunk(chunk: Dict) -> Dict:
    """
    Dispatch a chunk to its assigned agent.
    Currently simulates response — replace each case with real agent call.
    """
    agent_id = chunk["agent_id"]
    status = chunk["status"]

    if status != "active":
        chunk["result"] = f"[{agent_id} is {status} — queued for activation]"
        chunk["confidence"] = 0.0
        return chunk

    # ── Active agents ──
    if agent_id == "memory_agent":
        # Read from WGS session history
        wgs = load_wgs()
        history_summary = [
            f"{s['timestamp'][:10]}: {s['objective']}"
            for s in wgs.get("session_history", [])[-5:]
        ]
        chunk["result"] = "Recent sessions:\n" + "\n".join(history_summary)
        chunk["confidence"] = 0.95

    elif agent_id == "web_agent":
        # xAI handles web_search via realtime tools — flag for Caroline to use
        chunk["result"] = "[web_search routed to xAI realtime tool — Caroline will execute]"
        chunk["confidence"] = 0.9

    elif agent_id == "neurorank_agent":
        # Placeholder scoring until NeuroRank™ is wired
        words = len(chunk["request"].split())
        complexity = min(1.0, words / 50)
        chunk["result"] = {
            "complexity_score": round(complexity, 2),
            "regions_activated": ["language", "context", "execution"],
            "recommended_depth": "deep" if complexity > 0.5 else "shallow"
        }
        chunk["confidence"] = 0.8

    else:
        chunk["result"] = f"[{agent_id} placeholder — not yet implemented]"
        chunk["confidence"] = 0.0

    chunk["timestamp_completed"] = datetime.datetime.now().isoformat()
    return chunk

# ─── AuditAgent ───────────────────────────────────────────────────────────────

def audit_results(chunks: List[Dict], original_request: str) -> Dict:
    """
    AuditAgent — the judge.
    Reviews all chunk results independently.
    Flags contradictions, low confidence, or placeholder results.
    Synthesizes a verified answer summary for Caroline.
    """
    verified = []
    unverified = []
    contradictions = []

    results_text = []
    for chunk in chunks:
        if chunk["confidence"] is None or chunk["confidence"] == 0.0:
            unverified.append(chunk["agent_id"])
        elif chunk["confidence"] >= 0.7:
            verified.append(chunk["agent_id"])
            if chunk["result"] and not str(chunk["result"]).startswith("["):
                results_text.append(f"[{chunk['agent_name']}]: {chunk['result']}")
        else:
            unverified.append(chunk["agent_id"])

    # Simple contradiction check — look for opposing keywords
    all_results = " ".join(str(c["result"]) for c in chunks if c["result"])
    if "yes" in all_results.lower() and "no" in all_results.lower():
        contradictions.append("Potential yes/no conflict detected — manual review recommended")

    synthesis = {
        "verified_agents": verified,
        "unverified_agents": unverified,
        "contradictions": contradictions,
        "confidence_overall": round(
            sum(c["confidence"] or 0 for c in chunks) / max(len(chunks), 1), 2
        ),
        "verified_results": results_text,
        "audit_timestamp": datetime.datetime.now().isoformat(),
        "recommendation": (
            "High confidence — deliver to Tyler" if len(verified) >= len(chunks) * 0.6
            else "Partial confidence — Caroline should note gaps"
        )
    }

    return synthesis

# ─── Hermes: Main router ──────────────────────────────────────────────────────

async def route(request: str, wgs_path: str = "wade_global_state.json") -> Dict:
    """
    Main Hermes routing function.
    Call this with any natural language request.
    Returns the full request envelope with audit result.
    """
    print(f"\n🪄  HERMES — Routing request: '{request[:80]}...' \n")

    wgs = load_wgs(wgs_path)

    # 1. OpenClaw — chunk the request
    chunks = openclaw_chunk(request, wgs)
    print(f"📎  OPENCLAW — Chunked into {len(chunks)} tasks:")
    for c in chunks:
        print(f"    → {c['agent_name']} ({c['agent_role']}) [{c['status']}]")

    # 2. Build envelope
    envelope = build_request_envelope(request, chunks)

    # 3. Fan out to agents in parallel
    print(f"\n🚀  Dispatching to agents...")
    tasks = [execute_chunk(chunk) for chunk in envelope["chunks"]]
    envelope["chunks"] = await asyncio.gather(*tasks)

    # 4. AuditAgent
    print(f"\n⚖️   AUDIT AGENT — Reviewing results...")
    audit = audit_results(envelope["chunks"], request)
    envelope["audit_result"] = audit
    envelope["status"] = "completed"

    print(f"\n✅  AUDIT COMPLETE")
    print(f"    Verified agents: {audit['verified_agents']}")
    print(f"    Overall confidence: {audit['confidence_overall']}")
    print(f"    Recommendation: {audit['recommendation']}")

    # 5. Log to WGS message bus
    wgs["message_bus"]["completed_requests"].append(envelope)
    # Keep only last 50
    wgs["message_bus"]["completed_requests"] = wgs["message_bus"]["completed_requests"][-50:]
    save_wgs(wgs, wgs_path)

    return envelope

# ─── CLI ──────────────────────────────────────────────────────────────────────

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Hermes — Wade Ecosystem Router")
    parser.add_argument("--request", required=True, help="Natural language request to route")
    parser.add_argument("--wgs", default="wade_global_state.json", help="Path to WGS JSON")
    args = parser.parse_args()

    result = asyncio.run(route(args.request, args.wgs))
    print("\n─── FINAL ENVELOPE ───")
    print(json.dumps(result, indent=2))
