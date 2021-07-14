package com.example.histoquiz.dialogs

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.cardview.widget.CardView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.histoquiz.R
import com.example.histoquiz.activities.GameActivity
import java.util.*
import kotlin.math.roundToInt

/**
 * Classe utilizada para exibir um dialog informando os resultados ao fim de uma partida: pontuação
 * do jogador, pontuação do seu oponente e quem foi o ganhador
 */
class EndGameDialog
/**
 * Método construtor da classe, recebe como parâmetro a activity que instanciou esse dialog
 * @param parent - activity do tipo GameActivity, que é responsável por gerenciar partidas e
 * que criou esse dialog
 */(var parent: GameActivity) : AppCompatDialogFragment() {
    var playerScore: TextView? = null
    var opponentScore: TextView? = null
    private var winner: TextView? = null
    var playerCard: CardView? = null
    var opponentCard: CardView? = null
    private var backToMenu: Button? = null
    var newview: View? = null

    /**
     * Método chamado no instante que o dialog é criado, seta qual view será associada a essa classe
     * @param savedInstanceState - contém o estado anteriormente salvo da atividade (pode ser nulo)
     * @return - o dialog criado
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val newDialog = activity?.let { Dialog(it) }
        newDialog!!.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        initGUI()
        newview?.let { newDialog.setContentView(it) }
        setGameResultInfo()
        newDialog.setTitle("")
        newDialog.window!!.setLayout(
            (parent.screen.fullContent.width - parent.screen.fullContent.width * 0.018).roundToInt(),
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        newDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        newDialog.setCanceledOnTouchOutside(false)
        newDialog.window!!.setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
        newDialog.setOnShowListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                WindowCompat.setDecorFitsSystemWindows(newDialog.window!!, false)
                val controller = WindowCompat.getInsetsController(newDialog.window!!, newDialog.window!!.decorView)
                if (controller != null) {
                    controller.hide(WindowInsetsCompat.Type.statusBars())
                    controller.hide(WindowInsetsCompat.Type.navigationBars())
                    controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } else newDialog.window!!
                .decorView.systemUiVisibility = activity?.window?.decorView?.systemUiVisibility!!
            newDialog.window!!.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
            val wm = parent.windowManager
            wm.updateViewLayout(newDialog.window!!.decorView, newDialog.window!!.attributes)
        }
        return newDialog
    }

    /**
     * Método utilizado para obter a referência para certos objetos da view que serão manipulados
     * por essa classe e para inicializar alguns outros objetos utilizados aqui no back-end
     */
    fun initGUI() {
        newview = activity?.layoutInflater!!.inflate(R.layout.dialog_end_game, null)
        playerScore = newview?.findViewById(R.id.minhaPontuacao)
        opponentScore = newview?.findViewById(R.id.pontOponente)
        winner = newview?.findViewById(R.id.ganhadorPartida)
        playerCard = newview?.findViewById(R.id.cardMinhaPontuacao)
        opponentCard = newview?.findViewById(R.id.cardPontuacaoOponente)
        backToMenu = newview?.findViewById(R.id.menu)
        backToMenu?.tag = "BACK_MENU"
        backToMenu?.setOnClickListener(parent)
    }

    /**
     * Método utilizado para setar nesse dialog os resultados da partida que acabou de ser finaliza-
     * da, mostrando a pontuação de cada um dos jogadores e informando quem foi o ganhador
     */
    private fun setGameResultInfo() {
        playerScore!!.text = String.format(Locale.getDefault(), "%d", parent.getPlayerScore(1))
        opponentScore!!.text = String.format(Locale.getDefault(), "%d", parent.getPlayerScore(2))
        when {
            parent.getPlayerScore(1) > parent.getPlayerScore(2) -> {
                winner!!.text = getString(R.string.vc)
                playerCard!!.setCardBackgroundColor(resources.getColor(R.color.green))
                opponentCard!!.setCardBackgroundColor(resources.getColor(R.color.red))
            }
            parent.getPlayerScore(1) == parent.getPlayerScore(2) -> {
                winner!!.text = getString(R.string.empate)
                playerCard!!.setCardBackgroundColor(resources.getColor(R.color.green))
                opponentCard!!.setCardBackgroundColor(resources.getColor(R.color.green))
            }
            else -> {
                winner!!.text = getString(R.string.seuOponente)
                playerCard!!.setCardBackgroundColor(resources.getColor(R.color.red))
                opponentCard!!.setCardBackgroundColor(resources.getColor(R.color.green))
            }
        }
    }
}