package com.constructionmanager.domain.model

import kotlinx.datetime.LocalDateTime

data class User(
    val id: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val company: String,
    val role: UserRole,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime,
    val lastLoginAt: LocalDateTime? = null,
    val profileImageUrl: String? = null,
    val phoneNumber: String? = null,
    val address: String? = null,
    val city: String? = null,
    val state: String? = null,
    val zipCode: String? = null,
    val subscriptionTier: SubscriptionTier = SubscriptionTier.FREE,
    val preferences: UserPreferences = UserPreferences()
)

enum class UserRole {
    ADMIN,
    PROJECT_MANAGER,
    FOREMAN,
    CONTRACTOR,
    WORKER,
    CLIENT,
    VIEWER
}

enum class SubscriptionTier {
    FREE,
    PROFESSIONAL,
    ENTERPRISE
}

data class UserPreferences(
    val defaultRegion: String = "Midwest",
    val currency: String = "USD",
    val measurementUnit: String = "Imperial",
    val notificationsEnabled: Boolean = true,
    val emailNotifications: Boolean = true,
    val pushNotifications: Boolean = true,
    val darkMode: Boolean = false,
    val language: String = "en"
)

data class AuthToken(
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: LocalDateTime,
    val tokenType: String = "Bearer"
)