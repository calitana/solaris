package com.solartech.mantenimiento.ui.clientes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.solartech.mantenimiento.R
import com.solartech.mantenimiento.databinding.DialogAgregarClienteBinding
import com.solartech.mantenimiento.databinding.FragmentClientesBinding
import com.solartech.mantenimiento.ui.autenticacion.AutenticacionViewModel
import com.solartech.mantenimiento.utilidades.Resultado
import kotlinx.coroutines.launch
import com.solartech.mantenimiento.datos.modelo.Cliente

class ClientesFragment : Fragment() {

    private var _binding: FragmentClientesBinding? = null
    private val binding get() = _binding!!

    private val clientesViewModel: ClientesViewModel by viewModels()
    private val authViewModel: AutenticacionViewModel by viewModels()
    private lateinit var adapter: ClientesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClientesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ClientesAdapter(
            onEditarClick = { cliente ->
                mostrarDialogoCliente(cliente)
            },
            onEliminarClick = { cliente ->
                mostrarDialogoEliminar(cliente.id, cliente.nombre)
            }
        )

        binding.rvClientes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvClientes.adapter = adapter

        binding.btnLogout.setOnClickListener {
            authViewModel.cerrarSesion()
            findNavController().navigate(R.id.action_clientes_to_login)
        }

        binding.fabAgregarCliente.setOnClickListener {
            mostrarDialogoCliente(null)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                clientesViewModel.listaClientes.collect { resultado ->
                    when (resultado) {
                        is Resultado.Cargando -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.layoutVacio.visibility = View.GONE
                        }
                        is Resultado.Exito -> {
                            binding.progressBar.visibility = View.GONE
                            val clientes = resultado.datos
                            adapter.submitList(clientes)
                            binding.layoutVacio.visibility = if (clientes.isEmpty()) View.VISIBLE else View.GONE
                        }
                        is Resultado.Error -> {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(requireContext(), resultado.mensaje, Toast.LENGTH_SHORT).show()
                        }
                        is Resultado.Inactivo -> {
                            binding.progressBar.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    private fun mostrarDialogoCliente(cliente: Cliente?) {
        val dialogBinding = DialogAgregarClienteBinding.inflate(layoutInflater)
        
        // Si es edición, prellenar datos
        if (cliente != null) {
            dialogBinding.etNombre.setText(cliente.nombre)
            dialogBinding.etEmpresa.setText(cliente.direccion)
            dialogBinding.etTelefono.setText(cliente.telefono)
            dialogBinding.etEmail.setText(cliente.correo)
        }
        
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (cliente == null) "Nuevo Cliente" else "Editar Cliente")
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnGuardar.setOnClickListener {
            val nombre = dialogBinding.etNombre.text?.toString().orEmpty().trim()
            val direccion = dialogBinding.etEmpresa.text?.toString().orEmpty().trim()
            val telefono = dialogBinding.etTelefono.text?.toString().orEmpty().trim()
            val email = dialogBinding.etEmail.text?.toString().orEmpty().trim()

            if (nombre.isEmpty() || telefono.isEmpty() || direccion.isEmpty()) {
                Toast.makeText(requireContext(), "Por favor completa los campos obligatorios", Toast.LENGTH_SHORT).show()
            } else {
                if (cliente == null) {
                    clientesViewModel.agregarCliente(nombre, telefono, direccion, email)
                    Toast.makeText(requireContext(), "Cliente guardado exitosamente", Toast.LENGTH_SHORT).show()
                } else {
                    val clienteActualizado = cliente.copy(
                        nombre = nombre,
                        direccion = direccion,
                        telefono = telefono,
                        correo = email
                    )
                    clientesViewModel.actualizarCliente(clienteActualizado)
                    Toast.makeText(requireContext(), "Cliente actualizado", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun mostrarDialogoEliminar(idCliente: String, nombre: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar Cliente")
            .setMessage("¿Deseas eliminar a $nombre?")
            .setPositiveButton("Eliminar") { _, _ ->
                clientesViewModel.eliminarCliente(idCliente)
                Toast.makeText(requireContext(), "Cliente eliminado", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
