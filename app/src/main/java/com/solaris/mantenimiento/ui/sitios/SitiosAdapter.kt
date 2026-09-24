package com.solartech.mantenimiento.ui.sitios

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.solartech.mantenimiento.databinding.ItemSitioBinding
import com.solartech.mantenimiento.datos.modelo.SitioSolar

class SitiosAdapter(
    private val onEditarClick: ((SitioSolar) -> Unit)? = null,
    private val onEliminarClick: ((SitioSolar) -> Unit)? = null
) : ListAdapter<SitioSolar, SitiosAdapter.SitioViewHolder>(SitioDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SitioViewHolder {
        val binding = ItemSitioBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SitioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SitioViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SitioViewHolder(private val binding: ItemSitioBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(sitio: SitioSolar) {
            binding.tvNombreSitio.text = sitio.nombreLugar
            binding.tvPotencia.text = " ${sitio.potenciaKw} kWp"
            binding.tvClienteSitio.text = " Cliente: ${sitio.nombreCliente}"
            binding.tvDireccionSitio.text = " ${sitio.ubicacion}"
            binding.tvPaneles.text = "☀ ${sitio.cantidadPaneles} Paneles"
            binding.tvInversor.text = "Planta Solar"

            binding.btnEditar.setOnClickListener {
                onEditarClick?.invoke(sitio)
            }
            binding.btnEliminar.setOnClickListener {
                onEliminarClick?.invoke(sitio)
            }
        }
    }

    class SitioDiffCallback : DiffUtil.ItemCallback<SitioSolar>() {
        override fun areItemsTheSame(oldItem: SitioSolar, newItem: SitioSolar): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: SitioSolar, newItem: SitioSolar): Boolean =
            oldItem == newItem
    }
}
