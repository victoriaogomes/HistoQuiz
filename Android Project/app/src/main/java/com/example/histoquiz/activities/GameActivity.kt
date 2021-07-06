package com.example.histoquiz.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.histoquiz.R
import com.example.histoquiz.databinding.ActivityGameBinding
import com.example.histoquiz.dialogs.*
import com.example.histoquiz.model.Slide
import com.example.histoquiz.util.ComputerOpponent
import com.example.histoquiz.util.OnlineOpponent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.text.Collator
import java.util.*

/**
 * Classe utilizada para manipular atividades relativas a uma partida que são comuns a todas as mo-
 * dalidades de jogo (online - 1 vs 1, contra o computador e local - 2 vs 2)
 */
class GameActivity : AppCompatActivity(), View.OnClickListener {
    var mySlides: MutableMap<Int, Slide?> = HashMap()
    var firestoreDatabase: FirebaseFirestore? = null
    var opponentUID: String? = null
    private var user: FirebaseUser? = null
    var matchCreator = false
    var pcOpponent = false
    var perguntas: HashMap<String, Map<String, Any>>? = null
    var slideToGuess = "firstSlide"
    var category = 0
    var question = 0
    var slides = HashMap<Int, Slide>()
    var countDownTimer: CountDownTimer? = null

    @Volatile
    var questionsDone = false
    @Volatile
    var slidesDone = false
    private var opponentSlidesButtons: Array<ImageButton>? = null
    private var opponentSlidesCheck: Array<ImageView>? = null
    private var mySlidesCheck: Array<ImageView>? = null

    //Instância de classes utilizadas para controlar especificidades de certos modos de jogo
    @JvmField
    var computerOpponent: ComputerOpponent? = null

    @JvmField
    var onlineOpponent: OnlineOpponent? = null

    // Dialogs utilizados para exibir as "subtelas" necessárias no jogo
    private var selectQuestionDialog: SelectQuestionDialog? = null
    private var guessSlideDialog: GuessSlideDialog? = null
    private var questionFeedBackDialog: QuestionFeedBackDialog? = null
    private var slideImageDialog: SlideImageDialog? = null
    private var endGameDialog: EndGameDialog? = null


    lateinit var screen: ActivityGameBinding

    /**
     * Método executado no instante em que essa activity é criada, seta qual view será associada a
     * essa classe
     * @param savedInstanceState - contém o estado anteriormente salvo da atividade (pode ser nulo)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen = ActivityGameBinding.inflate(layoutInflater)
        setContentView(screen.root)
        matchCreator = intent.getBooleanExtra("matchCreator", false)
        opponentUID = intent.getStringExtra("opponentUID")
        pcOpponent = intent.getBooleanExtra("PCopponent", false)
        initGUI()
        getSlides()
        questions
        countDownTimer = object : CountDownTimer(1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                if (!(questionsDone && slidesDone)) {
                    countDownTimer!!.start()
                } else {
                    if (!pcOpponent) { // Caso o oponente dessa partida NÃO seja o computador
                        onlineOpponent = opponentUID?.let { OnlineOpponent(this@GameActivity, it, matchCreator) }
                    } else {
                        computerOpponent = perguntas?.let { ComputerOpponent(this@GameActivity, it, slides) }
                    }
                }
            }
        }.start()
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            if (controller != null) {
                controller.hide(WindowInsetsCompat.Type.statusBars())
                controller.hide(WindowInsetsCompat.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }

    /**
     * Método utilizado para obter do firebase as perguntas que estão cadastradas no banco de dados
     */
    private val questions: Unit
        get() {
            firestoreDatabase!!.collection("perguntas").get().addOnSuccessListener { queryDocumentSnapshots: QuerySnapshot ->
                perguntas = HashMap()
                for (document in queryDocumentSnapshots) {
                    perguntas!![document.id] = document.data
                }
                questionsDone = true
            }
        }

    /**
     * Método utilizado para obter do firebase as lâminas disponíveis para serem utilizadas durante
     * essa partida
     */
    private fun getSlides() {
        firestoreDatabase!!.collection("laminas").get().addOnSuccessListener { queryDocumentSnapshots: QuerySnapshot ->
            for (documentSnapshot in queryDocumentSnapshots) {
                val slide = documentSnapshot.toObject(Slide::class.java)
                slide.name = documentSnapshot.id
                slides[slide.code] = slide
            }
            raffleSlides(queryDocumentSnapshots.size(), slides, 6)
            slidesDone = true
        }
    }

