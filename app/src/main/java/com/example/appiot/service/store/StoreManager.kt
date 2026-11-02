package com.example.appiot.service.store

import android.util.Log
import com.example.appiot.models.Medicion
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.ZoneId
import java.util.Calendar
import java.util.TimeZone

class FirestoreManager {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val COLLECTION_MEDICION = "medicion"

    val currentUserId: String?
        get() = auth.currentUser?.uid

    suspend fun guardarMedicion(
        emisor: String, humedad: Double, temperatura: Double
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


    suspend fun obtenerMediciones(fecha: Timestamp): Result<List<Medicion>> {
        return runCatching {
            val uid = currentUserId ?: throw Exception("Usuario no autenticado")

            // 1. Extraer año, mes y día en UTC (tal como los eligió el usuario en el DatePicker)
            val calendarUtc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                time = fecha.toDate()
            }
            val anio = calendarUtc.get(Calendar.YEAR)
            val mes = calendarUtc.get(Calendar.MONTH)
            val dia = calendarUtc.get(Calendar.DAY_OF_MONTH)

            // 2. Crear el inicio del día (00:00:00.000) en la zona horaria LOCAL
            val calendarLocal = Calendar.getInstance().apply {
                set(Calendar.YEAR, anio)
                set(Calendar.MONTH, mes)
                set(Calendar.DAY_OF_MONTH, dia)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val inicioDia = Timestamp(calendarLocal.time)

            // 3. Crear el fin del día (23:59:59.999) en la zona horaria LOCAL
            calendarLocal.apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }
            val finDia = Timestamp(calendarLocal.time)

            Log.d("InicioLocal", inicioDia.toDate().toString())
            Log.d("FinLocal", finDia.toDate().toString())

            val snapshot = db.collection(COLLECTION_MEDICION)
                .whereGreaterThanOrEqualTo("fecha", inicioDia)
                .whereLessThanOrEqualTo("fecha", finDia)
                .orderBy("fecha", Query.Direction.DESCENDING)
                .get(Source.CACHE)
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

        val listener =
            db.collection(COLLECTION_MEDICION).orderBy("fecha", Query.Direction.DESCENDING)
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