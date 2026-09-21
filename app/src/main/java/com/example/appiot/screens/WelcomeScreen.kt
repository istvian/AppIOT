package com.example.appiot.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.expandHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appiot.AppNavegacion
import com.example.appiot.R
import com.example.appiot.service.auth.AuthService
import com.example.appiot.ui.theme.AppIOTTheme

@Composable
fun WelcomeScreen(modifier: Modifier = Modifier) {
    val authService = remember { AuthService() }
    var correo by rememberSaveable { mutableStateOf("") }
    val email = authService.usuarioActual?.email
    if (!email.isNullOrBlank()) {
        correo = email
    }

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
    ) {
        Text(
            text = "Bienvenido/a $correo",
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start,
            fontSize = 16.sp,
            modifier = Modifier
        )
        Button(onClick = {
            authService.cerrarSesion()
        }, modifier = Modifier.padding(4.dp)) {
            Text(text = "Cerrar sesión")
        }
    }
}

@Composable
fun WelcomeScreenContent(modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
    ) {
        Text(
            text = "Bienvenido/a correo",
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start,
            fontSize = 16.sp,
            modifier = Modifier
        )
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