---
name: skill-share
description: "Publish and receive Manus skills via GitHub repositories. Use for: installing skills from tywade1980/manus-DRS-skills, sharing skills between Manus sessions, publishing new skills to GitHub, and managing the skill version registry."
---

# Skill Share

This skill enables Manus to publish and receive skills through GitHub repositories, creating a persistent skill-sharing ecosystem across sessions.

## Core Workflows

### 1. Receive Skills from GitHub

To install skills from the Wade DRS skills repository:

```bash
python3 /home/ubuntu/skills/skill-share/scripts/receive_skill.py tywade1980/manus-DRS-skills
```

Options:
- `--version <tag>` — Install a specific tagged version (e.g., `v1.0.0`)
- `--skill <name>` — Install only one specific skill by name
- `--list` — List available skills without installing
- `--force` — Overwrite existing skills without prompting

### 2. Publish a Skill to GitHub

To publish a local skill to the DRS skills repository:

```bash
python3 /home/ubuntu/skills/skill-share/scripts/publish_skill.py <skill_name> tywade1980/manus-DRS-skills
```

Options:
- `--tag <version>` — Tag the release (e.g., `v1.0.0`)
- `--message <msg>` — Custom commit message

### 3. Skill Discovery

The `receive_skill.py` script automatically discovers skills in a repository by:
1. Checking for a root-level `SKILL.md` (single-skill repos)
2. Checking a `skills/` subdirectory for multiple skills
3. Checking root-level subdirectories containing `SKILL.md`

## Repository Structure

The `tywade1980/manus-DRS-skills` repository follows this structure:

```
manus-DRS-skills/
├── README.md
└── skills/
    ├── caroline-ai/
    │   ├── SKILL.md
    │   ├── scripts/
    │   └── references/
    ├── neurorank/
    │   └── SKILL.md
    └── wade-telephony/
        └── SKILL.md
```

## Version Registry

Installed skill versions are tracked in `/home/ubuntu/skills/.skill_versions.json`. This file maps skill names to their installed commit hashes for reproducibility.

## Best Practices

- Always run `receive_skill.py --list` first to preview available skills before installing.
- After publishing a new skill, tag it with a semantic version (e.g., `v1.0.0`).
- When building new skills for the Wade ecosystem, publish them to `tywade1980/manus-DRS-skills` so they persist across sessions.
