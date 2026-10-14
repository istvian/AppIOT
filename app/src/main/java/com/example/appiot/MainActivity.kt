package com.example.appiot

import android.annotation.SuppressLint
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appiot.screens.ConectarScreen
import com.example.appiot.screens.HistoricoScreen
import com.example.appiot.screens.HistoricoScreenContent
import com.example.appiot.screens.LoginScreen
import com.example.appiot.screens.RealtimeScreen
import com.example.appiot.screens.RegisterScreen
import com.example.appiot.screens.WelcomeScreen
import com.example.appiot.service.auth.AuthService
import com.example.appiot.service.ble.BleConnectionManager
import com.example.appiot.service.ble.BleScannerManager
import com.example.appiot.ui.theme.AppIOTTheme

class MainActivity : ComponentActivity() {
    private lateinit var scannerManager: BleScannerManager
    private lateinit var connectionManager: BleConnectionManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scannerManager = BleScannerManager(applicationContext)
        connectionManager = BleConnectionManager(applicationContext)
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
                        AppNavegacion(
                            scannerManager,
                            connectionManager
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        connectionManager.cerrarConexion()
    }
}

@SuppressLint("MissingPermission")
@Composable
fun AppNavegacion(
    scannerManager: BleScannerManager,
    connectionManager: BleConnectionManager
) {
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

    fun navTo(ruta: String) {
        navController.navigate((ruta))
    }

    NavHost(
        navController = navController, startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(onLogin = {
                redirectTo("welcome")
            }, onGoToRegister = { navTo("register") })
        }
        composable("welcome") {
            WelcomeScreen(navigation = { url ->
                navTo(url)
            }, redirect = { url ->
                redirectTo(url)
            }, connectionManager)
        }
        composable("register") {
            RegisterScreen(onRegister = {
                redirectTo("login")
            })
        }
        composable("conectar") {
            ConectarScreen(
                scannerManager = scannerManager,
                connectionManager = connectionManager,
            )
        }
        composable("realtime") {
            RealtimeScreen(connectionManager)
        }
        composable("historico") {
            HistoricoScreen()
        }
    }
    if (authService.estaAutenticado) {
        redirectTo("welcome")
    }
}

