package com.example.histoquiz.dialogs

import android.app.ActionBar
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.DialogFragment
import com.example.histoquiz.R
import com.example.histoquiz.activities.GameActivity
import com.example.histoquiz.util.GlideApp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*
import kotlin.math.roundToInt

/**
 * Classe utilizada para exibir ao jogador imagem da lâmina atual que o seu oponente está tentando
 * adivinhar no momento, para auxiliá-lo no momento de responder os questionamentos enviados pelo
 * seu oponente
 */
class SlideImageDialog
/**
 * Método construtor da classe, recebe como parâmetro a activity que instanciou esse dialog
 * @param parent - activity do tipo GameActivity, que é responsável por gerenciar
 * partidas e que criou esse dialog
 */(var parent: GameActivity) : DialogFragment(), View.OnClickListener {
    var next: ImageButton? = null
    private var imageSwitcher: ImageSwitcher? = null
    private var goBack: Button? = null
    private var storageReference: StorageReference? = null
    var position = 0
    private var myImageView: ImageView? = null
    private var imagesAmount = 0
    private var newview: View? = null
    private var slideName: TextView? = null

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
        newview = activity?.layoutInflater!!.inflate(R.layout.dialog_slide_image, null)
        position = 0
        goBack = newview?.findViewById(R.id.voltar)
        goBack?.setOnClickListener(this)
        goBack?.tag = "GO_BACK"
        next = newview?.findViewById(R.id.proximoButton)
        next?.tag = "NEXT"
        next?.setOnClickListener(this)
        slideName = newview?.findViewById(R.id.nomeLamina)
        imageSwitcher = newview?.findViewById(R.id.imageSW)
        imageSwitcher?.setFactory {
            myImageView = ImageView(parent.applicationContext)
            myImageView!!.layoutParams =
                FrameLayout.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT)
            myImageView!!.scaleType = ImageView.ScaleType.FIT_CENTER
            myImageView
        }
        imageToShow(0)
        imageSwitcher?.outAnimation = AnimationUtils.loadAnimation(parent, android.R.anim.slide_out_right)
        imageSwitcher?.inAnimation = AnimationUtils.loadAnimation(parent, android.R.anim.slide_in_left)
    }

    /**
     * Método utilizado para lidar com os cliques no dialog que exibe as imagens, que é o botão de
     * "voltar", para fechar a exibição da imagem da lâmina, e o botão "próximo", que aparece somen-
     * te quando a lâmina possui mais de uma foto cadastrada no banco de dados
     * @param v - view que recebeu o clique do usuário
     */
    override fun onClick(v: View) {
        when (v.tag.toString()) {
            "NEXT" -> {
                position++
                imageToShow(position)
            }
            "GO_BACK" -> parent.closeSlideImages()
        }
    }

    /**
     * Método que realiza efetivamente a exibição da imagem presente na posição recebida por parâ-
     * metro (primeira imagem de uma lâmina, segunda imagem, etc...)
     * @param position - posição da foto a ser exibida (1ª, 2ª, 3ª, ...)
     */
    private fun imageToShow(position: Int) {
        val keySet: Array<Any>
        var aux = 0
        //Toast.makeText(parent, Arrays.toString(keySet), Toast.LENGTH_LONG).show();
        val slide: String
        if (parent.pcOpponent) {
            slide = parent.computerOpponent!!.slideToGuess
            keySet = parent.mySlides.keys.toTypedArray()
        } else {
            slide = parent.onlineOpponent!!.opponentSlideToGuess
            keySet = parent.onlineOpponent!!.opponentSlides.keys.toTypedArray()
        }
        when (slide) {
            "firstSlide" -> aux = if (parent.pcOpponent) keySet[3] as Int else keySet[0] as Int
            "secondSlide" -> aux = if (parent.pcOpponent) keySet[4] as Int else keySet[1] as Int
            "thirdSlide" -> aux = if (parent.pcOpponent) keySet[5] as Int else keySet[2] as Int
        }
        storageReference = parent.mySlides[aux]?.images?.get(position)?.let { FirebaseStorage.getInstance().getReference(it) }
        imagesAmount = parent.mySlides[aux]?.images?.size!!
        slideName!!.text = parent.mySlides[aux]?.let {
            String.format(
                Locale.getDefault(), "%s: foto %d de %d", it.name, position + 1, imagesAmount
            )
        }
        if (imagesAmount == 1 || position + 1 == imagesAmount) {
            this.position = -1
        }
        if (imagesAmount == 1) {
            next!!.visibility = View.GONE
        } else {
            next!!.visibility = View.VISIBLE
        }
        GlideApp.with(parent).load(storageReference).into((imageSwitcher!!.currentView as ImageView))
    }
}