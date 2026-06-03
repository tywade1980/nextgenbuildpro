import requests
import json
import os
import argparse

def send_to_caroline(endpoint, data):
    """Send a message or data to the Caroline AI endpoint."""
    url = f"http://dmed1ybt9cju4h.runpod.net:8000/{endpoint}"
    try:
        response = requests.post(url, json=data, timeout=10)
        response.raise_for_status()
        return response.json()
    except requests.exceptions.RequestException as e:
        print(f"Error communicating with Caroline AI: {e}")
        return None

def main():
    parser = argparse.ArgumentParser(description="Caroline AI Communication Bridge")
    parser.add_argument("--action", choices=["ping", "sync", "message"], required=True)
    parser.add_argument("--data", help="JSON string of data to send")
    
    args = parser.parse_args()
    
    if args.action == "ping":
        result = send_to_caroline("ping", {})
        print(f"Caroline Status: {result}")
        
    elif args.action == "sync":
        # Pull state from WGS and push to Caroline
        wgs_path = "/home/ubuntu/wade-global-state/wade_global_state.json"
        if os.path.exists(wgs_path):
            with open(wgs_path, 'r') as f:
                wgs_data = json.load(f)
            result = send_to_caroline("sync", wgs_data)
            print(f"Sync Result: {result}")
            
    elif args.action == "message":
        if not args.data:
            print("Error: --data is required for message action.")
            return
        data = json.loads(args.data)
        result = send_to_caroline("chat", data)
        print(f"Caroline Response: {result}")

if __name__ == "__main__":
    main()
