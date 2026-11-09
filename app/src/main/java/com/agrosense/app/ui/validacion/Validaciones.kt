package com.agrosense.app.ui.validacion

/**
 * Porcentaje de 0 a 100 escrito por el usuario. Acepta coma decimal (teclado en español),
 * espacios y el símbolo %. Devuelve null si no es un número en ese rango.
 */
fun leerPorcentaje(texto: String): Float? =
    texto
        .replace("%", "")
        .replace(',', '.')
        .trim()
        .toFloatOrNull()
        ?.takeIf { it in 0f..100f }

/**
 * Mensaje para el primer dato que falta en el formulario de diagnóstico, o null si está completo.
 * Antes el botón Guardar simplemente no hacía nada y el usuario no sabía por qué.
 */
fun validarDiagnostico(
    hayParcela: Boolean,
    hayFoto: Boolean,
    resultado: String,
    confianza: String
): String? = when {
    !hayParcela -> "Elige la parcela del diagnóstico."
    !hayFoto -> "Toma o elige una foto."
    resultado.isBlank() -> "Escribe el resultado o toca \"Analizar con IA\"."
    leerPorcentaje(confianza) == null -> "La confianza debe ser un número de 0 a 100."
    else -> null
}

/**
 * Formato básico de correo: algo@dominio.ext, sin espacios. No comprueba que exista: la app no
 * envía correos, solo evita cuentas con un correo mal escrito con el que luego no se pueda entrar.
 */
fun correoValido(correo: String): Boolean = FORMATO_CORREO.matches(correo.trim())

private val FORMATO_CORREO = Regex("""^[^\s@]+@[^\s@]+\.[^\s@.]{2,}$""")
