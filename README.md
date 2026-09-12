# 🌱 AgroSense 2.0

<p align="center">
  <strong>Smart agriculture. Better decisions.</strong>
</p>

<p align="center">
  Aplicación móvil para el monitoreo inteligente de parcelas agrícolas,
  gestión de actividades, sensores, diagnósticos y predicciones.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Android-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white">
  <img src="https://img.shields.io/badge/Jetpack-Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white">
  <img src="https://img.shields.io/badge/Database-Room-3DDC84?style=for-the-badge&logo=android&logoColor=white">
  <img src="https://img.shields.io/badge/Status-In%20Development-orange?style=for-the-badge">
</p>

---

## 🌾 Sobre AgroSense

**AgroSense** es una aplicación Android diseñada para facilitar la administración y monitoreo de parcelas agrícolas desde un dispositivo móvil.

El sistema busca centralizar información relevante del cultivo para que el usuario pueda consultar sus parcelas, registrar actividades, visualizar información proveniente de sensores y mantener un historial de diagnósticos.

La arquitectura está siendo desarrollada de forma modular para permitir posteriormente la integración de nuevas fuentes de datos, servicios externos y herramientas de análisis.

---

## ✨ Funcionalidades

Actualmente el proyecto contempla:

- 👤 Gestión de usuarios
- 🌱 Registro y administración de parcelas
- 📋 Registro de actividades agrícolas
- 🌡️ Gestión de información de sensores
- 🔎 Historial de diagnósticos
- 📊 Consulta de información por parcela
- 🔮 Estructura para predicciones
- 💾 Persistencia local de datos
- 📱 Interfaz móvil desarrollada con Jetpack Compose

---

## 🛠️ Tecnologías

| Tecnología | Uso |
|---|---|
| **Kotlin** | Lenguaje principal |
| **Android Studio** | Entorno de desarrollo |
| **Jetpack Compose** | Interfaz de usuario |
| **Room** | Persistencia local |
| **SQLite** | Base de datos |
| **Coroutines / Flow** | Manejo de datos reactivos |
| **Gradle** | Gestión y compilación del proyecto |
| **Git / GitHub** | Control de versiones |

---

## 🏗️ Arquitectura

El proyecto separa la persistencia de datos de la interfaz de usuario.

```text
UI
 │
 ▼
Repository
 │
 ▼
DAO
 │
 ▼
Room Database
 │
 ▼
SQLite
```

Esto permite mantener una estructura escalable y facilita la evolución del proyecto.

---

## 🗄️ Modelo de datos

Actualmente AgroSense cuenta con entidades para:

```text
Usuario
   │
   └── Parcela
         │
         ├── Actividad
         ├── Diagnóstico
         ├── Sensor
         └── Predicción
```

La persistencia local se implementa utilizando **Room Database**.

---

## 📂 Estructura del proyecto

```text
com.agrosense.app
│
├── data
│   └── local
│       │
│       ├── converter
│       │   └── Converters.kt
│       │
│       ├── dao
│       │   ├── ActividadDao.kt
│       │   ├── DiagnosticoDao.kt
│       │   ├── ParcelaDao.kt
│       │   ├── SensorDao.kt
│       │   └── UsuarioDao.kt
│       │
│       ├── entity
│       │   ├── Actividad.kt
│       │   ├── Diagnostico.kt
│       │   ├── Parcela.kt
│       │   ├── Prediccion.kt
│       │   ├── Sensor.kt
│       │   └── Usuario.kt
│       │
│       ├── AgroSenseDatabase.kt
│       └── DatabaseProvider.kt
│
├── ui
│   └── theme
│
└── MainActivity.kt
```

---

## 🚧 Estado del proyecto

**AgroSense 2.0 se encuentra actualmente en desarrollo.**

### Backend local

- [x] Definición de entidades
- [x] Implementación de Room Database
- [x] DAOs
- [x] Type Converters
- [x] Database Provider
- [x] Compilación inicial exitosa
- [x] Repositories
- [x] ViewModels
- [x] Integración UI ↔ datos

### Aplicación

- [x] Login
- [ ] Registro de usuario
- [x] Dashboard
- [x] Gestión de parcelas
- [x] Registro de actividades
- [ ] Visualización de sensores
- [x] Diagnósticos
- [ ] Predicciones

### Pendientes conocidos

- El modelo `agrosense_crop_model.tflite` todavía no está en `app/src/main/assets/`; sin él la pantalla de captura avisa y no clasifica. El modelo y su `labels.txt` salen juntos de la sección 8 del notebook `AgroSense_modelo_cultivos.ipynb`, y deben copiarse juntos: el orden de las etiquetas tiene que coincidir con el orden de salida del modelo, porque el clasificador resuelve los nombres por índice y un archivo desalineado no falla, simplemente etiqueta mal cada diagnóstico. Por eso no se escribió a mano ese archivo.
- `Parcela` no tiene `usuarioId`: la relación Usuario → Parcela del diagrama no existe aún en el esquema.
- El acceso es local (SharedPreferences), no contra la tabla `usuarios`.
- La verificación en dispositivo real sigue pendiente: la puerta de calidad de esta migración fue `assembleDebug` + `lintDebug` y pruebas unitarias, pero la app todavía no se corrió en un teléfono físico, así que el flujo de punta a punta no está verificado.

---

## 🧠 Objetivo

AgroSense busca construir una plataforma que permita transformar información agrícola en datos útiles para la toma de decisiones, creando una base tecnológica preparada para futuras integraciones de **IoT, analítica y agricultura inteligente**.

---

## 🚀 Ejecución

Clona el repositorio:

```bash
git clone https://github.com/AnaBenassuli/AgroSense_2.0.git
```

Abre el proyecto en **Android Studio**, sincroniza las dependencias de Gradle y ejecuta la aplicación en un dispositivo Android o emulador.

---

## 👥 Equipo

Proyecto desarrollado como parte de **Ingeniería en Tecnologías de Software**.

---

<p align="center">
  🌱 <strong>AgroSense</strong><br>
  Tecnología aplicada a una agricultura más inteligente.
</p>
