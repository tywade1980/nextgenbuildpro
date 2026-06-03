---
name: runpod-connector
description: "Modular, state-driven connector for RunPod, enabling management of GPU pods (list, get details, start, stop, terminate) within the Centauri OS ecosystem, following the Centauri Interlock Standard."
---

# RunPod Connector: Centauri OS Integration

The `runpod-connector` is a modular Node designed to integrate RunPod GPU pod management seamlessly into the Centauri OS, adhering strictly to the Centauri Interlock Standard. It allows for state-aware execution and closed-loop reporting of RunPod operations.

## 1. Connector Standard & Interlock Compliance

This connector operates as a Centauri OS Node, ensuring:

- **Modular Node Encapsulation**: All RunPod interactions are encapsulated within `runpod_connector.py`.
- **State-Aware Execution**: Before any action, it reads `caroline_neuro_memory.json` to verify system state and `NeuroRank` priority.
- **Closed-Loop Broadcast**: All results (success, error, or data payload) are formatted as standard JSON and broadcasted to the `Command_Router` (via stdout for now, to be piped to `command_router.py`).

## 2. Core Functionality

The `runpod_connector.py` script provides the following capabilities:

- **List Pods**: Retrieve a list of all user's RunPod GPU pods with their current status and basic information.
- **Get Pod Details**: Fetch detailed information for a specific pod, including runtime metrics, GPU utilization, and port mappings.
- **Start Pod**: Resume an exited or stopped pod.
- **Stop Pod**: Stop a running pod.
- **Terminate Pod**: Permanently terminate a pod.

## 3. Usage

All interactions with the `runpod-connector` are performed by executing the `runpod_connector.py` script with specific environment variables to define the action and target pod.

**Prerequisites:**

- `RUNPOD_API_KEY` must be set as an environment variable.
- `caroline_neuro_memory.json` must exist (it will be initialized if not found).

### Examples:

#### 3.1. List All Pods

To list all your RunPod GPU pods:

```bash
export RUNPOD_API_KEY="your_runpod_api_key"
python3 /home/ubuntu/skills/runpod-connector/scripts/runpod_connector.py
# Or explicitly:
export RUNPOD_CONNECTOR_ACTION="list"
python3 /home/ubuntu/skills/runpod-connector/scripts/runpod_connector.py
```

#### 3.2. Get Details for a Specific Pod

To get detailed information for a pod (e.g., `18qipksrfdbhox`):

```bash
export RUNPOD_API_KEY="your_runpod_api_key"
export RUNPOD_CONNECTOR_ACTION="get_details"
export RUNPOD_TARGET_POD_ID="18qipksrfdbhox"
python3 /home/ubuntu/skills/runpod-connector/scripts/runpod_connector.py
```

#### 3.3. Start a Pod

To start an exited pod (e.g., `dmed1ybt9cju4h`):

```bash
export RUNPOD_API_KEY="your_runpod_api_key"
export RUNPOD_CONNECTOR_ACTION="start"
export RUNPOD_TARGET_POD_ID="dmed1ybt9cju4h"
python3 /home/ubuntu/skills/runpod-connector/scripts/runpod_connector.py
```

#### 3.4. Stop a Pod

To stop a running pod (e.g., `18qipksrfdbhox`):

```bash
export RUNPOD_API_KEY="your_runpod_api_key"
export RUNPOD_CONNECTOR_ACTION="stop"
export RUNPOD_TARGET_POD_ID="18qipksrfdbhox"
python3 /home/ubuntu/skills/runpod-connector/scripts/runpod_connector.py
```

#### 3.5. Terminate a Pod

To permanently terminate a pod (use with caution, e.g., `5vm9fv2abeqjzc`):

```bash
export RUNPOD_API_KEY="your_runpod_api_key"
export RUNPOD_CONNECTOR_ACTION="terminate"
export RUNPOD_TARGET_POD_ID="5vm9fv2abeqjzc"
python3 /home/ubuntu/skills/runpod-connector/scripts/runpod_connector.py
```

## 4. Environment Variables

- `RUNPOD_API_KEY`: Your RunPod API key (required for authentication).
- `RUNPOD_CONNECTOR_ACTION`: The action to perform (e.g., `list`, `get_details`, `start`, `stop`, `terminate`). Defaults to `list`.
- `RUNPOD_TARGET_POD_ID`: The ID of the target pod for actions like `get_details`, `start`, `stop`, `terminate`.

## 5. Interlock Standard Details

- **`read_system_state()`**: Loads `caroline_neuro_memory.json`. If the file does not exist, it initializes it with a default state.
- **`broadcast_result()`**: Prints a JSON object to stdout, which is intended to be captured and processed by the `Command_Router` for state updates.
