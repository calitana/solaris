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
import com.solartech.mantenimiento.databinding.FragmentLoginBinding
import com.solartech.mantenimiento.utilidades.Resultado
import kotlinx.coroutines.launch


class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AutenticacionViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Si ya está autenticado, navegar directamente a Clientes
        if (authViewModel.estaAutenticado) {
            findNavController().navigate(R.id.action_login_to_clientes)
            return
        }

        binding.btnIniciarSesion.setOnClickListener {
            val email = binding.etEmail.text?.toString().orEmpty().trim()
            val password = binding.etPassword.text?.toString().orEmpty().trim()

            if (email.isEmpty() || password.isEmpty()) {
                binding.tvError.text = "Por favor ingresa correo y contraseña"
                binding.tvError.visibility = View.VISIBLE
            } else {
                binding.tvError.visibility = View.GONE
                authViewModel.iniciarSesion(email, password)
            }
        }

        binding.btnIrARegistro.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_registro)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.estadoAutenticacion.collect { resultado ->
                    when (resultado) {
                        is Resultado.Cargando -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.btnIniciarSesion.isEnabled = false
                            binding.tvError.visibility = View.GONE
                        }
                        is Resultado.Exito -> {
                            binding.progressBar.visibility = View.GONE
                            binding.btnIniciarSesion.isEnabled = true
                            Toast.makeText(requireContext(), "Bienvenido técnico solar", Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.action_login_to_clientes)
                        }
                        is Resultado.Error -> {
                            binding.progressBar.visibility = View.GONE
                            binding.btnIniciarSesion.isEnabled = true
                            binding.tvError.text = resultado.mensaje
                            binding.tvError.visibility = View.VISIBLE
                        }
                        is Resultado.Inactivo -> {
                            binding.progressBar.visibility = View.GONE
                            binding.btnIniciarSesion.isEnabled = true
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
