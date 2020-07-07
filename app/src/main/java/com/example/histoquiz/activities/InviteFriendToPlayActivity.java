package com.example.histoquiz.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.histoquiz.R;
import com.example.histoquiz.util.FormFieldValidator;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Objects;

public class InviteFriendToPlayActivity extends AppCompatActivity {

    protected Button sendInvitation;
    protected FormFieldValidator fieldValitator;
    protected TextInputLayout friendEmail;
    protected FirebaseFirestore database;
    protected FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_friend_to_play);
        sendInvitation = findViewById(R.id.enviarConviteButton);
        friendEmail = findViewById(R.id.email);
        fieldValitator = new FormFieldValidator(this);
        fieldValitator.monitorarCampo(friendEmail);
        database = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        sendInviteToPlay();
    }


    protected void sendInviteToPlay(){
        sendInvitation.setOnClickListener(v -> {
            if(fieldValitator.preenchido(friendEmail)){
                Task<DocumentSnapshot> friendUID = database.collection("tabelaAuxiliar").document(Objects.requireNonNull(friendEmail.getEditText()).getText().toString()).get();
                friendUID.addOnSuccessListener(documentSnapshot -> {
                    Task<QuerySnapshot> friend = database.document("amizades/amigos").collection(user.getUid()).whereEqualTo(FieldPath.documentId(), documentSnapshot.get("UID")).get();
                    friend.addOnSuccessListener(queryDocumentSnapshots -> {
                        if(!queryDocumentSnapshots.isEmpty()){
                            database.document("partida/convites").collection(Objects.requireNonNull(documentSnapshot.get("UID")).toString()).document(user.getUid()).set(new HashMap<String, Object>());
                        }
                        else{
                            Toast.makeText(InviteFriendToPlayActivity.this, "Você não é amigo desse usuário.", Toast.LENGTH_SHORT).show();
                        }
                    });
                });

            }
        });
    }
}