package com.solartech.mantenimiento.ui.sitios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solartech.mantenimiento.datos.modelo.Cliente
import com.solartech.mantenimiento.datos.modelo.SitioSolar
import com.solartech.mantenimiento.datos.repositorio.RepositorioClientes
import com.solartech.mantenimiento.datos.repositorio.RepositorioSitios
import com.solartech.mantenimiento.utilidades.Resultado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para gestionar el listado y alta de Instalaciones / Sitios Solares.
 */
class SitiosViewModel(
    private val repositorioSitios: RepositorioSitios = RepositorioSitios(),
    private val repositorioClientes: RepositorioClientes = RepositorioClientes()
) : ViewModel() {

    private val _listaSitios = MutableStateFlow<Resultado<List<SitioSolar>>>(Resultado.Cargando)
    val listaSitios: StateFlow<Resultado<List<SitioSolar>>> = _listaSitios.asStateFlow()

    private val _listaClientes = MutableStateFlow<List<Cliente>>(emptyList())
    val listaClientes: StateFlow<List<Cliente>> = _listaClientes.asStateFlow()

    private val _estadoOperacion = MutableStateFlow<Resultado<Any>>(Resultado.Inactivo)
    val estadoOperacion: StateFlow<Resultado<Any>> = _estadoOperacion.asStateFlow()

    init {
        cargarSitios()
        cargarClientesDisponibles()
    }

    fun cargarSitios() {
        viewModelScope.launch {
            repositorioSitios.obtenerSitios().collect { resultado ->
                _listaSitios.value = resultado
            }
        }
    }

    private fun cargarClientesDisponibles() {
        viewModelScope.launch {
            repositorioClientes.obtenerClientes().collect { resultado ->
                if (resultado is Resultado.Exito) {
                    _listaClientes.value = resultado.datos
                }
            }
        }
    }

    /**
     * Agrega un nuevo sitio solar con cantidad de paneles y potencia en kW.
     */
    fun agregarSitio(
        nombreLugar: String,
        cantidadPaneles: Int,
        potenciaKw: Double,
        idCliente: String = "",
        nombreCliente: String = "",
        ubicacion: String = "",
        modelo: String = "Jinko Solar Tiger Neo"
    ) {
        if (nombreLugar.isBlank() || cantidadPaneles <= 0 || potenciaKw <= 0.0) {
            _estadoOperacion.value = Resultado.Error("Nombre del lugar, cantidad de paneles y potencia en kW son obligatorios.")
            return
        }

        val nuevoSitio = SitioSolar(
            nombreLugar = nombreLugar.trim(),
            cantidadPaneles = cantidadPaneles,
            potenciaKw = potenciaKw,
            idCliente = idCliente,
            nombreCliente = nombreCliente.ifBlank { "General" },
            ubicacion = ubicacion.trim(),
            modelo = modelo
        )

        viewModelScope.launch {
            repositorioSitios.agregarSitio(nuevoSitio).collect { resultado ->
                _estadoOperacion.value = resultado
            }
        }
    }

    fun actualizarSitio(sitio: SitioSolar) {
        if (sitio.nombreLugar.isBlank() || sitio.cantidadPaneles <= 0 || sitio.potenciaKw <= 0.0) {
            _estadoOperacion.value = Resultado.Error("Nombre del lugar, cantidad de paneles y potencia en kW son obligatorios.")
            return
        }

        viewModelScope.launch {
            repositorioSitios.actualizarSitio(sitio).collect { resultado ->
                _estadoOperacion.value = resultado
            }
        }
    }

    fun eliminarSitio(idSitio: String) {
        viewModelScope.launch {
            repositorioSitios.eliminarSitio(idSitio).collect { }
        }
    }

    fun reiniciarEstadoOperacion() {
        _estadoOperacion.value = Resultado.Inactivo
    }
}
