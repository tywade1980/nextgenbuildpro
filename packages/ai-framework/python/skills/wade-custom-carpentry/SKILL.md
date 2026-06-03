---
name: wade-custom-carpentry
description: "Specialized skill for Wade Custom Carpentry to manage design-build remodel projects. Use for: project take-offs, labor and material estimation, proposal generation, material sourcing from Home Depot/Lowe's/Ferguson/etc., and on-site performance tracking."
---

# Wade Custom Carpentry Skill

This skill enables Manus to act as a digital assistant for a design-build remodel firm, handling everything from initial project planning to real-time performance analytics.

## Core Workflows

### 1. Project Planning & Estimation
When a new project overview is provided (e.g., "Bathroom remodel with tub-to-shower conversion"):
1. **Analyze Scope**: Identify all dependencies, structural changes, and finish requirements.
2. **Generate Take-off**: Create a granular list of required materials and labor tasks.
3. **Source Materials**: Use the guidelines in `references/material_sourcing.md` to find current pricing at preferred suppliers.
4. **Calculate Costs**: Estimate labor hours based on task complexity and calculate total project costs.
5. **Draft Proposal**: Use `templates/proposal_template.md` to present a professional estimate to the user.

### 2. Material Sourcing & Web Tasks
When requested to find materials or update project dependencies:
- Navigate to supplier websites (Home Depot, Lowe's, Ferguson, Floor & Decor, Tile Shop).
- Find specific items that match the project's material grade requirements.
- Update the project's cost estimation and schedule based on availability and lead times.

### 3. On-Site Performance & Analytics
When the user provides on-site updates (photos, voice memos, or text logs):
- **Log Hours**: Extract and record job hours against specific tasks.
- **Capture Performance**: Analyze progress photos to update the project's completion percentage.
- **Live Adjustments**: If a task is running behind, automatically adjust the "Live Schedule" and notify the user of the impact on subsequent dependencies.
- **Voice Briefing**: After processing updates, generate a short audio summary (using the Voice Output workflow) so the user can hear the status update without stopping work.

## Key Resources
- **Material Sourcing**: See `references/material_sourcing.md` for supplier strategies.
- **Proposal Template**: Use `templates/proposal_template.md` for all client-facing estimates.
- **Market Context**: See `references/market_context.md` for Columbus, OH specific data and material grades.
- **RSMeans Integration**: When granular job costing is required, trigger the `rsmeans-cost-estimator` skill to fetch industry-standard labor and material data.
- **Voice Output (TTS)**: When the user is on-site or hands-free, use `scripts/generate_voice_update.py` to convert text summaries, schedule changes, or material alerts into audio files (.mp3) for voice delivery.

## Best Practices
- **Granularity**: Always break down broad tasks (like "Bathroom Remodel") into specific sub-tasks (Demo, Framing, Rough-in, Tile, etc.).
- **Location Awareness**: When searching for materials or logging hours, always consider the project's specific location for local pricing and availability.
- **Proactive Memory**: Maintain a persistent record of project decisions and material selections to ensure consistency throughout the remodel.
- **Hands-Free Priority**: When the user is likely on-site, prioritize generating a voice update (.mp3) alongside any text-based logs.
