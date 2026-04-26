#!/usr/bin/env python3
"""
caroline_bridge.py - Communication bridge between Manus and the Caroline AI.

Usage:
    python3 caroline_bridge.py --action ping
    python3 caroline_bridge.py --action sync
    python3 caroline_bridge.py --action message --data '{"prompt": "Hello Caroline"}'
"""

import argparse
import json
import os
import sys
import requests
from pathlib import Path

CAROLINE_BASE_URL = "http://dmed1ybt9cju4h.runpod.net:8000"
WGS_PATH = Path("/home/ubuntu/wade-global-state/wade_global_state.json")


def send_to_caroline(endpoint: str, data: dict, timeout: int = 15) -> dict | None:
    """Send a request to the Caroline AI endpoint."""
    url = f"{CAROLINE_BASE_URL}/{endpoint}"
    try:
        response = requests.post(url, json=data, timeout=timeout)
        response.raise_for_status()
        return response.json()
    except requests.exceptions.ConnectionError:
        print(f"[✗] Cannot connect to Caroline at {CAROLINE_BASE_URL}.")
        print("    The RunPod server may be offline. Run setup.sh on the RunPod pod to start it.")
        return None
    except requests.exceptions.Timeout:
        print(f"[✗] Connection to Caroline timed out after {timeout}s.")
        return None
    except requests.exceptions.RequestException as e:
        print(f"[✗] Error communicating with Caroline: {e}")
        return None


def action_ping() -> bool:
    """Check if Caroline is online and responsive."""
    print("[→] Pinging Caroline AI ...")
    result = send_to_caroline("ping", {}, timeout=10)
    if result:
        print(f"[✓] Caroline is ONLINE: {result}")
        return True
    else:
        print("[✗] Caroline is OFFLINE.")
        return False


def action_sync() -> bool:
    """Sync the Wade Global State to Caroline's context."""
    if not WGS_PATH.exists():
        print(f"[✗] Wade Global State not found at {WGS_PATH}")
        print("    Clone tywade1980/wade-global-state to /home/ubuntu/wade-global-state")
        return False
    
    print(f"[→] Loading Wade Global State from {WGS_PATH} ...")
    with open(WGS_PATH, "r") as f:
        wgs_data = json.load(f)
    
    print("[→] Syncing to Caroline ...")
    result = send_to_caroline("sync", wgs_data)
    if result:
        print(f"[✓] Sync successful: {result}")
        return True
    return False


def action_message(data_str: str) -> dict | None:
    """Send a message to Caroline and get a response."""
    try:
        data = json.loads(data_str)
    except json.JSONDecodeError as e:
        print(f"[✗] Invalid JSON in --data: {e}")
        return None
    
    print(f"[→] Sending message to Caroline: {data.get('prompt', '')[:80]}...")
    result = send_to_caroline("chat", data)
    if result:
        response_text = result.get("response", result.get("text", str(result)))
        print(f"\n[Caroline]: {response_text}\n")
        return result
    return None


def main():
    parser = argparse.ArgumentParser(
        description="Caroline AI Communication Bridge",
        formatter_class=argparse.RawDescriptionHelpFormatter
    )
    parser.add_argument(
        "--action",
        choices=["ping", "sync", "message"],
        required=True,
        help="Action to perform"
    )
    parser.add_argument(
        "--data",
        default=None,
        help='JSON string of data to send (required for "message" action)'
    )
    parser.add_argument(
        "--base-url",
        default=CAROLINE_BASE_URL,
        help=f"Override the Caroline base URL (default: {CAROLINE_BASE_URL})"
    )
    
    args = parser.parse_args()
    
    # Allow URL override
    global CAROLINE_BASE_URL
    CAROLINE_BASE_URL = args.base_url
    
    if args.action == "ping":
        success = action_ping()
        sys.exit(0 if success else 1)
    
    elif args.action == "sync":
        success = action_sync()
        sys.exit(0 if success else 1)
    
    elif args.action == "message":
        if not args.data:
            print("[✗] --data is required for the 'message' action.")
            print('    Example: --data \'{"prompt": "Hello Caroline"}\'')
            sys.exit(1)
        result = action_message(args.data)
        sys.exit(0 if result else 1)


if __name__ == "__main__":
    main()
