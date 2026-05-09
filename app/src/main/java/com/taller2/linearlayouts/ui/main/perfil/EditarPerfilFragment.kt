package com.taller2.linearlayouts.ui.main.perfil

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import coil.load
import coil.transform.CircleCropTransformation
import com.example.taller2.R
import com.taller2.linearlayouts.SupabaseCliente
import com.taller2.linearlayouts.data.UsuarioRepository
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch
import java.io.File
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class EditarPerfilFragment : Fragment() {

    private var uriFotoSeleccionada: Uri? = null
    private var fotoUrlActual: String? = null          // ← guarda la URL existente
    private lateinit var ivEditarFoto: ImageView
    private lateinit var archivoFotoTemp: File

    private val lanzadorPermisoCamara =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
            if (concedido) {
                abrirCamara()
            } else {
                Toast.makeText(requireContext(),
                    "Se necesita permiso de cámara para tomar fotos",
                    Toast.LENGTH_SHORT).show()
            }
        }

    private val lanzadorCamara =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
            if (exito) {
                uriFotoSeleccionada = Uri.fromFile(archivoFotoTemp)
                ivEditarFoto.load(uriFotoSeleccionada) {
                    transformations(CircleCropTransformation())
                }
            }
        }

    private val lanzadorGaleria =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                uriFotoSeleccionada = uri
                ivEditarFoto.load(uri) {
                    transformations(CircleCropTransformation())
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_editar_perfil, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ivEditarFoto       = view.findViewById(R.id.iv_editar_foto)
        val ivCamaraIcon   = view.findViewById<ImageView>(R.id.iv_camara_icon)
        val etNombres      = view.findViewById<EditText>(R.id.et_editar_nombres)
        val etApellidos    = view.findViewById<EditText>(R.id.et_editar_apellidos)
        val etCorreo       = view.findViewById<EditText>(R.id.et_editar_correo)
        val etContrasena   = view.findViewById<EditText>(R.id.et_editar_contrasena)
        val etReContrasena = view.findViewById<EditText>(R.id.et_editar_recontrasena)
        val btnGuardar     = view.findViewById<Button>(R.id.btn_guardar_perfil)

        // Cargar datos actuales
        lifecycleScope.launch {
            val usuario = UsuarioRepository.obtenerUsuarioActual()
            if (usuario != null) {
                etNombres.setText(usuario.nombres)
                etApellidos.setText(usuario.apellidos)
                etCorreo.setText(usuario.correo ?: "")
                fotoUrlActual = usuario.foto_url   // ← guardar URL actual

                if (!usuario.foto_url.isNullOrEmpty()) {
                    ivEditarFoto.load(usuario.foto_url) {
                        transformations(CircleCropTransformation())
                        placeholder(R.mipmap.logo_application)
                        error(R.mipmap.logo_application)
                    }
                }
            }
        }

        ivCamaraIcon.setOnClickListener { mostrarOpcionesFoto() }

        btnGuardar.setOnClickListener {
            guardarCambios(etNombres, etApellidos, etCorreo, etContrasena, etReContrasena)
        }
    }

    private fun mostrarOpcionesFoto() {
        val opciones = arrayOf("Tomar foto", "Elegir de galería")
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Foto de perfil")
            .setItems(opciones) { _, cual ->
                when (cual) {
                    0 -> verificarPermisoCamara()
                    1 -> lanzadorGaleria.launch("image/*")
                }
            }
            .show()
    }

    private fun verificarPermisoCamara() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> abrirCamara()

            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                android.app.AlertDialog.Builder(requireContext())
                    .setTitle("Permiso de cámara")
                    .setMessage("Necesitamos acceso a la cámara para que puedas tomar tu foto de perfil.")
                    .setPositiveButton("Entendido") { _, _ ->
                        lanzadorPermisoCamara.launch(Manifest.permission.CAMERA)
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }

            else -> lanzadorPermisoCamara.launch(Manifest.permission.CAMERA)
        }
    }

    private fun abrirCamara() {
        val carpeta = File(requireContext().cacheDir, "images")
        carpeta.mkdirs()
        archivoFotoTemp = File(carpeta, "foto_perfil_temp.jpg")

        val uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            archivoFotoTemp
        )
        lanzadorCamara.launch(uri)
    }

    private fun guardarCambios(
        etNombres: EditText,
        etApellidos: EditText,
        etCorreo: EditText,
        etContrasena: EditText,
        etReContrasena: EditText
    ) {
        val nombres      = etNombres.text.toString().trim()
        val apellidos    = etApellidos.text.toString().trim()
        val correo       = etCorreo.text.toString().trim()
        val contrasena   = etContrasena.text.toString()
        val reContrasena = etReContrasena.text.toString()

        if (nombres.isEmpty() || apellidos.isEmpty() || correo.isEmpty()) {
            Toast.makeText(requireContext(),
                "Nombres, apellidos y correo son obligatorios",
                Toast.LENGTH_SHORT).show()
            return
        }

        if (contrasena.isNotEmpty()) {
            if (contrasena.length < 6) {
                Toast.makeText(requireContext(),
                    "La contraseña debe tener mínimo 6 caracteres",
                    Toast.LENGTH_SHORT).show()
                return
            }
            if (contrasena != reContrasena) {
                Toast.makeText(requireContext(),
                    "Las contraseñas no coinciden",
                    Toast.LENGTH_SHORT).show()
                return
            }
        }

        lifecycleScope.launch {
            try {
                // Si el usuario seleccionó foto nueva → subirla
                // Si no → conservar la URL que ya estaba guardada
                val urlFinal: String? = if (uriFotoSeleccionada != null) {
                    val nueva = UsuarioRepository.subirFotoPerfil(
                        requireContext(),
                        uriFotoSeleccionada!!
                    )
                    android.util.Log.d("DEBUG_FOTO", "fotoUrl retornada: $nueva")
                    nueva.ifEmpty { fotoUrlActual }  // si falla la subida, no borrar la anterior
                } else {
                    fotoUrlActual   // ← conservar la foto existente
                }

                UsuarioRepository.actualizarPerfil(
                    nombres   = nombres,
                    apellidos = apellidos,
                    correo    = correo,
                    fotoUrl   = urlFinal
                )

                if (contrasena.isNotEmpty()) {
                    SupabaseCliente.client.auth.updateUser {
                        password = contrasena
                    }
                }

                runOnUiThread {
                    Toast.makeText(requireContext(),
                        "Perfil actualizado correctamente",
                        Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                }

            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(requireContext(),
                        "Error al guardar: ${e.message}",
                        Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun runOnUiThread(action: () -> Unit) {
        activity?.runOnUiThread(action)
    }
}