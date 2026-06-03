"""
Unified Agent System — NextGen AI Framework

Consolidates from:
  - wade-global-state: Hermes routing engine + WGS state
  - unified-agentic-ai-foundation: Multi-agent orchestrator (xAI Grok)
  - manus-DRS-skills: NeuroRank scoring, centauri-interlock, runpod-connector
  - nextgen_apk: BigDaddy/HRM agent architecture (Kotlin → Python mirror)

Architecture (OpenClaw multi-agent routing):
  User input
      │
      ▼
  Hermes Router (OpenClaw chunking)
      │
      ├── ConstructAgent  → WCC construction tools + RSMeans
      ├── Caroline        → Voice/conversation interface
      ├── Researcher      → Material + code research
      ├── AutoTooler      → Dynamic tool generation
      ├── NeuroRankAgent  → Cognitive scoring + audit
      └── MemoryAgent     → WGS persistent memory
"""

import asyncio
import json
import os
import re
from pathlib import Path
from typing import Any
from openai import AsyncOpenAI
from guardrails import guardrails as _guardrails

# ─── Configuration ────────────────────────────────────────────────────────────

XAI_BASE_URL = "https://api.x.ai/v1"
DEFAULT_MODEL = "grok-3-mini"
WGS_PATH = Path(__file__).parent.parent / "state" / "wade_global_state.json"
MEMORY_PATH = Path(__file__).parent.parent / "state" / "caroline_neuro_memory.json"

# ─── WGS State ────────────────────────────────────────────────────────────────

class WGSState:
    """Wade Global State — single source of truth for all agents."""

    def __init__(self, path: Path = WGS_PATH):
        self.path = path
        self._data: dict = {}
        self.load()

    def load(self):
        if self.path.exists():
            with open(self.path) as f:
                self._data = json.load(f)
        else:
            self._data = {"schema_version": "2.0.0", "agent_registry": {}, "message_bus": {"queue": [], "completed": []}}

    def save(self):
        with open(self.path, "w") as f:
            json.dump(self._data, f, indent=2)

    def get_agent(self, name: str) -> dict:
        return self._data.get("agent_registry", {}).get(name, {})

    def get_chunking_rules(self) -> list:
        return self._data.get("chunking_rules", [])

    def add_message(self, message: dict):
        bus = self._data.setdefault("message_bus", {})
        queue = bus.setdefault("queue", [])
        queue.append(message)
        self.save()

    def record_completion(self, result: dict):
        bus = self._data.setdefault("message_bus", {})
        completed = bus.setdefault("completed", [])
        completed.append(result)
        if len(completed) > 50:
            completed.pop(0)
        self.save()

    @property
    def user_profile(self) -> dict:
        return self._data.get("user_profile", {})


# ─── NeuroRank Scorer ─────────────────────────────────────────────────────────

class NeuroRankScorer:
    """
    NeuroRank™ 5-region cognitive scoring engine.
    Patent-pending under Mini Me Technologies LLC.

    Regions:
        Logic Core       35%
        Emotional Engine 25%
        Memory Bank      20%
        Intuition Layer  10%
        Error Correction 10%
    """

    REGIONS = {
        "logic_core":        0.35,
        "emotional_engine":  0.25,
        "memory_bank":       0.20,
        "intuition_layer":   0.10,
        "error_correction":  0.10,
    }
    CORRECTION_THRESHOLD = 70.0

    def score(self, response: str, context: dict) -> dict:
        scores = {
            "logic_core":        self._score_logic(response, context),
            "emotional_engine":  self._score_emotional(response, context),
            "memory_bank":       self._score_memory(response, context),
            "intuition_layer":   self._score_intuition(response, context),
            "error_correction":  self._score_error(response),
        }
        total = sum(scores[k] * w for k, w in self.REGIONS.items())
        needs_correction = total < self.CORRECTION_THRESHOLD
        return {"scores": scores, "total": round(total, 2), "needs_correction": needs_correction}

    def _score_logic(self, resp: str, ctx: dict) -> float:
        # Heuristics: structured content, numbers, reasoning words
        indicators = ["because", "therefore", "however", "analysis", "data", "estimate"]
        score = 60.0 + sum(10 for w in indicators if w in resp.lower())
        return min(score, 100.0)

    def _score_emotional(self, resp: str, ctx: dict) -> float:
        polite = ["thank", "please", "appreciate", "understand", "help"]
        return min(60.0 + sum(8 for w in polite if w in resp.lower()), 100.0)

    def _score_memory(self, resp: str, ctx: dict) -> float:
        # Credit for using context from WGS
        history_len = len(ctx.get("conversation_history", []))
        return min(50.0 + history_len * 5, 100.0)

    def _score_intuition(self, resp: str, ctx: dict) -> float:
        proactive = ["you might also", "consider", "recommend", "suggest", "may want"]
        return min(50.0 + sum(10 for w in proactive if w in resp.lower()), 100.0)

    def _score_error(self, resp: str) -> float:
        error_signals = ["error", "exception", "traceback", "undefined", "null"]
        deductions = sum(20 for w in error_signals if w in resp.lower())
        return max(100.0 - deductions, 0.0)


