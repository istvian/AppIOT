package com.example.appiot.service.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Build
import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

sealed class BleConnectionState {
    object Disconnected : BleConnectionState()
    object Connecting : BleConnectionState()
    data class Connected(val deviceName: String, val address: String) : BleConnectionState()
    data class Error(val message: String) : BleConnectionState()
}

class BleConnectionManager(private val context: Context) {

    // UUIDs configurados en el ESP32
    private val SERVICE_UUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b")
    private val CHARACTERISTIC_UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8")
    // UUID estándar de Bluetooth para habilitar notificaciones (CCCD)
    private val CCCD_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    var bluetoothGatt: BluetoothGatt? = null
    var connectionState = MutableStateFlow<BleConnectionState>(BleConnectionState.Disconnected)
    val connectedDevice = MutableStateFlow<BluetoothDevice?>(null)

    // Flow para emitir los datos recibidos (String o ByteArray)

    private val _receivedData = MutableStateFlow<String>("")
    val receivedData: StateFlow<String> = _receivedData.asStateFlow()

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val deviceAddress = gatt.device.address
            val deviceName = gatt.device.name ?: "Dispositivo"

            if (status == BluetoothGatt.GATT_SUCCESS) {
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        Log.d("BLE", "Conectado a $deviceAddress. Descubriendo servicios...")
                        connectedDevice.value = gatt.device
                        connectionState.value = BleConnectionState.Connected(deviceName, deviceAddress)
                        gatt.requestMtu(512)
                    }

                    BluetoothProfile.STATE_DISCONNECTED -> {
                        Log.d("BLE", "Desconectado de $deviceAddress")
                        connectedDevice.value = null
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

        @SuppressLint("MissingPermission")
        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("BLE", "MTU actualizado con éxito a: $mtu bytes")
            } else {
                Log.w("BLE", "Fallo al cambiar MTU, se mantiene el valor por defecto")
            }
            // Iniciar el descubrimiento de servicios después de negociar el MTU
            gatt.discoverServices()
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("BLE", "Servicios descubiertos. Buscando característica objetivo...")

                // Buscar el servicio y la característica del ESP32
                val service = gatt.getService(SERVICE_UUID)
                val characteristic = service?.getCharacteristic(CHARACTERISTIC_UUID)

                if (characteristic != null) {
                    habilitarNotificaciones(gatt, characteristic)
                } else {
                    Log.e("BLE", "No se encontró la característica objetivo")
                }
            } else {
                Log.e("BLE", "Fallo al descubrir servicios: $status")
            }
        }

        // --- RECEPCIÓN DE DATOS (Compatibilidad Android < 13 y >= 13) ---
        @Deprecated("Usado en API < 33")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                val dataBytes = characteristic.value
                procesarDatos(dataBytes)
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            // Android 13 (API 33) o superior entrega directamente el ByteArray
            procesarDatos(value)
        }
    }

    private fun procesarDatos(bytes: ByteArray?) {
        if (bytes != null && bytes.isNotEmpty()) {
            val texto = String(bytes, Charsets.UTF_8)
            Log.d("BLE", "Datos recibidos: $texto")

            // Emitir el valor al SharedFlow (utilizamos tryEmit en callbacks)
            _receivedData.tryEmit(texto)
        }
    }

    @SuppressLint("MissingPermission")
    private fun habilitarNotificaciones(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        // 1. Activar notificaciones en la capa del sistema Android
        gatt.setCharacteristicNotification(characteristic, true)

        // 2. Escribir en el Descriptor 0x2902 para que el ESP32 sepa que debe empezar a notificar
        val descriptor = characteristic.getDescriptor(CCCD_UUID)
        if (descriptor != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
            } else {
                @Suppress("DEPRECATION")
                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                @Suppress("DEPRECATION")
                gatt.writeDescriptor(descriptor)
            }
            Log.d("BLE", "Notificaciones habilitadas correctamente en el descriptor CCCD.")
        } else {
            Log.e("BLE", "No se encontró el descriptor CCCD (0x2902) en la característica.")
        }
    }

    @SuppressLint("MissingPermission")
    fun conectar(device: BluetoothDevice) {
        cerrarConexion()
        connectionState.value = BleConnectionState.Connecting
        Log.d("BLE", "Iniciando conexión con ${device.address}")

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