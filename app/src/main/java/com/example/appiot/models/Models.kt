package com.example.appiot.models

import com.google.firebase.Timestamp

data class Medicion(
    var id: String = "",
    var emisor: String = "",
    var macs: String ="",
    var humedad: Number = 0,
    var temperatura: Number = 0,
    var fecha: Timestamp = Timestamp.now()
)

data class User(
    var id: String = "",
    var mail: String = "",
    var rol: String = "user",
)