package com.example.histoquiz.dialogs;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.DialogFragment;
import com.example.histoquiz.R;
import com.example.histoquiz.activities.GameActivity;
import com.example.histoquiz.util.GlideApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
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
    protected Dialog dialog;


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
        dialog = new Dialog(getActivity());
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        initGUI();
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
        position = 0;
        inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
        view = inflater.inflate(R.layout.dialog_slide_image, null);
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
        Object [] keySet;
        Integer aux = 0;
        //Toast.makeText(parent, Arrays.toString(keySet), Toast.LENGTH_LONG).show();
        String slide;
        if(parent.PCopponent){
            slide = parent.computerOpponent.slideToGuess;
            keySet = parent.mySlides.keySet().toArray();
        }
        else{
            slide = parent.onlineOpponent.opponentSlideToGuess;
            keySet = parent.onlineOpponent.opponentSlides.keySet().toArray();
        }
        switch (slide){
            case "firstSlide":
                if(parent.PCopponent) aux = (Integer) keySet[3];
                else aux = (Integer) keySet[0];
                break;
            case "secondSlide":
                if(parent.PCopponent) aux = (Integer) keySet[4];
                else aux = (Integer) keySet[1];
                break;
            case "thirdSlide":
                if(parent.PCopponent) aux = (Integer) keySet[5];
                else aux = (Integer) keySet[2];
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
