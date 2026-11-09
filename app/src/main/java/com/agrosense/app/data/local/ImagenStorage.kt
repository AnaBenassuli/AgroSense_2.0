package com.agrosense.app.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Fotos de los diagnósticos: cargarlas reducidas, guardarlas y borrarlas.
 *
 * Antes las fotos de la galería se guardaban a resolución completa (4160x3120 en el teléfono de
 * pruebas, ~50 MB por foto al abrirla) y el Historial las abría todas así: con unas siete fotos en
 * una parcela se pasaba del límite de memoria y la app se cerraba. El modelo solo usa 224x224, así
 * que [LADO_MAXIMO] sobra para ver la foto y para analizarla.
 *
 * Todas las funciones leen o escriben disco: llamarlas fuera del hilo principal.
 */
object ImagenStorage {

    const val LADO_MAXIMO = 1024
    const val LADO_MINIATURA = 400
    const val CARPETA = "diagnosticos"
    private const val CALIDAD_JPEG = 85

    /**
     * Mayor potencia de 2 que deja la imagen con su lado mayor todavía >= [ladoMaximo]. Es lo que
     * pide BitmapFactory para decodificar ya reducido, sin cargar nunca la imagen completa.
     */
    fun calcularInSampleSize(ancho: Int, alto: Int, ladoMaximo: Int): Int {
        if (ancho <= 0 || alto <= 0 || ladoMaximo <= 0) return 1
        var muestra = 1
        while (max(ancho, alto) / (muestra * 2) >= ladoMaximo) {
            muestra *= 2
        }
        return muestra
    }

    /** Foto de la galería, reducida y con la rotación de la cámara aplicada. */
    fun cargarDesdeUri(context: Context, uri: Uri, ladoMaximo: Int = LADO_MAXIMO): Bitmap? = try {
        val resolver = context.contentResolver

        val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, limites) }

        val opciones = BitmapFactory.Options().apply {
            inSampleSize = calcularInSampleSize(limites.outWidth, limites.outHeight, ladoMaximo)
        }
        val muestreada = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, opciones)
        }

        val orientacion = resolver.openInputStream(uri)?.use {
            ExifInterface(it).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
        } ?: ExifInterface.ORIENTATION_NORMAL

        muestreada?.let { reducir(rotar(it, orientacion), ladoMaximo) }
    } catch (e: Exception) {
        null
    }

    /** Foto ya guardada por la app (o de versiones anteriores, que podían ser enormes). */
    fun cargarDesdeArchivo(ruta: String, ladoMaximo: Int = LADO_MAXIMO): Bitmap? = try {
        val archivo = File(ruta)
        if (!archivo.exists()) {
            null
        } else {
            val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(ruta, limites)
            val opciones = BitmapFactory.Options().apply {
                inSampleSize = calcularInSampleSize(limites.outWidth, limites.outHeight, ladoMaximo)
            }
            BitmapFactory.decodeFile(ruta, opciones)?.let { reducir(it, ladoMaximo) }
        }
    } catch (e: Exception) {
        null
    }

    /** Ajusta el lado mayor a [ladoMaximo]. Una imagen más chica se devuelve tal cual. */
    fun reducir(bitmap: Bitmap, ladoMaximo: Int = LADO_MAXIMO): Bitmap {
        val lado = max(bitmap.width, bitmap.height)
        if (lado <= ladoMaximo) return bitmap

        val escala = ladoMaximo.toFloat() / lado
        val reducida = Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * escala).roundToInt().coerceAtLeast(1),
            (bitmap.height * escala).roundToInt().coerceAtLeast(1),
            true
        )
        if (reducida !== bitmap) bitmap.recycle()
        return reducida
    }

    /** Guarda la foto en el almacenamiento privado de la app y devuelve su ruta. */
    fun guardar(context: Context, bitmap: Bitmap): String {
        val carpeta = File(context.filesDir, CARPETA).apply { mkdirs() }
        val archivo = File(carpeta, "diagnostico_${System.currentTimeMillis()}.jpg")

        // Lo normal es que ya llegue reducida. Si no, se reduce una copia: reducir() recicla su
        // entrada y el bitmap original sigue en pantalla.
        val aGuardar =
            if (max(bitmap.width, bitmap.height) <= LADO_MAXIMO) {
                bitmap
            } else {
                reducir(bitmap.copy(Bitmap.Config.ARGB_8888, false), LADO_MAXIMO)
            }

        FileOutputStream(archivo).use { salida ->
            aGuardar.compress(Bitmap.CompressFormat.JPEG, CALIDAD_JPEG, salida)
        }
        return archivo.absolutePath
    }

    fun borrar(ruta: String?) {
        if (!ruta.isNullOrBlank()) File(ruta).delete()
    }

    /**
     * Borra las fotos de la carpeta que ningún diagnóstico usa. Limpia las que dejaron versiones
     * anteriores, que guardaban la foto al elegirla aunque luego se cancelara el formulario.
     *
     * @return cuántas se borraron.
     */
    fun borrarHuerfanas(carpeta: File, rutasEnUso: Collection<String>): Int {
        val enUso = rutasEnUso.map { File(it).absolutePath }.toSet()
        return carpeta.listFiles().orEmpty()
            .filter { it.isFile && it.absolutePath !in enUso }
            .count { it.delete() }
    }

    private fun rotar(bitmap: Bitmap, orientacion: Int): Bitmap {
        val grados = when (orientacion) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> return bitmap
        }
        val rotada = Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.width, bitmap.height,
            Matrix().apply { postRotate(grados) },
            true
        )
        if (rotada !== bitmap) bitmap.recycle()
        return rotada
    }
}
