# Verificación — 3 de septiembre de 2026

## Resultados

- `testDebugUnitTest`: 6 pruebas correctas (resolución de ID/URI, búsqueda y prueba básica).
- `connectedDebugAndroidTest`: 6 pruebas correctas en Pixel 6a con Android 17.
- `assembleDebug`: APK de depuración generado correctamente.
- `lintDebug`: 0 errores y 46 advertencias. No se añadió un baseline para ocultar errores.

Las pruebas en dispositivo comprueban el contexto de la app, la consulta individual de la raíz, la paginación del menú, los metadatos y la consulta de canciones agregadas, la conexión y suscripción mediante el navegador multimedia clásico de Android, y la resolución de los recursos de icono del servicio y de atribución.

No se borró ni se sembró la biblioteca del teléfono. Las pruebas consultan el catálogo existente; la cobertura de canciones depende de que haya música agregada. Los resultados locales completos permanecen en las carpetas de compilación, excluidas del repositorio para no publicar datos del dispositivo.

## Correcciones realizadas

- Consulta individual del catálogo que antes devolvía operación no soportada.
- Paginación del menú que antes devolvía todas las categorías en cualquier página.
- Resolución consistente de identificadores de canciones y rechazo de elementos ajenos al catálogo.
- Cola de biblioteca al seleccionar una canción desde un cliente externo; búsqueda por título, artista o álbum.
- Suscripciones y avisos de cambios de canciones/favoritos.
- Declaración explícita del icono del servicio y recurso monocromático de audífonos.
- Declaración de búsqueda multimedia y anotaciones requeridas por Media3.
- Permiso de audio condicionado a la versión de Android.
- Sincronización del progreso y canción actual aunque la pantalla completa del reproductor esté cerrada.
- Codificación del nombre de listas en rutas de navegación.
- Manejo de ausencia de app de correo y de permisos persistentes de carpeta rechazados.
- Serialización de escaneos y rechazo de carpetas raíz sin acceso antes de reemplazar el catálogo.

## Pendiente y límites

- **No se ha comprobado visualmente el launcher ni el menú en un coche o DHU.** La prueba de recursos de icono y de protocolo no garantiza que un host acepte un APK instalado manualmente. Para continuar con DHU se necesita activar el servidor de unidad principal en el teléfono.
- No se comprobó audio extremo a extremo, asistente de voz, desconexión/reconexión del coche ni conducción real.
- Las correcciones de navegación, correo y escaneo requieren más pruebas de interacción y de fallos de proveedores de documentos.
- Persisten advertencias de Lint, principalmente mantenimiento de dependencias, recursos y recomendaciones de API/estilo. No se realizó una auditoría exhaustiva de seguridad o de vulnerabilidades de dependencias.
- No se validaron bibliotecas masivas, colisiones de identificadores derivados de URI, recuperación de portadas de caché ni interrupciones durante la reconciliación de la base de datos.
- Los tres widgets compilan y sus proveedores se comprueban mediante una prueba instrumentada. En la última revisión no había un teléfono conectado para ejecutar de nuevo esa prueba ni verificar visualmente los widgets en un launcher real.

Estos resultados describen el alcance real de la revisión; no certifican ausencia de todos los errores ni aprobación de Google Play/Android Auto.
