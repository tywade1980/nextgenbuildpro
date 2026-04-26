import os
import json
import argparse
import datetime
import subprocess

def run_command(command):
    """Run a shell command and return the output."""
    try:
        result = subprocess.run(command, shell=True, check=True, capture_output=True, text=True)
        return result.stdout.strip()
    except subprocess.CalledProcessError as e:
        print(f"Error running command: {e}")
        return None

def read_wgs(file_path):
    """Read the Wade Global State from the JSON file."""
    if os.path.exists(file_path):
        with open(file_path, 'r') as f:
            return json.load(f)
    else:
        print(f"Error: {file_path} not found.")
        return None

def write_wgs(file_path, data):
    """Write the Wade Global State to the JSON file."""
    with open(file_path, 'w') as f:
        json.dump(data, f, indent=2)
    print(f"Successfully updated {file_path}.")

def sync_with_github(repo_path):
    """Sync the repository with GitHub."""
    os.chdir(repo_path)
    run_command("git pull")
    run_command("git add .")
    run_command("git commit -m 'Sync WGS state'")
    run_command("git push")
    print("Successfully synced with GitHub.")

def main():
    parser = argparse.ArgumentParser(description="Wade Global State Synchronization Script")
    parser.add_argument("--action", choices=["read", "write"], required=True, help="Action to perform: read or write")
    parser.add_argument("--session-data", help="JSON string containing session data to write")
    parser.add_argument("--repo-path", default="/home/ubuntu/wade-global-state", help="Path to the WGS repository")
    
    args = parser.parse_args()
    file_path = os.path.join(args.repo_path, "wade_global_state.json")
    
    if args.action == "read":
        # First pull latest from GitHub
        sync_with_github(args.repo_path)
        data = read_wgs(file_path)
        if data:
            print(json.dumps(data, indent=2))
            
    elif args.action == "write":
        if not args.session_data:
            print("Error: --session-data is required for write action.")
            return
        
        # Read current state
        data = read_wgs(file_path)
        if not data:
            return
            
        # Parse session data
        try:
            session_data = json.loads(args.session_data)
        except json.JSONDecodeError:
            print("Error: --session-data must be a valid JSON string.")
            return
            
        # Update state
        data["last_updated"] = datetime.datetime.now().isoformat()
        
        # Add to session history
        session_entry = {
            "timestamp": data["last_updated"],
            "objective": session_data.get("objective", ""),
            "outcome": session_data.get("outcome", ""),
            "next_steps": session_data.get("next_steps", [])
        }
        data["session_history"].append(session_entry)
        
        # Update other fields if provided
        if "user_profile" in session_data:
            data["user_profile"].update(session_data["user_profile"])
        if "caroline_ai_project" in session_data:
            data["caroline_ai_project"].update(session_data["caroline_ai_project"])
        if "technical_state" in session_data:
            data["technical_state"].update(session_data["technical_state"])
            
        # Write back to file
        write_wgs(file_path, data)
        
        # Sync back to GitHub
        sync_with_github(args.repo_path)

if __name__ == "__main__":
    main()
