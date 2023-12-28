package com.ramon.infogo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class IncidenciaListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var incidenciaAdapter: IncidenciaListAdapter
    private lateinit var incidenciaList: MutableList<Incidencia>

    private lateinit var databaseReference: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.incidencia_list, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewIncidencias)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        incidenciaList = mutableListOf()
        incidenciaAdapter = IncidenciaListAdapter(incidenciaList)
        recyclerView.adapter = incidenciaAdapter

        databaseReference = FirebaseDatabase.getInstance().getReference("incidencias")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                incidenciaList.clear()
                for (incidenciaSnapshot in snapshot.children) {
                    val incidencia = incidenciaSnapshot.getValue(Incidencia::class.java)
                    incidencia?.let {
                        incidenciaList.add(it)
                    }
                }
                incidenciaAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar error en la lectura de datos
            }
        })

        return view
    }
}
