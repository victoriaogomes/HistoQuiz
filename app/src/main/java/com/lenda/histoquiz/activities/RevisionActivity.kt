package com.lenda.histoquiz.activities

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.lenda.histoquiz.R
import com.lenda.histoquiz.databinding.ActivityRevisionBinding
import com.lenda.histoquiz.dialogs.SlideDetailsDialog
import com.lenda.histoquiz.model.Slide
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.util.*
import kotlin.coroutines.CoroutineContext


class RevisionActivity : AppCompatActivity(), CoroutineScope, View.OnClickListener {
    // Sistema selecionado para ver a revisão
    private var selectedSystem: String? = null
    lateinit var screen: ActivityRevisionBinding
    var id: Int = 1
    var slides = HashMap<Int, Slide>()
    private var firestoreDatabase: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen = ActivityRevisionBinding.inflate(layoutInflater)
        setContentView(screen.root)
        val intent = intent
        selectedSystem = intent.getStringExtra("selectedSystem")
        launch {
            getData()
            initGUI()
        }
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

    private suspend fun getData() {
        screen.progress.visibility = View.VISIBLE
        screen.contentSection.visibility = View.GONE
        var systemId: Int? = null
        when (selectedSystem?.lowercase()) {
            "sistema reprodutor" -> { systemId = 0 }
            "sistema digestório" -> { systemId = 2 }
            "sistema cardiopulmonar" -> { systemId = 1 }
            "sistema osteomuscular" -> { systemId = 3 }
        }
        val ref = firestoreDatabase.collection("laminas").whereEqualTo("system", systemId).get()
        for (documentSnapshot in ref.await().documents) {
            val slide = documentSnapshot.toObject(Slide::class.java)
            slide?.name = documentSnapshot.id
            slides[slide!!.code] = slide
        }
        screen.progress.visibility = View.GONE
        screen.contentSection.visibility = View.VISIBLE
    }

    fun initGUI() {
        screen.systemTitle.text = selectedSystem
        when (selectedSystem?.lowercase()) {
            "sistema reprodutor" -> {
                screen.systemDescription.text = getString(R.string.sistRepDescricao)
                var block = infoBlock(getString(R.string.sisRepMascTitle))
                block.id = screen.contentSection.getChildAt(screen.contentSection.childCount - 1).id + 1
                newTextView("Lâminas contempladas no HistoQuiz:", "#FFFFFF", block.findViewById(R.id.cardContent))
                for (item in slides) {
                    if (item.value.code >= 7){
                        newTextView(item.value.name.toString(), "#FFFFFF", block.findViewById(R.id.cardContent), true, item.value.code)
                    }
                }
                screen.contentSection.addView(block)
                block = infoBlock(getString(R.string.sisRepFeminTitle))
                block.id = screen.contentSection.getChildAt(screen.contentSection.childCount - 1).id + 1
                newTextView("Lâminas contempladas no HistoQuiz:", "#FFFFFF", block.findViewById(R.id.cardContent))
                for (item in slides) {
                    if (item.value.code < 7){
                        newTextView(item.value.name.toString(), "#FFFFFF", block.findViewById(R.id.cardContent), true, item.value.code)
                    }
                }
                screen.contentSection.addView(block)
            }
            "sistema digestório" -> {
                id = screen.mainRelLayout.childCount
                newTextView("Lâminas contempladas no HistoQuiz:", "#FFFFFF", screen.mainRelLayout)
                for (item in slides) {
                    newTextView(item.value.name.toString(), "#FFFFFF", screen.mainRelLayout, true, item.value.code)
                }
                //screen.contentSection.addView(block)
            }
            "sistema cardiopulmonar" -> {
                id = screen.mainRelLayout.childCount
                newTextView("Lâminas contempladas no HistoQuiz:", "#FFFFFF", screen.mainRelLayout)
                for (item in slides) {
                    newTextView(item.value.name.toString(), "#FFFFFF", screen.mainRelLayout, true, item.value.code)
                }
            }
            "sistema osteomuscular" -> {
                screen.systemDescription.text = getString(R.string.sistOsteoDescricao)
                val block = infoBlock("Lâminas")
                block.id = screen.contentSection.getChildAt(screen.contentSection.childCount - 1).id + 1
                newTextView("Lâminas contempladas no HistoQuiz:", "#FFFFFF", block.findViewById(R.id.cardContent))
                for (item in slides) {
                    newTextView(item.value.name.toString(), "#FFFFFF", block.findViewById(R.id.cardContent), true, item.value.code)
                }
                screen.contentSection.addView(block)
            }
        }
    }

    fun newTextView(text: String, color: String, contentPlace: RelativeLayout, underline: Boolean = false, slideCode: Int = -1) {
        val textView: TextView = LayoutInflater.from(this).inflate(R.layout.special_textview, null) as TextView
        textView.text = text
        textView.id = this.id
        textView.setTextColor(Color.parseColor(color))
        if (underline) {
            textView.paintFlags = textView.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            textView.tag = slideCode
            textView.setOnClickListener(this)
        }
        contentPlace.addView(textView, relativeLayoutParams(contentPlace.getChildAt(contentPlace.childCount - 1).id))
        this.id = this.id + 1
    }

    private fun relativeLayoutParams(bellowId: Int): RelativeLayout.LayoutParams {
        val params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        params.addRule(RelativeLayout.BELOW, bellowId)
        return params
    }

    private fun infoBlock(title: String): CardView {
        val infoBlockView: CardView = LayoutInflater.from(this).inflate(R.layout.revision_block, screen.contentSection, false) as CardView
        infoBlockView.findViewById<TextView>(R.id.title).text = title
        id = infoBlockView.findViewById<RelativeLayout>(R.id.cardContent).childCount
        return infoBlockView
    }

    override fun onClick(v: View?) {
        SlideDetailsDialog(this, v?.tag.toString().toInt()).show(supportFragmentManager, "slide detail")
    }
}