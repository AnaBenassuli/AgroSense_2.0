# Migración de la UI de AgroSense a AgroSense_2.0 — Plan de Implementación

> **Para trabajadores agénticos:** SUB-SKILL REQUERIDA: usa `superpowers:subagent-driven-development` (recomendada) o `superpowers:executing-plans` para ejecutar este plan tarea por tarea. Los pasos usan checkbox (`- [ ]`) para seguimiento.

**Goal:** Llevar las 7 pantallas, la navegación, la cámara y el clasificador TFLite de la rama `feature/semana4-5-estructura-base` del repo viejo a `AgroSense_2.0`, conectándolas a los ViewModels y repositorios que ya existen ahí, hasta que la app arranque en Login y complete el flujo Home → Captura → Resultado.

**Architecture:** `AgroSense_2.0` ya tiene la mitad de abajo (entidades Room, DAOs, repositorios, ViewModels) y le falta toda la de arriba. La rama vieja tiene la de arriba, pero hablando directo a repositorios propios. La migración conserva **la capa de datos de 2.0 sin reescribirla** y adapta las pantallas: se añade una capa mínima de composición (`AppModule` + una `ViewModelProvider.Factory`) para que Compose pueda instanciar los ViewModels con sus dependencias, y cada pantalla pasa de `remember { Repo(db) }` a `viewModel(factory = ...)`.

**Tech Stack:** Kotlin 2.2.10, AGP 9.3.2, Gradle 9.5.0, Jetpack Compose (BOM 2026.02.01), Room 2.7.2 + KSP, Navigation Compose, CameraX, TensorFlow Lite Task Vision, Coil.

**Spec:** este mismo documento, sección *Contexto y decisiones*. No existe spec previo escrito; el origen es la "Lista de Tareas Prioritarias (CORREGIDA: Room/SQLite)" del 9-sep-2026 más el análisis comparativo de los dos repos hecho el 10-sep-2026.

---

## Contexto y decisiones

**Repo destino (working copy):** `C:\Users\ThinkpadT470\Downloads\AgroSense_2.0` — ya clonado de `AnaBenassuli/AgroSense_2.0`, `main` en `420d68e`.

**Repo origen (solo lectura):** `C:\Users\ThinkpadT470\Downloads\PruebaAgroSense\AgroSense`, rama `feature/semana4-5-estructura-base` (HEAD `490d13f`). Todo se extrae con `git show`, nunca copiando el árbol de trabajo.

**Estado verificado del destino (10-sep-2026):** `./gradlew assembleDebug` → BUILD SUCCESSFUL, APK 12 MB. Room y KSP funcionan. `MainActivity` muestra `"Hello Android!"`; nada más está conectado.

**Decisiones que este plan toma y que no se vuelven a discutir:**

1. **Los ids son `Long`.** 2.0 usa `Long` en todas las entidades; la rama vieja usaba `Int`. Se adapta la UI, no las entidades.
2. **`Prediccion` es una sola clase.** 2.0 ya tiene `data.local.entity.Prediccion(clase, confianza)` con su `TypeConverter`, y `Diagnostico.top3` es `List<Prediccion>`. El `ImageClassifier` migrado deja de declarar su propia `Prediccion` y de serializar a texto: devuelve la entidad directamente. Esto elimina `top3Serializado()` y `deserializarTop3()`.
3. **No se reescriben entidades, DAOs, repositorios ni ViewModels existentes.** Solo se **añaden** los métodos que falten (Tarea 2).
4. **No se toca el esquema de Room** (sigue en `version = 1`). `Parcela` sigue sin `usuarioId` — es una inconsistencia real con el README de 2.0, pero cambiarlo obliga a migración y no es necesario para el flujo de esta entrega. Queda anotado en la Tarea 11.
5. **El acceso local sigue en SharedPreferences** (`LocalAuthManager`), no en la tabla `usuarios`. Es lo que pedía la ficha ("acceso local sencillo") y evita inventar una relación Usuario → Parcela que hoy no existe.
6. **Los mensajes de commit van en inglés, en imperativo**, siguiendo el historial existente de 2.0 (`Add repository layer for local data access`).

**Problema de entorno que hay que resolver antes de compilar:** Avast intercepta TLS en esta máquina (`CN=Avast Web/Mail Shield Root`). La JVM no confía en esa CA, así que Gradle no puede descargar nada y falla con mensajes engañosos del tipo *"Plugin [id: 'x'] was not found in any of the following sources"*. La Tarea 1 lo arregla.

## Global Constraints

- Paquete raíz: `com.agrosense.app`. Ningún archivo migrado puede conservar `com.example.agrosense`.
- Kotlin `2.2.10`, AGP `9.3.2`, Gradle `9.5.0`, `compileSdk 37`, `minSdk 26`, `targetSdk 37`.
- KSP debe quedar en `2.2.10-2.0.2` (hoy dice `2.2.0-2.0.2`, desalineado con Kotlin 2.2.10).
- Room se queda en `2.7.2`. No subir a 2.8.x en esta entrega.
- JDK de compilación: `C:\Program Files\Android\Android Studio\jbr` (JDK 25). El `java` del PATH es 1.8 y no sirve.
- Sin Firebase, sin red, sin sincronización: todo local.
- Todos los ids de entidades y argumentos de navegación son `Long`.
- Cada tarea termina con el build verde y un commit.

**Comando de verificación (se repite en todas las tareas):**

```bash
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew assembleDebug --no-daemon
```

---

### Task 1: Entorno reproducible, rama y dependencias

**Files:**
- Create: `C:\Users\ThinkpadT470\.gradle\cacerts-avast` (truststore, fuera del repo)
- Create: `C:\Users\ThinkpadT470\.gradle\gradle.properties` (fuera del repo)
- Modify: `gradle/libs.versions.toml`
- Modify: `build.gradle.kts`
- Modify: `app/build.gradle.kts`

