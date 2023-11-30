package com.grupoventa.inventario_gvi.views.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.database.FirebaseDatabase
import com.grupoventa.inventario_gvi.R
import es.dmoral.toasty.Toasty

class Login : AppCompatActivity() {
    private lateinit var inputUser: EditText
    private lateinit var inputPass: EditText
    private lateinit var btnLogin: Button
    private lateinit var checkMantenerSesion: CheckBox
    private lateinit var mAuth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance()
    private lateinit var sharedPreferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        inputUser = findViewById(R.id.username)
        inputPass = findViewById(R.id.password)
        btnLogin = findViewById(R.id.login)
        mAuth = FirebaseAuth.getInstance()
        checkMantenerSesion = findViewById(R.id.recordarSesionCheck)
        sharedPreferences = getSharedPreferences("sessionPrefs", Context.MODE_PRIVATE)

        // Verifica si la sesión se debe mantener activa
        if (sharedPreferences.getBoolean("mantenerSesion", false)) {
            // Aquí puedes navegar a la siguiente actividad directamente
            val user = mAuth.currentUser
            Toasty.info(this, "Sesión activa como: ${user?.displayName}.", Toasty.LENGTH_SHORT).show()
            // Agrega aquí la lógica para navegar a la siguiente actividad
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnLogin.setOnClickListener {
            var user = inputUser.text.toString().trim()
            val pass = inputPass.text.toString().trim()

            // Verifica campos vacíos
            if (user.isEmpty() || pass.isEmpty()) {
                Toasty.warning(this, "Por favor, completa todos los campos.", Toasty.LENGTH_SHORT).show()
                return@setOnClickListener
            }else{
                user = "$user@recolecciongvi.com.mx"
            }

            // Inicia sesión
            mAuth.signInWithEmailAndPassword(user, pass)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Verifica CheckBox para mantener sesión activa
                        if (checkMantenerSesion.isChecked) {
                            // Guarda el estado de la sesión en SharedPreferences
                            with(sharedPreferences.edit()) {
                                putBoolean("mantenerSesion", true)
                                apply()
                            }
                        }
                        val user = mAuth.currentUser
                        // Inicio de sesión exitoso
                        Toasty.success(this, "Bienvenido ${user?.displayName}.", Toasty.LENGTH_SHORT).show()

                        // Aquí puedes navegar a la siguiente actividad o realizar otras acciones después del inicio de sesión
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()

                    } else {
                        // Error en el inicio de sesión
                        task.exception?.let { it1 -> handleLoginError(it1) }
                    }
                }
        }
    }
    private fun handleLoginError(exception: Exception) {
        when (exception) {
            is FirebaseAuthInvalidUserException -> {
                showErrorDialog("Usuario no encontrado", "El correo no existe")
            }
            is FirebaseAuthInvalidCredentialsException -> {
                showErrorDialog("Credenciales incorrectas", "La contraseña es incorrecta")
            }
            else -> {
                showErrorDialog("Error en el inicio de sesión", "Hubo un error en el inicio de sesión, por favor intenta nuevamente")
            }
        }
    }
    private fun showErrorDialog(title: String, content: String) {
        Toasty.error(this, "$title : $content", Toasty.LENGTH_SHORT).show()
    }
}