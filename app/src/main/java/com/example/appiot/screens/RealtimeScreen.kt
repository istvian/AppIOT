package com.example.appiot.screens

import android.content.Context
import android.os.Debug
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appiot.service.ble.BleConnectionManager
import com.example.appiot.service.ble.BleConnectionState
import com.example.appiot.ui.theme.AppIOTTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.json.JSONObject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds


@Composable
fun RealtimeScreen(bleManager: BleConnectionManager, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var intervaloActualizacion by remember { mutableStateOf(5) }
    var temperaturaMinima by remember { mutableStateOf(2) }

    // 1. Estados para almacenar los datos recibidos del ESP32
    var temperatura by remember { mutableStateOf("--") }
    var humedad by remember { mutableStateOf("--") }
    var emisor by remember { mutableStateOf("--") }
    var ultimaActualizacion by remember { mutableStateOf("Esperando datos...") }

    val device by bleManager.connectedDevice.collectAsState()
    // 2. Observar el estado de la conexión y el dispositivo conectado
    val connectionState by bleManager.connectionState.collectAsStateWithLifecycle(
        BleConnectionState.Disconnected
    )
    // 3. Recolectar las notificaciones BLE emitidas por el ESP32
    LaunchedEffect(bleManager) {
        bleManager.receivedData.collect { rawJson ->
            try {
                // Parsear el JSON recibido (ejemplo: {"temperatura": 24.5, "humedad": 60})
                val json = JSONObject(rawJson)
                Log.d("Json",rawJson)
                if (json.has("temperatura")) {
                    temperatura = "${json.getDouble("temperatura")}"
                }
                if (json.has("humedad")) {
                    humedad = "${json.getDouble("humedad")}"
                }
                if (json.has("emisor")) {
                    emisor = json.getString("emisor")
                }
                ultimaActualizacion = "Última actualización: hace 5 segundos"
            } catch (e: Exception) {
                // Si el ESP32 no envía JSON sino texto plano
                Log.d("Error", e.toString())
                Log.d("JSONError", rawJson)
                ultimaActualizacion = "Dato bruto: $rawJson"
            }
        }
    }
    LaunchedEffect(device) {
        while(device==null)
        {
            val prefs = context.getSharedPreferences("ble_prefs", Context.MODE_PRIVATE)
            val mac = prefs.getString("LAST_MAC_ADDRESS", null)
            if (mac != null) {
                bleManager.reconectar(mac)
            }
            delay(2500.milliseconds)
        }
    }
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .navigationBarsPadding()
            .fillMaxHeight()
            .fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Vista en tiempo real",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start,
                fontSize = 20.sp,

                )
        }
        Row() {
            Text(
                text = if (device == null) "Dispositivo desconectado" else "Conectado",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start,
                fontSize = 16.sp,
                modifier = Modifier.padding(8.dp)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
        ) {
            Text(
                text = if (device == null) "Dispositivo desconectado" else "Temperatura actual: ${temperatura}°C",
                modifier
                    .background(Color.Red)
                    .padding(8.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
        ) {
            Text(
                text = if (device == null) "Dispositivo desconectado" else "Humedad actual: ${humedad}°C",
                modifier
                    .background(Color.DarkGray)
                    .padding(8.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Column(
            modifier
                .padding(8.dp)
                .width(300.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Configuración",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                value = if (temperaturaMinima == 0) "" else temperaturaMinima.toString(),
                onValueChange = { nuevoTexto: String ->
                    // Filtrar para permitir solo números y hacer conversión segura
                    val textoLimpio = nuevoTexto.filter { it.isDigit() }
                    temperaturaMinima = textoLimpio.toIntOrNull() ?: 0
                },
                label = { Text("Temperatura mínima en °C") },
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number, imeAction = ImeAction.Next
                ),
                singleLine = true,
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth()
            )
            OutlinedTextField(
                value = if (intervaloActualizacion == 0) "" else intervaloActualizacion.toString(),
                onValueChange = { nuevoTexto: String ->
                    // Filtrar para permitir solo números y hacer conversión segura
                    val textoLimpio = nuevoTexto.filter { it.isDigit() }
                    intervaloActualizacion = textoLimpio.toIntOrNull() ?: 0
                },
                label = { Text("Intervalo de actualización (segundos)") },
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth()
            )
            Button(
                onClick = {
                    scope.launch {
                        bleManager.cambiarTiempoRefresco(intervaloActualizacion * 1000)
                        delay(200.milliseconds) // Pausa de 200 ms para dar tiempo al bus BLE
                        bleManager.temperaturaMinima(temperaturaMinima)
                    }
                    Toast.makeText(
                        context,
                        "Configuración guardada correctamente",
                        Toast.LENGTH_SHORT
                    ).show()
                }, modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth()
            ) { Text(text = "Guardar configuración") }
        }
        Column(
            modifier
                .padding(8.dp)
                .width(300.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Pruebas",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = {
                    bleManager.controlarPin(true)
                    Toast.makeText(
                        context,
                        "Relé encendido",
                        Toast.LENGTH_SHORT
                    ).show()
                }, modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth()
            ) { Text(text = "Encender relé") }
            Button(
                onClick = {
                    bleManager.controlarPin(false)
                    Toast.makeText(
                        context,
                        "Relé apagado",
                        Toast.LENGTH_SHORT
                    ).show()
                }, modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth()
            ) { Text(text = "Apagar relé") }
        }
    }
}

@Composable
fun RealtimeScreenContent(modifier: Modifier = Modifier) {

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .navigationBarsPadding()
            .fillMaxHeight()
            .fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Vista en tiempo real",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start,
                fontSize = 20.sp,

                )
        }
        Row() {
            Text(
                text = "MAC Dispositivo conectado",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start,
                fontSize = 16.sp,
                modifier = Modifier.padding(8.dp)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
        ) {
            Text(
                text = "Temperatura actual: 5°C",
                modifier
                    .background(Color.Red)
                    .padding(8.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
        ) {
            Text(
                text = "Humedad actual: 65%",
                modifier
                    .background(Color.DarkGray)
                    .padding(8.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Column(
            modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Estadisticas",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "A las 03:30 hrs se alcanzaran los 0°C",
                textAlign = TextAlign.Center,
                fontSize = 14.sp
            )
            Text(
                text = "Cantidad de nodos conectados: 2",
                textAlign = TextAlign.Center,
                fontSize = 14.sp
            )
            Text(
                text = "Última actualización hace 5 segundos",
                textAlign = TextAlign.Center,
                fontSize = 14.sp
            )
        }
        Column(
            modifier
                .padding(8.dp)
                .width(300.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Configuración",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                value = "2",
                onValueChange = {},
                label = { Text("Temperatura mínima en °C") },
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number, imeAction = ImeAction.Next
                ),
                singleLine = true,
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth()
            )
            OutlinedTextField(
                value = "5",
                onValueChange = {},
                label = { Text("Intervalo de actualización") },
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number, imeAction = ImeAction.Next
                ),
                singleLine = true,
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth()
            )
            Button(
                onClick = {}, modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth()
            ) { Text(text = "Guardar configuración") }
        }
        Column(
            modifier
                .padding(8.dp)
                .width(300.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Pruebas",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = {}, modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth()
            ) { Text(text = "Encender relé") }
            Button(
                onClick = {}, modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth()
            ) { Text(text = "Apagar relé") }
        }

    }
}


@Preview(showBackground = true)
@Composable
fun RealtimeScreenPreview() {
    AppIOTTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {
                RealtimeScreenContent()
            }
        }
    }
}