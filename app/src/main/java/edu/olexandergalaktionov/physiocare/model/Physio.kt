package edu.olexandergalaktionov.physiocare.model

import com.google.gson.annotations.SerializedName

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

data class PhysiosResponse(
    @SerializedName("ok") val ok: Boolean?,
    @SerializedName("resultado") val resultado: List<Physio>?
)