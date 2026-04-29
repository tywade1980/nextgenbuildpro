package com.nextgen.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * NextGen Backend Core Application
 * 
 * Provides comprehensive backend services including:
 * - RESTful API endpoints
 * - Database operations (PostgreSQL + Vector DB)
 * - Real-time WebSocket connections
 * - Integration hub management
 * - MCP server coordination
 * - Voice command processing backend
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling
class NextGenBackendApplication

fun main(args: Array<String>) {
    runApplication<NextGenBackendApplication>(*args)
}