**Interfaces:**
- Consumes: nada.
- Produces: alias del catálogo de versiones que usan las tareas 4-10: `libs.androidx.navigation.compose`, `libs.androidx.camera.core`, `libs.androidx.camera.camera2`, `libs.androidx.camera.lifecycle`, `libs.androidx.camera.view`, `libs.coil.compose`, `libs.tensorflow.lite.task.vision`, `libs.androidx.lifecycle.viewmodel.compose`, `libs.androidx.lifecycle.runtime.compose`, `libs.androidx.compose.material.icons.core`, `libs.kotlinx.coroutines.android`.

- [ ] **Step 1: Arreglar la confianza TLS de la JVM**

Exporta la raíz de Avast y métela en un truststore propio. No toca el JDK del sistema ni necesita administrador. Desde PowerShell:

```powershell
$c = Get-ChildItem Cert:\LocalMachine\Root, Cert:\CurrentUser\Root | Where-Object { $_.Subject -like '*Avast*' } | Select-Object -First 1
Export-Certificate -Cert $c -FilePath "$env:USERPROFILE\.gradle\avast-root.cer" -Type CERT
Copy-Item "C:\Program Files\Android\Android Studio\jbr\lib\security\cacerts" "$env:USERPROFILE\.gradle\cacerts-avast"
& "C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe" -importcert -noprompt -trustcacerts -alias avast-shield -file "$env:USERPROFILE\.gradle\avast-root.cer" -keystore "$env:USERPROFILE\.gradle\cacerts-avast" -storepass changeit
```

- [ ] **Step 2: Apuntar Gradle a ese truststore**

Crea `C:\Users\ThinkpadT470\.gradle\gradle.properties`. El de `GRADLE_USER_HOME` gana sobre el del proyecto, y al no estar en el repo no afecta al resto del equipo:

```properties
org.gradle.jvmargs=-Xmx2560m -Dfile.encoding=UTF-8 -Djavax.net.ssl.trustStore=C:/Users/ThinkpadT470/.gradle/cacerts-avast -Djavax.net.ssl.trustStorePassword=changeit
```

Alternativa: desactivar el análisis HTTPS de Avast (Protección → Core Shields → Web Shield). Con eso, este paso y el anterior sobran y Android Studio también deja de fallar al sincronizar.

- [ ] **Step 3: Verificar que la red de la JVM funciona antes de seguir**

```bash
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew help --no-daemon --refresh-dependencies
```

Esperado: `BUILD SUCCESSFUL`. Si dice *"was not found in any of the following sources"*, el truststore no quedó bien: no sigas, repite los pasos 1 y 2.

- [ ] **Step 4: Crear la rama de trabajo**

```bash
git checkout -b feature/migracion-ui
```

- [ ] **Step 5: Alinear KSP con Kotlin y llevar todo al catálogo**

En `gradle/libs.versions.toml`, bloque `[versions]`, añade:

```toml
ksp = "2.2.10-2.0.2"
room = "2.7.2"
lifecycle = "2.9.4"
navigationCompose = "2.9.8"
camerax = "1.5.3"
coil = "2.7.0"
tflite = "0.4.4"
coroutines = "1.9.0"
```

En `[libraries]` añade:

```toml
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }
androidx-lifecycle-runtime-compose = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycle" }
androidx-compose-material-icons-core = { group = "androidx.compose.material", name = "material-icons-core" }
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }
androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
androidx-camera-core = { group = "androidx.camera", name = "camera-core", version.ref = "camerax" }
androidx-camera-camera2 = { group = "androidx.camera", name = "camera-camera2", version.ref = "camerax" }
androidx-camera-lifecycle = { group = "androidx.camera", name = "camera-lifecycle", version.ref = "camerax" }
androidx-camera-view = { group = "androidx.camera", name = "camera-view", version.ref = "camerax" }
coil-compose = { group = "io.coil-kt", name = "coil-compose", version.ref = "coil" }
tensorflow-lite-task-vision = { group = "org.tensorflow", name = "tensorflow-lite-task-vision", version.ref = "tflite" }
kotlinx-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }
```

En `[plugins]` añade:

```toml
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
```

- [ ] **Step 6: Usar el alias del plugin en los dos build scripts**

`build.gradle.kts` (raíz) queda así:

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
}
```

En `app/build.gradle.kts`, bloque `plugins`, cambia `id("com.google.devtools.ksp")` por `alias(libs.plugins.ksp)`.

- [ ] **Step 7: Añadir las dependencias de la UI**

En `app/build.gradle.kts`, dentro de `dependencies`, reemplaza las tres líneas literales de Room por sus alias y añade el resto:

```kotlin
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.compose.material.icons.core)

    // Navegación entre pantallas
    implementation(libs.androidx.navigation.compose)

    // Persistencia local Room / SQLite
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.kotlinx.coroutines.android)

    // Cámara
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    // Carga de imágenes en Compose
    implementation(libs.coil.compose)

    // Clasificación de imágenes en dispositivo
    implementation(libs.tensorflow.lite.task.vision)
```

- [ ] **Step 8: Compilar**

```bash
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew assembleDebug --no-daemon
```

Esperado: `BUILD SUCCESSFUL`. El APK crece de ~12 MB a ~40 MB por TFLite y CameraX; es lo normal.

- [ ] **Step 9: Commit**

```bash
git add gradle/libs.versions.toml build.gradle.kts app/build.gradle.kts
git commit -m "Add UI, camera and TFLite dependencies to the version catalog"
```

---

### Task 2: Cerrar los huecos de la capa de datos

La pantalla de Resultado necesita **editar** un diagnóstico (guardar observaciones) y en 2.0 no existe ese camino: `DiagnosticoDao` no tiene `@Update`. Además `SensorViewRepository.kt` contiene la clase `SensorViewModel`.

**Files:**
- Modify: `app/src/main/java/com/agrosense/app/data/local/dao/DiagnosticoDao.kt`
- Modify: `app/src/main/java/com/agrosense/app/data/repository/DiagnosticoRepository.kt`
- Modify: `app/src/main/java/com/agrosense/app/ui/viewmodel/DiagnosticoViewModel.kt`
- Rename: `app/src/main/java/com/agrosense/app/ui/viewmodel/SensorViewRepository.kt` → `SensorViewModel.kt`
- Test: `app/src/test/java/com/agrosense/app/data/local/converter/ConvertersTest.kt`

**Interfaces:**
- Consumes: `Diagnostico`, `Prediccion` (ya existen).
- Produces:
  - `suspend fun DiagnosticoDao.actualizar(diagnostico: Diagnostico)`
  - `suspend fun DiagnosticoRepository.actualizarDiagnostico(diagnostico: Diagnostico)`
  - `fun DiagnosticoViewModel.actualizarDiagnostico(diagnostico: Diagnostico)` (no `suspend`, lanza en `viewModelScope`)

- [ ] **Step 1: Escribir el test que falla**

`app/src/test/java/com/agrosense/app/data/local/converter/ConvertersTest.kt` — el `TypeConverter` del top-3 es la única pieza de lógica pura del proyecto y hoy no tiene ninguna prueba. Va primero porque la Tarea 5 depende de que el ida y vuelta sea fiable:

```kotlin
package com.agrosense.app.data.local.converter

