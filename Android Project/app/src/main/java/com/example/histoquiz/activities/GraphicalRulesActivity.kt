package com.example.histoquiz.activities

import android.app.ActionBar
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.histoquiz.databinding.ActivityGraphicalRulesBinding
import com.example.histoquiz.util.GlideApp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

class GraphicalRulesActivity : AppCompatActivity(), CoroutineScope, View.OnClickListener {

    private lateinit var screen: ActivityGraphicalRulesBinding
    private var myImageView: ImageView? = null
    private var imagesAmount = 0
    private var storageReference: StorageReference? = null
    var progress: ProgressBar? = null
    var position = 0
    private var job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen = ActivityGraphicalRulesBinding.inflate(layoutInflater)
        setContentView(screen.root)
        hideSystemUI()
        initGUI()
        imageToShow(0)
    }

    fun initGUI() {
        screen.imageSW.setFactory {
            myImageView = ImageView(this)
            myImageView!!.layoutParams = FrameLayout.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT)
            myImageView!!.scaleType = ImageView.ScaleType.FIT_CENTER
//            myImageView!!.scaleType = ImageView.ScaleType.FIT_XY
            myImageView
        }
        screen.imageSW.outAnimation = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right)
        screen.imageSW.inAnimation = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left)
        screen.nextBTN.setOnClickListener(this)
        screen.nextBTN.tag = "NEXT"
        screen.previousBTN.setOnClickListener(this)
        screen.previousBTN.tag = "PREVIOUS"
        screen.goBackTXT.setOnClickListener(this)
        screen.goBackTXT.tag = "GO_BACK"
    }

    private fun imageToShow(position: Int) {
        screen.progress.visibility = View.VISIBLE
        storageReference = FirebaseStorage.getInstance().getReference("Instrucoes/" + Integer.toString(position + 1) + ".png")
        imagesAmount = 32
        if(position + 1 == imagesAmount){
            this.position = 0;
            screen.nextBTN.visibility = View.INVISIBLE
            screen.previousBTN.visibility = View.VISIBLE
        }
        else if(position == 0){
            screen.previousBTN.visibility = View.INVISIBLE
            screen.nextBTN.visibility = View.VISIBLE
        }
        else{
            screen.previousBTN.visibility = View.VISIBLE
            screen.nextBTN.visibility = View.VISIBLE
        }
        GlideApp.with(this).load(storageReference).listener(object: RequestListener<Drawable> {
            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean
            ): Boolean {
                //progress?.visibility = View.GONE
                return false
            }
            override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean
            ): Boolean {
                screen.progress.visibility = View.GONE
                return false
            }
        }).transition(DrawableTransitionOptions.withCrossFade()).into((screen.imageSW.currentView as ImageView))
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

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.tag.toString()) {
                "NEXT" -> {
                    position++
                    imageToShow(position)
                }
                "PREVIOUS" -> {
                    position--
                    imageToShow(position)
                }
                "GO_BACK" -> this.finish()
            }
        }
    }
}