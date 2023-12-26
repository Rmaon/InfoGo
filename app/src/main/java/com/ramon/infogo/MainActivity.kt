package com.ramon.infogo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.ramon.infogo.databinding.ActivityMainBinding
import com.ramon.infogo.databinding.DialogErrorBinding

class MainActivity : AppCompatActivity() {

    private val TAG = "RMN"
    private lateinit var bindingErrorDialog: DialogErrorBinding
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializa Firebase Auth
        auth = FirebaseAuth.getInstance()

        binding.btnLogin.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                signIn(email, password)
            } else {
                showCustomErrorDialog("Ingrese un correo electrónico y una contraseña.")
            }
        }

        binding.emailEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Muestra passwordEditText solo si emailEditText tiene texto
                if (s.isNullOrEmpty()) {
                    binding.passwordEditText.visibility = View.INVISIBLE
                    binding.btnLogin.visibility = View.INVISIBLE
                } else {
                    binding.passwordEditText.visibility = View.VISIBLE
                    binding.btnLogin.visibility = View.VISIBLE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No es necesario implementar este método
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // No es necesario implementar este método
            }
        })

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.your_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this,gso)


        binding.btnRegister.setOnClickListener {
            showRegisterDialog()
        }

        binding.btnGoogle.setOnClickListener {
            loginEnGoogle()
        }
    }

    private fun signIn(email: String, password: String) {
        if (isValidEmail(email)) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.e(TAG, "Inicio de sesión exitoso.")
                        // Al iniciar sesión exitosamente, obtener información adicional del usuario desde Firebase
                        val user = auth.currentUser
                        user?.let {
                            obtenerInformacionUsuario(it.uid)
                        }
                    } else {
                        showCustomErrorDialog("Usuario no registrado o contraseña incorrecta")
                        Log.e(TAG, "Error en el inicio de sesión: ${task.exception?.message}")
                    }
                }
        } else {
            showCustomErrorDialog("Ingrese una dirección de correo electrónico válida")
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})"
        return email.matches(emailRegex.toRegex())
    }


    private fun loginEnGoogle() {
        // Este método es nuestro.
        val signInClient = googleSignInClient.signInIntent
        launcherVentanaGoogle.launch(signInClient)
    }

    // Con este launcher, abro la ventana que me lleva a la validación de Google.
    private val launcherVentanaGoogle =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // Si la ventana va bien, se accede a las propiedades que trae la propia ventana q llamamos y recogemos en result.
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                manejarResultados(task)
            }
        }

    // Es como una muñeca rusa, vamos desgranando, de la ventana a task y de task a los datos concretos que me da google.
    private fun manejarResultados(task: Task<GoogleSignInAccount>) {
        if (task.isSuccessful) {
            val account: GoogleSignInAccount? = task.result
            if (account != null) {
                actualizarUI(account)
            }
        } else {
            Toast.makeText(this, task.exception.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    // Esta función actualiza o repinta la interfaz de usuario UI.
    private fun actualizarUI(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        // Pido un token, y con ese token, si todo va bien obtengo la info.
        auth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                // Realizar acciones después de iniciar sesión con Google
                cambiarAActividadPrincipal()
            } else {
                Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun obtenerInformacionUsuario(userId: String) {
        try {
            // Obtener la referencia a Firestore
            val db = FirebaseFirestore.getInstance()

            // Obtener la referencia al documento del usuario en Firestore
            val userRef: DocumentReference = db.collection("users").document(userId)

            userRef.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document.exists()) {
                        // El usuario ya tiene información en Firestore
                        val usuario = document.toObject(Usuario::class.java)
                        usuario?.let {
                            // Almacenar la información del usuario en el Singleton
                            UsuarioSingleton.usuario = it
                            // Cambiar a otra actividad después de iniciar sesión
                            cambiarAActividadPrincipal()
                        }
                    } else {
                        // No hay información del usuario en Firestore
                        Log.e(TAG, "No se encontró información del usuario en Firestore.")
                    }
                } else {
                    // Error al obtener información del usuario en Firestore
                    Log.e(TAG, "Error al obtener información del usuario en Firestore: ${task.exception?.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al obtener información del usuario: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun guardarUsuarioEnFirestore(userId: String, email: String, password: String, username: String) {
        try {
            val db = FirebaseFirestore.getInstance()
            val usersRef = db.collection("users").document(userId)

            // Crear un objeto de Usuario con la información necesaria
            val usuario = Usuario(email = email, password = password, username = username)

            // Guardar el usuario en Firestore
            usersRef.set(usuario)
                .addOnSuccessListener {
                    Log.e(TAG, "Usuario guardado en Firestore con éxito.")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error al guardar usuario en Firestore: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al guardar usuario en Firestore: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun cambiarAActividadPrincipal() {
        try {
            val intent = Intent(this, Incidencias::class.java)
            startActivity(intent)
            finish() // Agrega esta línea para cerrar la actividad actual
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al cambiar a la actividad principal: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun showRegisterDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_register, null)
        builder.setView(dialogView)

        val emailEditText = dialogView.findViewById(R.id.emailEditTextDialog) as EditText
        val passwordEditText = dialogView.findViewById(R.id.passwordEditTextDialog) as EditText
        val confirmPasswordEditText =
            dialogView.findViewById(R.id.confirmPasswordEditTextDialog) as EditText
        val usernameEditText = dialogView.findViewById(R.id.usernameEditTextDialog) as EditText

        builder.setPositiveButton("Registrar") { dialog, _ ->
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()
            val username = usernameEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty() && username.isNotEmpty()) {
                if (password == confirmPassword) {
                    // Contraseñas coinciden, proceder con el registro
                    register(email, password, username)
                } else {
                    // Contraseñas no coinciden, mostrar mensaje de error
                    showCustomErrorDialog("Las contraseñas no coinciden. Por favor, inténtalo de nuevo.")
                }
            } else {
                // Mostrar mensaje de error si algún campo está vacío
                showCustomErrorDialog("Por favor, completa todos los campos.")
            }
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun register(email: String, password: String, username: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Registro exitoso, ahora guardar en Firestore
                    val userId = auth.currentUser?.uid ?: ""
                    guardarUsuarioEnFirestore(userId, email, password, username)
                    Log.e(TAG, "Registro exitoso.")
                } else {
                    Log.e(TAG, "Error en el registro: ${task.exception?.message}")
                    showCustomErrorDialog("Error en el registro. Por favor, inténtalo de nuevo.")
                }
            }
    }

    private fun showCustomErrorDialog(message: String) {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        bindingErrorDialog = DialogErrorBinding.inflate(inflater)
        val dialogView = bindingErrorDialog.root
        builder.setView(dialogView)

        bindingErrorDialog.txtError.text = message
        builder.setPositiveButton("Aceptar") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }
}
