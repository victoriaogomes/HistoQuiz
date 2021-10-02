package com.lenda.histoquiz.dialogs

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.text.InputType
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
import com.lenda.histoquiz.R
import com.lenda.histoquiz.activities.GameActivity
import com.google.android.material.textfield.TextInputLayout
import java.text.Collator
import java.util.*
import kotlin.math.roundToInt

/**
 * Classe utilizada para exibir ao usuário as categorias de perguntas disponíveis no banco de dados,
 * bem como todas as perguntas relacionadas a cada uma delas, para que ele selecione uma e envie
 * para seu oponente responder
 */
class SelectQuestionDialog
/**
 * Método construtor da classe, recebe como parâmetro a activity que instanciou esse dialog
 * @param parentActivity - activity do tipo GameActivity, que é responsável por gerenciar
 * partidas e que criou esse dialog
 */(private var parentActivity: GameActivity) : AppCompatDialogFragment() {
    private var send: Button? = null
    var categories: TextInputLayout? = null
    private var questions: TextInputLayout? = null
    private var categoryNames: Array<String>? = null
    private var question = 0
    private var category = 0
    var categoriesDropdown: AutoCompleteTextView? = null
    var questionsDropdown: AutoCompleteTextView? = null
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
        newDialog.setTitle("")
        newDialog.window!!
            .setLayout(
                (parentActivity.screen.fullContent.width - parentActivity.screen.fullContent.width * 0.018).roundToInt(),
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
        newDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        populateCategoriesSpinner()
        populateQuestionSpinner(0)
        handleSpinnersClicks()
        handleQuestionSelectionButton()
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
        newview = activity?.layoutInflater!!.inflate(R.layout.dialog_choose_question, null)
        categories = newview?.findViewById(R.id.spinner_categorias)
        questions = newview?.findViewById(R.id.spinner_perguntas)
        send = newview?.findViewById(R.id.enviar)
        categoriesDropdown = newview?.findViewById(R.id.categorias_dropdown)
        questionsDropdown = newview?.findViewById(R.id.perguntas_dropdown)
        categoriesDropdown?.dropDownWidth = (parentActivity.screen.fullContent.width - parentActivity.screen.fullContent.width * 0.1).roundToInt()
        categoriesDropdown?.dropDownHeight = (parentActivity.screen.fullContent.height * 0.4).roundToInt()
        categoriesDropdown?.inputType = InputType.TYPE_NULL
        categoriesDropdown?.setDropDownBackgroundDrawable(ResourcesCompat.getDrawable(resources, R.drawable.dropdown_background, parentActivity
            .theme))
        questionsDropdown?.dropDownWidth = (parentActivity.screen.fullContent.width - parentActivity.screen.fullContent.width * 0.1).roundToInt()
        questionsDropdown?.dropDownHeight = (parentActivity.screen.fullContent.height * 0.4).roundToInt()
        questionsDropdown?.setDropDownBackgroundDrawable(ResourcesCompat.getDrawable(resources, R.drawable.dropdown_background, parentActivity.theme))
        questionsDropdown?.inputType = InputType.TYPE_NULL
    }

    /**
     * Método utilizado para popular o spinner com as categorias disponíveis no banco de dados de
     * forma alfabética, para que o usuário selecione uma e, em seguida, visualize as perguntas re-
     * lacionadas a ela
     */
    private fun populateCategoriesSpinner() {
        categoryNames = parentActivity.perguntas!!.keys.toTypedArray()
        Arrays.sort(categoryNames!!) { o1: String?, o2: String? ->
            val usCollator = Collator.getInstance(Locale("pt", "BR"))
            usCollator.compare(o1, o2)
        }
        val adapter = ArrayAdapter(parentActivity, R.layout.spinner_layout, R.id.textoSpin, categoryNames!!)
        categoriesDropdown!!.setAdapter(adapter)
        categoriesDropdown!!.setText(categoriesDropdown!!.adapter.getItem(0).toString(), false)
        category = 0
        parentActivity.category = 0
    }

    /**
     * Método utilizado para popular o spinner com as perguntas disponíveis no banco de dados, de
     * forma alfabética, relativas a categoria que ele selecionou anteriormente
     */
    private fun populateQuestionSpinner(category: Int) {
        val questionTexts = parentActivity.perguntas!![categoryNames?.get(category)]?.keys?.toTypedArray()
        Arrays.sort(questionTexts!!) { o1: String?, o2: String? ->
            val usCollator = Collator.getInstance(Locale("pt", "BR"))
            usCollator.compare(o1, o2)
        }
        val adapter2 = ArrayAdapter(parentActivity, R.layout.spinner_layout, R.id.textoSpin, questionTexts)
        questionsDropdown!!.setAdapter(adapter2)
        questionsDropdown!!.setText(questionsDropdown!!.adapter.getItem(0).toString(), false)
        question = 0
        parentActivity.question = 0
    }

    /**
     * Método utilizado para lidar com uma seleção realizada nos spinners. Armazena a escolha feita
     * relativa a categoria e a pergunta, para posteriormente repassar para as classes que irão lidar
     * com essa informação
     */
    fun handleSpinnersClicks() {
        categoriesDropdown!!.onItemClickListener = OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
            category = position
            categoriesDropdown!!.setText(categoriesDropdown!!.adapter.getItem(position).toString(), false)
            populateQuestionSpinner(position)
        }
        questionsDropdown!!.onItemClickListener = OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
            question = position
            questionsDropdown!!.setText(questionsDropdown!!.adapter.getItem(position).toString(), false)
        }
    }

    /**
     * Método utilizado para lidar com cliques no botão "enviar", que é responsável por repassar a
     * categoria e pergunta selecionada pelo jogador para que seu oponente responda
     */
    private fun handleQuestionSelectionButton() {
        send!!.setOnClickListener {
            if (parentActivity.pcOpponent) {
                parentActivity.category = category
                parentActivity.question = question
            } else {
                parentActivity.onlineOpponent!!.category = category
                parentActivity.onlineOpponent!!.question = question
            }
            parentActivity.handleQuestionSelectionButton()
        }
    }
}