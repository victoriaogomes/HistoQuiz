package com.lenda.histoquiz.util

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import com.lenda.histoquiz.activities.LocalGameActivity
import com.lenda.histoquiz.activities.MenuActivity
import com.lenda.histoquiz.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import java.util.*


/**
 * Método construtor da classe, utilizado para fazer as devidas ligações entre os listenners e
 * certos campos no realtimeDatabase, bem como para criar a sala de jogo e realizar algumas con-
 * figurações necessárias para a execução da partida
 * @param game_scene - activity do tipo GameActivity, que instanciou essa classe e irá manipulá-la
 * @param matchCreator - variável booleana que informa se esse jogador é o criador da partida
 */
class LocalOpponent(game_scene: LocalGameActivity, matchCreator: Boolean, roomName: String) {
    var gameScene: LocalGameActivity
    var roomName: String
    var startTimeInMillis: Long
    var mTimeLeftInMillis: Long
    var state: String? = null
    var actualSlideToGuess: String
    var myPlayerCode = 0
    var myDuoPlayerCode = 0
    var opponentCode = 0
    var opponentDuoCode = 0
    var firestore: FirebaseFirestore
    var bothEnded = false
    var matchState = "onHold"
    var myPersonalMatchState = "onHold"
    var allset = booleanArrayOf(false, false, false, false)
    var configTimer: CountDownTimer? = null
    private var matchCreator: Boolean
    private var realtimeDatabase: FirebaseDatabase
    private var playersRoomRef: Array<DatabaseReference?>
    private var countDownTimer: CountDownTimer
    private var mTimerRunning = false

