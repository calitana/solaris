package com.solartech.mantenimiento.ui.autenticacion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solartech.mantenimiento.datos.modelo.Usuario
import com.solartech.mantenimiento.datos.repositorio.RepositorioAutenticacion
import com.solartech.mantenimiento.utilidades.Resultado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class AutenticacionViewModel(
    private val repositorioAutenticacion: RepositorioAutenticacion = RepositorioAutenticacion()
) : ViewModel() {

    private val _estadoAutenticacion = MutableStateFlow<Resultado<Usuario>>(Resultado.Inactivo)
    val estadoAutenticacion: StateFlow<Resultado<Usuario>> = _estadoAutenticacion.asStateFlow()

    private val _perfilTecnico = MutableStateFlow<Resultado<Usuario>>(Resultado.Inactivo)
    val perfilTecnico: StateFlow<Resultado<Usuario>> = _perfilTecnico.asStateFlow()

    val usuarioActual: Usuario?
        get() = repositorioAutenticacion.usuarioActual

    val estaAutenticado: Boolean
        get() = repositorioAutenticacion.estaAutenticado

    init {
        usuarioActual?.let { user ->
            cargarPerfilTecnico(user.id)
        }
    }

    /**
     * Realiza el inicio de sesión con correo y contraseña.
     */
    fun iniciarSesion(correo: String, contrasena: String) {
        if (correo.isBlank() || contrasena.isBlank()) {
            _estadoAutenticacion.value = Resultado.Error("Por favor completa todos los campos.")
            return
        }

        viewModelScope.launch {
            repositorioAutenticacion.iniciarSesion(correo, contrasena).collect { resultado ->
                _estadoAutenticacion.value = resultado
                if (resultado is Resultado.Exito) {
                    cargarPerfilTecnico(resultado.datos.id)
                }
            }
        }
    }

    /**
     * Da de alta a un nuevo técnico en el sistema.
     */
    fun registrarTecnico(nombre: String, correo: String, contrasena: String) {
        if (nombre.isBlank() || correo.isBlank() || contrasena.isBlank()) {
            _estadoAutenticacion.value = Resultado.Error("Todos los campos son obligatorios.")
            return
        }
        if (contrasena.length < 6) {
            _estadoAutenticacion.value = Resultado.Error("La contraseña debe tener al menos 6 caracteres.")
            return
        }

        viewModelScope.launch {
            repositorioAutenticacion.registrarUsuario(nombre, correo, contrasena).collect { resultado ->
                _estadoAutenticacion.value = resultado
                if (resultado is Resultado.Exito) {
                    cargarPerfilTecnico(resultado.datos.id)
                }
            }
        }
    }

    /**
     * Carga los datos del perfil del técnico.
     */
    fun cargarPerfilTecnico(uid: String) {
        viewModelScope.launch {
            repositorioAutenticacion.obtenerPerfilTecnico(uid).collect { resultado ->
                _perfilTecnico.value = resultado
            }
        }
    }

    /**
     * Limpia el estado de autenticación (para reintentos de login/registro).
     */
    fun reiniciarEstado() {
        _estadoAutenticacion.value = Resultado.Inactivo
    }

    /**
     * Cierra la sesión activa.
     */
    fun cerrarSesion() {
        repositorioAutenticacion.cerrarSesion()
        _estadoAutenticacion.value = Resultado.Inactivo
        _perfilTecnico.value = Resultado.Inactivo
    }
}
