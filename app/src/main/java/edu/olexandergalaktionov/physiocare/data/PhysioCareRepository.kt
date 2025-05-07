package edu.olexandergalaktionov.physiocare.data

import android.util.Log
import edu.olexandergalaktionov.physiocare.model.Appointment
import edu.olexandergalaktionov.physiocare.model.AppointmentsResponse
import edu.olexandergalaktionov.physiocare.model.LoginRequest
import edu.olexandergalaktionov.physiocare.model.LoginResponse
import edu.olexandergalaktionov.physiocare.model.RecordResponse
import edu.olexandergalaktionov.physiocare.model.RecordsResponse
import edu.olexandergalaktionov.physiocare.utils.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class PhysioCareRepository(private val sessionManager: SessionManager) {
    val TAG = PhysioCareRepository::class.java.simpleName
    private val remoteDataSource = RemoteDataSource

    suspend fun login(request: LoginRequest): LoginResponse {
        val response = remoteDataSource.login(request)

        if (response.token != null) {
            sessionManager.saveSession(response.token, response.usuarioId.toString(), response.rol.toString())
        } else {
            Log.e(TAG, "Token nulo recibido: ${response.error}")
        }

        return response
    }

    suspend fun logout() {
        sessionManager.clearSession()
    }

    private fun getSessionFlow(): Flow<Pair<String?, String?>> = sessionManager.sessionFlow

    suspend fun getAllRecords(): RecordsResponse {
        val (token, _) = getSessionFlow().first()
        return remoteDataSource.getAllRecords(token.toString())
    }

    // Obtener expediente del paciente autenticado
    suspend fun getRecordByPatientId(patientId: String): RecordResponse {
        val (token, _) = getSessionFlow().first()
        return remoteDataSource.getRecordByPatientId(token.toString(), patientId)
    }

    //  Obtener citas separadas (futuras y pasadas) por paciente
    suspend fun getAppointmentsByPatientId(patientId: String): AppointmentsResponse {
        val (token, _) = getSessionFlow().first()
        return remoteDataSource.getAppointmentsByPatientId(token.toString(), patientId)
    }

    suspend fun getAppointmentById(id: String): Appointment {
        val (token, _) = getSessionFlow().first()
        return RemoteDataSource.getAppointmentById(token.toString(), id)
    }


    suspend fun getAppointmentsByPhysioId(physioId: String): List<Appointment> {
        val (token, _) = sessionManager.sessionFlow.first()
        return remoteDataSource.getAppointmentsByPhysioId(token!!, physioId)
    }


    suspend fun deleteAppointmentById(appointmentId: String) {
        val (token, _) = sessionManager.sessionFlow.first()
        remoteDataSource.deleteAppointmentById(token!!, appointmentId)
    }



}