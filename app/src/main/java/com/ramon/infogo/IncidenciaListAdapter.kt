package com.ramon.infogo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class IncidenciaListAdapter(private val incidenciaList: List<Incidencia>) :
    RecyclerView.Adapter<IncidenciaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncidenciaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.incidencia_list_item, parent, false)
        return IncidenciaViewHolder(view)
    }

    override fun onBindViewHolder(holder: IncidenciaViewHolder, position: Int) {
        holder.bind(incidenciaList[position])
    }

    override fun getItemCount(): Int {
        return incidenciaList.size
    }
}