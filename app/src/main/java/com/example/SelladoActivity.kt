package com.example

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

// Pantalla para visualizar el contenido de una carta sellada (modo lectura)
class SelladoActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TEXTO_CARTA = "extra_texto_carta"
        const val EXTRA_FECHA_SELLADO = "extra_fecha_sellado"
        const val EXTRA_CARTA_ID = "extra_carta_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sellado)

        val tvTitulo = findViewById<TextView>(R.id.tvTituloSellado)
        val tvFecha = findViewById<TextView>(R.id.tvFechaSellado)
        val tvContenido = findViewById<TextView>(R.id.tvContenidoCarta)
        val tvInsigniaBg = findViewById<TextView>(R.id.tvInsigniaBackground)
        val btnVolver = findViewById<Button>(R.id.btnVolverMisCartas)

        val textoRecibido = intent.getStringExtra(EXTRA_TEXTO_CARTA)
        val fechaRecibida = intent.getStringExtra(EXTRA_FECHA_SELLADO)
        val cartaId = intent.getIntExtra(EXTRA_CARTA_ID, -1)

        // Cargar datos recibidos desde el Intent o consultar el repositorio si hace falta
        if (!textoRecibido.isNullOrEmpty()) {
            tvContenido.text = textoRecibido
            tvFecha.text = fechaRecibida ?: "Sellada recientemente"
            if (cartaId > 0) {
                tvTitulo.text = "Carta al Futuro #$cartaId"
            }
        } else if (cartaId != -1) {
            val carta = RepositorioCartas.obtenerPorId(cartaId)
            if (carta != null) {
                tvTitulo.text = "Carta al Futuro #${carta.id}"
                tvFecha.text = "Sellada el ${carta.fechaSelladoTexto}"
                tvContenido.text = carta.texto

                if (carta.selladaEnSegundoPlano) {
                    tvInsigniaBg.visibility = View.VISIBLE
                } else {
                    tvInsigniaBg.visibility = View.GONE
                }
            } else {
                tvTitulo.text = "Carta no encontrada"
                tvContenido.text = "No fue posible encontrar la carta en la memoria de la sesión actual."
            }
        } else {
            tvTitulo.text = "Carta al Futuro"
            tvFecha.text = "Sellada recientemente"
            tvContenido.text = "(No se recibió el contenido de la carta)"
        }

        // Regresar a la lista de cartas
        btnVolver.setOnClickListener {
            val intent = Intent(this, ListaCartasActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            finish()
        }
    }
}
