package com.solartech.mantenimiento.ui.mantenimiento

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.solartech.mantenimiento.R
import com.solartech.mantenimiento.databinding.ItemMantenimientoBinding
import com.solartech.mantenimiento.datos.modelo.OrdenMantenimiento
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MantenimientosAdapter(
    private val onEditarClick: ((OrdenMantenimiento) -> Unit)? = null,
    private val onCambiarEstadoClick: ((OrdenMantenimiento) -> Unit)? = null,
    private val onEliminarClick: ((OrdenMantenimiento) -> Unit)? = null
) : ListAdapter<OrdenMantenimiento, MantenimientosAdapter.MantenimientoViewHolder>(MantenimientoDiffCallback()) {

    private val formatoFecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MantenimientoViewHolder {
        val binding = ItemMantenimientoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MantenimientoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MantenimientoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MantenimientoViewHolder(private val binding: ItemMantenimientoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(orden: OrdenMantenimiento) {
            val context = binding.root.context
            binding.tvNombreSitio.text = orden.nombreSitio
            binding.tvEstadoBadge.text = orden.estado

            when (orden.estado) {
                "Completado" -> {
                    binding.tvEstadoBadge.setBackgroundColor(ContextCompat.getColor(context, R.color.status_completed_bg))
                    binding.tvEstadoBadge.setTextColor(ContextCompat.getColor(context, R.color.status_completed))
                }
                "En Proceso" -> {
                    binding.tvEstadoBadge.setBackgroundColor(ContextCompat.getColor(context, R.color.status_in_progress_bg))
                    binding.tvEstadoBadge.setTextColor(ContextCompat.getColor(context, R.color.status_in_progress))
                }
                else -> {
                    binding.tvEstadoBadge.setBackgroundColor(ContextCompat.getColor(context, R.color.status_pending_bg))
                    binding.tvEstadoBadge.setTextColor(ContextCompat.getColor(context, R.color.status_pending))
                }
            }

            binding.tvTipoMantenimiento.text = " ${orden.tipo}"
            binding.tvPrioridad.text = " ${orden.nombreCliente}"
            binding.tvPrioridad.setTextColor(ContextCompat.getColor(context, R.color.on_surface_variant))

            binding.tvDescripcion.text = orden.observaciones
            binding.tvTecnico.text = " ${orden.nombreTecnico}"
            binding.tvFecha.text = formatoFecha.format(Date(orden.fechaServicio))

            binding.root.setOnClickListener {
                onCambiarEstadoClick?.invoke(orden)
            }

            binding.btnEditar.setOnClickListener {
                onEditarClick?.invoke(orden)
            }
            binding.btnEliminar.setOnClickListener {
                onEliminarClick?.invoke(orden)
            }
        }
    }

    class MantenimientoDiffCallback : DiffUtil.ItemCallback<OrdenMantenimiento>() {
        override fun areItemsTheSame(oldItem: OrdenMantenimiento, newItem: OrdenMantenimiento): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: OrdenMantenimiento, newItem: OrdenMantenimiento): Boolean =
            oldItem == newItem
    }
}
