package com.example.histoquiz.util

import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import com.example.histoquiz.activities.GameActivity
import com.example.histoquiz.model.Slide
import java.util.*

/**
 * Método construtor da classe, recebe a activity responsável por criá-la e gerenciá-la, as per-
 * guntas armazenadas no firebase e as lâminas disponíveis
 * @param game_scene - scene que instanciou essa classe
 * @param perguntas - perguntas relativas a lâminas, e separadas por categorias, que estão dis-
 * poníveis no firebase
 * @param slides - lâminas cadastradas no firebase
 */
class ComputerOpponent(var game_scene: GameActivity, private var perguntas: HashMap<String, Map<String, Any>>, slides: HashMap<Int, Slide>) {
    var slideToGuess = "firstSlide"
    var numberOfQuestions = 0
    var slides: HashMap<Int, Slide>
    var state: String? = null
    var mTimeLeftInMillis = START_TIME_IN_MILLIS
    private var mTimerRunning = false
    private var rndGenerator: Random
    private var raffledCategory = 0
    private var raffledQuestion = 0
    private var raffledValue: Int
    private var myAnswer = false
    private var general = true // Variável para indicar se o PC já pode selecionar perguntas de outra categoria
    private var trueAnswer = false
    private var askedQuestions: LinkedList<String>
    private var countDownTimer: CountDownTimer

    companion object {
        const val START_TIME_IN_MILLIS: Long = 120000
    }

    init {
        this.slides = slides.clone() as HashMap<Int, Slide>
        askedQuestions = LinkedList()
        val keyset = perguntas.keys.toTypedArray()
        Arrays.sort(keyset)
        for (i in keyset.indices) {
            if (keyset[i] == "Gerais") {
                raffledCategory = i
            }
        }
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
        rndGenerator = Random()
        raffledValue = generateRaffledValue(100, 1)
        game_scene.saveMatchInfo(false, 0)
        estadoA()
    }

    /**
     * Método utilizado para solicitar ao jogador que aguarde o seu oponente selecionar uma pergunta
     * para que ele responda
     */
    fun estadoA() {
        game_scene.showTextToPlayer("Aguardando oponente selecionar uma pergunta...")
        Handler(Looper.getMainLooper()).postDelayed({ estadoB() }, 2000)
    }

    /**
     * Método utilizado para sortear uma categoria e uma pergunta e enviá-la para o jogador respon-
     * der, vinda do seu oponente, que no caso é o computador. Inicialmente, o computador só pode
     * selecionar perguntas da categoria "geral". Após selecionar uma pergunta e receber "sim" como
     * resposta, o computador estará liberado para selecionar perguntas de todas as características
     * disponíveis
     * Não é possível selecionar a mesma pergunta mais de uma vez para uma mesma lâmina.
     * Um timer de 2 minutos deve ser iniciado, que é o tempo que o jogador tem para responder a
     * pergunta
     */
    fun estadoB() {
        if (!general) {
            raffledCategory = generateRaffledValue(perguntas.keys.size, 0)
        }
        numberOfQuestions = perguntas[game_scene.getCategoryName(raffledCategory)]?.size!!
        raffledQuestion = generateRaffledValue(numberOfQuestions, 0)
        while (askedQuestions.contains(game_scene.getQuestionText(raffledCategory, raffledQuestion))) {
            if (!general) raffledCategory = generateRaffledValue(perguntas.keys.size, 0)
            numberOfQuestions = perguntas[game_scene.getCategoryName(raffledCategory)]?.size!!
            raffledQuestion = generateRaffledValue(numberOfQuestions, 0)
        }
        askedQuestions.add(game_scene.getQuestionText(raffledCategory, raffledQuestion))
        game_scene.setQuestionForPlayerAnswer(game_scene.getQuestionText(raffledCategory, raffledQuestion))
        state = "B"
        startTimer()
    }

