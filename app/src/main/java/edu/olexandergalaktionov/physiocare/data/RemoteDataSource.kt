package edu.olexandergalaktionov.physiocare.data

import android.util.Log
import edu.olexandergalaktionov.physiocare.model.Appointment
import edu.olexandergalaktionov.physiocare.model.AppointmentPostRequest
import edu.olexandergalaktionov.physiocare.model.AppointmentsResponse
import edu.olexandergalaktionov.physiocare.model.LoginRequest
import edu.olexandergalaktionov.physiocare.model.LoginResponse
import edu.olexandergalaktionov.physiocare.model.Physio
import edu.olexandergalaktionov.physiocare.model.PhysiosResponse
import edu.olexandergalaktionov.physiocare.model.RecordResponse
import edu.olexandergalaktionov.physiocare.model.RecordsResponse
import edu.olexandergalaktionov.physiocare.model.Record
import retrofit2.HttpException

class RemoteDataSource {
    companion object {
        val TAG = RemoteDataSource::class.java.simpleName

        private val api = Retrofit2Api.getRetrofit2Api()

        suspend fun login(request: LoginRequest): LoginResponse {
            val response = api.login(request)
            if (response.isSuccessful) {
                return response.body() ?: throw Exception("Respuesta vacía del servidor")
            } else {
                val errorBody = response.errorBody()?.string() // Error detallado
                Log.e(TAG, "Error: ${response.message()} | $errorBody")
                throw Exception("Error en login: ${response.message()}")
            }
        }

        suspend fun getAllRecords(token: String): RecordsResponse {
            val response = api.getAllRecords("Bearer $token")
            if (response.isSuccessful) {
                Log.d(TAG, "Respuesta exitosa: ${response.body()}")
                return response.body() ?: throw Exception("Respuesta vacía del servidor")
            } else {
                Log.e(TAG, "Error: ${response.message()} | ${response.errorBody()?.string()}")
                throw Exception("Error al obtener records: ${response.message()}")
            }
        }

        //  Obtener expediente del paciente autenticado
        suspend fun getRecordByPatientId(token: String, patientId: String): RecordResponse {
            val response = api.getRecordByPatientId("Bearer $token", patientId)
            if (response.isSuccessful) {
                return response.body() ?: throw Exception("Respuesta vacía del servidor")
            } else if (response.code() == 404) {
                throw HttpException(response) // Para detectar el error 404 concretamente
            } else {
                Log.e(TAG, "Error: ${response.message()} | ${response.errorBody()?.string()}")
                throw Exception("Error al obtener expediente: ${response.message()}")
            }
        }

        //  Obtener citas separadas (futuras y pasadas) por paciente
        suspend fun getAppointmentsByPatientId(token: String, patientId: String): AppointmentsResponse {
            val response = api.getAppointmentsByPatientId("Bearer $token", patientId)
            if (response.isSuccessful) {
                return response.body() ?: throw Exception("Respuesta vacía del servidor")
            } else {
                throw Exception("Error al obtener citas del paciente: ${response.message()}")
            }
        }

        suspend fun getAppointmentById(token: String, appointmentId: String): Appointment {
            val response = api.getAppointmentById("Bearer $token", appointmentId )
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.ok == true && body.resultado != null) {
                    return body.resultado
                } else {
                    throw Exception("La cita no se encontró o está vacía")
                }
            } else {
                Log.e(TAG, "Error: ${response.message()} | ${response.errorBody()?.string()}")
                throw Exception("Error al obtener cita por ID: ${response.message()}")
            }
        }


        suspend fun getAppointmentsByPhysioId(token: String, physioId: String): List<Appointment> {
            val response = api.getAppointmentsByPhysio("Bearer $token", physioId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.ok && body.resultado != null) {
                    return body.resultado // resultado debe ser List<Appointment>
                } else {
                    throw Exception("No se encontraron citas para este fisio")
                }
            } else {
                throw Exception("Error al obtener citas: ${response.message()}")
            }
        }


        suspend fun deleteAppointmentById(token: String, appointmentId: String) {
            val response = api.deleteAppointment("Bearer $token", appointmentId)
            if (!response.isSuccessful) {
                throw Exception("Error al eliminar la cita: ${response.message()}")
            }
        }


        suspend fun getAllPhysios(token: String): List<Physio> {
            val response = api.getAllPhysios("Bearer $token")
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.ok == true && body.resultado != null) {
                    return body.resultado
                } else {
                    throw Exception("No se encontraron fisioterapeutas")
                }
            } else {
                Log.e(TAG, "Error: ${response.message()} | ${response.errorBody()?.string()}")
                Log.e(TAG, "Error: ${response.errorBody()?.string()}")
                throw Exception("Error al obtener fisioterapeutas: ${response.message()}")
            }
        }

        suspend fun postAppointmentToRecord(
            token: String,
            recordId: String,
            request: AppointmentPostRequest
        ): Boolean {
            val response = api.postAppointmentToRecord("Bearer $token", recordId, request)
            if (!response.isSuccessful) {
                Log.e(TAG, "Error al guardar cita: ${response.code()} | ${response.errorBody()?.string()}")
            }
            return response.isSuccessful
        }


    }
}