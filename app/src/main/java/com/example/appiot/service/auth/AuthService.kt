package com.example.appiot.service.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class AuthService {
    private val auth = FirebaseAuth.getInstance()
    private val WEB_CLIENT_ID = "379912503960-217s9lvk8amu0dorm859cvumjn0dg7d5.apps.googleusercontent.com"

    val usuarioActual: FirebaseUser?
        get() = auth.currentUser

    val estaAutenticado: Boolean
        get() = auth.currentUser != null

    suspend fun iniciarSesion(correo: String, clave: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(correo.trim(), clave).await()
            val user = result.user ?: throw Exception("Usuario no encontrado")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registrarUsuario(
        correo: String, clave: String
    ): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(correo.trim(), clave).await()
            val user = result.user ?: throw Exception("Usuario no encontrado")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun loginWithGoogle(context: Context): Result<FirebaseUser> {
        return try {
            // 1. Configurar la petición de Google Sign-In
            val credentialManager = CredentialManager.create(context)
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(WEB_CLIENT_ID)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            // 2. Desplegar el selector de cuentas de Android
            val response = credentialManager.getCredential(context = context, request = request)
            val credential = response.credential

            // 3. Extraer el idToken
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                // 4. Autenticar directamente en Firebase
                val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(authCredential).await()
                val user = authResult.user ?: throw Exception("Error al obtener usuario de Firebase")

                Result.success(user)
            } else {
                Result.failure(Exception("Tipo de credencial no soportado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun cerrarSesion() {
        auth.signOut()
    }
}