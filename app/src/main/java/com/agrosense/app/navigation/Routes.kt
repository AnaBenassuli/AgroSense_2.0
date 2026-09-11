package com.agrosense.app.navigation

/**
 * Destinos de navegación de la app.
 *
 * Las rutas con parámetro declaran la plantilla en [route] y construyen la
 * ruta concreta con create(...), para que ninguna pantalla arme cadenas
 * "captura/3" a mano.
 */
sealed class Route(val route: String) {

    data object Login : Route("login")

    data object Home : Route("home")

    data object CapturaDiagnostico : Route("captura/{$ARG_PARCELA_ID}") {
        fun create(parcelaId: Long) = "captura/$parcelaId"
    }

    data object ResultadoDiagnostico : Route("resultado/{$ARG_DIAGNOSTICO_ID}") {
        fun create(diagnosticoId: Long) = "resultado/$diagnosticoId"
    }

    data object HistorialDiagnosticos : Route("historial/{$ARG_PARCELA_ID}") {
        fun create(parcelaId: Long) = "historial/$parcelaId"
    }

    data object RegistroActividades : Route("actividades/{$ARG_PARCELA_ID}") {
        fun create(parcelaId: Long) = "actividades/$parcelaId"
    }

    data object Perfil : Route("perfil")

    companion object {
        const val ARG_PARCELA_ID = "parcelaId"
        const val ARG_DIAGNOSTICO_ID = "diagnosticoId"
    }
}
