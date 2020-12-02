package com.example.histoquiz.dialogs;

import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.histoquiz.R;
import com.example.histoquiz.activities.GameActivity;
import com.google.android.material.textfield.TextInputLayout;
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
    protected TextInputLayout categories, questions;
    protected String[] categoryNames;
    protected int question, category;
    protected Dialog dialog;
    protected AutoCompleteTextView categoriesDropdown, questionsDropdown;


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
        dialog = new Dialog(getActivity());
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        initGUI();
        dialog.setContentView(view);
        dialog.setTitle("");
        dialog.getWindow().setLayout((int) Math.round(parentActivity.content.getWidth() - (parentActivity.content.getWidth()*0.018)), RelativeLayout.LayoutParams.WRAP_CONTENT);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        populateCategoriesSpinner();
        populateQuestionSpinner(0);
        handleSpinnersClicks();
        handleQuestionSelectionButton();
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        dialog.setOnShowListener(dialog2 -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                WindowCompat.setDecorFitsSystemWindows(dialog.getWindow(), false);
                WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(dialog.getWindow(), dialog.getWindow().getDecorView());
                if(controller != null) {
                    controller.hide(WindowInsetsCompat.Type.statusBars());
                    controller.hide(WindowInsetsCompat.Type.navigationBars());
                    controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                }
            }
            else dialog.getWindow().getDecorView().setSystemUiVisibility(Objects.requireNonNull(getActivity()).getWindow().getDecorView().getSystemUiVisibility());
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
            WindowManager wm = parentActivity.getWindowManager();
            wm.updateViewLayout(dialog.getWindow().getDecorView(), Objects.requireNonNull(getDialog()).getWindow().getAttributes());
        });
        return dialog;
    }


    /**
     * Método utilizado para obter a referência para certos objetos da view que serão manipulados
     * por essa classe e para inicializar alguns outros objetos utilizados aqui no back-end
     */
    protected void initGUI(){
        inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
        view = inflater.inflate(R.layout.dialog_choose_question, null);
        categories = view.findViewById(R.id.spinner_categorias);
        questions = view.findViewById(R.id.spinner_perguntas);
        send = view.findViewById(R.id.enviar);
        categoriesDropdown = view.findViewById(R.id.categorias_dropdown);
        questionsDropdown = view.findViewById(R.id.perguntas_dropdown);
        int value = (int) Math.round(parentActivity.content.getWidth() - (parentActivity.content.getWidth()*0.1));
        categoriesDropdown.setDropDownWidth((int) Math.round(parentActivity.content.getWidth() - (parentActivity.content.getWidth()*0.1)));
        categoriesDropdown.setDropDownHeight((int) Math.round(parentActivity.content.getHeight()*0.4));
        categoriesDropdown.setInputType(InputType.TYPE_NULL);
        categoriesDropdown.setDropDownBackgroundDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.dropdown_background, parentActivity.getTheme()));
        questionsDropdown.setDropDownWidth((int) Math.round(parentActivity.content.getWidth() - (parentActivity.content.getWidth()*0.1)));
        questionsDropdown.setDropDownHeight((int) Math.round(parentActivity.content.getHeight()*0.4));
        questionsDropdown.setDropDownBackgroundDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.dropdown_background, parentActivity.getTheme()));
        questionsDropdown.setInputType(InputType.TYPE_NULL);
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
        ArrayAdapter<String> adapter = new ArrayAdapter<>(parentActivity, R.layout.spinner_layout, R.id.textoSpin, categoryNames);
        categoriesDropdown.setAdapter(adapter);
        categoriesDropdown.setText(categoriesDropdown.getAdapter().getItem(0).toString(), false);
        category = 0;
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
        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(parentActivity, R.layout.spinner_layout, R.id.textoSpin, questionTexts);
        questionsDropdown.setAdapter(adapter2);
        questionsDropdown.setText(questionsDropdown.getAdapter().getItem(0).toString(), false);
        question = 0;
        parentActivity.setQuestion(0);
    }


    /**
     * Método utilizado para lidar com uma seleção realizada nos spinners. Armazena a escolha feita
     * relativa a categoria e a pergunta, para posteriormente repassar para as classes que irão lidar
     * com essa informação
     */
    protected void handleSpinnersClicks(){
        categoriesDropdown.setOnItemClickListener((parent, view, position, id) -> {
            category = position;
            categoriesDropdown.setText(categoriesDropdown.getAdapter().getItem(position).toString(), false);
            populateQuestionSpinner(position);
        });
        questionsDropdown.setOnItemClickListener((parent, view, position, id) -> {
            question = position;
            questionsDropdown.setText(questionsDropdown.getAdapter().getItem(position).toString(), false);
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