package com.example.histoquiz.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.cardview.widget.CardView;

import com.example.histoquiz.R;
import com.example.histoquiz.activities.GameActivity;

import java.util.Objects;

public class QuestionFeedBackDialog extends AppCompatDialogFragment {

    protected CardView cardOpponentAnswer;
    protected TextView correctAnswer, opponentAnswer;
    protected Button guessSlide, nextRound;
    protected LayoutInflater inflater;
    protected View view;
    protected GameActivity parentActivity;
    protected boolean auxOpponentAnswer, auxCorrectAnswer;


    public QuestionFeedBackDialog(GameActivity parentActivity, boolean auxOpponentAnswer, boolean auxCorrectAnswer){
        this.parentActivity = parentActivity;
        this.auxOpponentAnswer = auxOpponentAnswer;
        this.auxCorrectAnswer = auxCorrectAnswer;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        initGUI();
        builder.setView(view).setTitle("");
        Dialog dialog = builder.create();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        handleButtonsClicks();

//        View decorView = parentActivity.getWindow().getDecorView();
//        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_FULLSCREEN);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    protected void initGUI(){
        inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
        view = inflater.inflate(R.layout.dialog_question_feedback, null);
        cardOpponentAnswer = view.findViewById(R.id.cardResp);
        correctAnswer = view.findViewById(R.id.feedbackPergunta);
        opponentAnswer = view.findViewById(R.id.respOponente);
        if(auxOpponentAnswer){
            opponentAnswer.setText(getString(R.string.sim));
        }
        else{
            opponentAnswer.setText(getString(R.string.nao));
        }
        if(auxCorrectAnswer){
            correctAnswer.setText(getString(R.string.simCaps));
        }
        else{
            correctAnswer.setText(getString(R.string.naoCaps));
        }
        guessSlide = view.findViewById(R.id.adivinharLamina);
        nextRound = view.findViewById(R.id.proximaRodada);
    }


    protected void handleButtonsClicks(){
        nextRound.setOnClickListener(v -> {
            parentActivity.closeQuestionFeedback();
            parentActivity.myOpponent._estado_0000();
        });
        guessSlide.setOnClickListener(v -> {
            parentActivity.closeQuestionFeedback();
            parentActivity.myOpponent._estado_1000();
        });
    }
}