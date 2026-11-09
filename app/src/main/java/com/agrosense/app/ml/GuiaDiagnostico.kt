package com.agrosense.app.ml

import com.agrosense.app.data.local.entity.Modulo
import com.agrosense.app.data.local.entity.Prediccion

/** Qué tan segura está la IA de su primera opción. */
enum class NivelConfianza(val titulo: String, val explicacion: String) {
    ALTA(
        "Confianza alta",
        "La IA está bastante segura, pero aun así puede equivocarse."
    ),
    MEDIA(
        "Confianza media",
        "La IA duda. Compara con las otras opciones y revisa tú mismo."
    ),
    BAJA(
        "Confianza baja",
        "No confíes en este resultado. Toma otra foto siguiendo los consejos de arriba."
    );

    companion object {
        /** Umbrales sobre la probabilidad de la primera opción (0 a 1). */
        const val UMBRAL_ALTA = 0.80f
        const val UMBRAL_MEDIA = 0.50f

        fun de(confianza: Float): NivelConfianza = when {
            confianza >= UMBRAL_ALTA -> ALTA
            confianza >= UMBRAL_MEDIA -> MEDIA
            else -> BAJA
        }
    }
}

/** Lo que se le dice al usuario sobre un resultado de la IA. */
data class LecturaResultado(
    val nivel: NivelConfianza,
    val consejo: String
)

/**
 * Textos que acompañan al diagnóstico con IA: cómo tomar la foto, qué reconoce cada modelo y
 * cómo leer el resultado.
 *
 * Están pensados para alguien que no conoce la app ni la IA. Los modelos se entrenaron con fotos
 * de laboratorio (hoja sola sobre fondo liso; animal de cerca) y en fotos de campo aciertan mucho
 * menos (cultivos, ~25% en PlantDoc), así que los pasos piden fotos parecidas a las de
 * entrenamiento y los avisos dejan claro que el resultado es una sugerencia.
 *
 * Si se reentrena un modelo con otras clases, revisar [queReconoce].
 */
object GuiaDiagnostico {

    /** Pasos para tomar una foto que el modelo pueda analizar bien. */
    fun pasos(modulo: Modulo): List<String> = when (modulo) {
        Modulo.CULTIVO -> listOf(
            "Fotografía una sola hoja, de cerca, que ocupe casi toda la foto.",
            "Elige la hoja con más síntomas: manchas, moho, color amarillo u hoja enrollada.",
            "Pon la hoja sobre un fondo liso, como tu mano o una hoja de papel.",
            "Usa luz de día, sin flash y sin sol directo que deje brillos o sombras.",
            "Espera a que la cámara enfoque: la foto no debe salir borrosa."
        )
        Modulo.GANADO -> listOf(
            "Fotografía la piel del costado, el cuello o el lomo, a 1 o 2 metros del animal.",
            "Si ves bultos o ronchas en la piel, ponlos en el centro de la foto.",
            "Que el animal ocupe casi toda la foto, sin otros animales ni cercas delante.",
            "Usa luz de día y espera a que el animal esté quieto para que no salga movida."
        )
    }

    /** Qué sabe reconocer el modelo, para que nadie lo use con lo que no conoce. */
    fun queReconoce(modulo: Modulo): String = when (modulo) {
        Modulo.CULTIVO ->
            "Reconoce 14 enfermedades en hojas de maíz, papa y tomate. Con otros cultivos, " +
                "o con fotos de frutos o tallos, el resultado no sirve."
        Modulo.GANADO ->
            "Solo busca dermatosis nodular contagiosa en bovinos. No detecta otras " +
                "enfermedades, y si la foto no es de una vaca o un toro el resultado no sirve."
    }

    /** Aviso permanente de que el resultado de la IA no es un diagnóstico profesional. */
    fun avisoIA(modulo: Modulo): String =
        "La IA da una sugerencia, no un diagnóstico seguro: puede equivocarse, sobre todo " +
            "con fotos tomadas en el campo. Revisa el resultado antes de guardarlo y, si " +
            "tienes dudas, consulta a ${profesional(modulo)}."

    /** Cómo leer la primera opción de la IA y qué hacer con ella. */
    fun leer(modulo: Modulo, mejor: Prediccion): LecturaResultado =
        LecturaResultado(
            nivel = NivelConfianza.de(mejor.confianza),
            consejo = consejo(modulo, esSano(mejor.clase))
        )

    private fun consejo(modulo: Modulo, sano: Boolean): String = when (modulo) {
        Modulo.CULTIVO ->
            if (sano) {
                "Si ves síntomas en otras plantas de la parcela, analiza también esas hojas."
            } else {
                "Antes de aplicar cualquier producto, confirma el diagnóstico con un agrónomo."
            }
        Modulo.GANADO ->
            if (sano) {
                "Que no salgan signos de dermatosis nodular no quiere decir que el animal esté " +
                    "sano: si lo ves enfermo, consulta a un veterinario."
            } else {
                "Si sospechas dermatosis nodular, separa al animal del resto y avisa de " +
                    "inmediato a un veterinario: es una enfermedad que se reporta a la " +
                    "autoridad sanitaria."
            }
    }

    private fun profesional(modulo: Modulo): String = when (modulo) {
        Modulo.CULTIVO -> "un agrónomo"
        Modulo.GANADO -> "un veterinario"
    }

    /** Las clases sanas de los dos modelos terminan en "___healthy" (ver los labels de assets/). */
    private fun esSano(clase: String): Boolean = clase.endsWith("___healthy")
}
