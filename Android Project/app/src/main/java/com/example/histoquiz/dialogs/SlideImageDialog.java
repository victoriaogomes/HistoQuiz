package com.example.histoquiz.dialogs;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
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
import androidx.fragment.app.DialogFragment;

import com.example.histoquiz.R;
import com.example.histoquiz.activities.GameActivity;
import com.example.histoquiz.util.GlideApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

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

    public SlideImageDialog(GameActivity parent){
        this.parent = parent;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Objects.requireNonNull(this.getActivity()).getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
//        View decorView = getActivity().getWindow().getDecorView();
//        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_FULLSCREEN);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        initGUI();
        builder.setView(view).setTitle("");
        Dialog dialog = builder.create();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

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

    public void imageToShow(int position){
        Object [] keySet = parent.mySlides.keySet().toArray();
        Integer aux = 0;
        Toast.makeText(parent, Arrays.toString(keySet), Toast.LENGTH_LONG).show();
        switch (parent.myOpponent.slideToGuess){
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
