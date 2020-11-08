package com.example.histoquiz.dialogs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.cardview.widget.CardView;
import com.example.histoquiz.R;
import com.example.histoquiz.activities.GameActivity;
import java.util.Locale;
import java.util.Objects;

/**
 * Classe utilizada para exibir um dialog informando os resultados ao fim de uma partida: pontuação
 * do jogador, pontuação do seu oponente e quem foi o ganhador
 */
public class EndGameDialog extends AppCompatDialogFragment {

    protected GameActivity parent;
    protected View view;
    protected LayoutInflater inflater;
    protected TextView playerScore, opponentScore, winner;
    protected CardView playerCard, opponentCard;
    protected Button backToMenu;

    // Variáveis para o controle da tela como fullscreen
    private final Handler mHideHandler = new Handler();
    private View mContentView;

    /**
     * Método construtor da classe, recebe como parâmetro a activity que instanciou esse dialog
     * @param parent - activity do tipo GameActivity, que é responsável por gerenciar partidas e
     *                 que criou esse dialog
     */
    public EndGameDialog(GameActivity parent){
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
        setGameResultInfo();
        builder.setView(view).setTitle("");
        Dialog dialog = builder.create();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
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
        view = inflater.inflate(R.layout.dialog_end_game, null);
        mContentView = view.findViewById(R.id.fullContent);
        playerScore = view.findViewById(R.id.minhaPontuacao);
        opponentScore = view.findViewById(R.id.pontOponente);
        winner = view.findViewById(R.id.ganhadorPartida);
        playerCard = view.findViewById(R.id.cardMinhaPontuacao);
        opponentCard = view.findViewById(R.id.cardPontuacaoOponente);
        backToMenu = view.findViewById(R.id.menu);
        backToMenu.setTag("BACK_MENU");
        backToMenu.setOnClickListener(parent);
    }

    /**
     * Método utilizado para setar nesse dialog os resultados da partida que acabou de ser finaliza-
     * da, mostrando a pontuação de cada um dos jogadores e informando quem foi o ganhador
     */
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
