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
    protected Dialog dialog;


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
        dialog = new Dialog(getActivity());
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        initGUI();
        dialog.setContentView(view);
        dialog.setTitle("");
        dialog.getWindow().setLayout((int) Math.round(parentActivity.content.getWidth() - (parentActivity.content.getWidth()*0.018)), RelativeLayout.LayoutParams.WRAP_CONTENT);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        handleButtonsClicks();
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
        view = inflater.inflate(R.layout.dialog_question_feedback, null);
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
            correctAnswer.setText(String.format(Locale.getDefault(), "%s %s", getString(R.string.feedbackPergunta), getString(R.string.simCaps)));
        }
        else{
            correctAnswer.setText(String.format(Locale.getDefault(), "%s %s", getString(R.string.feedbackPergunta), getString(R.string.naoCaps
            )));
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