import com.agrosense.app.data.local.entity.Prediccion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun `el top3 sobrevive el ida y vuelta`() {
        val original = listOf(
            Prediccion("Tomato___Bacterial_spot", 0.91f),
            Prediccion("Tomato___Early_blight", 0.06f),
            Prediccion("Potato___healthy", 0.03f)
        )

        val recuperado = converters.stringToTop3(converters.top3ToString(original))

        assertEquals(original, recuperado)
    }

    @Test
    fun `una lista vacia vuelve vacia`() {
        assertTrue(converters.stringToTop3(converters.top3ToString(emptyList())).isEmpty())
    }

    @Test
    fun `un valor nulo o basura no revienta`() {
        assertTrue(converters.stringToTop3(null).isEmpty())
        assertTrue(converters.stringToTop3("sin separadores").isEmpty())
    }

    @Test
    fun `una clase con los separadores dentro no rompe el formato`() {
        val original = listOf(Prediccion("clase;;rara::con separadores", 0.5f))

        val recuperado = converters.stringToTop3(converters.top3ToString(original))

        assertEquals(1, recuperado.size)
        assertEquals(0.5f, recuperado.first().confianza, 0.0001f)
    }
}
```

- [ ] **Step 2: Correr el test**

```bash
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew testDebugUnitTest --no-daemon --tests "com.agrosense.app.data.local.converter.ConvertersTest"
```

Esperado: los cuatro pasan. Si el cuarto falla, el `Converters` existente tiene un bug real de escapado — arréglalo ahí, no en el test.

- [ ] **Step 3: Añadir `actualizar` al DAO**

En `DiagnosticoDao.kt`, junto a `eliminar`, con el import `androidx.room.Update`:

```kotlin
    @Update
    suspend fun actualizar(diagnostico: Diagnostico)
```

- [ ] **Step 4: Exponerlo en el repositorio**

En `DiagnosticoRepository.kt`:

```kotlin
    suspend fun actualizarDiagnostico(diagnostico: Diagnostico) {
        diagnosticoDao.actualizar(diagnostico)
    }
```

- [ ] **Step 5: Exponerlo en el ViewModel**

En `DiagnosticoViewModel.kt`:

```kotlin
    fun actualizarDiagnostico(diagnostico: Diagnostico) {
        viewModelScope.launch {
            repository.actualizarDiagnostico(diagnostico)
        }
    }
```

- [ ] **Step 6: Renombrar el archivo mal nombrado**

```bash
git mv app/src/main/java/com/agrosense/app/ui/viewmodel/SensorViewRepository.kt app/src/main/java/com/agrosense/app/ui/viewmodel/SensorViewModel.kt
```

El contenido no cambia: la clase ya se llama `SensorViewModel`.

- [ ] **Step 7: Compilar y correr los tests**

```bash
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew assembleDebug testDebugUnitTest --no-daemon
```

- [ ] **Step 8: Commit**

```bash
git add -A
git commit -m "Add diagnostico update path and cover the top3 converter with tests"
```

---

### Task 3: Capa de composición (AppModule + ViewModel factory)

Hoy nada instancia los ViewModels: tienen dependencias en el constructor y no hay factory. Sin esto ninguna pantalla puede pedirlos.

**Files:**
- Create: `app/src/main/java/com/agrosense/app/di/AppModule.kt`
- Create: `app/src/main/java/com/agrosense/app/ui/viewmodel/AgroSenseViewModelFactory.kt`

**Interfaces:**
- Consumes: `DatabaseProvider.getDatabase(context)`, los cinco repositorios y los cinco ViewModels.
- Produces:
  - `AppModule.parcelaRepository(context): ParcelaRepository` y los análogos `diagnosticoRepository`, `actividadRepository`, `sensorRepository`, `usuarioRepository`
  - `fun agroSenseViewModelFactory(context: Context): ViewModelProvider.Factory` — lo que todas las pantallas de las tareas 7-9 usan como `viewModel(factory = agroSenseViewModelFactory(LocalContext.current))`

- [ ] **Step 1: Crear AppModule**

`app/src/main/java/com/agrosense/app/di/AppModule.kt`:

```kotlin
package com.agrosense.app.di

import android.content.Context
import com.agrosense.app.data.local.DatabaseProvider
import com.agrosense.app.data.repository.ActividadRepository
import com.agrosense.app.data.repository.DiagnosticoRepository
import com.agrosense.app.data.repository.ParcelaRepository
import com.agrosense.app.data.repository.SensorRepository
import com.agrosense.app.data.repository.UsuarioRepository

/**
 * Construcción de dependencias del prototipo. No es un framework de
 * inyección: es el punto único donde se arma la cadena
 * Context -> Room -> DAO -> Repositorio, para que ninguna pantalla
 * tenga que conocerla.
 */
object AppModule {

    fun parcelaRepository(context: Context) =
        ParcelaRepository(DatabaseProvider.getDatabase(context).parcelaDao())

    fun diagnosticoRepository(context: Context) =
        DiagnosticoRepository(DatabaseProvider.getDatabase(context).diagnosticoDao())

    fun actividadRepository(context: Context) =
        ActividadRepository(DatabaseProvider.getDatabase(context).actividadDao())

