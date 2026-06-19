# 🎵 CATUNES — Premium Android Music Player

<p align="center">
  <img src="app/src/main/res/drawable/catunes.png" alt="CATUNES Logo" width="120" style="border-radius: 20px;"/>
</p>

<p align="center">
  <a href="https://developer.android.com"><img src="https://img.shields.io/badge/Platform-Android%208.0%2B%20%28API%2026%2B%29-brightgreen.svg" alt="Platform"/></a>
  <a href="https://kotlinlang.org"><img src="https://img.shields.io/badge/Language-Kotlin%201.9-blue.svg" alt="Kotlin"/></a>
  <a href="https://developer.android.com/jetpack/compose"><img src="https://img.shields.io/badge/UI-Jetpack%20Compose-purple.svg" alt="Jetpack Compose"/></a>
  <a href="https://github.com/google/ExoPlayer"><img src="https://img.shields.io/badge/Player-Media3%20%2F%20ExoPlayer-red.svg" alt="Media3 / ExoPlayer"/></a>
  <a href="https://github.com/features/actions"><img src="https://img.shields.io/badge/Build-Gradle%20Kotlin%20DSL-cyan.svg" alt="Build System"/></a>
</p>

---

**CATUNES** es un reproductor de música local de nivel premium para Android diseñado con las directrices de **Material Design 3**, una tipografía estilizada basada en **SixtyFour Font**, transiciones optimizadas y un completo motor de audio impulsado por **Media3/ExoPlayer**.

---

## ✨ Características Premium (Ya Implementadas)

- 📂 **Acceso Seguro por Carpetas (SAF)**: Añade y gestiona múltiples carpetas de música (incluyendo tarjetas SD externas) sin conceder permisos globales invasivos.
- 🔍 **Buscador Premium con Chips**: Filtra tu biblioteca de forma instantánea por **Todo, Canción, Artista o Álbum**.
- 🍒 **Letras Sincronizadas (.lrc)**: Soporte completo de desplazamiento dinámico en tiempo real y auto-scroll de letras sincronizadas detectadas al lado de la música.
- 🔀 **Controles de Shuffle & Repeat**: Modos aleatorio y repetición totalmente integrados y nativos de Media3, disponibles tanto en el reproductor como en el MiniPlayer.
- 🎛️ **Ecualizador Nativo**: Ajustes finos de audio mediante sliders integrados para personalizar tus frecuencias favoritas.
- 🔊 **Volumen Estable (Normalización de Audio)**: Normalizador inteligente integrado mediante `LoudnessEnhancer` que previene cambios bruscos de nivel entre pistas.
- 💫 **MiniPlayer Premium**: Barra inferior flotante con transiciones optimizadas y gestos dinámicos, visible a lo largo de toda la navegación.
- 🎨 **Ajustes de Personalización**:
  - Selector de colores de acento dinámicos.
  - Alternador de pantalla (Modo Oscuro, Claro o Sincronizado con el Sistema).
  - Multiplicador de escala de fuente global para legibilidad.
- 🚗 **Conexión Automotriz (Bluetooth Metadata)**: Transmisión nativa de carátulas y metadatos (Título, Artista, Álbum) a vehículos o dispositivos externos vía perfiles AVRCP con controles de reproducción heredados.

---

## 🛠️ Stack Tecnológico

- **Lenguaje**: [Kotlin](https://kotlinlang.org/) (Corrutinas, StateFlow)
- **UI**: [Jetpack Compose](https://developer.android.com/jetpack/compose) con animaciones fluidas (`tween`).
- **Base de Datos**: [Room Database](https://developer.android.com/training/data-storage/room) para indexado rápido de metadata offline.
- **Motor de Audio**: [Jetpack Media3](https://developer.android.com/guide/topics/media/media3) (`ExoPlayer` + `MediaSession` + `MediaSessionService`).
- **Persistencia**: [DataStore Preferences](https://developer.android.com/topic/libraries/architecture/datastore) para configuración del usuario.
- **Carga de Imágenes**: [Coil](https://github.com/coil-kt/coil) para carátulas de álbum desde el caché interno.

---

## 💻 Guía de Compilación e Instalación

### 1. Requisitos previos
* [Android Studio Koala](https://developer.android.com/studio) o superior.
* JDK 17 (incluido automáticamente con Android Studio).
* Un dispositivo con Android 8.0 (API 26) o superior con Depuración USB activa, o un emulador.

### 2. Clonar y Compilar
Abre una terminal y ejecuta:
```bash
git clone https://github.com/TU_USUARIO/CATUNES.git
cd CATUNES
./gradlew.bat assembleDebug
```
El instalador APK estará en:  
`app/build/outputs/apk/debug/app-debug.apk`

---

## 🚀 Cómo Subir este Proyecto a GitHub

Si tienes Git instalado en tu computadora, sigue estos comandos para subir todo el proyecto a tu perfil de GitHub:

1. **Crear el repositorio en GitHub**: Ve a [GitHub](https://github.com/) y crea un nuevo repositorio llamado `CATUNES` (déjalo vacío, sin README ni .gitignore).
2. **Inicializar y subir desde la terminal**:
```bash
# Inicializar Git en la carpeta local
git init

# Agregar los archivos del proyecto (.gitignore ya está configurado)
git add .

# Crear el primer commit
git commit -m "Initial commit: CATUNES Music Player Premium Release"

# Cambiar a la rama principal
git branch -M main

# Conectar con tu repositorio remoto de GitHub
git remote add origin https://github.com/TU_USUARIO/CATUNES.git

# Subir los archivos
git push -u origin main
```
*(Reemplaza `TU_USUARIO` por tu nombre de usuario real en GitHub).*
