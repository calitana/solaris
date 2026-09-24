package com.solartech.mantenimiento.ui.clientes

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.solartech.mantenimiento.databinding.ItemClienteBinding
import com.solartech.mantenimiento.datos.modelo.Cliente

class ClientesAdapter(
    private val onEditarClick: ((Cliente) -> Unit)? = null,
    private val onEliminarClick: ((Cliente) -> Unit)? = null
) : ListAdapter<Cliente, ClientesAdapter.ClienteViewHolder>(ClienteDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClienteViewHolder {
        val binding = ItemClienteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ClienteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ClienteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ClienteViewHolder(private val binding: ItemClienteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(cliente: Cliente) {
            binding.tvNombreCliente.text = cliente.nombre
            binding.tvEmpresaCliente.text = cliente.direccion
            binding.tvCantidadSitios.text = "Cliente"
            binding.tvTelefonoCliente.text = " ${cliente.telefono}"
            binding.tvEmailCliente.text = if (cliente.correo.isNotBlank()) " ${cliente.correo}" else ""

            binding.btnEditar.setOnClickListener {
                onEditarClick?.invoke(cliente)
            }
            binding.btnEliminar.setOnClickListener {
                onEliminarClick?.invoke(cliente)
            }
        }
    }

    class ClienteDiffCallback : DiffUtil.ItemCallback<Cliente>() {
        override fun areItemsTheSame(oldItem: Cliente, newItem: Cliente): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Cliente, newItem: Cliente): Boolean =
            oldItem == newItem
    }
}
