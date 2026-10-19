package com.agrosense.app.data.local.entity

/**
 * A qué módulo pertenece un diagnóstico. Se guarda en Room por su nombre, así que renombrar una
 * constante requiere una migración.
 */
enum class Modulo {
    CULTIVO,
    GANADO
}
