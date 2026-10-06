package com.example.appiot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appiot.screens.ConectarScreen
import com.example.appiot.screens.LoginScreen
import com.example.appiot.screens.RegisterScreen
import com.example.appiot.screens.WelcomeScreen
import com.example.appiot.service.auth.AuthService
import com.example.appiot.ui.theme.AppIOTTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppIOTTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {
                        AppNavegacion()
                    }
                }
            }
        }
    }
}


@Composable
fun AppNavegacion() {
    val navController = rememberNavController()
    val authService = remember { AuthService() }

    fun redirectTo(ruta: String) {
        navController.navigate(ruta) {
            popUpTo(navController.graph.id) {
                inclusive = true
            }
            launchSingleTop = true
        }
    }
    fun navTo(ruta:String){
        navController.navigate((ruta))
    }

    NavHost(
        navController = navController, startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                onLogin = {
                    redirectTo("welcome")
                },
                onGoToRegister = { navTo("register") })
        }
        composable("welcome") {
            WelcomeScreen(onLogOut = {
                redirectTo("login")
            }, onConectar = { navTo("conectar") })
        }
        composable("register") {
            RegisterScreen(onRegister = {
                redirectTo("login")
            })
        }
        composable("conectar") {
            ConectarScreen()
        }
    }
    if (authService.estaAutenticado) {
        redirectTo("welcome")
    }
}

@Preview(showBackground = true)
@Composable
fun AppNavegacionPreview() {
    AppIOTTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {
                AppNavegacion()
            }
        }
    }
}