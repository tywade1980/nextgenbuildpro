# Manus API Interlock Integration

This repository has been prepared for **Manus API v2** integration under the **Centauri Interlock Standard**.

The integration pattern is intentionally conservative. It does not hard-code secrets, does not bypass `caroline_neuro_memory.json`, and does not create standalone automation silos. Every live Manus API operation must run as a Node or adapter that performs a state pre-flight and emits a closed-loop JSON broadcast back to the Command Router.

## Required Environment

| Variable | Purpose |
| --- | --- |
| `MANUS_API_KEY` | Manus API key generated from the Manus integration settings page. |
| `MANUS_API_BASE_URL` | Optional override. Defaults to `https://api.manus.ai`. |
| `CAROLINE_STATE_FILE` | Optional explicit path to `caroline_neuro_memory.json`. |

## Supported Initial Actions

| Action | Purpose |
| --- | --- |
| `health` | Verifies state visibility and whether the API key is present. |
| `create_task` | Creates a Manus task using `task.create`. |
| `list_messages` | Retrieves task messages using `task.listMessages`. |

## Cloud Computer Runtime Rule

For production, this repository should run on **Manus Cloud Computer** or another persistent host, not the temporary sandbox. Services must be installed with restart-safe process management such as `systemd`, and only protected web/API ports should be opened through the firewall.