# ─── OpenClaw Chunker ─────────────────────────────────────────────────────────

class OpenClaw:
    """Request fan-out engine — routes one request to N domain agents."""

    def __init__(self, wgs: WGSState):
        self.wgs = wgs
        self._rules = self._build_rules()

    def _build_rules(self) -> list[dict]:
        base_rules = [
            {"pattern": r"cost|price|estimate|material|labor|sqft|sf|bid|proposal",
             "agents": ["ConstructAgent", "WebAgent"]},
            {"pattern": r"call|schedule|remind|calendar|email|contact|message",
             "agents": ["Caroline"]},
            {"pattern": r"code|build|deploy|debug|git|error|fix|feature",
             "agents": ["AutoTooler", "Researcher"]},
            {"pattern": r"memory|remember|history|what did|last time",
             "agents": ["MemoryAgent"]},
            {"pattern": r"research|find|search|information|how to|explain",
             "agents": ["Researcher"]},
        ]
        # Overlay rules from WGS if available
        wgs_rules = self.wgs.get_chunking_rules()
        return wgs_rules if wgs_rules else base_rules

    def chunk(self, request: str) -> list[str]:
        matched = set()
        for rule in self._rules:
            if re.search(rule["pattern"], request, re.IGNORECASE):
                matched.update(rule["agents"])
        return list(matched) if matched else ["Caroline"]


# ─── Individual Agents ────────────────────────────────────────────────────────

CONSTRUCTION_ASSEMBLY_TAXONOMY = {
    "bathroom_remodel": {
        "phases": ["demo", "rough_plumbing", "tile", "fixtures", "paint"],
        "labor_hrs": 80, "material_est": 4500, "labor_rate": 85
    },
    "kitchen_remodel": {
        "phases": ["demo", "rough_plumbing", "electrical", "cabinets", "countertops", "appliances", "paint"],
        "labor_hrs": 120, "material_est": 8500, "labor_rate": 85
    },
    "trim_carpentry": {
        "phases": ["measure", "cut", "install", "caulk", "paint"],
        "labor_hrs": 24, "material_est": 800, "labor_rate": 85
    },
    "flooring": {
        "phases": ["prep", "install", "transitions", "cleanup"],
        "labor_hrs": 16, "material_est": 1200, "labor_rate": 85
    },
    "addition": {
        "phases": ["foundation", "framing", "roofing", "mep", "insulation", "drywall", "finish"],
        "labor_hrs": 400, "material_est": 35000, "labor_rate": 85
    }
}


