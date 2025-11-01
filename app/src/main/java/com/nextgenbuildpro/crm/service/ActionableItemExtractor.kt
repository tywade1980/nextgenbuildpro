package com.nextgenbuildpro.crm.service

import android.content.Context
import android.util.Log
import com.nextgenbuildpro.ai.llm.LLMService
import com.nextgenbuildpro.ai.llm.LLMContext
import com.nextgenbuildpro.shared.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * ActionableItemExtractor - Extracts actionable items from meeting transcriptions
 * 
 * Uses LLM to intelligently parse transcriptions and identify:
 * - Action items and tasks
 * - Assigned responsibilities
 * - Due dates and priorities
 * - Project references
 * - Follow-up items
 */
class ActionableItemExtractor(
    private val context: Context,
    private val llmService: LLMService
) {
    private val TAG = "ActionableItemExtractor"
    
    /**
     * Extract actionable items from a meeting transcription
     * @param transcription The meeting transcription text
     * @param meetingContext Context about the meeting (title, participants, etc.)
     * @return List of extracted actionable items as NextGenTask objects
     */
    suspend fun extractActionableItems(
        transcription: String,
        meetingContext: MeetingContext
    ): Result<List<NextGenTask>> = withContext(Dispatchers.Default) {
        try {
            Log.i(TAG, "Extracting actionable items from transcription (${transcription.length} chars)")
            
            // Build the extraction prompt
            val extractionPrompt = buildExtractionPrompt(transcription, meetingContext)
            
            // Create LLM context
            val llmContext = LLMContext(
                conversationId = UUID.randomUUID().toString(),
                systemPrompt = getSystemPrompt(),
                previousMessages = emptyList(),
                metadata = mapOf(
                    "meetingId" to meetingContext.meetingId,
                    "meetingType" to meetingContext.meetingType.toString()
                )
            )
            
            // Call LLM service to extract items
            val response = llmService.generateResponse(
                prompt = extractionPrompt,
                context = llmContext,
                agentType = AgentType.CEO_PERSONAL_ASSISTANT
            )
            
            if (response.isFailure) {
                Log.e(TAG, "LLM service failed: ${response.exceptionOrNull()?.message}")
                return@withContext Result.failure(response.exceptionOrNull() ?: Exception("Unknown error"))
            }
            
            val llmResponse = response.getOrNull()!!
            Log.d(TAG, "LLM response received: ${llmResponse.content.take(200)}")
            
            // Parse LLM response to extract tasks
            val tasks = parseActionableItems(llmResponse.content, meetingContext)
            
            Log.i(TAG, "Extracted ${tasks.size} actionable items")
            Result.success(tasks)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting actionable items", e)
            Result.failure(e)
        }
    }
    
    /**
     * Build the extraction prompt for the LLM
     */
    private fun buildExtractionPrompt(transcription: String, context: MeetingContext): String {
        return """
            Analyze the following meeting transcription and extract all actionable items, tasks, and commitments.
            
            Meeting Information:
            - Title: ${context.meetingTitle}
            - Type: ${context.meetingType}
            - Date: ${context.meetingDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}
            - Participants: ${context.participants.joinToString(", ")}
            
            Transcription:
            $transcription
            
            Please extract actionable items in JSON format with the following structure:
            {
              "actionableItems": [
                {
                  "title": "Brief task title",
                  "description": "Detailed description of what needs to be done",
                  "assignedTo": "Person or role responsible (if mentioned)",
                  "dueDate": "ISO date if mentioned, otherwise null",
                  "priority": "LOW|MEDIUM|HIGH|CRITICAL",
                  "type": "task|followup|decision|requirement",
                  "projectReference": "Related project name if mentioned",
                  "dependencies": ["List of dependencies if mentioned"]
                }
              ],
              "summary": "Brief summary of the meeting",
              "keyDecisions": ["List of key decisions made"],
              "nextSteps": ["List of next steps discussed"]
            }
            
            Focus on concrete, actionable items that require follow-up. Be specific and extract details mentioned in the conversation.
            Return ONLY the JSON response, no additional text.
        """.trimIndent()
    }
    
    /**
     * Get the system prompt for the LLM
     */
    private fun getSystemPrompt(): String {
        return """
            You are an expert construction project manager assistant specializing in extracting actionable items from meeting transcriptions.
            Your role is to:
            1. Identify all tasks, commitments, and action items from conversations
            2. Determine responsibilities, due dates, and priorities
            3. Recognize project-specific terminology and references
            4. Distinguish between decisions, tasks, and discussions
            5. Extract information accurately without hallucinating details
            
            Always provide responses in valid JSON format. Be thorough but precise.
        """.trimIndent()
    }
    
    /**
     * Parse the LLM response to extract actionable items as NextGenTask objects
     */
    private fun parseActionableItems(
        llmResponse: String,
        meetingContext: MeetingContext
    ): List<NextGenTask> {
        val tasks = mutableListOf<NextGenTask>()
        
        try {
            // Try to extract JSON from response (handle potential markdown formatting)
            val jsonContent = extractJsonFromResponse(llmResponse)
            val jsonObject = JSONObject(jsonContent)
            
            val actionableItems = jsonObject.optJSONArray("actionableItems") ?: JSONArray()
            
            for (i in 0 until actionableItems.length()) {
                val item = actionableItems.getJSONObject(i)
                
                // Parse priority
                val priorityStr = item.optString("priority", "MEDIUM")
                val priority = try {
                    Priority.valueOf(priorityStr)
                } catch (e: Exception) {
                    Priority.MEDIUM
                }
                
                // Parse due date
                val dueDateStr = item.optString("dueDate", null)
                val dueDate = if (dueDateStr != null && dueDateStr != "null" && dueDateStr.isNotBlank()) {
                    try {
                        LocalDateTime.parse(dueDateStr)
                    } catch (e: Exception) {
                        null
                    }
                } else {
                    null
                }
                
                // Parse assigned agent type based on task type or description
                val taskType = item.optString("type", "task")
                val description = item.optString("description", "")
                val assignedAgent = determineAgentType(taskType, description)
                
                // Create NextGenTask
                val task = NextGenTask(
                    id = UUID.randomUUID().toString(),
                    title = item.optString("title", "Untitled Task"),
                    description = item.optString("description", ""),
                    type = taskType,
                    assignedAgent = assignedAgent,
                    priority = priority,
                    status = TaskStatus.PENDING,
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now(),
                    dueDate = dueDate,
                    metadata = mapOf(
                        "source" to "meeting_recording",
                        "meetingId" to meetingContext.meetingId,
                        "meetingTitle" to meetingContext.meetingTitle,
                        "extractedFrom" to "transcription",
                        "assignedTo" to item.optString("assignedTo", ""),
                        "projectReference" to item.optString("projectReference", ""),
                        "keyDecisions" to jsonObject.optJSONArray("keyDecisions")?.let { 
                            List(it.length()) { i -> it.getString(i) }
                        }.orEmpty()
                    ),
                    requiresHumanApproval = true, // All extracted tasks require approval
                    automationLevel = AutomationLevel.HUMAN_IN_LOOP
                )
                
                tasks.add(task)
                Log.d(TAG, "Parsed task: ${task.title} (Priority: ${task.priority})")
            }
            
            // Store meeting summary in metadata if available
            val summary = jsonObject.optString("summary", "")
            if (summary.isNotBlank()) {
                Log.d(TAG, "Meeting summary: $summary")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing actionable items from LLM response", e)
            // Return empty list rather than failing completely
        }
        
        return tasks
    }
    
    /**
     * Extract JSON content from LLM response (handles markdown code blocks)
     */
    private fun extractJsonFromResponse(response: String): String {
        // Remove markdown code block formatting if present
        var content = response.trim()
        
        if (content.startsWith("```json")) {
            content = content.removePrefix("```json").removeSuffix("```").trim()
        } else if (content.startsWith("```")) {
            content = content.removePrefix("```").removeSuffix("```").trim()
        }
        
        return content
    }
    
    /**
     * Determine appropriate agent type based on task characteristics
     */
    private fun determineAgentType(taskType: String, description: String): AgentType {
        val descLower = description.lowercase()
        
        return when {
            // Financial/Estimating tasks
            descLower.contains("estimate") || descLower.contains("budget") || 
            descLower.contains("cost") || descLower.contains("invoice") ||
            descLower.contains("payment") -> AgentType.CFO_FINANCIAL_ORCHESTRATOR
            
            // Design/Technical tasks
            descLower.contains("design") || descLower.contains("blueprint") ||
            descLower.contains("cad") || descLower.contains("drawing") ||
            descLower.contains("specification") -> AgentType.CTO_DESIGN_ORCHESTRATOR
            
            // Safety/Compliance tasks
            descLower.contains("safety") || descLower.contains("permit") ||
            descLower.contains("inspection") || descLower.contains("compliance") ||
            descLower.contains("osha") -> AgentType.CSO_SAFETY_ORCHESTRATOR
            
            // Client/CRM tasks
            descLower.contains("client") || descLower.contains("customer") ||
            descLower.contains("meeting") || descLower.contains("follow up") ||
            descLower.contains("contract") -> AgentType.CHRO_CLIENT_HR_ORCHESTRATOR
            
            // Operations/Project Management tasks
            descLower.contains("schedule") || descLower.contains("equipment") ||
            descLower.contains("material") || descLower.contains("crew") ||
            descLower.contains("delivery") -> AgentType.COO_OPERATIONS_ORCHESTRATOR
            
            // Default to Personal Assistant for general tasks
            else -> AgentType.CEO_PERSONAL_ASSISTANT
        }
    }
}

