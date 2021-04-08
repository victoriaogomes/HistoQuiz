package com.example.histoquiz.util;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

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

    protected long START_TIME_IN_MILLIS;
    protected CountDownTimer countDownTimer;
    protected boolean mTimerRunning;
    protected long mTimeLeftInMillis;
    protected String state;
    public String actualSlideToGuess;
    protected int myPlayerCode, myDuoPlayerCode, opponentCode, opponentDuoCode;
    protected FirebaseFirestore firestore;
    public boolean bothEnded = false;
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
        START_TIME_IN_MILLIS = game_scene.roundTime * 1000;
        this.mTimeLeftInMillis = START_TIME_IN_MILLIS;
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
                endTimer();
            }
        };
        addListenerToMatch();
        addListenerToRoundTime();
        addListenerToSlideToGuess();
        game_scene.saveMatchInfo(false, 0);
        game_scene.tipButton.setOnClickListener(v -> _estado_A());
        if(myPlayerCode == 0){
            realtimeDatabase.getReference("partidaLocal/jogo/" + roomName).child("matchState").setValue("player1Setup");
            realtimeDatabase.getReference("partidaLocal/jogo/" + roomName).child("roundTime").setValue(START_TIME_IN_MILLIS);
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
                Log.d(String.format("code %s", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser().getDisplayName()).subSequence(0, 4)), Integer.toString(myPlayerCode));
                switch (i){
                    case 0: myDuoPlayerCode = 1; opponentCode = 2; opponentDuoCode = 3; break;
                    case 1: myDuoPlayerCode = 0; opponentCode = 3; opponentDuoCode = 2; break;
                    case 2: myDuoPlayerCode = 3; opponentCode = 0; opponentDuoCode = 1; break;
                    case 3: myDuoPlayerCode = 2; opponentCode = 1; opponentDuoCode = 0; break;
                }
            }
        }
        Log.d("myPlayerCode", Integer.toString(myPlayerCode));
        Log.d("myDuoPlayerCode", Integer.toString(myDuoPlayerCode));
        Log.d("opponentCode", Integer.toString(opponentCode));
        Log.d("opponentDuoCode", Integer.toString(opponentDuoCode));
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
        addListenerToScore();
    }

    /**
     * Método utilizado para obter a configuração relativa a duração do cronômetro em cada rodada
     */
    protected void addListenerToRoundTime(){
        realtimeDatabase.getReference("partidaLocal/jogo/" + roomName).child("roundTime").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                START_TIME_IN_MILLIS = Integer.parseInt(Objects.requireNonNull(snapshot.getValue()).toString());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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
     *      - player1Dica: Player 1 dando dica para o Player 2
     *      - player2Dica: Player 2 dando dica para o Player 1
     *      - player3Dica: Player 3 dando dica para o Player 4
     *      - player4Dica: Player 4 dando dica para o Player 3
     *      - player1Setup: liberar botão de dica para o Player 1
     *      - player2Setup: liberar botão de dica para o Player 2
     *      - player3Setup: liberar botão de dica para o Player 3
     *      - player4Setup: liberar botão de dica para o Player 4
     *      - ended: partida finalizada
     */
    protected void addListenerToMatch(){
        realtimeDatabase.getReference("partidaLocal/jogo/" + roomName).child("matchState").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue() != null) {
                    matchState = snapshot.getValue().toString();
                    Log.d("Estado:", matchState);
                    //Caso meu duo esteja dando dica, aparece para mim a tela de adivinhar lâmina
                    if (matchState.equals(String.format("player%sDica", (myDuoPlayerCode + 1)))) {
                        game_scene.tipButton.setVisibility(View.GONE);
                        game_scene.showSlide.setVisibility(View.GONE);
                        game_scene.imageSwitcher.setVisibility(View.GONE);
                        game_scene.previous.setVisibility(View.GONE);
                        game_scene.next.setVisibility(View.GONE);
                        if(myPersonalMatchState.equals("onHold")){
                            myPersonalMatchState = "recievingTip";
                            _estado_B();
                        }
                    }
                    // Caso eu seja o próximo a jogar, liberar o botão de dica para mim
                    else if(matchState.equals(String.format("player%sSetup", (myPlayerCode + 1))) || matchState.equals(String.format("player%sSetup", (opponentCode + 1)))){
                        if(matchState.equals(String.format("player%sSetup", (myPlayerCode + 1)))) game_scene.tipButton.setVisibility(View.VISIBLE);
                        game_scene.showSlide.setVisibility(View.VISIBLE);
                        game_scene.imageSwitcher.setVisibility(View.VISIBLE);
                        game_scene.previous.setVisibility(View.VISIBLE);
                        game_scene.next.setVisibility(View.VISIBLE);
                    }
                    //Caso eu que esteja dando dica
                    else if(matchState.equals(String.format("player%sDica", (myPlayerCode + 1)))){
                        game_scene.tipButton.setVisibility(View.VISIBLE);
                    }
                    else if(matchState.equals("ended")){
                        (new Handler(Looper.getMainLooper())).postDelayed(LocalOpponent.this::_estado_F, 2000);
                    }
                    //Caso seja a vez da outra dupla jogar, some com o botão de dica pra mim
                    else{
                        game_scene.tipButton.setVisibility(View.GONE);
                        game_scene.showSlide.setVisibility(View.GONE);
                        game_scene.imageSwitcher.setVisibility(View.GONE);
                        game_scene.previous.setVisibility(View.GONE);
                        game_scene.next.setVisibility(View.GONE);
                    }
                    //else if(matchState.equals("killed")) forceMatchEnd();
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
     * Método utilizado para iniciar o timer após o jogador clicar no botão "Dica", para que ele dê
     * dica para a sua dupla sobre a lâmina que está visualizando
     */
    public void _estado_A(){
        //if(mTimerRunning) stopTimer();
        //myPersonalMatchState = "waiting";
        state = "A";
        realtimeDatabase.getReference("partidaLocal/jogo/" + roomName).child("matchState").setValue(String.format("player%sDica", myPlayerCode+1));
        startTimer();
    }


    /**
     * Método utilizado para exibir para o jogador a lista de lâminas cadastradas no sistema relati-
     * vas ao sistema que ele selecionou para jogar, para que ele escolha aquela que acredita que
     * sua dupla está visualizando
     */
    public void _estado_B(){
        Log.d("Chamei estado", "B");
        state = "B";
        startTimer();
        game_scene.showGuessSlide();
    }


    /**
     * Método utilizado para informar ao jogador que sua resposta fornecida a pergunta do seu opo-
     * nente está sendo validada
     * @param answer - resposta fornecida pelo jogador a lâmina que ele acredita estar tentando
     *                 adivinhar
     */
    public void _estado_C(String answer){
        stopTimer();
        game_scene.roundFeedback.setText("Validando resposta...");
        (new Handler(Looper.getMainLooper())).postDelayed(() -> _estado_D(answer), 2000);
    }


    /**
     * Método que efetivamente verifica se a lâmina informada pelo jogador é realmente a que ele
     * estava tentando adivinhar
     * @param answer - resposta fornecida pelo jogador
     */
    public void _estado_D(String answer){
        myPersonalMatchState = "playing";
        stopTimer();
        Integer [] keySet = game_scene.slides.keySet().toArray(new Integer[0]);
        Arrays.sort(keySet);
        boolean answerValidation = false, matchEnded = false;
        String trueSlide = "";
        int systemCode = 0;
        switch (actualSlideToGuess){
            case "firstSlide":
                trueSlide = Objects.requireNonNull(game_scene.slides.get(keySet[0])).getName().toLowerCase();
                systemCode = Objects.requireNonNull(game_scene.slides.get(keySet[0])).getSystem();
                break;
            case "secondSlide":
                trueSlide = Objects.requireNonNull(game_scene.slides.get(keySet[1])).getName().toLowerCase();
                systemCode = Objects.requireNonNull(game_scene.slides.get(keySet[0])).getSystem();
                break;
            case "thirdSlide":
                trueSlide = Objects.requireNonNull(game_scene.slides.get(keySet[2])).getName().toLowerCase();
                systemCode = Objects.requireNonNull(game_scene.slides.get(keySet[0])).getSystem();
        }
        if(trueSlide.equals(answer.toLowerCase())){
            game_scene.changePlayerScore(1, 3);
            playersRoomRef[myPlayerCode].child("score").setValue(game_scene.getPlayerScore(1));
            playersRoomRef[myDuoPlayerCode].child("score").setValue(game_scene.getPlayerScore(1));
            answerValidation = true;
            if(actualSlideToGuess.equals("thirdSlide")) matchEnded = true;
            realtimeDatabase.getReference("partidaLocal/jogo/" + roomName).child("matchState").setValue(String.format("player%sSetup", opponentDuoCode+1));
            //game_scene.computePerformance(systemCode, 1);
        }
        _estado_E(answerValidation, matchEnded);
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
    public void _estado_E(boolean answerValidation, boolean matchEnded){
        game_scene.closeGuessSlide();
        if(answerValidation){
            if(matchEnded) game_scene.roundFeedback.setText("Resposta correta! Você ganhou 3 pontos! Fim de jogo...");
            else game_scene.roundFeedback.setText("Resposta correta! Você ganhou 3 pontos! Vamos para a próxima rodada...");
        }
        else{
            game_scene.roundFeedback.setText("Resposta incorreta! Seu oponente ganhou 3 pontos! Vamos para a próxima rodada...");
        }
        if(matchEnded){
            actualSlideToGuess = "allDone";
            realtimeDatabase.getReference("partidaLocal/jogo/" + roomName).child("matchState").setValue("ended");
        }
        else{
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
    public void _estado_F(){
        if(!matchCreator){
            realtimeDatabase.getReference("partida/jogo/" + roomName).setValue(null);
        }
        if(!actualSlideToGuess.equals("allDone")){
            Integer [] keySet = game_scene.slides.keySet().toArray(new Integer[0]);
            Arrays.sort(keySet);
            switch (actualSlideToGuess){
                case "firstSlide":
                    game_scene.computePerformance(Objects.requireNonNull(game_scene.slides.get(keySet[0])).getSystem(), 0);
                    game_scene.computePerformance(Objects.requireNonNull(game_scene.slides.get(keySet[1])).getSystem(), 0);
                    game_scene.computePerformance(Objects.requireNonNull(game_scene.slides.get(keySet[2])).getSystem(), 0);
                    break;
                case "secondSlide":
                    game_scene.computePerformance(Objects.requireNonNull(game_scene.slides.get(keySet[1])).getSystem(), 0);
                    game_scene.computePerformance(Objects.requireNonNull(game_scene.slides.get(keySet[2])).getSystem(), 0);
                    break;
                case "thirdSlide":
                    game_scene.computePerformance(Objects.requireNonNull(game_scene.slides.get(keySet[2])).getSystem(), 0);
                    break;
            }
        }
        game_scene.saveMatchInfo(game_scene.getPlayerScore(1) > game_scene.getPlayerScore(2), 1);
        bothEnded = true;
        game_scene.showEndGameDialog();
    }

    protected void _estado_G(){
        game_scene.roundFeedback.setText("Jogada da dupla adversária...");
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
        game_scene.roundFeedback.setText("Acabou o tempo para você realizar uma ação. Vamos para a próxima rodada!");
        mTimeLeftInMillis = START_TIME_IN_MILLIS;
        mTimerRunning = false;
        if(state.equals("B")){
            game_scene.closeGuessSlide();
            realtimeDatabase.getReference("partidaLocal/jogo/" + roomName).child("matchState").setValue(String.format("player%sSetup", opponentCode+1));
        }
        (new Handler(Looper.getMainLooper())).postDelayed(this::_estado_G, 2000);
    }
}
