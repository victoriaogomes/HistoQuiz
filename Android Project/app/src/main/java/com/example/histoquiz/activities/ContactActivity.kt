package com.example.histoquiz.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.histoquiz.databinding.ActivityContactBinding


class ContactActivity : AppCompatActivity() {
    private lateinit var screen: ActivityContactBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen = ActivityContactBinding.inflate(layoutInflater)
        setContentView(screen.root)
        setClickListeners()
        hideSystemUI()
    }

    /**
     * Método chamado quando a janela atual da activity ganha ou perde o foco, é utilizado para es-
     * conder novamente a barra de status e a navigation bar.
     * @param hasFocus - booleano que indica se a janela desta atividade tem foco.
     */
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        hideSystemUI()
    }

    /**
     * Método utilizado para fazer com que a barra de status e a navigation bar não sejam exibidas
     * na activity. Caso o usuário queira visualizá-las, ele deve realizar um movimento de arrastar
     * para cima (na navigation bar), ou para baixo (na status bar), o que fará com que elas apare-
     * çam por um momento e depois sumam novamente.
     */
    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        if (controller != null) {
            controller.hide(WindowInsetsCompat.Type.statusBars())
            controller.hide(WindowInsetsCompat.Type.navigationBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun setClickListeners(){
        screen.problemasTecBTN.setOnClickListener { sendMail("Problemas técnicos.") }
        screen.sugestoesBTN.setOnClickListener { sendMail("Sugestões para o App.") }
        screen.compartExprBTN.setOnClickListener { sendMail("Comentário sobre experiência com o App.") }
        screen.outrosAssuntosBTN.setOnClickListener { sendMail("Outros assuntos.") }
    }

    private fun sendMail(subject: String){
        val send = Intent(Intent.ACTION_SENDTO)
        val uriText = "mailto:" + Uri.encode("histoquiz.contato@gmail.com").toString() +
                "?subject=" + Uri.encode(subject).toString() +
                "&body=" + Uri.encode("")
        val uri: Uri = Uri.parse(uriText)
        send.data = uri
        startActivity(Intent.createChooser(send, "Enviar email..."))
    }
}