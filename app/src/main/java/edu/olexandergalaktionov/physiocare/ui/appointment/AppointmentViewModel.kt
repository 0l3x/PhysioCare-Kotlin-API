package edu.olexandergalaktionov.physiocare.ui.appointment

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import edu.olexandergalaktionov.physiocare.data.PhysioCareRepository
import edu.olexandergalaktionov.physiocare.model.Appointment
import edu.olexandergalaktionov.physiocare.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AppointmentViewModel(private val repository: PhysioCareRepository) : ViewModel() {

    private val _futureAppointments = MutableStateFlow<List<Appointment>>(emptyList())
    val futureAppointments: StateFlow<List<Appointment>> = _futureAppointments

    private val _pastAppointments = MutableStateFlow<List<Appointment>>(emptyList())
    val pastAppointments: StateFlow<List<Appointment>> = _pastAppointments

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun fetchAppointmentsByPatient(patientId: String) {
        viewModelScope.launch {
            try {
                // 1. Comprobar si tiene expediente
                val recordResponse = repository.getRecordByPatientId(patientId)
                if (!recordResponse.ok!!) {
                    _error.value = "No dispone de expediente médico."
                    _futureAppointments.value = emptyList()
                    _pastAppointments.value = emptyList()
                    return@launch
                }

                // 2. Obtener citas separadas
                val response = repository.getAppointmentsByPatientId(patientId)

                if (response.ok) {
                    _futureAppointments.value = response.futuras
                    _pastAppointments.value = response.pasadas

                    // 3. Mensajes específicos si no hay citas
                    when {
                        response.futuras.isEmpty() && response.pasadas.isEmpty() ->
                            _error.value = "No dispone de citas registradas."
                        response.futuras.isEmpty() ->
                            _error.value = "No dispone de citas pendientes."
                        else ->
                            _error.value = null
                    }
                } else {
                    _error.value = "No se pudieron recuperar las citas."
                    _futureAppointments.value = emptyList()
                    _pastAppointments.value = emptyList()
                }
            } catch (e: Exception) {
                Log.e("ERRORAppointmentVM", "Error en fetchAppointmentsByPatient: ${e.message}")
                e.printStackTrace()


                _error.value = "Error al cargar citas: ${e.message}"
                _futureAppointments.value = emptyList()
                _pastAppointments.value = emptyList()
            }
        }
    }
}

class AppointmentViewModelFactory(private val repository: PhysioCareRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AppointmentViewModel(repository) as T
    }
}
