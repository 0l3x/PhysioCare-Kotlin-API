package edu.olexandergalaktionov.physiocare.utils


import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * DataStore recomienda crear las propiedades de nivel superior.
 * Inicialización con extensión delegada.
 */
val Context.dataStore by preferencesDataStore(name = "settings")

class SessionManager(private val dataStore: DataStore<Preferences>) {
    companion object {
        private val TOKEN_KEY = stringPreferencesKey("token")
        private val ID_KEY = stringPreferencesKey("usuarioId")
        private val ROL_KEY = stringPreferencesKey("rol")
    }

    // Flujo de datos para la sesión. Devuelve un par con el token y el nombre de usuario.
    val sessionFlow: Flow<Pair<String?, String?>> = dataStore.data.map { preferences ->
        preferences[TOKEN_KEY] to preferences[ID_KEY]
    }

    val userIdFlow: Flow<String?> = dataStore.data.map { it[ID_KEY] } // Flujo para el ID de usuario
    val roleFlow: Flow<String?> = dataStore.data.map { it[ROL_KEY] } // Flujo para el rol

    // Función para guardar los datos de la sesión.
    suspend fun saveSession(token: String, usuarioId: String, rol: String) {
        dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
            preferences[ID_KEY] = usuarioId
            preferences[ROL_KEY] = rol
        }
    }

    // Función para limpiar la sesión. Borra los datos del fichero settings.
    suspend fun clearSession() {
        dataStore.edit { it.clear() }
    }
}