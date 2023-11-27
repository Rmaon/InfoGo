package com.ramon.infogo

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.ramon.infogo.UsuarioSingleton

class AjustesActivity : AppCompatActivity() {

    private lateinit var tvUserName: TextView
    private lateinit var editNewName: EditText
    private lateinit var btnGuardarCambios: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        tvUserName = findViewById(R.id.tvUserName)
        editNewName = findViewById(R.id.editNewName)
        btnGuardarCambios = findViewById(R.id.btnGuardarCambios)

        // Obtener el usuario actual desde el Singleton
        val usuarioActual = UsuarioSingleton.usuario

        // Mostrar el nombre de usuario actual
        tvUserName.text = "Nombre de Usuario: ${usuarioActual?.username}"

        btnGuardarCambios.setOnClickListener {
            guardarCambios()
        }
    }

    private fun guardarCambios() {
        val nuevoNombre = editNewName.text.toString().trim()

        if (nuevoNombre.isNotEmpty()) {
            // Actualizar el nombre de usuario en el objeto Usuario y en el Singleton
            UsuarioSingleton.usuario?.username = nuevoNombre

            // Actualizar el nombre de usuario en Firestore
            actualizarNombreUsuarioEnFirestore(nuevoNombre)

            // Actualizar la UI
            tvUserName.text = "Nombre de Usuario: $nuevoNombre"
            editNewName.text.clear()
        }
    }

    private fun actualizarNombreUsuarioEnFirestore(nuevoNombre: String) {
        try {
            // Obtener la referencia a Firestore
            val db = FirebaseFirestore.getInstance()

            // Obtener la referencia al documento del usuario en Firestore
            val userEmail = UsuarioSingleton.usuario?.email ?: ""
            val userRef = db.collection("users").document(userEmail)

            // Actualizar el nombre de usuario en Firestore
            userRef.update("username", nuevoNombre)
                .addOnSuccessListener {
                    // Éxito al actualizar el nombre de usuario en Firestore
                    // Puedes agregar acciones adicionales si es necesario
                }
                .addOnFailureListener { e ->
                    // Error al actualizar el nombre de usuario en Firestore
                    // Puedes manejar el error según tus necesidades
                }
        } catch (e: Exception) {
            // Excepción al actualizar el nombre de usuario en Firestore
            e.printStackTrace()
        }
    }
}
