package com.example.histoquiz.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.histoquiz.R;
import com.example.histoquiz.activities.GameActivity;

import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;

public class SelectQuestionDialog extends AppCompatDialogFragment{

    public Button send;
    protected View view;
    protected LayoutInflater inflater;
    protected GameActivity parentActivity;
    protected Spinner categories, questions;
    protected Object[] aux;
    protected String[] categoryNames;
    protected int question, category;


    public SelectQuestionDialog(GameActivity parentActivity){
        this.parentActivity = parentActivity;
    }


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
        View decorView = dialog.getWindow().getDecorView();
        dialog.setCanceledOnTouchOutside(false);
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN);
        return dialog;
    }


    protected void initGUI(){
        inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
        view = inflater.inflate(R.layout.dialog_choose_question, null);
        categories = view.findViewById(R.id.spinner_categorias);
        questions = view.findViewById(R.id.spinner_perguntas);
        send = view.findViewById(R.id.enviar);
    }


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