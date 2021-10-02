package com.lenda.histoquiz.activities

import android.app.ActionBar
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.lenda.histoquiz.databinding.ActivityLocalGameBinding
import com.lenda.histoquiz.dialogs.EndGameDialogv2
import com.lenda.histoquiz.dialogs.GuessSlideDialogv2
import com.lenda.histoquiz.dialogs.SetTeamsDialog
import com.lenda.histoquiz.model.Slide
import com.lenda.histoquiz.util.GlideApp
import com.lenda.histoquiz.util.LocalOpponent
import com.lenda.histoquiz.util.RoomCreator
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.lenda.histoquiz.R
import java.util.*

class LocalGameActivity : AppCompatActivity(), View.OnClickListener {
    private var systemCode = 0
    private var slideAmount = 0
    var roundTime = 0
    var firestoreDatabase: FirebaseFirestore? = null
    var countDownTimer: CountDownTimer? = null
    private var storageReference: StorageReference? = null
    private var imagesAmount = 0
    private var started = false
    var position = 0
    lateinit var screen: ActivityLocalGameBinding

    @Volatile
    var questionsDone = false
    @Volatile
    var slidesDone = false
    var roomCreationStatus = "not created"
    private var questionsFirebase: HashMap<String, Map<String, Any>>? = null


    var slides = HashMap<Int, Slide>()
    var matchSlides: MutableMap<Int, Slide> = HashMap()
    var actualSlide = 0
    var creator: RoomCreator? = null
    var matchCreator = false
    var nomeJogadores: ArrayList<String>? = null
    var uidJogadores: ArrayList<String>? = null
    var playersId: Array<Int?>? = null
    var localOpponent: LocalOpponent? = null
    private var roomName: String? = null
    private var setTeamsDialog: SetTeamsDialog? = null
    private var myImageView:ImageView? = null
    private var docIdRef: DocumentReference? = null

