# Notebooks de entrenamiento

Los dos modelos de `app/src/main/assets/` salen de estos notebooks. Están pensados para correr en
Google Colab con GPU (T4) y guardan datos, checkpoints y exportaciones en Google Drive
(`MyDrive/AgroSense/`). Se suben sin salidas; para ver resultados hay que ejecutarlos.

La explicación completa (datos, entrenamiento, resultados, uso en la app y limitaciones) está en
[`docs/pipeline-ia.md`](../docs/pipeline-ia.md).

| Notebook | Genera | Dataset |
|---|---|---|
| `AgroSense_modelo_cultivos.ipynb` | `agrosense_crop_model.tflite` + `labels.txt` (17 clases de maíz, papa y tomate) | PlantVillage (Kaggle `abdallahalidev/plantvillage-dataset`); validación de campo con PlantDoc (Roboflow, CC BY 4.0) |
| `AgroSense_modelo_ganado.ipynb` | `agrosense_ganado_model.tflite` + `labels_ganado.txt` (LSD / sin signos) | *Lumpy Skin Images Dataset*, Kumar & Shastri (2022), Mendeley Data, CC BY 4.0, https://doi.org/10.17632/w36hpf86j2.1 |

Ambos usan transfer learning con MobileNetV2 (entrada 224×224, píxeles a `[-1, 1]`), que es el
preprocesamiento que aplica `ImageClassifier.kt`.

## Resultados de referencia

- **Cultivos:** casi 100% en el split de prueba de PlantVillage (fotos de laboratorio), **~25%** en
  fotos de campo de PlantDoc. La brecha laboratorio → campo es la limitación principal del modelo.
- **Ganado:** sensibilidad para LSD entre **78% y 86%** según el entrenamiento, accuracy ~91–92%,
  sobre 128 fotos de prueba, después de quitar 144 casi-duplicados y 19 imágenes que no eran fotos
  reales. El Grad-CAM (sección 6.1) muestra que mira al animal; confunde a veces el pelaje Holstein
  con nódulos. Exportado en float16 porque la cuantización int8 cambiaba decisiones.

## Reglas al reentrenar

- **Nunca editar a mano los `labels*.txt`.** El orden de las líneas es el índice de salida del
  modelo; los genera el notebook. Los nombres en español viven aparte, en
  `app/src/main/java/com/agrosense/app/ml/NombresClases.kt`, y `NombresClasesTest` falla si una
  clase nueva no tiene traducción.
- Copiar el `.tflite` y su `labels` **juntos** a `assets/`, con los nombres que espera
  `ArchivosModelo` (no renombrar uno con el nombre del otro módulo).
- El notebook de ganado guarda en Drive el split (`ganado/split_manifiesto.json`) y las exclusiones
  manuales (`ganado/exclusiones.json`); con ellos el resultado es reproducible.

## Pendiente

Las secciones 8.2 y 8.3 del notebook de cultivos todavía describen el flujo antiguo con
`tflite-support` y metadata incrustada. La app ya no lo necesita (usa el `Interpreter` y lee el
`labels.txt`), así que esas dos secciones se pueden saltar.
