# AgroSense: así funciona la IA

Este documento explica cómo la app reconoce enfermedades en fotos: qué datos se usaron, cómo se
entrenaron los modelos, qué tan bien funcionan, cómo se usan dentro de la app y qué hacer para
reentrenarlos. Los detalles de cada entrenamiento están en los notebooks (`notebooks/`). El código
de la app está en `app/src/main/java/com/agrosense/app/ml/`.

## 1. Resumen

| | Cultivos | Ganado |
|---|---|---|
| Qué reconoce | 14 enfermedades + hoja sana en **maíz, papa y tomate** (17 clases) | **Dermatosis nodular contagiosa** (LSD) en bovinos, o "sin signos" (2 clases) |
| Dataset de entrenamiento | PlantVillage (fotos de laboratorio) | *Lumpy Skin Images Dataset* (Mendeley, CC BY 4.0) |
| Arquitectura | MobileNetV2 preentrenada en ImageNet, con transfer learning | La misma |
| Archivo en la app | `agrosense_crop_model.tflite` (2.5 MB, cuantización dinámica) + `labels.txt` | `agrosense_ganado_model.tflite` (4.5 MB, float16) + `labels_ganado.txt` |
| Resultado en laboratorio | Cerca del 100% en el split de prueba de PlantVillage | Accuracy 92.2%, sensibilidad para LSD 86.1% (31 de 36) |
| Resultado en campo | **~25%** en fotos reales de PlantDoc | No medido (no hay un dataset de campo con licencia) |

Todo corre **dentro del teléfono**: la foto no se envía a ningún servidor, el análisis funciona sin
internet y nada sale del dispositivo.

## 2. Recorrido de una foto dentro de la app

```
Foto (cámara o galería)
  │  ImagenStorage: se reduce a 1024 px por lado y se endereza según el EXIF
  ▼
ImageClassifier (uno por módulo, en un hilo de fondo)
  │  1. Se escala a 224×224 y los píxeles se pasan a [-1, 1]: (p − 127.5) / 127.5
  │  2. Intérprete de TensorFlow Lite → una probabilidad por clase (softmax)
  │  3. Se quedan las 3 clases más probables que superen el 1%
  ▼
NombresClases: etiqueta interna en inglés → nombre en español
  ▼
GuiaDiagnostico: nivel de confianza (alta / media / baja) y qué hacer con el resultado
  ▼
Formulario: el usuario revisa el resultado, puede corregirlo y guarda
  ▼
Room (tabla diagnosticos): resultado, confianza, top-3, módulo y ruta de la foto
```

Decisiones de diseño:

- **Sin la Task Library de TFLite.** Esa librería exige que el `.tflite` traiga metadata, y la
  herramienta que la escribe (`tflite-support`) ya no funciona con el Python actual de Colab. La app
  usa el `Interpreter` directamente y lee las etiquetas del `.txt`. A cambio, el preprocesamiento
  vive en `ImageClassifier.prepararEntrada` y **debe coincidir** con el del notebook
  (`mobilenet_v2.preprocess_input`).
- **El contrato es el orden de las etiquetas.** La línea *n* de `labels*.txt` es la salida *n* del
  modelo. Si el número de etiquetas no coincide con el de salidas, el clasificador se desactiva en
  lugar de mostrar nombres equivocados.
- **Los nombres en español van aparte.** Están en `NombresClases.kt`; los `.txt` no se editan a mano.
- **El análisis corre en segundo plano** (`Dispatchers.Default`) y la pantalla muestra
  "Analizando…". `classify` y `close` comparten un candado: cerrar el modelo a mitad de un análisis,
  al cambiar de módulo o salir de la pantalla, provocaría un error nativo que cierra la app.
- **La IA sugiere y el usuario decide.** El análisis llena los campos Resultado y Confianza, pero
  nada se guarda hasta que el usuario toca Guardar, y puede corregir el resultado antes.

## 3. Modelo de cultivos

**Datos.** PlantVillage, en la versión de Kaggle `abdallahalidev/plantvillage-dataset` (38 clases
de 14 especies). Se filtraron las clases de maíz, papa y tomate, que dan 17 clases. Se probó agregar
café con un dataset complementario, pero quedó fuera del modelo final. El split es 80% para
entrenamiento, 10% para validación y 10% para prueba, con semilla 42.

**Entrenamiento** (`AgroSense_modelo_cultivos.ipynb`, Colab con GPU T4):

1. MobileNetV2 congelada. Encima, GlobalAveragePooling, Dropout de 0.2 y una capa Dense softmax.
   Adam con tasa de aprendizaje 1e-3, 10 épocas.
