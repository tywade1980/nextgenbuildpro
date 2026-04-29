package com.constructionmanager.data.network

import com.constructionmanager.domain.model.User
import com.constructionmanager.domain.model.AuthToken
import retrofit2.http.*

interface AuthApiService {
    
    @FormUrlEncoded
    @POST("auth/login")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): LoginResponse
    
    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): LoginResponse
    
    @FormUrlEncoded
    @POST("auth/refresh")
    suspend fun refreshToken(
        @Field("refresh_token") refreshToken: String
    ): AuthToken
    
    @POST("auth/logout")
    suspend fun logout()
    
    @FormUrlEncoded
    @POST("auth/reset-password")
    suspend fun resetPassword(
        @Field("email") email: String
    )
    
    @GET("auth/me")
    suspend fun getCurrentUser(): User
    
    @PUT("auth/profile")
    suspend fun updateProfile(
        @Body user: User
    ): User
    
    @FormUrlEncoded
    @POST("auth/change-password")
    suspend fun changePassword(
        @Field("current_password") currentPassword: String,
        @Field("new_password") newPassword: String
    )
}

data class LoginResponse(
    val user: User,
    val token: AuthToken
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val company: String
)