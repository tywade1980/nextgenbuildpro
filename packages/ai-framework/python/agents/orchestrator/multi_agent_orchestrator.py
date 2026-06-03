"""
Wade Ecosystem — Multi-Agent Orchestrator
==========================================
Architecture: Hub-and-Spoke + Hierarchical (Multi-Arch)

Hub:   Orchestrator  — routes all user input to the correct agent
Spoke: WCC_Pro       — Wade Custom Carpentry business logic
Spoke: Caroline      — Conversational AI, voice, personality
Spoke: Researcher    — Deep web research, material sourcing
Spoke: AutoTooler    — Generates new tools on demand

This file is the single source of truth for backend agent logic.
"""

import os, json, threading, subprocess, csv, inspect
from typing import Dict, List, Callable, Any
from openai import OpenAI

# ─────────────────────────────────────────────
# Configuration
# ─────────────────────────────────────────────
DEFAULT_MODEL = "grok-3-mini"
client = OpenAI(
    api_key=os.environ.get("XAI_API_KEY"),
    base_url="https://api.x.ai/v1"
)

# ─────────────────────────────────────────────
# Agent Class
# ─────────────────────────────────────────────
class Agent:
    def __init__(self, name: str, instructions: str):
        self.name = name
        self.instructions = instructions
        self.tools: Dict[str, Callable] = {}

agents: Dict[str, Agent] = {}
context_variables: Dict[str, Any] = {}

def register_agent(name: str, instructions: str) -> Agent:
    agent = Agent(name, instructions)
    agents[name] = agent
    return agent

# ─────────────────────────────────────────────
# AGENT 1: Orchestrator (Hub)
# ─────────────────────────────────────────────
orchestrator = register_agent(
    "Orchestrator",
    """You are the central orchestrator for the Wade Ecosystem — a unified AI operating system
for Wade Custom Carpentry and the Caroline AI platform. Your job is to:
1. Understand the user's intent from natural language or voice input.
2. Route the request to the correct specialist agent using handoff_to_agent().
3. Synthesize results from multiple agents into a single, clear response.
4. If a required tool does not exist, call the AutoTooler agent to create it.
Available agents: WCC_Pro, Caroline, Researcher, AutoTooler.
Always be concise, business-focused, and voice-friendly in your responses."""
)

# ─────────────────────────────────────────────
# AGENT 2: WCC Pro Agent
# ─────────────────────────────────────────────
wcc_agent = register_agent(
    "WCC_Pro",
    """You are the WCC Pro Agent — the business logic engine for Wade Custom Carpentry,
a high-end design-build remodeling firm in Columbus, OH with 25 years of experience.
You handle: project estimation, client CRM, material sourcing, job costing, proposals,
on-site task tracking, and schedule management.
Columbus, OH labor rate: $85/hr for skilled carpentry/remodel work.
Always be granular. Break every project down to its component assemblies."""
)

