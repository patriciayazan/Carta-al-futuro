package com.example

import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Modelo de datos para las cartas creadas durante la sesión
data class Carta(
    val id: Int,
    val texto: String,
    val fechaSellado: Long,
    val selladaEnSegundoPlano: Boolean = false
) : Serializable {

    // Devuelve la fecha formateada en texto legible (dd/MM/yyyy HH:mm)
    val fechaSelladoTexto: String
        get() {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            return sdf.format(Date(fechaSellado))
        }
}
