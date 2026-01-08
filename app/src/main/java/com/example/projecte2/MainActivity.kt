package com.example.projecte2

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var myWebView: WebView
    private var filePathCallback: ValueCallback<Array<Uri>>? = null

    // Este es el "lanzador" que abre la carpeta de archivos del móvil
    private val fileChooserLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val results = WebChromeClient.FileChooserParams.parseResult(result.resultCode, data)
            filePathCallback?.onReceiveValue(results)
        } else {
            filePathCallback?.onReceiveValue(null)
        }
        filePathCallback = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        myWebView = findViewById(R.id.myWebView)

        // --- CONFIGURACIÓN PARA VUE ---
        myWebView.settings.javaScriptEnabled = true
        myWebView.settings.domStorageEnabled = true
        myWebView.settings.allowFileAccess = true
        myWebView.settings.allowContentAccess = true

        // Para que los enlaces no se abran en el navegador externo (Chrome)
        myWebView.webViewClient = WebViewClient()

        // --- CONFIGURACIÓN PARA SUBIR ARCHIVOS (IA) ---
        myWebView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                // Guardamos el callback para enviar el archivo de vuelta a Vue
                this@MainActivity.filePathCallback = filePathCallback

                val intent = fileChooserParams?.createIntent()
                try {
                    fileChooserLauncher.launch(intent)
                } catch (e: Exception) {
                    this@MainActivity.filePathCallback = null
                    return false
                }
                return true
            }
        }

        // --- CARGAR TU WEB ---
        // 10.0.2.2 es el túnel hacia el localhost de tu ordenador
        myWebView.loadUrl("http://10.0.2.2:8080")
    }

    // Si el usuario da a "atrás", que navegue por la web en vez de cerrar la app
    override fun onBackPressed() {
        if (myWebView.canGoBack()) {
            myWebView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}