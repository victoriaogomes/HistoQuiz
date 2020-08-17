package com.example.histoquiz.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.histoquiz.R;
import com.example.histoquiz.activities.GameActivity;
import com.example.histoquiz.model.Slide;

import java.util.Map;
import java.util.Objects;

public class GuessSlideDialog extends AppCompatDialogFragment {

    public Spinner guess;
    public Button goBack, send;
    protected View view;
    protected LayoutInflater inflater;
    protected GameActivity parent;
    protected String[] slides;

    public GuessSlideDialog(GameActivity parent){
        this.parent = parent;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        initGUI();
        dealWithButtons();
        builder.setView(view).setTitle("");
        Dialog dialog = builder.create();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        View decorView = dialog.getWindow().getDecorView();
        dialog.setCanceledOnTouchOutside(false);
        populateSpinner();
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
        view = inflater.inflate(R.layout.dialog_guess_slide, null);
        guess = view.findViewById(R.id.spinner_laminas);
        goBack = view.findViewById(R.id.voltar);
        send = view.findViewById(R.id.enviar);
    }

    protected void populateSpinner(){
        int i=0;
        slides =  new String[parent.slides.size()];
        for (Map.Entry<Integer, Slide> pair : parent.slides.entrySet()) {
            slides[i] = pair.getValue().getName();
            i++;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(parent, R.layout.spinner_layout, slides);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_layout);
        guess.setAdapter(adapter);
    }

    protected void dealWithButtons(){
        send.setOnClickListener(v -> {
            parent.myOpponent._estado_K(guess.getSelectedItem().toString());
        });
    }
}
