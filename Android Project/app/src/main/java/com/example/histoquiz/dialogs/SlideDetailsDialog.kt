package com.example.histoquiz.dialogs

import android.app.ActionBar
import android.app.Dialog
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.histoquiz.R
import com.example.histoquiz.activities.RevisionActivity
import com.example.histoquiz.util.GlideApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*
import kotlin.collections.ArrayList
import kotlin.coroutines.CoroutineContext
import kotlin.math.roundToInt

class SlideDetailsDialog(var parent: RevisionActivity, var slideCode: Int): AppCompatDialogFragment(), CoroutineScope, View.OnClickListener {

    var newview: View? = null
    var closeBTN: Button? = null
    var progress: ProgressBar? = null
    var progress2: ProgressBar? = null
    var fullContent: RelativeLayout? = null
    private var slideDescrip: ArrayList<String>? = null
    var position = 0
    private var myImageView: ImageView? = null
    private var imagesAmount = 0
    var next: ImageButton? = null
    private var imageSwitcher: ImageSwitcher? = null
    private var storageReference: StorageReference? = null
    private var job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

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
        launch {
            getData()// onResult is called on the main thread
        }

        return newDialog
    }

    fun initGUI() {
        newview = activity?.layoutInflater!!.inflate(R.layout.dialog_slide_details, null)
        position = 0
        progress = newview?.findViewById(R.id.progress)
        progress2 = newview?.findViewById(R.id.progress2)
        fullContent = newview?.findViewById(R.id.fullContent)
        next = newview?.findViewById(R.id.proximoButton)
        next?.tag = "NEXT"
        next?.setOnClickListener(this)
        closeBTN = newview?.findViewById(R.id.closeBTN)
        closeBTN?.tag = "GO_BACK"
        closeBTN?.setOnClickListener(this)
        newview?.findViewById<TextView>(R.id.nomeLamina)?.text = parent.slides[slideCode]?.name
        imageSwitcher = newview?.findViewById(R.id.imageSW)
        imageSwitcher?.setFactory {
            myImageView = ImageView(parent.applicationContext)
            myImageView!!.layoutParams =
                FrameLayout.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT)
            myImageView!!.scaleType = ImageView.ScaleType.FIT_CENTER
//            myImageView!!.scaleType = ImageView.ScaleType.FIT_XY
            myImageView
        }
        imageSwitcher?.outAnimation = AnimationUtils.loadAnimation(parent, android.R.anim.slide_out_right)
        imageSwitcher?.inAnimation = AnimationUtils.loadAnimation(parent, android.R.anim.slide_in_left)
    }

    private suspend fun getData() {
        imageToShow(0)
        progress?.visibility = View.VISIBLE
        fullContent?.visibility = View.GONE
        val ref = FirebaseFirestore.getInstance().collection("revisao").whereEqualTo("code", slideCode).get()
        for (documentSnapshot in ref.await().documents) {
            slideDescrip = documentSnapshot.get("descricao") as ArrayList<String>?
        }
        setContent()
        progress?.visibility = View.GONE
        fullContent?.visibility = View.VISIBLE
    }

    private fun setContent(){
        fullContent?.let { parent.id = it.childCount }
        if(slideDescrip != null){
            for (text in slideDescrip!!){
                fullContent?.let { parent.newTextView(text, "#FFFFFF", it) }
            }
        }
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.tag.toString()) {
                "NEXT" -> {
                    position++
                    imageToShow(position)
                }
                "GO_BACK" -> this.dismiss()
            }
        }
    }

    private fun imageToShow(position: Int) {
        progress2?.visibility = View.VISIBLE
        storageReference = parent.slides[slideCode]?.images?.get(position)?.let { FirebaseStorage.getInstance().getReference(it) }
        imagesAmount = parent.slides[slideCode]?.images?.size!!
        if (imagesAmount == 1 || position + 1 == imagesAmount) {
            this.position = -1
        }
        if (imagesAmount == 1) {
            next!!.visibility = View.GONE
        } else {
            next!!.visibility = View.VISIBLE
        }
        GlideApp.with(parent).load(storageReference).listener(object: RequestListener<Drawable> {
            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean
            ): Boolean {
                //progress?.visibility = View.GONE
                return false
            }
            override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean
            ): Boolean {
                progress2?.visibility = View.GONE
                return false
            }
        }).transition(DrawableTransitionOptions.withCrossFade()).into((imageSwitcher!!.currentView as ImageView))
    }


}