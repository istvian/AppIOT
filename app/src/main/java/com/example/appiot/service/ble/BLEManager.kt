package com.example.appiot.service.ble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresPermission
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class BleScannerManager(private val context: Context) {

    // 1. Obtener BluetoothAdapter a través de BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        bluetoothManager?.adapter
    }

    private val bluetoothLeScanner: BluetoothLeScanner?
        get() = bluetoothAdapter?.bluetoothLeScanner

    var scanning = MutableStateFlow(false)

    // Se recomienda pasar Looper.getMainLooper() para evitar el warning de deprecación
    private val handler = Handler(Looper.getMainLooper())

    private val SCAN_PERIOD: Long = 10000

    // Mapa para filtrar duplicados por dirección MAC y guardar información útil
    private val discoveredDevicesMap = mutableMapOf<String, BleDiscoveredDevice>()

    // Flujo observable para la interfaz de usuario
    private val _scannedDevices = MutableStateFlow<List<BleDiscoveredDevice>>(emptyList())
    val scannedDevices = _scannedDevices.asStateFlow()

    val leScanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)

            val device: BluetoothDevice = result.device
            val address = device.address // Dirección MAC única
            val name = device.name ?: result.scanRecord?.deviceName ?: "Desconocido"
            val rssi = result.rssi // Intensidad de señal (dBm)

            // Creamos o actualizamos el dispositivo en la lista
            val bleDevice = BleDiscoveredDevice(
                device = device,
                name = name,
                address = address,
                rssi = rssi
            )

            discoveredDevicesMap[address] = bleDevice
            // Actualizamos la lista visible
            _scannedDevices.value = discoveredDevicesMap.values.toList()
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            // Manejar error de escaneo si ocurre
        }
    }

    // Modelo de datos para manejar en UI
    data class BleDiscoveredDevice(
        val device: BluetoothDevice,
        val name: String,
        val address: String,
        val rssi: Int
    )
    // 2. Definir el callback para recibir los resultados


    // 3. Función de escaneo
    @SuppressLint("MissingPermission") // Asegúrate de verificar los permisos en tiempo de ejecución antes
    fun scanLeDevice() {
        val scanner = bluetoothLeScanner
        if (scanner == null) {
            Log.e("BLE", "BluetoothLeScanner no disponible (Bluetooth apagado o no soportado)")
            return
        }

        if (!scanning.value) {
            handler.postDelayed({
                if (scanning.value) {
                    scanning.value = false
                    scanner.stopScan(leScanCallback)
                    Log.d("BLE", "Escaneo detenido por tiempo límite")
                }
            }, SCAN_PERIOD)

            scanning.value = true
            scanner.startScan(leScanCallback)
            Log.d("BLE", "Escaneo iniciado")
        } else {
            scanning.value = false
            scanner.stopScan(leScanCallback)
            Log.d("BLE", "Escaneo detenido manualmente")
        }
    }
}