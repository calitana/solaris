package com.solartech.mantenimiento.datos.repositorio

import com.solartech.mantenimiento.datos.modelo.OrdenMantenimiento
import com.solartech.mantenimiento.utilidades.Resultado
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.util.UUID


class RepositorioMantenimientos {
    companion object {
        private val mantenimientosEnMemoria = MutableStateFlow<List<OrdenMantenimiento>>(emptyList())
    }

    /**
     * Escucha en tiempo real la lista de mantenimientos.
     */
    fun obtenerMantenimientos(): Flow<Resultado<List<OrdenMantenimiento>>> {
        return mantenimientosEnMemoria.map { lista ->
            Resultado.Exito(lista.sortedByDescending { it.fechaServicio })
        }
    }

    /**
     * Agrega un nuevo mantenimiento a la memoria RAM.
     */
    fun agregarMantenimiento(orden: OrdenMantenimiento): Flow<Resultado<String>> = flow {
        emit(Resultado.Cargando)
        delay(300)
        try {
            val nuevoId = UUID.randomUUID().toString()
            val nuevaOrden = orden.copy(id = nuevoId, fechaServicio = System.currentTimeMillis())
            val listaActual = mantenimientosEnMemoria.value.toMutableList()
            listaActual.add(nuevaOrden)
            mantenimientosEnMemoria.value = listaActual
            emit(Resultado.Exito(nuevoId))
        } catch (e: Exception) {
            emit(Resultado.Error(e.localizedMessage ?: "Error al agregar mantenimiento."))
        }
    }

    /**
     * Actualiza un mantenimiento existente en la memoria RAM.
     */
    fun actualizarMantenimiento(orden: OrdenMantenimiento): Flow<Resultado<Unit>> = flow {
        emit(Resultado.Cargando)
        delay(300)
        try {
            val listaActual = mantenimientosEnMemoria.value.toMutableList()
            val index = listaActual.indexOfFirst { it.id == orden.id }
            if (index != -1) {
                listaActual[index] = orden
                mantenimientosEnMemoria.value = listaActual
                emit(Resultado.Exito(Unit))
            } else {
                emit(Resultado.Error("Mantenimiento no encontrado."))
            }
        } catch (e: Exception) {
            emit(Resultado.Error(e.localizedMessage ?: "Error al actualizar mantenimiento."))
        }
    }

    /**
     * Elimina un mantenimiento por su ID en la memoria RAM.
     */
    fun eliminarMantenimiento(idMantenimiento: String): Flow<Resultado<Unit>> = flow {
        emit(Resultado.Cargando)
        delay(300)
        try {
            val listaActual = mantenimientosEnMemoria.value.toMutableList()
            val removido = listaActual.removeAll { it.id == idMantenimiento }
            if (removido) {
                mantenimientosEnMemoria.value = listaActual
                emit(Resultado.Exito(Unit))
            } else {
                emit(Resultado.Error("Mantenimiento no encontrado."))
            }
        } catch (e: Exception) {
            emit(Resultado.Error(e.localizedMessage ?: "Error al eliminar mantenimiento."))
        }
    }
}
