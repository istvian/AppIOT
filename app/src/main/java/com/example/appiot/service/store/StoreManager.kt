package com.example.appiot.service.store

import com.example.appiot.models.Medicion
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreManager {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val COLLECTION_MEDICION = "medicion"

    val currentUserId: String?
        get() = auth.currentUser?.uid

    suspend fun guardarMedicion(
        emisor: String,
        humedad: Number,
        temperatura: Number
    ): Result<Boolean> {
        return runCatching {
            val uid = currentUserId ?: throw Exception("Usuario no autenticado")

            // Referencia a un documento nuevo (genera un ID automáticamente)
            val docRef = db.collection(COLLECTION_MEDICION).document()

            var nuevaMedicion = Medicion()
            nuevaMedicion.id = docRef.id
            nuevaMedicion.fecha = Timestamp.now()
            nuevaMedicion.emisor = emisor
            nuevaMedicion.humedad = humedad
            nuevaMedicion.temperatura = temperatura


            docRef.set(nuevaMedicion).await()
            true
        }
    }


    suspend fun obtenerMediciones(): Result<List<Medicion>> {
        return runCatching {
            val uid = currentUserId ?: throw Exception("Usuario no autenticado")

            val snapshot = db.collection(COLLECTION_MEDICION)
                .orderBy("fecha", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { it.toObject<Medicion>() }
        }
    }


    fun obtenerMedicionesTiempoReal(): Flow<List<Medicion>> = callbackFlow {
        val uid = currentUserId
        if (uid == null) {
            close(Exception("Usuario no autenticado"))
            return@callbackFlow
        }

        val listener = db.collection(COLLECTION_MEDICION)
            .orderBy("fecha", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val mediciones = snapshot.documents.mapNotNull { it.toObject<Medicion>() }
                    trySend(mediciones)
                }
            }

        // Se ejecuta cuando el Flow se cancela o deja de usarse
        awaitClose { listener.remove() }
    }
}