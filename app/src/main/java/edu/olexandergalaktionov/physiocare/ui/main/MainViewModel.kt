package edu.olexandergalaktionov.physiocare.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import edu.olexandergalaktionov.physiocare.data.PhysioCareRepository
import edu.olexandergalaktionov.physiocare.model.LoginRequest
import edu.olexandergalaktionov.physiocare.model.LoginState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla principal de la aplicación.
 * Maneja el estado de inicio de sesión y las interacciones del usuario.
 *
 * @param repository Repositorio para acceder a los datos de la aplicación.
 */
class MainViewModel(private val repository: PhysioCareRepository) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState>
        get() = _loginState

    /**
     * Función para iniciar sesión.
     * Recibe el nombre de usuario y la contraseña, y llama al repositorio con loginRequest para autenticar al usuario.
     */
    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val response = repository.login(LoginRequest(username, password))
                _loginState.value = LoginState.Success(response)
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    /**
     * Función para cerrar sesión.
     * Llama al repositorio para cerrar sesión y actualiza el estado de inicio de sesión.
     */
    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _loginState.value = LoginState.Idle
        }
    }
}

/**
 * Factory para crear instancias de MainViewModel.
 */
@Suppress("UNCHECKED_CAST")
class MainViewModelFactory(private val repository: PhysioCareRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(repository) as T
    }
}
