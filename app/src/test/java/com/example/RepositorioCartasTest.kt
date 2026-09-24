package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RepositorioCartasTest {

    @Test
    fun testGuardarYRecuperarCarta() {
        val totalInicial = RepositorioCartas.totalCartas()
        val carta = RepositorioCartas.guardarCarta(
            texto = "Mi primera carta al futuro",
            fechaSellado = System.currentTimeMillis(),
            selladaEnSegundoPlano = false
        )

        assertNotNull(carta)
        assertEquals(totalInicial + 1, RepositorioCartas.totalCartas())

        val recuperada = RepositorioCartas.obtenerPorId(carta.id)
        assertNotNull(recuperada)
        assertEquals("Mi primera carta al futuro", recuperada?.texto)
        assertTrue(recuperada?.fechaSelladoTexto?.isNotEmpty() == true)
    }
}
