package edu.olexandergalaktionov.physiocare.model

import com.google.gson.annotations.SerializedName

/**
 * Cita médica
 * @param _id ID de la cita
 * @param date Fecha de la cita
 * @param diagnosis Diagnóstico de la cita
 * @param observations Observaciones de la cita
 * @param physio ID del fisioterapeuta que lleva la cita
 * @param physioName Nombre del fisioterapeuta que lleva la cita
 * @param physioSurname Apellido del fisioterapeuta que lleva la cita
 * @param treatment Tratamiento de la cita
 */
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

/**
 * Respuesta que contiene una lista de citas, futuras y pasadas
 */
data class AppointmentsResponse(
    val ok: Boolean,
    @SerializedName("futuras")
    val futuras: List<Appointment>,
    @SerializedName("pasadas")
    val pasadas: List<Appointment>
)

/**
 * Respuesta que contiene una sola cita (para el detalle)
 */
data class AppointmentResponse(
    val ok: Boolean?,
    val resultado: Appointment?
)

/**
 * Respuesta que contiene una lista de citas entera (atemporal)
 */
data class AppointmentsFlatResponse(
    val ok: Boolean,
    val resultado: List<Appointment>
)

/**
 * Request para crear una nueva cita
 */
data class AppointmentPostRequest(
    val physio: String,
    val diagnosis: String,
    val treatment: String,
    val observations: String,
    val date: String
)


