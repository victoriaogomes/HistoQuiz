package com.example.histoquiz.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.histoquiz.R;
import com.example.histoquiz.dialogs.GuessSlideDialog;
import com.example.histoquiz.dialogs.QuestionFeedBackDialog;
import com.example.histoquiz.dialogs.SelectQuestionDialog;
import com.example.histoquiz.util.ComputerOpponent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
public class GameActivity extends AppCompatActivity {

    public Map<Integer, String> mySlides = new HashMap<>();
    protected FirebaseDatabase realtimeDatabase;
    protected FirebaseFirestore firestoreDatabase;
    public DatabaseReference roomRef;
    protected String roomName, opponentUID;
    protected FirebaseUser user;
    protected boolean matchCreator, PCopponent;
    public HashMap<String, Map<String, Object>> perguntas;
    public String slideToGuess = "firstSlide";
    protected int category, question;
    protected TextView questionText, scorePlayer1, scorePlayer2;
    protected Button yesAnswer, noAnswer;
    public ComputerOpponent myOpponent;
    protected SelectQuestionDialog selectQuestionDialog;
    protected GuessSlideDialog guessSlideDialog;
    protected QuestionFeedBackDialog questionFeedBackDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        matchCreator = intent.getBooleanExtra("matchCreator", true);
        opponentUID = intent.getStringExtra("opponentUID");
        PCopponent = intent.getBooleanExtra("PCopponent", true);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN);
        setContentView(R.layout.activity_game);
        initGUI();
        handleAnswerButtons();
        firestoreDatabase.collection("laminas").get().addOnSuccessListener(queryDocumentSnapshots -> {
            if(PCopponent) {
                raffleSlides(queryDocumentSnapshots.size(), queryDocumentSnapshots, 6);
            }
            else{
                raffleSlides(queryDocumentSnapshots.size(), queryDocumentSnapshots, 3);
            }
        });
        firestoreDatabase.collection("perguntas").get().addOnSuccessListener(queryDocumentSnapshots -> {
            perguntas = new HashMap<>();
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                perguntas.put(document.getId(), document.getData());
            }
            if(PCopponent){
                myOpponent = new ComputerOpponent(this);
            }
        });
        if(!PCopponent) {
            if (matchCreator) {
                //Cria uma sala para o jogo e se adiciona como jogador número 1
                roomName = user.getUid();
                roomRef = realtimeDatabase.getReference("partida/jogo/" + roomName + "/player1");
            } else {//NÃO TESTADO AINDA
                //Entra numa sala para o jogo que foi convidado e se adiciona como jogador número 2
                roomName = opponentUID;
                roomRef = realtimeDatabase.getReference("partida/jogo/" + roomName + "/player2");
            }
            roomRef.setValue(roomName);
            addRoomEventListener();
        }
    }


    /**
     * Método utilizado para obter a referência para certos objetos da view que serão manipulados
     * por essa classe e para inicializar alguns outros objetos utilizados aqui no back-end
     */
    protected void initGUI(){
        questionText = findViewById(R.id.pergunta);
        yesAnswer = findViewById(R.id.respSim);
        noAnswer = findViewById(R.id.respNao);
        scorePlayer1 = findViewById(R.id.pontuacaoJogador1);
        scorePlayer2 = findViewById(R.id.pontuacaoJogador2);
        questionText.setVisibility(View.INVISIBLE);
        yesAnswer.setVisibility(View.INVISIBLE);
        noAnswer.setVisibility(View.INVISIBLE);
        user = FirebaseAuth.getInstance().getCurrentUser();
        realtimeDatabase = FirebaseDatabase.getInstance();
        firestoreDatabase = FirebaseFirestore.getInstance();
        selectQuestionDialog = new SelectQuestionDialog(this);
        guessSlideDialog = new GuessSlideDialog(this);
    }


    protected void addRoomEventListener(){
        roomRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    /**
     * Método utilizado para exibir ao usuário a tela para que ele selecione uma questão para
     * enviar para seu oponente responder
     */
    public void showQuestionSelection(){
        selectQuestionDialog.show(getSupportFragmentManager(), "choose question dialog");
    }


    /**
     * Método utilizado para deixar de exibir a tela utilizada para selecionar uma questão
     * para o oponente responder
     */
    public void closeQuestionSelection(){
        selectQuestionDialog.dismiss();
    }


    /**
     * Método utilizado para exibir ao usuário a tela para que ele adivinhe a sua lâmina
     * atual
     */
    public void showGuessSlide(){
        guessSlideDialog.show(getSupportFragmentManager(), "guess dialog");
    }

    /**
     * Método utilizado para deixar de exibir ao usuário a tela para que ele adivinhe sua lâmina
     * atual
     */
    public void closeGuessSlide(){
        guessSlideDialog.dismiss();
    }


    /**
     * Método utilizado para exibir ao usuário a resposta a sua pergunta dada pelo
     * seu oponente, bem como a resposta correta
     */
    public void showQuestionFeedback(boolean opponentAnswer, boolean correctAnswer){
        questionFeedBackDialog = new QuestionFeedBackDialog(this, opponentAnswer, correctAnswer);
        questionFeedBackDialog.show(getSupportFragmentManager(), "questionFeedBack");
    }


    public void closeQuestionFeedback(){
        questionFeedBackDialog.dismiss();
    }


    public void showTextToWaitOpponent(String text){
        this.questionText.setText(text);
        this.questionText.setVisibility(View.VISIBLE);
        yesAnswer.setVisibility(View.INVISIBLE);
        noAnswer.setVisibility(View.INVISIBLE);
    }


    public void closeTextToWaitOpponent(){
        this.questionText.setVisibility(View.INVISIBLE);
    }



    /**
     * Método utilizado para sortear as lâminas que este jogador deverá adivinhar e armazená-las
     * no firebase. Cada jogador é responsável por sortear suas lâminas e cadastrá-las na nuvem,
     * para que o seu oponente as obtenha
     */
    protected void raffleSlides(int slidesAmount, QuerySnapshot queryDocumentSnapshot, int raffleNumber) {
        Random rndGenerator = new Random();
        int raffledValue;
        for (int i = 0; i < raffleNumber; i++) {
            raffledValue = rndGenerator.nextInt(slidesAmount);
            while (mySlides.containsKey(raffledValue)) {
                raffledValue = rndGenerator.nextInt(slidesAmount);
            }
            mySlides.put(raffledValue, queryDocumentSnapshot.getDocuments().get(raffledValue).getId());
        }
//        roomRef.child("slides").setValue(mySlides);
//        roomRef.child("score").setValue(0);
//        roomRef.child("selectedCategory").setValue(0);
//        roomRef.child("selectedQuestion").setValue(0);
//        roomRef.child("firstSlide").setValue(false);
//        roomRef.child("secondSlide").setValue(false);
//        roomRef.child("thirdSlide").setValue(false);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 999) {
            if(resultCode == RESULT_OK) {
                matchCreator = data.getBooleanExtra("matchCreator", true);
                opponentUID = data.getStringExtra("opponentUID");
                PCopponent = data.getBooleanExtra("PCopponent", true);
            }
        }
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public void setQuestion(int question) {
        this.question = question;
    }

    public int getCategory(){
        return category;
    }

    public int getQuestion(){
        return question;
    }


    public void setQuestionForPlayerAnswer(String questionText) {
        this.questionText.setText(questionText);
        this.questionText.setVisibility(View.VISIBLE);
        yesAnswer.setVisibility(View.VISIBLE);
        noAnswer.setVisibility(View.VISIBLE);
    }

    public void handleAnswerButtons(){
        yesAnswer.setOnClickListener(v -> {
            if (PCopponent){
                myOpponent._estado_0010(true);
            }
        });
        noAnswer.setOnClickListener(v -> myOpponent._estado_0010(false));
    }


    public void handleQuestionSelectionButton(){
        if(PCopponent){
            myOpponent._estado_0101();
        }
    }


    public String getQuestionText(int category, int question){
        return Objects.requireNonNull(perguntas.get(perguntas.keySet().toArray()[category])).keySet().toArray()[question].toString();
    }

    public boolean getQuestionRealAnswer(int category, int question, int slide){
        String cat = perguntas.keySet().toArray()[category].toString();
        String quest = Objects.requireNonNull(perguntas.get(cat)).keySet().toArray()[question].toString();
        ArrayList<Boolean> respostas = (ArrayList<Boolean>) Objects.requireNonNull(perguntas.get(cat)).get(quest);
        return respostas.get(slide);
    }

    public void incrementPlayerScore(int player){
        switch (player){
            case 1: scorePlayer1.setText(String.format(Locale.ENGLISH, "%d", Integer.parseInt(scorePlayer1.getText().toString()) + 2)); break;
            case 2: scorePlayer2.setText(String.format(Locale.ENGLISH, "%d", Integer.parseInt(scorePlayer2.getText().toString()) + 2)); break;
        }
    }

    public void decrementPlayerScore(int player){
        switch (player){
            case 1: scorePlayer1.setText(String.format(Locale.ENGLISH, "%d", Integer.parseInt(scorePlayer1.getText().toString()) - 1)); break;
            case 2: scorePlayer2.setText(String.format(Locale.ENGLISH, "%d", Integer.parseInt(scorePlayer2.getText().toString()) - 1)); break;
        }
    }
}