    /**
     * Método utilizado para obter a referência para certos objetos da view que serão manipulados
     * por essa classe e para inicializar alguns outros objetos utilizados aqui no back-end
     */
    fun initGUI() {
        opponentSlidesButtons = arrayOf(findViewById(R.id.oponenteLamina1), findViewById(R.id.oponenteLamina2), findViewById(R.id.oponenteLamina3))
        opponentSlidesCheck =
            arrayOf(findViewById(R.id.oponenteCheckSlide1), findViewById(R.id.oponenteCheckSlide2), findViewById(R.id.oponenteCheckSlide3))
        mySlidesCheck = arrayOf(findViewById(R.id.meuCheckSlide1), findViewById(R.id.meuCheckSlide2), findViewById(R.id.meuCheckSlide3))
        opponentSlidesButtons!![0].setOnClickListener(this)
        opponentSlidesButtons!![0].tag = "OPPONENT_SLIDE_BUTTON"
        opponentSlidesButtons!![1].setOnClickListener(this)
        opponentSlidesButtons!![1].tag = "OPPONENT_SLIDE_BUTTON"
        opponentSlidesButtons!![2].setOnClickListener(this)
        opponentSlidesButtons!![2].tag = "OPPONENT_SLIDE_BUTTON"
        screen.questionTXT.text = getString(R.string.configJogo)
        screen.yesBTN.visibility = View.INVISIBLE
        screen.yesBTN.tag = "YES_ANSWER"
        screen.yesBTN.setOnClickListener(this)
        screen.noBTN.visibility = View.INVISIBLE
        screen.noBTN.tag = "NO_ANSWER"
        screen.noBTN.setOnClickListener(this)
        user = FirebaseAuth.getInstance().currentUser
        firestoreDatabase = FirebaseFirestore.getInstance()
        selectQuestionDialog = SelectQuestionDialog(this)
        guessSlideDialog = GuessSlideDialog(this)
        endGameDialog = EndGameDialog(this)
    }

    /**
     * Método utilizado para exibir ao usuário a tela para que ele selecione uma questão para
     * enviar para seu oponente responder
     */
    fun showQuestionSelection() {
        selectQuestionDialog!!.show(supportFragmentManager, "choose question dialog")
    }

    /**
     * Método utilizado para deixar de exibir a tela utilizada para selecionar uma questão
     * para o oponente responder
     */
    fun closeQuestionSelection() {
        selectQuestionDialog!!.dismiss()
    }

    /**
     * Método utilizado para exibir ao usuário a tela para que ele adivinhe a sua lâmina
     * atual
     */
    fun showGuessSlide() {
        guessSlideDialog!!.show(supportFragmentManager, "guess dialog")
    }

    /**
     * Método utilizado para deixar de exibir ao usuário a tela para que ele adivinhe sua lâmina
     * atual
     */
    fun closeGuessSlide() {
        guessSlideDialog!!.dismiss()
    }

    /**
     * Método utilizado para exibir ao usuário a resposta a sua pergunta dada pelo seu oponente,
     * bem como a resposta correta dela
     */
    fun showQuestionFeedback(opponentAnswer: Boolean?, correctAnswer: Boolean) {
        questionFeedBackDialog = QuestionFeedBackDialog(this, opponentAnswer, correctAnswer)
        questionFeedBackDialog!!.show(supportFragmentManager, "questionFeedBack")
    }

    /**
     * Método utilizado para fechar o dialog responsável por fornecer ao jogador um feedback relati-
     * vo a resposta da pergunta que ele selecionou para enviar ao seu oponente
     */
    fun closeQuestionFeedback() {
        questionFeedBackDialog!!.dismiss()
    }

    /**
     * Método utilizado para exibir para o jogador a dialog contendo as imagens da lâmina que seu
     * oponente está tentando adivinhar no momento
     */
    private fun showSlideImages() {
        slideImageDialog = SlideImageDialog(this)
        slideImageDialog!!.show(supportFragmentManager, "slide")
    }

    /**
     * Método utilizado para fechar o dialog que exibe a imagem das lâminas que o oponente desse
     * jogador tenta adivinhar durante a partida
     */
    fun closeSlideImages() {
        slideImageDialog!!.dismiss()
    }

