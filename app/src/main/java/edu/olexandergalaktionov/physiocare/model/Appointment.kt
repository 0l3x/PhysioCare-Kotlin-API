package edu.olexandergalaktionov.physiocare.model

import com.google.gson.annotations.SerializedName

data class Appointment(
    @SerializedName("_id") val _id: String?,
    @SerializedName("date") val date: String?,
    @SerializedName("diagnosis") val diagnosis: String?,
    @SerializedName("observations") val observations: String?,
    @SerializedName("physio") val physio: String?,
    @SerializedName("treatment") val treatment: String?
)

data class AppointmentsResponse(
    val ok: Boolean,
    @SerializedName("futuras")
    val futuras: List<Appointment>,
    @SerializedName("pasadas")
    val pasadas: List<Appointment>
)

data class AppointmentResponse(
    val ok: Boolean?,
    val resultado: Appointment?
)
