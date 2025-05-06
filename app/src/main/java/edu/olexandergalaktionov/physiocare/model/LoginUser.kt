package edu.olexandergalaktionov.physiocare.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("login")
    val username: String,
    @SerializedName("password")
    val password: String
)

data class LoginResponse(
    @SerializedName("token")
    val token: String?,
    @SerializedName("rol")
    val rol: String?,
    @SerializedName("error")
    val error: String?,
)