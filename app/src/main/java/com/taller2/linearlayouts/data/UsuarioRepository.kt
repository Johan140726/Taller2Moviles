package com.taller2.linearlayouts.data

import com.taller2.linearlayouts.SupabaseCliente
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.Serializable
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

object UsuarioRepository {

    @Serializable
    data class UsuarioData(
        val id: String,
        val nombres: String,
        val apellidos: String,
        val correo: String? = null,
        val rol: String = "cliente",
        val foto_url: String? = null
    )

    suspend fun existeUsuario(userId: String): Boolean {
        return try {
            val resultado = SupabaseCliente.client
                .postgrest["usuarios"]
                .select(Columns.raw("id")) {
                    filter { eq("id", userId) }
                }
                .decodeList<Map<String, String>>()
            resultado.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun obtenerUsuarioActual(): UsuarioData? {
        val userId = SupabaseCliente.client.auth
            .currentUserOrNull()?.id ?: return null
        return try {
            val resultado = SupabaseCliente.client
                .postgrest["usuarios"]
                .select {
                    filter { eq("id", userId) }
                }
                .decodeList<UsuarioData>()

            android.util.Log.d("DEBUG_QUERY", "Resultado completo: $resultado")
            resultado.firstOrNull()
        } catch (e: Exception) {
            android.util.Log.e("DEBUG_QUERY", "Error: ${e.message}")
            null
        }
    }

    suspend fun insertarUsuario(id: String, nombres: String, apellidos: String, correo: String) {
        SupabaseCliente.client.postgrest["usuarios"].insert(
            UsuarioData(id, nombres, apellidos, correo)
        )
    }

    suspend fun obtenerRolActual(): String {
        return try {
            val userId = SupabaseCliente.client.auth
                .currentUserOrNull()?.id ?: return "cliente"

            val resultado = SupabaseCliente.client
                .postgrest["usuarios"]
                .select {
                    filter { eq("id", userId) }
                }
                .decodeList<UsuarioData>()

            resultado.firstOrNull()?.rol ?: "cliente"
        } catch (e: Exception) {
            "cliente"
        }
    }

    suspend fun actualizarPerfil(
        nombres: String,
        apellidos: String,
        correo: String,
        fotoUrl: String? = null
    ) {
        val userId = SupabaseCliente.client.auth
            .currentUserOrNull()?.id ?: return

        android.util.Log.d("DEBUG_UPDATE", "Actualizando userId: $userId")
        android.util.Log.d("DEBUG_UPDATE", "nombres=$nombres, apellidos=$apellidos, correo=$correo, foto=$fotoUrl")

        // Construir el JsonObject manualmente para controlar
        // exactamente qué campos se envían.
        // Si fotoUrl es null NO se incluye el campo → no borra la foto existente.
        val campos = mutableMapOf<String, JsonPrimitive>(
            "nombres"   to JsonPrimitive(nombres),
            "apellidos" to JsonPrimitive(apellidos),
            "correo"    to JsonPrimitive(correo)
        )
        if (fotoUrl != null) {
            campos["foto_url"] = JsonPrimitive(fotoUrl)
        }
        val datos = JsonObject(campos)

        try {
            SupabaseCliente.client.postgrest["usuarios"]
                .update(datos) {
                    filter { eq("id", userId) }
                }
            android.util.Log.d("DEBUG_UPDATE", "Update ejecutado OK")
        } catch (e: Exception) {
            android.util.Log.e("DEBUG_UPDATE", "Error en update: ${e.message}", e)
            throw e
        }
    }

    suspend fun subirFotoPerfil(
        contexto: android.content.Context,
        uri: android.net.Uri
    ): String {
        val userId = SupabaseCliente.client.auth
            .currentUserOrNull()?.id ?: return ""

        android.util.Log.d("DEBUG_FOTO", "Uri scheme: ${uri.scheme}")
        android.util.Log.d("DEBUG_FOTO", "Uri path: ${uri.path}")

        val bytes = if (uri.scheme == "content") {
            contexto.contentResolver
                .openInputStream(uri)?.readBytes()
        } else {
            java.io.File(uri.path!!).readBytes()
        } ?: return ""

        android.util.Log.d("DEBUG_FOTO", "Bytes leídos: ${bytes.size}")

        val rutaArchivo = "perfil_$userId.jpg"

        SupabaseCliente.client.storage["avatars"]
            .upload(
                path = rutaArchivo,
                data = bytes,
                options = { upsert = true }
            )

        val url = SupabaseCliente.client.storage["avatars"]
            .publicUrl(rutaArchivo)

        android.util.Log.d("DEBUG_FOTO", "URL generada: $url")

        return url
    }
}