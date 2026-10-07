import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.bancopro.app',        // ← cambia esto antes de publicar en Play Store
  appName: 'BancoPro',
  webDir: 'www',

  // La WebView local se sirve por http:// en vez de https://localhost.
  // Evita el bloqueo de "contenido mixto" cuando la página llama a
  // http://www.lotfast.x10.mx/licencias/api.php
  server: {
    androidScheme: 'http',
    cleartext: true,
    allowNavigation: [
      'www.lotfast.x10.mx',
      'lotfast.x10.mx'
    ]
  },

  android: {
    // Red de seguridad por si en el futuro vuelves a https://localhost
    // pero la API sigue en HTTP. Sin esto, Android bloquea la llamada.
    allowMixedContent: true
  }
};

export default config;