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
    @SerializedName("usuarioId")
    val usuarioId: String?,
    @SerializedName("error")
    val error: String?,
)

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val response: LoginResponse) : LoginState()
    data class Error(val message: String) : LoginState()
}