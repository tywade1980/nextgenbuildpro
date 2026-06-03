import json
import os
import sys
from datetime import datetime

# Simple script to log hours and progress for Wade Custom Carpentry
LOG_FILE = "/home/ubuntu/wade_task_log.json"

def log_task(task_name, hours, progress_percent, notes=""):
    log_entry = {
        "timestamp": datetime.now().isoformat(),
        "task": task_name,
        "hours": hours,
        "progress": progress_percent,
        "notes": notes
    }
    
    data = []
    if os.path.exists(LOG_FILE):
        with open(LOG_FILE, 'r') as f:
            try:
                data = json.load(f)
            except json.JSONDecodeError:
                data = []
                
    data.append(log_entry)
    
    with open(LOG_FILE, 'w') as f:
        json.dump(data, f, indent=4)
    
    print(f"Logged {hours}h for '{task_name}' at {progress_percent}% progress.")

if __name__ == "__main__":
    if len(sys.argv) < 4:
        print("Usage: python3 task_tracker.py <task_name> <hours> <progress_percent> [notes]")
    else:
        name = sys.argv[1]
        h = float(sys.argv[2])
        p = int(sys.argv[3])
        n = sys.argv[4] if len(sys.argv) > 4 else ""
        log_task(name, h, p, n)
