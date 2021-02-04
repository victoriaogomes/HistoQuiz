//package com.example.histoquiz.util;
//
//import android.os.CountDownTimer;
//
//import com.example.histoquiz.activities.LocalGameActivity;
//import com.example.histoquiz.model.Slide;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.firestore.FirebaseFirestore;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.Locale;
//import java.util.Map;
//import java.util.Objects;
//
//public class LocalOpponent {
//    protected LocalGameActivity game_scene;
//    public FirebaseDatabase realtimeDatabase;
//    public String roomName;
//
//    public DatabaseReference myRoomRef, opponentRoomRef;
//    public int question, category;
//
//    public Map<Integer, Slide> mySlides = new HashMap<>();
//    public Map<Integer, Slide> opponentSlides = new HashMap<>();
//    protected static final long START_TIME_IN_MILLIS = 120000;
//    protected CountDownTimer countDownTimer;
//    protected boolean mTimerRunning;
//    protected long mTimeLeftInMillis = START_TIME_IN_MILLIS;
//    protected String state;
//    protected int opponentCategoryId, opponentQuestionId;
//    public String actualSlideToGuess;
//    protected FirebaseFirestore firestore;
//    public boolean bothEnded = false;
//    protected String matchState = "onHold";
//    protected String myPersonalMatchState = "onHold";
//    protected boolean matchCreator;
//
//    /**
//     * Método construtor da classe, utilizado para fazer as devidas ligações entre os listenners e
//     * certos campos no realtimeDatabase, bem como para criar a sala de jogo e realizar algumas con-
//     * figurações necessárias para a execução da partida
//     * @param game_scene - activity do tipo GameActivity, que instanciou essa classe e irá manipulá-la
//     * @param matchCreator - variável booleana que informa se esse jogador é o criador da partida
//     */
//    public LocalOpponent(LocalGameActivity game_scene, boolean matchCreator, String roomName){
//        this.actualSlideToGuess = "firstSlide"; // Lâmina que estamos tentando adivinhar nesse momento
//        this.matchCreator = matchCreator; // Informa se sou o criador da partida ou não
//        this.game_scene = game_scene; // Activity responsável pela cena de jogo que está sendo exibida
//        realtimeDatabase = FirebaseDatabase.getInstance();
//        firestore = FirebaseFirestore.getInstance();
//        Integer [] keySet = game_scene.matchSlides.keySet().toArray(new Integer[0]);
//        Arrays.sort(keySet);
//        this.roomName = roomName; // Nome da sala, foi definido quando o usuário criou a sala
//        if (matchCreator) {
//            //Cria uma sala para o jogo e se adiciona como jogador número 1
//            myRoomRef = realtimeDatabase.getReference("partida/jogo/" + roomName + "/player1");
//            opponentRoomRef = realtimeDatabase.getReference("partida/jogo/" + roomName + "/player2");
//            myRoomRef.child("UID").setValue(roomName);
//            realtimeDatabase.getReference("partida/jogo/" + roomName).child("slides")
//                    .setValue(new ArrayList<>(Arrays.asList(keySet[0], keySet[1], keySet[2],
//                            keySet[3], keySet[4], keySet[5])));
//            mySlides.put(keySet[0], game_scene.slides.get(keySet[0]));
//            mySlides.put(keySet[1], game_scene.slides.get(keySet[1]));
//            mySlides.put(keySet[2], game_scene.slides.get(keySet[2]));
//
//            opponentSlides.put(keySet[3], game_scene.slides.get(keySet[3]));
//            opponentSlides.put(keySet[4], game_scene.slides.get(keySet[4]));
//            opponentSlides.put(keySet[5], game_scene.slides.get(keySet[5]));
//        } else {
//            myRoomRef = realtimeDatabase.getReference("partida/jogo/" + roomName + "/player2");
//            opponentRoomRef = realtimeDatabase.getReference("partida/jogo/" + roomName + "/player1");
//            myRoomRef.child("UID").setValue(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
//            addListenerToSlides();
//        }
//        myRoomRef.child("slideToGuess").setValue("firstSlide");
//        myRoomRef.child("answer").setValue("-");
//        myRoomRef.child("categoryId").setValue(-1);
//        myRoomRef.child("questionId").setValue(-1);
//        myRoomRef.child("score").setValue(0);
//        myRoomRef.child("nextRound").setValue("-");
//        countDownTimer = new CountDownTimer(START_TIME_IN_MILLIS, 1000) {
//            @Override
//            public void onTick(long l) {
//                String minutos, segundos;
//                mTimeLeftInMillis = l;
//                int minutes = (int) (mTimeLeftInMillis/1000)/60;
//                int seconds = (int) (mTimeLeftInMillis/1000)%60;
//                if(minutes < 10) minutos = "0" + minutes;
//                else minutos = Integer.toString(minutes);
//                if(seconds < 10) segundos = "0" + seconds;
//                else segundos = Integer.toString(seconds);
//                game_scene.timer.setText(String.format(Locale.getDefault(), "Tempo: %s:%s", minutos, segundos));
//            }
//            @Override
//            public void onFinish() {
//                endTimer();
//            }
//        };
//        addListenerToMatch();
//        addListenerToSlideToGuess();
//        addListenerToAnswer();
//        addListenerToCategory();
//        addListenerToNextRound();
//        addListenerToQuestion();
//        addListenerToScore();
//        game_scene.saveMatchInfo(false, 0);
//        if(matchCreator){
//            realtimeDatabase.getReference("partida/jogo/" + roomName).child("matchState").setValue("running");
//            _estado_E();
//        }
//        else{
//            FirebaseFirestore.getInstance().document("partida/convites/" + Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()
//                    + "/" + opponentUID).update("inviteAccepted", "aceito");
//            _estado_A();
//        }
//    }
//}
