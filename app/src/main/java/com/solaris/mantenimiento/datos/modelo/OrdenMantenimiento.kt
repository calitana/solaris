package com.solartech.mantenimiento.datos.modelo


data class OrdenMantenimiento(
    var id: String = "",
    var tipo: String = "Preventivo",
    var estado: String = "Pendiente",
    var observaciones: String = "",
    var idSitio: String = "",
    var nombreSitio: String = "",
    var idCliente: String = "",
    var nombreCliente: String = "",
    var idTecnico: String = "",
    var nombreTecnico: String = "",
    var potenciaKw: Double = 0.0,
    var fechaServicio: Long = System.currentTimeMillis()
)
