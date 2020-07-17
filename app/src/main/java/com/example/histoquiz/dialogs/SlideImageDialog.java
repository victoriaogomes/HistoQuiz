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
import android.widget.ViewSwitcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.bumptech.glide.Glide;
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
        storageReference = FirebaseStorage.getInstance().getReference("Laminas/osteoclasto_4.jpeg");
        inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
        view = inflater.inflate(R.layout.dialog_slide_image, null);
        imageSwitcher = view.findViewById(R.id.imageSW);
        imageSwitcher.setFactory(() -> {
            ImageView imageView = new ImageView(parent.getApplicationContext());
            imageView.setLayoutParams(new ImageSwitcher.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT));
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            GlideApp.with(parent).load(storageReference).into(imageView);
//                imageView.setImageResource(imagem);
            return imageView;
        });
        next = view.findViewById(R.id.proximoButton);
        next.setTag("NEXT");
        Animation out = AnimationUtils.loadAnimation(parent, android.R.anim.slide_out_right);
        Animation in = AnimationUtils.loadAnimation(parent, android.R.anim.slide_in_left);
        imageSwitcher.setOutAnimation(out);
        imageSwitcher.setInAnimation(in);
    }

    @Override
    public void onClick(View v) {

    }
}
