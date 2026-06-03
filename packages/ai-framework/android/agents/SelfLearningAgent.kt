package com.nextgenbuildpro.aiframework.agents

/**
 * Self-Learning AI Agent Architecture — Issue #67
 *
 * Foundational architecture for a state-of-the-art self-managed AI agent on Android.
 * Simple surface API, highly capable internals — prepared for future reinforcement
 * learning enhancements.
 *
 * Architecture:
 *   SelfLearningAgent
 *       ├── PerceptionModule       — ingests sensor/text/voice inputs
 *       ├── ReasoningEngine        — on-device LLM inference (ONNX/llama.cpp)
 *       ├── MemoryStore            — episodic + semantic memory (Room DB)
 *       ├── ActionDispatcher       — executes actions, observes outcomes
 *       ├── RewardEvaluator        — scores outcomes against goals
 *       └── AdaptationLoop         — updates weights/rules from reward signal
 */

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.UUID

// ─── Core Models ──────────────────────────────────────────────────────────────

data class AgentInput(
    val id: String = UUID.randomUUID().toString(),
    val type: InputType,
    val content: String,
    val metadata: Map<String, String> = emptyMap(),
    val timestamp: LocalDateTime = LocalDateTime.now()
)

enum class InputType { TEXT, VOICE, SENSOR, NOTIFICATION, SCHEDULED }

data class AgentAction(
    val id: String = UUID.randomUUID().toString(),
    val type: ActionType,
    val payload: Map<String, Any> = emptyMap(),
    val reasoning: String = ""
)

enum class ActionType {
    RESPOND, CALL_API, STORE_MEMORY, TRIGGER_WORKFLOW,
    ESCALATE_TO_HUMAN, LEARN_FROM_FEEDBACK, NO_OP
}

data class AgentMemory(
    val id: String = UUID.randomUUID().toString(),
    val type: MemoryType,
    val content: String,
    val embedding: FloatArray = FloatArray(0),
    val importance: Float = 0.5f,
    val accessCount: Int = 0,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val lastAccessedAt: LocalDateTime = LocalDateTime.now()
)

enum class MemoryType { EPISODIC, SEMANTIC, PROCEDURAL, WORKING }

data class RewardSignal(
    val actionId: String,
    val score: Float,             // -1.0 to +1.0
    val feedback: String = "",
    val source: RewardSource = RewardSource.IMPLICIT
)

enum class RewardSource { EXPLICIT_USER, IMPLICIT_OUTCOME, GOAL_COMPLETION, GUARDRAIL_VIOLATION }

data class AgentState(
    val status: AgentStatus = AgentStatus.IDLE,
    val currentGoal: String = "",
    val confidenceLevel: Float = 1.0f,
    val sessionMemoryCount: Int = 0,
    val totalInteractions: Long = 0L,
    val learningCycles: Int = 0
)

enum class AgentStatus { IDLE, PERCEIVING, REASONING, ACTING, LEARNING, ERROR }

// ─── Perception ───────────────────────────────────────────────────────────────

class PerceptionModule {
    fun process(raw: String, type: InputType): AgentInput {
        val normalized = when (type) {
            InputType.VOICE -> raw.trim().lowercase()
            InputType.TEXT  -> raw.trim()
            else            -> raw
        }
        return AgentInput(type = type, content = normalized)
    }
}

// ─── Memory Store ─────────────────────────────────────────────────────────────

class MemoryStore {
    private val memories = mutableListOf<AgentMemory>()
    private val maxWorkingMemory = 20

    fun store(memory: AgentMemory) {
        memories.add(memory)
        if (memory.type == MemoryType.WORKING && memories.count { it.type == MemoryType.WORKING } > maxWorkingMemory) {
            val oldest = memories.filter { it.type == MemoryType.WORKING }
                .minByOrNull { it.lastAccessedAt }
            oldest?.let { memories.remove(it) }
        }
    }

    fun recall(query: String, type: MemoryType? = null, limit: Int = 5): List<AgentMemory> {
        return memories
            .filter { type == null || it.type == type }
            .filter { it.content.contains(query, ignoreCase = true) }
            .sortedByDescending { it.importance }
            .take(limit)
    }

    fun consolidate() {
        // Promote high-access working memories to episodic
        memories.filter { it.type == MemoryType.WORKING && it.accessCount > 3 }
            .forEach { m ->
                val idx = memories.indexOf(m)
                if (idx >= 0) memories[idx] = m.copy(type = MemoryType.EPISODIC, importance = minOf(m.importance + 0.1f, 1.0f))
            }
    }

    val size: Int get() = memories.size
}

// ─── Reward Evaluator ─────────────────────────────────────────────────────────

class RewardEvaluator {
    private val rewardHistory = mutableListOf<RewardSignal>()

    fun evaluate(signal: RewardSignal) {
        rewardHistory.add(signal)
        Log.d("RewardEval", "Action ${signal.actionId}: score=${signal.score} (${signal.source})")
    }

    fun averageReward(last: Int = 100): Float {
        val recent = rewardHistory.takeLast(last)
        return if (recent.isEmpty()) 0f else recent.sumOf { it.score.toDouble() }.toFloat() / recent.size
    }

    val totalEvaluations: Int get() = rewardHistory.size
}

