package com.ramon.infogo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase


class SettingsFragment : Fragment() {

    private lateinit var imgUserProfile: ImageView
    private lateinit var spinnerProfileImages: Spinner
    private lateinit var tvUserName: TextView
    private lateinit var editNewName: EditText
    private lateinit var btnGuardarCambios: Button

    private val profileImages = arrayOf(R.drawable.img1, R.drawable.img2, R.drawable.img3)
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.settings_activity, container, false)

        auth = Firebase.auth

        imgUserProfile = view.findViewById(R.id.imgUserProfile)
        spinnerProfileImages = view.findViewById(R.id.spinnerProfileImages)
        tvUserName = view.findViewById(R.id.tvUserName)
        editNewName = view.findViewById(R.id.editNewName)
        btnGuardarCambios = view.findViewById(R.id.btnGuardarCambios)

        // Obtener el nombre de usuario del usuario actualmente autenticado
        val currentUser = auth.currentUser
        val currentUserName = currentUser?.displayName

        // Setear el nombre de usuario en el TextView
        tvUserName.text = getString(R.string.user_name, currentUserName ?: "UsuarioActual")

        // Set up spinner with profile images
        val profileImageAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, profileImages)
        spinnerProfileImages.adapter = profileImageAdapter

        // Set the default image in ImageView
        imgUserProfile.setImageResource(profileImages[0])

        // Set spinner item selected listener
        spinnerProfileImages.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {
                // Set the selected image in ImageView
                imgUserProfile.setImageResource(profileImages[position])
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                // Do nothing
            }
        }

        // Set button click listener
        btnGuardarCambios.setOnClickListener {
            val newName = editNewName.text.toString()
            if (newName.isNotEmpty()) {
                // Actualizar el nombre de usuario
                updateUserNameInFirebase(newName)
            } else {
                Toast.makeText(requireContext(), "Ingrese un nuevo nombre", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun updateUserNameInFirebase(newName: String) {
        // Actualizar el nombre de usuario en Firebase Authentication
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                // Otros campos si es necesario
                .build()

            user.updateProfile(profileUpdates)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Actualizar el nombre de usuario en Firestore
                        updateUserNameInFirestore(user.uid, newName)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Error al actualizar el nombre de usuario en Firebase Authentication",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    private fun updateUserNameInFirestore(userId: String, newName: String) {
        try {
            val db = FirebaseFirestore.getInstance()
            val usersRef = db.collection("users").document(userId)

            // Actualizar el nombre de usuario en Firestore
            usersRef.update("nombre", newName)
                .addOnSuccessListener {
                    tvUserName.text = getString(R.string.user_name, newName)
                    // Limpiar el EditText
                    editNewName.text.clear()
                    Toast.makeText(requireContext(), "Cambios guardados", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        requireContext(),
                        "Error al actualizar el nombre de usuario en Firestore: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "Excepción al actualizar el nombre de usuario: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
            e.printStackTrace()
        }
    }

}