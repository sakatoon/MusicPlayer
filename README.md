# SakaToOn MusicPlayer

Reproductor de música local para Android, desarrollado por **SakaToOn** con Kotlin, Jetpack Compose y Media3. Permite elegir carpetas, organizar canciones y acceder a la biblioteca desde Android Auto.

## Funciones

- **Biblioteca local:** agrega varias carpetas con el selector de Android y explora también sus subcarpetas. Puedes quitar carpetas de la biblioteca o volver a escanearlas; estas acciones no borran tus archivos de música.
- **Detección de audio:** reconoce tipos MIME de audio y extensiones MP3, M4A, WAV, FLAC, OGG y AAC. La reproducción de un archivo concreto depende de sus códecs y del dispositivo.
- **Metadatos y portadas:** obtiene título, artista, álbum, duración e imagen incrustada. Guarda metadatos en Room y las portadas extraídas en la caché del teléfono.
- **Reproductor:** reproducir/pausar, canción anterior y siguiente, búsqueda de posición y tiempos de reproducción.
- **Mini reproductor ampliado:** portada, título, artista, progreso y controles grandes en Biblioteca y en el detalle de una lista.
- **Favoritos:** agrega y quita canciones; se conservan localmente.
- **Listas de reproducción:** crear y eliminar listas, agregar canciones y retirarlas. Al elegir una canción, la cola actual se basa en la biblioteca completa, no sólo en la lista visible.
- **Segundo plano:** sesión multimedia y notificación de reproducción mediante Media3; gestiona el foco de audio y pausa cuando se desconectan los audífonos. Cerrar la ventana del teléfono no detiene una reproducción activa.
- **Interfaz adaptable:** navegación inferior en teléfonos y lateral en pantallas amplias, tema oscuro negro/gris/blanco, controles de carpetas apilados cuando el espacio es reducido.
- **Información del desarrollador:** logotipo de SakaToOn, descripción, versión y botón que abre una aplicación de correo con el destinatario `sakatoon@gmail.com`. No envía mensajes automáticamente.

## Android Auto

La app del teléfono declara la capacidad `media` y publica un `MediaLibraryService`. **Android Auto dibuja la interfaz del coche**, utilizando el catálogo y los controles que ofrece la app; no se replica la pantalla Compose del teléfono.

### Menú del coche

- **Todas las canciones:** canciones agregadas previamente en el teléfono.
- **Favoritos:** canciones marcadas como favoritas.
- Seleccionar una canción permite reproducirla y recorrer la cola con anterior/siguiente.
- El catálogo admite paginación, consulta individual y avisos de cambios para clientes suscritos.
- La búsqueda multimedia admite coincidencias por título, artista y álbum. La disponibilidad del control por voz depende del asistente y del host de Android Auto.
- Se declara el icono de launcher de la app y un icono monocromático de audífonos para superficies de atribución/controles.

### Primer uso

1. Instala la app en un teléfono con Android 9 o superior.
2. Abre **Ajustes → Agregar carpeta**, concede acceso con el selector del sistema y espera el escaneo.
3. Comprueba que las canciones aparecen y se reproducen en el teléfono.
4. Conecta el teléfono a un vehículo compatible con Android Auto y abre **MusicPlayer** en el launcher del coche.
5. Entra a **Todas las canciones** o **Favoritos** para seleccionar música.

La app no importa carpetas desde la pantalla del coche. Configura tu biblioteca antes de conducir.

### Si la app no aparece

Un APK instalado manualmente puede no aparecer por las reglas del host o del origen de instalación. Para desarrollo, utiliza el **Desktop Head Unit (DHU)** y las opciones de desarrollo documentadas por Google. La publicación de este código en GitHub **no equivale a distribuir ni aprobar la app en Google Play o Android Auto**.

Esta es una app de teléfono compatible con **Android Auto**; no incluye un módulo independiente para instalar directamente en **Android Automotive OS**.

