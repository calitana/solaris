package com.solartech.mantenimiento.ui.sitios

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.solartech.mantenimiento.R
import com.solartech.mantenimiento.databinding.DialogAgregarSitioBinding
import com.solartech.mantenimiento.databinding.FragmentSitiosBinding
import com.solartech.mantenimiento.datos.modelo.Cliente
import com.solartech.mantenimiento.datos.modelo.SitioSolar
import com.solartech.mantenimiento.ui.autenticacion.AutenticacionViewModel
import com.solartech.mantenimiento.utilidades.Resultado
import kotlinx.coroutines.launch

class SitiosFragment : Fragment() {

    private var _binding: FragmentSitiosBinding? = null
    private val binding get() = _binding!!

    private val sitiosViewModel: SitiosViewModel by viewModels()
    private val authViewModel: AutenticacionViewModel by viewModels()
    private lateinit var adapter: SitiosAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSitiosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = SitiosAdapter(
            onEditarClick = { sitio ->
                mostrarDialogoSitio(sitio)
            },
            onEliminarClick = { sitio ->
                mostrarDialogoEliminar(sitio.id, sitio.nombreLugar)
            }
        )

        binding.rvSitios.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSitios.adapter = adapter

        binding.btnLogout.setOnClickListener {
            authViewModel.cerrarSesion()
            findNavController().navigate(R.id.action_sitios_to_login)
        }

        binding.fabAgregarSitio.setOnClickListener {
            mostrarDialogoSitio(null)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sitiosViewModel.listaSitios.collect { resultado ->
                    when (resultado) {
                        is Resultado.Cargando -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.layoutVacio.visibility = View.GONE
                        }
                        is Resultado.Exito -> {
                            binding.progressBar.visibility = View.GONE
                            val sitios = resultado.datos
                            adapter.submitList(sitios)
                            binding.layoutVacio.visibility = if (sitios.isEmpty()) View.VISIBLE else View.GONE
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

    private fun mostrarDialogoSitio(sitio: SitioSolar?) {
        val dialogBinding = DialogAgregarSitioBinding.inflate(layoutInflater)
        val clientes = sitiosViewModel.listaClientes.value

        var clienteSeleccionado: Cliente? = null

        // Configurar modelo
        val modelos = arrayOf(
            "Jinko Solar Tiger Neo",
            "Trina Solar Vertex+",
            "Trina Solar Vertex",
            "Canadian Solar HiKu",
            "Canadian Solar N-Type Ku",
            "Hanersun N-TOPCon Bifacial",
            "Amerisolar Monocristalino"
        )
        var modeloSeleccionado: String = modelos[0]

        val adapterModelos = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, modelos)
        dialogBinding.actvModelo.setAdapter(adapterModelos)
        dialogBinding.actvModelo.setText(modelos[0], false)
        dialogBinding.actvModelo.setOnItemClickListener { _, _, position, _ ->
            modeloSeleccionado = modelos[position]
        }
        
        // Si es edición, prellenar datos
        if (sitio != null) {
            dialogBinding.etNombreSitio.setText(sitio.nombreLugar)
            dialogBinding.etDireccionSitio.setText(sitio.ubicacion)
            dialogBinding.etCapacidadKwp.setText(sitio.potenciaKw.toString())
            dialogBinding.etCantidadPaneles.setText(sitio.cantidadPaneles.toString())
            dialogBinding.actvCliente.setText(sitio.nombreCliente, false)
            clienteSeleccionado = clientes.find { it.id == sitio.idCliente }

            modeloSeleccionado = sitio.modelo.ifBlank { modelos[0] }
            dialogBinding.actvModelo.setText(modeloSeleccionado, false)
        }

        val nombresClientes = clientes.map { it.nombre }
        val arrayAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, nombresClientes)
        dialogBinding.actvCliente.setAdapter(arrayAdapter)
        dialogBinding.actvCliente.setOnItemClickListener { _, _, position, _ ->
            clienteSeleccionado = clientes.getOrNull(position)
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (sitio == null) "Nuevo Sitio Solar" else "Editar Sitio Solar")
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnCancelarSitio.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnGuardarSitio.setOnClickListener {
            val nombre = dialogBinding.etNombreSitio.text?.toString().orEmpty().trim()
            val direccion = dialogBinding.etDireccionSitio.text?.toString().orEmpty().trim()
            val potenciaStr = dialogBinding.etCapacidadKwp.text?.toString().orEmpty().trim()
            val panelesStr = dialogBinding.etCantidadPaneles.text?.toString().orEmpty().trim()

            val potencia = potenciaStr.toDoubleOrNull() ?: 0.0
            val paneles = panelesStr.toIntOrNull() ?: 0

            if (nombre.isEmpty() || direccion.isEmpty() || potencia <= 0.0 || paneles <= 0) {
                Toast.makeText(requireContext(), "Por favor completa los campos obligatorios correctamente", Toast.LENGTH_SHORT).show()
            } else {
                if (sitio == null) {
                    sitiosViewModel.agregarSitio(
                        nombreLugar = nombre,
                        cantidadPaneles = paneles,
                        potenciaKw = potencia,
                        idCliente = clienteSeleccionado?.id.orEmpty(),
                        nombreCliente = clienteSeleccionado?.nombre ?: "Cliente General",
                        ubicacion = direccion,
                        modelo = modeloSeleccionado
                    )
                    Toast.makeText(requireContext(), "Sitio solar guardado exitosamente", Toast.LENGTH_SHORT).show()
                } else {
                    val sitioActualizado = sitio.copy(
                        nombreLugar = nombre,
                        cantidadPaneles = paneles,
                        potenciaKw = potencia,
                        idCliente = clienteSeleccionado?.id ?: sitio.idCliente,
                        nombreCliente = clienteSeleccionado?.nombre ?: sitio.nombreCliente,
                        ubicacion = direccion,
                        modelo = modeloSeleccionado
                    )
                    sitiosViewModel.actualizarSitio(sitioActualizado)
                    Toast.makeText(requireContext(), "Sitio actualizado", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun mostrarDialogoEliminar(idSitio: String, nombre: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar Sitio Solar")
            .setMessage("¿Deseas eliminar la instalación $nombre?")
            .setPositiveButton("Eliminar") { _, _ ->
                sitiosViewModel.eliminarSitio(idSitio)
                Toast.makeText(requireContext(), "Sitio eliminado", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
