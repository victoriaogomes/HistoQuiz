package com.example.histoquiz.util

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.histoquiz.R
import com.example.histoquiz.activities.GameActivity
import com.example.histoquiz.activities.MenuActivity
import com.example.histoquiz.model.Slide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

/**
 * Método construtor da classe, utilizado para fazer as devidas ligações entre os listenners e
 * certos campos no realtimeDatabase, bem como para criar a sala de jogo e realizar algumas con-
 * figurações necessárias para a execução da partida
 * @param game_scene - activity do tipo GameActivity, que instanciou essa classe e irá manipulá-la
 * @param opponentUID - UID do oponente dessa partida
 * @param matchCreator - variável booleana que informa se esse jogador é o criador da partida
 */
class OnlineOpponent(var game_scene: GameActivity, private var opponentUID: String, private var matchCreator: Boolean) {
    var realtimeDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    var firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    var roomName: String? = null
    var myRoomRef: DatabaseReference? = null
    var opponentRoomRef: DatabaseReference? = null
    var question = 0
    var category = 0
    var mySlides: MutableMap<Int, Slide?> = HashMap()
    var opponentSlides: MutableMap<Int, Slide?> = HashMap()
    var mTimeLeftInMillis = START_TIME_IN_MILLIS
    var state: String? = null
    var opponentCategoryId = 0
    var opponentQuestionId = 0
    var myOpponentAnswer: Boolean? = null
    var mySlideToGuess = "firstSlide"
    var opponentSlideToGuess = "firstSlide"
    var bothEnded = false
    var matchState = "onHold"
    var myPersonalMatchState = "onHold"
    private var trueAnswer = false
    private var countDownTimer: CountDownTimer
    private var mTimerRunning = false

    companion object {
        const val START_TIME_IN_MILLIS: Long = 120000
    }

    init {
        val keySet = game_scene.mySlides.keys.toTypedArray()
        Arrays.sort(keySet)
        if (matchCreator) {
            //Cria uma sala para o jogo e se adiciona como jogador número 1
            roomName = FirebaseAuth.getInstance().currentUser?.uid
            myRoomRef = realtimeDatabase.getReference("partida/jogo/$roomName/player1")
            opponentRoomRef = realtimeDatabase.getReference("partida/jogo/$roomName/player2")
            myRoomRef!!.child("UID").setValue(roomName)
            realtimeDatabase.getReference("partida/jogo/$roomName").child("slides")
                .setValue(ArrayList(listOf(keySet[0], keySet[1], keySet[2], keySet[3], keySet[4], keySet[5])))
            mySlides[keySet[0]] = game_scene.slides[keySet[0]]
            mySlides[keySet[1]] = game_scene.slides[keySet[1]]
            mySlides[keySet[2]] = game_scene.slides[keySet[2]]
            opponentSlides[keySet[3]] = game_scene.slides[keySet[3]]
            opponentSlides[keySet[4]] = game_scene.slides[keySet[4]]
            opponentSlides[keySet[5]] = game_scene.slides[keySet[5]]
        } else {
            //Entra numa sala para o jogo que foi convidado e se adiciona como jogador número 2
            roomName = opponentUID
            myRoomRef = realtimeDatabase.getReference("partida/jogo/$roomName/player2")
            opponentRoomRef = realtimeDatabase.getReference("partida/jogo/$roomName/player1")
            myRoomRef!!.child("UID").setValue(FirebaseAuth.getInstance().currentUser?.uid)
            addListenerToSlides()
        }
        myRoomRef!!.child("slideToGuess").setValue("firstSlide")
        myRoomRef!!.child("answer").setValue("-")
        myRoomRef!!.child("categoryId").setValue(-1)
        myRoomRef!!.child("questionId").setValue(-1)
        myRoomRef!!.child("score").setValue(0)
        myRoomRef!!.child("nextRound").setValue("-")
        countDownTimer = object : CountDownTimer(START_TIME_IN_MILLIS, 1000) {
            override fun onTick(l: Long) {
                val minutos: String
                val segundos: String
                mTimeLeftInMillis = l
                val minutes = (mTimeLeftInMillis / 1000).toInt() / 60
                val seconds = (mTimeLeftInMillis / 1000).toInt() % 60
                minutos = if (minutes < 10) "0$minutes" else minutes.toString()
                segundos = if (seconds < 10) "0$seconds" else seconds.toString()
                game_scene.screen.timerTXT.text = String.format(Locale.getDefault(), "Tempo: %s:%s", minutos, segundos)
            }

            override fun onFinish() {
                endTimer()
            }
        }
        addListenerToMatch()
        addListenerToSlideToGuess()
        addListenerToAnswer()
        addListenerToCategory()
        addListenerToNextRound()
        addListenerToQuestion()
        addListenerToScore()
        game_scene.saveMatchInfo(false, 0)
        if (matchCreator) {
            realtimeDatabase.getReference("partida/jogo/$roomName").child("matchState").setValue("running")
            estadoE()
        } else {
            FirebaseFirestore.getInstance().document("partida/convites/" + FirebaseAuth.getInstance().currentUser?.uid + "/" + opponentUID
            ).update("inviteAccepted", "aceito")
            estadoA()
        }
    }

