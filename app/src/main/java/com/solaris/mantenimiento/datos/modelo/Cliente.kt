package com.solartech.mantenimiento.datos.modelo


data class Cliente(
    var id: String = "",
    var nombre: String = "",
    var telefono: String = "",
    var direccion: String = "",
    var correo: String = "",
    var fechaCreacion: Long = System.currentTimeMillis()
)
