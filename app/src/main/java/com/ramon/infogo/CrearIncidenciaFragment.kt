package com.ramon.infogo

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.ramon.infogo.databinding.ActivityCrearIncidenciaBinding
import java.util.*

class CrearIncidenciaFragment : Fragment() {

    // Cambiado a propiedad pública
    private var _binding: ActivityCrearIncidenciaBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var storageReference: StorageReference
    private var selectedImageUri: Uri? = null

    private val launcherSeleccionarImagen =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUri = uri
                Toast.makeText(requireContext(), "Imagen seleccionada", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        firebaseAuth = FirebaseAuth.getInstance()
        storageReference = FirebaseStorage.getInstance().reference
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ActivityCrearIncidenciaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tipoIncidenciaOptions = arrayOf("Agresión", "Robo", "Accidente")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, tipoIncidenciaOptions)
        binding.spinnerTipoIncidencia.adapter = adapter

        binding.btnAddFoto.setOnClickListener {
            launcherSeleccionarImagen.launch("image/*")
        }

        binding.btnCrearIncidencia.setOnClickListener {
            crearIncidencia()
        }
    }

    private fun crearIncidencia() {
        val titulo = binding.editTitulo.text.toString()
        val descripcion = binding.editDescripcion.text.toString()
        val tipoIncidencia = binding.spinnerTipoIncidencia.selectedItem.toString()

        if (titulo.isNotEmpty() && descripcion.isNotEmpty() && selectedImageUri != null) {
            val imagesRef = storageReference.child("images/${UUID.randomUUID()}")

            imagesRef.putFile(selectedImageUri!!)
                .addOnSuccessListener {
                    imagesRef.downloadUrl.addOnSuccessListener { uri ->
                        val incidencia = hashMapOf(
                            "titulo" to titulo,
                            "descripcion" to descripcion,
                            "tipoIncidencia" to tipoIncidencia,
                            "imageUrl" to uri.toString(),
                            "usuarioId" to firebaseAuth.currentUser?.uid
                        )

                        FirebaseFirestore.getInstance().collection("incidencias")
                            .add(incidencia)
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "Incidencia creada con éxito", Toast.LENGTH_SHORT).show()
                                // Puedes realizar acciones adicionales después de crear la incidencia
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(requireContext(), "Error al crear la incidencia: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error al subir la imagen: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "Completa todos los campos y selecciona una imagen", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
