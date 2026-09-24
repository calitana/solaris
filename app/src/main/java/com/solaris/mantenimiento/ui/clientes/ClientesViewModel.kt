package com.solartech.mantenimiento.ui.clientes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solartech.mantenimiento.datos.modelo.Cliente
import com.solartech.mantenimiento.datos.repositorio.RepositorioClientes
import com.solartech.mantenimiento.utilidades.Resultado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class ClientesViewModel(
    private val repositorioClientes: RepositorioClientes = RepositorioClientes()
) : ViewModel() {

    private val _listaClientes = MutableStateFlow<Resultado<List<Cliente>>>(Resultado.Cargando)
    val listaClientes: StateFlow<Resultado<List<Cliente>>> = _listaClientes.asStateFlow()

    private val _estadoOperacion = MutableStateFlow<Resultado<Any>>(Resultado.Inactivo)
    val estadoOperacion: StateFlow<Resultado<Any>> = _estadoOperacion.asStateFlow()

    init {
        cargarClientes()
    }

    /**
     * Carga y escucha en tiempo real la lista de clientes.
     */
    fun cargarClientes() {
        viewModelScope.launch {
            repositorioClientes.obtenerClientes().collect { resultado ->
                _listaClientes.value = resultado
            }
        }
    }

    /**
     * Agrega un nuevo cliente a Firestore.
     */
    fun agregarCliente(nombre: String, telefono: String, direccion: String, correo: String = "") {
        if (nombre.isBlank() || telefono.isBlank() || direccion.isBlank()) {
            _estadoOperacion.value = Resultado.Error("Nombre, teléfono y dirección son obligatorios.")
            return
        }

        val nuevoCliente = Cliente(
            nombre = nombre.trim(),
            telefono = telefono.trim(),
            direccion = direccion.trim(),
            correo = correo.trim()
        )

        viewModelScope.launch {
            repositorioClientes.agregarCliente(nuevoCliente).collect { resultado ->
                _estadoOperacion.value = resultado
            }
        }
    }

    /**
     * Actualiza un cliente existente.
     */
    fun actualizarCliente(cliente: Cliente) {
        if (cliente.nombre.isBlank() || cliente.telefono.isBlank() || cliente.direccion.isBlank()) {
            _estadoOperacion.value = Resultado.Error("Nombre, teléfono y dirección son obligatorios.")
            return
        }

        viewModelScope.launch {
            repositorioClientes.actualizarCliente(cliente).collect { resultado ->
                _estadoOperacion.value = resultado
            }
        }
    }

    /**
     * Elimina un cliente.
     */
    fun eliminarCliente(idCliente: String) {
        viewModelScope.launch {
            repositorioClientes.eliminarCliente(idCliente).collect { }
        }
    }

    fun reiniciarEstadoOperacion() {
        _estadoOperacion.value = Resultado.Inactivo
    }
}
