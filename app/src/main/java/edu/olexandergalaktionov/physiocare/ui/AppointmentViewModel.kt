package edu.olexandergalaktionov.physiocare.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import edu.olexandergalaktionov.physiocare.data.PhysioCareRepository
import edu.olexandergalaktionov.physiocare.model.Appointment
import edu.olexandergalaktionov.physiocare.model.LoginState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AppointmentViewModel(private val repository: PhysioCareRepository) : ViewModel() {
    private val _appointmentList = MutableStateFlow<List<Appointment>>(emptyList())
    val appointmentList: StateFlow<List<Appointment>> = _appointmentList

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun fetchAppointments() {
        viewModelScope.launch {
            try {

                val records = repository.getAllRecords()
                val resultado = records.resultado
                if (resultado != null) {
                    val allAppointments = resultado.flatMap { it.appointments!! }
                    _appointmentList.value = allAppointments
                    _error.value = null
                } else {
                    _appointmentList.value = emptyList()
                    _error.value = "No se encontraron registros"
                }

            } catch (e: Exception) {
                _error.value = e.message ?: "Error desconocido"
            }
        }
    }
}

class AppointmentViewModelFactory(private val repository: PhysioCareRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AppointmentViewModel(repository) as T
    }
}
