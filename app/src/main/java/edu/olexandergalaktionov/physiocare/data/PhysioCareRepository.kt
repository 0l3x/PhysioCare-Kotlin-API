package edu.olexandergalaktionov.physiocare.data

import android.util.Log
import edu.olexandergalaktionov.physiocare.model.LoginRequest
import edu.olexandergalaktionov.physiocare.model.LoginResponse
import edu.olexandergalaktionov.physiocare.model.RecordsResponse
import edu.olexandergalaktionov.physiocare.utils.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class PhysioCareRepository(private val sessionManager: SessionManager) {
    val TAG = PhysioCareRepository::class.java.simpleName
    private val remoteDataSource = RemoteDataSource

    suspend fun login(request: LoginRequest): LoginResponse {
        val response = remoteDataSource.login(request)

        if (response.token != null) {
            sessionManager.saveSession(response.token, request.username)
        } else {
            Log.e(TAG, "Token nulo recibido: ${response.error}")
        }

        return response
    }

    suspend fun logout() {
        sessionManager.clearSession()
    }

    private fun getSessionFlow(): Flow<Pair<String?, String?>> = sessionManager.sessionFlow

    suspend fun getAllRecords(): RecordsResponse {
        val (token, _) = getSessionFlow().first()
        return remoteDataSource.getAllRecords(token.toString())
    }

}