2. Ajuste fino de las últimas ~30 capas de MobileNetV2 con Adam a 1e-5, 10 épocas más.

Aumentación de datos: volteo horizontal, rotación y zoom de ±15% y contraste de ±10%.

**Exportación.** A TFLite con cuantización dinámica (`Optimize.DEFAULT`): pesos en int8, entrada y
salida en float32.

**Resultados.**

- En el split de prueba de PlantVillage acierta cerca del 100%. Son fotos de laboratorio: una hoja
  sola, sobre fondo liso y con buena luz.
- En **PlantDoc**, que son fotos de campo reales (Roboflow, CC BY 4.0), acierta alrededor del
  **25%**. Esta brecha entre laboratorio y campo es la limitación principal del modelo y hay que
  presentarla tal cual.

## 4. Modelo de ganado

**Datos.** *Lumpy Skin Images Dataset* de Kumar & Shastri (2022), publicado en Mendeley Data con
licencia CC BY 4.0 (https://doi.org/10.17632/w36hpf86j2.1). Trae 324 fotos con LSD y 700 normales.

**Limpieza** (sección 3 del notebook). Hizo falta porque el dataset trae ruido que inflaba el
resultado:

- Se quitaron **144 casi-duplicados**. Se buscaron como vecinos cercanos según MobileNetV2 y se
  confirmaron con ORB + RANSAC. Algunos estaban en las dos clases a la vez, y otros repartidos entre
  entrenamiento y prueba.
- Se excluyeron **19 imágenes a mano**: dibujos, collages e infografías. La lista queda en
  `ganado/exclusiones.json`.
- Split estratificado de 70/15/15 sobre 853 imágenes (242 con LSD y 611 sanas). Se guarda en
  `ganado/split_manifiesto.json` para que el resultado se pueda reproducir.

**Entrenamiento.** Igual que el de cultivos, con tres cambios:

- Dropout de 0.3.
- 15 + 15 épocas.
- `class_weight` para compensar que hay casi el triple de fotos sanas.

**Exportación.** En float16. La cuantización int8 cambiaba decisiones: la clase ganadora coincidía
con Keras en 125 de 128 fotos de prueba. Con float16 coincide en 127 de 128, y la sección 7 del
notebook lo verifica antes de exportar.

**Resultados** (128 fotos de prueba que no se usaron para entrenar ni para elegir parámetros):

| Métrica | Valor |
|---|---|
| Accuracy | 92.2% |
| Sensibilidad para LSD (vacas enfermas detectadas) | 86.1% (31 de 36) |
| Falsas alarmas (sanas marcadas como LSD) | 5.4% (5 de 92) |

Cómo leer estos números:

- **Entre un entrenamiento y otro hay unos 8 puntos de variación.** Con el mismo split, otro
  entrenamiento dio 77.8% de sensibilidad. El rango honesto es **78–86%**.
- La primera corrida, antes de la limpieza, dio 87.5%, pero estaba inflada por duplicados que
  aparecían en entrenamiento y en prueba a la vez.
- **Umbral.** Se analizó bajarlo (sección 6.2), pero 0.45 no detectó más casos en prueba y sumó una
  falsa alarma. La app usa la clase más probable, que con dos clases equivale a un umbral de 0.50.
- **Grad-CAM** (sección 6.1). El modelo mira al animal, no al fondo. Sus errores típicos son
  confundir el pelaje manchado Holstein con nódulos y los casos cercanos al 50%.

## 5. Cómo se le presenta el resultado al usuario

La pantalla de diagnóstico, con sus componentes en `ui/components/GuiaDiagnosticoIA.kt` y los
textos en `ml/GuiaDiagnostico.kt`, muestra:

1. **Antes de la foto:** pasos para tomar una foto parecida a las de entrenamiento (hoja sola sobre
   fondo liso; animal de cerca) y qué reconoce cada modelo.
2. **Un aviso fijo:** la IA da una sugerencia, no un diagnóstico seguro, y si hay dudas se consulta
   a un agrónomo o un veterinario.
3. **Después de analizar:** el resultado con su porcentaje y un nivel de confianza con colores de
   semáforo:
   - **Alta**, 80% o más: "aun así puede equivocarse".
   - **Media**, de 50% a 79%: comparar con las otras opciones.
   - **Baja**, menos de 50%: no confiar en el resultado y tomar otra foto.
4. **Un consejo según el resultado.** Por ejemplo: antes de aplicar un producto, confirmar con un
   agrónomo; ante sospecha de LSD, separar al animal y avisar al veterinario; "sin signos" no
   significa que el animal esté sano.

Los umbrales de 80% y 50% son de presentación, no salen de los notebooks. Sirven para explicar el
resultado, no para decidir por el usuario.

## 6. Limitaciones conocidas

- **Ningún modelo sabe decir "esto no es una hoja" o "esto no es una vaca".** Siempre responde
  alguna de sus clases, a veces con mucha confianza. Con ruido aleatorio, el de ganado da entre 89%
  y 97% de probabilidad de LSD, y una imagen sintética salió como "Maíz: roya común, 78.6%". Por eso
  la pantalla insiste en qué reconoce cada modelo.
- **Cultivos funciona mal en el campo**, con ~25% de aciertos en PlantDoc. Una foto de campo tiene
  varias hojas, fondo de tierra o follaje y luz desigual, y nada de eso aparece en PlantVillage.
- **Ganado se entrenó con un solo dataset**, de una sola región y con pocas fotos. Su rendimiento
  con razas y condiciones de México no está medido.
- **La foto de la cámara llega en baja resolución** (`TakePicturePreview`, una miniatura). Para el
  modelo alcanza, porque trabaja a 224 px, pero se ve pobre en el historial. Para fotos completas
  hay que migrar a CameraX o a `TakePicture` con un archivo.

## 7. Cómo reentrenar y actualizar un modelo

1. Abrir el notebook en Colab con GPU y correrlo completo. Los datos y checkpoints van a
   `MyDrive/AgroSense/`.
2. Revisar las métricas: en cultivos, la sección 7 (PlantDoc); en ganado, las secciones 6, 6.1 y 6.2.
3. Descargar de `export/` el `.tflite` y su `labels*.txt` **juntos**, y copiarlos a
   `app/src/main/assets/` con los nombres que espera `ArchivosModelo`.
4. Si hay clases nuevas, agregar su nombre en español en `NombresClases.kt`. `NombresClasesTest`
   falla mientras falte alguno, y `GuiaDiagnosticoTest` revisa que el número de enfermedades que
   dice la pantalla coincida con las etiquetas.
5. Correr las pruebas unitarias y las instrumentadas (`ImageClassifierTest` carga los modelos
   reales en el dispositivo).

Nunca editar a mano los `labels*.txt`: el orden de sus líneas es el contrato con el modelo.

En el notebook de cultivos, las secciones 8.2 y 8.3 describen el flujo antiguo con metadata y
`tflite-support`. La app ya no lo necesita, así que se pueden saltar.

## 8. Pruebas que cubren la IA

| Prueba | Tipo | Qué verifica |
|---|---|---|
| `ImageClassifierTest` | Instrumentada | Los dos modelos cargan y clasifican en el teléfono; las etiquetas son del módulo correcto; cerrar el modelo mientras analiza en otro hilo no tumba la app |
| `ArchivosModeloTest` | Unitaria | Cada módulo apunta a su `.tflite` y a sus etiquetas, que existen en `assets/` y están en el orden esperado |
| `ClassificationResultTest` | Unitaria | Cuándo un resultado es válido y el formato del porcentaje |
| `NombresClasesTest` | Unitaria | Toda etiqueta de los modelos tiene nombre en español |
| `GuiaDiagnosticoTest` | Unitaria | Umbrales de confianza, consejos de cada caso y el conteo de enfermedades contra las etiquetas |
| `DiagnosticoModuloTest` | Instrumentada | El módulo se guarda en Room con cada diagnóstico |

## 9. Trabajo futuro

- **Fotos de campo para cultivos.** Agregar fotos propias o de PlantDoc al entrenamiento,
  reentrenar y volver a medir con PlantDoc.
- **Una clase "otra cosa"** en cada modelo, o un detector previo, para que la app diga "esto no
  parece una hoja" en vez de inventar una enfermedad.
- **Más datos de ganado**, sobre todo de razas de la región y fotos tomadas con el celular en el
  potrero.
- **CameraX**, para guardar la foto de la cámara en resolución completa.

## 10. Fuentes y licencias

- **PlantVillage:** Hughes & Salathé (2015), *An open access repository of images on plant health
  to enable the development of mobile disease diagnostics*, arXiv:1511.08060. Versión de Kaggle
  `abdallahalidev/plantvillage-dataset`.
- **PlantDoc:** Singh et al. (2020), *PlantDoc: A Dataset for Visual Plant Disease Detection*, CODS-COMAD.
  Copia en Roboflow con licencia CC BY 4.0. Solo se usó para evaluar.
- **Lumpy Skin Images Dataset:** Kumar & Shastri (2022), Mendeley Data, CC BY 4.0,
  https://doi.org/10.17632/w36hpf86j2.1.
- **MobileNetV2:** Sandler et al. (2018), con pesos de ImageNet de Keras Applications.
