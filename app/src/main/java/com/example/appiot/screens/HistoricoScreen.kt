package com.example.appiot.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CalendarLocale
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DateRangePickerState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appiot.ui.theme.AppIOTTheme
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import com.example.appiot.models.Medicion
import com.example.appiot.service.ble.BleConnectionManager
import com.example.appiot.service.store.FirestoreManager
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone


@Composable
fun HistoricoScreen(modifier: Modifier = Modifier) {
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }

    // 1. Estados para almacenar los datos recibidos y el estado de carga
    var mediciones by remember { mutableStateOf<List<Medicion>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    val handleDismiss = { showDatePicker = false }

    val handleDateSelected: (Long?) -> Unit = { millis ->
        selectedDateMillis = millis
        showDatePicker = false
    }

    LaunchedEffect(selectedDateMillis) {
        val millis = selectedDateMillis
        if (millis != null) {
            isLoading = true

            // Convertimos los milisegundos del DatePicker a Timestamp de Firebase
            val fechaTimestamp = Timestamp(Date(millis))

            // Llamada a tu función suspend
            val result = FirestoreManager().obtenerMediciones(fechaTimestamp)

            result.onSuccess { listaObtenida ->
                mediciones = listaObtenida
                Log.d("Datos descargados", "cantidad: ${listaObtenida.size}")
            }.onFailure { error ->
                Log.e("FirestoreError", "Error al obtener mediciones", error)
            }

            isLoading = false
        }
    }

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .navigationBarsPadding()
            .fillMaxHeight()
            .fillMaxWidth()
            .padding(16.dp)
    ) {

        Text(
            text = "Datos historicos",
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start,
            fontSize = 20.sp,
        )
        Button(onClick = {
            showDatePicker = true
        }) { Text(text = "Seleccionar fecha") }

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = selectedDateMillis
            )

            DatePickerDialog(onDismissRequest = handleDismiss, confirmButton = {
                TextButton(
                    onClick = { handleDateSelected(datePickerState.selectedDateMillis) },
                    enabled = datePickerState.selectedDateMillis != null
                ) {
                    Text("Aceptar")
                }
            }, dismissButton = {
                TextButton(onClick = handleDismiss) {
                    Text("Cancelar")
                }
            }) {
                DatePicker(state = datePickerState)
            }

        }
        val millis = selectedDateMillis
        val fechaTexto = remember(millis) {
            if (millis != null) {
                val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC") // Evita la conversión a hora local
                }
                formatter.format(Date(millis))
            } else {
                ""
            }
        }

        Text(text = fechaTexto)

        if (mediciones.isEmpty()) {
            Text(text = "Sin datos", fontSize = 12.sp)
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* Acción al presionar la medición */ },
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Hora", fontSize = 12.sp)
                Text(text = "Temperatura", fontSize = 12.sp)
                Text(text = "Humedad", fontSize = 12.sp)
            }
        }
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(mediciones) { medicion ->

                    val horaFormateada = remember(medicion.fecha) {
                        val sdf = SimpleDateFormat("HH:mm 'hrs.'", Locale.getDefault())
                        sdf.format(medicion.fecha.toDate())
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { /* Acción al presionar la medición */ },
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = horaFormateada, fontSize = 12.sp)
                        Text(text = "${medicion.temperatura}°C", fontSize = 12.sp)
                        Text(text = "${medicion.humedad} %", fontSize = 12.sp)
                    }
                }
            }
        }
    }


}

@Composable
fun HistoricoScreenContent(modifier: Modifier = Modifier) {
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    val datePickerState = rememberDatePickerState()
    val handleDismiss = {
        showDatePicker = false
    }

    val handleDateSelected: (Long?) -> Unit = { millis ->
        selectedDateMillis = millis
        showDatePicker = false
    }
    val nombres = listOf("Audifonos", "ESP32", "PC-Escritorio")

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .navigationBarsPadding()
            .fillMaxHeight()
            .fillMaxWidth()
            .padding(16.dp)
    ) {

        Text(
            text = "Datos historicos",
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start,
            fontSize = 20.sp,
        )
        Button(onClick = {
            showDatePicker = true
        }) { Text(text = "Seleccionar fecha") }

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = selectedDateMillis
            )

            DatePickerDialog(onDismissRequest = handleDismiss, confirmButton = {
                TextButton(
                    onClick = { handleDateSelected(datePickerState.selectedDateMillis) },
                    enabled = datePickerState.selectedDateMillis != null
                ) {
                    Text("Aceptar")
                }
            }, dismissButton = {
                TextButton(onClick = handleDismiss) {
                    Text("Cancelar")
                }
            }) {
                DatePicker(state = datePickerState)
            }

        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(nombres) { nombre ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            // Conectar con item.device
                        }, horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "07:35 hrs.", fontSize = 12.sp)
                    }
                    Text(text = "10°C", fontSize = 12.sp)
                }
            }
        }
    }


}

private fun formatDate(millis: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC") // Material3 trabaja en UTC
    }
    return formatter.format(Date(millis))
}


@Preview(showBackground = true)
@Composable
fun HistoricoScreenPreview() {
    AppIOTTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {
                HistoricoScreenContent()
            }
        }
    }
}