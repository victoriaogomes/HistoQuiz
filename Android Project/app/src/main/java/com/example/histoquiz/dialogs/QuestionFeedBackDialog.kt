package com.example.histoquiz.dialogs

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.histoquiz.R
import com.example.histoquiz.activities.GameActivity
import java.util.*
import kotlin.math.roundToInt

/**
 * Método utilizado para exibir ao usuário um feedback referente a pergunta que ele selecionou para
 * que seu oponente respondesse. Exibe a resposta correta a sua pergunta, a resposta do oponente e
 * solicita que ele informe se deseja prosseguir para a próxima rodada ou se quer tentar adivinhar
 * sua lâmina
 */
class QuestionFeedBackDialog
/**
 * Método construtor da classe, recebe por parâmetro a activity que instanciou-a, a resposta
 * fornecida pelo oponente e a resposta correta da pergunta
 * @param parentActivity - activity do tipo GameActivity, que é responsável por gerenciar parti-
 * das e que criou esse dialog
 * @param auxOpponentAnswer - resposta do oponente, que pode ser nula, caso ele tenha ultrapas-
 * sado o prazo de 2 min para responder
 * @param auxCorrectAnswer - resposta correta da pergunta feita
 */(private var parentActivity: GameActivity, private var auxOpponentAnswer: Boolean?, private var auxCorrectAnswer: Boolean) :
    AppCompatDialogFragment() {
    private var cardOpponentAnswer: CardView? = null
    private var correctAnswer: TextView? = null
    private var opponentAnswer: TextView? = null
    private var guessSlide: Button? = null
    private var nextRound: Button? = null
    private var newview: View? = null

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
        newDialog.setTitle("")
        newDialog.window!!
            .setLayout(
                (parentActivity.screen.fullContent.width - parentActivity.screen.fullContent.width * 0.018).roundToInt(),
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
        newDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        handleButtonsClicks()
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
            val wm = parentActivity.windowManager
            wm.updateViewLayout(newDialog.window!!.decorView, newDialog.window!!.attributes)
        }
        return newDialog
    }

    /**
     * Método utilizado para obter a referência para certos objetos da view que serão manipulados
     * por essa classe e para inicializar alguns outros objetos utilizados aqui no back-end
     */
    fun initGUI() {
        newview = activity?.layoutInflater!!.inflate(R.layout.dialog_question_feedback, null)
        cardOpponentAnswer = newview?.findViewById(R.id.cardResp)
        correctAnswer = newview?.findViewById(R.id.feedbackPergunta)
        opponentAnswer = newview?.findViewById(R.id.respOponente)
        if (auxOpponentAnswer != null) {
            if (auxOpponentAnswer as Boolean) {
                opponentAnswer?.text = getString(R.string.sim)
            } else {
                opponentAnswer?.text = getString(R.string.nao)
            }
            if (auxCorrectAnswer == auxOpponentAnswer) context?.let { it -> cardOpponentAnswer!!.setCardBackgroundColor(ContextCompat.getColor(it, R.color.green))}
            else context?.let {cardOpponentAnswer!!.setCardBackgroundColor(ContextCompat.getColor(it, R.color.red)) }
        } else {
            opponentAnswer?.text = getString(R.string.naoResp)
            context?.let {cardOpponentAnswer!!.setCardBackgroundColor(ContextCompat.getColor(it, R.color.yellow)) }
        }
        if (auxCorrectAnswer) {
            correctAnswer?.text = String.format(Locale.getDefault(), "%s %s", getString(R.string.feedbackPergunta), getString(R.string.simCaps))
        } else {
            correctAnswer?.text = String.format(
                Locale.getDefault(), "%s %s", getString(R.string.feedbackPergunta), getString(
                    R.string.naoCaps
                )
            )
        }
        guessSlide = newview?.findViewById(R.id.adivinharLamina)
        nextRound = newview?.findViewById(R.id.proximaRodada)
    }

    /**
     * Método utilizado para lidar com os cliques nos botões desse dialog ("próxima rodada" ou
     * "adivinhar lâmina")
     */
    private fun handleButtonsClicks() {
        nextRound!!.setOnClickListener {
            parentActivity.closeQuestionFeedback()
            if (parentActivity.pcOpponent) parentActivity.computerOpponent!!.estadoA()
            else {
                parentActivity.onlineOpponent!!.myRoomRef?.child("nextRound")?.setValue("sim")
                parentActivity.onlineOpponent!!.estadoA()
            }
        }
        guessSlide!!.setOnClickListener {
            parentActivity.closeQuestionFeedback()
            if (parentActivity.pcOpponent) parentActivity.computerOpponent!!.estadoJ() else parentActivity.onlineOpponent!!.estadoI()
        }
    }
}