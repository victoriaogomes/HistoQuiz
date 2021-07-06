package com.example.histoquiz.activities

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.histoquiz.databinding.ActivityWhatItIsBinding

class WhatItIsActivity : AppCompatActivity() {
    private lateinit var screen: ActivityWhatItIsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen = ActivityWhatItIsBinding.inflate(layoutInflater)
        setContentView(screen.root)
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            if (controller != null) {
                controller.hide(WindowInsetsCompat.Type.statusBars())
                controller.hide(WindowInsetsCompat.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }
}