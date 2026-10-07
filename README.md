# BancoPro — proyecto Capacitor listo para compilar

Este proyecto ya tiene:
- `www/index.html`: la app de BancoPro (todo el procesamiento es local; internet
  solo se usa para bajar la configuración del banco).
- `android/`: proyecto nativo generado con Capacitor, con dos cambios ya hechos:
  1. **`AndroidManifest.xml`**: intent-filters para que BancoPro aparezca en el
     selector de "Compartir" de WhatsApp y para abrir un `.jgd` tocándolo.
  2. **`MainActivity.java`**: recibe ese archivo y se lo pasa a la app
     (`window.bancoImportar`) para validarlo y calcular premios al momento.

**No pude compilar el APK aquí** porque este entorno no tiene acceso a
`services.gradle.org` ni al SDK de Android (solo permite unos pocos dominios
de npm/GitHub). Vas a necesitar compilarlo en tu computadora o en Android
Studio, donde sí hay internet completo. Los pasos de abajo son solo para eso,
no hace falta tocar ni entender el código.

## Opción A — Android Studio (la más simple)

1. Instala [Android Studio](https://developer.android.com/studio) si no lo tienes.
2. Descomprime este proyecto en tu computadora.
3. Abre Android Studio → **Open** → elige la carpeta `android/` (no la carpeta raíz).
4. Espera a que termine de sincronizar (la primera vez descarga Gradle y el SDK; necesita internet).
5. Menú **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
6. Cuando termine, aparece un enlace "locate" — ahí está el `app-debug.apk`.
7. Copia ese APK al teléfono e instálalo (activa "orígenes desconocidos" si el teléfono lo pide).

## Opción B — línea de comandos

Necesitas tener instalados Android SDK (con `ANDROID_HOME` configurado) y Java 17.

```bash
cd android
./gradlew assembleDebug
# el APK queda en: android/app/build/outputs/apk/debug/app-debug.apk
```

## Si cambias algo en www/index.html

Después de editar `www/index.html`, hay que volver a copiarlo dentro del
proyecto Android antes de compilar:

```bash
npm install          # solo la primera vez
npx cap sync android
```

Luego repite la Opción A o B para generar el APK de nuevo.

## Para una versión firmada (subir a los teléfonos sin pasar por "debug")

`assembleDebug` genera un APK firmado con una clave de pruebas de Android, que
funciona perfecto para instalar directo en los teléfonos del banco, pero
Android mostrará que es una app "de depuración". Si más adelante quieres una
versión firmada de verdad (`assembleRelease`), en Android Studio ve a
**Build → Generate Signed Bundle / APK** y sigue el asistente para crear tu
propia clave (`.jks`); guárdala en un lugar seguro, porque las próximas
actualizaciones de la app necesitan la misma clave.

## Antes del primer uso real

1. Sube la acción nueva `banca_get_paquete_offline` a tu `api.php` en el
   servidor (el archivo `api.php` completo con ese cambio ya te lo entregué
   antes en esta conversación).
2. Abre la app, entra con el usuario y contraseña del banco (pestaña
   *Configuración*) y aprieta **Descargar / actualizar configuración**.
3. A partir de ahí, todo (importar `.jgd`, códigos, resultados, cuadre)
   funciona sin conexión.

## Estructura del proyecto

```
bancopro-app/
├── www/index.html              ← la app (HTML/JS, sin build step)
├── capacitor.config.ts         ← appId com.tuempresa.bancopro
├── package.json
└── android/                    ← proyecto nativo, ábrelo con Android Studio
    ├── app/src/main/AndroidManifest.xml   (con los intent-filter de WhatsApp)
    ├── app/src/main/java/.../MainActivity.java  (recibe el .jgd compartido)
    └── ...
```
