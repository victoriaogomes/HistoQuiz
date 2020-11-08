package com.example.histoquiz.dialogs;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import com.example.histoquiz.R;
import com.example.histoquiz.activities.GameActivity;
import com.example.histoquiz.util.GlideApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

/**
 * Classe utilizada para exibir ao jogador imagem da lâmina atual que o seu oponente está tentando
 * adivinhar no momento, para auxiliá-lo no momento de responder os questionamentos enviados pelo
 * seu oponente
 */
public class SlideImageDialog extends DialogFragment implements View.OnClickListener {

    protected GameActivity parent;
    protected LayoutInflater inflater;
    protected View view;
    protected ImageButton next;
    protected ImageSwitcher imageSwitcher;
    protected Button goBack;
    protected StorageReference storageReference;
    protected TextView slideName;
    protected int position;
    protected ImageView myImageView;
    protected int imagesAmount;

    // Variáveis para o controle da tela como fullscreen
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;


    /**
     * Método construtor da classe, recebe como parâmetro a activity que instanciou esse dialog
     * @param parent - activity do tipo GameActivity, que é responsável por gerenciar
     *                         partidas e que criou esse dialog
     */
    public SlideImageDialog(GameActivity parent){
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
        androidx.appcompat.app.ActionBar actionBar = ((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
        }
    };


    /**
     * Programa uma chamada ao método hide() após uma quantidade delayMillis de millisegundos,
     * cancelando qualquer chamada programada previamente
     */
    private void hideNow() {
       androidx.appcompat.app.ActionBar actionBar = ((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar();
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
        position = 0;
        inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
        view = inflater.inflate(R.layout.dialog_slide_image, null);
        mContentView = view.findViewById(R.id.fullContent);
        goBack = view.findViewById(R.id.voltar);
        goBack.setOnClickListener(this);
        goBack.setTag("GO_BACK");
        next = view.findViewById(R.id.proximoButton);
        next.setTag("NEXT");
        next.setOnClickListener(this);
        slideName = view.findViewById(R.id.nomeLamina);
        imageSwitcher = view.findViewById(R.id.imageSW);
        imageSwitcher.setFactory(() -> {
            myImageView = new ImageView(parent.getApplicationContext());
            myImageView.setLayoutParams(new ImageSwitcher.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT));
            myImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            return myImageView;
        });
        imageToShow(0);
        imageSwitcher.setOutAnimation(AnimationUtils.loadAnimation(parent, android.R.anim.slide_out_right));
        imageSwitcher.setInAnimation(AnimationUtils.loadAnimation(parent, android.R.anim.slide_in_left));
    }


    /**
     * Método utilizado para lidar com os cliques no dialog que exibe as imagens, que é o botão de
     * "voltar", para fechar a exibição da imagem da lâmina, e o botão "próximo", que aparece somen-
     * te quando a lâmina possui mais de uma foto cadastrada no banco de dados
     * @param v - view que recebeu o clique do usuário
     */
    @Override
    public void onClick(View v) {
        switch (v.getTag().toString()){
            case "NEXT":
                position++;
                imageToShow(position);
                break;
            case "GO_BACK":
                parent.closeSlideImages();
                break;
        }
    }


    /**
     * Método que realiza efetivamente a exibição da imagem presente na posição recebida por parâ-
     * metro (primeira imagem de uma lâmina, segunda imagem, etc...)
     * @param position - posição da foto a ser exibida (1ª, 2ª, 3ª, ...)
     */
    public void imageToShow(int position){
        Object [] keySet = parent.mySlides.keySet().toArray();
        Integer aux = 0;
        Toast.makeText(parent, Arrays.toString(keySet), Toast.LENGTH_LONG).show();
        switch (parent.computerOpponent.slideToGuess){
            case "firstSlide":
                aux = (Integer) keySet[3];
                break;
            case "secondSlide":
                aux = (Integer) keySet[4];
                break;
            case "thirdSlide":
                aux = (Integer) keySet[5];
                break;
        }
        storageReference = FirebaseStorage.getInstance().getReference(Objects.requireNonNull(parent.mySlides.get(aux)).getImages().get(position));
        imagesAmount = Objects.requireNonNull(parent.mySlides.get(aux)).getImages().size();
        slideName.setText(String.format(Locale.getDefault(), "%s: foto %d de %d", Objects.requireNonNull(parent.mySlides.get(aux)).getName(), position + 1, imagesAmount));
        if(imagesAmount == 1 || (position+1) == imagesAmount){
            this.position = -1;
        }
        if(imagesAmount == 1){
            next.setVisibility(View.GONE);
        }
        else{
            next.setVisibility(View.VISIBLE);
        }
        GlideApp.with(parent).load(storageReference).into((ImageView) imageSwitcher.getCurrentView());
    }
}
