import os
import json
import threading
from typing import Dict, Any, List, Callable
from openai import OpenAI

# Set up the client (xAI Grok API compatible with OpenAI SDK)
client = OpenAI(
    api_key=os.getenv("XAI_API_KEY"),
    base_url="https://api.x.ai/v1"
)

# Default model (use a reasoning-capable Grok model)
DEFAULT_MODEL = "grok-beta"  # Updated to a valid xAI model; adjust if needed

# Agent class
class Agent:
    def __init__(self, name: str, instructions: str, tools: Dict[str, Callable] = None):
        self.name = name
        self.instructions = instructions
        self.tools = tools or {}

# Global registry for agents and shared context
agents: Dict[str, Agent] = {}
context_variables: Dict[str, Any] = {}

# Built-in Orchestrator Agent
orchestrator = Agent(
    name="Orchestrator",
    instructions="You are the central communication layer. Orchestrate tasks by delegating to other agents, handle complex conversations, and respond helpfully. Use tools to manage the system. If a query is complex, break it down and use parallel execution if needed.",
    tools={}
)

# Register initial agent
agents["Orchestrator"] = orchestrator

# Tool: Handoff to another agent
def handoff_to_agent(agent_name: str) -> str:
    if agent_name in agents:
        return f"Handoff successful to {agent_name}. Now responding as {agent_name}."
    else:
        return f"Agent {agent_name} not found."

# Tool: Add a new agent
def add_agent(name: str, instructions: str) -> str:
    if name in agents:
        return f"Agent {name} already exists."
    agents[name] = Agent(name, instructions)
    return f"Added new agent: {name} with instructions: {instructions}"

# Tool: Add a tool to an agent
def add_tool_to_agent(agent_name: str, tool_name: str, tool_description: str, tool_code: str) -> str:
    if agent_name not in agents:
        return f"Agent {agent_name} not found."
    # Dynamically create a function from code (use eval cautiously in production)
    try:
        tool_func = eval(tool_code)
        agents[agent_name].tools[tool_name] = tool_func
        return f"Added tool {tool_name} to {agent_name}."
    except Exception as e:
        return f"Error adding tool: {str(e)}"

# Tool: Run tasks in parallel (simple threading)
def run_parallel_tasks(tasks: List[Dict[str, str]]) -> str:
    results = []
    threads = []
    def run_task(task, result_list):
        agent_name = task.get("agent", "Orchestrator")
        query = task["query"]
        response = process_query(agent_name, [{"role": "user", "content": query}])
        result_list.append({"task": query, "response": response})

    for task in tasks:
        t = threading.Thread(target=run_task, args=(task, results))
        threads.append(t)
        t.start()

    for t in threads:
        t.join()

    return json.dumps(results)

# Add built-in tools to Orchestrator
orchestrator.tools["handoff_to_agent"] = handoff_to_agent
orchestrator.tools["add_agent"] = add_agent
orchestrator.tools["add_tool_to_agent"] = add_tool_to_agent
orchestrator.tools["run_parallel_tasks"] = run_parallel_tasks

# Function to format tools for API
def format_tools(tools: Dict[str, Callable]) -> List[Dict]:
    formatted = []
    for name, func in tools.items():
        # Simple description extraction (improve with docstrings in production)
        desc = func.__doc__ or "No description"
        params = {"type": "object", "properties": {}}  # Add param inference if needed
        formatted.append({
            "type": "function",
            "function": {
                "name": name,
                "description": desc,
                "parameters": params
            }
        })
    return formatted

# Process a query with an agent (handles tool calling loop)
def process_query(agent_name: str, messages: List[Dict], max_turns: int = 5) -> str:
    if agent_name not in agents:
        return "Agent not found."

    agent = agents[agent_name]
    system_message = {"role": "system", "content": agent.instructions}
    full_messages = [system_message] + messages

    for _ in range(max_turns):
        response = client.chat.completions.create(
            model=DEFAULT_MODEL,
            messages=full_messages,
            tools=format_tools(agent.tools) if agent.tools else None,
            tool_choice="auto" if agent.tools else None
        )

        message = response.choices[0].message
        full_messages.append(message.model_dump(exclude_unset=True))

        if message.content:
            return message.content

        if message.tool_calls:
            for tool_call in message.tool_calls:
                func_name = tool_call.function.name
                if func_name in agent.tools:
                    args = json.loads(tool_call.function.arguments) if tool_call.function.arguments else {}
                    result = agent.tools[func_name](**args)
                    full_messages.append({
                        "role": "tool",
                        "tool_call_id": tool_call.id,
                        "name": func_name,
                        "content": str(result)
                    })
                else:
                    full_messages.append({
                        "role": "tool",
                        "content": "Tool not found."
                    })
        else:
            return "No response generated."

    return "Max turns reached."

# Main interaction loop (self-building through user interactions)
def main():
    current_agent = "Orchestrator"
    conversation_history: List[Dict] = []

    print("Welcome to the Self-Evolving Multi-Agent Orchestrator!")
    print("Interact naturally or use commands like /add_agent <name> <instructions>")
    print("Type 'exit' to quit.")

    while True:
        user_input = input(f"[{current_agent}] You: ")
        if user_input.lower() == "exit":
            break

        # Handle special commands for self-building (bypass API for simplicity)
        if user_input.startswith("/add_agent"):
            parts = user_input.split(maxsplit=2)
            if len(parts) == 3:
                _, name, instructions = parts
                print(add_agent(name, instructions))
                continue

        # Process via current agent
        conversation_history.append({"role": "user", "content": user_input})
        response = process_query(current_agent, conversation_history)

        # Check for handoff in response (simple parse; improve with regex)
        if "Handoff successful to" in response:
            new_agent = response.split("to ")[-1].strip(".")
            if new_agent in agents:
                current_agent = new_agent

        print(f"[{current_agent}] AI: {response}")
        conversation_history.append({"role": "assistant", "content": response})

if __name__ == "__main__":
    main()