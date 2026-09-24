package com.example

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

// Pantalla principal de bienvenida y acceso a las funciones de la app
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnEscribir = findViewById<Button>(R.id.btnEscribirCarta)
        val btnMisCartas = findViewById<Button>(R.id.btnMisCartas)

        // Ir a la pantalla para redactar una nueva carta
        btnEscribir.setOnClickListener {
            val intent = Intent(this, EscrituraActivity::class.java)
            startActivity(intent)
        }

        // Ir a la lista de cartas selladas en esta sesión
        btnMisCartas.setOnClickListener {
            val intent = Intent(this, ListaCartasActivity::class.java)
            startActivity(intent)
        }
    }
}
