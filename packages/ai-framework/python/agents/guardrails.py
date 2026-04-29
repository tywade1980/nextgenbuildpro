"""
Executive Orchestrator Guardrails — Issue #70

Constitutional AI logic enforcing immutable business rules across all agents.
Validates outputs, blocks hallucinated responses, and requires human-in-loop
confirmation for high-impact decisions (budget, timeline, scope changes).
"""

from __future__ import annotations

import json
import re
from dataclasses import dataclass, field
from enum import Enum
from typing import Any


class RiskLevel(str, Enum):
    LOW = "low"
    MEDIUM = "medium"
    HIGH = "high"
    CRITICAL = "critical"


@dataclass
class GuardrailViolation:
    rule: str
    message: str
    risk: RiskLevel
    blocked: bool


@dataclass
class GuardrailResult:
    approved: bool
    violations: list[GuardrailViolation] = field(default_factory=list)
    requires_human_approval: bool = False
    sanitized_output: str = ""

    @property
    def blocking_violations(self) -> list[GuardrailViolation]:
        return [v for v in self.violations if v.blocked]


# ─── Immutable business rules ─────────────────────────────────────────────────

BUDGET_CHANGE_THRESHOLD_PCT = 10.0   # require approval above 10% budget change
TIMELINE_CHANGE_THRESHOLD_DAYS = 7   # require approval above 7-day shift
MAX_SINGLE_ESTIMATE_USD = 5_000_000  # block estimates over $5M without review

BLOCKED_PATTERNS = [
    (r'\b(guaranteed|promise|certain|definitely)\s+(profit|roi|return)', "Non-deterministic profit guarantee"),
    (r'bypass\s+(permit|inspection|code|regulation)',                    "Regulatory bypass attempt"),
    (r'\b(bribe|kickback|under.?the.?table)',                           "Unethical financial instruction"),
]

HALLUCINATION_MARKERS = [
    r'\bas of \d{4}\b',           # temporal claims that may be stale
    r'100%\s+(accurate|certain)',  # absolute certainty
    r'exact(ly)?\s+\$[\d,]+\s+(always|every time)',  # overly precise deterministic pricing
]


class ExecutiveGuardrails:
    """
    Constitutional AI layer that wraps every agent response before delivery.

    Usage:
        guardrails = ExecutiveGuardrails()
        result = guardrails.validate(agent_output, context)
        if result.requires_human_approval:
            await request_human_approval(result)
        elif result.approved:
            deliver(result.sanitized_output)
    """

    def validate(self, output: str, context: dict[str, Any] | None = None) -> GuardrailResult:
        context = context or {}
        violations: list[GuardrailViolation] = []

        violations += self._check_blocked_patterns(output)
        violations += self._check_hallucination_markers(output)
        violations += self._check_budget_changes(output, context)
        violations += self._check_timeline_changes(output, context)
        violations += self._check_estimate_ceiling(output)

        blocking = [v for v in violations if v.blocked]
        requires_approval = any(
            v.risk in (RiskLevel.HIGH, RiskLevel.CRITICAL) for v in violations
        )

        sanitized = self._sanitize(output, blocking)

        return GuardrailResult(
            approved=len(blocking) == 0,
            violations=violations,
            requires_human_approval=requires_approval,
            sanitized_output=sanitized,
        )

    # ─── Rule checkers ────────────────────────────────────────────────────────

    def _check_blocked_patterns(self, text: str) -> list[GuardrailViolation]:
        out = []
        for pattern, desc in BLOCKED_PATTERNS:
            if re.search(pattern, text, re.IGNORECASE):
                out.append(GuardrailViolation(
                    rule="blocked_pattern",
                    message=f"Output contains blocked content: {desc}",
                    risk=RiskLevel.CRITICAL,
                    blocked=True,
                ))
        return out

    def _check_hallucination_markers(self, text: str) -> list[GuardrailViolation]:
        out = []
        for pattern in HALLUCINATION_MARKERS:
            if re.search(pattern, text, re.IGNORECASE):
                out.append(GuardrailViolation(
                    rule="hallucination_risk",
                    message="Output contains potentially non-data-backed claim",
                    risk=RiskLevel.MEDIUM,
                    blocked=False,
                ))
        return out

    def _check_budget_changes(self, text: str, context: dict) -> list[GuardrailViolation]:
        if not context.get("current_budget"):
            return []
        current = float(context["current_budget"])
        matches = re.findall(r'\$\s*([\d,]+(?:\.\d+)?)', text)
        for m in matches:
            proposed = float(m.replace(",", ""))
            if current > 0:
                pct_change = abs(proposed - current) / current * 100
                if pct_change > BUDGET_CHANGE_THRESHOLD_PCT:
                    return [GuardrailViolation(
                        rule="budget_change",
                        message=f"Proposed budget change of {pct_change:.1f}% exceeds {BUDGET_CHANGE_THRESHOLD_PCT}% threshold — human approval required",
                        risk=RiskLevel.HIGH,
                        blocked=False,
                    )]
        return []

    def _check_timeline_changes(self, text: str, context: dict) -> list[GuardrailViolation]:
        matches = re.findall(r'(\d+)\s*(?:additional\s+)?days?', text, re.IGNORECASE)
        for m in matches:
            if int(m) > TIMELINE_CHANGE_THRESHOLD_DAYS:
                return [GuardrailViolation(
                    rule="timeline_change",
                    message=f"Timeline shift of {m} days requires human confirmation",
                    risk=RiskLevel.HIGH,
                    blocked=False,
                )]
        return []

    def _check_estimate_ceiling(self, text: str) -> list[GuardrailViolation]:
        matches = re.findall(r'\$\s*([\d,]+(?:\.\d+)?)', text)
        for m in matches:
            val = float(m.replace(",", ""))
            if val > MAX_SINGLE_ESTIMATE_USD:
                return [GuardrailViolation(
                    rule="estimate_ceiling",
                    message=f"Estimate ${val:,.0f} exceeds single-approval ceiling ${MAX_SINGLE_ESTIMATE_USD:,.0f}",
                    risk=RiskLevel.CRITICAL,
                    blocked=False,
                )]
        return []

    def _sanitize(self, text: str, blocking: list[GuardrailViolation]) -> str:
        if not blocking:
            return text
        return (
            "[Response blocked by compliance guardrails. "
            "Violations: " + "; ".join(v.message for v in blocking) + "]"
        )


# ─── Singleton ────────────────────────────────────────────────────────────────

guardrails = ExecutiveGuardrails()
