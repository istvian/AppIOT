package com.example.appiot.service.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class BleConnectionState {
    object Disconnected : BleConnectionState()
    object Connecting : BleConnectionState()
    data class Connected(val deviceName: String, val address: String) : BleConnectionState()
    data class Error(val message: String) : BleConnectionState()
}

class BleConnectionManager(private val context: Context) {

    private var bluetoothGatt: BluetoothGatt? = null

    var connectionState = MutableStateFlow<BleConnectionState>(BleConnectionState.Disconnected)

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val deviceAddress = gatt.device.address
            val deviceName = gatt.device.name ?: "Dispositivo"

            if (status == BluetoothGatt.GATT_SUCCESS) {
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        Log.d("BLE", "Conectado a $deviceAddress. Descubriendo servicios...")
                        connectionState.value = BleConnectionState.Connected(deviceName, deviceAddress)
                        // Inmediatamente después de conectar, es obligatorio descubrir los servicios
                        gatt.discoverServices()
                    }
                    BluetoothProfile.STATE_DISCONNECTED -> {
                        Log.d("BLE", "Desconectado de $deviceAddress")
                        connectionState.value = BleConnectionState.Disconnected
                        cerrarConexion()
                    }
                }
            } else {
                Log.e("BLE", "Fallo de conexión o error GATT: $status")
                connectionState.value = BleConnectionState.Error("Error al conectar (Código: $status)")
                cerrarConexion()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("BLE", "Servicios descubiertos con éxito:")
                for (service in gatt.services) {
                    Log.d("BLE", "Service UUID: ${service.uuid}")
                    for (characteristic in service.characteristics) {
                        Log.d("BLE", " -> Characteristic: ${characteristic.uuid}")
                    }
                }
            } else {
                Log.e("BLE", "Fallo al descubrir servicios: $status")
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun conectar(device: BluetoothDevice) {
        // Cerrar cualquier conexión previa antes de una nueva
        cerrarConexion()

        connectionState.value = BleConnectionState.Connecting
        Log.d("BLE", "Iniciando conexión con ${device.address}")

        // autoConnect = false asegura conexión directa e inmediata
        // TRANSPORT_LE fuerza el transporte sobre Bluetooth Low Energy
        bluetoothGatt = device.connectGatt(
            context,
            false,
            gattCallback,
            BluetoothDevice.TRANSPORT_LE
        )
    }

    @SuppressLint("MissingPermission")
    fun desconectar() {
        bluetoothGatt?.disconnect()
    }

    @SuppressLint("MissingPermission")
    fun cerrarConexion() {
        bluetoothGatt?.close()
        bluetoothGatt = null
    }
}