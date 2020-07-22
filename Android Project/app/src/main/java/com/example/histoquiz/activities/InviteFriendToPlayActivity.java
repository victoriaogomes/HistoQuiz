package com.example.histoquiz.activities;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.histoquiz.R;
import com.example.histoquiz.util.FormFieldValidator;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
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
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN);
        setContentView(R.layout.activity_invite_friend_to_play);
        sendInvitation = findViewById(R.id.enviarConviteButton);
        friendEmail = findViewById(R.id.email);
        fieldValitator = new FormFieldValidator(this);
        fieldValitator.monitorarCampo(friendEmail);
        database = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        friendEmail.getEditText().setText("victoria.fo.f@hotmail.com");
        sendInviteToPlay();
    }


    /**
     * Método utilizado para armazenar no firebase um convite para uma nova partida enviado pelo
     * usuário que está logado no momeno
     */
    protected void sendInviteToPlay(){
        sendInvitation.setOnClickListener(v -> {
            if(fieldValitator.preenchido(friendEmail)){
                Task<DocumentSnapshot> friendUID = database.collection("tabelaAuxiliar").document(Objects.requireNonNull(friendEmail.getEditText()).getText().toString()).get();
                friendUID.addOnSuccessListener(documentSnapshot -> {
                    Task<QuerySnapshot> friend = database.document("amizades/amigos").collection(user.getUid()).whereEqualTo(FieldPath.documentId(), documentSnapshot.get("UID")).get();
                    friend.addOnSuccessListener(queryDocumentSnapshots -> {
                        if(!queryDocumentSnapshots.isEmpty()){
                            HashMap <String, Object> map = new HashMap<>();
                            map.put("inviteAccepted", "não respondido");
                            database.document("partida/convites").collection(Objects.requireNonNull(documentSnapshot.get("UID")).toString()).
                                    document(user.getUid()).set(map).
                                    addOnSuccessListener(aVoid -> addInviteResponseEventListener(Objects.requireNonNull(documentSnapshot.get("UID")).toString()));
                        }
                        else{
                            Toast.makeText(InviteFriendToPlayActivity.this, "Você não é amigo desse usuário.", Toast.LENGTH_SHORT).show();
                        }
                    });
                });

            }
        });
    }


    /**
     * Método utilizado para verificar quando o usuário convidado para jogar responder ao usuário
     * que o convidou. Caso o usuário aceite, ele será redirecionado para a tela de jogo. Caso re-
     * cuse, uma mensagem será exibida para ele, informando o que ocorreu
     * @param friendUID - UID do amigo convidado para jogar
     */
    protected void addInviteResponseEventListener(String friendUID){
        DocumentReference ref = database.document("partida/convites/" + friendUID + "/" + user.getUid());
        ref.addSnapshotListener((documentSnapshot, e) -> {
            assert documentSnapshot != null;
            if(Objects.equals(documentSnapshot.get("inviteAccepted"), "aceito")){
                Intent troca = new Intent(InviteFriendToPlayActivity.this, GameActivity.class);
                troca.putExtra("matchCreator", true);
                troca.putExtra("opponentUID", friendUID);
                startActivityForResult(troca, 999);
            }
            else if(Objects.equals(documentSnapshot.get("inviteAccepted"), "recusado")){
                Toast.makeText(InviteFriendToPlayActivity.this, "Seu amigo recusou o convite para jogar.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}