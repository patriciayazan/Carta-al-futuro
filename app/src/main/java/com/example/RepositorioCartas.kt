package com.example

// Repositorio en memoria para almacenar las cartas durante la sesión actual de la app
object RepositorioCartas {

    private val listaCartas: MutableList<Carta> = mutableListOf()
    private var siguienteId: Int = 1

    // Devuelve todas las cartas registradas
    fun obtenerCartas(): List<Carta> {
        return listaCartas.toList()
    }

    // Genera identificadores autoincrementables
    @Synchronized
    fun generarNuevoId(): Int = siguienteId++

    // Registra una carta existente en la lista
    @Synchronized
    fun agregarCarta(carta: Carta) {
        listaCartas.add(carta)
    }

    // Crea y almacena una nueva carta con ID único
    @Synchronized
    fun guardarCarta(texto: String, fechaSellado: Long, selladaEnSegundoPlano: Boolean = false): Carta {
        val nuevaCarta = Carta(
            id = generarNuevoId(),
            texto = texto,
            fechaSellado = fechaSellado,
            selladaEnSegundoPlano = selladaEnSegundoPlano
        )
        agregarCarta(nuevaCarta)
        return nuevaCarta
    }

    // Busca una carta por su ID
    fun obtenerPorId(id: Int): Carta? {
        return listaCartas.find { it.id == id }
    }

    // Cantidad total de cartas selladas
    fun totalCartas(): Int = listaCartas.size
}
