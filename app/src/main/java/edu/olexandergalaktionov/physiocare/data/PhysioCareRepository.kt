package edu.olexandergalaktionov.physiocare.data

import edu.olexandergalaktionov.physiocare.model.LoginRequest
import edu.olexandergalaktionov.physiocare.model.LoginResponse
import edu.olexandergalaktionov.physiocare.utils.SessionManager

class PhysioCareRepository(private val sessionManager: SessionManager) {
    val TAG = PhysioCareRepository::class.java.simpleName
    private val remoteDataSource = RemoteDataSource

    suspend fun login(request: LoginRequest): LoginResponse {
        val response = remoteDataSource.login(request)
        sessionManager.saveSession(response.token!!, request.username) // Save the session
        return response
    }
}