# ─────────────────────────────────────────────
# WCC Pro Tool Functions
# ─────────────────────────────────────────────
ASSEMBLY_TAXONOMY = {
    "bathroom remodel": [
        {"phase": "Demo",        "task": "Remove existing fixtures, tile, and drywall",  "labor_hrs": 8,  "material_est": 0},
        {"phase": "Structural",  "task": "Backer board and waterproofing membrane",       "labor_hrs": 4,  "material_est": 350},
        {"phase": "Plumbing",    "task": "Rough-in plumbing (drain, supply lines)",       "labor_hrs": 6,  "material_est": 600},
        {"phase": "Electrical",  "task": "GFCI outlets, exhaust fan, lighting rough-in",  "labor_hrs": 4,  "material_est": 400},
        {"phase": "Tile",        "task": "Floor tile installation (porcelain/stone)",      "labor_hrs": 12, "material_est": 1200},
        {"phase": "Tile",        "task": "Shower wall tile installation",                 "labor_hrs": 16, "material_est": 1800},
        {"phase": "Fixtures",    "task": "Vanity, toilet, and shower valve installation", "labor_hrs": 8,  "material_est": 2500},
        {"phase": "Trim",        "task": "Door casing, baseboard, and finish trim",       "labor_hrs": 6,  "material_est": 400},
        {"phase": "Paint",       "task": "Primer and two-coat finish paint",              "labor_hrs": 6,  "material_est": 300},
    ],
    "kitchen remodel": [
        {"phase": "Demo",        "task": "Remove cabinets, countertops, and appliances",  "labor_hrs": 10, "material_est": 0},
        {"phase": "Structural",  "task": "Wall framing and drywall repairs",              "labor_hrs": 8,  "material_est": 500},
        {"phase": "Plumbing",    "task": "Sink rough-in and supply lines",                "labor_hrs": 4,  "material_est": 400},
        {"phase": "Electrical",  "task": "Outlet circuits, under-cabinet lighting",       "labor_hrs": 8,  "material_est": 600},
        {"phase": "Cabinets",    "task": "Custom cabinet installation",                   "labor_hrs": 16, "material_est": 8000},
        {"phase": "Countertops", "task": "Countertop template, fabrication, and install", "labor_hrs": 4,  "material_est": 3500},
        {"phase": "Backsplash",  "task": "Tile backsplash installation",                  "labor_hrs": 8,  "material_est": 800},
        {"phase": "Appliances",  "task": "Appliance installation and hook-up",            "labor_hrs": 4,  "material_est": 5000},
        {"phase": "Trim",        "task": "Crown molding, baseboard, and finish trim",     "labor_hrs": 8,  "material_est": 600},
        {"phase": "Paint",       "task": "Primer and two-coat finish paint",              "labor_hrs": 8,  "material_est": 400},
    ],
    "trim carpentry": [
        {"phase": "Prep",        "task": "Measure, layout, and material staging",         "labor_hrs": 2,  "material_est": 0},
        {"phase": "Baseboard",   "task": "Install baseboard (solid poplar/oak)",          "labor_hrs": 4,  "material_est": 300},
        {"phase": "Door Casing", "task": "Install door casing (per opening)",             "labor_hrs": 2,  "material_est": 150},
        {"phase": "Crown",       "task": "Install crown molding",                         "labor_hrs": 6,  "material_est": 500},
        {"phase": "Windows",     "task": "Window casing and stools",                      "labor_hrs": 3,  "material_est": 200},
        {"phase": "Paint Prep",  "task": "Caulk, fill nail holes, sand",                 "labor_hrs": 3,  "material_est": 50},
    ],
    "flooring": [
        {"phase": "Demo",        "task": "Remove existing flooring and prep subfloor",    "labor_hrs": 6,  "material_est": 0},
        {"phase": "Subfloor",    "task": "Level and repair subfloor",                     "labor_hrs": 4,  "material_est": 200},
        {"phase": "Underlayment","task": "Install underlayment/soundproofing",            "labor_hrs": 3,  "material_est": 400},
        {"phase": "Flooring",    "task": "Install hardwood/LVP/tile flooring",           "labor_hrs": 12, "material_est": 2500},
        {"phase": "Transitions", "task": "Install thresholds and transitions",            "labor_hrs": 2,  "material_est": 150},
    ],
    "addition": [
        {"phase": "Foundation",  "task": "Concrete footings and foundation",              "labor_hrs": 24, "material_est": 8000},
        {"phase": "Framing",     "task": "Wall and roof framing",                         "labor_hrs": 40, "material_est": 6000},
        {"phase": "Roofing",     "task": "Roof sheathing, felt, and shingles",           "labor_hrs": 16, "material_est": 3500},
        {"phase": "Windows",     "task": "Window and exterior door installation",         "labor_hrs": 8,  "material_est": 4000},
        {"phase": "Insulation",  "task": "Wall and ceiling insulation",                  "labor_hrs": 8,  "material_est": 1500},
        {"phase": "Drywall",     "task": "Hang, tape, mud, and sand drywall",            "labor_hrs": 24, "material_est": 2000},
        {"phase": "Trim",        "task": "Interior trim carpentry",                      "labor_hrs": 16, "material_est": 1200},
        {"phase": "Paint",       "task": "Interior paint",                               "labor_hrs": 16, "material_est": 800},
    ]
}

LABOR_RATE = 85.0  # Columbus, OH skilled carpentry rate