    /**
     * Método utilizado para informar ao jogador que sua resposta fornecida a pergunta do seu opo-
     * nente (Computador) está sendo validada. Esse método também deve pausar o timer iniciado no
     * método anterior (_estado_B())
     * @param answer - resposta fornecida pelo jogador a pergunta que ele recebeu do PC
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
        val keySet: Array<Any> = game_scene.mySlides.keys.toTypedArray()
        Arrays.sort(keySet)
        val text: String
        when (slideToGuess) {
            "firstSlide" -> trueAnswer = game_scene.getQuestionRealAnswer(raffledCategory, raffledQuestion, keySet[3].toString().toInt())
            "secondSlide" -> trueAnswer = game_scene.getQuestionRealAnswer(raffledCategory, raffledQuestion, keySet[4].toString().toInt())
            "thirdSlide" -> trueAnswer = game_scene.getQuestionRealAnswer(raffledCategory, raffledQuestion, keySet[5].toString().toInt())
        }
        if (trueAnswer) general = false
        if (answer != null) {
            text = if (trueAnswer == answer) {
                game_scene.changePlayerScore(1, 1)
                "ganhou 1 ponto!"
            } else {
                game_scene.changePlayerScore(1, -1)
                "perdeu 1 ponto!"
            }
            game_scene.showTextToPlayer("Você $text Aguardando oponente analisar sua resposta...")
        }
        Handler(Looper.getMainLooper()).postDelayed({ estadoE(trueAnswer) }, 2000)
    }

    /**
     * Método utilizado para remover do roll de possibilidades as lâminas cuja resposta a pergunta
     * feita pelo PC não se adequam a resposta verdadeira recebida. Além disso, aqui é verificado
     * quantas possibilidades de lâminas ainda estão disponíveis para serem advinhadas e, caso sejam
     * 3 ou menos, o PC tenta adivinhar sua lâmina
     * @param trueAnswer - resposta verdadeira da pergunta selecionada pelo PC para o jogador
     * responder
     */
    fun estadoE(trueAnswer: Boolean) {
        var delay = 1000
        val copy = slides.clone() as HashMap<Int, Slide>
        for ((key) in copy) {
            if (game_scene.getQuestionRealAnswer(raffledCategory, raffledQuestion, key) != trueAnswer) {
                slides.remove(key)
            }
        }
        //Toast.makeText(game_scene, "Possibilidades: " + slides.size(), Toast.LENGTH_LONG).show();
        if (slides.size in 1..3) {
            var position = 0
            delay = 3000
            val keySet = game_scene.mySlides.keys.toTypedArray()
            Arrays.sort(keySet)
            var slideName: String? = ""
            when (slideToGuess) {
                "firstSlide" -> {
                    slideName = game_scene.mySlides[keySet[3]]?.name
                    position = 0
                }
                "secondSlide" -> {
                    slideName = game_scene.mySlides[keySet[4]]?.name
                    position = 1
                }
                "thirdSlide" -> {
                    slideName = game_scene.mySlides[keySet[5]]?.name
                    position = 2
                }
            }
            if (slideName == slides[slides.keys.toTypedArray()[0]]?.name) {
                game_scene.showTextToPlayer("Seu oponente adivinhou sua lâmina e ganhou 3 pontos!")
                game_scene.checkSlide(position, 2)
                game_scene.changePlayerScore(2, 3)
                slides = game_scene.slides.clone() as HashMap<Int, Slide>
                general = true
                val keySet2 = perguntas.keys.toTypedArray()
                Arrays.sort(keySet2)
                for (i in keySet2.indices) {
                    if (keySet2[i] == "Gerais") {
                        raffledCategory = i
                    }
                }
                askedQuestions = LinkedList()
                if (position == 2) {
                    estadoM()
                }
            } else {
                game_scene.showTextToPlayer("Seu oponente tentou adivinhar sua lâmina e errou. Você ganhou 3 pontos!")
                game_scene.changePlayerScore(1, 3)
                slides.remove(slides.keys.toTypedArray()[0].toString().toInt())
            }
        }
        Handler(Looper.getMainLooper()).postDelayed({ estadoF() }, delay.toLong())
    }

