package com.example.histoquiz.dialogs;

import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.cardview.widget.CardView;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

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
    protected Dialog dialog;


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
        dialog = new Dialog(getActivity());
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        initGUI();
        setGameResultInfo();
        dialog.setContentView(view);
        dialog.setTitle("");
        dialog.getWindow().setLayout((int) Math.round(parent.content.getWidth() - (parent.content.getWidth()*0.018)), RelativeLayout.LayoutParams.WRAP_CONTENT);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
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
            WindowManager wm = parent.getWindowManager();
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
        view = inflater.inflate(R.layout.dialog_end_game, null);
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
        else if(parent.getPlayerScore(1) == parent.getPlayerScore(2)){
            winner.setText(getString(R.string.empate));
            playerCard.setCardBackgroundColor(getResources().getColor(R.color.green));
            opponentCard.setCardBackgroundColor(getResources().getColor(R.color.green));
        }
        else{
            winner.setText(getString(R.string.seuOponente));
            playerCard.setCardBackgroundColor(getResources().getColor(R.color.red));
            opponentCard.setCardBackgroundColor(getResources().getColor(R.color.green));
        }
    }
}
