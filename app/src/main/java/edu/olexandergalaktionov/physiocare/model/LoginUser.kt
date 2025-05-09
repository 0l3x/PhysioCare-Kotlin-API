package edu.olexandergalaktionov.physiocare.model

import com.google.gson.annotations.SerializedName

/**
 * Request para iniciar sesión
 * @param username Nombre de usuario
 * @param password Contraseña
 */
data class LoginRequest(
    @SerializedName("login")
    val username: String,
    @SerializedName("password")
    val password: String
)

/**
 * Respuesta de la API al iniciar sesión
 * @param token Token de autenticación
 * @param rol Rol del usuario (paciente o fisioterapeuta)
 * @param usuarioId ID del usuario
 * @param error Mensaje de error (si lo hay)
 */
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

/**
 * Estado de la pantalla de inicio de sesión
 * @param Idle Estado inicial
 * @param Loading Estado de carga
 * @param Success Estado de éxito
 * @param Error Estado de error
 */
sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val response: LoginResponse) : LoginState()
    data class Error(val message: String) : LoginState()
}