    /**
     * Método chamado sempre que o estado atual da partida é alterado. Os estados podem ser:
     * - onHold: aguardando para começar
     * - running: partida rolando
     * - ended: partida finalizada
     */
    private fun addListenerToMatch() {
        realtimeDatabase.getReference("partida/jogo/$roomName").child("matchState").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value != null) {
                    matchState = snapshot.value.toString()
                    if (matchState == "ended") {
                        Handler(Looper.getMainLooper()).postDelayed({ estadoL() }, 2000)
                    } else if (matchState == "killed") forceMatchEnd()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    /**
     * Método utilizado para obter as lâminas a serem utilizadas nessa partida, as quais são sortea-
     * das e enviadas pelo usuário que criou essa partida
     */
    private fun addListenerToSlides() {
        realtimeDatabase.getReference("partida/jogo/$roomName").child("slides").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val t = object : GenericTypeIndicator<ArrayList<Int?>?>() {}
                val slidesId = snapshot.getValue(t)
                if (slidesId != null) {
                    mySlides[slidesId[3]!!] = game_scene.slides[slidesId[3]]
                    mySlides[slidesId[4]!!] = game_scene.slides[slidesId[4]]
                    mySlides[slidesId[5]!!] = game_scene.slides[slidesId[5]]
                    opponentSlides[slidesId[0]!!] = game_scene.slides[slidesId[0]]
                    opponentSlides[slidesId[1]!!] = game_scene.slides[slidesId[1]]
                    opponentSlides[slidesId[2]!!] = game_scene.slides[slidesId[2]]
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    /**
     * Método chamado sempre que seu oponente adivinhar uma das suas lâminas disponíveis, para indi-
     * car que o alvo agora é a lâmina seguinte
     */
    private fun addListenerToSlideToGuess() {
        opponentRoomRef!!.child("slideToGuess").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value != null) {
                    opponentSlideToGuess = snapshot.value.toString()
                    when (opponentSlideToGuess) {
                        "firstSlide" -> {
                        }
                        "secondSlide" -> game_scene.checkSlide(0, 2)
                        "thirdSlide" -> game_scene.checkSlide(1, 2)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    /**
     * Método invocado sempre que o oponente responder a pergunta solicitada por esse usuário e ar-
     * mazená-la no firebase
     */
    private fun addListenerToAnswer() {
        opponentRoomRef!!.child("answer").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value != null) {
                    if (myPersonalMatchState == "waiting" && matchState == "running" && snapshot.value.toString() != "-") {
                        myOpponentAnswer = if (snapshot.value.toString() == "null") null else snapshot.value.toString() == "sim"
                        opponentRoomRef!!.child("answer").setValue("-")
                        estadoH()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    /**
     * Método chamado sempre que o campo categoryId do oponente é alterado, sempre que ele selecio-
     * onar a categoria da pergunta que deseja enviar
     */
    private fun addListenerToCategory() {
        opponentRoomRef!!.child("categoryId").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value != null) {
                    if (snapshot.value.toString().toInt() != -1) {
                        opponentCategoryId = snapshot.value.toString().toInt()
                        Log.d("Cat. da pergunta: ", opponentCategoryId.toString())
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    /**
     * Método chamado sempre que o oponente informar que a partida deve seguir para a próxima roda-
     * da, que pode ocorrer porque ele não realizou uma ação no tempo estipulado, ou porque ele não
     * deseja tentar adivinhar sua lâmina no momento
     */
    private fun addListenerToNextRound() {
        opponentRoomRef!!.child("nextRound").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (myPersonalMatchState == "waiting" && matchState == "running" && snapshot.value != null) {
                    if (snapshot.value != "-") {
                        estadoE()
                        opponentRoomRef!!.child("nextRound").setValue("-")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    /**
     * Método chamado sempre que o campo questionId do oponente é modificado, o que ocorre quando
     * ele selecionar a pergunta para que este jogador responda. Em seguida, ele move a máquina de
     * estados para o estado B
     */
    private fun addListenerToQuestion() {
        opponentRoomRef!!.child("questionId").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (myPersonalMatchState == "waiting" && matchState == "running" && snapshot.value != null) {
                    if (snapshot.value.toString().toInt() != -1) {
                        opponentQuestionId = snapshot.value.toString().toInt()
                        Log.d("Id. da pergunta: ", opponentQuestionId.toString())
                        estadoB()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    /**
     * Método utilizado para monitorar mudanças no score do seu oponente e atualizá-la na view do
     * seu jogo
     */
    private fun addListenerToScore() {
        opponentRoomRef!!.child("score").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value != null) {
                    if(snapshot.value.toString().toInt() - Integer.parseInt(game_scene.screen.scorePlayer2TXT.text.toString()) == 3){
                        game_scene.showTextToPlayer("Seu oponente acertou a lâmina! Clique nas cartas para ver sua próxima lâmina...")
                        Handler(Looper.getMainLooper()).postDelayed({game_scene.screen.scorePlayer2TXT.text = String.format(Locale.getDefault(), "%d", snapshot.value.toString().toInt())}, 5000)
                    }
                    else{
                        game_scene.screen.scorePlayer2TXT.text = String.format(Locale.getDefault(), "%d", snapshot.value.toString().toInt())
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
        myRoomRef!!.child("score").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value != null) {
                    game_scene.screen.scorePlayer1TXT.text = String.format(Locale.getDefault(), "%d", snapshot.value.toString().toInt())
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
    //////////////////////////////////   MÁQUINA DE ESTADOS   /////////////////////////////////////
    /**
     * Método utilizado para solicitar ao jogador que aguarde o seu oponente selecionar uma pergunta
     * para que ele responda
     */
    fun estadoA() {
        if (mTimerRunning) stopTimer()
        myPersonalMatchState = "waiting"
        game_scene.showTextToPlayer(game_scene.getString(R.string.vezOponente))
        Handler(Looper.getMainLooper()).postDelayed({ game_scene.showTextToPlayer(game_scene.getString(R.string.oponenteSelecPerg)) }, 5000)
    }

    /**
     * Método utilizado para exibir para o jogador a pergunta selecionada por seu oponente para que
     * ele responda. Um timer de 2 minutos deve ser iniciado, que é o tempo que o jogador tem para
     * responder a pergunta
     */
    fun estadoB() {
        myPersonalMatchState = "playing"
        game_scene.setQuestionForPlayerAnswer(game_scene.getQuestionText(opponentCategoryId, opponentQuestionId))
        opponentRoomRef!!.child("questionId").setValue(-1)
        opponentRoomRef!!.child("categoryId").setValue(-1)
        state = "B"
        startTimer()
    }

    /**
     * Método utilizado para informar ao jogador que sua resposta fornecida a pergunta do seu opo-
     * nente está sendo validada. Esse método também deve pausar o timer iniciado no
     * método anterior (_estado_B())
     * @param answer - resposta fornecida pelo jogador a pergunta que ele recebeu do oponente
     */
    fun estadoC(answer: Boolean) {
        stopTimer()
        game_scene.showTextToPlayer("Validando resposta...")
        Handler(Looper.getMainLooper()).postDelayed({ estadoD(answer) }, 2000)
    }

    /**
     * Método que efetivamente verifica se a resposta fornecida pelo jogador está correta, realiza a
     * distribuição da pontuação de acordo com isso e fornece o feedback ao jogador. Caso tenha acer-
     * tado a pergunta, o jogador ganha um ponto, e caso tenha errado, ele perde um ponto.
     * @param answer - resposta fornecida pelo jogador
     */
    private fun estadoD(answer: Boolean?) {
        myPersonalMatchState = "waiting"
        val keySet = opponentSlides.keys.toTypedArray()
        Arrays.sort(keySet)
        when (opponentSlideToGuess) {
            "firstSlide" -> trueAnswer = game_scene.getQuestionRealAnswer(opponentCategoryId, opponentQuestionId, keySet[0])
            "secondSlide" -> trueAnswer = game_scene.getQuestionRealAnswer(opponentCategoryId, opponentQuestionId, keySet[1])
            "thirdSlide" -> trueAnswer = game_scene.getQuestionRealAnswer(opponentCategoryId, opponentQuestionId, keySet[2])
        }
        if (answer != null) {
            if (trueAnswer == answer) {
                game_scene.changePlayerScore(1, 1)
                game_scene.showTextToPlayer("Parabéns, você acertou! Aguardando oponente analisar a resposta...")
            } else {
                game_scene.changePlayerScore(1, -1)
                game_scene.showTextToPlayer("Resposta incorreta, não foi dessa vez! Aguardando seu oponente analisar a resposta...")
            }
            if (answer) myRoomRef!!.child("answer").setValue("sim") else myRoomRef!!.child("answer").setValue("não")
        } else {
            game_scene.showTextToPlayer("Resposta incorreta, não foi dessa vez! Aguardando seu oponente analisar a resposta...")
            myRoomRef!!.child("answer").setValue("null")
        }
    }

    /**
     * Método utilizado para solicitar ao jogador que ele escolha uma categoria e uma pergunta para
     * enviar para seu oponente responder. Além disso, um timer é iniciado, pois o jogador tem 2
     * minutos para realizar essa ação
     */
    fun estadoE() {
        myPersonalMatchState = "playing"
        game_scene.showTextToPlayer("É a sua vez de jogar!")
        Handler(Looper.getMainLooper()).postDelayed({
            game_scene.showQuestionSelection()
            state = "E"
            startTimer()
        }, 5000)
    }

    /**
     * Método utilizado para enviar ao jogador uma mensagem informando que sua pergunta está
     * sendo repassada ao seu oponente
     */
    fun estadoF() {
        stopTimer()
        game_scene.closeQuestionSelection()
        game_scene.showTextToPlayer("Enviando...")
        Handler(Looper.getMainLooper()).postDelayed({ estadoG() }, 2000)
    }

    /**
     * Método utilizado para informar ao jogador que a resposta do seu oponente a sua pergunta está
     * sendo aguardada
     */
    private fun estadoG() {
        myPersonalMatchState = "waiting"
        myRoomRef!!.child("categoryId").setValue(category)
        myRoomRef!!.child("questionId").setValue(question)
        game_scene.showTextToPlayer("Aguardando resposta do oponente...")
    }

    /**
     * Método utilizado para exibir para o jogador um feedback, exibindo a resposta que seu oponente
     * forneceu a sua pergunta, a resposta correta e a modificação de pontuação que foi feita no
     * score do seu adversário
     * Em seguida, é perguntado para o jogador se ele deseja seguir para a próxima rodada, ou se
     * quer tentar advinhar sua lâmina; ele terá também 2 minutos para tomar essa decisão.
     */
    fun estadoH() {
        myPersonalMatchState = "playing"
        var slide = 0
        val keySet = mySlides.keys.toTypedArray()
        Arrays.sort(keySet)
        when (mySlideToGuess) {
            "firstSlide" -> slide = keySet[0]
            "secondSlide" -> slide = keySet[1]
            "thirdSlide" -> slide = keySet[2]
        }
        game_scene.showQuestionFeedback(myOpponentAnswer, game_scene.getQuestionRealAnswer(category, question, slide))
        state = "H"
        startTimer()
    }

    /**
     * Método utilizado para exibir ao jogador uma lista contendo todas as lâminas disponíveis no
     * jogo, para que ele selecione a que ele acha que está tentando adivinhar
     */
    fun estadoI() {
        myPersonalMatchState = "playing"
        state = "I"
        game_scene.showGuessSlide()
    }

    /**
     * Método utilizado para verificar se a lâmina informada pelo jogador é realmente a que ele
     * está tentando adivinhar, atribuindo pontuação da seguinte forma:
     * - Caso tenha acertado, o jogador ganha 3 pontos
     * - Caso tenha errado, seu oponente ganha 3 pontos
     * Em seguida, o timer iniciado para o jogador selecionar a lâmina será pausado
     * @param answer - lâmina informada pelo jogador
     */
    fun estadoJ(answer: String) {
        myPersonalMatchState = "playing"
        stopTimer()
        val keySet = mySlides.keys.toTypedArray()
        Arrays.sort(keySet)
        var answerValidation = false
        var matchEnded = false
        var trueSlide = ""
        var systemCode = 0
        var position = 0
        when (mySlideToGuess) {
            "firstSlide" -> {
                trueSlide = mySlides[keySet[0]]?.name!!.lowercase()
                systemCode = mySlides[keySet[0]]?.system!!
                position = 0
            }
            "secondSlide" -> {
                trueSlide = mySlides[keySet[1]]?.name!!.lowercase()
                systemCode = mySlides[keySet[0]]?.system!!
                position = 1
            }
            "thirdSlide" -> {
                trueSlide = mySlides[keySet[2]]?.name!!.lowercase()
                systemCode = mySlides[keySet[0]]?.system!!
                position = 2
            }
        }
        if (trueSlide == answer.lowercase()) {
            game_scene.changePlayerScore(1, 3)
            myRoomRef!!.child("score").setValue(game_scene.getPlayerScore(1))
            answerValidation = true
            if (mySlideToGuess == "thirdSlide") matchEnded = true
            game_scene.checkSlide(position, 1)
            game_scene.computePerformance(systemCode, 1)
        } else {
            game_scene.changePlayerScore(2, 3)
            opponentRoomRef!!.child("score").setValue(game_scene.getPlayerScore(2))
        }
        estadoK(answerValidation, matchEnded)
    }

    /**
     * Método utilizado para exibir ao jogador o feedback relativo a sua tentativa de advinhar sua
     * lâmina e:
     * - Seguir para a próxima rodada,caso ainda hajam lâminas para serem adivinhadas
     * - Seguir para o fim do jogo, caso o jogador já tenha advinhado todas as suas lâminas
     * @param answerValidation - validação da resposta; true se o jogador tiver acertado a lâmina,
     * e false caso tenha errado
     * @param matchEnded - variável que indica se a partida deve ser finalizada ou não
     */
    private fun estadoK(answerValidation: Boolean, matchEnded: Boolean) {
        game_scene.closeGuessSlide()
        if (answerValidation) {
            game_scene.showTextToPlayer("Você acertou a lâmina. Bom trabalho!")
        } else {
            game_scene.showTextToPlayer("Você errou a lâmina. Não desanime, que sabe na próxima!")
        }
        if (matchEnded) {
            Handler(Looper.getMainLooper()).postDelayed({
                mySlideToGuess = "allDone"
                realtimeDatabase.getReference("partida/jogo/$roomName").child("matchState").setValue("ended")
            }, 5000)
        } else {
            myRoomRef!!.child("nextRound").setValue("sim")
            Handler(Looper.getMainLooper()).postDelayed({ estadoA() }, 5000)
        }
    }

    /**
     * Método utilizado para exibir o dialog que informa ao jogador que a partida foi finalizada,
     * exibir as pontuações, informar quem ganhou o jogo, apagar as informações desse convite de
     * jogo armazenadas no firebase, apagar os dados dessa partida no realtime database e finalizar
     * a atividade de salvar informações relativas a performance desse jogador, caso ele não tenha
     * finalizado essa partida
     */
    fun estadoL() {
        if (!matchCreator) {
            realtimeDatabase.getReference("partida/jogo/$roomName").setValue(null)
        }
        if (mySlideToGuess != "allDone") {
            val keySet = mySlides.keys.toTypedArray()
            Arrays.sort(keySet)
            when (mySlideToGuess) {
                "firstSlide" -> {
                    game_scene.computePerformance(mySlides[keySet[0]]?.system!!, 0)
                    game_scene.computePerformance(mySlides[keySet[1]]?.system!!, 0)
                    game_scene.computePerformance(mySlides[keySet[2]]?.system!!, 0)
                }
                "secondSlide" -> {
                    game_scene.computePerformance(mySlides[keySet[1]]?.system!!, 0)
                    game_scene.computePerformance(mySlides[keySet[2]]?.system!!, 0)
                }
                "thirdSlide" -> game_scene.computePerformance(mySlides[keySet[2]]?.system!!, 0)
            }
        }
        game_scene.saveMatchInfo(game_scene.getPlayerScore(1) > game_scene.getPlayerScore(2), 1)
        bothEnded = true
        game_scene.showEndGameDialog()
    }

    fun forceMatchEnd() {
        realtimeDatabase.getReference("partida/jogo/$roomName").setValue(null)
        val builder = AlertDialog.Builder(game_scene).setTitle(R.string.fimJogo).setMessage(R.string.endMessage)
        builder.setPositiveButton(R.string.menuInicial) { _: DialogInterface?, _: Int ->
            val troca = Intent(game_scene, MenuActivity::class.java)
            game_scene.startActivity(troca)
        }.create().show()
    }

    /**
     * Método utilizado para iniciar o timer utilizado na partida
     */
    private fun startTimer() {
        if (mTimerRunning) stopTimer()
        countDownTimer.start()
        mTimerRunning = true
    }

    /**
     * Método utilizado para pausar o timer utilizado na partida
     */
    private fun stopTimer() {
        countDownTimer.cancel()
        mTimerRunning = false
        mTimeLeftInMillis = START_TIME_IN_MILLIS
        game_scene.screen.timerTXT.text = game_scene.getString(R.string.vezOponente)
    }

    /**
     * Método chamado quando o timer utilizado chega a 00min:00s. Ele exibe ao usuário uma mensagem
     * informando a ele que o tempo para realização de uma ação acabou, aguarda um tempo e segue
     * para a próxima ação do jogo
     */
    fun endTimer() {
        game_scene.showTextToPlayer("Acabou o tempo para você realizar uma ação. Vamos para a próxima rodada!")
        mTimeLeftInMillis = START_TIME_IN_MILLIS
        mTimerRunning = false
        when (state) {
            "B" -> Handler(Looper.getMainLooper()).postDelayed({ estadoD(null) }, 2000)
            "E" -> {
                game_scene.closeQuestionSelection()
                myRoomRef!!.child("nextRound").setValue("sim")
                Handler(Looper.getMainLooper()).postDelayed({ estadoA() }, 2000)
            }
            "H" -> {
                game_scene.closeQuestionFeedback()
                myRoomRef!!.child("nextRound").setValue("sim")
                Handler(Looper.getMainLooper()).postDelayed({ estadoA() }, 2000)
            }
            "I" -> {
                game_scene.closeGuessSlide()
                myRoomRef!!.child("nextRound").setValue("sim")
                Handler(Looper.getMainLooper()).postDelayed({ estadoA() }, 2000)
            }
        }
    }
}