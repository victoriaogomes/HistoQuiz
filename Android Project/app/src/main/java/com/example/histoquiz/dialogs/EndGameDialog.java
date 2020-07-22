package com.example.histoquiz.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
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
import com.example.histoquiz.activities.MenuActivity;

import java.util.Locale;
import java.util.Objects;

public class EndGameDialog extends AppCompatDialogFragment {

    protected GameActivity parent;
    protected View view;
    protected LayoutInflater inflater;
    protected TextView playerScore, opponentScore, winner;
    protected CardView playerCard, opponentCard;
    protected Button backToMenu;

    public EndGameDialog(GameActivity parent){
        this.parent = parent;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        initGUI();
        setGameResultInfo();
        builder.setView(view).setTitle("");
        Dialog dialog = builder.create();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
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
        view = inflater.inflate(R.layout.dialog_end_game, null);
        playerScore = view.findViewById(R.id.minhaPontuacao);
        opponentScore = view.findViewById(R.id.pontOponente);
        winner = view.findViewById(R.id.ganhadorPartida);
        playerCard = view.findViewById(R.id.cardMinhaPontuacao);
        opponentCard = view.findViewById(R.id.cardPontuacaoOponente);
        backToMenu = view.findViewById(R.id.menu);
        backToMenu.setOnClickListener(v -> {
            Intent troca = new Intent(parent, MenuActivity.class);
            startActivity(troca);
        });
    }

    protected void setGameResultInfo(){
        playerScore.setText(String.format(Locale.getDefault(), "%d", parent.getPlayerScore(1)));
        opponentScore.setText(String.format(Locale.getDefault(), "%d", parent.getPlayerScore(2)));
        if(parent.getPlayerScore(1) > parent.getPlayerScore(2)){
            winner.setText(getString(R.string.vc));
            playerCard.setCardBackgroundColor(getResources().getColor(R.color.green));
            opponentCard.setCardBackgroundColor(getResources().getColor(R.color.red));
        }
        else{
            winner.setText(getString(R.string.seuOponente));
            playerCard.setCardBackgroundColor(getResources().getColor(R.color.red));
            opponentCard.setCardBackgroundColor(getResources().getColor(R.color.green));
        }
    }
}
