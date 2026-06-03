---
name: wade-telephony
description: "AI-powered telephony system for Wade Custom Carpentry. Use for: building or managing the AI receptionist, handling smart in-call services, configuring call routing, managing the telephony agent for construction business calls, and integrating with the next-genai call screen system."
---

# Wade Telephony Skill

This skill consolidates all telephony-related AI capabilities for Wade Custom Carpentry into a unified system. It covers the AI receptionist, smart in-call service, and the next-gen call screen — all designed to handle client calls hands-free while Wade is on-site.

## Core Systems

| System | Repository | Purpose |
|---|---|---|
| AI Receptionist | `tywade1980/smart-incallservice` | Handle incoming calls, take messages, schedule |
| Next-Gen AI | `tywade1980/next-genai` | Smart call screen with 3 AI models + CBMS |
| Telephony Agent | `tywade1980/telephony_agent` | Core agent logic for call handling |
| Next-Gen Tele | `tywade1980/nextgentele` | Telephony infrastructure |

## Core Workflows

### 1. AI Receptionist Setup

The AI receptionist handles incoming calls when Wade is on-site or unavailable:

1. Clone `tywade1980/smart-incallservice` to the server.
2. Configure the call routing rules in `references/call_routing.md`.
3. Connect to the telephony provider (see `references/telephony_providers.md`).
4. Start the receptionist agent:
   ```bash
   python3 scripts/start_receptionist.py --config config/receptionist.json
   ```

### 2. Smart In-Call Service

During active calls, the smart in-call service provides real-time assistance:

- **Client lookup**: Automatically identifies the caller and pulls their project history.
- **Estimate retrieval**: Fetches relevant estimates and project data during the call.
- **Scheduling**: Books appointments directly into the calendar during the call.
- **Voice briefing**: Whispers key information to Wade through an earpiece.

### 3. Call Screen Integration

The `next-genai` system provides a visual call screen dashboard:

- **3 AI Models**: Routes queries to the best model (Caroline, GPT-4.1-mini, Gemini).
- **CBMS**: Construction Business Management Solution — pulls live project data.
- **Call History**: Logs all calls with transcripts and action items.

## Key References

- **Call Routing**: See `references/call_routing.md` for routing logic and rules.
- **Telephony Providers**: See `references/telephony_providers.md` for Twilio/VAPI setup.
- **Call Scripts**: See `templates/call_scripts.md` for common call scenario templates.

## Best Practices

- **Voice-first**: All responses to Wade during a call must be voice-delivered, not text.
- **Client context**: Always pull the client's project history before connecting the call.
- **Hands-free**: The system must operate without requiring Wade to touch his phone while working.
- **Caroline integration**: Route complex questions to Caroline for the most personalized response.
