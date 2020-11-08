package com.example.histoquiz.dialogs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
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
import java.text.Collator;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

/**
 * Classe utilizada para exibir ao usuário as categorias de perguntas disponíveis no banco de dados,
 * bem como todas as perguntas relacionadas a cada uma delas, para que ele selecione uma e envie
 * para seu oponente responder
 */
public class SelectQuestionDialog extends AppCompatDialogFragment{

    public Button send;
    protected View view;
    protected LayoutInflater inflater;
    protected GameActivity parentActivity;
    protected Spinner categories, questions;
    protected String[] categoryNames;
    protected int question, category;

    // Variáveis para o controle da tela como fullscreen
    private final Handler mHideHandler = new Handler();
    private View mContentView;


    /**
     * Método construtor da classe, recebe como parâmetro a activity que instanciou esse dialog
     * @param parentActivity - activity do tipo GameActivity, que é responsável por gerenciar
     *                         partidas e que criou esse dialog
     */
    public SelectQuestionDialog(GameActivity parentActivity){
        this.parentActivity = parentActivity;
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
        builder.setView(view).setTitle("");
        Dialog dialog = builder.create();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        populateCategoriesSpinner();
        populateQuestionSpinner(0);
        handleSpinnersClicks();
        handleQuestionSelectionButton();
        dialog.setCanceledOnTouchOutside(false);
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
    public void hideNow() {
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
        view = inflater.inflate(R.layout.dialog_choose_question, null);
        mContentView = view.findViewById(R.id.fullContent);
        categories = view.findViewById(R.id.spinner_categorias);
        questions = view.findViewById(R.id.spinner_perguntas);
        send = view.findViewById(R.id.enviar);
    }


    /**
     * Método utilizado para popular o spinner com as categorias disponíveis no banco de dados de
     * forma alfabética, para que o usuário selecione uma e, em seguida, visualize as perguntas re-
     * lacionadas a ela
     */
    protected void populateCategoriesSpinner(){
        categoryNames =  parentActivity.perguntas.keySet().toArray(new String[0]);
        Arrays.sort(categoryNames, (o1, o2) -> {
            Collator usCollator = Collator.getInstance(new Locale("pt", "BR"));
            return usCollator.compare(o1, o2);
        });
        ArrayAdapter<String> adapter = new ArrayAdapter<>(parentActivity, R.layout.spinner_layout, categoryNames);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_layout);
        categories.setAdapter(adapter);
        parentActivity.setCategory(0);
    }



    /**
     * Método utilizado para popular o spinner com as perguntas disponíveis no banco de dados, de
     * forma alfabética, relativas a categoria que ele selecionou anteriormente
     */
    protected void populateQuestionSpinner(int category){
        String[] questionTexts =  Objects.requireNonNull(parentActivity.perguntas.get(categoryNames[category])).keySet().toArray(new String[0]);
        Arrays.sort(questionTexts, (o1, o2) -> {
            Collator usCollator = Collator.getInstance(new Locale("pt", "BR"));
            return usCollator.compare(o1, o2);
        });
        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(parentActivity, R.layout.spinner_layout, questionTexts);
        adapter2.setDropDownViewResource(R.layout.spinner_dropdown_item_layout);
        questions.setAdapter(adapter2);
        parentActivity.setQuestion(0);
    }


    /**
     * Método utilizado para lidar com uma seleção realizada nos spinners. Armazena a escolha feita
     * relativa a categoria e a pergunta, para posteriormente repassar para as classes que irão lidar
     * com essa informação
     */
    protected void handleSpinnersClicks(){
        categories.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                category = position;
                populateQuestionSpinner(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        questions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                question = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    /**
     * Método utilizado para lidar com cliques no botão "enviar", que é responsável por repassar a
     * categoria e pergunta selecionada pelo jogador para que seu oponente responda
     */
    protected void handleQuestionSelectionButton(){
        send.setOnClickListener(v -> {
            if(parentActivity.PCopponent){
                parentActivity.setCategory(category);
                parentActivity.setQuestion(question);
            }
            else{
                parentActivity.onlineOpponent.category = category;
                parentActivity.onlineOpponent.question = question;
            }
            parentActivity.handleQuestionSelectionButton();
        });
    }
}