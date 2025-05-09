package edu.olexandergalaktionov.physiocare.model

import com.google.gson.annotations.SerializedName

/**
 * Expediente
 * @param id ID del registro
 * @param patient Paciente asociado al registro
 * @param medicalRecord Historia clínica del paciente
 * @param appointments Lista de citas asociadas al registro
 */
data class Record(
    @SerializedName("_id") val id: String?,
    @SerializedName("patient") val patient: Patient,
    @SerializedName("medicalRecord") val medicalRecord: String?,
    @SerializedName("appointments") val appointments: List<Appointment>?
)

/**
 * Respuesta que contiene un solo registro
 * @param ok Indica si la respuesta es correcta
 * @param resultado Registro asociado al paciente
 */
data class RecordResponse(
    @SerializedName("ok") val ok: Boolean?,
    @SerializedName("resultado") val resultado: Record?
)

/**
 * Respuesta que contiene una lista de registros
 * @param ok Indica si la respuesta es correcta
 * @param resultado Lista de registros asociados al paciente
 */
data class RecordsResponse(
    @SerializedName("ok") val ok: Boolean?,
    @SerializedName("resultado") val resultado: List<Record>?
)



