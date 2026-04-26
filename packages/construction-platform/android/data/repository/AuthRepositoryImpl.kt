package com.constructionmanager.data.repository

import android.content.SharedPreferences
import com.constructionmanager.data.network.AuthApiService
import com.constructionmanager.domain.model.AuthToken
import com.constructionmanager.domain.model.User
import com.constructionmanager.domain.model.UserRole
import com.constructionmanager.domain.model.SubscriptionTier
import com.constructionmanager.domain.model.UserPreferences
import com.constructionmanager.domain.repository.AuthRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService,
    private val sharedPreferences: SharedPreferences
) : AuthRepository {
    
    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_IS_DEMO = "is_demo"
    }
    
    override suspend fun login(email: String, password: String): User {
        return try {
            // For demo purposes, simulate API call
            if (email == "demo@constructionmanager.com" && password == "demo123") {
                val demoUser = createDemoUser()
                saveUserSession(demoUser, isDemoUser = true)
                demoUser
            } else {
                // In production, this would make an actual API call
                val response = authApiService.login(email, password)
                saveUserSession(response.user, isDemoUser = false)
                response.user
            }
        } catch (e: Exception) {
            throw Exception("Invalid email or password")
        }
    }
    
    override suspend fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        company: String
    ): User {
        return try {
            // In production, this would make an actual API call
            val newUser = User(
                id = "user_${System.currentTimeMillis()}",
                email = email,
                firstName = firstName,
                lastName = lastName,
                company = company,
                role = UserRole.PROJECT_MANAGER,
                isActive = true,
                createdAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                lastLoginAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                subscriptionTier = SubscriptionTier.FREE,
                preferences = UserPreferences()
            )
            
            saveUserSession(newUser, isDemoUser = false)
            newUser
        } catch (e: Exception) {
            throw Exception("Registration failed: ${e.message}")
        }
    }
    
    override suspend fun loginAsDemo(): User {
        val demoUser = createDemoUser()
        saveUserSession(demoUser, isDemoUser = true)
        return demoUser
    }
    
    override suspend fun logout() {
        sharedPreferences.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_USER_ID)
            .remove(KEY_USER_EMAIL)
            .remove(KEY_USER_NAME)
            .remove(KEY_IS_DEMO)
            .apply()
    }
    
    override suspend fun isAuthenticated(): Boolean {
        return sharedPreferences.contains(KEY_ACCESS_TOKEN) && 
                sharedPreferences.contains(KEY_USER_ID)
    }
    
    override suspend fun getCurrentUser(): User? {
        if (!isAuthenticated()) return null
        
        val userId = sharedPreferences.getString(KEY_USER_ID, null) ?: return null
        val userEmail = sharedPreferences.getString(KEY_USER_EMAIL, null) ?: return null
        val userName = sharedPreferences.getString(KEY_USER_NAME, null) ?: return null
        val isDemo = sharedPreferences.getBoolean(KEY_IS_DEMO, false)
        
        return if (isDemo) {
            createDemoUser()
        } else {
            // In production, fetch full user data from API
            val nameParts = userName.split(" ")
            User(
                id = userId,
                email = userEmail,
                firstName = nameParts.getOrNull(0) ?: "",
                lastName = nameParts.getOrNull(1) ?: "",
                company = "Construction Company",
                role = UserRole.PROJECT_MANAGER,
                isActive = true,
                createdAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                lastLoginAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                subscriptionTier = SubscriptionTier.PROFESSIONAL,
                preferences = UserPreferences()
            )
        }
    }
    
    override suspend fun refreshToken(): AuthToken {
        // In production, make API call to refresh token
        return AuthToken(
            accessToken = "refreshed_token_${System.currentTimeMillis()}",
            refreshToken = "new_refresh_token_${System.currentTimeMillis()}",
            expiresAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        )
    }
    
    override suspend fun resetPassword(email: String) {
        // In production, make API call to send reset password email
        // For demo purposes, just simulate success
    }
    
    override suspend fun updateProfile(user: User): User {
        // In production, make API call to update user profile
        return user
    }
    
    override suspend fun changePassword(currentPassword: String, newPassword: String) {
        // In production, make API call to change password
        // For demo purposes, just simulate success
    }
    
    private fun createDemoUser(): User {
        return User(
            id = "demo_user_001",
            email = "demo@constructionmanager.com",
            firstName = "Demo",
            lastName = "Manager",
            company = "Demo Construction Co.",
            role = UserRole.PROJECT_MANAGER,
            isActive = true,
            createdAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            lastLoginAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            subscriptionTier = SubscriptionTier.PROFESSIONAL,
            preferences = UserPreferences(
                defaultRegion = "Midwest",
                notificationsEnabled = true,
                emailNotifications = false // Demo user doesn't get real emails
            )
        )
    }
    
    private fun saveUserSession(user: User, isDemoUser: Boolean) {
        sharedPreferences.edit()
            .putString(KEY_ACCESS_TOKEN, "demo_token_${System.currentTimeMillis()}")
            .putString(KEY_REFRESH_TOKEN, "demo_refresh_${System.currentTimeMillis()}")
            .putString(KEY_USER_ID, user.id)
            .putString(KEY_USER_EMAIL, user.email)
            .putString(KEY_USER_NAME, "${user.firstName} ${user.lastName}")
            .putBoolean(KEY_IS_DEMO, isDemoUser)
            .apply()
    }
}