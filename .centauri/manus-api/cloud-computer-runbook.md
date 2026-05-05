# Centauri OS Cloud Computer Runtime Runbook

This runbook defines Manus **Cloud Computer** as the persistent runtime target for Centauri OS, Caroline, and related Manus API-enabled systems.

## Runtime Role

Cloud Computer is the always-on execution layer. GitHub stores source code, but Cloud Computer runs the actual services, routers, workers, queues, and scheduled jobs. The control-plane repository is `centauri-os`; application repositories plug into it as Nodes, adapters, or service units.

## Baseline Layout

| Path | Purpose |
| --- | --- |
| `/opt/centauri-os` | Control-plane checkout containing `core/command_router.py` and `caroline_neuro_memory.json`. |
| `/opt/centauri-systems` | Application and integration repository checkouts. |
| `/var/lib/centauri` | Persistent runtime state, logs, queues, and generated artifacts. |
| `/etc/centauri` | Environment files and deployment configuration. |

## Required Services

| Service | Description |
| --- | --- |
| `centauri-router.service` | Keeps the Command Router/event-bus loop alive where applicable. |
| `centauri-manus-api.service` | Runs Manus API task bridge workers or webhook processors. |
| `centauri-healthcheck.timer` | Periodically verifies router, state file, disk, memory, and API configuration. |

## Security Rules

Only SSH is open by default. Open additional ports only for authenticated APIs or web services. Never expose databases, admin panels, or unauthenticated worker endpoints directly to the public internet.

## Deployment Principle

All services must restart automatically after reboot. Cloud Computer tier changes, manual restarts, and restore operations can reboot the machine; `systemd` units prevent silent downtime.
