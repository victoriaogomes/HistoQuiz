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
import com.example.histoquiz.activities.LocalGameActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Arrays;
import java.util.Objects;

public class SetTeamsDialog extends AppCompatDialogFragment {
    public Button continuar;
    protected View view;
    protected LayoutInflater inflater;
    protected LocalGameActivity parentActivity;
    protected TextInputLayout player1, player2, player3, player4;
    protected String[] playersNames;
    protected int aux;
    protected Dialog dialog;
    protected AutoCompleteTextView player1Dropdown, player2Dropdown, player3Dropdown, player4Dropdown;


    /**
     * Método construtor da classe, recebe como parâmetro a activity que instanciou esse dialog
     *
     * @param parentActivity - activity do tipo GameActivity, que é responsável por gerenciar
     *                       partidas e que criou esse dialog
     */
    public SetTeamsDialog(LocalGameActivity parentActivity) {
        this.parentActivity = parentActivity;
    }


    /**
     * Método chamado no instante que o dialog é criado, seta qual view será associada a essa classe
     *
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
        dialog.getWindow().setLayout((int) Math.round(parentActivity.content.getWidth() - (parentActivity.content.getWidth() * 0.018)), RelativeLayout.LayoutParams.WRAP_CONTENT);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        populatePlayer1Spinner();
        populatePlayer2Spinner();
        populatePlayer3Spinner();
        populatePlayer4Spinner();
        handleSpinnersClicks();
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        dialog.setOnShowListener(dialog2 -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                WindowCompat.setDecorFitsSystemWindows(dialog.getWindow(), false);
                WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(dialog.getWindow(), dialog.getWindow().getDecorView());
                if (controller != null) {
                    controller.hide(WindowInsetsCompat.Type.statusBars());
                    controller.hide(WindowInsetsCompat.Type.navigationBars());
                    controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                }
            } else
                dialog.getWindow().getDecorView().setSystemUiVisibility(Objects.requireNonNull(getActivity()).getWindow().getDecorView().getSystemUiVisibility());
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
    protected void initGUI() {
        inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
        playersNames = parentActivity.nomeJogadores.toArray(new String[0]);
        view = inflater.inflate(R.layout.dialog_set_teams, null);

        player1 = view.findViewById(R.id.spinner_player1);
        player1Dropdown = view.findViewById(R.id.player1_dropdown);
        player1Dropdown.setDropDownWidth((int) Math.round(parentActivity.content.getWidth() - (parentActivity.content.getWidth() * 0.1)));
        player1Dropdown.setDropDownHeight((int) Math.round(parentActivity.content.getHeight() * 0.4));
        player1Dropdown.setInputType(InputType.TYPE_NULL);
        player1Dropdown.setDropDownBackgroundDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.dropdown_background, parentActivity.getTheme()));

        player2 = view.findViewById(R.id.spinner_player2);
        player2Dropdown = view.findViewById(R.id.player2_dropdown);
        player2Dropdown.setDropDownWidth((int) Math.round(parentActivity.content.getWidth() - (parentActivity.content.getWidth() * 0.1)));
        player2Dropdown.setDropDownHeight((int) Math.round(parentActivity.content.getHeight() * 0.4));
        player2Dropdown.setInputType(InputType.TYPE_NULL);
        player2Dropdown.setDropDownBackgroundDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.dropdown_background, parentActivity.getTheme()));

        player3 = view.findViewById(R.id.spinner_player3);
        player3Dropdown = view.findViewById(R.id.player3_dropdown);
        player3Dropdown.setDropDownWidth((int) Math.round(parentActivity.content.getWidth() - (parentActivity.content.getWidth() * 0.1)));
        player3Dropdown.setDropDownHeight((int) Math.round(parentActivity.content.getHeight() * 0.4));
        player3Dropdown.setInputType(InputType.TYPE_NULL);
        player3Dropdown.setDropDownBackgroundDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.dropdown_background, parentActivity.getTheme()));

        player4 = view.findViewById(R.id.spinner_player4);
        player4Dropdown = view.findViewById(R.id.player4_dropdown);
        player4Dropdown.setDropDownWidth((int) Math.round(parentActivity.content.getWidth() - (parentActivity.content.getWidth() * 0.1)));
        player4Dropdown.setDropDownHeight((int) Math.round(parentActivity.content.getHeight() * 0.4));
        player4Dropdown.setInputType(InputType.TYPE_NULL);
        player4Dropdown.setDropDownBackgroundDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.dropdown_background, parentActivity.getTheme()));

        continuar = view.findViewById(R.id.continuar);
        handleTeamSelectionButton();
    }


    /**
     * Método utilizado para popular o spinner com os jogadores disponíveis no banco de dados, para
     * auxiliar o jogador na separação dos times
     */
    protected void populatePlayer1Spinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(parentActivity, R.layout.spinner_layout, R.id.textoSpin, playersNames);
        player1Dropdown.setAdapter(adapter);
        player1Dropdown.setText(player1Dropdown.getAdapter().getItem(0).toString(), false);
        parentActivity.playersId[0] = 0;
    }


    /**
     * Método utilizado para popular o spinner com os jogadores disponíveis no banco de dados, para
     * auxiliar o jogador na separação dos times
     */
    protected void populatePlayer2Spinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(parentActivity, R.layout.spinner_layout, R.id.textoSpin, playersNames);
        player2Dropdown.setAdapter(adapter);
        player2Dropdown.setText(player2Dropdown.getAdapter().getItem(1).toString(), false);
        parentActivity.playersId[1] = 1;
    }


    /**
     * Método utilizado para popular o spinner com os jogadores disponíveis no banco de dados, para
     * auxiliar o jogador na separação dos times
     */
    protected void populatePlayer3Spinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(parentActivity, R.layout.spinner_layout, R.id.textoSpin, playersNames);
        player3Dropdown.setAdapter(adapter);
        player3Dropdown.setText(player3Dropdown.getAdapter().getItem(2).toString(), false);
        parentActivity.playersId[2] = 2;
    }


    /**
     * Método utilizado para popular o spinner com os jogadores disponíveis no banco de dados, para
     * auxiliar o jogador na separação dos times
     */
    protected void populatePlayer4Spinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(parentActivity, R.layout.spinner_layout, R.id.textoSpin, playersNames);
        player4Dropdown.setAdapter(adapter);
        player4Dropdown.setText(player4Dropdown.getAdapter().getItem(3).toString(), false);
        parentActivity.playersId[3] = 3;
    }


    /**
     * Método utilizado para lidar com uma seleção realizada nos spinners. Armazena a escolha feita
     * relativa a categoria e a pergunta, para posteriormente repassar para as classes que irão lidar
     * com essa informação
     */
    protected void handleSpinnersClicks() {
        player1Dropdown.setOnItemClickListener((parent, view, position, id) -> {
            aux = parentActivity.playersId[0];
            parentActivity.playersId[0] = position;
            player1Dropdown.setText(player1Dropdown.getAdapter().getItem(position).toString(), false);
            if (player2Dropdown.getText().toString().equals(player2Dropdown.getAdapter().getItem(position).toString())) {
                player2Dropdown.setText(player2Dropdown.getAdapter().getItem(aux).toString(), false);
                parentActivity.playersId[1] = aux;
            } else if (player3Dropdown.getText().toString().equals(player3Dropdown.getAdapter().getItem(position).toString())) {
                player3Dropdown.setText(player3Dropdown.getAdapter().getItem(aux).toString(), false);
                parentActivity.playersId[2] = aux;
            } else if (player4Dropdown.getText().toString().equals(player4Dropdown.getAdapter().getItem(position).toString())) {
                player4Dropdown.setText(player4Dropdown.getAdapter().getItem(aux).toString(), false);
                parentActivity.playersId[3] = aux;
            }
        });
        player2Dropdown.setOnItemClickListener((parent, view, position, id) -> {
            aux = parentActivity.playersId[1];
            parentActivity.playersId[1] = position;
            player2Dropdown.setText(player2Dropdown.getAdapter().getItem(position).toString(), false);
            if (player1Dropdown.getText().toString().equals(player1Dropdown.getAdapter().getItem(position).toString())) {
                player1Dropdown.setText(player1Dropdown.getAdapter().getItem(aux).toString(), false);
                parentActivity.playersId[0] = aux;
            } else if (player3Dropdown.getText().toString().equals(player3Dropdown.getAdapter().getItem(position).toString())) {
                player3Dropdown.setText(player3Dropdown.getAdapter().getItem(aux).toString(), false);
                parentActivity.playersId[2] = aux;
            } else if (player4Dropdown.getText().toString().equals(player4Dropdown.getAdapter().getItem(position).toString())) {
                player4Dropdown.setText(player4Dropdown.getAdapter().getItem(aux).toString(), false);
                parentActivity.playersId[3] = aux;
            }
        });
        player3Dropdown.setOnItemClickListener((parent, view, position, id) -> {
            aux = parentActivity.playersId[2];
            parentActivity.playersId[2] = position;
            player3Dropdown.setText(player3Dropdown.getAdapter().getItem(position).toString(), false);
            if (player1Dropdown.getText().toString().equals(player1Dropdown.getAdapter().getItem(position).toString())) {
                player1Dropdown.setText(player1Dropdown.getAdapter().getItem(aux).toString(), false);
                parentActivity.playersId[0] = aux;
            } else if (player2Dropdown.getText().toString().equals(player2Dropdown.getAdapter().getItem(position).toString())) {
                player2Dropdown.setText(player2Dropdown.getAdapter().getItem(aux).toString(), false);
                parentActivity.playersId[1] = aux;
            } else if (player4Dropdown.getText().toString().equals(player4Dropdown.getAdapter().getItem(position).toString())) {
                player4Dropdown.setText(player4Dropdown.getAdapter().getItem(aux).toString(), false);
                parentActivity.playersId[3] = aux;
            }
        });
        player4Dropdown.setOnItemClickListener((parent, view, position, id) -> {
            aux = parentActivity.playersId[3];
            parentActivity.playersId[3] = position;
            player4Dropdown.setText(player4Dropdown.getAdapter().getItem(position).toString(), false);
            if (player1Dropdown.getText().toString().equals(player1Dropdown.getAdapter().getItem(position).toString())) {
                player1Dropdown.setText(player1Dropdown.getAdapter().getItem(aux).toString(), false);
                parentActivity.playersId[0] = aux;
            } else if (player2Dropdown.getText().toString().equals(player2Dropdown.getAdapter().getItem(position).toString())) {
                player2Dropdown.setText(player2Dropdown.getAdapter().getItem(aux).toString(), false);
                parentActivity.playersId[1] = aux;
            } else if (player3Dropdown.getText().toString().equals(player3Dropdown.getAdapter().getItem(position).toString())) {
                player3Dropdown.setText(player3Dropdown.getAdapter().getItem(aux).toString(), false);
                parentActivity.playersId[2] = aux;
            }
        });
    }


    /**
     * Método utilizado para lidar com cliques no botão "enviar", que é responsável por repassar a
     * categoria e pergunta selecionada pelo jogador para que seu oponente responda
     */
    protected void handleTeamSelectionButton() {
        continuar.setOnClickListener(v -> parentActivity.startGame());
    }
}
