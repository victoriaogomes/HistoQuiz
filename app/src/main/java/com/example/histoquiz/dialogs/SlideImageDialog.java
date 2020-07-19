package com.example.histoquiz.dialogs;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import com.example.histoquiz.R;
import com.example.histoquiz.activities.GameActivity;
import com.example.histoquiz.util.GlideApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Locale;
import java.util.Objects;

public class SlideImageDialog extends AppCompatDialogFragment implements View.OnClickListener {

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

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        initGUI();
        builder.setView(view).setTitle("");
        Dialog dialog = builder.create();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        View decorView = parent.getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN);
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
        Animation out = AnimationUtils.loadAnimation(parent, android.R.anim.slide_out_right);
        Animation in = AnimationUtils.loadAnimation(parent, android.R.anim.slide_in_left);
        imageSwitcher.setOutAnimation(out);
        imageSwitcher.setInAnimation(in);
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
        switch (parent.myOpponent.slideToGuess){
            case "firstSlide":
                storageReference = FirebaseStorage.getInstance().getReference(Objects.requireNonNull(parent.mySlides.get(keySet[0])).getImages().get(position));
                imagesAmount = Objects.requireNonNull(parent.mySlides.get(keySet[0])).getImages().size();
                slideName.setText(String.format(Locale.getDefault(), "%s: foto %d de %d", Objects.requireNonNull(parent.mySlides.get(keySet[0])).getName(), position + 1, imagesAmount));
                break;
            case "secondSlide":
                storageReference = FirebaseStorage.getInstance().getReference(Objects.requireNonNull(parent.mySlides.get(keySet[1])).getImages().get(position));
                imagesAmount = Objects.requireNonNull(parent.mySlides.get(keySet[1])).getImages().size();
                break;
            case "thirdSlide":
                storageReference = FirebaseStorage.getInstance().getReference(Objects.requireNonNull(parent.mySlides.get(keySet[2])).getImages().get(position));
                imagesAmount = Objects.requireNonNull(parent.mySlides.get(keySet[2])).getImages().size();
                break;
        }
        if(imagesAmount == 1 || (position+1) == imagesAmount){
            this.position = -1;
        }
        GlideApp.with(parent).load(storageReference).into((ImageView) imageSwitcher.getCurrentView());
    }
}
