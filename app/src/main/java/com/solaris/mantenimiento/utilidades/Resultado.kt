package com.solartech.mantenimiento.utilidades

/**
 * Clase sellada para representar estados asíncronos (Éxito, Error, Cargando, Inactivo).
 */
sealed class Resultado<out T> {
    data object Inactivo : Resultado<Nothing>()
    data object Cargando : Resultado<Nothing>()
    data class Exito<out T>(val datos: T) : Resultado<T>()
    data class Error(val mensaje: String, val excepcion: Throwable? = null) : Resultado<Nothing>()
}