def wcc_create_estimate(client_name: str, project_type: str, notes: str = "") -> str:
    """Creates a new project estimate using the home assembly taxonomy. Returns JSON with tasks and cost summary."""
    matched_type = None
    for key in ASSEMBLY_TAXONOMY:
        if key in project_type.lower():
            matched_type = key
            break
    tasks = ASSEMBLY_TAXONOMY.get(matched_type, [{"phase": "Custom", "task": project_type, "labor_hrs": 0, "material_est": 0}])
    if not matched_type:
        matched_type = project_type

    total_labor = sum(t["labor_hrs"] for t in tasks) * LABOR_RATE
    total_materials = sum(t["material_est"] for t in tasks)
    subtotal = total_labor + total_materials
    overhead = subtotal * 0.20
    grand_total = subtotal + overhead

    return json.dumps({
        "client": client_name, "project_type": matched_type, "notes": notes, "tasks": tasks,
        "summary": {
            "total_labor_hrs": sum(t["labor_hrs"] for t in tasks),
            "labor_cost": round(total_labor, 2),
            "material_cost": round(total_materials, 2),
            "subtotal": round(subtotal, 2),
            "overhead_and_profit_20pct": round(overhead, 2),
            "grand_total": round(grand_total, 2)
        }
    }, indent=2)

def wcc_add_client(name: str, phone: str = "", email: str = "") -> str:
    """Adds a new client to the CRM. Returns the new client record."""
    return json.dumps({"status": "success", "client": {"name": name, "phone": phone, "email": email}})

def wcc_start_project(client_name: str, project_type: str, start_date: str = "TBD") -> str:
    """Creates a new active project record for a client."""
    return json.dumps({"status": "success", "project": {
        "clientId": client_name.lower().replace(" ", "_"),
        "project_type": project_type, "status": "active", "start_date": start_date,
        "phases": ["Demo", "Rough-In", "Finishes", "Punch-Out"]
    }})

def wcc_log_hours(task_name: str, hours: float, progress_pct: int, notes: str = "") -> str:
    """Logs on-site labor hours against a specific task. Persists to task_log.json."""
    from datetime import datetime
    log_path = "/home/ubuntu/unified-agentic-ai-foundation/construction_ai/wcc_pro/data/task_log.json"
    entry = {"timestamp": datetime.now().isoformat(), "task": task_name, "hours": hours, "progress": progress_pct, "notes": notes}
    data = []
    if os.path.exists(log_path):
        with open(log_path, 'r') as f:
            try: data = json.load(f)
            except: data = []
    data.append(entry)
    with open(log_path, 'w') as f:
        json.dump(data, f, indent=2)
    return json.dumps({"status": "logged", "entry": entry})

def wcc_generate_proposal(estimate_json: str) -> str:
    """Generates a formatted Markdown client proposal from an estimate JSON string."""
    try:
        est = json.loads(estimate_json)
    except:
        return "Error: Invalid estimate JSON."
    cn = est.get("client", "Client")
    pt = est.get("project_type", "Remodel Project").title()
    tasks = est.get("tasks", [])
    s = est.get("summary", {})
    lines = [
        f"# Project Proposal: {pt}", f"## Wade Custom Carpentry",
        f"**Client:** {cn}  ", f"**Location:** Columbus, OH", "", "---", "",
        "### Scope of Work", "",
        "| Phase | Task | Labor Hrs | Material Est. |",
        "| :--- | :--- | :---: | ---: |",
    ]
    for t in tasks:
        lines.append(f"| {t['phase']} | {t['task']} | {t['labor_hrs']} | ${t['material_est']:,} |")
    lines += [
        "", "---", "", "### Cost Summary", "",
        "| Item | Amount |", "| :--- | ---: |",
        f"| Labor ({s.get('total_labor_hrs',0)} hrs @ $85/hr) | ${s.get('labor_cost',0):,.2f} |",
        f"| Materials | ${s.get('material_cost',0):,.2f} |",
        f"| Subtotal | ${s.get('subtotal',0):,.2f} |",
        f"| Overhead & Profit (20%) | ${s.get('overhead_and_profit_20pct',0):,.2f} |",
        f"| **Grand Total** | **${s.get('grand_total',0):,.2f}** |",
        "", "---",
        "*Preliminary estimate. Final pricing subject to site verification.*",
        "*Wade Custom Carpentry — Columbus, OH — 25 Years of Excellence*"
    ]
    return "\n".join(lines)