    fun sensorRepository(context: Context) =
        SensorRepository(DatabaseProvider.getDatabase(context).sensorDao())

    fun usuarioRepository(context: Context) =
        UsuarioRepository(DatabaseProvider.getDatabase(context).usuarioDao())
}
```

- [ ] **Step 2: Crear la factory**

`app/src/main/java/com/agrosense/app/ui/viewmodel/AgroSenseViewModelFactory.kt`:

```kotlin
package com.agrosense.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.agrosense.app.di.AppModule

/**
 * Factory única para los ViewModels de la app.
 *
 * Se usa el applicationContext a propósito: el ViewModel sobrevive al
 * cambio de configuración y quedarse con el Context de la Activity lo
 * convertiría en una fuga de memoria.
 */
fun agroSenseViewModelFactory(context: Context): ViewModelProvider.Factory {
    val app = context.applicationContext
    return viewModelFactory {
        initializer { ParcelaViewModel(AppModule.parcelaRepository(app)) }
        initializer { DiagnosticoViewModel(AppModule.diagnosticoRepository(app)) }
        initializer { ActividadViewModel(AppModule.actividadRepository(app)) }
        initializer { SensorViewModel(AppModule.sensorRepository(app)) }
        initializer { UsuarioViewModel(AppModule.usuarioRepository(app)) }
    }
}
```

- [ ] **Step 3: Compilar**

```bash
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew assembleDebug --no-daemon
```

Si falla con `Unresolved reference: viewModelFactory`, falta `libs.androidx.lifecycle.viewmodel.compose` de la Tarea 1 Step 7.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/agrosense/app/di app/src/main/java/com/agrosense/app/ui/viewmodel/AgroSenseViewModelFactory.kt
git commit -m "Wire repositories and view models through a single factory"
```

---

### Task 4: Utilidades locales (sesión y almacenamiento de fotos)

**Files:**
- Create: `app/src/main/java/com/agrosense/app/local/LocalAuthManager.kt`
- Create: `app/src/main/java/com/agrosense/app/local/ImagenStorage.kt`

**Interfaces:**
- Consumes: nada del proyecto.
- Produces:
  - `LocalAuthManager(context)` con `iniciarSesion(nombre: String, recordarme: Boolean)`, `getUserName(): String?`, `isLoggedIn(): Boolean`, `getRecordarme(): Boolean`, `logout()`, `limpiarSesionNoRecordada()`
  - `ImagenStorage.nuevoArchivoFoto(context): File`, `ImagenStorage.copiarDesdeUri(context, uri): File?`, `ImagenStorage.comoUri(archivo: File): String`, `ImagenStorage.cargarBitmap(uri: String, ladoMaximo: Int = 1024): Bitmap?`

- [ ] **Step 1: Extraer los dos archivos del repo viejo**

Las variables `SRC` y `B` se reutilizan en las tareas 5-10; expórtalas una vez por sesión de terminal:

```bash
export SRC="C:/Users/ThinkpadT470/Downloads/PruebaAgroSense/AgroSense"
export B=feature/semana4-5-estructura-base
mkdir -p app/src/main/java/com/agrosense/app/local
for f in LocalAuthManager ImagenStorage; do
  git -C "$SRC" show "$B:app/src/main/java/com/example/agrosense/local/$f.kt" \
    > "app/src/main/java/com/agrosense/app/local/$f.kt"
done
```

- [ ] **Step 2: Renombrar el paquete**

```bash
sed -i 's/com\.example\.agrosense/com.agrosense.app/g' app/src/main/java/com/agrosense/app/local/*.kt
```

Ninguno de los dos toca entidades ni repositorios, así que con eso quedan completos.

- [ ] **Step 3: Compilar**

```bash
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew assembleDebug --no-daemon
```

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/agrosense/app/local
git commit -m "Add local session manager and diagnosis image storage"
```

---

### Task 5: Clasificador TFLite unificado con la entidad Prediccion

**Files:**
- Create: `app/src/main/java/com/agrosense/app/ml/ImageClassifier.kt`
- Test: `app/src/test/java/com/agrosense/app/ml/ClassificationResultTest.kt`

**Interfaces:**
- Consumes: `com.agrosense.app.data.local.entity.Prediccion`.
- Produces:
  - `class ImageClassifier(context: Context) : Closeable` con `val disponible: Boolean`, `fun classify(bitmap: Bitmap): ClassificationResult` y `companion object { const val MODELO; const val ETIQUETAS }`
  - `data class ClassificationResult(val label: String, val confidence: Float, val top3: List<Prediccion>)` con `val esValido: Boolean` y las constantes `NO_DISPONIBLE`, `SIN_RESULTADO`, `ERROR`
  - `fun Prediccion.porcentaje(): String` — usado por las pantallas de Resultado e Historial

- [ ] **Step 1: Escribir el test que falla**

`app/src/test/java/com/agrosense/app/ml/ClassificationResultTest.kt`:

```kotlin
package com.agrosense.app.ml

import com.agrosense.app.data.local.entity.Prediccion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClassificationResultTest {

    @Test
    fun `un resultado con predicciones es valido`() {
        val resultado = ClassificationResult(
            label = "Tomato___Early_blight",
            confidence = 0.8f,
            top3 = listOf(Prediccion("Tomato___Early_blight", 0.8f))
        )

        assertTrue(resultado.esValido)
    }

    @Test
    fun `los resultados de fallo no son validos`() {
        assertFalse(ClassificationResult.NO_DISPONIBLE.esValido)
        assertFalse(ClassificationResult.SIN_RESULTADO.esValido)
        assertFalse(ClassificationResult.ERROR.esValido)
    }

    @Test
    fun `el porcentaje se formatea con un decimal`() {
        assertEquals("91.2%", Prediccion("x", 0.9123f).porcentaje())
        assertEquals("0.0%", Prediccion("x", 0f).porcentaje())
    }
}
```

- [ ] **Step 2: Correr el test y verificar que falla**

```bash
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew testDebugUnitTest --no-daemon --tests "com.agrosense.app.ml.ClassificationResultTest"
```

Esperado: FALLA con `Unresolved reference: ClassificationResult`.

- [ ] **Step 3: Extraer el clasificador y renombrar el paquete**

```bash
mkdir -p app/src/main/java/com/agrosense/app/ml
git -C "$SRC" show "$B:app/src/main/java/com/example/agrosense/ml/ImageClassifier.kt" \
  > app/src/main/java/com/agrosense/app/ml/ImageClassifier.kt
