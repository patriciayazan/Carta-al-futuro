package com.example

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Locale

// Pantalla de redacción de la carta con temporizador de 10 minutos.
// Maneja persistencia con Bundle (para rotaciones) y SharedPreferences (borrador en disco al pausar).
class EscrituraActivity : AppCompatActivity() {

    companion object {
        const val DURACION_TOTAL_MS: Long = 600_000L // 10 minutos
        private const val INTERVALO_TIC_MS: Long = 1_000L // 1 segundo

        // Claves para el Bundle (onSaveInstanceState)
        private const val KEY_TEXTO_CARTA = "key_texto_carta"
        private const val KEY_POSICION_CURSOR = "key_posicion_cursor"
        private const val KEY_TIEMPO_RESTANTE = "key_tiempo_restante"
        private const val KEY_TIMER_ACTIVO = "key_timer_activo"

        // Claves para SharedPreferences
        private const val PREFS_BORRADOR = "borrador_carta_prefs"
        private const val PREF_KEY_BORRADOR_TEXTO = "pref_borrador_texto"
        private const val PREF_KEY_BORRADOR_TIEMPO = "pref_borrador_tiempo"
    }

    private lateinit var editTextCarta: EditText
    private lateinit var textViewTimer: TextView
    private lateinit var btnPausarReanudar: Button
    private lateinit var btnSellarAhora: Button

