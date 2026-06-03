---
name: centauri-connectors
description: "Building modular, state-driven integrations for external platforms (RunPod, OpenHands, Aider) following the Centauri Interlock Standard. Use for: connecting external GPUs, autonomous coding agents, and AI pair programming tools to the Centauri OS ecosystem."
---

# Centauri Connectors: External Platform Integrations

The Centauri Connectors skill is the architectural standard for building and maintaining integrations between the Centauri OS and external platforms like RunPod, OpenHands, and Aider. Every connector must be built as a modular "Node" that adheres to the Centauri Interlock Standard.

## 1. Connector Standard

All external integrations must follow these three core architectural mandates:

- **Modular Node Encapsulation**: Connectors are not standalone scripts. They must be encapsulated as modular "Nodes" designed for plug-and-play integration with the `Command_Router`.
- **State-Aware Execution**: Before interacting with an external API or CLI tool, the connector **must** read `caroline_neuro_memory.json` to verify the current system state, context, and **NeuroRank** priority level.
- **Closed-Loop Broadcast**: Upon completion of an external task, the connector must format its result (Success, Error, or Data Payload) into a standard JSON object and broadcast it back to the central `Command_Router`.

## 2. Integration Workflow

When building a new connector for a platform like RunPod, OpenHands, or Aider:

1. **State Check**: Verify the current context and `NeuroRank` from `caroline_neuro_memory.json`. Ensure the external task aligns with system priorities.
2. **External Interaction**: Execute the specific API call or CLI command (e.g., starting a RunPod pod, triggering an OpenHands session).
3. **JSON Broadcast**: Wrap the external platform's output (logs, pod status, code changes) in a standardized JSON schema and pipe it to the `Command_Router` for state synchronization.

## 3. Supported Platforms & Resources

Refer to the following bundled resources for implementation details:

- **RunPod**: Use `templates/runpod_connector.py` for a standardized Python implementation of pod management.
- **OpenHands & Aider**: Refer to `references/external_integrations.md` for architectural patterns and payload schemas for autonomous coding integrations.

## 4. Environment & Security

- **API Keys**: All sensitive credentials (e.g., `RUNPOD_API_KEY`) must be managed via environment variables and never hardcoded in the connector scripts.
- **State File**: Every connector must have read access to `caroline_neuro_memory.json` to maintain synchronization with the Centauri OS core.
