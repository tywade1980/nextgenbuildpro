import json
import os
import sys
from datetime import datetime

# --- Centauri OS: Command_Router ---
# The Central Event Bus for Modular Nodes
# ---------------------------------------

STATE_FILE = "caroline_neuro_memory.json"

def update_system_state(node_id, status, payload=None, error=None):
    """
    Update the persistent system state in caroline_neuro_memory.json
    based on a Node's broadcast.
    """
    # 1. Load current state
    if os.path.exists(STATE_FILE):
        with open(STATE_FILE, 'r') as f:
            state = json.load(f)
    else:
        state = {"status": "initialized", "last_node": None, "history": []}
    
    # 2. Update state with Node result
    state["last_node"] = node_id
    state["last_status"] = status
    state["last_update"] = datetime.now().isoformat()
    
    if status == "SUCCESS" and payload:
        # Merge payload into context if needed, or just log it
        state["context"] = payload.get("context", state.get("context", "default"))
    
    # 3. Log to history (keep it lean)
    log_entry = {
        "node_id": node_id,
        "status": status,
        "timestamp": datetime.now().isoformat()
    }
    state.setdefault("history", []).append(log_entry)
    if len(state["history"]) > 50: # Keep only last 50 events
        state["history"] = state["history"][-50:]
    
    # 4. Save updated state
    with open(STATE_FILE, 'w') as f:
        json.dump(state, f, indent=2)
    
    return state

def main():
    """
    Command_Router: Receives Node broadcasts via stdin and updates state.
    """
    try:
        # Read broadcast from stdin (e.g., piped from a Node)
        input_data = sys.stdin.read()
        if not input_data:
            return
            
        broadcast = json.loads(input_data)
        
        node_id = broadcast.get("node_id", "unknown_node")
        status = broadcast.get("status", "UNKNOWN")
        payload = broadcast.get("payload")
        error = broadcast.get("error")
        
        new_state = update_system_state(node_id, status, payload, error)
        
        print(f"--- Centauri OS: Command_Router ---")
        print(f"Node '{node_id}' reported: {status}")
        if error:
            print(f"Error: {error}")
        print(f"System State Updated: {STATE_FILE}")
        
    except json.JSONDecodeError:
        print("Error: Invalid broadcast format received by Command_Router.")
    except Exception as e:
        print(f"Command_Router Failure: {str(e)}")

if __name__ == "__main__":
    main()
