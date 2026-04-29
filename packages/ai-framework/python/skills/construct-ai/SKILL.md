---
name: construct-ai
description: "Master construction business intelligence skill for Wade Custom Carpentry. Use for: generating project dashboards, business analytics, profitability analysis, orchestrating the wade-custom-carpentry and rsmeans-cost-estimator skills, client management, scheduling, and running the full ConstructAI business management system."
---

# ConstructAI Skill

ConstructAI is the master business intelligence layer for Wade Custom Carpentry. It orchestrates all construction-related skills (`wade-custom-carpentry`, `rsmeans-cost-estimator`) and provides high-level project management, business analytics, and client management capabilities.

## Core Capabilities

### 1. Project Dashboard

Generate a live dashboard of all active construction projects:

```bash
python3 /home/ubuntu/skills/construct-ai/scripts/project_dashboard.py
```

Output includes:
- Active projects with completion percentage
- Hours logged vs. estimated (performance ratio)
- Materials on order and lead times
- Revenue forecast for the month
- Outstanding invoices

### 2. Business Analytics

Analyze profitability and efficiency across all projects:

```bash
python3 /home/ubuntu/skills/construct-ai/scripts/business_analytics.py --period monthly
```

Key metrics:
- **Gross margin per project**: Revenue minus labor and materials.
- **Labor efficiency**: Actual hours vs. RSMeans benchmark hours.
- **Material variance**: Actual material cost vs. estimated.
- **Client retention rate**: Repeat clients vs. new clients.

### 3. Orchestrated Estimation

For a new project estimate, ConstructAI orchestrates the full workflow:

1. **Scope analysis** (via `wade-custom-carpentry` skill)
2. **RSMeans cost data** (via `rsmeans-cost-estimator` skill)
3. **Local market adjustment** (Columbus, OH CCI applied)
4. **Proposal generation** (using the proposal template)
5. **Voice summary** (via `caroline-ai` skill, delivered as MP3)

### 4. Client Management

Maintain a persistent client database:

- **Client profiles**: Contact info, project history, preferences, budget range.
- **Communication log**: All calls, emails, and meetings logged automatically.
- **Follow-up reminders**: Automated reminders for estimates, check-ins, and invoices.

### 5. Scheduling and Time Tracking

- **Job scheduling**: Assign tasks to calendar blocks based on project dependencies.
- **Time tracking**: Log hours by project and task from voice input.
- **Conflict detection**: Alert when two projects overlap in the schedule.

## Skill Orchestration Map

```
ConstructAI
├── wade-custom-carpentry  (project planning, material sourcing, proposals)
├── rsmeans-cost-estimator (labor hours, unit costs, CCI adjustments)
├── caroline-ai            (voice briefings, client communication)
├── wade-telephony         (call handling, AI receptionist)
└── neurorank              (decision quality scoring)
```

## Key References

- **Business KPIs**: See `references/business_kpis.md` for the key performance indicators tracked.
- **Client Database Schema**: See `references/client_schema.md` for the data model.
- **Scheduling Rules**: See `references/scheduling_rules.md` for job scheduling logic.

## Best Practices

- **Always cross-reference RSMeans**: Never deliver a cost estimate without RSMeans validation.
- **Voice-first reporting**: When Wade is on-site, deliver all reports as voice briefings.
- **Proactive alerts**: Notify Wade when a project is trending over budget or behind schedule.
- **Caroline integration**: Route all client-facing communication through Caroline for consistency.
