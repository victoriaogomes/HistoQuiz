package com.example.histoquiz.dialogs

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
import com.example.histoquiz.R
import com.example.histoquiz.activities.LocalGameActivity
import com.google.android.material.textfield.TextInputLayout
import java.text.Collator
import java.util.*
import kotlin.math.roundToInt

/**
 * Classe utilizada para exibir o dialog que lista todas as possíveis lâminas disponíveis no siste-
 * ma, para que o usuário selecione qual ele imagina estar tentando adivinhar no momento
 */
class GuessSlideDialogv2
/**
 * Método construtor da classe, recebe como parâmetro a activity que instanciou esse dialog
 * @param parent - activity do tipo GameActivity, que é responsável por gerenciar partidas e
 * que criou esse dialog
 */(var parent: LocalGameActivity) : AppCompatDialogFragment() {
    private var guess: TextInputLayout? = null
    private var goBack: Button? = null
    private var send: Button? = null
    var slides: Array<String?>? = null
    private var slidesDropdown: AutoCompleteTextView? = null
    private var slideChoosed = ""
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
        dealWithButtons()
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
        populateSpinner()
        handleSpinnersClicks()
        return newDialog
    }

    /**
     * Método utilizado para obter a referência para certos objetos da view que serão manipulados
     * por essa classe e para inicializar alguns outros objetos utilizados aqui no back-end
     */
    fun initGUI() {
        activity?.layoutInflater!!.inflate(R.layout.dialog_guess_slide, null)
        guess = newview?.findViewById(R.id.spinner_slides)
        slidesDropdown = newview?.findViewById(R.id.slides_dropdown)
        slidesDropdown?.dropDownWidth = (parent.screen.fullContent.width - parent.screen.fullContent.width * 0.1).roundToInt()
        slidesDropdown?.dropDownHeight = (parent.screen.fullContent.height * 0.4).roundToInt()
        slidesDropdown?.inputType = InputType.TYPE_NULL
        slidesDropdown?.setDropDownBackgroundDrawable(ResourcesCompat.getDrawable(resources, R.drawable.dropdown_background, parent.theme))
        goBack = newview?.findViewById(R.id.voltar)
        send = newview?.findViewById(R.id.enviar)
    }

    /**
     * Método utilizado para popular o spinner com as possíveis lâminas disponíveis no banco de da-
     * dos de forma alfabética, para que o usuário selecione uma
     */
    private fun populateSpinner() {
        var i = 0
        slides = arrayOfNulls(parent.slides.size)
        for ((_, value) in parent.slides) {
            slides!![i] = value.name
            i++
        }
        Arrays.sort(slides!!) { o1: String?, o2: String? ->
            val usCollator = Collator.getInstance(Locale("pt", "BR"))
            usCollator.compare(o1, o2)
        }
        val adapter = ArrayAdapter(parent, R.layout.spinner_layout, R.id.textoSpin, slides!!)
        slidesDropdown!!.setAdapter(adapter)
        slideChoosed = slidesDropdown!!.adapter.getItem(0).toString()
        slidesDropdown!!.setText(slideChoosed, false)
    }

    private fun handleSpinnersClicks() {
        slidesDropdown!!.onItemClickListener = OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
            slideChoosed = slidesDropdown!!.adapter.getItem(position).toString()
            slidesDropdown!!.setText(slideChoosed, false)
        }
    }

    /**
     * Método utilizado para lidar com os cliques nos botões desse dialog ("enviar" ou "voltar")
     */
    private fun dealWithButtons() {
        send!!.setOnClickListener { parent.localOpponent!!.estadoC(slideChoosed) }
        goBack!!.setOnClickListener { }
    }
}