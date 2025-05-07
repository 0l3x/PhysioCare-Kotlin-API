package edu.olexandergalaktionov.physiocare.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import edu.olexandergalaktionov.physiocare.data.PhysioCareRepository
import edu.olexandergalaktionov.physiocare.model.Appointment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PhysioViewModel(private val repository: PhysioCareRepository) : ViewModel() {

    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments: StateFlow<List<Appointment>> = _appointments

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadAppointmentsForPhysio(physioId: String) {
        viewModelScope.launch {
            _error.value = null
            try {
                val result = repository.getAppointmentsByPhysioId(physioId)
                _appointments.value = result
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun deleteAppointmentById(appointmentId: String, physioId: String) {
        viewModelScope.launch {
            _error.value = null
            try {
                repository.deleteAppointmentById(appointmentId)
                loadAppointmentsForPhysio(physioId) // recargar tras eliminar
            } catch (e: Exception) {
                _error.value = "No se pudo eliminar: ${e.message}"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}

class PhysioViewModelFactory(private val repository: PhysioCareRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PhysioViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PhysioViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
