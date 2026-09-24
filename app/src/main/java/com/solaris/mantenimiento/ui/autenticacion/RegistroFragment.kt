package com.solartech.mantenimiento.ui.autenticacion

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.solartech.mantenimiento.R
import com.solartech.mantenimiento.databinding.FragmentRegistroBinding
import com.solartech.mantenimiento.utilidades.Resultado
import kotlinx.coroutines.launch


class RegistroFragment : Fragment() {

    private var _binding: FragmentRegistroBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AutenticacionViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistroBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnIrALogin.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnRegistrar.setOnClickListener {
            val nombre = binding.etNombre.text?.toString().orEmpty().trim()
            val email = binding.etEmail.text?.toString().orEmpty().trim()
            val password = binding.etPassword.text?.toString().orEmpty().trim()
            val confirmPassword = binding.etConfirmarPassword.text?.toString().orEmpty().trim()

            when {
                nombre.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() -> {
                    mostrarError("Por favor completa todos los campos obligatorios.")
                }
                password.length < 6 -> {
                    mostrarError("La contraseña debe tener al menos 6 caracteres.")
                }
                password != confirmPassword -> {
                    mostrarError("Las contraseñas no coinciden.")
                }
                else -> {
                    binding.tvErrorRegistro.visibility = View.GONE
                    authViewModel.registrarTecnico(nombre, email, password)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.estadoAutenticacion.collect { resultado ->
                    when (resultado) {
                        is Resultado.Cargando -> {
                            binding.progressBarRegistro.visibility = View.VISIBLE
                            binding.btnRegistrar.isEnabled = false
                            binding.tvErrorRegistro.visibility = View.GONE
                        }
                        is Resultado.Exito -> {
                            binding.progressBarRegistro.visibility = View.GONE
                            binding.btnRegistrar.isEnabled = true
                            Toast.makeText(requireContext(), "Cuenta técnica creada exitosamente", Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.action_registro_to_clientes)
                        }
                        is Resultado.Error -> {
                            binding.progressBarRegistro.visibility = View.GONE
                            binding.btnRegistrar.isEnabled = true
                            mostrarError(resultado.mensaje)
                        }
                        is Resultado.Inactivo -> {
                            binding.progressBarRegistro.visibility = View.GONE
                            binding.btnRegistrar.isEnabled = true
                        }
                    }
                }
            }
        }
    }

    private fun mostrarError(mensaje: String) {
        binding.tvErrorRegistro.text = mensaje
        binding.tvErrorRegistro.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
