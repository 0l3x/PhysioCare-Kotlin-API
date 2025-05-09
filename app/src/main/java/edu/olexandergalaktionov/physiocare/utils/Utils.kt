package edu.olexandergalaktionov.physiocare.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/**
 * Función checkConnection.kt
 *
 * Comprueba si hay una conexión a internet activa disponible en el dispositivo.
 *
 * @autor Olexandr Galaktionov Tsisar
 *
 * @param context El contexto de la aplicación necesario para acceder a los servicios del sistema.
 * @return True si el dispositivo está conectado a internet, false en caso contrario.
 */
fun checkConnection(context: Context) : Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkInfo = cm.activeNetwork

    if (networkInfo != null) {
        val activeNetwork = cm.getNetworkCapabilities(networkInfo)
        if (activeNetwork != null) {
            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        }
    }

    return false
}

/**
 * Variable isPhysio.kt
 * Indica si el usuario es un fisioterapeuta o no.
 */
var isPhysio = false