package com.example.histoquiz.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.histoquiz.R;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.Collator;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class ConfigLocalGameActivity extends AppCompatActivity {

    protected AutoCompleteTextView systemsDropdown;
    protected TextInputLayout qntd_slides, roundTime;
    public FirebaseFirestore firestoreDatabase;
    protected HashMap<String, Integer> systems;
    protected String[] systemsNames;
    protected String systemName;
    protected LinearLayout content;
    protected int systemCode;
    protected Button createRoom;

    /**
     * Método executado no instante em que essa activity é criada, seta qual view será associada a
     * essa classe
     * @param savedInstanceState - contém o estado anteriormente salvo da atividade (pode ser nulo)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_local_game);
        hideSystemUI();
        initGUI();
        getSystems();
        handleRoomCreation();
    }


    /**
     * Método utilizado para obter a referência para certos objetos da view que serão manipulados
     * por essa classe e para inicializar alguns outros objetos utilizados aqui no back-end
     */
    protected void initGUI(){
        qntd_slides = findViewById(R.id.qntd_laminas);
        roundTime = findViewById(R.id.tempoRodada);
        createRoom = findViewById(R.id.criarSalaButton);
        firestoreDatabase = FirebaseFirestore.getInstance();
        content = findViewById(R.id.fullContent);
        systemsDropdown = findViewById(R.id.sistemas_dropdown);
        //systemsDropdown.setDropDownWidth((int) Math.round(content.getWidth() - (content.getWidth()*0.1)));
        //systemsDropdown.setDropDownHeight((int) Math.round(content.getHeight()*0.4));
        systemsDropdown.setInputType(InputType.TYPE_NULL);
        systemsDropdown.setDropDownBackgroundDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.dropdown_background, getTheme()));
    }


    /**
     * Método utilizado para obter do firebase os sistemas relativos as lâminas cadastradas no banco
     * de dados
     */
    protected void getSystems(){
        firestoreDatabase.collection("sistemas").get().addOnSuccessListener(queryDocumentSnapshots -> {
            systems = new HashMap<>();
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                systems.put(document.getId(), Integer.parseInt(Objects.requireNonNull(document.get("code")).toString()));
            }
            systems.put("Aleatório", -1);
            populateSystemSpinner();
        });
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
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }


    /**
     * Método utilizado para popular o spinner com as categorias disponíveis no banco de dados de
     * forma alfabética, para que o usuário selecione uma e, em seguida, visualize as perguntas re-
     * lacionadas a ela
     */
    protected void populateSystemSpinner(){
        systemsNames =  systems.keySet().toArray(new String[0]);
        Arrays.sort(systemsNames, (o1, o2) -> {
            Collator usCollator = Collator.getInstance(new Locale("pt", "BR"));
            return usCollator.compare(o1, o2);
        });
        ArrayAdapter<String> adapter = new ArrayAdapter<>(ConfigLocalGameActivity.this, R.layout.spinner_layout, R.id.textoSpin, systemsNames);
        systemsDropdown.setAdapter(adapter);
        systemName = systemsDropdown.getAdapter().getItem(0).toString();
        systemCode = systems.get(systemName);
        systemsDropdown.setText(systemName, false);
        handleSpinnersClicks();
    }


    /**
     * Método utilizado para lidar com cliques no spinner que exibe as categorias disponíveis para o jogo
     */
    protected void handleSpinnersClicks(){
        systemsDropdown.setOnItemClickListener((parent, view, position, id) -> {
            systemName = systemsDropdown.getAdapter().getItem(position).toString();
            systemCode = systems.get(systemName);
            systemsDropdown.setText(systemName, false);

        });
    }


    /**
     * Método utilizado para criar uma sala de jogo utilizando como parâmetros para as configurações
     * dela as informações fornecidas por esse usuário
     */
    protected void handleRoomCreation(){
        createRoom.setOnClickListener(v -> {
            Intent troca = new Intent(ConfigLocalGameActivity.this, LocalGameActivity.class);
            troca.putExtra("systemCode", systemCode);
            troca.putExtra("slidesAmount", Integer.parseInt(Objects.requireNonNull(qntd_slides.getEditText()).getText().toString()));
            troca.putExtra("roundTime", Integer.parseInt(Objects.requireNonNull(roundTime.getEditText()).getText().toString()));
            troca.putExtra("matchCreator", true);
            troca.putExtra("roomCode", "");
            startActivityForResult(troca, 999);
        });
    }

}