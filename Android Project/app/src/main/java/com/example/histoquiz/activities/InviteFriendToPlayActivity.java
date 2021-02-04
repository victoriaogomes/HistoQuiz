package com.example.histoquiz.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import android.content.Intent;
import android.os.Build;
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


/**
 * Classe utilizada para manipular os procedimentos necessários para enviar um convite de jogo
 * para um amigo
 */
public class InviteFriendToPlayActivity extends AppCompatActivity {

    protected Button sendInvitation;
    protected FormFieldValidator fieldValitator;
    protected TextInputLayout friendEmail;
    protected FirebaseFirestore database;
    protected FirebaseUser user;


    /**
     * Método executado no instante em que essa activity é criada, seta qual view será associada a
     * essa classe
     * @param savedInstanceState - contém o estado anteriormente salvo da atividade (pode ser nulo)
     */
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
        //friendEmail.getEditText().setText("victoria.fo.f@hotmail.com");
        sendInviteToPlay();
        hideSystemUI();
    }


    /**
     * Método chamado quando a janela atual da activity ganha ou perde o foco, é utilizado para es-
     * conder novamente a barra de status e a navigation bar.
     * @param hasFocus - booleano que indica se a janela desta atividade tem foco.
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        hideSystemUI();
    }


    /**
     * Método utilizado para fazer com que a barra de status e a navigation bar não sejam exibidas
     * na activity. Caso o usuário queira visualizá-las, ele deve realizar um movimento de arrastar
     * para cima (na navigation bar), ou para baixo (na status bar), o que fará com que elas apare-
     * çam por um momento e depois sumam novamente.
     */
    private void hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
            WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
            if(controller != null) {
                controller.hide(WindowInsetsCompat.Type.statusBars());
                controller.hide(WindowInsetsCompat.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        }
        else {
            //noinspection deprecation
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }


    /**
     * Método utilizado para armazenar no firebase um convite para uma nova partida enviado pelo
     * usuário que está logado no momento. Primeiro, é verificado se o usuário é amigo do usuário
     * que possui o email informado e, caso seja, o convite é efetivamente enviado
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
                            Toast.makeText(InviteFriendToPlayActivity.this, "Convite enviado com sucesso!", Toast.LENGTH_LONG).show();
                        }
                        else{
                            Toast.makeText(InviteFriendToPlayActivity.this, "Você não é amigo desse usuário.", Toast.LENGTH_LONG).show();
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
                FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                firestore.document("partida/convites/" + friendUID + "/" + Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).delete();
                Intent troca = new Intent(InviteFriendToPlayActivity.this, GameActivity.class);
                troca.putExtra("matchCreator", true);
                troca.putExtra("opponentUID", friendUID);
                troca.putExtra("PCopponent", false);
                startActivityForResult(troca, 999);
            }
            else if(Objects.equals(documentSnapshot.get("inviteAccepted"), "recusado")){
                Toast.makeText(InviteFriendToPlayActivity.this, "Seu amigo recusou o convite para jogar.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}