package com.example.histoquiz.activities;

import androidx.appcompat.app.AppCompatActivity;
import com.example.histoquiz.R;
import com.example.histoquiz.dialogs.SetTeamsDialog;
import com.example.histoquiz.model.Slide;
import com.example.histoquiz.util.GlideApp;
import com.example.histoquiz.util.RoomCreator;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class LocalGameActivity extends AppCompatActivity implements View.OnClickListener {

    protected int systemCode, slideAmount, roundTime;
    public FirebaseFirestore firestoreDatabase;
    protected CountDownTimer countDownTimer;
    protected ImageSwitcher imageSwitcher;
    protected StorageReference storageReference;
    protected int imagesAmount;
    protected ImageView myImageView;
    protected ImageButton next, previous;
    protected int position;
    protected volatile boolean questionsDone = false, slidesDone = false;
    protected String roomCreationStatus = "not created";
    public HashMap<String, Map<String, Object>> perguntas;
    public HashMap<Integer, Slide> slides = new HashMap<>();
    public Map<Integer, Slide> matchSlides = new HashMap<>();
    protected int actualSlide;
    protected Button showSlide;
    protected RoomCreator creator;
    protected DocumentReference docIdRef;
    protected TextView codSala;
    public boolean matchCreator;
    public LinearLayout content;
    public ArrayList<String> nomeJogadores;
    public ArrayList<String> uidJogadores;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        systemCode = intent.getIntExtra("systemCode", -1);
        slideAmount = intent.getIntExtra("slidesAmount", 3);
        roundTime = intent.getIntExtra("roundTime", 120);
        matchCreator = intent.getBooleanExtra("matchCreator", false);
        setContentView(R.layout.activity_local_game);
        initGUI();
        getSlides();
        getQuestions();
        createRoomName();
        countDownTimer = new CountDownTimer(1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }
            @Override
            public void onFinish() {
                if(roomCreationStatus.equals("create another")){
                    roomCreationStatus = "not created";
                    countDownTimer.start();
                }
                else if(roomCreationStatus.equals("room created")){
                    if(!(questionsDone && slidesDone)){
                        countDownTimer.start();
                    }
                    else{
                        Map<String, Object> dados = new HashMap<>();
                        dados.put("creatorUID", FirebaseAuth.getInstance().getUid());
                        dados.put("qntd", "1");
                        dados.put("nomeJogadores", new ArrayList<>(Arrays.asList(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getDisplayName(), "", "", "")));
                        dados.put("uidJogadores", new ArrayList<>(Arrays.asList(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid(), "", "", "")));
                        firestoreDatabase.collection("partidaLocal").document(creator.getActualRoomName()).set(dados);
                        codSala.setText(String.format("Cód. da sala: %s", creator.getActualRoomName()));
                        imageToShow(0);
                        addRoomFilled();
                    }
                }
            }
        }.start();
    }


    /**
     * Método utilizado para obter uma referência para os elementos da view que está sendo exibida,
     * que serão utilizados para mudar algumas de suas configurações. Além disso, inicializa algumas
     * variáveis que serão utilizadas.
     */
    protected void initGUI(){
        position = 0;
        actualSlide = 0;
        creator = new RoomCreator();
        showSlide = findViewById(R.id.toggleLamina);
        content = findViewById(R.id.fullContent);
        showSlide.setOnClickListener(this);
        showSlide.setTag("OCULTAR_LAMINA");
        firestoreDatabase = FirebaseFirestore.getInstance();
        next = findViewById(R.id.proximoButton);
        next.setOnClickListener(this);
        next.setTag("NEXT");
        previous = findViewById(R.id.anteriorButton);
        previous.setOnClickListener(this);
        previous.setTag("PREVIOUS");
        codSala = findViewById(R.id.codSala);
        imageSwitcher = findViewById(R.id.imageSW);
        imageSwitcher.setFactory(() -> {
            myImageView = new ImageView(getApplicationContext());
            myImageView.setLayoutParams(new ImageSwitcher.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT));
            myImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            return myImageView;
        });
        imageSwitcher.setOutAnimation(AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right));
        imageSwitcher.setInAnimation(AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left));
    }


    /**
     * Método utilizado para obter do firebase as perguntas que estão cadastradas no banco de dados
     */
    protected void getQuestions(){
        firestoreDatabase.collection("perguntas").get().addOnSuccessListener(queryDocumentSnapshots -> {
            perguntas = new HashMap<>();
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                perguntas.put(document.getId(), document.getData());
            }
            questionsDone = true;
        });
    }


    /**
     * Método utilizado para obter do firebase as lâminas disponíveis para serem utilizadas durante
     * essa partida
     */
    protected void getSlides(){
        OnSuccessListener<QuerySnapshot> listener = queryDocumentSnapshots -> {
            for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                Slide slide = documentSnapshot.toObject(Slide.class);
                slide.setName(documentSnapshot.getId());
                slides.put(slide.getCode(), slide);
            }
            raffleSlides(queryDocumentSnapshots.size(), slides, slideAmount);
            slidesDone = true;
        };
        if(systemCode == -1){
            firestoreDatabase.collection("laminas").get().addOnSuccessListener(listener);
        }
        else{
            CollectionReference slidesRef = firestoreDatabase.collection("laminas");
            slidesRef.whereEqualTo("system", systemCode).get().addOnSuccessListener(listener);
        }
    }


    /**
     * Método utilizado para lidar com os cliques no dialog que exibe as imagens, que é o botão de
     * "voltar", para fechar a exibição da imagem da lâmina, e o botão "próximo", que aparece somen-
     * te quando a lâmina possui mais de uma foto cadastrada no banco de dados
     * @param v - view que recebeu o clique do usuário
     */
    @Override
    public void onClick(View v) {
        switch (v.getTag().toString()){
            case "NEXT":
                if(position != slideAmount-1){
                    position++;
                    imageToShow(position);
                }
                break;
            case "PREVIOUS":
                if(position != 0){
                    position--;
                    imageToShow(position);
                }
                break;
            case "OCULTAR_LAMINA":
                showSlide.setText(getText(R.string.showLamina));
                imageSwitcher.setVisibility(View.GONE);
                next.setVisibility(View.GONE);
                previous.setVisibility(View.GONE);
                showSlide.setTag("MOSTRAR_LAMINA");
                break;
            case "MOSTRAR_LAMINA":
                imageToShow(0);
                showSlide.setText(R.string.hideLamina);
                imageSwitcher.setVisibility(View.VISIBLE);
                showSlide.setTag("OCULTAR_LAMINA");
                break;
        }
    }


    /**
     * Método utilizado para sortear as lâminas que este jogador deverá adivinhar e armazená-las
     * no firebase. Cada jogador é responsável por sortear suas lâminas e cadastrá-las na nuvem,
     * para que o seu oponente as obtenha
     */
    protected void raffleSlides(int slidesAmount, HashMap<Integer, Slide> slides, int raffleNumber) {
        Random rndGenerator = new Random();
        int raffledValue, limit;
        List<Slide> slidesValues = new ArrayList<>(slides.values());
        //Toast.makeText(this,"Laminas sorteadas: ", Toast.LENGTH_SHORT).show();
        limit = Math.min(raffleNumber, slidesAmount);
        while(limit %2 != 0){
            limit--;
        }
        for (int i = 0; i < limit; i++) {
            raffledValue = rndGenerator.nextInt(slidesAmount);
            while (matchSlides.containsKey(slidesValues.get(raffledValue).getCode())) {
                raffledValue = rndGenerator.nextInt(slidesAmount);
            }
            matchSlides.put(slidesValues.get(raffledValue).getCode(), slidesValues.get(raffledValue));
            //Toast.makeText(this,slidesValues.get(raffledValue).getName(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Método que realiza efetivamente a exibição da imagem presente na posição recebida por parâ-
     * metro (primeira imagem de uma lâmina, segunda imagem, etc...)
     * @param position - posição da foto a ser exibida (1ª, 2ª, 3ª, ...)
     */
    public void imageToShow(int position){
        Object [] keySet;
        Integer aux = 0;
        keySet = matchSlides.keySet().toArray();
        aux = (Integer) keySet[actualSlide];
        storageReference = FirebaseStorage.getInstance().getReference(Objects.requireNonNull(matchSlides.get(aux)).getImages().get(position));
        imagesAmount = Objects.requireNonNull(matchSlides.get(aux)).getImages().size();
        if(imagesAmount == 1 || (position+1) == imagesAmount){
            next.setVisibility(View.INVISIBLE);
        }
        else if(position == 0){
            previous.setVisibility(View.INVISIBLE);
        }
        if(imagesAmount == 1){
            next.setVisibility(View.INVISIBLE);
            previous.setVisibility(View.INVISIBLE);
        }
        else{
            next.setVisibility(View.VISIBLE);
            previous.setVisibility(View.VISIBLE);
        }
        GlideApp.with(this).load(storageReference).into((ImageView) imageSwitcher.getCurrentView());
    }

    protected void setTeams(){

    }

    @SuppressWarnings("unchecked")
    protected void addRoomFilled(){
        DocumentReference ref = firestoreDatabase.collection("partidaLocal").document(creator.getActualRoomName());
        ref.addSnapshotListener((documentSnapshot, e) -> {
            assert documentSnapshot != null;
            if(Objects.equals(documentSnapshot.get("qntd"), "4")){
                nomeJogadores = (ArrayList<String>) documentSnapshot.get("nomeJogadores");
                uidJogadores = (ArrayList<String>) documentSnapshot.get("uidJogadores");
                SetTeamsDialog setTeamsDialog = new SetTeamsDialog(this);
                setTeamsDialog.show(getSupportFragmentManager(), "choose teams dialog");
            }
            else{
                Toast.makeText(LocalGameActivity.this, String.format("Faltam %s jogadores para iniciarmos a partida.", 4 - Integer.parseInt(Objects.requireNonNull(documentSnapshot.get("qntd")).toString())), Toast.LENGTH_SHORT).show();
            }
        });
    }

    protected void createRoomName(){
        docIdRef = firestoreDatabase.collection("partidaLocal").document(creator.newRoomCode(6));
        docIdRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                assert document != null;
                if (document.exists()) {
                   roomCreationStatus = "create another";
                } else {
                    roomCreationStatus = "room created";
                }
            } else {
                Log.d("Erro criando a sala", "Failed with: ", task.getException());
            }
        });
    }
}