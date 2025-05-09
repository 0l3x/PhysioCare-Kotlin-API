package edu.olexandergalaktionov.physiocare.model

import com.google.gson.annotations.SerializedName

/**
 * Fisioterapeuta
 * @param _id ID del fisioterapeuta
 * @param name Nombre del fisioterapeuta
 * @param surname Apellido del fisioterapeuta
 * @param specialty Especialidad del fisioterapeuta
 * @param licenseNumber Número de licencia del fisioterapeuta
 * @param userID ID del usuario asociado al fisioterapeuta
 */
data class Physio (
    @SerializedName("_id")
    val _id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("surname")
    val surname: String,
    @SerializedName("specialty")
    val specialty: String,
    @SerializedName("licenseNumber")
    val licenseNumber: String,
    @SerializedName("userID")
    val userID: String? = null
) {
    val fullName: String
        get() = "$name $surname"
}

/**
 * Respuesta que contiene una lista de fisioterapeutas
 * @param ok Indica si la respuesta es correcta
 * @param resultado Lista de fisioterapeutas
 */
data class PhysiosResponse(
    @SerializedName("ok") val ok: Boolean?,
    @SerializedName("resultado") val resultado: List<Physio>?
)