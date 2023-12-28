package com.ramon.infogo

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class IncidenciaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val txtNombre: TextView = itemView.findViewById(R.id.txtNombre)
    private val txtTipo: TextView = itemView.findViewById(R.id.txtTipo)

    fun bind(incidencia: Incidencia) {
        txtNombre.text = incidencia.nombre
        txtTipo.text = incidencia.tipo
    }
}
