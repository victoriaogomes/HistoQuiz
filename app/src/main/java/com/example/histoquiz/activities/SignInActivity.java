package com.example.histoquiz.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.histoquiz.R;
import com.example.histoquiz.util.FormFieldValidator;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

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


    /**
     * Método chamado assim que essa activity é invocada.
     * @param savedInstanceState - contém o estado anteriormente salvo da atividade (pode ser nulo)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN);
        initGui();
        validarCampo.monitorarCampo(email);
        validarCampo.monitorarCampo(senha);
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
        email.getEditText().setText("victoria.oliveiragomes@gmail.com");
        senha.getEditText().setText("pjo30317512");
//        email.getEditText().setText("eazevedo@hotmail.com");
//        senha.getEditText().setText("eniale12345");
    }


    /**
     * Método utilizado para logar um usuário com o email e senha informado por este. Antes, é
     * verificado se os campos realmente foram informados
     */
    protected void login(){
        String emailTxt = Objects.requireNonNull(email.getEditText()).getText().toString();
        String senhaTxt = Objects.requireNonNull(senha.getEditText()).getText().toString();
        if(checarTodosOsCampos()){
            firebase.signInWithEmailAndPassword(emailTxt, senhaTxt).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Intent troca = new Intent(SignInActivity.this, MenuActivity.class);
                        startActivity(troca);
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(SignInActivity.this);
                        builder.setMessage(Objects.requireNonNull(task.getException()).getMessage()).setNeutralButton(R.string.ok, null);
                        builder.show();
                    }
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