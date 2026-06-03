---
name: wade-ecosystem
description: Provides persistent context about Wade (Mr. T/Tyler), his business, his projects, technical environment, and communication preferences. This skill should be loaded at the beginning of every session to ensure continuity and avoid re-explaining context. It contains references to his core projects like the Caroline AI, NeuroRank™, and his construction business, as well as his technical setup and digital assets.
---

# Wade Ecosystem Skill

This skill provides a comprehensive, persistent knowledge base about Wade (also known as Mr. T or Tyler) to ensure continuity across all Manus sessions. Its primary purpose is to eliminate the need for Wade to repeat information about himself, his projects, or his technical environment.

## Core Principle: Continuity

**Always consult this skill at the start of a new session with Wade.** The goal is to pick up exactly where the last session left off. Before creating new files or starting new projects, check the `references/` directory to see if related work already exists.

## How to Use This Skill

This skill is organized into several reference files, each covering a specific domain of knowledge. Refer to the appropriate file based on the context of the task.

### Reference Files

The following files are located in the `references/` directory and contain the detailed knowledge base:

| File                 | Description                                                                                                                            |
| -------------------- | -------------------------------------------------------------------------------------------------------------------------------------- |
| `profile.md`         | Contains details about Wade's identity, personality, communication preferences, and working style.                                     |
| `projects.md`        | A detailed overview of all of Wade's key projects, including the Caroline AI, NeuroRank™, Centauri OS, and others.                      |
| `technical_setup.md` | Information on his technical environment, including his RunPod server, local machines, and preferred software.                           |
| `digital_assets.md`  | An inventory of his digital assets, including key GitHub repositories and Google Drive files.                                          |
| `vision.md`          | Outlines Wade's long-term vision for his AI and business projects, including the future of Caroline and his construction business.       |

### Workflow

1.  **On session start:** Load this skill and review the `SKILL.md` to understand the available context.
2.  **During the task:** Refer to the specific markdown files in the `references/` directory as needed to access detailed information.
3.  **Before creating new assets:** Check the `digital_assets.md` reference to see if a similar asset already exists in his GitHub or Google Drive.
