package com.example.histoquiz.dialogs

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.histoquiz.R
import com.example.histoquiz.activities.LocalGameActivity
import com.google.android.material.textfield.TextInputLayout
import kotlin.math.roundToInt

class SetTeamsDialog
/**
 * Método construtor da classe, recebe como parâmetro a activity que instanciou esse dialog
 *
 * @param parentActivity - activity do tipo GameActivity, que é responsável por gerenciar
 * partidas e que criou esse dialog
 */(private var parentActivity: LocalGameActivity) : AppCompatDialogFragment() {
    var continuar: Button? = null
    var inflater: LayoutInflater? = null
    var player1: TextInputLayout? = null
    var player2: TextInputLayout? = null
    var player3: TextInputLayout? = null
    var player4: TextInputLayout? = null
    private var playersNames: Array<String>? = null
    private var aux = 0
    private var player1Dropdown: AutoCompleteTextView? = null
    private var player2Dropdown: AutoCompleteTextView? = null
    private var player3Dropdown: AutoCompleteTextView? = null
    private var player4Dropdown: AutoCompleteTextView? = null
    var newview: View? = null

    /**
     * Método chamado no instante que o dialog é criado, seta qual view será associada a essa classe
     *
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
        populatePlayer1Spinner()
        populatePlayer2Spinner()
        populatePlayer3Spinner()
        populatePlayer4Spinner()
        handleSpinnersClicks()
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
        newview = activity?.layoutInflater!!.inflate(R.layout.dialog_set_teams, null)
        playersNames = parentActivity.nomeJogadores!!.toTypedArray()
        player1 = newview?.findViewById(R.id.spinner_player1)
        player1Dropdown = newview?.findViewById(R.id.player1_dropdown)
        player1Dropdown?.dropDownWidth = (parentActivity.screen.fullContent.width - parentActivity.screen.fullContent.width * 0.1).roundToInt()
        player1Dropdown?.dropDownHeight = (parentActivity.screen.fullContent.height * 0.4).roundToInt()
        player1Dropdown?.inputType = InputType.TYPE_NULL
        player1Dropdown?.setDropDownBackgroundDrawable(ResourcesCompat.getDrawable(resources, R.drawable.dropdown_background, parentActivity.theme))
        player2 = newview?.findViewById(R.id.spinner_player2)
        player2Dropdown = newview?.findViewById(R.id.player2_dropdown)
        player2Dropdown?.dropDownWidth = (parentActivity.screen.fullContent.width - parentActivity.screen.fullContent.width * 0.1).roundToInt()
        player2Dropdown?.dropDownHeight = (parentActivity.screen.fullContent.height * 0.4).roundToInt()
        player2Dropdown?.inputType = InputType.TYPE_NULL
        player2Dropdown?.setDropDownBackgroundDrawable(ResourcesCompat.getDrawable(resources, R.drawable.dropdown_background, parentActivity.theme))
        player3 = newview?.findViewById(R.id.spinner_player3)
        player3Dropdown = newview?.findViewById(R.id.player3_dropdown)
        player3Dropdown?.dropDownWidth = (parentActivity.screen.fullContent.width - parentActivity.screen.fullContent.width * 0.1).roundToInt()
        player3Dropdown?.dropDownHeight = (parentActivity.screen.fullContent.height * 0.4).roundToInt()
        player3Dropdown?.inputType = InputType.TYPE_NULL
        player3Dropdown?.setDropDownBackgroundDrawable(ResourcesCompat.getDrawable(resources, R.drawable.dropdown_background, parentActivity.theme))
        player4 = newview?.findViewById(R.id.spinner_player4)
        player4Dropdown = newview?.findViewById(R.id.player4_dropdown)
        player4Dropdown?.dropDownWidth = (parentActivity.screen.fullContent.width - parentActivity.screen.fullContent.width * 0.1).roundToInt()
        player4Dropdown?.dropDownHeight = (parentActivity.screen.fullContent.height * 0.4).roundToInt()
        player4Dropdown?.inputType = InputType.TYPE_NULL
        player4Dropdown?.setDropDownBackgroundDrawable(ResourcesCompat.getDrawable(resources, R.drawable.dropdown_background, parentActivity.theme))
        continuar = newview?.findViewById(R.id.continuar)
        handleTeamSelectionButton()
    }

    /**
     * Método utilizado para popular o spinner com os jogadores disponíveis no banco de dados, para
     * auxiliar o jogador na separação dos times
     */
    private fun populatePlayer1Spinner() {
        val adapter = ArrayAdapter(parentActivity, R.layout.spinner_layout, R.id.textoSpin, playersNames!!)
        player1Dropdown!!.setAdapter(adapter)
        player1Dropdown!!.setText(player1Dropdown!!.adapter.getItem(0).toString(), false)
        parentActivity.playersId!![0] = 0
    }

    /**
     * Método utilizado para popular o spinner com os jogadores disponíveis no banco de dados, para
     * auxiliar o jogador na separação dos times
     */
    private fun populatePlayer2Spinner() {
        val adapter = ArrayAdapter(parentActivity, R.layout.spinner_layout, R.id.textoSpin, playersNames!!)
        player2Dropdown!!.setAdapter(adapter)
        player2Dropdown!!.setText(player2Dropdown!!.adapter.getItem(1).toString(), false)
        parentActivity.playersId!![1] = 1
    }

    /**
     * Método utilizado para popular o spinner com os jogadores disponíveis no banco de dados, para
     * auxiliar o jogador na separação dos times
     */
    private fun populatePlayer3Spinner() {
        val adapter = ArrayAdapter(parentActivity, R.layout.spinner_layout, R.id.textoSpin, playersNames!!)
        player3Dropdown!!.setAdapter(adapter)
        player3Dropdown!!.setText(player3Dropdown!!.adapter.getItem(2).toString(), false)
        parentActivity.playersId!![2] = 2
    }

    /**
     * Método utilizado para popular o spinner com os jogadores disponíveis no banco de dados, para
     * auxiliar o jogador na separação dos times
     */
    private fun populatePlayer4Spinner() {
        val adapter = ArrayAdapter(parentActivity, R.layout.spinner_layout, R.id.textoSpin, playersNames!!)
        player4Dropdown!!.setAdapter(adapter)
        player4Dropdown!!.setText(player4Dropdown!!.adapter.getItem(3).toString(), false)
        parentActivity.playersId!![3] = 3
    }

    /**
     * Método utilizado para lidar com uma seleção realizada nos spinners. Armazena a escolha feita
     * relativa a categoria e a pergunta, para posteriormente repassar para as classes que irão lidar
     * com essa informação
     */
    fun handleSpinnersClicks() {
        player1Dropdown!!.onItemClickListener = OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
            aux = parentActivity.playersId!![0]!!
            parentActivity.playersId!![0] = position
            player1Dropdown!!.setText(player1Dropdown!!.adapter.getItem(position).toString(), false)
            when {
                player2Dropdown!!.text.toString() == player2Dropdown!!.adapter.getItem(position).toString() -> {
                    player2Dropdown!!.setText(player2Dropdown!!.adapter.getItem(aux).toString(), false)
                    parentActivity.playersId!![1] = aux
                }
                player3Dropdown!!.text.toString() == player3Dropdown!!.adapter.getItem(position).toString() -> {
                    player3Dropdown!!.setText(player3Dropdown!!.adapter.getItem(aux).toString(), false)
                    parentActivity.playersId!![2] = aux
                }
                player4Dropdown!!.text.toString() == player4Dropdown!!.adapter.getItem(position).toString() -> {
                    player4Dropdown!!.setText(player4Dropdown!!.adapter.getItem(aux).toString(), false)
                    parentActivity.playersId!![3] = aux
                }
            }
        }
        player2Dropdown!!.onItemClickListener = OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
            aux = parentActivity.playersId!![1]!!
            parentActivity.playersId!![1] = position
            player2Dropdown!!.setText(player2Dropdown!!.adapter.getItem(position).toString(), false)
            when {
                player1Dropdown!!.text.toString() == player1Dropdown!!.adapter.getItem(position).toString() -> {
                    player1Dropdown!!.setText(player1Dropdown!!.adapter.getItem(aux).toString(), false)
                    parentActivity.playersId!![0] = aux
                }
                player3Dropdown!!.text.toString() == player3Dropdown!!.adapter.getItem(position).toString() -> {
                    player3Dropdown!!.setText(player3Dropdown!!.adapter.getItem(aux).toString(), false)
                    parentActivity.playersId!![2] = aux
                }
                player4Dropdown!!.text.toString() == player4Dropdown!!.adapter.getItem(position).toString() -> {
                    player4Dropdown!!.setText(player4Dropdown!!.adapter.getItem(aux).toString(), false)
                    parentActivity.playersId!![3] = aux
                }
            }
        }
        player3Dropdown!!.onItemClickListener = OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
            aux = parentActivity.playersId!![2]!!
            parentActivity.playersId!![2] = position
            player3Dropdown!!.setText(player3Dropdown!!.adapter.getItem(position).toString(), false)
            when {
                player1Dropdown!!.text.toString() == player1Dropdown!!.adapter.getItem(position).toString() -> {
                    player1Dropdown!!.setText(player1Dropdown!!.adapter.getItem(aux).toString(), false)
                    parentActivity.playersId!![0] = aux
                }
                player2Dropdown!!.text.toString() == player2Dropdown!!.adapter.getItem(position).toString() -> {
                    player2Dropdown!!.setText(player2Dropdown!!.adapter.getItem(aux).toString(), false)
                    parentActivity.playersId!![1] = aux
                }
                player4Dropdown!!.text.toString() == player4Dropdown!!.adapter.getItem(position).toString() -> {
                    player4Dropdown!!.setText(player4Dropdown!!.adapter.getItem(aux).toString(), false)
                    parentActivity.playersId!![3] = aux
                }
            }
        }
        player4Dropdown!!.onItemClickListener = OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
            aux = parentActivity.playersId!![3]!!
            parentActivity.playersId!![3] = position
            player4Dropdown!!.setText(player4Dropdown!!.adapter.getItem(position).toString(), false)
            when {
                player1Dropdown!!.text.toString() == player1Dropdown!!.adapter.getItem(position).toString() -> {
                    player1Dropdown!!.setText(player1Dropdown!!.adapter.getItem(aux).toString(), false)
                    parentActivity.playersId!![0] = aux
                }
                player2Dropdown!!.text.toString() == player2Dropdown!!.adapter.getItem(position).toString() -> {
                    player2Dropdown!!.setText(player2Dropdown!!.adapter.getItem(aux).toString(), false)
                    parentActivity.playersId!![1] = aux
                }
                player3Dropdown!!.text.toString() == player3Dropdown!!.adapter.getItem(position).toString() -> {
                    player3Dropdown!!.setText(player3Dropdown!!.adapter.getItem(aux).toString(), false)
                    parentActivity.playersId!![2] = aux
                }
            }
        }
    }

    /**
     * Método utilizado para lidar com cliques no botão "enviar", que é responsável por repassar a
     * categoria e pergunta selecionada pelo jogador para que seu oponente responda
     */
    private fun handleTeamSelectionButton() {
        continuar!!.setOnClickListener { parentActivity.startGame() }
    }
}