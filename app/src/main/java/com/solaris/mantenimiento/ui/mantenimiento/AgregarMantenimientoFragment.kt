package com.solartech.mantenimiento.ui.mantenimiento

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.solartech.mantenimiento.databinding.FragmentAgregarMantenimientoBinding
import com.solartech.mantenimiento.datos.modelo.SitioSolar
import com.solartech.mantenimiento.datos.modelo.OrdenMantenimiento
import com.solartech.mantenimiento.ui.autenticacion.AutenticacionViewModel
import com.solartech.mantenimiento.utilidades.Resultado

class AgregarMantenimientoFragment : Fragment() {

    companion object {
        var idOrdenAEditar: String? = null
    }

    private var _binding: FragmentAgregarMantenimientoBinding? = null
    private val binding get() = _binding!!

    private val mantenimientosViewModel: MantenimientosViewModel by activityViewModels()
    private val authViewModel: AutenticacionViewModel by viewModels()

    private var sitioSeleccionado: SitioSolar? = null
    private var tipoSeleccionado: String = "Preventivo"
    private var prioridadSeleccionada: String = "Media"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAgregarMantenimientoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Configurar sitios
        val sitios = mantenimientosViewModel.listaSitios.value
        val nombresSitios = sitios.map { "${it.nombreLugar} (${it.nombreCliente})" }
        val adapterSitios = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, nombresSitios)
        binding.actvSitio.setAdapter(adapterSitios)
        binding.actvSitio.setOnItemClickListener { _, _, position, _ ->
            sitioSeleccionado = sitios.getOrNull(position)
        }

        // Configurar tipo
        val tipos = arrayOf("Preventivo", "Correctivo", "Inspección General", "Limpieza de Paneles")
        val adapterTipos = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, tipos)
        binding.actvTipo.setAdapter(adapterTipos)
        binding.actvTipo.setText(tipos[0], false)
        binding.actvTipo.setOnItemClickListener { _, _, position, _ ->
            tipoSeleccionado = tipos[position]
        }

        // Configurar prioridad
        val prioridades = arrayOf("Alta", "Media", "Baja")
        val adapterPrioridades = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, prioridades)
        binding.actvPrioridad.setAdapter(adapterPrioridades)
        binding.actvPrioridad.setText(prioridades[1], false)
        binding.actvPrioridad.setOnItemClickListener { _, _, position, _ ->
            prioridadSeleccionada = prioridades[position]
        }

        // Nombre de técnico sugerido
        val usuario = authViewModel.usuarioActual
        binding.etTecnico.setText(usuario?.nombre ?: usuario?.correo?.substringBefore("@") ?: "Técnico Solar")

        var ordenAEditar: OrdenMantenimiento? = null

        // Si es edición, prellenar datos
        val idEditar = idOrdenAEditar
        if (idEditar != null) {
            val listaMantenimientos = (mantenimientosViewModel.listaMantenimientos.value as? Resultado.Exito)?.datos ?: emptyList()
            ordenAEditar = listaMantenimientos.find { it.id == idEditar }
            if (ordenAEditar != null) {
                binding.btnCrearMantenimiento.text = "Actualizar Orden"
                
                // Prellenar observaciones (quitando el prefijo si existe para la vista)
                var desc = ordenAEditar.observaciones
                var obs = ""
                if (desc.contains("[Obs:")) {
                    val partes = desc.split("[Obs:")
                    desc = partes[0].trim()
                    obs = partes[1].replace("]", "").trim()
                }
                binding.etDescripcion.setText(desc)
                binding.etObservaciones.setText(obs)
                
                binding.etTecnico.setText(ordenAEditar.nombreTecnico)
                
                // Set sitio
                sitioSeleccionado = sitios.find { it.id == ordenAEditar.idSitio }
                if (sitioSeleccionado != null) {
                    binding.actvSitio.setText("${sitioSeleccionado!!.nombreLugar} (${sitioSeleccionado!!.nombreCliente})", false)
                }

                // Set tipo
                tipoSeleccionado = ordenAEditar.tipo
                binding.actvTipo.setText(tipoSeleccionado, false)
            }
        }

        binding.btnCrearMantenimiento.setOnClickListener {
            val descripcion = binding.etDescripcion.text?.toString().orEmpty().trim()
            val tecnico = binding.etTecnico.text?.toString().orEmpty().trim()
            val observaciones = binding.etObservaciones.text?.toString().orEmpty().trim()

            if (sitioSeleccionado == null) {
                Toast.makeText(requireContext(), "Por favor selecciona un sitio solar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (descripcion.isEmpty() || tecnico.isEmpty()) {
                Toast.makeText(requireContext(), "Por favor completa la descripción y el técnico responsable", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val textoCompleto = if (observaciones.isNotEmpty()) "$descripcion [Obs: $observaciones]" else descripcion

            if (ordenAEditar == null) {
                mantenimientosViewModel.crearMantenimiento(
                    tipo = tipoSeleccionado,
                    estado = "Pendiente",
                    observaciones = textoCompleto,
                    sitio = sitioSeleccionado,
                    nombreTecnico = tecnico,
                    idTecnico = usuario?.id.orEmpty()
                )
                Toast.makeText(requireContext(), "Orden de servicio registrada exitosamente", Toast.LENGTH_SHORT).show()
            } else {
                val ordenActualizada = ordenAEditar.copy(
                    tipo = tipoSeleccionado,
                    observaciones = textoCompleto,
                    nombreTecnico = tecnico,
                    idSitio = sitioSeleccionado?.id ?: ordenAEditar.idSitio,
                    nombreSitio = sitioSeleccionado?.nombreLugar ?: ordenAEditar.nombreSitio,
                    idCliente = sitioSeleccionado?.idCliente ?: ordenAEditar.idCliente,
                    nombreCliente = sitioSeleccionado?.nombreCliente ?: ordenAEditar.nombreCliente,
                    potenciaKw = sitioSeleccionado?.potenciaKw ?: ordenAEditar.potenciaKw
                )
                mantenimientosViewModel.actualizarMantenimiento(ordenActualizada)
                Toast.makeText(requireContext(), "Orden actualizada", Toast.LENGTH_SHORT).show()
            }

            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