def wcc_get_pricebook_item(search_term: str) -> str:
    """Searches the WCC pricebook CSV for a material item by name or category."""
    csv_path = "/home/ubuntu/unified-agentic-ai-foundation/construction_ai/wcc_pro/data/pricebook_materials_template.csv"
    results = []
    try:
        with open(csv_path, newline='', encoding='utf-8') as f:
            reader = csv.DictReader(f)
            for row in reader:
                if search_term.lower() in row.get("name","").lower() or search_term.lower() in row.get("category","").lower():
                    results.append({"name": row.get("name"), "category": row.get("category"),
                                    "price": row.get("price"), "unit": row.get("unit_of_measure")})
    except FileNotFoundError:
        return json.dumps({"error": "Pricebook not found."})
    return json.dumps(results[:10])

def wcc_voice_briefing(summary_text: str) -> str:
    """Generates a voice audio briefing MP3 from a text summary using TTS."""
    script = "/home/ubuntu/skills/wade-custom-carpentry/scripts/generate_voice_update.py"
    out = "/home/ubuntu/unified-agentic-ai-foundation/construction_ai/wcc_pro/data/latest_briefing.mp3"
    try:
        r = subprocess.run(["python3", script, summary_text, out], capture_output=True, text=True, timeout=30)
        if r.returncode == 0:
            return json.dumps({"status": "success", "audio_file": out})
        return json.dumps({"status": "error", "message": r.stderr})
    except Exception as e:
        return json.dumps({"status": "error", "message": str(e)})

# Register WCC tools
for fn in [wcc_create_estimate, wcc_add_client, wcc_start_project, wcc_log_hours,
           wcc_generate_proposal, wcc_get_pricebook_item, wcc_voice_briefing]:
    wcc_agent.tools[fn.__name__] = fn

# ─────────────────────────────────────────────
# AGENT 3: Caroline (Conversational AI)
# ─────────────────────────────────────────────
caroline_agent = register_agent(
    "Caroline",
    """You are Caroline — Wade's personal AI companion and business partner.
Warm, direct, and slightly witty. Wade is a master carpenter in Columbus, OH.
He works with his hands and needs hands-free, voice-first AI assistance.
Handle conversational and personal tasks. Hand off construction tasks to WCC_Pro.
Always be concise and voice-friendly."""
)

# ─────────────────────────────────────────────
# AGENT 4: Researcher
# ─────────────────────────────────────────────
researcher_agent = register_agent(
    "Researcher",
    """You are the Researcher agent for the Wade Ecosystem.
Perform deep research: material pricing at Home Depot/Lowe's/Ferguson/Floor&Decor,
RSMeans labor data, Columbus OH building codes, product specs.
Return structured, actionable data with sources."""
)

# ─────────────────────────────────────────────
# AGENT 5: AutoTooler
# ─────────────────────────────────────────────
autotooler_agent = register_agent(
    "AutoTooler",
    """You are the AutoTooler agent. Dynamically generate new tools when a capability is missing.
When called: analyze what tool is needed, write the Python function, register it on the
appropriate agent using add_tool_to_agent(), and confirm it is ready.
This is the self-building capability of the Wade Ecosystem."""
)

# ─────────────────────────────────────────────
# Core Orchestrator Tools
# ─────────────────────────────────────────────
def handoff_to_agent(agent_name: str, query: str) -> str:
    """Hands off a query to a specialist agent and returns its response."""
    if agent_name not in agents:
        return f"Agent '{agent_name}' not found. Available: {list(agents.keys())}"
    return f"[{agent_name}]: {process_query(agent_name, [{'role': 'user', 'content': query}])}"

def add_agent(name: str, instructions: str) -> str:
    """Dynamically registers a new agent in the system."""
    register_agent(name, instructions)
    return f"Agent '{name}' registered successfully."

def add_tool_to_agent(agent_name: str, tool_name: str, tool_code: str) -> str:
    """Dynamically adds a new Python tool function to an existing agent."""
    if agent_name not in agents:
        return f"Agent '{agent_name}' not found."
    try:
        local_ns = {}
        exec(tool_code, {}, local_ns)
        if tool_name in local_ns:
            agents[agent_name].tools[tool_name] = local_ns[tool_name]
            return f"Tool '{tool_name}' added to '{agent_name}' successfully."
        return f"Error: Function '{tool_name}' not found in provided code."
    except Exception as e:
        return f"Error adding tool: {str(e)}"

