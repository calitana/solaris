package com.solartech.mantenimiento.datos.repositorio

import com.solartech.mantenimiento.datos.modelo.Cliente
import com.solartech.mantenimiento.utilidades.Resultado
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.util.UUID


class RepositorioClientes {
    companion object {
        private val clientesEnMemoria = MutableStateFlow<List<Cliente>>(emptyList())
    }

    /**
     * Escucha en tiempo real la lista de clientes.
     */
    fun obtenerClientes(): Flow<Resultado<List<Cliente>>> {
        return clientesEnMemoria.map { lista ->
            Resultado.Exito(lista.sortedByDescending { it.fechaCreacion })
        }
    }

    /**
     * Agrega un nuevo cliente a la memoria RAM.
     */
    fun agregarCliente(cliente: Cliente): Flow<Resultado<String>> = flow {
        emit(Resultado.Cargando)
        delay(300)
        try {
            val nuevoId = UUID.randomUUID().toString()
            val nuevoCliente = cliente.copy(id = nuevoId, fechaCreacion = System.currentTimeMillis())
            val listaActual = clientesEnMemoria.value.toMutableList()
            listaActual.add(nuevoCliente)
            clientesEnMemoria.value = listaActual
            emit(Resultado.Exito(nuevoId))
        } catch (e: Exception) {
            emit(Resultado.Error(e.localizedMessage ?: "Error al agregar cliente."))
        }
    }

    /**
     * Actualiza un cliente existente en la memoria RAM.
     */
    fun actualizarCliente(cliente: Cliente): Flow<Resultado<Unit>> = flow {
        emit(Resultado.Cargando)
        delay(300)
        try {
            val listaActual = clientesEnMemoria.value.toMutableList()
            val index = listaActual.indexOfFirst { it.id == cliente.id }
            if (index != -1) {
                listaActual[index] = cliente
                clientesEnMemoria.value = listaActual
                emit(Resultado.Exito(Unit))
            } else {
                emit(Resultado.Error("Cliente no encontrado."))
            }
        } catch (e: Exception) {
            emit(Resultado.Error(e.localizedMessage ?: "Error al actualizar cliente."))
        }
    }

    /**
     * Elimina un cliente por su ID en la memoria RAM.
     */
    fun eliminarCliente(idCliente: String): Flow<Resultado<Unit>> = flow {
        emit(Resultado.Cargando)
        delay(300)
        try {
            val listaActual = clientesEnMemoria.value.toMutableList()
            val removido = listaActual.removeAll { it.id == idCliente }
            if (removido) {
                clientesEnMemoria.value = listaActual
                emit(Resultado.Exito(Unit))
            } else {
                emit(Resultado.Error("Cliente no encontrado."))
            }
        } catch (e: Exception) {
            emit(Resultado.Error(e.localizedMessage ?: "Error al eliminar cliente."))
        }
    }
}
