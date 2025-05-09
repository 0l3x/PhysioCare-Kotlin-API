package edu.olexandergalaktionov.physiocare.model

import com.google.gson.annotations.SerializedName
import java.util.Date

data class Appointment(
    @SerializedName("_id") val _id: String?,
    @SerializedName("date") val date: String?,
    @SerializedName("diagnosis") val diagnosis: String?,
    @SerializedName("observations") val observations: String?,
    @SerializedName("physio") val physio: String?,
    @SerializedName("physioName") val physioName: String?,
    @SerializedName("physioSurname") val physioSurname: String?,
    @SerializedName("treatment") val treatment: String?
)

data class AppointmentsResponse(
    val ok: Boolean,
    @SerializedName("futuras")
    val futuras: List<Appointment>,
    @SerializedName("pasadas")
    val pasadas: List<Appointment>
)

// Respuesta de una sola cita, detalle
data class AppointmentResponse(
    val ok: Boolean?,
    val resultado: Appointment?
)

data class AppointmentsFlatResponse(
    val ok: Boolean,
    val resultado: List<Appointment>
)

data class AppointmentPostRequest(
    val physio: String,
    val diagnosis: String,
    val treatment: String,
    val observations: String,
    val date: String
)


