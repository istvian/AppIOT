package com.example.appiot.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.appiot.R
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

import com.example.appiot.service.auth.AuthService
import com.example.appiot.ui.theme.AppIOTTheme

@Composable
fun LoginScreen(onLogin: () -> Unit, onGoToRegister: () -> Unit, modifier: Modifier = Modifier) {
    var correo by rememberSaveable { mutableStateOf("") }
    var contraseña by rememberSaveable { mutableStateOf("") }
    var mensajeError by rememberSaveable { mutableStateOf<String?>(null) }
    var cargando by rememberSaveable { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val authService = remember { AuthService() }
    val context = LocalContext.current

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
    ) {
        Image(
            painter = painterResource(id = R.drawable.androide),
            contentDescription = "Logo de la App",
            modifier = Modifier
                .size(80.dp)
                .padding(10.dp)
        )
        Text(
            text = "Iniciar sesión",
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start,
            modifier = Modifier
        )
        OutlinedTextField(
            value = correo,
            onValueChange = { nuevoTexto -> correo = nuevoTexto },
            label = { Text("Ingresa tu correo") },
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            modifier = Modifier.padding(4.dp)
        )
        OutlinedTextField(
            value = contraseña,
            onValueChange = { nuevoTexto -> contraseña = nuevoTexto },
            label = { Text("Ingresa tu contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            modifier = Modifier.padding(4.dp)
        )
        Column(
            modifier = Modifier.width(250.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {
                    if (correo.isBlank() || contraseña.isBlank() || cargando) {
                        Toast.makeText(context, "Completa todos los campos", Toast.LENGTH_LONG)
                            .show()
                        return@Button
                    }
                    coroutineScope.launch {
                        cargando = true
                        val resultado = authService.iniciarSesion(correo, contraseña)
                        if (resultado.isSuccess) {
                            Toast.makeText(
                                context,
                                "Sesión iniciada correctamente",
                                Toast.LENGTH_LONG
                            )
                                .show()
                            onLogin()
                            cargando = false
                        } else if (resultado.isFailure) {
                            Toast.makeText(context, "Algo falló", Toast.LENGTH_LONG).show()
                            cargando = false
                        } else {
                            Toast.makeText(context, "Datos incorrectos", Toast.LENGTH_LONG).show()
                            cargando = false
                        }
                    }
                }, modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth()
            ) {
                Text(text = "Iniciar sesión")
            }
            Button(
                onClick = {
                    onGoToRegister()
                }, modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth()
            ) {
                Text(text = "Registrarme")
            }
            Button(
                onClick = {
                    coroutineScope.launch {
                        val resultado = authService.loginWithGoogle(context)
                        if (resultado.isSuccess) {
                            Toast.makeText(
                                context,
                                "Sesión iniciada correctamente",
                                Toast.LENGTH_LONG
                            )
                                .show()
                            onLogin()
                            cargando = false
                        } else if (resultado.isFailure) {
                            Toast.makeText(context, "Algo falló", Toast.LENGTH_LONG).show()
                            cargando = false
                        } else {
                            Toast.makeText(context, "Datos incorrectos", Toast.LENGTH_LONG).show()
                            cargando = false
                        }
                    }
                }, modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth()
            ) {
                Text("Iniciar sesión con Google")
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    AppIOTTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {
                LoginScreen({}, {})
            }
        }
    }
}