class UnifiedAgentSystem:
    """Main entry point for the multi-agent system."""

    def __init__(self):
        self.wgs = WGSState()
        self.chunker = OpenClaw(self.wgs)
        self.scorer = NeuroRankScorer()
        self.client = AsyncOpenAI(
            api_key=os.environ.get("XAI_API_KEY", ""),
            base_url=XAI_BASE_URL
        )
        self._conversation_history: list[dict] = []
        self._model = os.environ.get("LLM_MODEL", DEFAULT_MODEL)

    # ─── Public API ───────────────────────────────────────────────────────────

    async def process(self, user_input: str) -> dict:
        """Process a user request through the full OpenClaw pipeline."""
        agents = self.chunker.chunk(user_input)
        tasks = [self._dispatch(agent, user_input) for agent in agents]
        results = await asyncio.gather(*tasks, return_exceptions=True)

        agent_results = {
            agents[i]: str(results[i]) if isinstance(results[i], Exception) else results[i]
            for i in range(len(agents))
        }

        final = await self._synthesize(user_input, agent_results)
        score = self.scorer.score(final, {"conversation_history": self._conversation_history})

        self._conversation_history.append({"role": "user", "content": user_input})
        self._conversation_history.append({"role": "assistant", "content": final})
        if len(self._conversation_history) > 40:
            self._conversation_history = self._conversation_history[-40:]

        self.wgs.record_completion({
            "input": user_input,
            "agents_used": agents,
            "neurorank": score["total"],
            "response_preview": final[:100]
        })

        # Run executive guardrails before returning
        guard = _guardrails.validate(final, {"current_budget": self.wgs.get("current_budget")})
        if not guard.approved:
            final = guard.sanitized_output
        if guard.requires_human_approval:
            self.wgs.record_completion({"guardrail_flag": True, "violations": [v.message for v in guard.violations]})

        return {
            "response": final,
            "agents": agents,
            "neurorank": score,
            "guardrails": {
                "approved": guard.approved,
                "requires_human_approval": guard.requires_human_approval,
                "violations": [{"rule": v.rule, "message": v.message, "risk": v.risk} for v in guard.violations],
            },
        }

    async def create_estimate(self, scope: str, area_sqft: float = 0) -> dict:
        """Generate a construction cost estimate using the assembly taxonomy."""
        taxonomy = CONSTRUCTION_ASSEMBLY_TAXONOMY.get(scope.lower().replace(" ", "_"))
        if not taxonomy:
            available = ", ".join(CONSTRUCTION_ASSEMBLY_TAXONOMY.keys())
            return {"error": f"Unknown scope. Available: {available}"}

        labor_cost = taxonomy["labor_hrs"] * taxonomy["labor_rate"]
        if area_sqft > 0:
            material_cost = taxonomy["material_est"] * (area_sqft / 100)
        else:
            material_cost = taxonomy["material_est"]

        subtotal = labor_cost + material_cost
        overhead = subtotal * 0.20
        total = subtotal + overhead

        return {
            "scope": scope,
            "phases": taxonomy["phases"],
            "labor_hours": taxonomy["labor_hrs"],
            "labor_cost": round(labor_cost, 2),
            "material_cost": round(material_cost, 2),
            "overhead_20pct": round(overhead, 2),
            "total": round(total, 2),
            "labor_rate_per_hr": taxonomy["labor_rate"]
        }

    # ─── Agent Dispatchers ────────────────────────────────────────────────────

    async def _dispatch(self, agent: str, request: str) -> str:
        dispatcher = {
            "ConstructAgent": self._construct_agent,
            "Caroline": self._caroline_agent,
            "Researcher": self._researcher_agent,
            "AutoTooler": self._autotooler_agent,
            "MemoryAgent": self._memory_agent,
            "WebAgent": self._web_agent,
        }
        fn = dispatcher.get(agent, self._caroline_agent)
        return await fn(request)

    async def _construct_agent(self, request: str) -> str:
        prompt = f"""You are WCC_Pro, a construction business intelligence agent for Wade Custom Carpentry in Columbus, OH.
Labor rate: $85/hr. Answer concisely with pricing, phases, and any scope clarifications needed.
Request: {request}"""
        return await self._llm(prompt)

    async def _caroline_agent(self, request: str) -> str:
        prompt = f"""You are Caroline, a voice-first executive assistant for Tyler Wade (master carpenter, ADHD-aware interface).
Be concise, warm, and practical. No bullet points in voice responses.
Request: {request}"""
        return await self._llm(prompt)

    async def _researcher_agent(self, request: str) -> str:
        prompt = f"""You are a construction research specialist. Provide accurate, cited information about materials, codes, techniques.
Request: {request}"""
        return await self._llm(prompt)

    async def _autotooler_agent(self, request: str) -> str:
        prompt = f"""You are AutoTooler — a dynamic capability generator. Generate practical code snippets or step-by-step procedures.
Request: {request}"""
        return await self._llm(prompt)

    async def _memory_agent(self, request: str) -> str:
        history = self._conversation_history[-10:]
        summary = json.dumps(history, indent=2)
        return f"[Memory Agent] Last {len(history)} conversation turns:\n{summary}"

    async def _web_agent(self, request: str) -> str:
        # Routes to xAI Realtime for real-time web lookups
        prompt = f"Provide current information about: {request}"
        return await self._llm(prompt)

    async def _synthesize(self, request: str, agent_results: dict[str, str]) -> str:
        parts = "\n".join(f"[{agent}]: {result}" for agent, result in agent_results.items())
        prompt = f"""Synthesize these agent responses into one clear, concise answer for Tyler Wade.
Voice-friendly format. Remove duplicates. Highlight the most actionable information.

Original request: {request}

Agent responses:
{parts}"""
        return await self._llm(prompt)

    async def _llm(self, prompt: str) -> str:
        try:
            resp = await self.client.chat.completions.create(
                model=self._model,
                messages=[{"role": "user", "content": prompt}],
                max_tokens=1024,
            )
            return resp.choices[0].message.content or ""
        except Exception as e:
            return f"[LLM error: {e}]"


# ─── CLI entry point ──────────────────────────────────────────────────────────

if __name__ == "__main__":
    import sys

    system = UnifiedAgentSystem()

    if "--estimate" in sys.argv:
        scope = sys.argv[sys.argv.index("--estimate") + 1]
        sqft = float(sys.argv[sys.argv.index("--sqft") + 1]) if "--sqft" in sys.argv else 0
        result = asyncio.run(system.create_estimate(scope, sqft))
        print(json.dumps(result, indent=2))
    else:
        query = " ".join(sys.argv[1:]) or "What can you help me with?"
        result = asyncio.run(system.process(query))
        print(result["response"])
        print(f"\n[NeuroRank: {result['neurorank']['total']}/100 | Agents: {', '.join(result['agents'])}]")
