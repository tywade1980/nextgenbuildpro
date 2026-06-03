package com.nextgen.backend.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * Web configuration for NextGen Backend
 */
@Configuration
@EnableWebMvc
class WebConfig : WebMvcConfigurer {
    
    @Value("\${nextgen.cors.allowed-origins:*}")
    private lateinit var allowedOrigins: String
    
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOriginPatterns = listOf(allowedOrigins)
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true
        
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
    
    @Bean("taskExecutor")
    fun taskExecutor(): Executor {
        return Executors.newFixedThreadPool(16)
    }
}