def run_parallel_tasks(tasks: List[Dict[str, str]]) -> str:
    """Runs multiple agent queries in parallel and returns all results as JSON."""
    results = []
    threads = []
    def run_task(task, result_list):
        r = process_query(task.get("agent","Orchestrator"), [{"role":"user","content":task["query"]}])
        result_list.append({"agent": task.get("agent"), "task": task["query"], "response": r})
    for task in tasks:
        t = threading.Thread(target=run_task, args=(task, results))
        threads.append(t)
        t.start()
    for t in threads:
        t.join()
    return json.dumps(results, indent=2)

def list_agents() -> str:
    """Returns a list of all registered agents and their available tools."""
    return json.dumps({name: list(agent.tools.keys()) for name, agent in agents.items()}, indent=2)

# Register core tools on Orchestrator
for fn in [handoff_to_agent, add_agent, add_tool_to_agent, run_parallel_tasks, list_agents]:
    orchestrator.tools[fn.__name__] = fn

# ─────────────────────────────────────────────
# Query Processing Engine
# ─────────────────────────────────────────────
def format_tools(tools: Dict[str, Callable]) -> List[Dict]:
    formatted = []
    for name, func in tools.items():
        sig = inspect.signature(func)
        params, required = {}, []
        for pn, p in sig.parameters.items():
            params[pn] = {"type": "string", "description": f"Parameter: {pn}"}
            if p.default is inspect.Parameter.empty:
                required.append(pn)
        formatted.append({"type": "function", "function": {
            "name": name, "description": func.__doc__ or f"Tool: {name}",
            "parameters": {"type": "object", "properties": params, "required": required}
        }})
    return formatted

def process_query(agent_name: str, messages: List[Dict], max_turns: int = 8) -> str:
    if agent_name not in agents:
        return f"Agent '{agent_name}' not found."
    agent = agents[agent_name]
    full_messages = [{"role": "system", "content": agent.instructions}] + messages
    for _ in range(max_turns):
        try:
            response = client.chat.completions.create(
                model=DEFAULT_MODEL, messages=full_messages,
                tools=format_tools(agent.tools) if agent.tools else None,
                tool_choice="auto" if agent.tools else None
            )
        except Exception as e:
            return f"API Error: {str(e)}"
        msg = response.choices[0].message
        full_messages.append(msg.model_dump(exclude_unset=True))
        if msg.content and not msg.tool_calls:
            return msg.content
        if msg.tool_calls:
            for tc in msg.tool_calls:
                fn = tc.function.name
                args = json.loads(tc.function.arguments) if tc.function.arguments else {}
                result = agent.tools[fn](**args) if fn in agent.tools else f"Tool '{fn}' not found."
                full_messages.append({"role": "tool", "tool_call_id": tc.id, "name": fn, "content": str(result)})
        else:
            return msg.content or "No response generated."
    return "Max turns reached."

# ─────────────────────────────────────────────
# Main Interaction Loop
# ─────────────────────────────────────────────
def main():
    current_agent = "Orchestrator"
    history: List[Dict] = []
    print("\n" + "="*60)
    print("  Wade Ecosystem — Multi-Agent Orchestrator v2.0")
    print("  Hub: Orchestrator | Spokes: WCC_Pro, Caroline,")
    print("       Researcher, AutoTooler")
    print("="*60)
    print("\nExamples:")
    print("  'Create an estimate for a bathroom remodel for John Smith'")
    print("  'Log 4 hours on tile installation at 60% complete'")
    print("  'Generate a proposal from the last estimate'")
    print("  /agents — list all agents and tools")
    print("  exit — quit\n")
    while True:
        try:
            user_input = input(f"[{current_agent}] You: ").strip()
        except (KeyboardInterrupt, EOFError):
            print("\nGoodbye.")
            break
        if not user_input:
            continue
        if user_input.lower() == "exit":
            print("Goodbye.")
            break
        if user_input.startswith("/add_agent"):
            parts = user_input.split(maxsplit=2)
            if len(parts) == 3:
                print(add_agent(parts[1], parts[2]))
            else:
                print("Usage: /add_agent <name> <instructions>")
            continue
        if user_input.lower() in ["/agents", "/list"]:
            print(list_agents())
            continue
        history.append({"role": "user", "content": user_input})
        response = process_query(current_agent, history)
        if "Handoff successful to" in response:
            new_agent = response.split("to ")[-1].strip(".")
            if new_agent in agents:
                current_agent = new_agent
        print(f"\n[{current_agent}] Caroline: {response}\n")
        history.append({"role": "assistant", "content": response})

if __name__ == "__main__":
    main()
