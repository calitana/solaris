package com.solartech.mantenimiento.datos.modelo


data class Usuario(
    var id: String = "",
    var nombre: String = "",
    var correo: String = "",
    var rol: String = "Técnico Solar",
    var fechaRegistro: Long = System.currentTimeMillis()
)
