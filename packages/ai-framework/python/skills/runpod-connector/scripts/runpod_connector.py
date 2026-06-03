

import json
import os
import requests
from datetime import datetime

# --- Centauri OS: Interlock Standard ---
STATE_FILE = "caroline_neuro_memory.json"
COMMAND_ROUTER_SCRIPT = "/home/ubuntu/skills/centauri-interlock/scripts/command_router.py" # Assuming a path for the command router

def read_system_state():
    if not os.path.exists(STATE_FILE):
        # Initialize with a default state if the file doesn't exist
        default_state = {"status": "initialized", "NeuroRank": 0, "runpod_active_pod_id": ""}
        with open(STATE_FILE, 'w') as f:
            json.dump(default_state, f, indent=2)
        return default_state
    with open(STATE_FILE, 'r') as f:
        return json.load(f)

def broadcast_result(status, payload=None, error=None):
    result = {
        "node_id": "runpod_connector",
        "timestamp": datetime.now().isoformat(),
        "status": status,
        "payload": payload,
        "error": error
    }
    # In a real scenario, this would call the command_router.py script
    # For now, we'll just print it to stdout
    print(json.dumps(result, indent=2))
    # Example of how it would be called if command_router.py existed:
    # os.system(f"python3 {COMMAND_ROUTER_SCRIPT} --data '{json.dumps(result)}'")
    return result

# --- RunPod GraphQL API Interaction ---
RUNPOD_GRAPHQL_ENDPOINT = "https://api.runpod.io/graphql"

def _run_graphql_query(query, variables=None):
    api_key = os.getenv("RUNPOD_API_KEY")
    if not api_key:
        raise ValueError("RUNPOD_API_KEY environment variable not set.")

    headers = {"Content-Type": "application/json"}
    payload = {"query": query}
    if variables:
        payload["variables"] = variables

    response = requests.post(RUNPOD_GRAPHQL_ENDPOINT, params={'api_key': api_key}, json=payload, timeout=30)
    response.raise_for_status()
    return response.json()

def list_pods():
    query = """
    { myself { pods { id name desiredStatus costPerHr runtime { uptimeInSeconds gpus { id gpuUtilPercent memoryUtilPercent } } } } }
    """
    data = _run_graphql_query(query)
    return data.get("data", {}).get("myself", {}).get("pods", [])

def get_pod_details(pod_id):
    query = """
    query PodDetails($podId: String!) {
      pod(input: { podId: $podId }) {
        id
        name
        desiredStatus
        costPerHr
        runtime {
          uptimeInSeconds
          gpus { id gpuUtilPercent memoryUtilPercent }
          ports { ip isIpPublic privatePort publicPort type }
        }
        containerDiskInGb
        volumeInGb
        vcpuCount
        memoryInGb
        imageName
      }
    }
    """
    variables = {"podId": pod_id}
    data = _run_graphql_query(query, variables)
    return data.get("data", {}).get("pod")

def start_pod(pod_id, gpu_count=1):
    query = """
    mutation PodResume($podId: String!, $gpuCount: Int!) {
      podResume(input: { podId: $podId, gpuCount: $gpuCount }) {
        id
        desiredStatus
      }
    }
    """
    variables = {"podId": pod_id, "gpuCount": gpu_count}
    data = _run_graphql_query(query, variables)
    return data.get("data", {}).get("podResume")

def stop_pod(pod_id):
    query = """
    mutation PodStop($podId: String!) {
      podStop(input: { podId: $podId }) {
        id
        desiredStatus
      }
    }
    """
    variables = {"podId": pod_id}
    data = _run_graphql_query(query, variables)
    return data.get("data", {}).get("podStop")

def terminate_pod(pod_id):
    query = """
    mutation PodTerminate($podId: String!) {
      podTerminate(input: { podId: $podId }) 
    }
    """
    variables = {"podId": pod_id}
    data = _run_graphql_query(query, variables)
    return data.get("data", {}).get("podTerminate")

def main():
    state = read_system_state()
    broadcast_result("INFO", payload={"message": "RunPod connector started", "current_state": state})

    # Example usage based on a hypothetical command from the Command Router
    # In a real scenario, the action and pod_id would come from the router/state
    action = os.getenv("RUNPOD_CONNECTOR_ACTION", "list") # Default to list pods
    target_pod_id = os.getenv("RUNPOD_TARGET_POD_ID", state.get("runpod_active_pod_id"))

    try:
        if action == "list":
            pods = list_pods()
            broadcast_result("SUCCESS", payload={"action": "list_pods", "pods": pods})
        elif action == "get_details" and target_pod_id:
            details = get_pod_details(target_pod_id)
            broadcast_result("SUCCESS", payload={"action": "get_pod_details", "pod_id": target_pod_id, "details": details})
        elif action == "start" and target_pod_id:
            result = start_pod(target_pod_id)
            broadcast_result("SUCCESS", payload={"action": "start_pod", "pod_id": target_pod_id, "result": result})
        elif action == "stop" and target_pod_id:
            result = stop_pod(target_pod_id)
            broadcast_result("SUCCESS", payload={"action": "stop_pod", "pod_id": target_pod_id, "result": result})
        elif action == "terminate" and target_pod_id:
            result = terminate_pod(target_pod_id)
            broadcast_result("SUCCESS", payload={"action": "terminate_pod", "pod_id": target_pod_id, "result": result})
        else:
            broadcast_result("ERROR", error=f"Invalid action or missing pod ID: {action}, {target_pod_id}")

    except Exception as e:
        broadcast_result("ERROR", error=str(e))

if __name__ == "__main__":
    main()
