package com.example.histoquiz.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import com.example.histoquiz.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;
import java.util.Vector;

public class GameActivity extends AppCompatActivity {

    protected Vector<Integer> mySlides = new Vector<>();
    protected FirebaseDatabase database;
    protected DatabaseReference roomRef;
    protected String roomName;
    protected FirebaseUser user;
    protected boolean matchCreator = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        user = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance();
        if(matchCreator){
            //Cria uma sala para o jogo e se adiciona como jogador número 1
            roomName = user.getUid();
            roomRef = database.getReference("partida/jogo/" + roomName + "/player1");
            addRoomEventListener();
            roomRef.setValue(roomName);
        }
        else{//NÃO TESTADO AINDA
            //Entra numa sala para o jogo que foi convidado e se adiciona como jogador número 2
            roomName = user.getUid();
            roomRef = database.getReference("partida/jogo/" + roomName + "/player2");
            addRoomEventListener();
            roomRef.setValue(roomName);
        }
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
     * Método utilizado para sortear as lâminas que este jogador deverá adivinhar e armazená-las
     * no firebase. Cada jogador é responsável por sortear suas lâminas e cadastrá-las na nuvem,
     * para que o seu oponente as obtenha
     */
    protected void raffleSlides(int slidesAmount){
        Random rndGenerator = new Random();
        int raffledValue;
        for(int i=0;i<3;i++){
            raffledValue = rndGenerator.nextInt(slidesAmount);
            while (mySlides.contains(raffledValue)){
                raffledValue = rndGenerator.nextInt(slidesAmount);
            }
            mySlides.add(raffledValue);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == 999) {
//            if(resultCode == RESULT_OK) {
                matchCreator = data.getBooleanExtra("matchCreator", true);
//            }
//        }
    }


}