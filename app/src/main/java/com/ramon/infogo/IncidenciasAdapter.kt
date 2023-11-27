package com.ramon.infogo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class IncidenciasAdapter(private val incidenciasList: List<Incidencia>) :
    RecyclerView.Adapter<IncidenciasAdapter.IncidenciaViewHolder>() {

    inner class IncidenciaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtNombre: TextView = itemView.findViewById(R.id.txtNombre)
        val txtTipo: TextView = itemView.findViewById(R.id.txtTipo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncidenciaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return IncidenciaViewHolder(view)
    }

    override fun onBindViewHolder(holder: IncidenciaViewHolder, position: Int) {
        val incidencia = incidenciasList[position]
        holder.txtNombre.text = incidencia.nombre
        holder.txtTipo.text = incidencia.tipo
    }

    override fun getItemCount(): Int {
        return incidenciasList.size
    }
}
