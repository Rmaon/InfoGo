package com.ramon.infogo

import android.Manifest
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.ramon.infogo.databinding.ActivityCrearIncidenciaBinding
import java.io.IOException
import java.util.UUID

class CrearIncidenciaFragment : Fragment() {

    private val TAG = "RMAON"
    private var _binding: ActivityCrearIncidenciaBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var storageReference: StorageReference
    private var selectedImageUri: Uri? = null

    private lateinit var bitmap: Bitmap

    private val openCamera = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            handleCameraResult(result.data)
        }
    }

    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                startCamera()
            } else {
                Log.e(TAG, "Permiso de cámara no concedido")
            }
        }

    private val launcherSeleccionarImagen =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                handleGalleryResult(uri)
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
        setupSpinner()

        binding.btnAddFoto.setOnClickListener {
            mostrarDialogoSeleccionImagen()
        }

        binding.btnCrearIncidencia.setOnClickListener {
            crearIncidencia()
        }
    }

    private fun setupSpinner() {
        val tipoIncidenciaOptions = arrayOf("Agresión", "Robo", "Accidente")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, tipoIncidenciaOptions)
        binding.spinnerTipoIncidencia.adapter = adapter
    }

    private fun mostrarDialogoSeleccionImagen() {
        val opciones = arrayOf("Cámara", "Galería")

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Seleccionar imagen desde:")
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    1 -> launcherSeleccionarImagen.launch("image/*")
                }
            }
            .show()
    }

    private fun handleCameraResult(data: Intent?) {
        val imageBitmap = data?.extras?.get("data") as Bitmap?
        imageBitmap?.let {
            selectedImageUri = saveImageToGallery(it)
            binding.imgSelect.setImageBitmap(it)
        }
    }

    private fun startCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        openCamera.launch(intent)
    }

    private fun handleGalleryResult(uri: Uri) {
        selectedImageUri = uri
        binding.imgSelect.setImageURI(uri)
    }

    private fun saveImageToGallery(bitmap: Bitmap): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "image_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }

        val contentResolver = requireContext().contentResolver
        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        try {
            val outputStream = uri?.let { contentResolver.openOutputStream(it) }
            outputStream?.use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error al guardar la imagen en la galería", Toast.LENGTH_SHORT).show()
            return null
        }

        Toast.makeText(requireContext(), "Imagen guardada en la galería", Toast.LENGTH_SHORT).show()
        return uri
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
