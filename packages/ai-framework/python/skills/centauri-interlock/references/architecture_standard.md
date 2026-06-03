# Centauri OS: The Interlock Standard

The Interlock Standard is the mandatory architectural framework for all modular components within the Centauri OS ecosystem. It ensures that no script or tool operates in isolation, maintaining a state-aware, closed-loop environment.

## 1. No Silos: Modular Node Pattern

Every script, tool, or function must be encapsulated as a **Node**. A Node is a self-contained unit of logic designed for plug-and-play integration.

- **Isolation**: Nodes do not contain hardcoded environment variables or external dependencies that aren't managed by the central system.
- **Pluggability**: Nodes are designed to be invoked by the **Command_Router**.

## 2. Mandatory State-Aware Execution

Before any Node performs a physical or digital action, it **must** verify the system context.

- **State File**: `caroline_neuro_memory.json`
- **Verification**: Read the state file to check:
    - Current System State (e.g., `idle`, `processing`, `emergency`)
    - Context (e.g., `active_project`, `current_user`)
    - **NeuroRank**: The priority level assigned to the current task.

## 3. Closed-Loop Reporting (The Broadcast)

A Node must never terminate without reporting its outcome to the **Command_Router**.

- **Standard Output**: JSON format.
- **Fields**:
    - `node_id`: Unique identifier for the module.
    - `timestamp`: ISO 8601 timestamp of completion.
    - `status`: `SUCCESS`, `ERROR`, or `PARTIAL_SUCCESS`.
    - `payload`: The actual data generated or processed.
    - `error`: Detailed error message if status is `ERROR`.

## 4. The Command_Router (Event Bus)

The central router is the orchestrator of all Nodes. It:
- Receives broadcasts from Nodes.
- Updates `caroline_neuro_memory.json` based on Node outputs.
- Triggers subsequent Nodes based on the updated state.
