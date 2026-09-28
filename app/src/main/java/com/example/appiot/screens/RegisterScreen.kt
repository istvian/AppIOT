package com.example.appiot.screens

import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appiot.service.auth.AuthService
import com.example.appiot.ui.theme.AppIOTTheme
import kotlinx.coroutines.launch


@Composable
fun RegisterScreen(onRegister: () -> Unit, modifier: Modifier = Modifier) {
    var correo by rememberSaveable { mutableStateOf("") }
    var contraseña by rememberSaveable { mutableStateOf("") }
    var contraseña2 by rememberSaveable { mutableStateOf("") }
    var cargando by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val authService = remember { AuthService() }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .navigationBarsPadding()
            .fillMaxHeight()
            .fillMaxWidth()
    ) {
        Text(
            text = "Crea un nuevo usuario",
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start,
            fontSize = 16.sp,
            modifier = Modifier.padding(8.dp)
        )

        OutlinedTextField(
            value = correo,
            onValueChange = { nuevoTexto -> correo = nuevoTexto },
            label = { Text("Ingresa tu correo") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            shape = RoundedCornerShape(16.dp),
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
        OutlinedTextField(
            value = contraseña2,
            onValueChange = { nuevoTexto -> contraseña2 = nuevoTexto },
            label = { Text("Confirma tu contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            modifier = Modifier.padding(4.dp)
        )

        Button(onClick = {
            if (contraseña.isNotBlank() && contraseña == contraseña2 && Patterns.EMAIL_ADDRESS.matcher(
                    correo
                ).matches() && !cargando
            ) {
                coroutineScope.launch {
                    cargando = true
                    val resultado = authService.registrarUsuario(correo, contraseña)
                    if (resultado.isSuccess) {
                        Toast.makeText(context, "Registrado correctamente, inicia sesión", Toast.LENGTH_LONG)
                            .show()
                        onRegister()
                        cargando = false
                    } else if (resultado.isFailure) {
                        Toast.makeText(context, "Algo falló", Toast.LENGTH_LONG).show()
                        cargando = false
                    } else {
                        Toast.makeText(context, "Datos incorrectos", Toast.LENGTH_LONG).show()
                        cargando = false
                    }
                }


            } else {
                Toast.makeText(context, "Datos incorrectos", Toast.LENGTH_LONG).show()
            }
        }) {
            Text(text = "Registrar")
        }

    }
}

@Composable
fun RegisterScreenContent(onRegister: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .navigationBarsPadding()
            .fillMaxHeight()
            .fillMaxWidth()
    ) {
        Text(
            text = "Crea un nuevo usuario",
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start,
            fontSize = 16.sp,
            modifier = Modifier.padding(8.dp)
        )

        OutlinedTextField(
            value = "",
            onValueChange = { },
            label = { Text("Ingresa tu correo") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            modifier = Modifier.padding(4.dp)
        )
        OutlinedTextField(
            value = "",
            onValueChange = { },
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
        OutlinedTextField(
            value = "",
            onValueChange = { },
            label = { Text("Confirma tu contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            modifier = Modifier.padding(4.dp)
        )

        Button(onClick = {}) {
            Text(text = "Registrarme")
        }

    }
}


@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    AppIOTTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {
                RegisterScreenContent({})
            }
        }
    }
}