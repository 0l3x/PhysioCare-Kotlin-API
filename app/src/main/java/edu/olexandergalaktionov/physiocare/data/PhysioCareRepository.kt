package edu.olexandergalaktionov.physiocare.data

import android.util.Log
import edu.olexandergalaktionov.physiocare.model.Appointment
import edu.olexandergalaktionov.physiocare.model.AppointmentPostRequest
import edu.olexandergalaktionov.physiocare.model.AppointmentsResponse
import edu.olexandergalaktionov.physiocare.model.LoginRequest
import edu.olexandergalaktionov.physiocare.model.LoginResponse
import edu.olexandergalaktionov.physiocare.model.Physio
import edu.olexandergalaktionov.physiocare.model.RecordResponse
import edu.olexandergalaktionov.physiocare.model.RecordsResponse
import edu.olexandergalaktionov.physiocare.utils.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * Esta clase actúa como un repositorio que interactúa con la fuente de datos remota.
 * @author Olexandr Galaktionov Tsisar
 */
class PhysioCareRepository(private val sessionManager: SessionManager) {
    val TAG = PhysioCareRepository::class.java.simpleName
    private val remoteDataSource = RemoteDataSource

    /**
     * Realiza la autenticación del usuario y devuelve el token de acceso.
     * @param request Objeto de solicitud de inicio de sesión.
     * @return Respuesta de inicio de sesión que contiene el token y otros datos.
     */
    suspend fun login(request: LoginRequest): LoginResponse {
        val response = remoteDataSource.login(request)

        if (response.token != null) {
            sessionManager.saveSession(response.token, response.usuarioId.toString(), response.rol.toString())
        } else {
            Log.e(TAG, "Token nulo recibido: ${response.error}")
        }

        return response
    }

    /**
     * Cierra la sesión del usuario.
     */
    suspend fun logout() {
        sessionManager.clearSession()
    }

    /**
     * Obtiene el flujo de sesión.
     */
    private fun getSessionFlow(): Flow<Pair<String?, String?>> = sessionManager.sessionFlow

    /**
     * Obtiene todos los registros de pacientes.
     */
    suspend fun getAllRecords(): RecordsResponse {
        val (token, _) = getSessionFlow().first()
        return remoteDataSource.getAllRecords(token.toString())
    }

    /**
     * Obtiene el expediente del paciente autenticado.
     */
    suspend fun getRecordByPatientId(patientId: String): RecordResponse {
        val (token, _) = getSessionFlow().first()
        return remoteDataSource.getRecordByPatientId(token.toString(), patientId)
    }

    /**
     * Obtiene las citas separadas (futuras y pasadas) por paciente.
     */
    suspend fun getAppointmentsByPatientId(patientId: String): AppointmentsResponse {
        val (token, _) = getSessionFlow().first()
        return remoteDataSource.getAppointmentsByPatientId(token.toString(), patientId)
    }

    /**
     * Obtiene una cita por ID.
     */
    suspend fun getAppointmentById(id: String): Appointment {
        val (token, _) = getSessionFlow().first()
        return RemoteDataSource.getAppointmentById(token.toString(), id)
    }

    /**
     * Obtiene la lista de citas por ID de fisioterapeuta(propias).
     */
    suspend fun getAppointmentsByPhysioId(physioId: String): List<Appointment> {
        val (token, _) = sessionManager.sessionFlow.first()
        return remoteDataSource.getAppointmentsByPhysioId(token!!, physioId)
    }

    /**
     * Elimina una cita por ID.
     */
    suspend fun deleteAppointmentById(appointmentId: String) {
        val (token, _) = sessionManager.sessionFlow.first()
        remoteDataSource.deleteAppointmentById(token!!, appointmentId)
    }

    /**
     * Obtiene todos los fisioterapeutas.
     */
    suspend fun getAllPhysios() : List<Physio> {
        val (token, _) = sessionManager.sessionFlow.first()
        return remoteDataSource.getAllPhysios(token.toString())
    }

    /**
     * Crea una cita en el expediente del paciente.
     */
    suspend fun postAppointmentToRecord(
        recordId: String,
        request: AppointmentPostRequest
    ): Boolean {
        val (token, _) = sessionManager.sessionFlow.first()
        return remoteDataSource.postAppointmentToRecord(token.toString(), recordId, request)
    }
}