package com.example.histoquiz.activities;

import androidx.appcompat.app.AppCompatActivity;
import com.example.histoquiz.R;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class RevisionActivity extends AppCompatActivity {

    // Sistema selecionado para ver a revisão
    String selectedSystem;

    //Componentes da tela de revisão
    TextView systemTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_revision);
        Intent intent = getIntent();
        selectedSystem = intent.getStringExtra("selectedSystem");

    }

    protected void initGUI(){
        systemTitle = findViewById(R.id.systemTitle);
    }




}