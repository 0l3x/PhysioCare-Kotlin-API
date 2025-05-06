package edu.olexandergalaktionov.physiocare.data

import android.util.Log
import edu.olexandergalaktionov.physiocare.model.LoginRequest
import edu.olexandergalaktionov.physiocare.model.LoginResponse
import edu.olexandergalaktionov.physiocare.model.RecordsResponse

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
                return response.body() ?: throw Exception("Respuesta vacía del servidor")
            } else {
                throw Exception("Error al obtener records: ${response.message()}")
            }
        }

    }
}