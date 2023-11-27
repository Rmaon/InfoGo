package com.ramon.infogo

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.appcompat.widget.Toolbar
import com.google.firebase.firestore.FirebaseFirestore
import com.ramon.infogo.databinding.IncidenciasListBinding

class Incidencias : AppCompatActivity() {

    private lateinit var binding: IncidenciasListBinding
    private lateinit var incidenciasAdapter: IncidenciasAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = IncidenciasListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar el Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Configurar RecyclerView
        binding.recyclerViewIncidencias.layoutManager = GridLayoutManager(this, 2)

        // Obtener incidencias de Firestore
        obtenerIncidenciasDeFirestore()
    }

    private fun obtenerIncidenciasDeFirestore() {
        // Consultar la colección de incidencias en Firestore
        db.collection("incidencias")
            .get()
            .addOnSuccessListener { result ->
                val incidenciasList = mutableListOf<Incidencia>()

                for (document in result) {
                    // Mapear cada documento a un objeto Incidencia
                    val incidencia = document.toObject(Incidencia::class.java)
                    incidenciasList.add(incidencia)
                }

                // Configurar el adaptador con la lista de incidencias
                incidenciasAdapter = IncidenciasAdapter(incidenciasList)
                binding.recyclerViewIncidencias.adapter = incidenciasAdapter
            }
            .addOnFailureListener { exception ->
                // Manejar errores al obtener incidencias
                // Registra el mensaje de error en el log
                Log.e("RMN", "Error al obtener incidencias", exception)
            }
    }
}
