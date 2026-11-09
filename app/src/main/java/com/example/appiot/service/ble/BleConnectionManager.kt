package com.example.appiot.service.ble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.rememberCoroutineScope
import com.example.appiot.models.Medicion
import com.example.appiot.service.store.FirestoreManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.UUID

sealed class BleConnectionState {
    object Disconnected : BleConnectionState()
    object Connecting : BleConnectionState()
    object AwaitingPin : BleConnectionState() // Esperando que el usuario ingrese el PIN en la UI
    data class Connected(val deviceName: String, val address: String) : BleConnectionState()
    data class Error(val message: String) : BleConnectionState()
}

@SuppressLint("ServiceCast")
class BleConnectionManager(private val context: Context) {

    // UUIDs configurados en el ESP32
    private val SERVICE_UUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b")
    private val CHARACTERISTIC_UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8")

    // UUID estándar de Bluetooth para habilitar notificaciones (CCCD)
    private val CCCD_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val bluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        bluetoothManager?.adapter
    }

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
                        connectionState.value =
                            BleConnectionState.Connected(deviceName, deviceAddress)
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
                connectionState.value =
                    BleConnectionState.Error("Error al conectar (Código: $status)")
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
                val service = gatt.getService(SERVICE_UUID)
                val characteristic = service?.getCharacteristic(CHARACTERISTIC_UUID)

                if (characteristic != null) {
                    // PASO 1: Habilitar notificaciones en CCCD (SIN enviar el PIN todavía)
                    habilitarNotificaciones(gatt, characteristic)
                } else {
                    Log.e("BLE", "No se encontró la característica objetivo")
                }
            } else {
                Log.e("BLE", "Fallo al descubrir servicios: $status")
            }
        }
        @SuppressLint("MissingPermission")
        override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
            super.onDescriptorWrite(gatt, descriptor, status)
            if (status == BluetoothGatt.GATT_SUCCESS && descriptor.uuid == CCCD_UUID) {
                Log.d("BLE", "Descriptor CCCD escrito con éxito. Notificaciones activas en el ESP32.")
                // PASO 3: Ahora que las notificaciones están confirmadas, enviamos el PIN
                validarPin("1234")
            } else {
                Log.e("BLE", "Fallo al escribir descriptor CCCD: $status")
            }
        }

        // --- RECEPCIÓN DE DATOS (Compatibilidad Android < 13 y >= 13) ---
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
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

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            // Android 13 (API 33) o superior entrega directamente el ByteArray
            procesarDatos(value)
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun procesarDatos(bytes: ByteArray?) {
        if (bytes != null && bytes.isNotEmpty()) {
            val texto = String(bytes, Charsets.UTF_8)
            Log.d("BLE", "Datos recibidos: $texto")

            try {
                val json = JSONObject(texto)

                // 1. Validar respuesta del PIN
                if (json.has("auth")) {
                    val autenticado = json.getBoolean("auth")
                    if (autenticado) {
                        Log.d("BLE", "Autenticación exitosa")
                        val dev = connectedDevice.value
                        connectionState.value =
                            BleConnectionState.Connected(dev?.name ?: "ESP32", dev?.address ?: "")
                    } else {
                        Log.e("BLE", "PIN incorrecto")
                        connectionState.value = BleConnectionState.Error("PIN Incorrecto")
                    }
                    return
                }

                // 2. Procesar mediciones normales si ya está autenticado
                CoroutineScope(Dispatchers.IO).launch {
                    val medicion = Medicion()
                    if (json.has("temperatura")) medicion.temperatura =
                        json.getDouble("temperatura")
                    if (json.has("humedad")) medicion.humedad = json.getDouble("humedad")
                    if (json.has("emisor")) medicion.emisor = json.getString("emisor")

                    if (json.has("temperatura") || json.has("humedad")) {
                        FirestoreManager().guardarMedicion(
                            medicion.emisor,
                            medicion.humedad,
                            medicion.temperatura
                        )
                    }
                }

                _receivedData.tryEmit(texto)

            } catch (e: Exception) {
                Log.e("BLE", "Error al parsear JSON: ${e.message}")
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun habilitarNotificaciones(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic
    ) {
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
            true,
            gattCallback,
            BluetoothDevice.TRANSPORT_LE
        )
        //validarPin("1234")
    }


    @SuppressLint("MissingPermission")
    fun reconectar(macAddress: String) {
        // 1. Validar si el formato de la MAC es válido
        if (!BluetoothAdapter.checkBluetoothAddress(macAddress)) {
            Log.e("BLE", "Dirección MAC no válida: $macAddress")
            connectionState.value = BleConnectionState.Error("Dirección MAC no válida")
            return
        }

        // 2. Verificar disponibilidad de Bluetooth
        val adapter = bluetoothAdapter
        if (adapter == null || !adapter.isEnabled) {
            Log.e("BLE", "Bluetooth desactivado o no disponible")
            connectionState.value = BleConnectionState.Error("Bluetooth no disponible")
            return
        }

        try {
            // 3. Obtener la instancia de BluetoothDevice a partir del String de la MAC
            val device: BluetoothDevice = adapter.getRemoteDevice(macAddress)

            // 4. Reutilizar la función conectar existente
            conectar(device)
        } catch (e: IllegalArgumentException) {
            Log.e("BLE", "Error al obtener dispositivo con MAC $macAddress", e)
            connectionState.value = BleConnectionState.Error("Dispositivo no encontrado")
        }
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

    @SuppressLint("MissingPermission")
    fun enviarComando(comando: String) {
        val gatt = bluetoothGatt ?: run {
            Log.e("BLE", "Gatt es nulo, no se puede enviar comando")
            return
        }

        val service = gatt.getService(SERVICE_UUID)
        val characteristic = service?.getCharacteristic(CHARACTERISTIC_UUID)

        if (characteristic != null) {
            val bytes = comando.toByteArray(Charsets.UTF_8)

            // Configurar el tipo de escritura
            characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                gatt.writeCharacteristic(
                    characteristic,
                    bytes,
                    BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                )
            } else {
                @Suppress("DEPRECATION")
                characteristic.value = bytes
                @Suppress("DEPRECATION")
                gatt.writeCharacteristic(characteristic)
            }
            Log.d("BLE", "Comando enviado: $comando")
        } else {
            Log.e("BLE", "Característica no disponible para escritura")
        }
    }

    // Enviar PIN de 4 dígitos para autenticarse
    fun validarPin(pin: String) {
        enviarComando("""{"cmd":"AUTH","pin":"$pin"}""")
    }

    // Cambiar la velocidad de refresco (en milisegundos)
    fun cambiarTiempoRefresco(ms: Int) {
        enviarComando("""{"cmd":"SET_INTERVAL","value":$ms}""")
    }

    // Encender/Apagar un PIN digital (Ej: LED o Relé)
    fun controlarPin(encender: Boolean) {
        val estado = if (encender) 1 else 0
        enviarComando("""{"cmd":"SET_GPIO","state":$estado}""")
    }
    fun temperaturaMinima(ms:Int) {
        enviarComando("""{"cmd":"SET_MIN_TEMP","value":$ms}""")
    }
}