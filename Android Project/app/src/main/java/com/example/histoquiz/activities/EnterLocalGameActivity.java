package com.example.histoquiz.activities;

import androidx.appcompat.app.AppCompatActivity;
import com.example.histoquiz.R;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Objects;

public class EnterLocalGameActivity extends AppCompatActivity {

    public FirebaseFirestore firestoreDatabase;
    protected Button joinRoom;
    protected TextInputLayout roomCode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_local_game);
        initGUI();
    }

    protected void initGUI(){
        firestoreDatabase = FirebaseFirestore.getInstance();
        joinRoom = findViewById(R.id.entrarNaSala);
        roomCode = findViewById(R.id.codSala);
        joinRoom.setOnClickListener(v -> {
            if(Objects.requireNonNull(roomCode.getEditText()).getText().toString().isEmpty()){
                Toast.makeText(EnterLocalGameActivity.this, "Informe um código de sala.", Toast.LENGTH_LONG).show();
            }
            else{
                joinGameRoom(roomCode.getEditText().getText().toString());
            }
        });
    }

    @SuppressWarnings("unchecked")
    protected void joinGameRoom(String roomCode){
        firestoreDatabase.collection("partidaLocal").whereEqualTo(FieldPath.documentId(), roomCode).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if(!Objects.requireNonNull(task.getResult()).getDocuments().isEmpty()) {
                    DocumentSnapshot document = Objects.requireNonNull(task.getResult()).getDocuments().get(0);
                    if (document.exists()) {
                        int qntdPlayers = Integer.parseInt(Objects.requireNonNull(document.get("qntd")).toString());
                        if (qntdPlayers >= 4) {
                            Toast.makeText(EnterLocalGameActivity.this, "Essa sala já está cheia!", Toast.LENGTH_LONG).show();
                        } else {
                            ArrayList<String> nomeJogadores = (ArrayList<String>) document.get("nomeJogadores");
                            assert nomeJogadores != null;
                            nomeJogadores.set(qntdPlayers, Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getDisplayName());
                            firestoreDatabase.collection("partidaLocal").document(document.getId()).update("nomeJogadores", nomeJogadores);

                            ArrayList<String> uidJogadores = (ArrayList<String>) document.get("uidJogadores");
                            assert uidJogadores != null;
                            uidJogadores.set(qntdPlayers, FirebaseAuth.getInstance().getCurrentUser().getUid());
                            firestoreDatabase.collection("partidaLocal").document(document.getId()).update("uidJogadores", uidJogadores);

                            firestoreDatabase.collection("partidaLocal").document(document.getId()).update("qntd", Integer.toString(Integer.parseInt(Objects.requireNonNull(document.get("qntd")).toString()) + 1));

                            Intent troca = new Intent(EnterLocalGameActivity.this, LocalGameActivity.class);
                            troca.putExtra("matchCreator", false);
                            troca.putExtra("roomCode", roomCode);
                            startActivityForResult(troca, 999);
                        }
                    }
                }
                else {
                    Toast.makeText(EnterLocalGameActivity.this, "Essa sala não existe!", Toast.LENGTH_LONG).show();
                }
            } else {
                Log.d("Erro ao entrar na sala", "Erro: ", task.getException());
            }
        });
    }
}