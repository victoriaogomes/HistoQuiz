package com.example.histoquiz.util;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.example.histoquiz.R;
import com.example.histoquiz.activities.LocalGameActivity;
import com.example.histoquiz.activities.MenuActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

public class LocalOpponent {
    protected LocalGameActivity game_scene;
    public FirebaseDatabase realtimeDatabase;
    public String roomName;

    public DatabaseReference [] playersRoomRef;

    protected static final long START_TIME_IN_MILLIS = 120000;
    protected CountDownTimer countDownTimer;
    protected boolean mTimerRunning;
    protected long mTimeLeftInMillis = START_TIME_IN_MILLIS;
    public String actualSlideToGuess;
    protected int myPlayerCode, myDuoPlayerCode, opponentCode, opponentDuoCode;
    protected FirebaseFirestore firestore;
    protected boolean matchCreator;
    protected String matchState = "onHold";
    protected String myPersonalMatchState = "onHold";

    /**
     * Método construtor da classe, utilizado para fazer as devidas ligações entre os listenners e
     * certos campos no realtimeDatabase, bem como para criar a sala de jogo e realizar algumas con-
     * figurações necessárias para a execução da partida
     * @param game_scene - activity do tipo GameActivity, que instanciou essa classe e irá manipulá-la
     * @param matchCreator - variável booleana que informa se esse jogador é o criador da partida
     */
    public LocalOpponent(LocalGameActivity game_scene, boolean matchCreator, String roomName){
        this.playersRoomRef = new DatabaseReference[4];
        this.actualSlideToGuess = "firstSlide"; // Lâmina que estamos tentando adivinhar nesse momento
        this.matchCreator = matchCreator; // Informa se sou o criador da partida ou não
        this.game_scene = game_scene; // Activity responsável pela cena de jogo que está sendo exibida
        realtimeDatabase = FirebaseDatabase.getInstance();
        firestore = FirebaseFirestore.getInstance();
        Integer [] keySet = game_scene.matchSlides.keySet().toArray(new Integer[0]);
        Arrays.sort(keySet);
        this.roomName = roomName; // Nome da sala, foi definido quando o usuário criou a sala
        if(matchCreator){
            identifyPlayersPosition();
            realtimeDatabase.getReference("partidaLocal/jogo/" + roomName).child("slides").setValue(Arrays.asList(keySet));
            game_scene.imageToShow(0);
        }
        else {
            addListenerToPlayerPos();
            addListenerToSlides();
        }
//        myRoomRef.child("slideToGuess").setValue("firstSlide");
//        myRoomRef.child("answer").setValue("-");
//        myRoomRef.child("categoryId").setValue(-1);
//        myRoomRef.child("questionId").setValue(-1);
//        myRoomRef.child("score").setValue(0);
//        myRoomRef.child("nextRound").setValue("-");
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
                game_scene.timer.setText(String.format(Locale.getDefault(), "Tempo: %s:%s", minutos, segundos));
            }
            @Override
            public void onFinish() {
                //endTimer();
            }
        };
        addListenerToMatch();
        addListenerToSlideToGuess();
        addListenerToNextRound();
        addListenerToScore();
        //game_scene.saveMatchInfo(false, 0);
        if(matchCreator){
            realtimeDatabase.getReference("partidaLocal/jogo/" + roomName).child("matchState").setValue("running");
            _estado_E();
        }
        else{
//            FirebaseFirestore.getInstance().document("partida/convites/" + Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()
//                    + "/" + opponentUID).update("inviteAccepted", "aceito");
            _estado_A();
        }
    }


    /**
     * Método utilizado para obter o id relativo ao número do jogador que eu represento (1, 2, 3 ou 4)
     * e também para obter o id relativo ao número do jogador que a minha dupla representa
     */
    public void identifyPlayersPosition(){
        for(int i=0;i<4;i++){
            if(game_scene.uidJogadores.get(game_scene.playersId[i]).equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())){
                myPlayerCode = i;
                switch (i){
                    case 0: myDuoPlayerCode = 1; opponentCode = 2; opponentDuoCode = 3; break;
                    case 1: myDuoPlayerCode = 0; opponentCode = 2; opponentDuoCode = 3; break;
                    case 2: myDuoPlayerCode = 3; opponentCode = 1; opponentDuoCode = 0; break;
                    case 3: myDuoPlayerCode = 2; opponentCode = 1; opponentDuoCode = 0; break;
                }
            }
        }
        playersRoomRef[0] = realtimeDatabase.getReference("partidaLocal/jogo/" + roomName + "/player1");
        playersRoomRef[1] = realtimeDatabase.getReference("partidaLocal/jogo/" + roomName + "/player2");
        playersRoomRef[2] = realtimeDatabase.getReference("partidaLocal/jogo/" + roomName + "/player3");
        playersRoomRef[3] = realtimeDatabase.getReference("partidaLocal/jogo/" + roomName + "/player4");
        if(matchCreator){
            playersRoomRef[0].child("uid").setValue(game_scene.uidJogadores.get(game_scene.playersId[0]));
            playersRoomRef[0].child("duoUid").setValue(game_scene.uidJogadores.get(game_scene.playersId[1]));
            playersRoomRef[1].child("uid").setValue(game_scene.uidJogadores.get(game_scene.playersId[1]));
            playersRoomRef[1].child("duoUid").setValue(game_scene.uidJogadores.get(game_scene.playersId[0]));
            playersRoomRef[2].child("uid").setValue(game_scene.uidJogadores.get(game_scene.playersId[2]));
            playersRoomRef[2].child("duoUid").setValue(game_scene.uidJogadores.get(game_scene.playersId[3]));
            playersRoomRef[3].child("uid").setValue(game_scene.uidJogadores.get(game_scene.playersId[3]));
            playersRoomRef[3].child("duoUid").setValue(game_scene.uidJogadores.get(game_scene.playersId[2]));
        }
    }

    /**
     * Método utilizado para obter a configuração relativa a posição de cada um dos 4 players dessa
     * partida, para que possamos saber quem é dupla de quem
     */
    @SuppressWarnings("unchecked")
    protected void addListenerToPlayerPos(){
        DocumentReference ref = firestore.collection("partidaLocal").document(game_scene.creator.getActualRoomName());
        ref.addSnapshotListener((documentSnapshot, e) -> {
            assert documentSnapshot != null;
            if(!Objects.equals(documentSnapshot.get("playersId"), new ArrayList<>(Arrays.asList(1, 1, 1, 1)))){
                Long [] auxiliar = new Long[4];
                auxiliar = ((ArrayList<Long>) Objects.requireNonNull(documentSnapshot.get("playersId"))).toArray(auxiliar);
                for(int i=0;i<auxiliar.length;i++){
                    game_scene.playersId[i] = auxiliar[i].intValue();
                }
                identifyPlayersPosition();
            }
        });
    }

    /**
     * Método utilizado para obter as lâminas a serem utilizadas nessa partida, as quais são sortea-
     * das e enviadas pelo usuário que criou essa sala de jogo
     */
    protected void addListenerToSlides(){
        realtimeDatabase.getReference("partidaLocal/jogo/" + roomName).child("slides").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                GenericTypeIndicator<ArrayList<Integer>> t = new GenericTypeIndicator<ArrayList<Integer>>() {};
                ArrayList<Integer> slidesId = snapshot.getValue(t);
                if(slidesId != null) {
                    for(int i=0; i<slidesId.size(); i++){
                        game_scene.matchSlides.put(slidesId.get(i), game_scene.slides.get(slidesId.get(i)));
                    }
                    game_scene.imageToShow(0);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    //*********************************************************************************************************************************************************
    //*********************************************************************************************************************************************************
    //*********************************************************************************************************************************************************
    //*********************************************************************************************************************************************************

    /**
     * Método chamado sempre que o estado atual da partida é alterado. Os estados podem ser:
     *      - onHold: aguardando para começar
     *      - running: partida rolando
     *      - ended: partida finalizada
     */
    protected void addListenerToMatch(){
        realtimeDatabase.getReference("partidaLocal/jogo/" + roomName).child("matchState").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue() != null) {
                    matchState = snapshot.getValue().toString();
                    if (matchState.equals("ended")) {
                        (new Handler(Looper.getMainLooper())).postDelayed(LocalOpponent.this::_estado_L, 2000);
                    }
                    else if(matchState.equals("killed")) forceMatchEnd();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    /**
     * Método chamado sempre que a dupla de oponentes adivinhar uma das suas lâminas disponíveis, para indi-
     * car que o alvo coletivo agora é a lâmina seguinte
     */
    protected void addListenerToSlideToGuess(){
        realtimeDatabase.getReference("partidaLocal/jogo/" + roomName).child("slideToGuess").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue()!=null) {
                    actualSlideToGuess = snapshot.getValue().toString();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    /**
     * Método chamado sempre que o oponente informar que a partida deve seguir para a próxima roda-
     * da, que pode ocorrer porque ele não realizou uma ação no tempo estipulado, ou porque ele não
     * deseja tentar adivinhar sua lâmina no momento
     */
    protected void addListenerToNextRound(){
        realtimeDatabase.getReference("partidaLocal/jogo/" + roomName).child("nextRound").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(myPersonalMatchState.equals("waiting") && matchState.equals("running") && snapshot.getValue()!=null) {
                    if(!snapshot.getValue().equals("-")){
                        _estado_E();
                        realtimeDatabase.getReference("partidaLocal/jogo/" + roomName).child("nextRound").setValue("-");
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



    /**
     * Método utilizado para monitorar mudanças no score do seu oponente e atualizá-la na view do
     * seu jogo
     */
    protected void addListenerToScore(){
        playersRoomRef[myPlayerCode].child("score").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue() != null) {
                    game_scene.myTeamPontuation.setText(String.format(Locale.getDefault(), "%d", Integer.parseInt(snapshot.getValue().toString())));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        playersRoomRef[myDuoPlayerCode].child("score").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue() != null) {
                    game_scene.myTeamPontuation.setText(String.format(Locale.getDefault(), "%d", Integer.parseInt(snapshot.getValue().toString())));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        playersRoomRef[opponentCode].child("score").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue() != null) {
                    game_scene.myOpponentTeamPontuation.setText(String.format(Locale.getDefault(), "%d", Integer.parseInt(snapshot.getValue().toString())));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        playersRoomRef[opponentDuoCode].child("score").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue() != null) {
                    game_scene.myOpponentTeamPontuation.setText(String.format(Locale.getDefault(), "%d", Integer.parseInt(snapshot.getValue().toString())));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    //////////////////////////////////   MÁQUINA DE ESTADOS   /////////////////////////////////////

    /**
     * Método utilizado para solicitar ao jogador que aguarde o seu oponente selecionar uma pergunta
     * para que ele responda
     */
    public void _estado_A(){
        if(mTimerRunning) stopTimer();
        myPersonalMatchState = "waiting";
        //game_scene.showTextToPlayer("Aguardando oponente selecionar uma pergunta...");
    }


    /**
     * Método utilizado para exibir para o jogador a pergunta selecionada por seu oponente para que
     * ele responda. Um timer de 2 minutos deve ser iniciado, que é o tempo que o jogador tem para
     * responder a pergunta
     */
    public void _estado_B(){
        myPersonalMatchState = "playing";
        //game_scene.setQuestionForPlayerAnswer(game_scene.getQuestionText(opponentCategoryId, opponentQuestionId));
        opponentRoomRef.child("questionId").setValue(-1);
        opponentRoomRef.child("categoryId").setValue(-1);
        state = "B";
        startTimer();
    }


    /**
     * Método utilizado para informar ao jogador que sua resposta fornecida a pergunta do seu opo-
     * nente está sendo validada. Esse método também deve pausar o timer iniciado no
     * método anterior (_estado_B())
     * @param answer - resposta fornecida pelo jogador a pergunta que ele recebeu do oponente
     */
    public void _estado_C(boolean answer){
        stopTimer();
        game_scene.showTextToPlayer("Validando resposta...");
        (new Handler(Looper.getMainLooper())).postDelayed(() -> _estado_D(answer), 2000);
    }


    /**
     * Método que efetivamente verifica se a resposta fornecida pelo jogador está correta, realiza a
     * distribuição da pontuação de acordo com isso e fornece o feedback ao jogador. Caso tenha acer-
     * tado a pergunta, o jogador ganha um ponto, e caso tenha errado, ele perde um ponto.
     * @param answer - resposta fornecida pelo jogador
     */
    public void _estado_D(Boolean answer){
        myPersonalMatchState = "waiting";
        String text;
        Integer[] keySet = opponentSlides.keySet().toArray(new Integer[0]);
        Arrays.sort(keySet);
        switch (opponentSlideToGuess) {
            case "firstSlide":
                trueAnswer = game_scene.getQuestionRealAnswer(opponentCategoryId, opponentQuestionId, keySet[0]);
                break;
            case "secondSlide":
                trueAnswer = game_scene.getQuestionRealAnswer(opponentCategoryId, opponentQuestionId, keySet[1]);
                break;
            case "thirdSlide":
                trueAnswer = game_scene.getQuestionRealAnswer(opponentCategoryId, opponentQuestionId, keySet[2]);
                break;
        }
        if(answer != null) {
            if (trueAnswer == answer) {
                game_scene.changePlayerScore(1, 1);
                text = "ganhou 1 ponto!";
            } else {
                game_scene.changePlayerScore(1, -1);
                text = "perdeu 1 ponto!";
            }
            game_scene.showTextToPlayer("Você " + text + " Aguardando oponente analisar sua resposta...");
            if(answer) myRoomRef.child("answer").setValue("sim");
            else myRoomRef.child("answer").setValue("não");
        }
        else{
            game_scene.showTextToPlayer("Aguardando seu oponente analisar a resposta correta da sua pergunta...");
            myRoomRef.child("answer").setValue("null");
        }
    }


    /**
     * Método utilizado para solicitar ao jogador que ele escolha uma categoria e uma pergunta para
     * enviar para seu oponente responder. Além disso, um timer é iniciado, pois o jogador tem 2
     * minutos para realizar essa ação
     */
    public void _estado_E(){
        myPersonalMatchState = "playing";
        game_scene.showQuestionSelection();
        state = "E";
        startTimer();
    }


    /**
     * Método utilizado para enviar ao jogador uma mensagem informando que sua pergunta está
     * sendo repassada ao seu oponente
     */
    public void _estado_F(){
        stopTimer();
        game_scene.closeQuestionSelection();
        game_scene.showTextToPlayer("Enviando...");
        (new Handler(Looper.getMainLooper())).postDelayed(this::_estado_G, 2000);
    }


    /**
     * Método utilizado para informar ao jogador que a resposta do seu oponente a sua pergunta está
     * sendo aguardada
     */
    public void _estado_G(){
        myPersonalMatchState = "waiting";
        myRoomRef.child("categoryId").setValue(category);
        myRoomRef.child("questionId").setValue(question);
        game_scene.showTextToPlayer("Aguardando resposta do oponente...");
    }


    /**
     * Método utilizado para exibir para o jogador um feedback, exibindo a resposta que seu oponente
     * forneceu a sua pergunta, a resposta correta e a modificação de pontuação que foi feita no
     * score do seu adversário
     * Em seguida, é perguntado para o jogador se ele deseja seguir para a próxima rodada, ou se
     * quer tentar advinhar sua lâmina; ele terá também 2 minutos para tomar essa decisão.
     */
    public void _estado_H(){
        myPersonalMatchState = "playing";
        int slide = 0;
        Integer [] keySet = mySlides.keySet().toArray(new Integer[0]);
        Arrays.sort(keySet);
        switch (mySlideToGuess){
            case "firstSlide":
                slide =  keySet[0];
                break;
            case "secondSlide":
                slide =  keySet[1];
                break;
            case "thirdSlide":
                slide =  keySet[2];
                break;
        }
        game_scene.showQuestionFeedback(myOpponentAnswer, game_scene.getQuestionRealAnswer(category, question, slide));
        state = "H";
        startTimer();
    }


    /**
     * Método utilizado para exibir ao jogador uma lista contendo todas as lâminas disponíveis no
     * jogo, para que ele selecione a que ele acha que está tentando adivinhar
     */
    public void _estado_I(){
        myPersonalMatchState = "playing";
        state = "I";
        game_scene.showGuessSlide();
    }


    /**
     * Método utilizado para verificar se a lâmina informada pelo jogador é realmente a que ele
     * está tentando adivinhar, atribuindo pontuação da seguinte forma:
     *      - Caso tenha acertado, o jogador ganha 3 pontos
     *      - Caso tenha errado, seu oponente ganha 3 pontos
     * Em seguida, o timer iniciado para o jogador selecionar a lâmina será pausado
     * @param answer - lâmina informada pelo jogador
     */
    public void _estado_J(String answer){
        myPersonalMatchState = "playing";
        stopTimer();
        Integer [] keySet = mySlides.keySet().toArray(new Integer[0]);
        Arrays.sort(keySet);
        boolean answerValidation = false, matchEnded = false;
        String trueSlide = "";
        int systemCode = 0;
        int position = 0;
        switch (mySlideToGuess){
            case "firstSlide":
                trueSlide = Objects.requireNonNull(mySlides.get(keySet[0])).getName().toLowerCase();
                systemCode = Objects.requireNonNull(mySlides.get(keySet[0])).getSystem();
                position = 0;
                break;
            case "secondSlide":
                trueSlide = Objects.requireNonNull(mySlides.get(keySet[1])).getName().toLowerCase();
                systemCode = Objects.requireNonNull(mySlides.get(keySet[0])).getSystem();
                position = 1;
                break;
            case "thirdSlide":
                trueSlide = Objects.requireNonNull(mySlides.get(keySet[2])).getName().toLowerCase();
                systemCode = Objects.requireNonNull(mySlides.get(keySet[0])).getSystem();
                position = 2;
        }
        if(trueSlide.equals(answer.toLowerCase())){
            game_scene.changePlayerScore(1, 3);
            myRoomRef.child("score").setValue(game_scene.getPlayerScore(1));
            answerValidation = true;
            if(mySlideToGuess.equals("thirdSlide")) matchEnded = true;
            game_scene.checkSlide(position, 1);
            game_scene.computePerformance(systemCode, 1);
        }
        else{
            game_scene.changePlayerScore(2, 3);
            opponentRoomRef.child("score").setValue(game_scene.getPlayerScore(2));
        }
        _estado_K(answerValidation, matchEnded);
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
    public void _estado_K(boolean answerValidation, boolean matchEnded){
        game_scene.closeGuessSlide();
        if(answerValidation){
            if(matchEnded) game_scene.showTextToPlayer("Resposta correta! Você ganhou 3 pontos! Fim de jogo...");
            else game_scene.showTextToPlayer("Resposta correta! Você ganhou 3 pontos! Vamos para a próxima rodada...");
        }
        else{
            game_scene.showTextToPlayer("Resposta incorreta! Seu oponente ganhou 3 pontos! Vamos para a próxima rodada...");
        }
        if(matchEnded){
            mySlideToGuess = "allDone";
            realtimeDatabase.getReference("partida/jogo/" + roomName).child("matchState").setValue("ended");
        }
        else{
            myRoomRef.child("nextRound").setValue("sim");
            (new Handler(Looper.getMainLooper())).postDelayed(this::_estado_A, 2000);
        }
    }


    /**
     * Método utilizado para exibir o dialog que informa ao jogador que a partida foi finalizada,
     * exibir as pontuações, informar quem ganhou o jogo, apagar as informações desse convite de
     * jogo armazenadas no firebase, apagar os dados dessa partida no realtime database e finalizar
     * a atividade de salvar informações relativas a performance desse jogador, caso ele não tenha
     * finalizado essa partida
     */
    public void _estado_L(){
        if(!matchCreator){
            realtimeDatabase.getReference("partida/jogo/" + roomName).setValue(null);
        }
        if(!mySlideToGuess.equals("allDone")){
            Integer [] keySet = mySlides.keySet().toArray(new Integer[0]);
            Arrays.sort(keySet);
            switch (mySlideToGuess){
                case "firstSlide":
                    game_scene.computePerformance(Objects.requireNonNull(mySlides.get(keySet[0])).getSystem(), 0);
                    game_scene.computePerformance(Objects.requireNonNull(mySlides.get(keySet[1])).getSystem(), 0);
                    game_scene.computePerformance(Objects.requireNonNull(mySlides.get(keySet[2])).getSystem(), 0);
                    break;
                case "secondSlide":
                    game_scene.computePerformance(Objects.requireNonNull(mySlides.get(keySet[1])).getSystem(), 0);
                    game_scene.computePerformance(Objects.requireNonNull(mySlides.get(keySet[2])).getSystem(), 0);
                    break;
                case "thirdSlide":
                    game_scene.computePerformance(Objects.requireNonNull(mySlides.get(keySet[2])).getSystem(), 0);
                    break;
            }
        }
        game_scene.saveMatchInfo(game_scene.getPlayerScore(1) > game_scene.getPlayerScore(2), 1);
        bothEnded = true;
        game_scene.showEndGameDialog();
    }

    protected void forceMatchEnd(){
        realtimeDatabase.getReference("partida/jogo/" + roomName).setValue(null);
        AlertDialog.Builder builder = new AlertDialog.Builder(game_scene).setTitle(R.string.fimJogo).setMessage(R.string.endMessage);
        builder.setPositiveButton(R.string.menuInicial, (dialog, id) -> {
            Intent troca = new Intent(game_scene, MenuActivity.class);
            game_scene.startActivity(troca);
        }).create().show();
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
        game_scene.timer.setText(game_scene.getString(R.string.vezOponente));
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
                (new Handler(Looper.getMainLooper())).postDelayed(() -> _estado_D(null), 2000);
                break;
            case "E":
                game_scene.closeQuestionSelection();
                myRoomRef.child("nextRound").setValue("sim");
                (new Handler(Looper.getMainLooper())).postDelayed(this::_estado_A, 2000);
                break;
            case "H":
                game_scene.closeQuestionFeedback();
                myRoomRef.child("nextRound").setValue("sim");
                (new Handler(Looper.getMainLooper())).postDelayed(this::_estado_A, 2000);
                break;
            case "I":
                game_scene.closeGuessSlide();
                myRoomRef.child("nextRound").setValue("sim");
                (new Handler(Looper.getMainLooper())).postDelayed(this::_estado_A, 2000);
                break;
        }
    }
}
