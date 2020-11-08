package com.example.histoquiz.util;

import android.os.CountDownTimer;
import android.os.Handler;
import android.widget.Toast;
import com.example.histoquiz.activities.GameActivity;
import com.example.histoquiz.model.Slide;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class ComputerOpponent {

    protected GameActivity game_scene; // Cena do jogo
    public String slideToGuess = "firstSlide";
    protected Random rndGenerator;
    protected int raffledCategory, raffledQuestion, numberOfQuestions, raffledValue;
    boolean myAnswer;
    protected boolean general = true; // Variável para indicar se o PC já pode selecionar perguntas de outra categoria
    protected HashMap<String, Map<String, Object>> perguntas;
    protected HashMap<Integer, Slide> slides;
    protected boolean trueAnswer;
    protected LinkedList<String> askedQuestions;

    protected static final long START_TIME_IN_MILLIS = 120000;
    protected CountDownTimer countDownTimer;
    protected boolean mTimerRunning;
    protected long mTimeLeftInMillis = START_TIME_IN_MILLIS;
    protected String state;


    /**
     * Método construtor da classe, recebe a activity responsável por criá-la e gerenciá-la, as per-
     * guntas armazenadas no firebase e as lâminas disponíveis
     * @param game_scene - scene que instanciou essa classe
     * @param perguntas - perguntas relativas a lâminas, e separadas por categorias, que estão dis-
     *                    poníveis no firebase
     * @param slides - lâminas cadastradas no firebase
     */
    public ComputerOpponent(GameActivity game_scene, HashMap<String, Map<String, Object>> perguntas, HashMap<Integer, Slide> slides){
        this.game_scene = game_scene;
        this.perguntas = perguntas;
        this.slides = (HashMap<Integer, Slide>) slides.clone();
        askedQuestions = new LinkedList<>();
        for(int i=0; i<perguntas.keySet().size(); i++){
            if(perguntas.keySet().toArray()[i].equals("Gerais")){
                raffledCategory = i;
            }
        }
        countDownTimer = new CountDownTimer(START_TIME_IN_MILLIS, 1000) {
            @Override
            public void onTick(long l) {
                String minutos, segundos;
                mTimeLeftInMillis = l;
                int minutes = (int) (mTimeLeftInMillis/1000)/60;
                int seconds = (int) (mTimeLeftInMillis/1000)%60;
                if(minutes < 10) minutos = "0" + minutes;
                else minutos = Integer.toString(minutes);
                if(seconds < 10) segundos = "0" + seconds;
                else segundos = Integer.toString(seconds);
                game_scene.timer.setText("Tempo: " + minutos + ":" + segundos);
            }
            @Override
            public void onFinish() {
                endTimer();
            }
        };
        rndGenerator = new Random();
        raffledValue = generateRaffledValue(100, 1);
        _estado_A();
    }


    /**
     * Método utilizado para solicitar ao jogador que aguarde o seu oponente selecionar uma pergunta
     * para que ele responda
     */
    public void _estado_A(){
        game_scene.showTextToPlayer("Aguardando oponente selecionar uma pergunta...");
        (new Handler()).postDelayed(this::_estado_B, 2000);
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
    public void _estado_B(){
        if(!general){
            raffledCategory = generateRaffledValue(perguntas.keySet().size(), 0);
        }
        numberOfQuestions = Objects.requireNonNull(perguntas.get(game_scene.getCategoryName(raffledCategory))).size();
        raffledQuestion = generateRaffledValue(numberOfQuestions, 0);
        while (askedQuestions.contains(game_scene.getQuestionText(raffledCategory, raffledQuestion))){
            if(!general) raffledCategory = generateRaffledValue(perguntas.keySet().size(), 0);
            numberOfQuestions = Objects.requireNonNull(perguntas.get(game_scene.getCategoryName(raffledCategory))).size();
            raffledQuestion = generateRaffledValue(numberOfQuestions, 0);
        }
        askedQuestions.add(game_scene.getQuestionText(raffledCategory, raffledQuestion));
        game_scene.setQuestionForPlayerAnswer(game_scene.getQuestionText(raffledCategory, raffledQuestion));
        state = "B";
        startTimer();
    }


    /**
     * Método utilizado para informar ao jogador que sua resposta fornecida a pergunta do seu opo-
     * nente (Computador) está sendo validada. Esse método também deve pausar o timer iniciado no
     * método anterior (_estado_B())
     * @param answer - resposta fornecida pelo jogador a pergunta que ele recebeu do PC
     */
    public void _estado_C(boolean answer){
        stopTimer();
        game_scene.showTextToPlayer("Validando resposta...");
        (new Handler()).postDelayed(() -> _estado_D(answer), 2000);
    }


    /**
     * Método que efetivamente verifica se a resposta fornecida pelo jogador está correta, realiza a
     * distribuição da pontuação de acordo com isso e fornece o feedback ao jogador. Caso tenha acer-
     * tado a pergunta, o jogador ganha um ponto, e caso tenha errado, ele perde um ponto.
     * @param answer - resposta fornecida pelo jogador
     */
    public void _estado_D(Boolean answer){
        Object[] keySet = game_scene.mySlides.keySet().toArray();
        String text;
        switch (slideToGuess) {
            case "firstSlide":
                trueAnswer = game_scene.getQuestionRealAnswer(raffledCategory, raffledQuestion, Integer.parseInt(keySet[3].toString()));
                break;
            case "secondSlide":
                trueAnswer = game_scene.getQuestionRealAnswer(raffledCategory, raffledQuestion, Integer.parseInt(keySet[4].toString()));
                break;
            case "thirdSlide":
                trueAnswer = game_scene.getQuestionRealAnswer(raffledCategory, raffledQuestion, Integer.parseInt(keySet[5].toString()));
                break;
        }
        if (trueAnswer) general = false;
        if(answer != null) {
            if (trueAnswer == answer) {
                game_scene.changePlayerScore(1, 1);
                text = "ganhou 1 ponto!";
            } else {
                game_scene.changePlayerScore(1, -1);
                text = "perdeu 1 ponto!";
            }
            game_scene.showTextToPlayer("Você " + text + " Aguardando oponente analisar sua resposta...");
        }
        (new Handler()).postDelayed(() -> _estado_E(trueAnswer), 2000);
    }


    /**
     * Método utilizado para remover do roll de possibilidades as lâminas cuja resposta a pergunta
     * feita pelo PC não se adequam a resposta verdadeira recebida. Além disso, aqui é verificado
     * quantas possibilidades de lâminas ainda estão disponíveis para serem advinhadas e, caso sejam
     * 3 ou menos, o PC tenta adivinhar sua lâmina
     * @param trueAnswer - resposta verdadeira da pergunta selecionada pelo PC para o jogador
     *                     responder
     */
    public void _estado_E(boolean trueAnswer){
        int delay = 1000;
        HashMap<Integer, Slide> copy = (HashMap<Integer, Slide>) slides.clone();
        for (Map.Entry<Integer,Slide> pair : copy.entrySet()) {
            if (game_scene.getQuestionRealAnswer(raffledCategory, raffledQuestion, pair.getKey()) != trueAnswer){
                slides.remove(pair.getKey());
            }
        }
        Toast.makeText(game_scene, "Possibilidades: " + slides.size(), Toast.LENGTH_LONG).show();
        if(slides.size() <= 3 && slides.size()>0){
            int position = 0;
            delay = 3000;
            Integer [] keySet = game_scene.mySlides.keySet().toArray(new Integer[0]);
            String slideName = "";
            switch (slideToGuess){
                case "firstSlide":
                    slideName = Objects.requireNonNull(game_scene.mySlides.get(keySet[3])).getName();
                    position = 0;
                    break;
                case "secondSlide":
                    slideName = Objects.requireNonNull(game_scene.mySlides.get(keySet[4])).getName();
                    position = 1;
                    break;
                case "thirdSlide":
                    slideName = Objects.requireNonNull(game_scene.mySlides.get(keySet[5])).getName();
                    position = 2;
                    break;
            }
            if(slideName.equals(Objects.requireNonNull(slides.get(slides.keySet().toArray(new Integer[0])[0])).getName())){
                game_scene.showTextToPlayer("Seu oponente adivinhou sua lâmina e ganhou 3 pontos!");
                game_scene.checkSlide(position, 2);
                game_scene.changePlayerScore(2, 3);
                slides = (HashMap<Integer, Slide>) game_scene.slides.clone();
                general = true;
                for(int i=0; i<perguntas.keySet().size(); i++){
                    if(perguntas.keySet().toArray()[i].equals("Gerais")){
                        raffledCategory = i;
                    }
                }
                askedQuestions = new LinkedList<>();
                if(position == 2){
                    _estado_M();
                }
            }
            else{
                game_scene.showTextToPlayer("Seu oponente tentou adivinhar sua lâmina e errou. Você ganhou 3 pontos!");
                game_scene.changePlayerScore(1, 3);
                slides.remove(Integer.parseInt(slides.keySet().toArray()[0].toString()));
            }
        }
        (new Handler()).postDelayed(this::_estado_F, delay);
    }


    /**
     * Método utilizado para solicitar ao jogador que ele escolha uma categoria e uma pergunta para
     * enviar para o PC responder. Além disso, um timer é iniciado, pois o jogador tem 2 minutos
     * para realizar essa ação
     */
    public void _estado_F(){
        game_scene.showQuestionSelection();
        state = "F";
        startTimer();
    }


    /**
     * Método utilizado para enviar ao jogador uma mensagem informando que sua pergunta está
     * sendo repassada ao seu oponente (nesse caso, o computador)
     */
    public void _estado_G(){
        stopTimer();
        game_scene.closeQuestionSelection();
        game_scene.showTextToPlayer("Enviando...");
        (new Handler()).postDelayed(this::_estado_H, 2000);
    }


    /**
     * Método utilizado para informar ao jogador que a resposta do seu oponente (o PC) a sua pergun-
     * ta está sendo aguardada
     */
    public void _estado_H(){
        game_scene.showTextToPlayer("Aguardando resposta do oponente...");
        (new Handler()).postDelayed(this::_estado_I, 2000);
    }


    /**
     * Método utilizado para sortear uma resposta para o computador fornecer a pergunta do jogador,
     * e para fazer a modificação da pontuação do computador da seguinte forma:
     *      - Caso tenha respondido corretamente, o computador ganha 1 ponto
     *      - Caso tenha respondido erroneamente, o computador perde 1 ponto
     * Além disso, após essa verificação é exibido para o jogador um feedback, exibindo a resposta
     * que o PC forneceu a sua pergunta, a resposta correta e a modificação de pontuação que foi
     * feita no score do seu adversário
     * Em seguida, é perguntado para o jogador se ele deseja seguir para a próxima rodada, ou se
     * quer tentar advinhar sua lâmina; ele terá também 2 minutos para tomar essa decisão.
     */
    public void _estado_I(){
        raffledValue = generateRaffledValue(100, 1);
        myAnswer = raffledValue % 2 == 0;
        int slide = 0;
        Integer [] keySet = game_scene.mySlides.keySet().toArray(new Integer[0]);
        game_scene.closeQuestionSelection();
        switch (game_scene.slideToGuess){
            case "firstSlide":
                slide = keySet[0];
                break;
            case "secondSlide":
                slide = keySet[1];
                break;
            case "thirdSlide":
                slide = keySet[2];
                break;
        }
        if(game_scene.getQuestionRealAnswer(game_scene.getCategory(), game_scene.getQuestion(), slide) == myAnswer){
            game_scene.changePlayerScore(2, 1);
        }
        else{
            game_scene.changePlayerScore(2, -1);
        }
        game_scene.showQuestionFeedback(myAnswer, game_scene.getQuestionRealAnswer(game_scene.getCategory(), game_scene.getQuestion(), slide));
        state = "I";
        startTimer();
    }


    /**
     * Método utilizado para exibir ao jogador uma lista contendo todas as lâminas disponíveis no
     * jogo, para que ele selecione a que ele acha que está tentando adivinhar
     */
    public void _estado_J(){
        state = "J";
        game_scene.showGuessSlide();
    }


    /**
     * Método utilizado para verificar se a lâmina informada pelo jogador é realmente a que ele
     * está tentando adivinhar, atribuindo pontuação da seguinte forma:
     *      - Caso tenha acertado, o jogador ganha 3 pontos
     *      - Caso tenha errado, o PC ganha 3 pontos
     * Em seguida, o timer iniciado para o jogador selecionar a lâmina será pausado
     * @param answer - lâmina informada pelo jogador
     */
    public void _estado_K(String answer){
        stopTimer();
        Integer [] keySet = game_scene.mySlides.keySet().toArray(new Integer[0]);
        boolean answerValidation = false, matchEnded = false;
        String trueSlide = "";
        int position = 0;
        switch (game_scene.slideToGuess){
            case "firstSlide":
                trueSlide = Objects.requireNonNull(game_scene.mySlides.get(keySet[0])).getName().toLowerCase();
                position = 0;
                break;
            case "secondSlide":
                trueSlide = Objects.requireNonNull(game_scene.mySlides.get(keySet[1])).getName().toLowerCase();
                position = 1;
                break;
            case "thirdSlide":
                trueSlide = Objects.requireNonNull(game_scene.mySlides.get(keySet[2])).getName().toLowerCase();
                position = 2;
        }
        if(trueSlide.equals(answer.toLowerCase())){
            game_scene.changePlayerScore(1, 3);
            answerValidation = true;
            if(game_scene.slideToGuess.equals("thirdSlide")) matchEnded = true;
            game_scene.checkSlide(position, 1);
        }
        else{
            game_scene.changePlayerScore(2, 3);
        }
        _estado_L(answerValidation, matchEnded);
    }


    /**
     * Método utilizado para exibir ao jogador o feedback relativo a sua tentativa de advinhar sua
     * lâmina e:
     *      - Seguir para a próxima rodada,caso ainda hajam lâminas para serem adivinhadas
     *      - Seguir para o fim do jogo, caso o jogador já tenha advinhado todas as suas lâminas
     * @param answerValidation - validação da resposta; true se o jogador tiver acertado a lâmina,
     *                          e false caso tenha errado
     * @param matchEnded - variável que indica se a partida deve ser finalizada ou não
     */
    public void _estado_L(boolean answerValidation, boolean matchEnded){
        game_scene.closeGuessSlide();
        if(answerValidation){
            if(matchEnded) game_scene.showTextToPlayer("Resposta correta! Você ganhou 3 pontos! Fim de jogo...");
            else game_scene.showTextToPlayer("Resposta correta! Você ganhou 3 pontos! Vamos para a próxima rodada...");
        }
        else{
            game_scene.showTextToPlayer("Resposta incorreta! Seu oponente ganhou 3 pontos! Vamos para a próxima rodada...");
        }
        if(matchEnded){
            (new Handler()).postDelayed(this::_estado_M, 2000);
        }
        else{
            (new Handler()).postDelayed(this::_estado_A, 2000);
        }
    }


    /**
     * Método utilizado para exibir o dialog que informa ao jogador que a partida foi finalizada,
     * exibir as pontuações e informar quem ganhou o jogo
     */
    public void _estado_M(){
        game_scene.showEndGameDialog();
    }


    /**
     * Método que retorna um valor aleatório gerado entre o máximo e o mínimo recebido por parâmetro
     * @param limit - valor máximo do número aleatório que deve ser sorteado
     * @param start - valor mínimo do número aleatório que deve ser sorteado
     * @return - número aleatório gerado
     */
    protected int generateRaffledValue(int limit, int start){
        return rndGenerator.nextInt(limit) + start;
    }


    /**
     * Método utilizado para iniciar o timer utilizado na partida
     */
    protected void startTimer(){
        if(mTimerRunning) stopTimer();
        countDownTimer.start();
        mTimerRunning = true;
    }


    /**
     * Método utilizado para pausar o timer utilizado na partida
     */
    protected void stopTimer(){
        countDownTimer.cancel();
        mTimerRunning = false;
        mTimeLeftInMillis = START_TIME_IN_MILLIS;
    }


    /**
     * Método chamado quando o timer utilizado chega a 00min:00s. Ele exibe ao usuário uma mensagem
     * informando a ele que o tempo para realização de uma ação acabou, aguarda um tempo e segue
     * para a próxima ação do jogo
     */
    protected void endTimer(){
        game_scene.showTextToPlayer("Acabou o tempo para você realizar uma ação. Vamos para a próxima rodada!");
        mTimeLeftInMillis = START_TIME_IN_MILLIS;
        mTimerRunning = false;
        switch (state){
            case "B":
                (new Handler()).postDelayed(() -> _estado_D(null), 2000);
                break;
            case "F":
                game_scene.closeQuestionSelection();
                (new Handler()).postDelayed(this::_estado_A, 2000);
                break;
            case "I":
                game_scene.closeQuestionFeedback();
                (new Handler()).postDelayed(this::_estado_A, 2000);
                break;
            case "J":
                game_scene.closeGuessSlide();
                (new Handler()).postDelayed(this::_estado_A, 2000);
                break;
        }
    }

}
