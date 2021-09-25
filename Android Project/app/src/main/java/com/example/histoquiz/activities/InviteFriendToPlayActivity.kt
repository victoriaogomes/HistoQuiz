package com.example.histoquiz.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.histoquiz.databinding.ActivityInviteFriendToPlayBinding
import com.example.histoquiz.util.FormFieldValidator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import java.util.*

/**
 * Classe utilizada para manipular os procedimentos necessários para enviar um convite de jogo
 * para um amigo
 */
class InviteFriendToPlayActivity : AppCompatActivity() {
    private var database: FirebaseFirestore? = null
    private var fieldValitator: FormFieldValidator? = null
    private var user: FirebaseUser? = null
    private lateinit var binding: ActivityInviteFriendToPlayBinding

    /**
     * Método executado no instante em que essa activity é criada, seta qual view será associada a
     * essa classe
     * @param savedInstanceState - contém o estado anteriormente salvo da atividade (pode ser nulo)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInviteFriendToPlayBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fieldValitator = FormFieldValidator(this)
        fieldValitator!!.monitorField(binding.friendEmailEDT)
        database = FirebaseFirestore.getInstance()
        user = FirebaseAuth.getInstance().currentUser
        //friendEmail.getEditText().setText("victoria.fo.f@hotmail.com");
        sendInviteToPlay()
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

    /**
     * Método utilizado para armazenar no firebase um convite para uma nova partida enviado pelo
     * usuário que está logado no momento. Primeiro, é verificado se o usuário é amigo do usuário
     * que possui o email informado e, caso seja, o convite é efetivamente enviado
     */
    private fun sendInviteToPlay() {
        binding.sendInviteBTN.setOnClickListener {
            if (fieldValitator!!.isFilled(binding.friendEmailEDT)) {
                val friendUID = database!!.collection("tabelaAuxiliar").document(binding.friendEmailEDT.editText?.text.toString()).get()
                friendUID.addOnSuccessListener { documentSnapshot: DocumentSnapshot ->
                    val friend = database!!.document("amizades/amigos").collection(
                        user!!.uid
                    ).whereEqualTo(FieldPath.documentId(), documentSnapshot["UID"]).get()
                    friend.addOnSuccessListener { queryDocumentSnapshots: QuerySnapshot ->
                        if (!queryDocumentSnapshots.isEmpty) {
                            val map = HashMap<String, Any>()
                            map["inviteAccepted"] = "não respondido"
                            database!!.document("partida/convites").collection(Objects.requireNonNull(documentSnapshot["UID"]).toString()).document(
                                user!!.uid
                            ).set(map).addOnSuccessListener {
                                addInviteResponseEventListener(
                                    Objects.requireNonNull(
                                        documentSnapshot["UID"]
                                    ).toString()
                                )
                            }
                            Toast.makeText(this@InviteFriendToPlayActivity, "Convite enviado com sucesso!", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this@InviteFriendToPlayActivity, "Você não é amigo desse usuário.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    /**
     * Método utilizado para verificar quando o usuário convidado para jogar responder ao usuário
     * que o convidou. Caso o usuário aceite, ele será redirecionado para a tela de jogo. Caso re-
     * cuse, uma mensagem será exibida para ele, informando o que ocorreu
     * @param friendUID - UID do amigo convidado para jogar
     */
    private fun addInviteResponseEventListener(friendUID: String) {
        val ref = database!!.document("partida/convites/" + friendUID + "/" + user!!.uid)
        ref.addSnapshotListener { documentSnapshot: DocumentSnapshot?, _: FirebaseFirestoreException? ->
            assert(documentSnapshot != null)
            if (documentSnapshot!!["inviteAccepted"] == "aceito") {
                val firestore = FirebaseFirestore.getInstance()
                firestore.document("partida/convites/$friendUID/" + (FirebaseAuth.getInstance().currentUser?.uid)).delete()
                val troca = Intent(this@InviteFriendToPlayActivity, GameActivity::class.java)
                troca.putExtra("matchCreator", true)
                troca.putExtra("opponentUID", friendUID)
                troca.putExtra("PCopponent", false)
                startActivityForResult(troca, 999)
            } else if (documentSnapshot["inviteAccepted"] == "recusado") {
                Toast.makeText(this@InviteFriendToPlayActivity, "Seu amigo recusou o convite para jogar.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}