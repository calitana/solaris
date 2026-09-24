package com.solartech.mantenimiento.datos.modelo


data class SitioSolar(
    var id: String = "",
    var nombreLugar: String = "",
    var cantidadPaneles: Int = 0,
    var potenciaKw: Double = 0.0,
    var idCliente: String = "",
    var nombreCliente: String = "",
    var ubicacion: String = "",
    var modelo: String = "Jinko Solar Tiger Neo",
    var fechaInstalacion: Long = System.currentTimeMillis()
)
