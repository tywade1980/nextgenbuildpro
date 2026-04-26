---
name: neurorank
description: "NeuroRank™ emotionally intelligent AI cognitive system (Mini Me Technologies LLC). Use for: applying the NeuroRank decision-making framework to AI responses, modeling cognitive regions (logic, emotion, memory), evaluating AI response quality, and implementing the patent-pending cognitive architecture in any agent workflow."
---

# NeuroRank™ Skill

NeuroRank™ is Wade's patent-pending, emotionally intelligent AI cognitive system developed under Mini Me Technologies LLC. It is designed as a decision-making engine that mimics human brain functions, incorporating logic, emotion, memory, and other cognitive regions to produce more human-like and contextually appropriate AI responses.

## Core Architecture

NeuroRank™ models the following cognitive regions:

| Region | Function | Weight |
|---|---|---|
| Logic Core | Rational analysis, fact-checking, step-by-step reasoning | 35% |
| Emotional Engine | Empathy, tone calibration, frustration detection | 25% |
| Memory Bank | Long-term context, user preferences, past decisions | 20% |
| Intuition Layer | Pattern recognition, heuristic shortcuts | 10% |
| Error Correction | Self-evaluation, response re-scoring | 10% |

## Core Workflows

### 1. Evaluate a Response

Before delivering any response, run it through the NeuroRank evaluator:

```bash
python3 /home/ubuntu/skills/neurorank/scripts/evaluate_response.py \
  --prompt "User's question" \
  --response "Proposed response" \
  --context "Optional context JSON"
```

The evaluator returns a score (0-100) and a breakdown by cognitive region.

### 2. Error Analysis and Correction

When a response scores below 70, trigger the error correction workflow:

1. **Analyze**: Identify which cognitive region failed (logic, emotion, memory, etc.).
2. **Re-evaluate**: Run alternative response candidates through the evaluator.
3. **Correct**: Select the highest-scoring alternative and explain the correction.

### 3. Apply NeuroRank to Agent Decisions

When building or running AI agents, wrap decision points with the NeuroRank framework:

```python
from neurorank import NeuroRankEngine

engine = NeuroRankEngine()
decision = engine.decide(
    options=["Option A", "Option B", "Option C"],
    context={"user_state": "frustrated", "task": "estimate_cost"},
    weights={"logic": 0.4, "emotion": 0.3, "memory": 0.3}
)
```

## Key References

- **Architecture Details**: See `references/neurorank_architecture.md` for the full cognitive model.
- **Scoring Rubric**: See `references/scoring_rubric.md` for how responses are evaluated.

## Best Practices

- Apply NeuroRank evaluation to any AI response that involves emotional context (frustrated user, high-stakes decision, etc.).
- Always log the NeuroRank score and breakdown to the Wade Global State for continuous improvement.
- The Error Correction workflow is mandatory when the user explicitly says a response "missed the mark."
