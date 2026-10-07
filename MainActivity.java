package com.tuempresa.bancopro;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.webkit.WebView;
import com.getcapacitor.BridgeActivity;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Recibe los archivos .jgd que llegan desde WhatsApp (u otra app) por
 * "Compartir" (ACTION_SEND / ACTION_SEND_MULTIPLE) o al tocarlos
 * directamente (ACTION_VIEW), y se los pasa al WebView llamando a la
 * función window.bancoImportar(nombre, texto) que ya existe en www/index.html.
 *
 * Todo el procesamiento (validar firma, calcular premios, cuadre) ocurre
 * en JavaScript dentro del WebView; esta clase solo entrega los bytes.
 */
public class MainActivity extends BridgeActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Se espera a que el WebView cargue index.html antes de intentar
        // llamar a window.bancoImportar; 1500ms es margen suficiente en
        // casi todos los teléfonos, y si la app tarda más, MODULO_SYNC/lo
        // que sea da igual porque no hay pérdida de datos: el Intent con
        // los datos ya quedó guardado por Android hasta que se procese.
        getBridge().getWebView().postDelayed(() -> manejarIntent(getIntent()), 1500);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        manejarIntent(intent);
    }

    private void manejarIntent(Intent it) {
        if (it == null) return;
        String accion = it.getAction();
        ArrayList<Uri> uris = new ArrayList<>();

        if (Intent.ACTION_VIEW.equals(accion) && it.getData() != null) {
            uris.add(it.getData());
        } else if (Intent.ACTION_SEND.equals(accion)) {
            Uri u = it.getParcelableExtra(Intent.EXTRA_STREAM);
            if (u != null) uris.add(u);
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(accion)) {
            ArrayList<Uri> l = it.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            if (l != null) uris.addAll(l);
        } else {
            return; // apertura normal de la app, nada que importar
        }

        WebView webView = getBridge().getWebView();
        for (Uri uri : uris) {
            try {
                String nombre = nombreDeArchivo(uri);
                String contenido = leerTexto(uri);
                String js = "window.bancoImportar(" + JSONObject.quote(nombre) + "," + JSONObject.quote(contenido) + ")";
                runOnUiThread(() -> webView.evaluateJavascript(js, null));
            } catch (Exception e) {
                // Un archivo ilegible o demasiado grande no debe tumbar la app;
                // simplemente se ignora ese archivo y se sigue con los demás.
            }
        }

        // Limpia el intent para no reimportar el mismo archivo si el
        // usuario rota la pantalla o vuelve a esta actividad.
        setIntent(new Intent());
    }

    private String nombreDeArchivo(Uri uri) {
        String nombre = "archivo.jgd";
        try (Cursor c = getContentResolver().query(uri, null, null, null, null)) {
            if (c != null && c.moveToFirst()) {
                int i = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (i >= 0) {
                    String n = c.getString(i);
                    if (n != null && !n.trim().isEmpty()) nombre = n;
                }
            }
        } catch (Exception ignored) {}
        return nombre;
    }

    private String leerTexto(Uri uri) throws Exception {
        try (InputStream in = getContentResolver().openInputStream(uri);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            if (in == null) throw new IllegalStateException("No se pudo abrir el archivo");
            byte[] buf = new byte[8192];
            int n;
            // Límite de 20 MB por archivo: un .jgd normal pesa unos pocos KB,
            // este tope es solo para no cargar algo enorme por error a memoria.
            while ((n = in.read(buf)) > 0 && out.size() < 20_000_000) {
                out.write(buf, 0, n);
            }
            return out.toString("UTF-8");
        }
    }
}
