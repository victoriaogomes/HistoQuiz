package com.example.histoquiz.dialogs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDialogFragment;
import com.example.histoquiz.R;
import com.example.histoquiz.activities.GameActivity;
import com.example.histoquiz.model.Slide;
import java.text.Collator;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Classe utilizada para exibir o dialog que lista todas as possíveis lâminas disponíveis no siste-
 * ma, para que o usuário selecione qual ele imagina estar tentando adivinhar no momento
 */
public class GuessSlideDialog extends AppCompatDialogFragment {

    public Spinner guess;
    public Button goBack, send;
    protected View view;
    protected LayoutInflater inflater;
    protected GameActivity parent;
    protected String[] slides;

    // Variáveis para o controle da tela como fullscreen
    private final Handler mHideHandler = new Handler();
    private View mContentView;


    /**
     * Método construtor da classe, recebe como parâmetro a activity que instanciou esse dialog
     * @param parent - activity do tipo GameActivity, que é responsável por gerenciar partidas e
     *                 que criou esse dialog
     */
    public GuessSlideDialog(GameActivity parent){
        this.parent = parent;
    }


    /**
     * Método chamado no instante que o dialog é criado, seta qual view será associada a essa classe
     * @param savedInstanceState - contém o estado anteriormente salvo da atividade (pode ser nulo)
     * @return - o dialog criado
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        initGUI();
        dealWithButtons();
        builder.setView(view).setTitle("");
        Dialog dialog = builder.create();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCanceledOnTouchOutside(false);
        populateSpinner();
        hideNow();
        return dialog;
    }


    /**
     * Runnable utilizado para remover automaticamente a barra de botões e a de status dessa
     * activity
     */
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };


    /**
     * Runnable utilizado para exibir a barra de botões e a de status dessa activity quando o
     * usuário solicitar
     */
    private final Runnable mShowPart2Runnable = () -> {
        ActionBar actionBar = ((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
        }
    };


    /**
     * Programa uma chamada ao método hide() após uma quantidade delayMillis de millisegundos,
     * cancelando qualquer chamada programada previamente
     */
    private void hideNow() {
        ActionBar actionBar = ((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, 0);
    }


    /**
     * Método utilizado para obter a referência para certos objetos da view que serão manipulados
     * por essa classe e para inicializar alguns outros objetos utilizados aqui no back-end
     */
    protected void initGUI(){
        inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
        view = inflater.inflate(R.layout.dialog_guess_slide, null);
        mContentView = view.findViewById(R.id.fullContent);
        guess = view.findViewById(R.id.spinner_laminas);
        goBack = view.findViewById(R.id.voltar);
        send = view.findViewById(R.id.enviar);
    }


    /**
     * Método utilizado para popular o spinner com as possíveis lâminas disponíveis no banco de da-
     * dos de forma alfabética, para que o usuário selecione uma
     */
    protected void populateSpinner(){
        int i=0;
        slides =  new String[parent.slides.size()];
        for (Map.Entry<Integer, Slide> pair : parent.slides.entrySet()) {
            slides[i] = pair.getValue().getName();
            i++;
        }
        Arrays.sort(slides, (o1, o2) -> {
            Collator usCollator = Collator.getInstance(new Locale("pt", "BR"));
            return usCollator.compare(o1, o2);
        });
        ArrayAdapter<String> adapter = new ArrayAdapter<>(parent, R.layout.spinner_layout, slides);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_layout);
        guess.setAdapter(adapter);
    }


    /**
     * Método utilizado para lidar com os cliques nos botões desse dialog ("enviar" ou "voltar")
     */
    protected void dealWithButtons(){
        send.setOnClickListener(v -> {
            if(parent.PCopponent) parent.computerOpponent._estado_K(guess.getSelectedItem().toString());
            else parent.onlineOpponent._estado_J(guess.getSelectedItem().toString());
        });
        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Fazer alguma coisa quando clicar em voltar
            }
        });
    }
}
