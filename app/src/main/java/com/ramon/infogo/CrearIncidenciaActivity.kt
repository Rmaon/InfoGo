package com.ramon.infogo

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class CrearIncidenciaActivity : AppCompatActivity() {

    private lateinit var editTitulo: EditText
    private lateinit var editTipo: EditText
    private lateinit var editDescripcion: EditText
    private lateinit var btnCrearIncidencia: Button

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_incidencia)

        editTitulo = findViewById(R.id.editTitulo)
        editTipo = findViewById(R.id.editTipo)
        editDescripcion = findViewById(R.id.editDescripcion)
        btnCrearIncidencia = findViewById(R.id.btnCrearIncidencia)

        btnCrearIncidencia.setOnClickListener {
            // Obtener el usuario actual desde el Singleton o cualquier otra fuente
            val usuarioActual = UsuarioSingleton.usuario

            if (usuarioActual != null) {
                crearIncidencia(usuarioActual)
            } else {
                // Manejar el caso donde el usuario no está disponible
                Toast.makeText(
                    this@CrearIncidenciaActivity,
                    "Usuario no disponible",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    private fun crearIncidencia(usuario: Usuario) {
        val titulo = editTitulo.text.toString().trim()
        val tipo = editTipo.text.toString().trim()
        val descripcion = editDescripcion.text.toString().trim()

        if (titulo.isNotEmpty() && tipo.isNotEmpty() && descripcion.isNotEmpty()) {
            // Creamos un objeto Incidencia con los datos
            val incidencia = Incidencia(titulo, tipo, descripcion, usuario.username)

            // Añadimos la incidencia a Firestore
            db.collection("incidencias")
                .add(incidencia)
                .addOnSuccessListener {
                    // Incidencia creada con éxito
                    finish()
                }
                .addOnFailureListener { e ->
                    // Error al crear la incidencia
                    Toast.makeText(
                        this@CrearIncidenciaActivity,
                        "Error al crear la incidencia: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } else {
            // Mostramos un mensaje si algún campo está vacío
            Toast.makeText(this@CrearIncidenciaActivity, "Completa todos los campos", Toast.LENGTH_SHORT).show()
        }
    }

}