sed -i 's/com\.example\.agrosense/com.agrosense.app/g' app/src/main/java/com/agrosense/app/ml/ImageClassifier.kt
```

- [ ] **Step 4: Unificar `Prediccion` con la entidad**

En `ImageClassifier.kt`:

1. Añade el import `com.agrosense.app.data.local.entity.Prediccion`.
2. **Borra** la `data class Prediccion(val etiqueta: String, val confianza: Float) { ... }` local.
3. En `classify`, cambia la construcción de predicciones a la entidad:

```kotlin
            val predicciones = categorias.take(MAX_RESULTADOS).map { categoria ->
                Prediccion(
                    clase = nombreLegible(categoria.label, categoria.index),
                    confianza = categoria.score
                )
            }

            ClassificationResult(
                label = predicciones.first().clase,
                confidence = predicciones.first().confianza,
                top3 = predicciones
            )
```

4. **Borra** de `ClassificationResult` los métodos `top3Serializado()` y `deserializarTop3()` y el helper privado `lineSplitSeguro()`: el `TypeConverter` de Room ya hace ese trabajo, y tener dos formatos de serialización es justo el bug que nadie encuentra después.
5. Al final del archivo, añade la extensión que reemplaza al `porcentaje()` que vivía en la clase borrada:

```kotlin
/** Confianza como texto para mostrar en pantalla, p. ej. "91.2%". */
fun Prediccion.porcentaje(): String = "%.1f%%".format(confianza * 100)
```

- [ ] **Step 5: Correr el test y verificar que pasa**

```bash
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew testDebugUnitTest --no-daemon --tests "com.agrosense.app.ml.ClassificationResultTest"
```

- [ ] **Step 6: Compilar**

```bash
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew assembleDebug --no-daemon
```

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/agrosense/app/ml app/src/test/java/com/agrosense/app/ml
git commit -m "Add on-device TFLite classifier reusing the Prediccion entity"
```

---

### Task 6: Rutas de navegación con ids Long

**Files:**
- Create: `app/src/main/java/com/agrosense/app/navigation/Routes.kt`

**Interfaces:**
- Consumes: nada.
- Produces: `Route.Login`, `Route.Home`, `Route.Perfil` (rutas sin argumento); `Route.CapturaDiagnostico.create(parcelaId: Long)`, `Route.HistorialDiagnosticos.create(parcelaId: Long)`, `Route.RegistroActividades.create(parcelaId: Long)`, `Route.ResultadoDiagnostico.create(diagnosticoId: Long)`; constantes `Route.ARG_PARCELA_ID`, `Route.ARG_DIAGNOSTICO_ID`.

- [ ] **Step 1: Extraer y renombrar el paquete**

```bash
mkdir -p app/src/main/java/com/agrosense/app/navigation
git -C "$SRC" show "$B:app/src/main/java/com/example/agrosense/navigation/Routes.kt" \
  > app/src/main/java/com/agrosense/app/navigation/Routes.kt
sed -i 's/com\.example\.agrosense/com.agrosense.app/g' app/src/main/java/com/agrosense/app/navigation/Routes.kt
```

- [ ] **Step 2: Pasar los argumentos a Long**

```bash
sed -i 's/fun create(parcelaId: Int)/fun create(parcelaId: Long)/; s/fun create(diagnosticoId: Int)/fun create(diagnosticoId: Long)/' \
  app/src/main/java/com/agrosense/app/navigation/Routes.kt
```

Verifica a ojo que quedaron las cuatro funciones `create` con `Long` y que ninguna otra línea menciona `Int`.

- [ ] **Step 3: Compilar**

```bash
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew assembleDebug --no-daemon
```

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/agrosense/app/navigation
git commit -m "Add navigation routes with Long entity ids"
```

---

### Task 7: Pantallas Login, Home y Perfil

**Files:**
- Create: `app/src/main/java/com/agrosense/app/ui/screens/Placeholders.kt`
- Create: `app/src/main/java/com/agrosense/app/ui/screens/LoginScreen.kt`
- Create: `app/src/main/java/com/agrosense/app/ui/screens/HomeScreen.kt`
- Create: `app/src/main/java/com/agrosense/app/ui/screens/PerfilScreen.kt`

**Interfaces:**
- Consumes: `Route`, `LocalAuthManager`, `agroSenseViewModelFactory`, `ParcelaViewModel`.
- Produces: `LoginScreen(navController: NavController)`, `HomeScreen(navController: NavController)`, `PerfilScreen(navController: NavController)`, `PantallaEnConstruccion(titulo: String, detalle: String? = null)`.

- [ ] **Step 1: Extraer las cuatro pantallas**

```bash
mkdir -p app/src/main/java/com/agrosense/app/ui/screens
for f in Placeholders LoginScreen HomeScreen PerfilScreen; do
  git -C "$SRC" show "$B:app/src/main/java/com/example/agrosense/ui/screens/$f.kt" \
    > "app/src/main/java/com/agrosense/app/ui/screens/$f.kt"
