package com.example.appiot.screens

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appiot.service.ble.BleConnectionManager
import com.example.appiot.service.ble.BleConnectionState
import com.example.appiot.service.ble.BleScannerManager
import com.example.appiot.ui.theme.AppIOTTheme


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