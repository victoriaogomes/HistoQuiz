package com.example.histoquiz.dialogs;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import com.example.histoquiz.R;
import com.example.histoquiz.activities.GameActivity;
import com.example.histoquiz.util.GlideApp;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.Objects;

public class SlideImageDialog extends AppCompatDialogFragment implements View.OnClickListener {

    protected GameActivity parent;
    protected LayoutInflater inflater;
    protected View view;
    protected MaterialButton next;
    protected ImageSwitcher imageSwitcher;
    protected StorageReference storageReference;
    protected int position = 0;
    protected ImageView myImageView;

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
        inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
        view = inflater.inflate(R.layout.dialog_slide_image, null);
        next = view.findViewById(R.id.proximoButton);
        next.setTag("NEXT");
        next.setOnClickListener(this);
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
        if(v.getTag() == "NEXT"){
            position++;
            imageToShow(position);
        }
    }

    public void imageToShow(int position){
        int imagesAmount;
        Object [] keySet = parent.mySlides.keySet().toArray();
        switch (parent.myOpponent.slideToGuess){
            case "firstSlide":
                storageReference = FirebaseStorage.getInstance().getReference(Objects.requireNonNull(parent.mySlides.get(keySet[0])).getImages().get(position));
                imagesAmount = Objects.requireNonNull(parent.mySlides.get(keySet[0])).getImages().size();
                Toast.makeText(parent, Integer.toString(imagesAmount), Toast.LENGTH_LONG).show();
                if(imagesAmount > 1 && (position+1) < imagesAmount){
                    next.setVisibility(View.VISIBLE);
                }
                else{
                    next.setVisibility(View.GONE);
                }
                break;
            case "secondSlide":
                storageReference = FirebaseStorage.getInstance().getReference(Objects.requireNonNull(parent.mySlides.get(keySet[1])).getImages().get(position));
                imagesAmount = Objects.requireNonNull(parent.mySlides.get(keySet[1])).getImages().size();
                if(imagesAmount > 1 && (position+1) < imagesAmount){
                    next.setVisibility(View.VISIBLE);
                }
                else{
                    next.setVisibility(View.GONE);
                }
                break;
            case "thirdSlide":
                storageReference = FirebaseStorage.getInstance().getReference(Objects.requireNonNull(parent.mySlides.get(keySet[2])).getImages().get(position));
                imagesAmount = Objects.requireNonNull(parent.mySlides.get(keySet[2])).getImages().size();
                if(imagesAmount > 1 && (position+1) < imagesAmount){
                    next.setVisibility(View.VISIBLE);
                }
                else{
                    next.setVisibility(View.GONE);
                }
                break;
        }
        GlideApp.with(parent).load(storageReference).into(myImageView);
    }
}
