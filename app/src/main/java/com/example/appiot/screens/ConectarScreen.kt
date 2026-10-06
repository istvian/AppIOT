package com.example.appiot.screens

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
fun ConectarScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scannerManager = remember { BleScannerManager(context) }
    val devices by scannerManager.scannedDevices.collectAsStateWithLifecycle()
    val isScanning by scannerManager.scanning.collectAsStateWithLifecycle()
    val connectionStatus by BleConnectionManager(context).connectionState.collectAsStateWithLifecycle()

    // 1. Determinar permisos según la versión de Android
    val requiredPermissions = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    // 2. Launcher para solicitar permisos
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            scannerManager.scanLeDevice()
        } else {
            Toast.makeText(
                context,
                "Se requieren permisos para escanear Bluetooth",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // 3. Detener el escaneo si la pantalla sale de composición
    DisposableEffect(Unit) {
        onDispose {
            scannerManager.scanLeDevice() // Si estaba escaneando, lo apaga
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
        Row() {
            Text(
                text = "Dispositivo desconectado",
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
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if(isScanning)"Buscando dispositivos" else "Buscador de dispositivos",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start,
                fontSize = 16.sp,
                modifier = Modifier.padding(8.dp)
            )
            Button(onClick = { permissionLauncher.launch(requiredPermissions)}) {
                Text(text = if (isScanning) "Buscando..." else "Escanear")
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp, 0.dp),
        ) {
            Text(
                text = "Se encontraron ${devices.size} dispositivos",
                textAlign = TextAlign.Start,
                fontSize = 14.sp,
                modifier = Modifier.padding(8.dp)
            )
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(devices, key = { it.address }) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            BleConnectionManager(context).conectar(item.device)
                            // Conectar con item.device
                        }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = item.name, fontWeight = FontWeight.Bold)
                        Text(text = item.address, style = MaterialTheme.typography.bodySmall)
                    }
                    Text(text = "${item.rssi} dBm")
                }
            }
        }

    }
}

@Composable
fun ConectarScreenContent(modifier: Modifier = Modifier) {
    val nombres = listOf("Audifonos Bluetooth", "ESP32", "PC-Escritorio")

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .navigationBarsPadding()
            .fillMaxHeight()
            .fillMaxWidth()
    ) {
        Row() {
            Text(
                text = "Dispositivo desconectado",
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
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Buscando dispositivos",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start,
                fontSize = 16.sp,
                modifier = Modifier.padding(8.dp)
            )
            Button(onClick = {}) {
                Text(text = "Buscar")
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp, 0.dp),
        ) {
            Text(
                text = "Se encontraron 3 dispositivos",
                textAlign = TextAlign.Start,
                fontSize = 14.sp,
                modifier = Modifier.padding(8.dp)
            )
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
                        }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = nombre, fontWeight = FontWeight.Bold)
                        Text(text = nombre, style = MaterialTheme.typography.bodySmall)
                    }
                    Text(text = "${nombre} dBm", fontSize = 12.sp)
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ConectarScreenPreview() {
    AppIOTTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {
                ConectarScreenContent()
            }
        }
    }
}