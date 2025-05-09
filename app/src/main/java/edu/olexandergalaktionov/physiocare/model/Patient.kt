package edu.olexandergalaktionov.physiocare.model

import com.google.gson.annotations.SerializedName

/**
 * Paciente
 * @param id ID del paciente
 * @param name Nombre del paciente
 * @param surname Apellido del paciente
 * @param birthDate Fecha de nacimiento del paciente
 * @param address Dirección del paciente
 * @param insuranceNumber Número de seguro del paciente
 * @param userID ID del usuario asociado al paciente
 */
data class Patient(
    @SerializedName("_id") val id: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("surname") val surname: String?,
    @SerializedName("birthDate") val birthDate: String?,
    @SerializedName("address") val address: String?,
    @SerializedName("insuranceNumber") val insuranceNumber: String?,
    @SerializedName("userID") val userID: String?
)