    /**
     * Método utilizado para solicitar ao jogador que ele escolha uma categoria e uma pergunta para
     * enviar para o PC responder. Além disso, um timer é iniciado, pois o jogador tem 2 minutos
     * para realizar essa ação
     */
    private fun estadoF() {
        game_scene.showQuestionSelection()
        state = "F"
        startTimer()
    }

    /**
     * Método utilizado para enviar ao jogador uma mensagem informando que sua pergunta está
     * sendo repassada ao seu oponente (nesse caso, o computador)
     */
    fun estadoG() {
        stopTimer()
        game_scene.closeQuestionSelection()
        game_scene.showTextToPlayer("Enviando...")
        Handler(Looper.getMainLooper()).postDelayed({ estadoH() }, 2000)
    }

    /**
     * Método utilizado para informar ao jogador que a resposta do seu oponente (o PC) a sua pergun-
     * ta está sendo aguardada
     */
    private fun estadoH() {
        game_scene.showTextToPlayer("Aguardando resposta do oponente...")
        Handler(Looper.getMainLooper()).postDelayed({ estadoI() }, 2000)
    }

    /**
     * Método utilizado para sortear uma resposta para o computador fornecer a pergunta do jogador,
     * e para fazer a modificação da pontuação do computador da seguinte forma:
     * - Caso tenha respondido corretamente, o computador ganha 1 ponto
     * - Caso tenha respondido erroneamente, o computador perde 1 ponto
     * Além disso, após essa verificação é exibido para o jogador um feedback, exibindo a resposta
     * que o PC forneceu a sua pergunta, a resposta correta e a modificação de pontuação que foi
     * feita no score do seu adversário
     * Em seguida, é perguntado para o jogador se ele deseja seguir para a próxima rodada, ou se
     * quer tentar advinhar sua lâmina; ele terá também 2 minutos para tomar essa decisão.
     */
    private fun estadoI() {
        raffledValue = generateRaffledValue(100, 1)
        myAnswer = raffledValue % 2 == 0
        var slide = 0
        val keySet = game_scene.mySlides.keys.toTypedArray()
        Arrays.sort(keySet)
        game_scene.closeQuestionSelection()
        when (game_scene.slideToGuess) {
            "firstSlide" -> slide = keySet[0]
            "secondSlide" -> slide = keySet[1]
            "thirdSlide" -> slide = keySet[2]
        }
        if (game_scene.getQuestionRealAnswer(game_scene.category, game_scene.question, slide) == myAnswer) {
            game_scene.changePlayerScore(2, 1)
        } else {
            game_scene.changePlayerScore(2, -1)
        }
        game_scene.showQuestionFeedback(myAnswer, game_scene.getQuestionRealAnswer(game_scene.category, game_scene.question, slide))
        state = "I"
        startTimer()
    }

    /**
     * Método utilizado para exibir ao jogador uma lista contendo todas as lâminas disponíveis no
     * jogo, para que ele selecione a que ele acha que está tentando adivinhar
     */
    fun estadoJ() {
        state = "J"
        game_scene.showGuessSlide()
    }

