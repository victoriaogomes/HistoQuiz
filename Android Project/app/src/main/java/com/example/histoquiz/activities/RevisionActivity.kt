package com.example.histoquiz.activities

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.histoquiz.R
import com.example.histoquiz.databinding.ActivityRevisionBinding
import com.example.histoquiz.model.Slide
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.util.*
import kotlin.coroutines.CoroutineContext


class RevisionActivity : AppCompatActivity(), CoroutineScope, View.OnClickListener {
    // Sistema selecionado para ver a revisão
    private var selectedSystem: String? = null
    private lateinit var screen: ActivityRevisionBinding
    private var id: Int = 1
    var slides = HashMap<Int, Slide>()
    var firestoreDatabase: FirebaseFirestore = FirebaseFirestore.getInstance()
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
            getData()// onResult is called on the main thread
            initGUI()
        }
    }

    private suspend fun getData() {
        screen.progress.visibility = View.VISIBLE
        screen.contentSection.visibility = View.GONE
        val ref = firestoreDatabase.collection("laminas").whereEqualTo("system", 0).get()
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
                        newTextView(item.value.name.toString(), "#FFFFFF", block.findViewById(R.id.cardContent), true)
                    }
                }
                screen.contentSection.addView(block)
                block = infoBlock(getString(R.string.sisRepFeminTitle))
                block.id = screen.contentSection.getChildAt(screen.contentSection.childCount - 1).id + 1
                newTextView("Lâminas contempladas no HistoQuiz:", "#FFFFFF", block.findViewById(R.id.cardContent))
                for (item in slides) {
                    if (item.value.code < 7){
                        newTextView(item.value.name.toString(), "#FFFFFF", block.findViewById(R.id.cardContent), true)
                    }
                }
                screen.contentSection.addView(block)
            }
            "sistema digestório" -> {
            }
            "sistema cardiopulmonar" -> {
            }
            "sistema osteomuscular" -> {
            }
        }
    }

    fun newTextView(text: String, color: String, contentPlace: RelativeLayout, underline: Boolean = false) {
        val textView: TextView = LayoutInflater.from(this).inflate(R.layout.special_textview, null) as TextView
        textView.text = text
        textView.id = this.id
        textView.setTextColor(Color.parseColor(color))
        if (underline) {
            textView.paintFlags = textView.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            textView.tag = text
            textView.setOnClickListener(this)
        }
        contentPlace.addView(textView, relativeLayoutParams(contentPlace.getChildAt(contentPlace.childCount - 1).id))
        this.id = this.id + 1
    }

    fun relativeLayoutParams(bellowId: Int): RelativeLayout.LayoutParams {
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
        Toast.makeText(this, v?.tag.toString(), Toast.LENGTH_SHORT).show()
    }
}