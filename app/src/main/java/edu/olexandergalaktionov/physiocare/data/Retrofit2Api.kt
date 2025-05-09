package edu.olexandergalaktionov.physiocare.data

import edu.olexandergalaktionov.physiocare.model.AppointmentPostRequest
import edu.olexandergalaktionov.physiocare.model.AppointmentResponse
import edu.olexandergalaktionov.physiocare.model.AppointmentsFlatResponse
import edu.olexandergalaktionov.physiocare.model.AppointmentsResponse
import edu.olexandergalaktionov.physiocare.model.LoginRequest
import edu.olexandergalaktionov.physiocare.model.LoginResponse
import edu.olexandergalaktionov.physiocare.model.Physio
import edu.olexandergalaktionov.physiocare.model.PhysiosResponse
import edu.olexandergalaktionov.physiocare.model.RecordResponse
import edu.olexandergalaktionov.physiocare.model.RecordsResponse
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

class Retrofit2Api {
    companion object {
        const val BASE_URL = "http://olexanderg.net:8080/"

        fun getRetrofit2Api(): Retrofit2ApiInterface {
            return Retrofit.Builder().baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create()).build()
                .create(Retrofit2ApiInterface::class.java)
        }
    }
}

interface Retrofit2ApiInterface {
    @POST("auth/login")
    @Headers("Content-Type: application/json")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("records")
    @Headers("Content-Type: application/json")
    suspend fun getAllRecords(@Header("Authorization") token: String): Response<RecordsResponse>

    // Obtener expediente del paciente autenticado
    @GET("records/patient/{id}")
    suspend fun getRecordByPatientId(
        @Header("Authorization") token: String,
        @Path("id") patientId: String
    ): Response<RecordResponse>

    // Obtener citas separadas (futuras y pasadas) por paciente
    @GET("records/appointments/patients/{id}")
    suspend fun getAppointmentsByPatientId(
        @Header("Authorization") token: String,
        @Path("id") patientId: String
    ): Response<AppointmentsResponse> // Define una clase que tenga `futuras` y `pasadas`

    // Obtiene una cita por ID
    @GET("records/appointments/{id}")
    suspend fun getAppointmentById(
        @Header("Authorization") token: String,
        @Path("id") id: String,
    ): Response<AppointmentResponse>

    // Obtener citas por ID de fisioterapeuta (todas)
    @GET("records/appointments/physio/{id}")
    suspend fun getAppointmentsByPhysio(
        @Header("Authorization") token: String,
        @Path("id") physioId: String
    ): Response<AppointmentsFlatResponse>

    // Delete de cita por ID
    @DELETE("records/appointments/{id}")
    suspend fun deleteAppointment(
        @Header("Authorization") token: String,
        @Path("id") appointmentId: String
    ): Response<Unit>

    @GET("physios")
    @Headers("Content-Type: application/json")
    suspend fun getAllPhysios(@Header("Authorization") token: String): Response<PhysiosResponse>

    @POST("records/{id}/appointments")
    suspend fun postAppointmentToRecord(
        @Header("Authorization") token: String,
        @Path("id") recordId: String,
        @Body appointment: AppointmentPostRequest
    ): Response<Unit>


}