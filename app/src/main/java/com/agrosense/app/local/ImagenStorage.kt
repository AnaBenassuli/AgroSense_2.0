package com.agrosense.app.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.net.toUri
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Almacenamiento de las fotos de diagnóstico dentro del almacenamiento
 * privado de la app.
 *
 * Las fotos deben vivir aquí y no como URI externa: el permiso de lectura
 * que entrega el selector de galería se pierde al morir el proceso, así que
 * un diagnóstico guardado en Room con esa URI quedaría sin imagen la
 * siguiente vez que se abre la app.
 */
object ImagenStorage {

    private const val CARPETA = "diagnosticos"

    private fun carpeta(context: Context): File =
        File(context.filesDir, CARPETA).apply { if (!exists()) mkdirs() }

    /** Archivo destino, todavía vacío, para una foto nueva. */
    fun nuevoArchivoFoto(context: Context): File {
        val marca = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date())
        return File(carpeta(context), "diag_$marca.jpg")
    }

    /**
     * Copia una imagen elegida en la galería al almacenamiento de la app.
     * Devuelve null si la URI ya no es legible.
     */
    fun copiarDesdeUri(context: Context, uri: Uri): File? = try {
        val destino = nuevoArchivoFoto(context)
        context.contentResolver.openInputStream(uri)?.use { entrada ->
            destino.outputStream().use { salida -> entrada.copyTo(salida) }
        }
        if (destino.length() > 0) destino else null.also { destino.delete() }
    } catch (e: Exception) {
        null
    }

    /** URI file:// utilizable como modelo de imagen y como valor persistible. */
    fun comoUri(archivo: File): String = Uri.fromFile(archivo).toString()

    /**
     * Decodifica una foto guardada, reducida a [ladoMaximo] píxeles de lado.
     *
     * La reducción no es un lujo: una foto de 12 MP son ~48 MB en memoria y
     * el modelo la escala a 224x224 de todas formas.
     */
    fun cargarBitmap(uri: String, ladoMaximo: Int = 1024): Bitmap? {
        val ruta = uri.toUri().path ?: return null
        val archivo = File(ruta)
        if (!archivo.exists()) return null

        val medicion = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(archivo.absolutePath, medicion)

        var muestreo = 1
        while (medicion.outWidth / muestreo > ladoMaximo || medicion.outHeight / muestreo > ladoMaximo) {
            muestreo *= 2
        }

        return BitmapFactory.decodeFile(
            archivo.absolutePath,
            BitmapFactory.Options().apply { inSampleSize = muestreo }
        )
    }
}
