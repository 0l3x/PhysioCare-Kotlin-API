package edu.olexandergalaktionov.physiocare.model

import com.google.gson.annotations.SerializedName

data class Record(
    @SerializedName("_id") val id: String?,
    @SerializedName("patient") val patient: String?,
    @SerializedName("medicalRecord") val medicalRecord: String?,
    @SerializedName("appointments") val appointments: List<Appointment>?
)

// Respuesta que contiene un solo record
data class RecordResponse(
    @SerializedName("ok") val ok: Boolean?,
    @SerializedName("resultado") val resultado: Record?
)

// Respuesta que contiene una lista de records
data class RecordsResponse(
    @SerializedName("ok") val ok: Boolean?,
    @SerializedName("resultado") val resultado: List<Record>?
)



