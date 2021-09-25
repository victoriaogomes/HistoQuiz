package com.example.histoquiz.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.histoquiz.databinding.ActivityEnterLocalGameBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.util.*

class EnterLocalGameActivity : AppCompatActivity() {

    private var firestoreDatabase: FirebaseFirestore? = null
    private lateinit var screen: ActivityEnterLocalGameBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen = ActivityEnterLocalGameBinding.inflate(layoutInflater)
        setContentView(screen.root)
        initGUI()
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

    fun initGUI() {
        firestoreDatabase = FirebaseFirestore.getInstance()
        screen.joinRoomBTN.setOnClickListener {
            if (screen.roomCodeEDT.editText?.text.toString().isEmpty()) {
                Toast.makeText(this@EnterLocalGameActivity, "Informe um código de sala.", Toast.LENGTH_LONG).show()
            } else {
                joinGameRoom(screen.roomCodeEDT.editText!!.text.toString())
            }
        }
    }
    @Suppress("UNCHECKED_CAST")
    private fun joinGameRoom(roomCode: String?) {
        firestoreDatabase!!.collection("partidaLocal").whereEqualTo(FieldPath.documentId(), roomCode).get()
            .addOnCompleteListener { task: Task<QuerySnapshot?> ->
                if (task.isSuccessful) {
                    if (task.result?.documents?.isNotEmpty() == true) {
                        val document = task.result?.documents!![0]
                        if (document.exists()) {
                            val qntdPlayers = document["qntd"].toString().toInt()
                            if (qntdPlayers >= 4) {
                                Toast.makeText(this@EnterLocalGameActivity, "Essa sala já está cheia!", Toast.LENGTH_LONG).show()
                            } else {
                                val nomeJogadores = (document["nomeJogadores"] as ArrayList<String?>?)!!
                                nomeJogadores[qntdPlayers] = FirebaseAuth.getInstance().currentUser?.displayName
                                firestoreDatabase!!.collection("partidaLocal").document(document.id).update("nomeJogadores", nomeJogadores)
                                val uidJogadores = (document["uidJogadores"] as ArrayList<String>?)!!
                                uidJogadores[qntdPlayers] = FirebaseAuth.getInstance().currentUser!!.uid
                                firestoreDatabase!!.collection("partidaLocal").document(document.id).update("uidJogadores", uidJogadores)
                                firestoreDatabase!!.collection("partidaLocal").document(document.id).update(
                                    "qntd", (Objects.requireNonNull(
                                        document["qntd"]
                                    ).toString().toInt() + 1).toString()
                                )
                                val troca = Intent(this@EnterLocalGameActivity, LocalGameActivity::class.java)
                                troca.putExtra("matchCreator", false)
                                troca.putExtra("roomCode", roomCode)
                                startActivityForResult(troca, 999)
                            }
                        }
                    } else {
                        Toast.makeText(this@EnterLocalGameActivity, "Essa sala não existe!", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Log.d("Erro ao entrar na sala", "Erro: ", task.exception)
                }
            }
    }
}