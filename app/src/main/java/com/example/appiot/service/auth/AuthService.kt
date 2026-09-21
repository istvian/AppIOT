package com.example.appiot.service.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class AuthService {
    private val auth = FirebaseAuth.getInstance()

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

    fun cerrarSesion() {
        auth.signOut()
    }
}