    // Dialogs utilizados para exibir as "subtelas" necessárias no jogo
    private var guessSlideDialog: GuessSlideDialogv2? = null
    private var endGameDialog: EndGameDialogv2? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        systemCode = intent.getIntExtra("systemCode", -1)
        slideAmount = intent.getIntExtra("slidesAmount", 3)
        roundTime = intent.getIntExtra("roundTime", 120)
        matchCreator = intent.getBooleanExtra("matchCreator", false)
        roomName = intent.getStringExtra("roomCode")
        screen = ActivityLocalGameBinding.inflate(layoutInflater)
        setContentView(screen.root)
        initGUI()
        getSlides()
        questions
        createRoomName()
        countDownTimer = object : CountDownTimer(1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                if (roomCreationStatus == "create another") {
                    roomCreationStatus = "not created"
                    countDownTimer!!.start()
                } else if (roomCreationStatus == "room created") {
                    if (!(questionsDone && slidesDone)) {
                        countDownTimer!!.start()
                    } else {
                        if (matchCreator) {
                            val dados: MutableMap<String, Any?> = HashMap()
                            dados["creatorUID"] = FirebaseAuth.getInstance().uid
                            dados["qntd"] = "1"
                            dados["nomeJogadores"] = ArrayList(listOf(FirebaseAuth.getInstance().currentUser!!.displayName, "", "", ""))
                            dados["uidJogadores"] = ArrayList(listOf(FirebaseAuth.getInstance().currentUser!!.uid, "", "", ""))
                            dados["playersId"] = ArrayList(listOf(1, 1, 1, 1))
                            creator!!.actualRoomName?.let { firestoreDatabase!!.collection("partidaLocal").document(it).set(dados) }
                        }
                        screen.roomCodeTXT.text = String.format("Cód. da sala: %s", creator!!.actualRoomName)
                        addRoomFilled()
                    }
                }
            }
        }.start()
    }

    /**
     * Método utilizado para obter uma referência para os elementos da view que está sendo exibida,
     * que serão utilizados para mudar algumas de suas configurações. Além disso, inicializa algumas
     * variáveis que serão utilizadas.
     */
    fun initGUI() {
        position = 0
        actualSlide = 0
        setTeamsDialog = SetTeamsDialog(this)
        playersId = arrayOfNulls(4)
        creator = RoomCreator()
        setFeedbackText("Aguardando configuração dos demais participantes...")
        screen.slideVisibToggleBTN.setOnClickListener(this)
        screen.slideVisibToggleBTN.tag = "OCULTAR_LAMINA"
        firestoreDatabase = FirebaseFirestore.getInstance()
        screen.nextImgBTN.setOnClickListener(this)
        screen.nextImgBTN.tag = "NEXT"
        screen.previousImgBTN.setOnClickListener(this)
        screen.previousImgBTN.tag = "PREVIOUS"
        screen.imageSW.setFactory {
            myImageView = ImageView(applicationContext)
            myImageView!!.layoutParams =
                FrameLayout.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT)
            myImageView!!.scaleType = ImageView.ScaleType.FIT_CENTER
            myImageView
        }
        screen.imageSW.outAnimation = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right)
        screen.imageSW.inAnimation = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left)
        endGameDialog = EndGameDialogv2(this)
    }

    /**
     * Método utilizado para obter do firebase as perguntas que estão cadastradas no banco de dados
     */
    private val questions: Unit
        get() {
            firestoreDatabase!!.collection("perguntas").get().addOnSuccessListener { queryDocumentSnapshots: QuerySnapshot ->
                questionsFirebase = HashMap()
                for (document in queryDocumentSnapshots) {
                    questionsFirebase!![document.id] = document.data
                }
                questionsDone = true
            }
        }

    /**
     * Método utilizado para obter do firebase as lâminas disponíveis para serem utilizadas durante
     * essa partida
     */
    private fun getSlides() {
        val listener = OnSuccessListener { queryDocumentSnapshots: QuerySnapshot ->
            for (documentSnapshot in queryDocumentSnapshots) {
                val slide = documentSnapshot.toObject(Slide::class.java)
                slide.name = documentSnapshot.id
                slides[slide.code] = slide
            }
            if (matchCreator) raffleSlides(queryDocumentSnapshots.size(), slides, slideAmount)
            slidesDone = true
        }
        if (systemCode == -1) {
            firestoreDatabase!!.collection("laminas").get().addOnSuccessListener(listener)
        } else {
            val slidesRef = firestoreDatabase!!.collection("laminas")
            slidesRef.whereEqualTo("system", systemCode).get().addOnSuccessListener(listener)
        }
    }

    /**
     * Método utilizado para lidar com os cliques no dialog que exibe as imagens, que é o botão de
     * "voltar", para fechar a exibição da imagem da lâmina, e o botão "próximo", que aparece somen-
     * te quando a lâmina possui mais de uma foto cadastrada no banco de dados
     * @param v - view que recebeu o clique do usuário
     */
    override fun onClick(v: View) {
        when (v.tag.toString()) {
            "NEXT" -> if (position != slideAmount - 1) {
                position++
                imageToShow(position)
            }
            "PREVIOUS" -> if (position != 0) {
                position--
                imageToShow(position)
            }
            "OCULTAR_LAMINA" -> {
                screen.slideVisibToggleBTN.text = getText(R.string.showLamina)
                screen.imageSW.visibility = View.INVISIBLE
                screen.nextImgBTN.visibility = View.INVISIBLE
                screen.previousImgBTN.visibility = View.INVISIBLE
                screen.slideVisibToggleBTN.tag = "MOSTRAR_LAMINA"
            }
            "MOSTRAR_LAMINA" -> {
                imageToShow(0)
                screen.slideVisibToggleBTN.setText(R.string.hideLamina)
                screen.imageSW.visibility = View.VISIBLE
                screen.slideVisibToggleBTN.tag = "OCULTAR_LAMINA"
            }
        }
    }

    /**
     * Método utilizado para sortear as lâminas que este jogador deverá adivinhar e armazená-las
     * no firebase. Cada jogador é responsável por sortear suas lâminas e cadastrá-las na nuvem,
     * para que o seu oponente as obtenha
     */
    private fun raffleSlides(slidesAmount: Int, slides: HashMap<Int, Slide>, raffleNumber: Int) {
        val rndGenerator = Random()
        var raffledValue: Int
        val slidesValues: List<Slide> = ArrayList(slides.values)
        //Toast.makeText(this,"Laminas sorteadas: ", Toast.LENGTH_SHORT).show();
        var limit: Int = raffleNumber.coerceAtMost(slidesAmount)
        while (limit % 2 != 0) {
            limit--
        }
        for (i in 0 until limit) {
            raffledValue = rndGenerator.nextInt(slidesAmount)
            while (matchSlides.containsKey(slidesValues[raffledValue].code)) {
                raffledValue = rndGenerator.nextInt(slidesAmount)
            }
            matchSlides[slidesValues[raffledValue].code] = slidesValues[raffledValue]
            //Toast.makeText(this,slidesValues.get(raffledValue).getName(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Método que realiza efetivamente a exibição da imagem presente na posição recebida por parâ-
     * metro (primeira imagem de uma lâmina, segunda imagem, etc...)
     * @param position - posição da foto a ser exibida (1ª, 2ª, 3ª, ...)
     */
    fun imageToShow(position: Int) {
        val aux: Int
        val keySet: Array<Any> = matchSlides.keys.toTypedArray()
        aux = keySet[actualSlide] as Int
        storageReference = matchSlides[aux]?.images?.get(position)?.let { FirebaseStorage.getInstance().getReference(it) }
        imagesAmount = matchSlides[aux]?.images?.size!!
        if (imagesAmount == 1 || position + 1 == imagesAmount) {
            screen.nextImgBTN.visibility = View.INVISIBLE
        } else if (position == 0) {
            screen.previousImgBTN.visibility = View.INVISIBLE
        }
        if (imagesAmount == 1) {
            screen.nextImgBTN.visibility = View.INVISIBLE
            screen.previousImgBTN.visibility = View.INVISIBLE
        } else {
            screen.nextImgBTN.visibility = View.VISIBLE
            screen.previousImgBTN.visibility = View.VISIBLE
        }
        screen.imageSW.visibility = View.VISIBLE
        screen.slideVisibToggleBTN.visibility = View.VISIBLE
        GlideApp.with(this).load(storageReference).into((screen.imageSW.currentView as ImageView))
    }

    @Suppress("UNCHECKED_CAST")
    fun addRoomFilled() {
        creator!!.actualRoomName?.let { firestoreDatabase!!.collection("partidaLocal").document(it) }
            ?.addSnapshotListener { documentSnapshot: DocumentSnapshot?, _: FirebaseFirestoreException? ->
                assert(documentSnapshot != null)
                if (documentSnapshot!!["qntd"] == "4") {
                    nomeJogadores = documentSnapshot["nomeJogadores"] as ArrayList<String>?
                    uidJogadores = documentSnapshot["uidJogadores"] as ArrayList<String>?
                    if (matchCreator) {
                        if (!started) {
                            if (nomeJogadores!![3].isNotEmpty()) {
                                setTeamsDialog!!.show(supportFragmentManager, "choose teams dialog")
                                started = true
                            }
                        }
                    } else {
                        if (localOpponent == null) {
                            Toast.makeText(this@LocalGameActivity, "Aguarde enquanto o criador da partida separa os times!", Toast.LENGTH_LONG).show()
                            startGame()
                        }
                    }
                } else {
                    Toast.makeText(
                        this@LocalGameActivity, String.format(
                            "Faltam %s jogadores para iniciarmos a partida.", 4 - Objects.requireNonNull(
                                documentSnapshot["qntd"]
                            ).toString().toInt()
                        ), Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun createRoomName() {
        if (matchCreator) {
            docIdRef = firestoreDatabase!!.collection("partidaLocal").document(creator!!.newRoomCode(6))
            docIdRef!!.get().addOnCompleteListener { task: Task<DocumentSnapshot?> ->
                if (task.isSuccessful) {
                    val document = task.result!!
                    roomCreationStatus = if (document.exists()) {
                        "create another"
                    } else {
                        "room created"
                    }
                } else {
                    Log.d("Erro criando a sala", "Failed with: ", task.exception)
                }
            }
        } else {
            creator!!.actualRoomName = roomName
            roomCreationStatus = "room created"
        }
    }

    fun startGame() {
        if (matchCreator) {
            setTeamsDialog!!.dismiss()
        }
        if (localOpponent == null) {
            localOpponent = creator!!.actualRoomName?.let { LocalOpponent(this, matchCreator, it) }
        }
    }

    /**
     * Método utilizado para exibir ao usuário a tela para que ele adivinhe a sua lâmina
     * atual
     */
    fun showGuessSlide() {
        guessSlideDialog = GuessSlideDialogv2(this)
        guessSlideDialog!!.show(supportFragmentManager, "guess dialog")
        //        Toast.makeText(this, "Exibir tela de escolher lamina", Toast.LENGTH_LONG).show();
    }

    /**
     * Método utilizado para obter o score de um determinado player na partida atual
     * @param player - número correspondente ao jogador (1 ou 2)
     * @return - pontuação do jogador selecionado
     */
    fun getPlayerScore(player: Int): Int {
        return when (player) {
            1 -> screen.myTeamPontTXT.text.toString().toInt()
            2 -> screen.myOppTeamPontTXT.text.toString().toInt()
            else -> 0
        }
    }

    /**
     * Método utilizado para modificar a pontuação de determinado player. Ele soma a pontuação atual
     * o valor recebido em "pontuation", que pode corresponder a um acréscimo (caso pontuation > 0)
     * ou um decréscimo (caso pontuation < 0)
     * @param player - jogador cuja pontuação deve ser modificada (1 ou 2)
     * @param pontuation - valor que deve ser adicionado (ou decrementado) a pontuação atual do player
     */
    fun changePlayerScore(player: Int, pontuation: Int) {
        when (player) {
            1 -> if (getPlayerScore(1) + pontuation > 0) {
                screen.myTeamPontTXT.text = String.format(Locale.getDefault(), "%d", getPlayerScore(1) + pontuation)
            } else {
                screen.myTeamPontTXT.text = "0"
            }
            2 -> if (getPlayerScore(2) + pontuation > 0) {
                screen.myOppTeamPontTXT.text = String.format(Locale.getDefault(), "%d", getPlayerScore(2) + pontuation)
            } else {
                screen.myOppTeamPontTXT.text = "0"
            }
        }
    }

    /**
     * Método utilizado para deixar de exibir ao usuário a tela para que ele adivinhe sua lâmina
     * atual
     */
    fun closeGuessSlide() {
        guessSlideDialog!!.dismiss()
    }

    /**
     * Método utilizado para salvar no firebase os dados relativos a performance desse jogador
     * em relação a determinada lâmina
     * @param system - código que representa o sistema ao qual essa lâmina pertence
     * @param situation - informa se a lâmina foi errada (0) ou acertada (1)
     */
    @Suppress("UNCHECKED_CAST")
    fun computePerformance(system: Int, situation: Int) {
        val sisName = arrayOf("")
        firestoreDatabase!!.collection("sistemas").whereEqualTo("code", system).get().addOnSuccessListener { queryDocumentSnapshots: QuerySnapshot ->
            val ref = FirebaseAuth.getInstance().currentUser?.let { firestoreDatabase!!.collection("desempenho").document(it.uid) }
            sisName[0] = "sis" + queryDocumentSnapshots.documents[0].id
            ref?.get()?.addOnSuccessListener { documentSnapshot: DocumentSnapshot ->
                val info = documentSnapshot[sisName[0]] as ArrayList<Long>?
                if (info != null) {
                    when (situation) {
                        0 -> ref.update("sis" + queryDocumentSnapshots.documents[0].id, ArrayList(listOf(info[0] + 1, info[1])))
                        1 -> ref.update("sis" + queryDocumentSnapshots.documents[0].id, ArrayList(listOf(info[0], info[1] + 1)))
                    }
                }
            }
        }
    }

    /**
     * Método utilizado para salvar no firebase desse usuário que ele jogou mais uma partida e,
     * se ele houver ganhado, salvar essa informação também
     * @param winner - boolean que indica se o usuário ganhou a partida ou não
     * @param part - variável que indica se é para adicionar mais uma partida ao contador de partidas
     * desse usuário, ou se é para adicionar mais uma partida como ganha
     */
    fun saveMatchInfo(winner: Boolean, part: Int) {
        val ref = firestoreDatabase!!.document("desempenho/" + (FirebaseAuth.getInstance().currentUser?.uid))
        if (part == 0 || winner && part == 1) {
            ref.get().addOnSuccessListener { documentSnapshot: DocumentSnapshot ->
                if (part == 0) ref.update(
                    "numPartidas", Objects.requireNonNull(
                        documentSnapshot["numPartidas"]
                    ).toString().toInt() + 1
                ) else {
                    if (winner) {
                        ref.update("vitorias", Objects.requireNonNull(documentSnapshot["vitorias"]).toString().toInt() + 1)
                    }
                }
            }
        }
    }

    /**
     * Método utilizado para exibir ao jogador um fragmento contendo a pontuação de cada um dos
     * jogadores da partida que acabou de ser finalizada, informando quem é o ganhador
     */
    fun showEndGameDialog() {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.add(endGameDialog!!, "endGame").commitAllowingStateLoss()
    }

    /**
     * Método utilizado para exibir ou deixar invisível itens da interface com base em quem vai
     * jogar a próxima rodada
     * @param constants - um vetor de 2 posições: tendo o valor 0, é invisível, e 1 é visível
     * A primeira posição fala sobre visibilidade do botão de dica, e a segunda
     * sobre visibilidade da imagem da lâmina
     */
    fun changeItensVisibility(constants: IntArray) {
        if (constants[0] == 0) screen.tipBTN.visibility = View.INVISIBLE else if (constants[0] == 1) screen.tipBTN.visibility = View.VISIBLE
        if (constants[1] == 0) {
            screen.imageSW.visibility = View.INVISIBLE
            screen.nextImgBTN.visibility = View.INVISIBLE
            screen.previousImgBTN.visibility = View.INVISIBLE
            screen.slideVisibToggleBTN.visibility = View.INVISIBLE
        } else if (constants[1] == 1) imageToShow(0)
    }

    fun setFeedbackText(text: String?) {
        //roundFeedback.setVisibility(View.VISIBLE);
        screen.roundFeedbackTXT.text = text
    }
}