    init {
        startTimeInMillis = (game_scene.roundTime * 1000).toLong()
        mTimeLeftInMillis = startTimeInMillis
        playersRoomRef = arrayOfNulls(4)
        actualSlideToGuess = game_scene.actualSlide.toString() // Lâmina que estamos tentando adivinhar nesse momento
        this.matchCreator = matchCreator // Informa se sou o criador da partida ou não
        this.gameScene = game_scene // Activity responsável pela cena de jogo que está sendo exibida
        realtimeDatabase = FirebaseDatabase.getInstance()
        firestore = FirebaseFirestore.getInstance()
        val keySet = game_scene.matchSlides.keys.toTypedArray()
        Arrays.sort(keySet)
        this.roomName = roomName // Nome da sala, foi definido quando o usuário criou a sala
        if (matchCreator) {
            game_scene.imageToShow(0)
            realtimeDatabase.getReference("partidaLocal/jogo/$roomName").child("roundTime").setValue(startTimeInMillis)
            realtimeDatabase.getReference("partidaLocal/jogo/$roomName").child("slides").setValue(listOf(*keySet))
            identifyPlayersPosition()
            addAllSetListener()
            playersRoomRef[myPlayerCode]!!.child("allSet").setValue("true")
        } else {
            addListenerToPlayerPos()
            addListenerToSlides()
        }
        countDownTimer = object : CountDownTimer(startTimeInMillis, 1000) {
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
        addListenerToRoundTime()
        addListenerToSlideToGuess()
        game_scene.saveMatchInfo(false, 0)
        game_scene.screen.tipBTN.setOnClickListener { estadoA() }
        if (matchCreator) {
            game_scene.creator!!.actualRoomName?.let {
                game_scene.firestoreDatabase!!.collection("partidaLocal").document(it)
                    .update(
                        "playersId", listOf(
                            game_scene.playersId!![0],
                            game_scene.playersId!![1], game_scene.playersId!![2],
                            game_scene.playersId!![3]
                        )
                    )
            }
            runConfigTimer()
        }
    }

    private fun addAllSetListener() {
        playersRoomRef[0]!!.child("allSet").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                checkAllSet(snapshot, 0)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
        playersRoomRef[1]!!.child("allSet").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                checkAllSet(snapshot, 1)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
        playersRoomRef[2]!!.child("allSet").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                checkAllSet(snapshot, 2)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
        playersRoomRef[3]!!.child("allSet").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                checkAllSet(snapshot, 3)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    /**
     * Método utilizado para iniciar a partida, após todos os demais jogadores confirmarem que estão prontos e possuem todas
     * as informações necessárias para o início da partida
     */
    fun checkAllSet(snapshot: DataSnapshot, index: Int) {
        if (snapshot.exists()) {
            if (Objects.requireNonNull(snapshot.value).toString() == "true") {
                allset[index] = true
                if (allset[0] && allset[1] && allset[2] && allset[3]) {
                    configTimer!!.cancel()
                    gameScene.screen.roundFeedbackTXT.visibility = View.GONE
                    realtimeDatabase.getReference("partidaLocal/jogo/$roomName").child("matchState").setValue("player1Setup")
                }
            }
        }
    }

    /**
     * Método utilizado para obter o id relativo ao número do jogador que eu represento (1, 2, 3 ou 4)
     * e também para obter o id relativo ao número do jogador que a minha dupla representa
     */
    private fun identifyPlayersPosition() {
        for (i in 0..3) {
            if (gameScene.uidJogadores!![gameScene.playersId!![i]!!] == FirebaseAuth.getInstance().currentUser?.uid) {
                myPlayerCode = i
                Log.d(String.format("code %s", FirebaseAuth.getInstance().currentUser!!.displayName?.subSequence(0, 4)), myPlayerCode.toString()
                )
                when (myPlayerCode) {
                    0 -> {
                        myDuoPlayerCode = 1
                        opponentCode = 2
                        opponentDuoCode = 3
                    }
                    1 -> {
                        myDuoPlayerCode = 0
                        opponentCode = 3
                        opponentDuoCode = 2
                    }
                    2 -> {
                        myDuoPlayerCode = 3
                        opponentCode = 0
                        opponentDuoCode = 1
                    }
                    3 -> {
                        myDuoPlayerCode = 2
                        opponentCode = 1
                        opponentDuoCode = 0
                    }
                }
            }
        }
        Log.d("myPlayerCode", myPlayerCode.toString())
        Log.d("myDuoPlayerCode", myDuoPlayerCode.toString())
        Log.d("opponentCode", opponentCode.toString())
        Log.d("opponentDuoCode", opponentDuoCode.toString())
        playersRoomRef[0] = realtimeDatabase.getReference("partidaLocal/jogo/$roomName/player1")
        playersRoomRef[1] = realtimeDatabase.getReference("partidaLocal/jogo/$roomName/player2")
        playersRoomRef[2] = realtimeDatabase.getReference("partidaLocal/jogo/$roomName/player3")
        playersRoomRef[3] = realtimeDatabase.getReference("partidaLocal/jogo/$roomName/player4")
        if (matchCreator) {
            playersRoomRef[0]!!.child("uid").setValue(gameScene.uidJogadores!![gameScene.playersId!![0]!!])
            playersRoomRef[0]!!.child("duoUid").setValue(gameScene.uidJogadores!![gameScene.playersId!![1]!!])
            playersRoomRef[1]!!.child("uid").setValue(gameScene.uidJogadores!![gameScene.playersId!![1]!!])
            playersRoomRef[1]!!.child("duoUid").setValue(gameScene.uidJogadores!![gameScene.playersId!![0]!!])
            playersRoomRef[2]!!.child("uid").setValue(gameScene.uidJogadores!![gameScene.playersId!![2]!!])
            playersRoomRef[2]!!.child("duoUid").setValue(gameScene.uidJogadores!![gameScene.playersId!![3]!!])
            playersRoomRef[3]!!.child("uid").setValue(gameScene.uidJogadores!![gameScene.playersId!![3]!!])
            playersRoomRef[3]!!.child("duoUid").setValue(gameScene.uidJogadores!![gameScene.playersId!![2]!!])
        }
        addListenerToScore()
    }

    /**
     * Método utilizado para obter a configuração relativa a duração do cronômetro em cada rodada
     */
    private fun addListenerToRoundTime() {
        realtimeDatabase.getReference("partidaLocal/jogo/$roomName").child("roundTime").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    startTimeInMillis = snapshot.value.toString().toInt().toLong()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    /**
     * Método utilizado para obter a configuração relativa a posição de cada um dos 4 players dessa
     * partida, para que possamos saber quem é dupla de quem
     */
    @Suppress("UNCHECKED_CAST")
    fun addListenerToPlayerPos() {
        gameScene.creator!!.actualRoomName?.let { firestore.collection("partidaLocal").document(it) }
            ?.addSnapshotListener { documentSnapshot: DocumentSnapshot?, _: FirebaseFirestoreException? ->
                assert(documentSnapshot != null)
                var auxiliar = arrayOfNulls<Long>(4)
                auxiliar = (Objects.requireNonNull(documentSnapshot!!["playersId"]) as ArrayList<Long?>).toArray(auxiliar)
                val array = arrayOfNulls<Long>(4)
                Arrays.fill(array, 1L)
                if (!auxiliar.contentEquals(array)) {
                    for (i in auxiliar.indices) {
                        gameScene.playersId!![i] = auxiliar[i]!!.toInt()
                    }
                    identifyPlayersPosition()
                    runConfigTimer()
                    playersRoomRef[myPlayerCode]!!.child("allSet").setValue("true") { error, _ ->
                        System.err.println(
                            "Value was set. Error = $error"
                        )
                    }
                }
            }
    }

    /**
     * Método utilizado para obter as lâminas a serem utilizadas nessa partida, as quais são sortea-
     * das e enviadas pelo usuário que criou essa sala de jogo
     */
    private fun addListenerToSlides() {
        realtimeDatabase.getReference("partidaLocal/jogo/$roomName").child("slides").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val t = object : GenericTypeIndicator<ArrayList<Int?>?>() {}
                val slidesId = snapshot.getValue(t)
                if (slidesId != null) {
                    for (i in slidesId.indices) {
                        gameScene.matchSlides[slidesId[i]!!] = gameScene.slides[slidesId[i]]!!
                    }
                    gameScene.imageToShow(0)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
    //*********************************************************************************************************************************************************
    //*********************************************************************************************************************************************************
    //*********************************************************************************************************************************************************
    //*********************************************************************************************************************************************************
    /**
     * Método chamado sempre que o estado atual da partida é alterado. Os estados podem ser:
     * - onHold: aguardando para começar
     * - player1Dica: Player 1 dando dica para o Player 2
     * - player2Dica: Player 2 dando dica para o Player 1
     * - player3Dica: Player 3 dando dica para o Player 4
     * - player4Dica: Player 4 dando dica para o Player 3
     * - player1Setup: liberar botão de dica para o Player 1
     * - player2Setup: liberar botão de dica para o Player 2
     * - player3Setup: liberar botão de dica para o Player 3
     * - player4Setup: liberar botão de dica para o Player 4
     * - ended: partida finalizada
     */
    private fun addListenerToMatch() {
        realtimeDatabase.getReference("partidaLocal/jogo/$roomName").child("matchState").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value != null) {
                    configTimer!!.cancel()
                    matchState = snapshot.value.toString()
                    Log.d("Estado:", matchState)
                    //Caso meu duo esteja dando dica, aparece para mim a tela de adivinhar lâmina
                    if (matchState == String.format("player%sDica", myDuoPlayerCode + 1)) {
                        gameScene.changeItensVisibility(intArrayOf(0, 0))
                        if (myPersonalMatchState == "onHold") {
                            myPersonalMatchState = "recievingTip"
                            estadoB()
                        }
                    } else if (matchState == String.format("player%sDica", opponentCode + 1) || matchState == String.format(
                            "player%sDica",
                            myPlayerCode + 1
                        )
                    ) {
                        gameScene.changeItensVisibility(intArrayOf(0, 1))
                    } else if (matchState == String.format("player%sDica", opponentDuoCode + 1)) {
                        gameScene.changeItensVisibility(intArrayOf(0, 0))
                    } else if (matchState == String.format("player%sSetup", myPlayerCode + 1)) {
                        gameScene.changeItensVisibility(intArrayOf(1, 1))
                    } else if (matchState == String.format("player%sSetup", opponentCode + 1)) {
                        gameScene.changeItensVisibility(intArrayOf(0, 1))
                    } else if (matchState == String.format("player%sSetup", opponentDuoCode + 1)) {
                        gameScene.changeItensVisibility(intArrayOf(0, 0))
                    } else if (matchState == String.format("player%sSetup", myDuoPlayerCode + 1)) {
                        gameScene.changeItensVisibility(intArrayOf(0, 0))
                    } else if (matchState == "ended") {
                        Handler(Looper.getMainLooper()).postDelayed({ estadoF() }, 2000)
                    } else if (matchState == String.format("answered%s", myDuoPlayerCode + 1)) {
                        stopTimer()
                    } else if (matchState == "killed") forceMatchEnd("Seu oponente se desconectou da partida.")
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    /**
     * Método chamado sempre que a dupla de oponentes adivinhar uma das suas lâminas disponíveis, para indi-
     * car que o alvo coletivo agora é a lâmina seguinte
     */
    fun addListenerToSlideToGuess() {
        realtimeDatabase.getReference("partidaLocal/jogo/$roomName").child("slideToGuess").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value != null) {
                    actualSlideToGuess = snapshot.value.toString()
                    if (snapshot.value.toString() != "allDone") {
                        gameScene.actualSlide = snapshot.value.toString().toInt()
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
        playersRoomRef[myPlayerCode]!!.child("score").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value != null) {
                    gameScene.screen.myTeamPontTXT.text = String.format(Locale.getDefault(), "%d", snapshot.value.toString().toInt())
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
        playersRoomRef[myDuoPlayerCode]!!.child("score").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value != null) {
                    gameScene.screen.myTeamPontTXT.text = String.format(Locale.getDefault(), "%d", snapshot.value.toString().toInt())
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
        playersRoomRef[opponentCode]!!.child("score").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value != null) {
                    gameScene.screen.myOppTeamPontTXT.text = String.format(Locale.getDefault(), "%d", snapshot.value.toString().toInt())
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
        playersRoomRef[opponentDuoCode]!!.child("score").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value != null) {
                    gameScene.screen.myOppTeamPontTXT.text = String.format(Locale.getDefault(), "%d", snapshot.value.toString().toInt())
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
    //////////////////////////////////   MÁQUINA DE ESTADOS   /////////////////////////////////////
    /**
     * Método utilizado para iniciar o timer após o jogador clicar no botão "Dica", para que ele dê
     * dica para a sua dupla sobre a lâmina que está visualizando
     */
    private fun estadoA() {
        // Toast.makeText(game_scene, "Estou no estado A", Toast.LENGTH_LONG).show();
        state = "A"
        realtimeDatabase.getReference("partidaLocal/jogo/$roomName").child("matchState").setValue(String.format("player%sDica", myPlayerCode + 1))
        startTimer()
    }

    /**
     * Método utilizado para exibir para o jogador a lista de lâminas cadastradas no sistema relati-
     * vas ao sistema que ele selecionou para jogar, para que ele escolha aquela que acredita que
     * sua dupla está visualizando
     */
    fun estadoB() {
        // Toast.makeText(game_scene, "Estou no estado B", Toast.LENGTH_LONG).show();
        state = "B"
        startTimer()
        gameScene.showGuessSlide()
    }

    /**
     * Método utilizado para informar ao jogador que sua resposta fornecida a pergunta do seu opo-
     * nente está sendo validada
     * @param answer - resposta fornecida pelo jogador a lâmina que ele acredita estar tentando
     * adivinhar
     */
    fun estadoC(answer: String) {
        // Toast.makeText(game_scene, "Estou no estado C", Toast.LENGTH_LONG).show();
        realtimeDatabase.getReference("partidaLocal/jogo/$roomName").child("matchState").setValue("answered" + (myPlayerCode + 1))
        gameScene.closeGuessSlide()
        stopTimer()
        gameScene.setFeedbackText("Validando resposta...")
        Handler(Looper.getMainLooper()).postDelayed({ estadoD(answer) }, 2000)
    }

    /**
     * Método que efetivamente verifica se a lâmina informada pelo jogador é realmente a que ele
     * estava tentando adivinhar
     * @param answer - resposta fornecida pelo jogador
     */
    private fun estadoD(answer: String) {
        // Toast.makeText(game_scene, "Estou no estado D", Toast.LENGTH_LONG).show();
        myPersonalMatchState = "playing"
        stopTimer()
        val keySet = gameScene.matchSlides.keys.toTypedArray()
        Arrays.sort(keySet)
        var answerValidation = false
        var matchEnded = false
        val trueSlide: String = gameScene.matchSlides[keySet[gameScene.actualSlide]]?.name!!.lowercase()
        val systemCode: Int = gameScene.matchSlides[keySet[gameScene.actualSlide]]?.system!!
        if (trueSlide == answer.lowercase()) {
            gameScene.changePlayerScore(1, 3)
            playersRoomRef[myPlayerCode]!!.child("score").setValue(gameScene.getPlayerScore(1))
            playersRoomRef[myDuoPlayerCode]!!.child("score").setValue(gameScene.getPlayerScore(1))
            answerValidation = true
            if (actualSlideToGuess.toInt() + 1 == gameScene.matchSlides.size) matchEnded = true
            gameScene.computePerformance(systemCode, 1)
        }
        estadoE(answerValidation, matchEnded)
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
    fun estadoE(answerValidation: Boolean, matchEnded: Boolean) {
        // Toast.makeText(game_scene, "Estou no estado D", Toast.LENGTH_LONG).show();
        if (answerValidation) {
            if (matchEnded) {
                gameScene.setFeedbackText("Resposta correta! Você ganhou 3 pontos! Fim de jogo...")
                actualSlideToGuess = "allDone"
                realtimeDatabase.getReference("partidaLocal/jogo/$roomName").child("slideToGuess").setValue(actualSlideToGuess)
                realtimeDatabase.getReference("partidaLocal/jogo/$roomName").child("matchState").setValue("ended")
            } else {
                gameScene.actualSlide += 1
                realtimeDatabase.getReference("partidaLocal/jogo/$roomName").child("slideToGuess").setValue(gameScene.actualSlide.toString())
                gameScene.setFeedbackText("Resposta correta! Você ganhou 3 pontos! Vamos para a próxima rodada...")
                realtimeDatabase.getReference("partidaLocal/jogo/$roomName").child("matchState")
                    .setValue(String.format("player%sSetup", opponentCode + 1))
                myPersonalMatchState = "onHold"
            }
        } else {
            gameScene.setFeedbackText("Resposta incorreta! Seu oponente ganhou 3 pontos! Vamos para a próxima rodada...")
            realtimeDatabase.getReference("partidaLocal/jogo/$roomName").child("matchState")
                .setValue(String.format("player%sSetup", opponentDuoCode + 1))
            myPersonalMatchState = "onHold"
        }
    }

    /**
     * Método utilizado para exibir o dialog que informa ao jogador que a partida foi finalizada,
     * exibir as pontuações, informar quem ganhou o jogo, apagar as informações desse convite de
     * jogo armazenadas no firebase, apagar os dados dessa partida no realtime database e finalizar
     * a atividade de salvar informações relativas a performance desse jogador, caso ele não tenha
     * finalizado essa partida
     */
    fun estadoF() {
        // Toast.makeText(game_scene, "Estou no estado F", Toast.LENGTH_LONG).show();
        if (!matchCreator) {
            realtimeDatabase.getReference("partida/jogo/$roomName").setValue(null)
        }
        if (actualSlideToGuess != "allDone") {
            val keySet = gameScene.matchSlides.keys.toTypedArray()
            Arrays.sort(keySet)
            for (i in actualSlideToGuess.toInt() until gameScene.matchSlides.size) {
                gameScene.matchSlides[keySet[i]]?.system?.let { gameScene.computePerformance(it, 0) }
            }
        }
        if (gameScene.getPlayerScore(1) != gameScene.getPlayerScore(2)) {
            gameScene.saveMatchInfo(gameScene.getPlayerScore(1) > gameScene.getPlayerScore(2), 1)
        }
        bothEnded = true
        gameScene.showEndGameDialog()
    }

    private fun estadoG() {
        // Toast.makeText(game_scene, "Estou no estado G", Toast.LENGTH_LONG).show();
        gameScene.setFeedbackText("Jogada da dupla adversária...")
    }

    fun forceMatchEnd(message: String?) {
        realtimeDatabase.getReference("partida/jogo/$roomName").setValue(null)
        val builder = AlertDialog.Builder(gameScene).setTitle(R.string.fimJogo).setMessage(message)
        builder.setPositiveButton(R.string.menuInicial) { _: DialogInterface?, _: Int ->
            val troca = Intent(gameScene, MenuActivity::class.java)
            gameScene.startActivity(troca)
        }.create().show()
    }

    /**
     * Método utilizado para iniciar o timer utilizado na partida
     */
    fun startTimer() {
        if (mTimerRunning) stopTimer()
        countDownTimer.start()
        mTimerRunning = true
    }

    /**
     * Método utilizado para pausar o timer utilizado na partida
     */
    fun stopTimer() {
        countDownTimer.cancel()
        mTimerRunning = false
        mTimeLeftInMillis = startTimeInMillis
        gameScene.screen.timerTXT.text = gameScene.getString(R.string.vezOponente)
    }

    /**
     * Método chamado quando o timer utilizado chega a 00min:00s. Ele exibe ao usuário uma mensagem
     * informando a ele que o tempo para realização de uma ação acabou, aguarda um tempo e segue
     * para a próxima ação do jogo
     */
    fun endTimer() {
        gameScene.setFeedbackText("Acabou o tempo para você realizar uma ação. Vamos para a próxima rodada!")
        mTimeLeftInMillis = startTimeInMillis
        mTimerRunning = false
        if (state == "B") {
            gameScene.closeGuessSlide()
            realtimeDatabase.getReference("partidaLocal/jogo/$roomName").child("matchState")
                .setValue(String.format("player%sSetup", opponentDuoCode + 1))
            myPersonalMatchState = "onHold"
        }
        Handler(Looper.getMainLooper()).postDelayed({ hideFeedback() }, 2000)
        Handler(Looper.getMainLooper()).postDelayed({ estadoG() }, 2000)
    }

    private fun hideFeedback() {
        gameScene.screen.roundFeedbackTXT.visibility = View.INVISIBLE
    }

    private fun runConfigTimer() {
        configTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                forceMatchEnd("Algum dos participantes da partida se desconectou e, por isso, o jogo foi finalizado")
            }
        }.start()
    }
}