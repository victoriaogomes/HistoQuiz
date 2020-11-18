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
import com.example.histoquiz.activities.GameActivity;
import com.example.histoquiz.model.Slide;
import com.google.android.material.textfield.TextInputLayout;

import java.text.Collator;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Classe utilizada para exibir o dialog que lista todas as possíveis lâminas disponíveis no siste-
 * ma, para que o usuário selecione qual ele imagina estar tentando adivinhar no momento
 */
public class GuessSlideDialog extends AppCompatDialogFragment {

    public TextInputLayout guess;
    public Button goBack, send;
    protected View view;
    protected LayoutInflater inflater;
    protected GameActivity parent;
    protected String[] slides;
    protected AutoCompleteTextView slidesDropdown;
    protected String slideChoosed = "";
    protected Dialog dialog;


    /**
     * Método construtor da classe, recebe como parâmetro a activity que instanciou esse dialog
     * @param parent - activity do tipo GameActivity, que é responsável por gerenciar partidas e
     *                 que criou esse dialog
     */
    public GuessSlideDialog(GameActivity parent){
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
        dealWithButtons();
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
        populateSpinner();
        handleSpinnersClicks();
        return dialog;
    }


    /**
     * Método utilizado para obter a referência para certos objetos da view que serão manipulados
     * por essa classe e para inicializar alguns outros objetos utilizados aqui no back-end
     */
    protected void initGUI(){
        inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
        view = inflater.inflate(R.layout.dialog_guess_slide, null);
        guess = view.findViewById(R.id.spinner_slides);
        slidesDropdown = view.findViewById(R.id.slides_dropdown);
        slidesDropdown.setDropDownWidth((int) Math.round(parent.content.getWidth() - (parent.content.getWidth()*0.1)));
        slidesDropdown.setDropDownHeight((int) Math.round(parent.content.getHeight()*0.4));
        slidesDropdown.setInputType(InputType.TYPE_NULL);
        slidesDropdown.setDropDownBackgroundDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.dropdown_background, parent.getTheme()));
        goBack = view.findViewById(R.id.voltar);
        send = view.findViewById(R.id.enviar);
    }


    /**
     * Método utilizado para popular o spinner com as possíveis lâminas disponíveis no banco de da-
     * dos de forma alfabética, para que o usuário selecione uma
     */
    protected void populateSpinner(){
        int i=0;
        slides =  new String[parent.slides.size()];
        for (Map.Entry<Integer, Slide> pair : parent.slides.entrySet()) {
            slides[i] = pair.getValue().getName();
            i++;
        }
        Arrays.sort(slides, (o1, o2) -> {
            Collator usCollator = Collator.getInstance(new Locale("pt", "BR"));
            return usCollator.compare(o1, o2);
        });
        ArrayAdapter<String> adapter = new ArrayAdapter<>(parent, R.layout.spinner_layout, R.id.textoSpin, slides);
        slidesDropdown.setAdapter(adapter);
        slideChoosed = slidesDropdown.getAdapter().getItem(0).toString();
        slidesDropdown.setText(slideChoosed, false);
    }

    protected void handleSpinnersClicks(){
        slidesDropdown.setOnItemClickListener((parent, view, position, id) -> {
            slideChoosed = slidesDropdown.getAdapter().getItem(position).toString();
            slidesDropdown.setText(slideChoosed, false);
        });
    }

    /**
     * Método utilizado para lidar com os cliques nos botões desse dialog ("enviar" ou "voltar")
     */
    protected void dealWithButtons(){
        send.setOnClickListener(v -> {
            if(parent.PCopponent) parent.computerOpponent._estado_K(slideChoosed);
            else parent.onlineOpponent._estado_J(slideChoosed);
        });
        goBack.setOnClickListener(v -> {
            // Fazer alguma coisa quando clicar em voltar
        });
    }
}