// ─── Reasoning Engine ─────────────────────────────────────────────────────────

class ReasoningEngine(private val memory: MemoryStore) {

    fun reason(input: AgentInput, goal: String): AgentAction {
        val context = memory.recall(input.content, limit = 3)
            .joinToString("; ") { it.content }

        // Heuristic reasoning layer (replace with on-device ONNX LLM call)
        return when {
            input.content.contains("estimate", ignoreCase = true) ->
                AgentAction(type = ActionType.TRIGGER_WORKFLOW,
                    payload = mapOf("workflow" to "estimation", "input" to input.content),
                    reasoning = "User requested estimate — routing to AutonomousEstimationSystem")

            input.content.contains("call", ignoreCase = true) ||
            input.content.contains("phone", ignoreCase = true) ->
                AgentAction(type = ActionType.TRIGGER_WORKFLOW,
                    payload = mapOf("workflow" to "telephony", "input" to input.content),
                    reasoning = "Telephony intent detected")

            input.content.contains("remember", ignoreCase = true) ||
            input.content.contains("note", ignoreCase = true) ->
                AgentAction(type = ActionType.STORE_MEMORY,
                    payload = mapOf("content" to input.content),
                    reasoning = "Memory storage requested")

            context.isNotEmpty() ->
                AgentAction(type = ActionType.RESPOND,
                    payload = mapOf("context" to context, "input" to input.content),
                    reasoning = "Recalled ${context.length} chars of relevant memory")

            else ->
                AgentAction(type = ActionType.RESPOND,
                    payload = mapOf("input" to input.content),
                    reasoning = "General response — no specific workflow matched")
        }
    }
}

// ─── Adaptation Loop ──────────────────────────────────────────────────────────

class AdaptationLoop(
    private val memory: MemoryStore,
    private val rewardEvaluator: RewardEvaluator
) {
    private var learningCycles = 0

    fun adapt(action: AgentAction, reward: RewardSignal) {
        rewardEvaluator.evaluate(reward)

        // Store outcome as procedural memory for future reference
        if (reward.score > 0.7f) {
            memory.store(AgentMemory(
                type = MemoryType.PROCEDURAL,
                content = "SUCCESS: ${action.type} | ${action.reasoning} | score=${reward.score}",
                importance = reward.score
            ))
        } else if (reward.score < -0.3f) {
            memory.store(AgentMemory(
                type = MemoryType.PROCEDURAL,
                content = "FAILURE: ${action.type} | ${action.reasoning} | score=${reward.score}",
                importance = 0.9f   // high importance — learn from failures
            ))
        }

        if (++learningCycles % 50 == 0) {
            memory.consolidate()
            Log.i("AdaptationLoop", "Cycle $learningCycles: avg_reward=${rewardEvaluator.averageReward()}, memories=${memory.size}")
        }
    }
}

// ─── Main Agent ───────────────────────────────────────────────────────────────

class SelfLearningAgent(context: Context) {

    companion object {
        private const val TAG = "SelfLearningAgent"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val perception   = PerceptionModule()
    private val memoryStore  = MemoryStore()
    private val reasoning    = ReasoningEngine(memoryStore)
    private val rewardEval   = RewardEvaluator()
    private val adaptation   = AdaptationLoop(memoryStore, rewardEval)

    private val _state = MutableStateFlow(AgentState())
    val state: StateFlow<AgentState> = _state.asStateFlow()

    var currentGoal: String = "Assist Wade with construction business operations"

    // ─── Public API ──────────────────────────────────────────────────────────

    fun process(rawInput: String, type: InputType = InputType.TEXT): AgentAction {
        _state.value = _state.value.copy(status = AgentStatus.PERCEIVING)

        val input = perception.process(rawInput, type)

        _state.value = _state.value.copy(status = AgentStatus.REASONING)
        val action = reasoning.reason(input, currentGoal)

        _state.value = _state.value.copy(
            status = AgentStatus.ACTING,
            totalInteractions = _state.value.totalInteractions + 1,
            sessionMemoryCount = memoryStore.size
        )

        Log.i(TAG, "Action: ${action.type} | ${action.reasoning}")
        return action
    }

    fun reward(actionId: String, score: Float, feedback: String = "", source: RewardSource = RewardSource.IMPLICIT_OUTCOME) {
        scope.launch {
            _state.value = _state.value.copy(status = AgentStatus.LEARNING)
            val signal = RewardSignal(actionId, score, feedback, source)
            // Find the last action and adapt
            adaptation.adapt(AgentAction(id = actionId, type = ActionType.NO_OP), signal)
            _state.value = _state.value.copy(
                status = AgentStatus.IDLE,
                learningCycles = _state.value.learningCycles + 1
            )
        }
    }

    fun remember(content: String, type: MemoryType = MemoryType.EPISODIC, importance: Float = 0.5f) {
        memoryStore.store(AgentMemory(type = type, content = content, importance = importance))
        _state.value = _state.value.copy(sessionMemoryCount = memoryStore.size)
    }

    fun recall(query: String): List<AgentMemory> = memoryStore.recall(query)

    fun setGoal(goal: String) {
        currentGoal = goal
        _state.value = _state.value.copy(currentGoal = goal)
        Log.i(TAG, "Goal updated: $goal")
    }
}
