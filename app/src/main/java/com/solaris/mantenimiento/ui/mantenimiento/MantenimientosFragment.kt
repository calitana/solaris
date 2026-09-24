package com.solartech.mantenimiento.ui.mantenimiento

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.solartech.mantenimiento.R
import com.solartech.mantenimiento.databinding.FragmentMantenimientosBinding
import com.solartech.mantenimiento.datos.modelo.OrdenMantenimiento
import com.solartech.mantenimiento.ui.autenticacion.AutenticacionViewModel
import com.solartech.mantenimiento.utilidades.Resultado
import kotlinx.coroutines.launch

class MantenimientosFragment : Fragment() {

    private var _binding: FragmentMantenimientosBinding? = null
    private val binding get() = _binding!!

    private val mantenimientosViewModel: MantenimientosViewModel by activityViewModels()
    private val authViewModel: AutenticacionViewModel by viewModels()
    private lateinit var adapter: MantenimientosAdapter

    private var listaOriginal: List<OrdenMantenimiento> = emptyList()
    private var filtroActual: String = "Todos"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMantenimientosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = MantenimientosAdapter(
            onEditarClick = { orden ->
                AgregarMantenimientoFragment.idOrdenAEditar = orden.id
                findNavController().navigate(R.id.action_mantenimientos_to_agregar)
            },
            onCambiarEstadoClick = { orden ->
                mostrarDialogoCambioEstado(orden)
            },
            onEliminarClick = { orden ->
                mostrarDialogoEliminar(orden.id, orden.nombreSitio)
            }
        )

        binding.rvMantenimientos.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMantenimientos.adapter = adapter

        binding.btnLogout.setOnClickListener {
            authViewModel.cerrarSesion()
            findNavController().navigate(R.id.action_mantenimientos_to_login)
        }

        binding.fabAgregarMantenimiento.setOnClickListener {
            AgregarMantenimientoFragment.idOrdenAEditar = null
            findNavController().navigate(R.id.action_mantenimientos_to_agregar)
        }

        configurarFiltros()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect the list of mantenimientos
                launch {
                    mantenimientosViewModel.listaMantenimientos.collect { resultado ->
                        when (resultado) {
                            is Resultado.Cargando -> {
                                binding.progressBar.visibility = View.VISIBLE
                                binding.layoutVacio.visibility = View.GONE
                            }
                            is Resultado.Exito -> {
                                binding.progressBar.visibility = View.GONE
                                listaOriginal = resultado.datos
                                aplicarFiltro()
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
                // Observe operation result to reset filter after a successful creation
                launch {
                    mantenimientosViewModel.estadoOperacion.collect { opResult ->
                        when (opResult) {
                            is Resultado.Exito -> {
                                binding.chipGroupFiltros.check(R.id.chipTodos)
                                filtroActual = "Todos"
                                aplicarFiltro()
                                mantenimientosViewModel.reiniciarEstadoOperacion()
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }

    private fun configurarFiltros() {
        binding.chipGroupFiltros.setOnCheckedStateChangeListener { _, checkedIds ->
            filtroActual = when {
                checkedIds.contains(R.id.chipPendiente) -> "Pendiente"
                checkedIds.contains(R.id.chipEnProceso) -> "En Proceso"
                checkedIds.contains(R.id.chipCompletado) -> "Completado"
                else -> "Todos"
            }
            aplicarFiltro()
        }
    }

    private fun aplicarFiltro() {
        val filtrados = if (filtroActual == "Todos") {
            listaOriginal
        } else {
            listaOriginal.filter { it.estado.equals(filtroActual, ignoreCase = true) }
        }
        adapter.submitList(filtrados)
        binding.layoutVacio.visibility = if (filtrados.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun mostrarDialogoCambioEstado(orden: OrdenMantenimiento) {
        val estados = arrayOf("Pendiente", "En Proceso", "Completado")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Actualizar Estado")
            .setItems(estados) { _, which ->
                val nuevoEstado = estados[which]
                mantenimientosViewModel.cambiarEstado(orden.id, nuevoEstado)
                Toast.makeText(requireContext(), "Estado actualizado a $nuevoEstado", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogoEliminar(idOrden: String, nombreSitio: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar Orden")
            .setMessage("¿Deseas eliminar la orden de mantenimiento de $nombreSitio?")
            .setPositiveButton("Eliminar") { _, _ ->
                mantenimientosViewModel.eliminarMantenimiento(idOrden)
                Toast.makeText(requireContext(), "Orden eliminada", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
