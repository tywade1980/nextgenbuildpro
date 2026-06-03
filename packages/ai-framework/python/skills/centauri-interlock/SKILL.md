---
name: centauri-interlock
description: "Mandatory architectural standard for Centauri OS. Use for: retrofitting old scripts into modular Nodes, building new state-driven tools, and ensuring all code integrates with the Command_Router and caroline_neuro_memory.json."
---

# Centauri Interlock: Modular State-Driven Architecture

The Centauri Interlock is the foundational "Skill" for all development within the Centauri OS ecosystem. It mandates that no code executes in a vacuum. Every tool, script, or automation must be built as a modular "Node" that plugs into a central Event Bus (the Command Router) and pulls its context from a persistent state file.

## 1. The Interlock Standard

All future capabilities must adhere to these three core architectural mandates:

- **No Standalone Execution**: Convert any isolated script into a modular "Node". This means logic is encapsulated, parameters are standardized, and the script is designed for plug-and-play integration.
- **Mandatory State Check**: Before a Node executes its primary function, it must read `caroline_neuro_memory.json`. This file contains the current system state, user context, and the **NeuroRank** priority level.
- **Closed-Loop Broadcast**: Upon completion, a Node must format its result (Success, Error, or Data Payload) into a standard JSON object and broadcast it back to the central `Command_Router`.

## 2. Implementation Workflow

When building or retrofitting a module, follow this sequence:

1. **State Initialization**: Load the `caroline_neuro_memory.json` file to verify the current context and ensure the task aligns with the system's "NeuroRank" priority.
2. **Modular Logic**: Implement the core functionality within a self-contained Node structure. Avoid hardcoding values that should be pulled from the state.
3. **JSON Broadcast**: Wrap all outputs in a standardized JSON schema and pipe them to the `Command_Router` script for state synchronization.

## 3. Standard Node Structure (Python)

Refer to the bundled resource `templates/interlock_node_template.py` for a starting point. Every Node should include:

- **`read_system_state()`**: A function to fetch and parse the current state from `caroline_neuro_memory.json`.
- **`broadcast_result(status, payload, error)`**: A function to output the final state in JSON format to the system bus.
- **Main Logic Block**: Encapsulated within a try-except block to ensure even failures are broadcasted as a closed loop.

## 4. The Command Router

The `scripts/command_router.py` acts as the central event bus. It is responsible for receiving broadcasts from Nodes, updating the persistent state, and maintaining the system's history. 

For more detailed architectural patterns, refer to the documentation in `references/architecture_standard.md`.
