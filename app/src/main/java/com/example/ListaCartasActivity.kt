package com.example

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

// Pantalla que muestra la lista de cartas selladas guardadas en la sesión
open class ListaCartasActivity : AppCompatActivity() {

    private lateinit var recyclerViewCartas: RecyclerView
    private lateinit var layoutVacio: LinearLayout
    private lateinit var btnVolverInicio: Button
    private lateinit var btnNuevaDesdeLista: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_cartas)

        recyclerViewCartas = findViewById(R.id.recyclerViewCartas)
        layoutVacio = findViewById(R.id.layoutVacio)
        btnVolverInicio = findViewById(R.id.btnVolverInicio)
        btnNuevaDesdeLista = findViewById(R.id.btnNuevaDesdeLista)

        recyclerViewCartas.layoutManager = LinearLayoutManager(this)

        btnVolverInicio.setOnClickListener {
            finish()
        }

        btnNuevaDesdeLista.setOnClickListener {
            val intent = Intent(this, EscrituraActivity::class.java)
            startActivity(intent)
        }

        cargarCartas()
    }

    override fun onResume() {
        super.onResume()
        // Recargar el listado por si se agregó una carta recientemente
        cargarCartas()
    }

    // Obtiene las cartas de la memoria y actualiza la interfaz
    private fun cargarCartas() {
        val cartas = RepositorioCartas.obtenerCartas()

        if (cartas.isEmpty()) {
            recyclerViewCartas.visibility = View.GONE
            layoutVacio.visibility = View.VISIBLE
        } else {
            recyclerViewCartas.visibility = View.VISIBLE
            layoutVacio.visibility = View.GONE

            val adapter = CartasRecyclerAdapter(cartas) { cartaSeleccionada ->
                // Abrir la pantalla de detalle al seleccionar una carta
                val intent = Intent(this, SelladoActivity::class.java).apply {
                    putExtra(SelladoActivity.EXTRA_CARTA_ID, cartaSeleccionada.id)
                    putExtra(SelladoActivity.EXTRA_TEXTO_CARTA, cartaSeleccionada.texto)
                    putExtra(SelladoActivity.EXTRA_FECHA_SELLADO, "Sellada el " + cartaSeleccionada.fechaSelladoTexto)
                }
                startActivity(intent)
            }
            recyclerViewCartas.adapter = adapter
        }
    }

    // Adaptador para renderizar las cartas en el RecyclerView
    class CartasRecyclerAdapter(
        private val listaCartas: List<Carta>,
        private val onItemClick: (Carta) -> Unit
    ) : RecyclerView.Adapter<CartasRecyclerAdapter.CartaViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartaViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_carta_sellada, parent, false)
            return CartaViewHolder(view)
        }

        override fun onBindViewHolder(holder: CartaViewHolder, position: Int) {
            val carta = listaCartas[position]
            holder.bind(carta, onItemClick)
        }

        override fun getItemCount(): Int = listaCartas.size

        class CartaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvTitulo: TextView = itemView.findViewById(R.id.tvItemTitulo)
            private val tvFecha: TextView = itemView.findViewById(R.id.tvItemFecha)
            private val tvFragmento: TextView = itemView.findViewById(R.id.tvItemFragmento)
            private val tvBadge: TextView = itemView.findViewById(R.id.tvItemBadge)

            fun bind(carta: Carta, onItemClick: (Carta) -> Unit) {
                tvTitulo.text = "Carta #${carta.id}"
                tvFecha.text = carta.fechaSelladoTexto

                // Mostrar una vista previa de las primeras palabras
                val textoCompleto = carta.texto.trim()
                tvFragmento.text = if (textoCompleto.length > 90) {
                    textoCompleto.substring(0, 90) + "..."
                } else {
                    textoCompleto
                }

                // Estado de sellado
                if (carta.selladaEnSegundoPlano) {
                    tvBadge.text = "⏳ Sellada por tiempo agotado"
                } else {
                    tvBadge.text = "🔏 Sellada manualmente"
                }

                itemView.setOnClickListener {
                    onItemClick(carta)
                }
            }
        }
    }
}
