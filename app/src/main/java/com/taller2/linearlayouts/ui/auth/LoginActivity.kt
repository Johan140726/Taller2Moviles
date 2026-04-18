package com.taller2.linearlayouts.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.lifecycleScope
import com.example.taller2.R
import com.taller2.linearlayouts.SupabaseCliente
import com.taller2.linearlayouts.data.CredentialesManager
import com.taller2.linearlayouts.ui.main.MainActivity
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var etCorreo: EditText
    private lateinit var etContra: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnGoogle: Button
    private lateinit var tvRegistrate: TextView
    private lateinit var tvRecuperar: TextView
    private lateinit var tvHuellaDigital: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        // Inicializar vistas
        etCorreo = findViewById(R.id.etCorreo)
        etContra = findViewById(R.id.et_contra)
        btnLogin = findViewById(R.id.botonLogin)
        btnGoogle = findViewById(R.id.ingresaConGoogle)
        tvRegistrate = findViewById(R.id.registrateLogin)
        tvRecuperar = findViewById(R.id.textoRecuperar)
        tvHuellaDigital = findViewById(R.id.ingresarConHuella)

        // Manejo del teclado y barras de sistema
        val rootView = findViewById<ViewGroup>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val bottomPadding = maxOf(systemBars.bottom, imeInsets.bottom)
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, bottomPadding)
            insets
        }

        // Lógica de visibilidad del botón de huella
        // Si hay una huella activa guardada en las preferencias, mostramos el botón
        if (CredentialesManager.huellaActiva(this)) {
            tvHuellaDigital.visibility = View.VISIBLE
        } else {
            tvHuellaDigital.visibility = View.GONE
        }

        // Listener del botón Ingresar
        btnLogin.setOnClickListener {
            iniciarsesion()
        }

        // Ir a Registro
        tvRegistrate.setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }

        // Recuperar contraseña
        tvRecuperar.setOnClickListener {
            Toast.makeText(this, "Funcionalidad próximamente", Toast.LENGTH_SHORT).show()
        }

        // Botón de Google
        btnGoogle.setOnClickListener {
            iniciarSesionConGoogle()
        }

        // Iniciar sesión con huella
        tvHuellaDigital.setOnClickListener {
            mostrarDialogHuella()
        }
    }

    private fun iniciarsesion() {
        val correo = etCorreo.text.toString().trim()
        val contra = etContra.text.toString().trim()

        // Validaciones locales
        if (correo.isEmpty() || contra.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (contra.length < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
            return
        }

        // Llamada a Supabase Auth
        lifecycleScope.launch {
            try {
                SupabaseCliente.client.auth.signInWith(Email) {
                    email = correo
                    password = contra
                }
                
                // Al iniciar sesión con éxito por primera vez, activamos la huella para la próxima vez
                CredentialesManager.guardarCredenciales(this@LoginActivity, correo, contra, true)
                
                irAPantallaPrincipal()
            } catch (e: Exception) {
                val message = when {
                    e.message?.contains("invalid login credentials", ignoreCase = true) == true -> 
                        "Correo o contraseña incorrectos"
                    else -> "Error al iniciar sesión: ${e.message}"
                }
                Toast.makeText(this@LoginActivity, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun iniciarSesionConGoogle() {
        lifecycleScope.launch {
            try {
                // 1. Configurar la solicitud de Google
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId("415075410923-es3rbeb4ll2cadcirqpi0rikj2q3r547.apps.googleusercontent.com")
                    .setAutoSelectEnabled(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                // 2. Mostrar el selector de cuentas de google
                val credentialManager = CredentialManager.create(this@LoginActivity)
                val result = credentialManager.getCredential(this@LoginActivity, request)
                
                // 3. Obtener el token de Google
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)

                // 4. Enviar el token a Supabase
                SupabaseCliente.client.auth.signInWith(IDToken) {
                    idToken = googleIdTokenCredential.idToken
                    provider = Google
                }
                irAPantallaPrincipal()
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "Error Google: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun mostrarDialogHuella() {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                if (errorCode != BiometricPrompt.ERROR_USER_CANCELED && errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    Toast.makeText(this@LoginActivity, "Error biométrico: $errString", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                val correo = CredentialesManager.obtenerCorreo(this@LoginActivity)
                val contra = CredentialesManager.obtenerContrasena(this@LoginActivity)

                if (correo != null && contra != null) {
                    lifecycleScope.launch {
                        try {
                            SupabaseCliente.client.auth.signInWith(Email) {
                                email = correo
                                password = contra
                            }
                            irAPantallaPrincipal()
                        } catch (e: Exception) {
                            runOnUiThread {
                                Toast.makeText(this@LoginActivity, "Error al iniciar sesión: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(this@LoginActivity, "Inicia sesión con tu email primero", Toast.LENGTH_SHORT).show()
                    CredentialesManager.limpiarCredenciales(this@LoginActivity)
                    tvHuellaDigital.visibility = View.GONE
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(this@LoginActivity, "Autenticación fallida", Toast.LENGTH_SHORT).show()
            }
        })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Acceso con huella")
            .setSubtitle("Usa tu huella dactilar para ingresar")
            .setNegativeButtonText("Cancelar")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun irAPantallaPrincipal() {
        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
    }
}
