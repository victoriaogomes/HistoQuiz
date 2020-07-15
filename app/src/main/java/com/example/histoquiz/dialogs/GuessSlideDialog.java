package com.example.histoquiz.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.histoquiz.R;
import com.example.histoquiz.activities.GameActivity;
import com.example.histoquiz.util.FormFieldValidator;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

public class GuessSlideDialog extends AppCompatDialogFragment {

    public TextInputLayout guess;
    public Button goBack, send;
    protected View view;
    protected LayoutInflater inflater;
    public FormFieldValidator formFieldValidator;
    protected GameActivity parent;


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
        formFieldValidator = new FormFieldValidator(getActivity());
        formFieldValidator.monitorarCampo(guess);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        View decorView = parent.getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    protected void initGUI(){
        inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
        view = inflater.inflate(R.layout.dialog_guess_slide, null);
        guess = view.findViewById(R.id.lamina);
        goBack = view.findViewById(R.id.voltar);
        send = view.findViewById(R.id.enviar);
    }

    protected void dealWithButtons(){
        send.setOnClickListener(v -> {
            if(formFieldValidator.preenchido(guess)){
//                if(Objects.requireNonNull(guess.getEditText()).getText().toString().toLowerCase().equals(parent.roomRef.child(parent.slideToGuess).toString().toLowerCase())){
//                    parent.incrementPlayerScore(1);
//                }
//                else{
//                    Toast.makeText(parent, "Esta não é a lâmina que você está tentando acertar.", Toast.LENGTH_SHORT).show();
//                }
                parent.myOpponent._estado_1001(Objects.requireNonNull(guess.getEditText()).getText().toString());
            }
        });
    }
}
