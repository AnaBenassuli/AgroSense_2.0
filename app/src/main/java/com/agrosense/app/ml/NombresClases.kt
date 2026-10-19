package com.agrosense.app.ml

/**
 * Nombres en español de las clases de los modelos, para mostrar en pantalla.
 *
 * Las etiquetas de los .txt de assets/ se quedan en inglés a propósito: son el contrato con el
 * modelo (índice de salida -> etiqueta) y salen tal cual de los notebooks de entrenamiento.
 * Editarlas a mano podría desalinearlas y renombrar mal todos los diagnósticos sin ningún error.
 * Por eso la traducción vive aquí, aparte, y se aplica solo al mostrar.
 *
 * Si se reentrena un modelo con clases nuevas, NombresClasesTest falla hasta que se agreguen aquí.
 */
object NombresClases {

    private val nombres = mapOf(
        // Cultivos (PlantVillage)
        "Corn_(maize)___Cercospora_leaf_spot Gray_leaf_spot" to "Maíz: mancha gris de la hoja (Cercospora)",
        "Corn_(maize)___Common_rust_" to "Maíz: roya común",
        "Corn_(maize)___Northern_Leaf_Blight" to "Maíz: tizón foliar del norte",
        "Corn_(maize)___healthy" to "Maíz: hoja sana",
        "Potato___Early_blight" to "Papa: tizón temprano",
        "Potato___Late_blight" to "Papa: tizón tardío",
        "Potato___healthy" to "Papa: hoja sana",
        "Tomato___Bacterial_spot" to "Tomate: mancha bacteriana",
        "Tomato___Early_blight" to "Tomate: tizón temprano",
        "Tomato___Late_blight" to "Tomate: tizón tardío",
        "Tomato___Leaf_Mold" to "Tomate: moho de la hoja",
        "Tomato___Septoria_leaf_spot" to "Tomate: mancha foliar por Septoria",
        "Tomato___Spider_mites Two-spotted_spider_mite" to "Tomate: araña roja (ácaro de dos manchas)",
        "Tomato___Target_Spot" to "Tomate: mancha diana",
        "Tomato___Tomato_Yellow_Leaf_Curl_Virus" to "Tomate: virus del rizado amarillo (TYLCV)",
        "Tomato___Tomato_mosaic_virus" to "Tomate: virus del mosaico (ToMV)",
        "Tomato___healthy" to "Tomate: hoja sana",
        // Ganado. "Sin signos de…" y no "sano": el modelo solo distingue esta enfermedad
        "Cattle___Lumpy_skin_disease" to "Bovino: dermatosis nodular contagiosa",
        "Cattle___healthy" to "Bovino: sin signos de dermatosis nodular",
    )

    /** Nombre para mostrar. Lo que no sea una etiqueta conocida (p. ej. texto escrito por el usuario) se deja igual. */
    fun legible(clase: String): String = nombres[clase] ?: clase

    fun tieneNombre(clase: String): Boolean = clase in nombres
}