    private var tiempoRestanteMillis: Long = DURACION_TOTAL_MS
    private var timerActivo: Boolean = true
    private var timer: CountDownTimer? = null
    private var cartaYaSellada: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_escritura)

        // Inicialización de las vistas de la pantalla
        val rootLayout = findViewById<View>(R.id.escrituraRootLayout)
        editTextCarta = findViewById(R.id.editTextCarta)
        textViewTimer = findViewById(R.id.textViewTimer)
        btnPausarReanudar = findViewById(R.id.btnPausarReanudar)
        btnSellarAhora = findViewById(R.id.btnSellarAhora)

        // Ajustar el padding cuando se despliega el teclado para que no tape los botones
        val initialPaddingStart = rootLayout.paddingStart
        val initialPaddingTop = rootLayout.paddingTop
        val initialPaddingEnd = rootLayout.paddingEnd
        val initialPaddingBottom = rootLayout.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { view, insets ->
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val sysBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            val bottomOffset = maxOf(sysBarInsets.bottom, imeInsets.bottom)

            view.setPaddingRelative(
                initialPaddingStart,
                sysBarInsets.top + initialPaddingTop,
                initialPaddingEnd,
                bottomOffset + initialPaddingBottom
            )
            insets
        }

        btnPausarReanudar.setOnClickListener {
            toggleTimer()
        }

        btnSellarAhora.setOnClickListener {
            sellarCarta(selladaEnBackground = false)
        }

        // Si no es un cambio de orientación, revisamos si había un borrador guardado en disco
        if (savedInstanceState == null) {
            cargarBorradorSiExiste()
            actualizarTextoTimer(tiempoRestanteMillis)
            iniciarTimer(tiempoRestanteMillis)
        }
    }

    // Guardar el estado en memoria para recuperar texto, cursor y temporizador si gira la pantalla
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(KEY_TEXTO_CARTA, editTextCarta.text.toString())
        outState.putInt(KEY_POSICION_CURSOR, editTextCarta.selectionStart)
        outState.putLong(KEY_TIEMPO_RESTANTE, tiempoRestanteMillis)
        outState.putBoolean(KEY_TIMER_ACTIVO, timerActivo)

        detenerTimer()
    }

    // Restablecer el texto, posición del cursor y el estado del temporizador tras girar la pantalla
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        val textoGuardado = savedInstanceState.getString(KEY_TEXTO_CARTA, "")
        editTextCarta.setText(textoGuardado)

        val posicionCursor = savedInstanceState.getInt(KEY_POSICION_CURSOR, 0)
        editTextCarta.setSelection(posicionCursor.coerceIn(0, editTextCarta.text.length))

        tiempoRestanteMillis = savedInstanceState.getLong(KEY_TIEMPO_RESTANTE, DURACION_TOTAL_MS)
        actualizarTextoTimer(tiempoRestanteMillis)

        timerActivo = savedInstanceState.getBoolean(KEY_TIMER_ACTIVO, true)

        if (timerActivo && tiempoRestanteMillis > 0L) {
            iniciarTimer(tiempoRestanteMillis)
        } else {
            detenerTimer()
            btnPausarReanudar.text = "▶  Reanudar"
        }
    }

    // Guardar borrador en disco cuando la app pasa a segundo plano o se pierde el foco
    override fun onPause() {
        super.onPause()
        guardarBorradorRapido()
    }

    // Pausar el temporizador para liberar memoria y evitar consumos en segundo plano
    override fun onStop() {
        super.onStop()
        guardarBorradorRapido()
        detenerTimer()
    }

    // Guarda el texto y tiempo restante en SharedPreferences si la carta aún no fue sellada
    private fun guardarBorradorRapido() {
        if (!cartaYaSellada) {
            val prefs = getSharedPreferences(PREFS_BORRADOR, Context.MODE_PRIVATE)
            prefs.edit()
                .putString(PREF_KEY_BORRADOR_TEXTO, editTextCarta.text.toString())
                .putLong(PREF_KEY_BORRADOR_TIEMPO, tiempoRestanteMillis)
                .apply()
        }
    }

    // Reanudar el temporizador si la carta sigue activa al regresar a la pantalla
    override fun onResume() {
        super.onResume()
        if (cartaYaSellada) return

        if (timerActivo && tiempoRestanteMillis > 0L) {
            iniciarTimer(tiempoRestanteMillis)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        detenerTimer()
    }

    // Arranca el temporizador decreciente y actualiza la vista cada segundo
    private fun iniciarTimer(millis: Long) {
        detenerTimer()
        timer = object : CountDownTimer(millis, INTERVALO_TIC_MS) {
            override fun onTick(millisUntilFinished: Long) {
                tiempoRestanteMillis = millisUntilFinished
                actualizarTextoTimer(millisUntilFinished)
            }

            override fun onFinish() {
                tiempoRestanteMillis = 0L
                actualizarTextoTimer(0L)
                sellarCarta(selladaEnBackground = false)
            }
        }.start()
        timerActivo = true
        btnPausarReanudar.text = "⏸  Pausar"
    }

    // Cancela el temporizador si existe para evitar fugas de memoria
    private fun detenerTimer() {
        timer?.cancel()
        timer = null
    }

    // Pausa o reanuda la cuenta regresiva según el estado actual
    private fun toggleTimer() {
        if (timerActivo) {
            detenerTimer()
            timerActivo = false
            btnPausarReanudar.text = "▶  Reanudar"
            Toast.makeText(this, "Cronómetro pausado", Toast.LENGTH_SHORT).show()
        } else {
            iniciarTimer(tiempoRestanteMillis)
            Toast.makeText(this, "Cronómetro reanudado", Toast.LENGTH_SHORT).show()
        }
    }

    // Convierte milisegundos a formato mm:ss para mostrarlo en pantalla
    private fun actualizarTextoTimer(millis: Long) {
        val totalSegundos = (millis / 1000).coerceAtLeast(0)
        val minutos = totalSegundos / 60
        val segundos = totalSegundos % 60
        textViewTimer.text = String.format(Locale.getDefault(), "%02d:%02d", minutos, segundos)
    }

    // Guarda la carta en la lista global de la sesión, limpia el borrador y abre la pantalla de detalle
    private fun sellarCarta(selladaEnBackground: Boolean) {
        if (cartaYaSellada) return
        cartaYaSellada = true

        detenerTimer()

        val texto = editTextCarta.text.toString().trim()
        val contenidoFinal = if (texto.isEmpty()) "(Carta en blanco sellada al futuro)" else texto

        val nuevaCarta = Carta(
            id = RepositorioCartas.generarNuevoId(),
            texto = contenidoFinal,
            fechaSellado = System.currentTimeMillis(),
            selladaEnSegundoPlano = selladaEnBackground
        )

        RepositorioCartas.agregarCarta(nuevaCarta)
        limpiarBorrador()

        Toast.makeText(this, "Carta sellada con éxito 🔏", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, SelladoActivity::class.java).apply {
            putExtra(SelladoActivity.EXTRA_CARTA_ID, nuevaCarta.id)
            putExtra(SelladoActivity.EXTRA_TEXTO_CARTA, nuevaCarta.texto)
            putExtra(SelladoActivity.EXTRA_FECHA_SELLADO, "Sellada el " + nuevaCarta.fechaSelladoTexto)
        }
        startActivity(intent)

        finish()
    }

    // Si existe un borrador guardado en disco, lo recupera en la interfaz
    private fun cargarBorradorSiExiste() {
        val prefs = getSharedPreferences(PREFS_BORRADOR, Context.MODE_PRIVATE)
        val textoGuardado = prefs.getString(PREF_KEY_BORRADOR_TEXTO, null)
        val tiempoGuardado = prefs.getLong(PREF_KEY_BORRADOR_TIEMPO, -1L)

        if (!textoGuardado.isNullOrEmpty() && tiempoGuardado > 0L) {
            editTextCarta.setText(textoGuardado)
            editTextCarta.setSelection(textoGuardado.length.coerceIn(0, editTextCarta.text.length))
            tiempoRestanteMillis = tiempoGuardado
            Toast.makeText(this, "Borrador recuperado desde disco", Toast.LENGTH_SHORT).show()
        }
    }

    // Borra las preferencias del borrador una vez que la carta fue sellada
    private fun limpiarBorrador() {
        val prefs = getSharedPreferences(PREFS_BORRADOR, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}