    /**
     * Método utilizado para verificar se a lâmina informada pelo jogador é realmente a que ele
     * está tentando adivinhar, atribuindo pontuação da seguinte forma:
     * - Caso tenha acertado, o jogador ganha 3 pontos
     * - Caso tenha errado, o PC ganha 3 pontos
     * Em seguida, o timer iniciado para o jogador selecionar a lâmina será pausado
     * @param answer - lâmina informada pelo jogador
     */
    fun estadoK(answer: String) {
        stopTimer()
        val keySet = game_scene.mySlides.keys.toTypedArray()
        Arrays.sort(keySet)
        var answerValidation = false
        var matchEnded = false
        var trueSlide = ""
        var systemCode = 0
        var position = 0
        when (game_scene.slideToGuess) {
            "firstSlide" -> {
                trueSlide = game_scene.mySlides[keySet[0]]?.name!!.lowercase()
                systemCode = game_scene.mySlides[keySet[0]]?.system!!
                position = 0
            }
            "secondSlide" -> {
                trueSlide = game_scene.mySlides[keySet[1]]?.name!!.lowercase()
                systemCode = game_scene.mySlides[keySet[1]]?.system!!
                position = 1
            }
            "thirdSlide" -> {
                trueSlide = game_scene.mySlides[keySet[2]]?.name!!.lowercase()
                systemCode = game_scene.mySlides[keySet[2]]?.system!!
                position = 2
            }
        }
        if (trueSlide == answer.lowercase()) {
            game_scene.changePlayerScore(1, 3)
            answerValidation = true
            if (game_scene.slideToGuess == "thirdSlide") matchEnded = true
            game_scene.checkSlide(position, 1)
            game_scene.computePerformance(systemCode, 1)
        } else {
            game_scene.changePlayerScore(2, 3)
        }
        estadoL(answerValidation, matchEnded)
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
    private fun estadoL(answerValidation: Boolean, matchEnded: Boolean) {
        game_scene.closeGuessSlide()
        if (answerValidation) {
            if (matchEnded) game_scene.showTextToPlayer("Resposta correta! Você ganhou 3 pontos! Fim de jogo...") else game_scene.showTextToPlayer("Resposta correta! Você ganhou 3 pontos! Vamos para a próxima rodada...")
        } else {
            game_scene.showTextToPlayer("Resposta incorreta! Seu oponente ganhou 3 pontos! Vamos para a próxima rodada...")
        }
        if (matchEnded) {
            game_scene.slideToGuess = "allDone"
            Handler(Looper.getMainLooper()).postDelayed({ estadoM() }, 2000)
        } else {
            Handler(Looper.getMainLooper()).postDelayed({ estadoA() }, 2000)
        }
    }

    /**
     * Método utilizado para exibir o dialog que informa ao jogador que a partida foi finalizada,
     * exibir as pontuações, informar quem ganhou o jogo e computar as informações relativas a seu
     * desempenho na partida que forem necessárias
     */
    private fun estadoM() {
        if (game_scene.slideToGuess != "allDone") {
            val keySet = game_scene.mySlides.keys.toTypedArray()
            Arrays.sort(keySet)
            when (game_scene.slideToGuess) {
                "firstSlide" -> {
                    game_scene.computePerformance(game_scene.mySlides[keySet[0]]?.system!!, 0)
                    game_scene.computePerformance(game_scene.mySlides[keySet[1]]?.system!!, 0)
                    game_scene.computePerformance(game_scene.mySlides[keySet[2]]?.system!!, 0)
                }
                "secondSlide" -> {
                    game_scene.computePerformance(game_scene.mySlides[keySet[1]]?.system!!, 0)
                    game_scene.computePerformance(game_scene.mySlides[keySet[2]]?.system!!, 0)
                }
                "thirdSlide" -> game_scene.computePerformance(game_scene.mySlides[keySet[2]]?.system!!, 0)
            }
        }
        game_scene.saveMatchInfo(game_scene.getPlayerScore(1) > game_scene.getPlayerScore(2), 1)
        game_scene.showEndGameDialog()
    }

    /**
     * Método que retorna um valor aleatório gerado entre o máximo e o mínimo recebido por parâmetro
     * @param limit - valor máximo do número aleatório que deve ser sorteado
     * @param start - valor mínimo do número aleatório que deve ser sorteado
     * @return - número aleatório gerado
     */
    private fun generateRaffledValue(limit: Int, start: Int): Int {
        return rndGenerator.nextInt(limit) + start
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
    private fun stopTimer() {
        countDownTimer.cancel()
        mTimerRunning = false
        mTimeLeftInMillis = START_TIME_IN_MILLIS
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
            "F" -> {
                game_scene.closeQuestionSelection()
                Handler(Looper.getMainLooper()).postDelayed({ estadoA() }, 2000)
            }
            "I" -> {
                game_scene.closeQuestionFeedback()
                Handler(Looper.getMainLooper()).postDelayed({ estadoA() }, 2000)
            }
            "J" -> {
                game_scene.closeGuessSlide()
                Handler(Looper.getMainLooper()).postDelayed({ estadoA() }, 2000)
            }
        }
    }
}