done
sed -i 's/com\.example\.agrosense/com.agrosense.app/g' app/src/main/java/com/agrosense/app/ui/screens/*.kt
```

`LoginScreen` y `PerfilScreen` sólo usan `LocalAuthManager`: con el renombrado quedan listas.

- [ ] **Step 2: Arreglar el import de entidad en HomeScreen**

`HomeScreen` importaba `com.agrosense.app.Parcela`, que aquí no existe. Corrige la línea a:

```kotlin
import com.agrosense.app.data.local.entity.Parcela
```

- [ ] **Step 3: Cambiar HomeScreen de repositorio a ViewModel**

Reemplaza:

```kotlin
    val parcelaRepo = remember(context) { ParcelaRepository(AgroSenseDatabase.getInstance(context)) }
```

por:

```kotlin
    val parcelaViewModel: ParcelaViewModel = viewModel(factory = agroSenseViewModelFactory(context))
```

con los imports `androidx.lifecycle.viewmodel.compose.viewModel`, `com.agrosense.app.ui.viewmodel.ParcelaViewModel` y `com.agrosense.app.ui.viewmodel.agroSenseViewModelFactory`. Borra los de `AgroSenseDatabase` y `ParcelaRepository`.

Luego las dos llamadas:

| Antes | Después |
|---|---|
| `parcelaRepo.obtenerTodas().collectAsStateWithLifecycle(emptyList())` | `parcelaViewModel.parcelas.collectAsStateWithLifecycle()` |
| `scope.launch { parcelaRepo.insertar(Parcela(nombre = nombre, ubicacion = ubicacion, cultivo = cultivo)) }` | `parcelaViewModel.registrarParcela(nombre, ubicacion, cultivo)` |

`parcelas` ya es un `StateFlow` con valor inicial, así que `collectAsStateWithLifecycle()` va sin argumento. La segunda deja de necesitar `scope.launch` porque el ViewModel ya lanza en `viewModelScope`; si `scope` queda sin uso, borra su declaración.

- [ ] **Step 4: Verificar los tipos de id**

`Parcela.id` ya es `Long` en 2.0, así que `Route.CapturaDiagnostico.create(parcela.id)` compila tal cual. Comprueba que no quede ninguna variable local declarada `Int` para ids.

- [ ] **Step 5: Compilar**

```bash
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew assembleDebug --no-daemon
```

Los errores típicos aquí son imports sobrantes de `AgroSenseDatabase`/`ParcelaRepository` y `Type mismatch: Int vs Long`.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/agrosense/app/ui/screens
git commit -m "Add login, home and profile screens backed by view models"
```

---

### Task 8: Cámara y captura de diagnóstico

**Files:**
- Create: `app/src/main/java/com/agrosense/app/ui/components/CameraPreview.kt`
- Create: `app/src/main/java/com/agrosense/app/ui/screens/CapturaDiagnosticoScreen.kt`
- Modify: `app/src/main/AndroidManifest.xml`

**Interfaces:**
- Consumes: `ImagenStorage`, `ImageClassifier`, `AppModule.diagnosticoRepository`, `Route`.
- Produces: `CameraPreview(onPhotoCaptured: (String) -> Unit, ...)`, `CapturaDiagnosticoScreen(navController: NavController, parcelaId: Long)`.

- [ ] **Step 1: Declarar el permiso de cámara**

En `app/src/main/AndroidManifest.xml`, arriba de `<application>`:

```xml
    <uses-permission android:name="android.permission.CAMERA" />

    <!-- La cámara es opcional: sin ella la app sigue sirviendo para elegir
         fotos de la galería y consultar el historial. -->
    <uses-feature
        android:name="android.hardware.camera"
        android:required="false" />
```

- [ ] **Step 2: Extraer los dos archivos**

```bash
mkdir -p app/src/main/java/com/agrosense/app/ui/components
git -C "$SRC" show "$B:app/src/main/java/com/example/agrosense/ui/components/CameraPreview.kt" \
  > app/src/main/java/com/agrosense/app/ui/components/CameraPreview.kt
git -C "$SRC" show "$B:app/src/main/java/com/example/agrosense/ui/screens/CapturaDiagnosticoScreen.kt" \
  > app/src/main/java/com/agrosense/app/ui/screens/CapturaDiagnosticoScreen.kt
sed -i 's/com\.example\.agrosense/com.agrosense.app/g' \
  app/src/main/java/com/agrosense/app/ui/components/CameraPreview.kt \
  app/src/main/java/com/agrosense/app/ui/screens/CapturaDiagnosticoScreen.kt
```

`CameraPreview` sólo depende de `ImagenStorage`: queda lista con el renombrado.

- [ ] **Step 3: Adaptar la firma y los imports de la pantalla**

En `CapturaDiagnosticoScreen.kt`:

1. Firma: `fun CapturaDiagnosticoScreen(navController: NavController, parcelaId: Long)`.
2. Borra los imports de `AgroSenseDatabase` y `DiagnosticoRepository`; añade `com.agrosense.app.data.local.entity.Diagnostico` y `com.agrosense.app.di.AppModule`.
3. Borra la línea `val diagnosticoRepo = remember(context) { DiagnosticoRepository(AgroSenseDatabase.getInstance(context)) }`.

- [ ] **Step 4: Guardar el diagnóstico con el top-3 tipado**

El guardado necesita el id que devuelve Room para navegar a Resultado, y `DiagnosticoViewModel.registrarDiagnostico` devuelve `Unit`. Por eso aquí se llama al repositorio dentro del `scope` que la pantalla ya tiene. Reemplaza el bloque de inserción por:

```kotlin
                else -> {
                    val id = AppModule.diagnosticoRepository(context).registrarDiagnostico(
                        Diagnostico(
                            parcelaId = parcelaId,
                            imagenRuta = uri,
                            resultado = resultado.label,
                            confianza = resultado.confidence,
                            top3 = resultado.top3
                        )
                    )
                    navController.navigate(Route.ResultadoDiagnostico.create(id))
                }
```

`top3` ya no se serializa: se pasa la lista de `Prediccion` y el `TypeConverter` se encarga.

- [ ] **Step 5: Compilar**

```bash
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew assembleDebug --no-daemon
```

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/agrosense/app/ui app/src/main/AndroidManifest.xml
git commit -m "Add camera capture screen with on-device diagnosis"
```

---

### Task 9: Resultado, Historial y Actividades

**Files:**
- Create: `app/src/main/java/com/agrosense/app/ui/screens/ResultadoDiagnosticoScreen.kt`
- Create: `app/src/main/java/com/agrosense/app/ui/screens/HistorialDiagnosticosScreen.kt`
- Create: `app/src/main/java/com/agrosense/app/ui/screens/RegistroActividadesScreen.kt`

**Interfaces:**
- Consumes: `DiagnosticoViewModel`, `ActividadViewModel`, `Prediccion.porcentaje()`, `Route`.
- Produces: `ResultadoDiagnosticoScreen(navController, diagnosticoId: Long)`, `HistorialDiagnosticosScreen(navController, parcelaId: Long)`, `RegistroActividadesScreen(navController, parcelaId: Long)`.

- [ ] **Step 1: Extraer las tres pantallas**

```bash
for f in ResultadoDiagnosticoScreen HistorialDiagnosticosScreen RegistroActividadesScreen; do
  git -C "$SRC" show "$B:app/src/main/java/com/example/agrosense/ui/screens/$f.kt" \
    > "app/src/main/java/com/agrosense/app/ui/screens/$f.kt"
  sed -i 's/com\.example\.agrosense/com.agrosense.app/g' "app/src/main/java/com/agrosense/app/ui/screens/$f.kt"
done
```

- [ ] **Step 2: Adaptar ResultadoDiagnosticoScreen**

1. Firma: `fun ResultadoDiagnosticoScreen(navController: NavController, diagnosticoId: Long)`.
2. Imports: fuera `AgroSenseDatabase` y `DiagnosticoRepository`; dentro `com.agrosense.app.data.local.entity.Diagnostico`, `androidx.lifecycle.viewmodel.compose.viewModel`, `com.agrosense.app.ui.viewmodel.DiagnosticoViewModel`, `com.agrosense.app.ui.viewmodel.agroSenseViewModelFactory` y `com.agrosense.app.ml.porcentaje`.
3. `val repo = remember(context) { ... }` → `val diagnosticoViewModel: DiagnosticoViewModel = viewModel(factory = agroSenseViewModelFactory(context))`.
4. Carga: `repo.obtener(diagnosticoId)` → `diagnosticoViewModel.obtenerPorId(diagnosticoId)` (es `suspend`, ya se llama dentro de un `LaunchedEffect`).
5. Guardado de observaciones: `repo.actualizar(actual.copy(observaciones = observaciones.trim()))` → `diagnosticoViewModel.actualizarDiagnostico(actual.copy(observaciones = observaciones.trim()))`. No es `suspend`; si el `scope.launch` que lo envolvía queda vacío, quítalo.
6. Top-3: donde la pantalla llamaba a `ClassificationResult.deserializarTop3(diagnostico.top3)`, ahora `diagnostico.top3` **ya es** `List<Prediccion>` y se usa directo. Cambia `prediccion.etiqueta` por `prediccion.clase` y usa `prediccion.porcentaje()` para el texto de confianza.

- [ ] **Step 3: Adaptar HistorialDiagnosticosScreen**

1. Firma: `parcelaId: Long`.
2. `val repo = remember(context) { ... }` → `val diagnosticoViewModel: DiagnosticoViewModel = viewModel(factory = agroSenseViewModelFactory(context))`.
3. `repo.obtenerPorParcela(parcelaId)` → `diagnosticoViewModel.observarPorParcela(parcelaId)`. Devuelve un `Flow` sin valor inicial, así que la recolección queda `.collectAsStateWithLifecycle(emptyList())`.
4. Si muestra top-3 o etiqueta, aplica los mismos cambios `etiqueta` → `clase` y `porcentaje()` del paso anterior.

- [ ] **Step 4: Adaptar RegistroActividadesScreen**

1. Firma: `parcelaId: Long`.
2. Import de entidad: `com.agrosense.app.data.local.entity.Actividad`; además `com.agrosense.app.ui.viewmodel.ActividadViewModel`.
3. `val repo = remember(context) { ... }` → `val actividadViewModel: ActividadViewModel = viewModel(factory = agroSenseViewModelFactory(context))`.
4. Lectura: `repo.obtenerPorParcela(parcelaId)` → `actividadViewModel.observarPorParcela(parcelaId)` con `.collectAsStateWithLifecycle(emptyList())`.
5. Escritura — ojo con los nombres de campo, que **cambian** respecto al repo viejo (`tipoActividad` → `tipo`, y `descripcion` deja de ser nullable):

```kotlin
                    actividadViewModel.registrarActividad(
                        Actividad(
                            parcelaId = parcelaId,
                            tipo = tipo,
                            descripcion = descripcion.trim(),
                            fecha = fecha
                        )
                    )
```

El `scope.launch` que envolvía la inserción sobra: el ViewModel ya lanza.

- [ ] **Step 5: Compilar**

```bash
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew assembleDebug --no-daemon
```

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/agrosense/app/ui/screens
git commit -m "Add diagnosis result, history and activity log screens"
```

---

### Task 10: Grafo de navegación y arranque de la app

Es la tarea que convierte el proyecto de "compila" a "funciona": hasta aquí ninguna pantalla es alcanzable.

**Files:**
- Create: `app/src/main/java/com/agrosense/app/navigation/MainNavHost.kt`
- Modify: `app/src/main/java/com/agrosense/app/MainActivity.kt`

**Interfaces:**
- Consumes: las siete pantallas de las tareas 7-9, `Route`, `LocalAuthManager`.
- Produces: `MainNavHost(navController: NavHostController, startDestination: String = Route.Login.route)`.

- [ ] **Step 1: Extraer el NavHost**

```bash
git -C "$SRC" show "$B:app/src/main/java/com/example/agrosense/navigation/MainNavHost.kt" \
  > app/src/main/java/com/agrosense/app/navigation/MainNavHost.kt
sed -i 's/com\.example\.agrosense/com.agrosense.app/g' app/src/main/java/com/agrosense/app/navigation/MainNavHost.kt
```

- [ ] **Step 2: Pasar los argumentos de navegación a Long**

```bash
sed -i 's/NavType\.IntType/NavType.LongType/g; s/getInt(/getLong(/g' app/src/main/java/com/agrosense/app/navigation/MainNavHost.kt
```

Y cambia a mano el helper del final del archivo:

```kotlin
private fun androidx.navigation.NavBackStackEntry.parcelaId(): Long =
    arguments?.getLong(Route.ARG_PARCELA_ID) ?: 0L
```

En el destino de Resultado, `?: 0` pasa a `?: 0L`.

- [ ] **Step 3: Reemplazar MainActivity**

`app/src/main/java/com/agrosense/app/MainActivity.kt` completo — se borran el `Greeting` y el `GreetingPreview` de la plantilla:

```kotlin
package com.agrosense.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.agrosense.app.local.LocalAuthManager
import com.agrosense.app.navigation.MainNavHost
import com.agrosense.app.navigation.Route
import com.agrosense.app.ui.theme.AgroSenseTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val authManager = LocalAuthManager(this)
        // Si el usuario no marcó "Recordarme", la sesión no debe sobrevivir al
        // cierre de la app. Se limpia antes de decidir la pantalla inicial.
        authManager.limpiarSesionNoRecordada()
        val destinoInicial =
            if (authManager.isLoggedIn()) Route.Home.route else Route.Login.route

        setContent {
            AgroSenseTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    MainNavHost(
                        navController = navController,
                        startDestination = remember { destinoInicial }
                    )
                }
            }
        }
    }
}
```

Si `AgroSenseTheme` no resuelve, usa el nombre real del composable de `app/src/main/java/com/agrosense/app/ui/theme/Theme.kt`; el tema de 2.0 manda, no se migra el del repo viejo.

- [ ] **Step 4: Compilar, pasar lint y correr los tests**

```bash
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew assembleDebug lintDebug testDebugUnitTest --no-daemon
```

Esperado: `BUILD SUCCESSFUL` y ningún aviso de lint sobre código de `com.agrosense.app`.

- [ ] **Step 5: Probar la app de verdad**

Instala el APK en un teléfono físico. El emulador de esta máquina tarda demasiado y la cámara no funciona bien ahí:

```bash
"/c/Users/ThinkpadT470/AppData/Local/Android/Sdk/platform-tools/adb.exe" install -r app/build/outputs/apk/debug/app-debug.apk
```

Recorrido mínimo a verificar, anotando lo que falle:

1. Arranca en Login. Escribe un nombre, marca "Recordarme", entra.
2. Home muestra la lista vacía. Crea una parcela. Aparece en la lista.
3. Entra a Captura desde esa parcela. Concede el permiso de cámara. Toma una foto.
4. Sin modelo en assets, debe mostrar el aviso de "modelo no disponible" y **no** cerrarse.
5. Historial y Actividades abren desde la parcela y guardan.
6. Cierra la app y vuelve a abrirla: debe entrar directo a Home (sesión recordada).
7. Perfil → cerrar sesión → vuelve a Login.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/agrosense/app/navigation app/src/main/java/com/agrosense/app/MainActivity.kt
git commit -m "Replace template activity with the app navigation graph"
```

---

### Task 11: Documentación y pull request

**Files:**
- Create: `app/src/main/assets/labels.txt`
- Modify: `README.md`

**Interfaces:**
- Consumes: `ImageClassifier.MODELO`, `ImageClassifier.ETIQUETAS`.
- Produces: nada de código.

- [ ] **Step 1: Dejar preparado el hueco del modelo**

El `.tflite` todavía no existe: sale del notebook `AgroSense_modelo_cultivos.ipynb`, que quedó a medio entrenar. El clasificador ya degrada solo, pero las etiquetas sí se pueden dejar listas:

```bash
mkdir -p app/src/main/assets
cat > app/src/main/assets/labels.txt <<'LABELS'
Corn___Common_rust
Corn___Northern_Leaf_Blight
Corn___healthy
Potato___Early_blight
Potato___Late_blight
Potato___healthy
Tomato___Bacterial_spot
Tomato___Early_blight
Tomato___Late_blight
Tomato___healthy
LABELS
```

El orden de estas líneas **tiene que coincidir** con el de las clases del modelo cuando se exporte; si no, los diagnósticos saldrán con el nombre equivocado. Ver la celda de `mvp_classes` del notebook.

- [ ] **Step 2: Actualizar el estado en el README**

En `README.md`, sección *🚧 Estado del proyecto*, marca `[x]` en `Repositories`, `ViewModels`, `Integración UI ↔ datos`, `Login`, `Dashboard`, `Gestión de parcelas`, `Registro de actividades` y `Diagnósticos`. Deja en `[ ]` `Registro de usuario`, `Visualización de sensores` y `Predicciones`, que siguen sin pantalla.

Añade al final de esa sección:

```markdown
### Pendientes conocidos

- El modelo `agrosense_crop_model.tflite` todavía no está en `app/src/main/assets/`; sin él la pantalla de captura avisa y no clasifica.
- `Parcela` no tiene `usuarioId`: la relación Usuario → Parcela del diagrama no existe aún en el esquema.
- El acceso es local (SharedPreferences), no contra la tabla `usuarios`.
```

- [ ] **Step 3: Commit y push**

```bash
git add app/src/main/assets/labels.txt README.md
git commit -m "Document migration status and add classifier labels"
git push -u origin feature/migracion-ui
```

- [ ] **Step 4: Abrir el PR**

```bash
gh pr create --repo AnaBenassuli/AgroSense_2.0 --base main --head feature/migracion-ui \
  --title "Migrate app screens, navigation, camera and on-device classifier"
```

En la descripción deja claro, arriba del todo: qué se migró, que **el modelo TFLite no está incluido**, que la verificación en dispositivo fue manual (no hay tests instrumentados) y qué quedó pendiente.

---

## Notas sobre verificación

Este plan tiene menos TDD del que sería deseable, y es una decisión consciente, no un descuido: las únicas piezas con lógica pura son el `TypeConverter` del top-3 y el formateo de predicciones, y ésas sí llevan test (Tareas 2 y 5). Todo lo demás es UI de Compose y acceso a Room, que necesita dispositivo o emulador para probarse de verdad. Arrancar el emulador en esta máquina ya resultó impracticable, así que la puerta de calidad de las tareas 3-10 es `assembleDebug` + `lintDebug` y, al final, el recorrido manual de la Tarea 10 Step 5.

Si más adelante el equipo quiere cubrir esto en serio, el orden de mayor retorno es: tests instrumentados de los DAOs con `Room.inMemoryDatabaseBuilder` (rápidos y estables), y después `createAndroidComposeRule` para el flujo Login → Home.
