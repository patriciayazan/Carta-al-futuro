package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    context.getSharedPreferences("borrador_carta_prefs", Context.MODE_PRIVATE).edit().clear().commit()
  }

  @After
  fun tearDown() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    context.getSharedPreferences("borrador_carta_prefs", Context.MODE_PRIVATE).edit().clear().commit()
  }

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Carta al Futuro", appName)
  }

  @Test
  fun `main activity inflates buttons successfully`() {
    val scenario = androidx.test.core.app.ActivityScenario.launch(MainActivity::class.java)
    scenario.onActivity { activity ->
      val btnEscribir = activity.findViewById<android.widget.Button>(R.id.btnEscribirCarta)
      val btnMisCartas = activity.findViewById<android.widget.Button>(R.id.btnMisCartas)
      org.junit.Assert.assertNotNull(btnEscribir)
      org.junit.Assert.assertNotNull(btnMisCartas)
    }
  }

  @Test
  fun `escritura activity inflates all key components successfully`() {
    val scenario = androidx.test.core.app.ActivityScenario.launch(EscrituraActivity::class.java)
    scenario.onActivity { activity ->
      val tvTimer = activity.findViewById<android.widget.TextView>(R.id.textViewTimer)
      val etCarta = activity.findViewById<android.widget.EditText>(R.id.editTextCarta)
      val btnPausar = activity.findViewById<android.widget.Button>(R.id.btnPausarReanudar)
      val btnSellar = activity.findViewById<android.widget.Button>(R.id.btnSellarAhora)

      org.junit.Assert.assertNotNull(tvTimer)
      org.junit.Assert.assertNotNull(etCarta)
      org.junit.Assert.assertNotNull(btnPausar)
      org.junit.Assert.assertNotNull(btnSellar)
      org.junit.Assert.assertTrue(
        "Timer text was ${tvTimer.text}",
        tvTimer.text.toString() == "10:00" || tvTimer.text.toString() == "09:59"
      )

      // Probar pulsar pausa
      btnPausar.performClick()
      assertEquals("▶  Reanudar", btnPausar.text.toString())

      // Probar pulsar reanudar
      btnPausar.performClick()
      assertEquals("⏸  Pausar", btnPausar.text.toString())
    }
  }

  @Test
  fun `escritura activity preserves text cursor and timer across recreation`() {
    val scenario = androidx.test.core.app.ActivityScenario.launch(EscrituraActivity::class.java)
    scenario.onActivity { activity ->
      val etCarta = activity.findViewById<android.widget.EditText>(R.id.editTextCarta)
      etCarta.setText("Mensaje para el futuro 2027")
      etCarta.setSelection(7)
    }

    // Simula recreación de la pantalla (ej. rotación de pantalla)
    scenario.recreate()

    scenario.onActivity { activity ->
      val etCarta = activity.findViewById<android.widget.EditText>(R.id.editTextCarta)
      assertEquals("Mensaje para el futuro 2027", etCarta.text.toString())
      assertEquals(7, etCarta.selectionStart)
    }
  }

  @Test
  fun `escritura activity saves draft to SharedPreferences on pause`() {
    val scenario = androidx.test.core.app.ActivityScenario.launch(EscrituraActivity::class.java)
    scenario.onActivity { activity ->
      val etCarta = activity.findViewById<android.widget.EditText>(R.id.editTextCarta)
      etCarta.setText("Borrador de emergencia en disco")
    }

    // Lleva la actividad a onPause
    scenario.moveToState(androidx.lifecycle.Lifecycle.State.STARTED)

    scenario.onActivity { activity ->
      val prefs = activity.getSharedPreferences("borrador_carta_prefs", android.content.Context.MODE_PRIVATE)
      val borradorGuardado = prefs.getString("pref_borrador_texto", null)
      assertEquals("Borrador de emergencia en disco", borradorGuardado)
    }
  }

  @Test
  fun `sellado activity displays text and date received from intent`() {
    val intent = android.content.Intent(androidx.test.core.app.ApplicationProvider.getApplicationContext(), SelladoActivity::class.java).apply {
      putExtra(SelladoActivity.EXTRA_TEXTO_CARTA, "Mi testamento al futuro del 2030")
      putExtra(SelladoActivity.EXTRA_FECHA_SELLADO, "Sellada el 22/09/2026 14:30")
    }
    val scenario = androidx.test.core.app.ActivityScenario.launch<SelladoActivity>(intent)
    scenario.onActivity { activity ->
      val tvContenido = activity.findViewById<android.widget.TextView>(R.id.tvContenidoCarta)
      val tvFecha = activity.findViewById<android.widget.TextView>(R.id.tvFechaSellado)
      val btnVolver = activity.findViewById<android.widget.Button>(R.id.btnVolverMisCartas)

      org.junit.Assert.assertNotNull(tvContenido)
      org.junit.Assert.assertNotNull(tvFecha)
      org.junit.Assert.assertNotNull(btnVolver)
      assertEquals("Mi testamento al futuro del 2030", tvContenido.text.toString())
      assertEquals("Sellada el 22/09/2026 14:30", tvFecha.text.toString())
    }
  }

  @Test
  fun `lista cartas activity inflates recyclerview and binds items`() {
    RepositorioCartas.guardarCarta("Carta de prueba para el RecyclerView", System.currentTimeMillis(), false)
    val scenario = androidx.test.core.app.ActivityScenario.launch(ListaCartasActivity::class.java)
    scenario.onActivity { activity ->
      val rv = activity.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerViewCartas)
      val emptyLayout = activity.findViewById<android.widget.LinearLayout>(R.id.layoutVacio)

      org.junit.Assert.assertNotNull(rv)
      org.junit.Assert.assertNotNull(emptyLayout)
      assertEquals(android.view.View.VISIBLE, rv.visibility)
      assertEquals(android.view.View.GONE, emptyLayout.visibility)
      org.junit.Assert.assertTrue((rv.adapter?.itemCount ?: 0) > 0)
    }
  }
}
