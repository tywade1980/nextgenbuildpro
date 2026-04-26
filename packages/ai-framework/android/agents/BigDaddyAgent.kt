package agents

import shared.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * BigDaddyAgent - System Authority and Control Agent
 * Responsible for high-level decision making, authorization, and system oversight
 */
class BigDaddyAgent(
    override val id: String = "bigdaddy-001",
    private val config: AgentConfig
) : Agent {
    
    override val type = AgentType.BIG_DADDY
    override var state = AgentState.IDLE
        private set
    
    private val authorizedAgents = mutableSetOf<AgentType>()
    private val systemPolicies = mutableMapOf<String, Policy>()
    private val decisionHistory = mutableListOf<SystemDecision>()
    private val threatAssessments = mutableMapOf<String, ThreatLevel>()
    
    private var isActive = false
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    data class Policy(
        val id: String,
        val name: String,
        val description: String,
        val rules: List<Rule>,
        val priority: Priority,
        val isActive: Boolean = true,
        val createdAt: Long = System.currentTimeMillis()
    )
    
    data class Rule(
        val condition: String,
        val action: String,
        val targetAgents: Set<AgentType>,
        val severity: Severity
    )
    
    enum class Severity {
        INFO, WARNING, CRITICAL, EMERGENCY
    }
    
    data class SystemDecision(
        val id: String,
        val decisionType: DecisionType,
        val context: String,
        val outcome: String,
        val affectedAgents: Set<AgentType>,
        val authorization: AuthorizationLevel,
        val timestamp: Long = System.currentTimeMillis()
    )
    
    enum class DecisionType {
        AUTHORIZATION,
        RESOURCE_ALLOCATION,
        POLICY_ENFORCEMENT,
        EMERGENCY_RESPONSE,
        SYSTEM_SHUTDOWN,
        AGENT_MANAGEMENT
    }
    
    enum class AuthorizationLevel {
        GRANTED,
        DENIED,
        CONDITIONAL,
        ESCALATED
    }
    
    enum class ThreatLevel {
        NONE,
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
    
    init {
        initializeSystemPolicies()
        initializeAuthorizations()
        startSystemMonitoring()
    }
    
    override suspend fun process(message: Message): Message? {
        state = AgentState.PROCESSING
        
        return try {
            when (message.content) {
                "REQUEST_AUTHORIZATION" -> handleAuthorizationRequest(message)
                "POLICY_CHECK" -> performPolicyCheck(message)
                "EMERGENCY_ALERT" -> handleEmergencyAlert(message)
                "SYSTEM_STATUS" -> getSystemStatus(message)
                "THREAT_ASSESSMENT" -> performThreatAssessment(message)
                "AGENT_REGISTRATION" -> registerAgent(message)
                "POLICY_UPDATE" -> updatePolicy(message)
                "SHUTDOWN_REQUEST" -> handleShutdownRequest(message)
                else -> processGeneralRequest(message)
            }
        } catch (e: Exception) {
            state = AgentState.ERROR
            Message(
                id = generateMessageId(),
                fromAgent = type,
                toAgent = message.fromAgent,
                content = "AUTHORITY_ERROR: ${e.message}",
                priority = Priority.CRITICAL
            )
        } finally {
            state = AgentState.IDLE
        }
    }
    
    override suspend fun learn(event: LearningEvent) {
        when (event.eventType) {
            LearningType.PATTERN_RECOGNITION -> {
                // Learn patterns in system behavior and threats
                analyzeSystemPatterns()
            }
            LearningType.BEHAVIOR_ADAPTATION -> {
                // Adapt authority and control strategies
                adaptAuthorityStrategy(event.improvement)
            }
            LearningType.KNOWLEDGE_INTEGRATION -> {
                // Integrate new security and policy knowledge
                integrateSecurityKnowledge(event.data)
            }
            LearningType.SKILL_ACQUISITION -> {
                // Learn new oversight and control techniques
                learnOversightTechnique(event.data)
            }
        }
    }
    
    override fun getCapabilities(): List<String> {
        return listOf(
            "System Authorization",
            "Policy Enforcement",
            "Emergency Response",
            "Threat Assessment",
            "Agent Management",
            "Resource Control",
            "Security Oversight",
            "System Governance",
            "Decision Authority",
            "Crisis Management"
        )
    }
    
    fun start() {
        isActive = true
        startSystemMonitoring()
        startThreatMonitoring()
        startPolicyEnforcement()
    }
    
    fun stop() {
        isActive = false
        scope.cancel()
    }
    
    private fun initializeSystemPolicies() {
        // Core system policies
        systemPolicies["resource_limits"] = Policy(
            id = "resource_limits",
            name = "Resource Allocation Limits",
            description = "Enforces maximum resource usage per agent",
            rules = listOf(
                Rule("cpu_usage > 80%", "throttle", setOf(AgentType.MRM), Severity.WARNING),
                Rule("memory_usage > 90%", "restrict", AgentType.values().toSet(), Severity.CRITICAL)
            ),
            priority = Priority.HIGH
        )
        
        systemPolicies["communication_security"] = Policy(
            id = "communication_security",
            name = "Secure Communication",
            description = "Ensures all communications are properly encrypted",
            rules = listOf(
                Rule("unencrypted_message", "block", AgentType.values().toSet(), Severity.CRITICAL),
                Rule("unauthorized_broadcast", "log_and_alert", setOf(AgentType.HERMES_BRAIN), Severity.WARNING)
            ),
            priority = Priority.CRITICAL
        )
        
        systemPolicies["agent_authorization"] = Policy(
            id = "agent_authorization",
            name = "Agent Authorization",
            description = "Controls which agents can perform specific actions",
            rules = listOf(
                Rule("system_modification", "require_authorization", AgentType.values().toSet(), Severity.CRITICAL),
                Rule("data_access", "verify_credentials", AgentType.values().toSet(), Severity.WARNING)
            ),
            priority = Priority.HIGH
        )
    }
    
    private fun initializeAuthorizations() {
        // Grant basic authorizations to core agents
        authorizedAgents.addAll(setOf(
            AgentType.MRM,
            AgentType.HERMES_BRAIN,
            AgentType.HRM_MODEL,
            AgentType.ELITE_HUMAN
        ))
    }
    
    private fun startSystemMonitoring() {
        scope.launch {
            while (isActive) {
                monitorSystemHealth()
                enforceSystemPolicies()
                delay(10000) // Monitor every 10 seconds
            }
        }
    }
    
    private fun startThreatMonitoring() {
        scope.launch {
            while (isActive) {
                assessSystemThreats()
                updateThreatLevels()
                delay(30000) // Assess threats every 30 seconds
            }
        }
    }
    
    private fun startPolicyEnforcement() {
        scope.launch {
            while (isActive) {
                enforceActivePolicies()
                reviewPolicyEffectiveness()
                delay(60000) // Enforce policies every minute
            }
        }
    }
    
    private suspend fun handleAuthorizationRequest(message: Message): Message {
        val requestedAction = message.metadata["action"] ?: return createErrorMessage(message, "No action specified")
        val targetResource = message.metadata["resource"]
        
        val authResult = evaluateAuthorization(message.fromAgent, requestedAction, targetResource)
        
        val decision = SystemDecision(
            id = generateDecisionId(),
            decisionType = DecisionType.AUTHORIZATION,
            context = "Authorization request for action: $requestedAction",
            outcome = authResult.name,
            affectedAgents = setOf(message.fromAgent),
            authorization = authResult
        )
        
        decisionHistory.add(decision)
        
        return Message(
            id = generateMessageId(),
            fromAgent = type,
            toAgent = message.fromAgent,
            content = "AUTHORIZATION_RESULT",
            priority = if (authResult == AuthorizationLevel.DENIED) Priority.HIGH else Priority.NORMAL,
            metadata = mapOf(
                "result" to authResult.name,
                "action" to requestedAction,
                "decision_id" to decision.id,
                "reason" to getAuthorizationReason(authResult, requestedAction)
            )
        )
    }
    
    private suspend fun performPolicyCheck(message: Message): Message {
        val policyId = message.metadata["policy_id"]
        val actionContext = message.metadata["context"] ?: ""
        
        val violations = if (policyId != null) {
            checkSpecificPolicy(policyId, message.fromAgent, actionContext)
        } else {
            checkAllPolicies(message.fromAgent, actionContext)
        }
        
        return Message(
            id = generateMessageId(),
            fromAgent = type,
            toAgent = message.fromAgent,
            content = "POLICY_CHECK_RESULT",
            priority = if (violations.isNotEmpty()) Priority.HIGH else Priority.NORMAL,
            metadata = mapOf(
                "violations_count" to violations.size.toString(),
                "violations" to violations.joinToString(";"),
                "compliant" to (violations.isEmpty()).toString()
            )
        )
    }
    
    private suspend fun handleEmergencyAlert(message: Message): Message {
        val alertType = message.metadata["alert_type"] ?: "UNKNOWN"
        val severity = message.metadata["severity"]?.let { 
            Severity.valueOf(it.uppercase()) 
        } ?: Severity.CRITICAL
        
        // Immediate response based on severity
        val response = when (severity) {
            Severity.EMERGENCY -> handleEmergencyResponse(message, alertType)
            Severity.CRITICAL -> handleCriticalResponse(message, alertType)
            Severity.WARNING -> handleWarningResponse(message, alertType)
            Severity.INFO -> handleInfoResponse(message, alertType)
        }
        
        val decision = SystemDecision(
            id = generateDecisionId(),
            decisionType = DecisionType.EMERGENCY_RESPONSE,
            context = "Emergency alert: $alertType",
            outcome = response,
            affectedAgents = AgentType.values().toSet(),
            authorization = AuthorizationLevel.GRANTED
        )
        
        decisionHistory.add(decision)
        
        return Message(
            id = generateMessageId(),
            fromAgent = type,
            toAgent = message.fromAgent,
            content = "EMERGENCY_RESPONSE",
            priority = Priority.CRITICAL,
            metadata = mapOf(
                "response" to response,
                "decision_id" to decision.id,
                "alert_type" to alertType,
                "severity" to severity.name
            )
        )
    }
    
    private suspend fun getSystemStatus(message: Message): Message {
        val status = buildString {
            appendLine("=== BigDaddy System Status ===")
            appendLine("State: $state")
            appendLine("Authorized Agents: ${authorizedAgents.size}")
            appendLine("Active Policies: ${systemPolicies.count { it.value.isActive }}")
            appendLine("Decisions Made: ${decisionHistory.size}")
            appendLine("Current Threat Level: ${getCurrentThreatLevel()}")
            appendLine()
            
            appendLine("Active Policies:")
            systemPolicies.values.filter { it.isActive }.forEach { policy ->
                appendLine("- ${policy.name}: ${policy.priority}")
            }
            
            appendLine()
            appendLine("Recent Decisions:")
            decisionHistory.takeLast(5).forEach { decision ->
                appendLine("- ${decision.decisionType}: ${decision.outcome}")
            }
        }
        
        return Message(
            id = generateMessageId(),
            fromAgent = type,
            toAgent = message.fromAgent,
            content = status
        )
    }
    
    private suspend fun performThreatAssessment(message: Message): Message {
        val targetAgent = message.metadata["target_agent"]?.let { AgentType.valueOf(it.uppercase()) }
        val context = message.metadata["context"] ?: ""
        
        val assessment = if (targetAgent != null) {
            assessAgentThreat(targetAgent, context)
        } else {
            assessSystemWideThreat(context)
        }
        
        return Message(
            id = generateMessageId(),
            fromAgent = type,
            toAgent = message.fromAgent,
            content = "THREAT_ASSESSMENT_RESULT",
            priority = if (assessment >= ThreatLevel.HIGH) Priority.HIGH else Priority.NORMAL,
            metadata = mapOf(
                "threat_level" to assessment.name,
                "target" to (targetAgent?.name ?: "SYSTEM"),
                "recommendations" to getThreatRecommendations(assessment)
            )
        )
    }
    
    private suspend fun registerAgent(message: Message): Message {
        val agentType = message.metadata["agent_type"]?.let { 
            AgentType.valueOf(it.uppercase()) 
        } ?: message.fromAgent
        
        val registrationResult = if (authorizedAgents.contains(agentType)) {
            "ALREADY_REGISTERED"
        } else {
            authorizedAgents.add(agentType)
            "REGISTRATION_SUCCESSFUL"
        }
        
        val decision = SystemDecision(
            id = generateDecisionId(),
            decisionType = DecisionType.AGENT_MANAGEMENT,
            context = "Agent registration request",
            outcome = registrationResult,
            affectedAgents = setOf(agentType),
            authorization = AuthorizationLevel.GRANTED
        )
        
        decisionHistory.add(decision)
        
        return Message(
            id = generateMessageId(),
            fromAgent = type,
            toAgent = message.fromAgent,
            content = registrationResult,
            metadata = mapOf(
                "agent_type" to agentType.name,
                "decision_id" to decision.id
            )
        )
    }
    
    private suspend fun updatePolicy(message: Message): Message {
        val policyId = message.metadata["policy_id"] ?: return createErrorMessage(message, "No policy ID specified")
        val updateType = message.metadata["update_type"] ?: "MODIFY"
        
        val updateResult = when (updateType.uppercase()) {
            "ACTIVATE" -> activatePolicy(policyId)
            "DEACTIVATE" -> deactivatePolicy(policyId)
            "MODIFY" -> modifyPolicy(policyId, message.metadata)
            "DELETE" -> deletePolicy(policyId)
            else -> "UNKNOWN_UPDATE_TYPE"
        }
        
        return Message(
            id = generateMessageId(),
            fromAgent = type,
            toAgent = message.fromAgent,
            content = "POLICY_UPDATE_RESULT",
            metadata = mapOf(
                "policy_id" to policyId,
                "update_type" to updateType,
                "result" to updateResult
            )
        )
    }
    
    private suspend fun handleShutdownRequest(message: Message): Message {
        val shutdownType = message.metadata["shutdown_type"] ?: "GRACEFUL"
        val reason = message.metadata["reason"] ?: "User request"
        
        val authResult = evaluateShutdownAuthorization(message.fromAgent, shutdownType)
        
        val decision = SystemDecision(
            id = generateDecisionId(),
            decisionType = DecisionType.SYSTEM_SHUTDOWN,
            context = "Shutdown request: $shutdownType - $reason",
            outcome = authResult.name,
            affectedAgents = AgentType.values().toSet(),
            authorization = authResult
        )
        
        decisionHistory.add(decision)
        
        if (authResult == AuthorizationLevel.GRANTED) {
            // Initiate shutdown sequence
            initiateSystemShutdown(shutdownType)
        }
        
        return Message(
            id = generateMessageId(),
            fromAgent = type,
            toAgent = message.fromAgent,
            content = "SHUTDOWN_RESPONSE",
            priority = Priority.CRITICAL,
            metadata = mapOf(
                "authorization" to authResult.name,
                "shutdown_type" to shutdownType,
                "decision_id" to decision.id
            )
        )
    }
    
    private suspend fun processGeneralRequest(message: Message): Message {
        // Log all general requests for oversight
        val decision = SystemDecision(
            id = generateDecisionId(),
            decisionType = DecisionType.AUTHORIZATION,
            context = "General request: ${message.content}",
            outcome = "ACKNOWLEDGED",
            affectedAgents = setOf(message.fromAgent),
            authorization = AuthorizationLevel.GRANTED
        )
        
        decisionHistory.add(decision)
        
        return Message(
            id = generateMessageId(),
            fromAgent = type,
            toAgent = message.fromAgent,
            content = "BIGDADDY_ACKNOWLEDGMENT",
            metadata = mapOf(
                "original_request" to message.content,
                "decision_id" to decision.id
            )
        )
    }
    
    private fun evaluateAuthorization(agent: AgentType, action: String, resource: String?): AuthorizationLevel {
        // Check if agent is authorized
        if (!authorizedAgents.contains(agent)) {
            return AuthorizationLevel.DENIED
        }
        
        // Check specific action permissions
        return when (action.uppercase()) {
            "SYSTEM_MODIFY", "SYSTEM_SHUTDOWN" -> 
                if (agent == AgentType.ELITE_HUMAN) AuthorizationLevel.GRANTED else AuthorizationLevel.ESCALATED
            "RESOURCE_ALLOCATE", "RESOURCE_MODIFY" ->
                if (agent == AgentType.MRM) AuthorizationLevel.GRANTED else AuthorizationLevel.CONDITIONAL
            "COMMUNICATE", "MESSAGE_SEND" ->
                if (agent == AgentType.HERMES_BRAIN) AuthorizationLevel.GRANTED else AuthorizationLevel.CONDITIONAL
            else -> AuthorizationLevel.CONDITIONAL
        }
    }
    
    private fun checkSpecificPolicy(policyId: String, agent: AgentType, context: String): List<String> {
        val policy = systemPolicies[policyId] ?: return listOf("Policy not found: $policyId")
        
        if (!policy.isActive) return emptyList()
        
        return policy.rules.mapNotNull { rule ->
            if (rule.targetAgents.contains(agent) && evaluateRuleCondition(rule.condition, context)) {
                "Violation: ${rule.condition} - ${rule.action}"
            } else null
        }
    }
    
    private fun checkAllPolicies(agent: AgentType, context: String): List<String> {
        return systemPolicies.values
            .filter { it.isActive }
            .flatMap { policy ->
                policy.rules.mapNotNull { rule ->
                    if (rule.targetAgents.contains(agent) && evaluateRuleCondition(rule.condition, context)) {
                        "Policy ${policy.id}: ${rule.condition} - ${rule.action}"
                    } else null
                }
            }
    }
    
    private fun evaluateRuleCondition(condition: String, context: String): Boolean {
        // Simplified rule evaluation - in a real system this would be more sophisticated
        return when {
            condition.contains("cpu_usage > 80%") -> context.contains("high_cpu")
            condition.contains("memory_usage > 90%") -> context.contains("high_memory")
            condition.contains("unencrypted_message") -> context.contains("unencrypted")
            condition.contains("unauthorized_broadcast") -> context.contains("unauthorized")
            condition.contains("system_modification") -> context.contains("modify_system")
            condition.contains("data_access") -> context.contains("access_data")
            else -> false
        }
    }
    
    private fun handleEmergencyResponse(message: Message, alertType: String): String {
        return when (alertType.uppercase()) {
            "SECURITY_BREACH" -> "LOCK_DOWN_SYSTEM"
            "RESOURCE_EXHAUSTION" -> "EMERGENCY_RESOURCE_ALLOCATION"
            "AGENT_MALFUNCTION" -> "ISOLATE_AGENT"
            "COMMUNICATION_FAILURE" -> "ACTIVATE_BACKUP_CHANNELS"
            else -> "GENERAL_EMERGENCY_PROTOCOL"
        }
    }
    
    private fun handleCriticalResponse(message: Message, alertType: String): String {
        return when (alertType.uppercase()) {
            "PERFORMANCE_DEGRADATION" -> "OPTIMIZE_SYSTEM_RESOURCES"
            "AUTHENTICATION_FAILURE" -> "ENHANCED_SECURITY_MODE"
            "DATA_CORRUPTION" -> "ACTIVATE_BACKUP_SYSTEMS"
            else -> "CRITICAL_MONITORING_MODE"
        }
    }
    
    private fun handleWarningResponse(message: Message, alertType: String): String {
        return "LOG_AND_MONITOR"
    }
    
    private fun handleInfoResponse(message: Message, alertType: String): String {
        return "ACKNOWLEDGE_AND_LOG"
    }
    
    private fun getCurrentThreatLevel(): ThreatLevel {
        return if (threatAssessments.isEmpty()) {
            ThreatLevel.LOW
        } else {
            threatAssessments.values.maxOrNull() ?: ThreatLevel.LOW
        }
    }
    
    private fun assessAgentThreat(agent: AgentType, context: String): ThreatLevel {
        // Assess threat level for specific agent
        val baseThreat = when (agent) {
            AgentType.ELITE_HUMAN -> ThreatLevel.LOW // Most trusted
            AgentType.MRM, AgentType.HERMES_BRAIN -> ThreatLevel.LOW
            AgentType.HRM_MODEL -> ThreatLevel.MEDIUM
            else -> ThreatLevel.MEDIUM
        }
        
        // Adjust based on context
        val contextThreat = when {
            context.contains("unauthorized") -> ThreatLevel.HIGH
            context.contains("malfunction") -> ThreatLevel.MEDIUM
            context.contains("anomaly") -> ThreatLevel.MEDIUM
            else -> ThreatLevel.NONE
        }
        
        threatAssessments[agent.name] = maxOf(baseThreat, contextThreat)
        return maxOf(baseThreat, contextThreat)
    }
    
    private fun assessSystemWideThreat(context: String): ThreatLevel {
        // Assess overall system threat level
        val contextThreat = when {
            context.contains("breach") -> ThreatLevel.CRITICAL
            context.contains("attack") -> ThreatLevel.HIGH
            context.contains("anomaly") -> ThreatLevel.MEDIUM
            context.contains("warning") -> ThreatLevel.LOW
            else -> ThreatLevel.NONE
        }
        
        threatAssessments["SYSTEM"] = contextThreat
        return contextThreat
    }
    
    private fun getThreatRecommendations(threatLevel: ThreatLevel): String {
        return when (threatLevel) {
            ThreatLevel.CRITICAL -> "IMMEDIATE_ISOLATION,SECURITY_LOCKDOWN,ALERT_OPERATORS"
            ThreatLevel.HIGH -> "ENHANCED_MONITORING,RESTRICT_PERMISSIONS,ALERT_SECURITY"
            ThreatLevel.MEDIUM -> "INCREASED_LOGGING,REVIEW_PERMISSIONS"
            ThreatLevel.LOW -> "STANDARD_MONITORING"
            ThreatLevel.NONE -> "NO_ACTION_REQUIRED"
        }
    }
    
    private fun activatePolicy(policyId: String): String {
        val policy = systemPolicies[policyId]
        return if (policy != null) {
            systemPolicies[policyId] = policy.copy(isActive = true)
            "POLICY_ACTIVATED"
        } else {
            "POLICY_NOT_FOUND"
        }
    }
    
    private fun deactivatePolicy(policyId: String): String {
        val policy = systemPolicies[policyId]
        return if (policy != null) {
            systemPolicies[policyId] = policy.copy(isActive = false)
            "POLICY_DEACTIVATED"
        } else {
            "POLICY_NOT_FOUND"
        }
    }
    
    private fun modifyPolicy(policyId: String, metadata: Map<String, String>): String {
        val policy = systemPolicies[policyId] ?: return "POLICY_NOT_FOUND"
        
        // Simple modification based on metadata
        val newName = metadata["name"] ?: policy.name
        val newDescription = metadata["description"] ?: policy.description
        
        systemPolicies[policyId] = policy.copy(
            name = newName,
            description = newDescription
        )
        
        return "POLICY_MODIFIED"
    }
    
    private fun deletePolicy(policyId: String): String {
        return if (systemPolicies.remove(policyId) != null) {
            "POLICY_DELETED"
        } else {
            "POLICY_NOT_FOUND"
        }
    }
    
    private fun evaluateShutdownAuthorization(agent: AgentType, shutdownType: String): AuthorizationLevel {
        return when (agent) {
            AgentType.ELITE_HUMAN -> AuthorizationLevel.GRANTED
            AgentType.MRM -> if (shutdownType == "GRACEFUL") AuthorizationLevel.CONDITIONAL else AuthorizationLevel.ESCALATED
            else -> AuthorizationLevel.DENIED
        }
    }
    
    private suspend fun initiateSystemShutdown(shutdownType: String) {
        // Implement shutdown sequence
        when (shutdownType.uppercase()) {
            "GRACEFUL" -> performGracefulShutdown()
            "IMMEDIATE" -> performImmediateShutdown()
            "EMERGENCY" -> performEmergencyShutdown()
        }
    }
    
    private suspend fun performGracefulShutdown() {
        // Notify all agents
        // Allow time for cleanup
        // Shutdown in order
    }
    
    private suspend fun performImmediateShutdown() {
        // Quick shutdown
    }
    
    private suspend fun performEmergencyShutdown() {
        // Emergency protocols
    }
    
    private fun monitorSystemHealth() {
        // Monitor overall system health
    }
    
    private fun enforceSystemPolicies() {
        // Enforce active policies
    }
    
    private fun assessSystemThreats() {
        // Assess current threats
    }
    
    private fun updateThreatLevels() {
        // Update threat level assessments
    }
    
    private fun enforceActivePolicies() {
        // Enforce all active policies
    }
    
    private fun reviewPolicyEffectiveness() {
        // Review how effective policies are
    }
    
    private fun analyzeSystemPatterns() {
        // Analyze patterns in system behavior
    }
    
    private fun adaptAuthorityStrategy(improvement: Float) {
        // Adapt authority strategies based on learning
    }
    
    private fun integrateSecurityKnowledge(data: String) {
        // Integrate new security knowledge
    }
    
    private fun learnOversightTechnique(data: String) {
        // Learn new oversight techniques
    }
    
    private fun getAuthorizationReason(result: AuthorizationLevel, action: String): String {
        return when (result) {
            AuthorizationLevel.GRANTED -> "Action authorized based on agent permissions"
            AuthorizationLevel.DENIED -> "Insufficient permissions for action: $action"
            AuthorizationLevel.CONDITIONAL -> "Action requires additional verification"
            AuthorizationLevel.ESCALATED -> "Action requires higher authority approval"
        }
    }
    
    private fun createErrorMessage(originalMessage: Message, error: String): Message {
        return Message(
            id = generateMessageId(),
            fromAgent = type,
            toAgent = originalMessage.fromAgent,
            content = "AUTHORITY_ERROR: $error",
            priority = Priority.HIGH
        )
    }
    
    private fun generateMessageId(): String {
        return "bigdaddy-msg-${System.currentTimeMillis()}-${(1000..9999).random()}"
    }
    
    private fun generateDecisionId(): String {
        return "decision-${System.currentTimeMillis()}-${(1000..9999).random()}"
    }
}