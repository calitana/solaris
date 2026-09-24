package com.solartech.mantenimiento.datos.repositorio

import com.solartech.mantenimiento.datos.modelo.SitioSolar
import com.solartech.mantenimiento.utilidades.Resultado
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.util.UUID


class RepositorioSitios {
    companion object {
        private val sitiosEnMemoria = MutableStateFlow<List<SitioSolar>>(emptyList())
    }

    /**
     * Escucha en tiempo real la lista de sitios solares.
     */
    fun obtenerSitios(): Flow<Resultado<List<SitioSolar>>> {
        return sitiosEnMemoria.map { lista ->
            Resultado.Exito(lista.sortedByDescending { it.fechaInstalacion })
        }
    }

    /**
     * Agrega un nuevo sitio a la memoria RAM.
     */
    fun agregarSitio(sitio: SitioSolar): Flow<Resultado<String>> = flow {
        emit(Resultado.Cargando)
        delay(300)
        try {
            val nuevoId = UUID.randomUUID().toString()
            val nuevoSitio = sitio.copy(id = nuevoId, fechaInstalacion = System.currentTimeMillis())
            val listaActual = sitiosEnMemoria.value.toMutableList()
            listaActual.add(nuevoSitio)
            sitiosEnMemoria.value = listaActual
            emit(Resultado.Exito(nuevoId))
        } catch (e: Exception) {
            emit(Resultado.Error(e.localizedMessage ?: "Error al agregar sitio."))
        }
    }

    /**
     * Actualiza un sitio existente en la memoria RAM.
     */
    fun actualizarSitio(sitio: SitioSolar): Flow<Resultado<Unit>> = flow {
        emit(Resultado.Cargando)
        delay(300)
        try {
            val listaActual = sitiosEnMemoria.value.toMutableList()
            val index = listaActual.indexOfFirst { it.id == sitio.id }
            if (index != -1) {
                listaActual[index] = sitio
                sitiosEnMemoria.value = listaActual
                emit(Resultado.Exito(Unit))
            } else {
                emit(Resultado.Error("Sitio no encontrado."))
            }
        } catch (e: Exception) {
            emit(Resultado.Error(e.localizedMessage ?: "Error al actualizar sitio."))
        }
    }

    /**
     * Elimina un sitio por su ID en la memoria RAM.
     */
    fun eliminarSitio(idSitio: String): Flow<Resultado<Unit>> = flow {
        emit(Resultado.Cargando)
        delay(300)
        try {
            val listaActual = sitiosEnMemoria.value.toMutableList()
            val removido = listaActual.removeAll { it.id == idSitio }
            if (removido) {
                sitiosEnMemoria.value = listaActual
                emit(Resultado.Exito(Unit))
            } else {
                emit(Resultado.Error("Sitio no encontrado."))
            }
        } catch (e: Exception) {
            emit(Resultado.Error(e.localizedMessage ?: "Error al eliminar sitio."))
        }
    }
}
