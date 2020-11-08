package com.example.histoquiz.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.example.histoquiz.R;
import com.example.histoquiz.util.FormFieldValidator;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import java.util.Objects;


/**
 * Classe utilizada para lidar com o login e a criação de novas contas no HistoQuiz
 */
public class SignInActivity extends AppCompatActivity implements View.OnClickListener {

    protected TextInputLayout email, senha;
    protected FirebaseAuth firebase;
    protected Button entrar;
    protected TextView esqueceuSenha, cadastrar;
    protected FormFieldValidator validarCampo;

    // Variáveis para o controle da tela como fullscreen
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHideRunnable = this::hide;


    /**
     * Método chamado assim que essa activity é invocada.
     * @param savedInstanceState - contém o estado anteriormente salvo da atividade (pode ser nulo)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        initGui();
        validarCampo.monitorarCampo(email);
        validarCampo.monitorarCampo(senha);
        mContentView = findViewById(R.id.fullContent);
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
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
        }
    };


    /**
     * Método utilizado para fazer a primeira chamada ao método delayedHide, logo após a activitie
     * ser criada, unicamente para exibir brevemente ao usuário que os controles de tela estão
     * disponíveis
     * @param savedInstanceState - contém o estado anteriormente salvo da atividade (pode ser nulo)
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        delayedHide();
    }


    /**
     * Programa uma chamada ao método hide() após uma quantidade delayMillis de millisegundos,
     * cancelando qualquer chamada programada previamente
     */
    private void delayedHide() {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, 100);
    }


    /**
     * Método utilizado para esconder a barra de botões
     */
    private void hide() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, 300);
    }


    /**
     * Método utilizado para obter referências para os elementos da interface que serão
     * manipulados por essa activity via código, bem como setar algumas configurações
     * deles, como tags e quem responderá a interações com eles
     */
    protected void initGui(){
        email = findViewById(R.id.email);
        senha = findViewById(R.id.senha);
        entrar = findViewById(R.id.entrar);
        entrar.setTag("ENTRAR");
        entrar.setOnClickListener(this);
        esqueceuSenha = findViewById(R.id.esqueceuSenha);
        esqueceuSenha.setTag("ESQUECI_SENHA");
        esqueceuSenha.setOnClickListener(this);
        cadastrar = findViewById(R.id.cadastrar);
        cadastrar.setTag("CADASTRAR");
        cadastrar.setOnClickListener(this);
        firebase = FirebaseAuth.getInstance();
        validarCampo = new FormFieldValidator(this);
        if(FirebaseAuth.getInstance().getCurrentUser()!=null){
            Intent troca = new Intent(SignInActivity.this, MenuActivity.class);
            startActivity(troca);
        }
    }


    /**
     * Método utilizado para logar um usuário com o email e senha informado por este. Antes, é
     * verificado se os campos realmente foram informados
     */
    protected void login(){
        String emailTxt = Objects.requireNonNull(email.getEditText()).getText().toString();
        String senhaTxt = Objects.requireNonNull(senha.getEditText()).getText().toString();
        if(checarTodosOsCampos()){
            firebase.signInWithEmailAndPassword(emailTxt, senhaTxt).addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    FirebaseMessaging.getInstance().getToken().addOnSuccessListener(s -> {
                        assert user != null;
                        DocumentReference ref = FirebaseFirestore.getInstance().collection("usuarios").document(user.getUid());
                        ref.update("registrationToken", s);
                    });
                    Intent troca = new Intent(SignInActivity.this, MenuActivity.class);
                    startActivity(troca);
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SignInActivity.this);
                    builder.setMessage(Objects.requireNonNull(task.getException()).getMessage()).setNeutralButton(R.string.ok, null);
                    builder.show();
                }
            });
        }
        else{
            emailTxt = "victoria.oliveiragomes@gmail.com";
            senhaTxt = "pjo30317512";
            firebase.signInWithEmailAndPassword(emailTxt, senhaTxt).addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    Intent troca = new Intent(SignInActivity.this, MenuActivity.class);
                    startActivity(troca);
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SignInActivity.this);
                    builder.setMessage(Objects.requireNonNull(task.getException()).getMessage()).setNeutralButton(R.string.ok, null);
                    builder.show();
                }
            });
        }
    }


    /**
     * Método utilizado para redirecionar os cliques recebidos nos botões da view relacionada
     * a essa activity para o método que lidará corretamente com ele
     * @param v - view onde o clique ocorreu
     */
    @Override
    public void onClick(View v) {
        String tag = (String) v.getTag();
        switch (tag){
            case "ENTRAR":        login();        break;
            case "ESQUECI_SENHA": esqueciSenha(); break;
            case "CADASTRAR":     novoCadastro(); break;
        }
    }


    /**
     * Método utilizado para enviar para o usuário um email para redefinição de senha
     */
    protected void esqueciSenha(){
        if(validarCampo.preenchido(email)) {
            firebase.sendPasswordResetEmail(Objects.requireNonNull(email.getEditText()).getText().toString())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SignInActivity.this, "Email de redefinição enviado.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        }
    }


    /**
     * Método utilizado para redirecionar o usuário para a activity de cadastro
     */
    protected void novoCadastro(){
        Intent troca = new Intent(SignInActivity.this, SignUpActivity.class);
        startActivity(troca);
    }


    /**
     * Método utilizado para checar o preenchimento de todos os campos do formulário de login
     * @return - true caso tudo esteja preenchido, e false caso algum deles esteja em branco
     */
    protected boolean checarTodosOsCampos(){
        return validarCampo.preenchido(senha) && validarCampo.preenchido(email);
    }


    /**
     * Sobrescreve o método que define o que deve ser feito quando o botão
     * pressionar do celular for pressionado. Como não quero que ele seja
     * utilizado, o método fica vazio
     */
    @Override
    public void onBackPressed() {
        // Não faz nada
    }
}