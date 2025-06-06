package edu.olexandergalaktionov.physiocare.ui.appointment.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import edu.olexandergalaktionov.physiocare.data.PhysioCareRepository
import edu.olexandergalaktionov.physiocare.model.Appointment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para gestionar la lógica de negocio relacionada con los detalles de una cita.
 *
 * @param repository Repositorio de datos.
 * @constructor Crea una instancia de [AppointmentDetailViewModel].
 * @author Olexandr Galaktionov Tsisar
 */
class AppointmentDetailViewModel(private val repository: PhysioCareRepository) : ViewModel() {

    private val _appointment = MutableStateFlow<Appointment?>(null)
    val appointment: StateFlow<Appointment?> = _appointment

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    /**
     * Carga una cita específica por su ID.
     *
     * @param id ID de la cita.
     */
    fun loadAppointment(id: String) {
        viewModelScope.launch {
            try {
                val result = repository.getAppointmentById(id)
                _appointment.value = result
            } catch (e: Exception) {
                _error.value = "No se pudo cargar la cita"
            }
        }
    }
}

/**
 * Factory para crear instancias de [AppointmentDetailViewModel].
 */
class AppointmentDetailViewModelFactory(private val repository: PhysioCareRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AppointmentDetailViewModel(repository) as T
    }
}