Referencias oficiales: [integración multimedia](https://developer.android.com/media/implement/surfaces/cars), [iconos y manifiesto](https://developer.android.com/training/cars/media/configure-manifest), [pruebas con DHU](https://developer.android.com/training/cars/testing/dhu).

## Privacidad y permisos

- Las canciones permanecen en las carpetas elegidas por el usuario. No se suben a GitHub ni a un servidor de la app.
- Room conserva el catálogo, los favoritos y las relaciones de listas; DataStore recuerda las carpetas autorizadas.
- Se solicitan permisos de lectura de audio/almacenamiento según la versión de Android, además de acceso persistente a las carpetas elegidas mediante Storage Access Framework.
- Los permisos de servicio en primer plano permiten la reproducción multimedia en segundo plano.
- El manifiesto incluye acceso a Internet; esta versión no implementa cuentas, catálogo de streaming, anuncios ni sincronización en la nube.
- Android tiene habilitada la copia de seguridad de la app. Una restauración puede requerir volver a autorizar las carpetas del nuevo dispositivo.
- No se incluyen claves de firma, credenciales, rutas personales, bases de datos ni archivos musicales en este repositorio.

## Compilar

Requisitos: Android Studio compatible con AGP 8.13.2, JDK 17 o superior (comprobado con JDK 21), SDK Android 36, Build Tools 35.0.0 y acceso a Google Maven/Maven Central para descargar dependencias.

1. Clona el repositorio y ábrelo en Android Studio.
2. Instala el SDK solicitado y permite la sincronización de Gradle.
3. Android Studio genera `local.properties` con tu ruta al SDK. Este archivo no se versiona.
4. Ejecuta los comandos con el wrapper incluido:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat lintDebug
.\gradlew.bat assembleDebug
# Requiere un dispositivo conectado y autorizado; instala el APK de pruebas.
.\gradlew.bat connectedDebugAndroidTest
```

En Linux/macOS utiliza `./gradlew` en lugar de `gradlew.bat`.

El APK de depuración se genera en `app/build/outputs/apk/debug/app-debug.apk`. No es una versión de producción firmada para distribución. Para distribuir, configura tu propia firma fuera del repositorio.

## Pruebas y alcance de verificación

- Pruebas unitarias: resolución de canciones por ID numérico o URI, identificadores desconocidos y búsqueda del catálogo.
- Pruebas instrumentadas: conexión a `MediaBrowser` moderno y clásico, consulta de la raíz, paginación, metadatos de canciones agregadas y recursos de iconos, sin borrar ni modificar la biblioteca del teléfono.
- Revisión estática: Android Lint; las advertencias de actualización de dependencias no son una auditoría completa de vulnerabilidades.
- La validación del servicio no sustituye la comprobación visual en un vehículo/DHU ni una prueba de audio extremo a extremo. Consulta [VERIFICATION.md](VERIFICATION.md) para conocer los resultados y límites de esta revisión.

## Estructura

```text
app/src/main/java/com/sakatoon/musicplayer/
  data/model/        Canciones, favoritos y listas
  data/db/           Base de datos y consultas Room
  data/repository/   Escaneo de carpetas y preferencias
  service/           Sesión multimedia y catálogo de Android Auto
  ui/components/     Mini reproductor, navegación y diálogos
  ui/screens/        Biblioteca, reproductor, favoritos, listas y ajustes
  ui/viewmodel/      Estado del teléfono y conexión al servicio
app/src/test/        Pruebas unitarias
app/src/androidTest/ Pruebas en dispositivo
```

## Límites actuales

No incluye ecualizador, letras, reproducción aleatoria, repetición configurable, temporizador, streaming ni edición de etiquetas. Las listas personalizadas se gestionan en el teléfono y no se muestran como categorías del menú del coche. La música debe seguir accesible en las carpetas autorizadas; cambiar rutas o permisos requiere un nuevo escaneo. La recuperación de portadas eliminadas de la caché y la gestión de bibliotecas muy grandes requieren pruebas adicionales.

## Desarrollo y contribuciones

Antes de proponer cambios, ejecuta las pruebas y Lint, evita incluir archivos privados y describe los dispositivos probados. Este repositorio público no establece por sí solo una licencia de reutilización: no se ha añadido una licencia de software o de los recursos gráficos.

**Desarrollador:** SakaToOn · **Contacto:** [sakatoon@gmail.com](mailto:sakatoon@gmail.com)
