package com.example.appiot.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appiot.service.auth.AuthService
import com.example.appiot.ui.theme.AppIOTTheme


@Composable
fun WelcomeScreen(onLogOut: () -> Unit, onConectar: ()-> Unit, modifier: Modifier = Modifier) {
    val authService = remember { AuthService() }
    var correo by rememberSaveable { mutableStateOf("") }
    val email = authService.usuarioActual?.email
    val context = LocalContext.current
    if (!email.isNullOrBlank()) {
        correo = email
    }

    var mostrarDialogo by remember { mutableStateOf(false) }
    var cerrarSesion by remember { mutableStateOf(false) }

    BackHandler {
        mostrarDialogo = true
    }
    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text(text = "Salir de la aplicación") },
            text = { Text(text = "¿Estás seguro de que deseas salir?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarDialogo = false
                        (context as? Activity)?.finish()
                    }
                ) {
                    Text("Salir")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
    if (cerrarSesion) {
        AlertDialog(
            onDismissRequest = { cerrarSesion = false },
            title = { Text(text = "Cerrar sesión") },
            text = { Text(text = "¿Estás seguro de que deseas cerrar sesión?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        cerrarSesion=false
                        authService.cerrarSesion()
                        onLogOut()
                    }
                ) {
                    Text("Cerrar sesión")
                }
            },
            dismissButton = {
                TextButton(onClick = { cerrarSesion = false }) {
                    Text("Cancelar")
                }
            }
        )
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
                text = "Bienvenido/a correo",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start,
                fontSize = 16.sp,
                modifier = Modifier.padding(8.dp)
            )
        }
        Row() {
            Text(
                text = "Dispositivo desconectado",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start,
                fontSize = 16.sp,
                modifier = Modifier.padding(8.dp)
            )
        }
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(200.dp)
                .fillMaxHeight()
        ) {
            Button(onClick = {onConectar()}, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Conectar dispositivo")
            }
            Button(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Ver datos historicos")
            }
            Button(onClick = {
                cerrarSesion= true
            }, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Cerrar sesión")
            }
        }
    }

}

@Composable
fun WelcomeScreenContent(modifier: Modifier = Modifier) {
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
                text = "Bienvenido/a correo",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start,
                fontSize = 16.sp,
                modifier = Modifier.padding(8.dp)
            )
        }
        Row() {
            Text(
                text = "Dispositivo desconectado",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start,
                fontSize = 16.sp,
                modifier = Modifier.padding(8.dp)
            )
        }
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(200.dp)
                .fillMaxHeight()
        ) {
            Button(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Conectar dispositivo")
            }
            Button(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Ver en tiempo real")
            }
            Button(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Ver datos historicos")
            }
            Button(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Cerrar sesión")
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    AppIOTTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {
                WelcomeScreenContent()
            }
        }
    }
}