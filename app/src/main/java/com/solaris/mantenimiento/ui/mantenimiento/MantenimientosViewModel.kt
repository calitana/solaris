package com.solartech.mantenimiento.ui.mantenimiento

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solartech.mantenimiento.datos.modelo.Cliente
import com.solartech.mantenimiento.datos.modelo.OrdenMantenimiento
import com.solartech.mantenimiento.datos.modelo.SitioSolar
import com.solartech.mantenimiento.datos.repositorio.RepositorioClientes
import com.solartech.mantenimiento.datos.repositorio.RepositorioMantenimientos
import com.solartech.mantenimiento.datos.repositorio.RepositorioSitios
import com.solartech.mantenimiento.utilidades.Resultado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para la gestión de Órdenes de Mantenimiento e integración de Analytics.
 */
class MantenimientosViewModel(
    private val repositorioMantenimientos: RepositorioMantenimientos = RepositorioMantenimientos(),
    private val repositorioSitios: RepositorioSitios = RepositorioSitios(),
    private val repositorioClientes: RepositorioClientes = RepositorioClientes()
) : ViewModel() {

    private val _listaMantenimientos = MutableStateFlow<Resultado<List<OrdenMantenimiento>>>(Resultado.Cargando)
    val listaMantenimientos: StateFlow<Resultado<List<OrdenMantenimiento>>> = _listaMantenimientos.asStateFlow()

    private val _listaSitios = MutableStateFlow<List<SitioSolar>>(emptyList())
    val listaSitios: StateFlow<List<SitioSolar>> = _listaSitios.asStateFlow()

    private val _filtroEstado = MutableStateFlow("Todos")
    val filtroEstado: StateFlow<String> = _filtroEstado.asStateFlow()

    private val _estadoOperacion = MutableStateFlow<Resultado<Any>>(Resultado.Inactivo)
    val estadoOperacion: StateFlow<Resultado<Any>> = _estadoOperacion.asStateFlow()

    init {
        cargarMantenimientos()
        cargarSitiosDisponibles()
    }

    fun cargarMantenimientos() {
        viewModelScope.launch {
            repositorioMantenimientos.obtenerMantenimientos().collect { resultado ->
                _listaMantenimientos.value = resultado
            }
        }
    }

    private fun cargarSitiosDisponibles() {
        viewModelScope.launch {
            repositorioSitios.obtenerSitios().collect { resultado ->
                if (resultado is Resultado.Exito) {
                    _listaSitios.value = resultado.datos
                }
            }
        }
    }

    fun establecerFiltro(filtro: String) {
        _filtroEstado.value = filtro
    }

    /**
     * Guarda un nuevo servicio de mantenimiento y emite el evento de Analytics.
     */
    fun crearMantenimiento(
        tipo: String,
        estado: String,
        observaciones: String,
        sitio: SitioSolar?,
        nombreTecnico: String,
        idTecnico: String
    ) {
        if (observaciones.isBlank()) {
            _estadoOperacion.value = Resultado.Error("Las observaciones del servicio técnico son obligatorias.")
            return
        }

        val nuevaOrden = OrdenMantenimiento(
            tipo = tipo,
            estado = estado,
            observaciones = observaciones.trim(),
            idSitio = sitio?.id ?: "",
            nombreSitio = sitio?.nombreLugar ?: "Sitio General",
            idCliente = sitio?.idCliente ?: "",
            nombreCliente = sitio?.nombreCliente ?: "Cliente General",
            idTecnico = idTecnico,
            nombreTecnico = nombreTecnico.ifBlank { "Técnico en Turno" },
            potenciaKw = sitio?.potenciaKw ?: 0.0,
            fechaServicio = System.currentTimeMillis()
        )

        viewModelScope.launch {
            repositorioMantenimientos.agregarMantenimiento(nuevaOrden).collect { resultado ->
                _estadoOperacion.value = resultado
                if (resultado is Resultado.Exito) {
                    // Refresh the list to reflect the newly added maintenance order
                    cargarMantenimientos()
                    // Reset any active filter so the new item is visible
                    _filtroEstado.value = "Todos"
                }
            }
        }
    }

    fun actualizarMantenimiento(orden: OrdenMantenimiento) {
        if (orden.observaciones.isBlank()) {
            _estadoOperacion.value = Resultado.Error("Las observaciones del servicio técnico son obligatorias.")
            return
        }

        viewModelScope.launch {
            repositorioMantenimientos.actualizarMantenimiento(orden).collect { resultado ->
                _estadoOperacion.value = resultado
            }
        }
    }

    /**
     * Actualiza el estado de una orden (p.ej. de Pendiente a Completado).
     */
    fun cambiarEstado(idOrden: String, nuevoEstado: String) {
        viewModelScope.launch {
            val orden = (_listaMantenimientos.value as? Resultado.Exito)?.datos?.find { it.id == idOrden }
            if (orden != null) {
                repositorioMantenimientos.actualizarMantenimiento(orden.copy(estado = nuevoEstado)).collect { }
            }
        }
    }

    fun eliminarMantenimiento(idOrden: String) {
        viewModelScope.launch {
            repositorioMantenimientos.eliminarMantenimiento(idOrden).collect { }
        }
    }

    /**
     * Reset the operation state to Inactivo. Useful after showing a success/error toast.
     */
    fun reiniciarEstadoOperacion() {
        _estadoOperacion.value = Resultado.Inactivo
    }
}
