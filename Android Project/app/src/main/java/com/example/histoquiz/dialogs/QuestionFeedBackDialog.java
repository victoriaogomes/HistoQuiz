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
import java.util.Objects;

/**
 * Método utilizado para exibir ao usuário um feedback referente a pergunta que ele selecionou para
 * que seu oponente respondesse. Exibe a resposta correta a sua pergunta, a resposta do oponente e
 * solicita que ele informe se deseja prosseguir para a próxima rodada ou se quer tentar adivinhar
 * sua lâmina
 */
public class QuestionFeedBackDialog extends AppCompatDialogFragment {

    protected CardView cardOpponentAnswer;
    protected TextView correctAnswer, opponentAnswer;
    protected Button guessSlide, nextRound;
    protected LayoutInflater inflater;
    protected View view;
    protected GameActivity parentActivity;
    protected Boolean auxOpponentAnswer, auxCorrectAnswer;

    // Variáveis para o controle da tela como fullscreen
    private final Handler mHideHandler = new Handler();
    private View mContentView;


    /**
     * Método construtor da classe, recebe por parâmetro a activity que instanciou-a, a resposta
     * fornecida pelo oponente e a resposta correta da pergunta
     * @param parentActivity - activity do tipo GameActivity, que é responsável por gerenciar parti-
     *                         das e que criou esse dialog
     * @param auxOpponentAnswer - resposta do oponente, que pode ser nula, caso ele tenha ultrapas-
     *                          sado o prazo de 2 min para responder
     * @param auxCorrectAnswer - resposta correta da pergunta feita
     */
    public QuestionFeedBackDialog(GameActivity parentActivity, Boolean auxOpponentAnswer, boolean auxCorrectAnswer){
        this.parentActivity = parentActivity;
        this.auxOpponentAnswer = auxOpponentAnswer;
        this.auxCorrectAnswer = auxCorrectAnswer;
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
        handleButtonsClicks();
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
        view = inflater.inflate(R.layout.dialog_question_feedback, null);
        mContentView = view.findViewById(R.id.fullContent);
        cardOpponentAnswer = view.findViewById(R.id.cardResp);
        correctAnswer = view.findViewById(R.id.feedbackPergunta);
        opponentAnswer = view.findViewById(R.id.respOponente);
        if(auxOpponentAnswer != null) {
            if (auxOpponentAnswer) {
                opponentAnswer.setText(getString(R.string.sim));
            } else {
                opponentAnswer.setText(getString(R.string.nao));
            }
            if(auxCorrectAnswer == auxOpponentAnswer) cardOpponentAnswer.setCardBackgroundColor(getResources().getColor(R.color.green));
            else cardOpponentAnswer.setCardBackgroundColor(getResources().getColor(R.color.red));
        }
        else {
            opponentAnswer.setText(getString(R.string.naoResp));
            cardOpponentAnswer.setCardBackgroundColor(getResources().getColor(R.color.yellow));
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



    /**
     * Método utilizado para lidar com os cliques nos botões desse dialog ("próxima rodada" ou
     * "adivinhar lâmina")
     */
    protected void handleButtonsClicks(){
        nextRound.setOnClickListener(v -> {
            parentActivity.closeQuestionFeedback();
            if(parentActivity.PCopponent) parentActivity.computerOpponent._estado_A();
            else{
                parentActivity.onlineOpponent.myRoomRef.child("nextRound").setValue("sim");
                parentActivity.onlineOpponent._estado_A();
            }
        });
        guessSlide.setOnClickListener(v -> {
            parentActivity.closeQuestionFeedback();
            if(parentActivity.PCopponent) parentActivity.computerOpponent._estado_J();
            else parentActivity.onlineOpponent._estado_I();
        });
    }
}