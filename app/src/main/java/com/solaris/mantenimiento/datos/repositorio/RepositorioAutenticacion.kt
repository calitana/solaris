package com.solartech.mantenimiento.datos.repositorio

import com.solartech.mantenimiento.datos.modelo.Usuario
import com.solartech.mantenimiento.utilidades.Resultado
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID


class RepositorioAutenticacion {

    companion object {
        // Almacenamiento en RAM estático para simular persistencia en la sesión
        private val usuariosEnMemoria = mutableListOf<Usuario>()
        private var usuarioLogueado: Usuario? = null
    }

    val usuarioActual: Usuario?
        get() = usuarioLogueado

    val estaAutenticado: Boolean
        get() = usuarioLogueado != null

    /**
     * Inicia sesión verificando los datos en RAM.
     */
    fun iniciarSesion(correo: String, contrasena: String): Flow<Resultado<Usuario>> = flow {
        emit(Resultado.Cargando)
        delay(500) // Simular red
        try {
            // Simulamos que la contraseña siempre es válida si el correo existe (simplificación en RAM sin encriptar contraseñas)
            // o para demo, aceptamos cualquier admin@solaris.com
            var usuario = usuariosEnMemoria.find { it.correo == correo.trim() }
            
            if (usuario == null && correo == "admin@solaris.com") {
                // Crear usuario por defecto
                usuario = Usuario(
                    id = UUID.randomUUID().toString(),
                    nombre = "Administrador Solaris",
                    correo = correo,
                    rol = "Técnico Solar"
                )
                usuariosEnMemoria.add(usuario)
            }

            if (usuario != null) {
                usuarioLogueado = usuario
                emit(Resultado.Exito(usuario))
            } else {
                emit(Resultado.Error("Usuario no encontrado en la memoria."))
            }
        } catch (e: Exception) {
            emit(Resultado.Error(e.localizedMessage ?: "Error al iniciar sesión."))
        }
    }

    /**
     * Registra un nuevo técnico en memoria RAM.
     */
    fun registrarUsuario(nombre: String, correo: String, contrasena: String): Flow<Resultado<Usuario>> = flow {
        emit(Resultado.Cargando)
        delay(500) // Simular red
        try {
            val existe = usuariosEnMemoria.any { it.correo == correo.trim() }
            if (existe) {
                emit(Resultado.Error("El correo ya está registrado en memoria."))
                return@flow
            }

            val nuevoUsuario = Usuario(
                id = UUID.randomUUID().toString(),
                nombre = "Técnico: ${nombre.trim()}",
                correo = correo.trim(),
                rol = "Técnico Solar",
                fechaRegistro = System.currentTimeMillis()
            )
            usuariosEnMemoria.add(nuevoUsuario)
            
            // Loguear automáticamente
            usuarioLogueado = nuevoUsuario
            emit(Resultado.Exito(nuevoUsuario))
        } catch (e: Exception) {
            emit(Resultado.Error(e.localizedMessage ?: "Error al registrar la cuenta."))
        }
    }

    /**
     * Obtiene los datos del perfil del técnico desde la memoria.
     */
    fun obtenerPerfilTecnico(uid: String): Flow<Resultado<Usuario>> = flow {
        emit(Resultado.Cargando)
        delay(200)
        val usuario = usuariosEnMemoria.find { it.id == uid }
        if (usuario != null) {
            emit(Resultado.Exito(usuario))
        } else {
            emit(Resultado.Error("El usuario no existe en la memoria."))
        }
    }

    /**
     * Cierra la sesión activa.
     */
    fun cerrarSesion() {
        usuarioLogueado = null
    }
}