    /**
     * Método utilizado para exibir a mensagem recebida por parâmetro ao jogador
     * @param text - mensagem que deve ser exibida ao jogador
     */
    fun showTextToPlayer(text: String?) {
        screen.questionTXT.text = text
        screen.questionTXT.visibility = View.VISIBLE
        screen.yesBTN.visibility = View.INVISIBLE
        screen.noBTN.visibility = View.INVISIBLE
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
     * Método utilizado para sortear as lâminas que este jogador deverá adivinhar e armazená-las
     * no firebase. Cada jogador é responsável por sortear suas lâminas e cadastrá-las na nuvem,
     * para que o seu oponente as obtenha
     */
    private fun raffleSlides(slidesAmount: Int, slides: HashMap<Int, Slide>, raffleNumber: Int) {
        val rndGenerator = Random()
        var raffledValue: Int
        mySlides[41] = slides[41] // Fibrocartilagem
        mySlides[40] = slides[40] // Cartilagem hialina
        mySlides[42] = slides[42] // Cartilagem elástica
        mySlides[43] = slides[43] // Tecido ósseo
        mySlides[46] = slides[46] // Placa metafisária
        mySlides[44] = slides[44] // Osteócito

//        for (int i = 0; i < raffleNumber; i++) {
//            raffledValue = rndGenerator.nextInt(slidesAmount);
//            while (mySlides.containsKey(raffledValue)) {
//                raffledValue = rndGenerator.nextInt(slidesAmount);
//            }
//            mySlides.put(raffledValue, slides.get(raffledValue));
//        }
    }

    /**
     * Método chamado a partir do momento que a execução dessa atividade é iniciada
     * @param requestCode - código de solicitação fornecido pelo método startActivityForResult(),
     * permitindo identificar de quem esse resultado veio.
     * @param resultCode - código de resultado retornado pela atividade filho por meio do método
     * setResult()
     * @param data - dados que foram retornados para essa activity
     */
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 999) {
            if (resultCode == RESULT_OK) {
                matchCreator = data!!.getBooleanExtra("matchCreator", true)
                opponentUID = data.getStringExtra("opponentUID")
                pcOpponent = data.getBooleanExtra("PCopponent", true)
            }
        }
    }

    /**
     * Método que exibe na view a pergunta que o oponente enviou para este jogador responder
     * @param questionText - texto da questão a ser respondida
     */
    fun setQuestionForPlayerAnswer(questionText: String?) {
        screen.questionTXT.text = questionText
        screen.questionTXT.visibility = View.VISIBLE
        screen.yesBTN.visibility = View.VISIBLE
        screen.noBTN.visibility = View.VISIBLE
    }

    /**
     * Método utilizado para realizar uma transição de estados após o usuário selecionar a pergunta
     * que deseja enviar ao oponente
     */
    fun handleQuestionSelectionButton() {
        if (pcOpponent) computerOpponent!!.estadoG() else onlineOpponent!!.estadoF()
    }

    /**
     * Método utilizado para retornar o texto de uma questão selecionada
     * @param category - inteiro representado a categoria a qual a pergunta pertence
     * @param question - inteiro que representa a questão selecionada dentre as disponíveis nesta
     * categoria
     * @return - texto da questão selecionada
     */
    fun getQuestionText(category: Int, question: Int): String {
        val questions = perguntas!![getCategoryName(category)]?.keys?.toTypedArray()
        Arrays.sort(questions!!) { o1: String?, o2: String? ->
            val usCollator = Collator.getInstance(Locale("pt", "BR"))
            usCollator.compare(o1, o2)
        }
        return questions[question]
    }

    /**
     * Método utilizado para obter o nome da categoria associada a determinado número
     * @param category - número relativo a categoria cujo nome deseja-se obter
     * @return - nome da categoria associada ao inteiro recebido por parâmetro
     */
    fun getCategoryName(category: Int): String {
        val categories = perguntas!!.keys.toTypedArray()
        Arrays.sort(categories) { o1: String?, o2: String? ->
            val usCollator = Collator.getInstance(Locale("pt", "BR"))
            usCollator.compare(o1, o2)
        }
        return categories[category]
    }

    /**
     * Método utilizado para pegar a resposta da pergunta atual relativa a lâmina atual que foi ca-
     * dastrada no firebase
     * @param category - categoria da pergunta
     * @param question - pergunta selecionada na lista disponível para essa categoria
     * @param slide - lâmina relativa a qual a pergunta deve ser respondida
     * @return - retorna true caso a resposta seja verdadeira, e false caso contrário
     */
    // usado para suprimir o warning relativo ao cast no ArrayList
    @Suppress("UNCHECKED_CAST")
    fun getQuestionRealAnswer(category: Int, question: Int, slide: Int): Boolean {
        val respostas = (perguntas!![getCategoryName(category)]?.get(getQuestionText(category, question)) as ArrayList<Boolean>?)!!
        return respostas[slide]
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
            1 -> {
                if (getPlayerScore(1) + pontuation > 0) {
                    screen.scorePlayer1TXT.text = String.format(Locale.getDefault(), "%d", getPlayerScore(1) + pontuation)
                } else {
                    screen.scorePlayer1TXT.text = "0"
                }
                if (!pcOpponent) onlineOpponent!!.myRoomRef?.child("score")?.setValue(getPlayerScore(1))
            }
            2 -> {
                if (getPlayerScore(2) + pontuation > 0) {
                    screen.scorePlayer2TXT.text = String.format(Locale.getDefault(), "%d", getPlayerScore(2) + pontuation)
                } else {
                    screen.scorePlayer2TXT.text = "0"
                }
                if (!pcOpponent) onlineOpponent!!.opponentRoomRef?.child("score")?.setValue(getPlayerScore(2))
            }
        }
    }

    /**
     * Método utilizado para obter o score de um determinado player na partida atual
     * @param player - número correspondente ao jogador (1 ou 2)
     * @return - pontuação do jogador selecionado
     */
    fun getPlayerScore(player: Int): Int {
        return when (player) {
            1 -> screen.scorePlayer1TXT.text.toString().toInt()
            2 -> screen.scorePlayer2TXT.text.toString().toInt()
            else -> 0
        }
    }

    /**
     * Método utilizado para colocar o símbolo de ✓ no quadradinho correspondente a lâmina que foi
     * adivinhada
     * @param position - posição da lâmina adivinhada (0, 1 ou 2)
     * @param player - jogador cuja lâmina foi adivinhada (1 ou 2)
     */
    fun checkSlide(position: Int, player: Int) {
        when (player) {
            1 -> {
                mySlidesCheck?.get(position)?.visibility = View.VISIBLE
                when (position) {
                    0 -> if (pcOpponent) slideToGuess = "secondSlide" else {
                        onlineOpponent!!.myRoomRef?.child("slideToGuess")?.setValue("secondSlide")
                        onlineOpponent!!.mySlideToGuess = "secondSlide"
                    }
                    1 -> if (pcOpponent) slideToGuess = "thirdSlide" else {
                        onlineOpponent!!.myRoomRef?.child("slideToGuess")?.setValue("thirdSlide")
                        onlineOpponent!!.mySlideToGuess = "thirdSlide"
                    }
                    2 -> {
                    }
                }
            }
            2 -> {
                opponentSlidesCheck?.get(position)?.visibility = View.VISIBLE
                when (position) {
                    0 -> if (pcOpponent) slideToGuess = "secondSlide"
                    1 -> if (pcOpponent) slideToGuess = "thirdSlide"
                    2 -> {
                    }
                }
            }
        }
    }

    /**
     * Método utilizado para lidar com os cliques nos botões da interface do jogo
     * @param v - Objeto da view que recebeu o clique
     */
    override fun onClick(v: View) {
        when (v.tag.toString()) {
            "OPPONENT_SLIDE_BUTTON" -> showSlideImages()
            "YES_ANSWER" -> if (pcOpponent) {
                computerOpponent!!.estadoC(true)
            } else {
                onlineOpponent!!.estadoC(true)
            }
            "NO_ANSWER" -> if (pcOpponent) {
                computerOpponent!!.estadoC(false)
            } else {
                onlineOpponent!!.estadoC(false)
            }
            "BACK_MENU" -> {
                endGameDialog!!.dismiss()
                val troca = Intent(this, MenuActivity::class.java)
                startActivity(troca)
            }
        }
    }

    /**
     * Método utilizado para salvar no firebase os dados relativos a performance desse jogador
     * em relação a determinada lâmina
     * @param system - código que representa o sistema ao qual essa lâmina pertence
     * @param situation - informa se a lâmina foi errada (0) ou acertada (1)
     */
    // usado para suprimir o warning relativo ao cast no ArrayList
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
                        0 -> ref.update(
                            "sis" + queryDocumentSnapshots.documents[0].id, ArrayList(
                                listOf(
                                    info[0] + 1, info[1]
                                )
                            )
                        )
                        1 -> ref.update(
                            "sis" + queryDocumentSnapshots.documents[0].id, ArrayList(
                                listOf(
                                    info[0], info[1] + 1
                                )
                            )
                        )
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

    override fun onPause() {
        super.onPause()
        if (!pcOpponent) {
            if (!onlineOpponent!!.bothEnded) {
                onlineOpponent!!.realtimeDatabase.getReference("partida/jogo/" + onlineOpponent!!.roomName).child("matchState").setValue("killed")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!pcOpponent) {
            if (!onlineOpponent!!.bothEnded) {
                onlineOpponent!!.realtimeDatabase.getReference("partida/jogo/" + onlineOpponent!!.roomName).child("matchState").setValue("killed")
            }
        }
    }
}