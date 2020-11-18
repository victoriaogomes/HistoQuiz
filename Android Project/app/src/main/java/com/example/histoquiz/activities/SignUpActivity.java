package com.example.histoquiz.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.example.histoquiz.R;
import com.example.histoquiz.util.FormFieldValidator;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Classe utilizada para manipular a view com o formulário que deve ser preenchido
 * para cadastrar um novo usuário e para efetivar esse cadastro junto ao firebase,
 * lidando com eventuais erros que venham a ocorrer
 */
public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    protected Button cadastrar, voltar;
    protected FirebaseAuth firebase;
    protected TextInputLayout nome, email, senha, universidade, anoIngresso;
    protected FormFieldValidator validarCampo;


    /**
     * Método chamado assim que essa activity é invocada.
     * @param savedInstanceState - contém o estado anteriormente salvo da atividade (pode ser nulo).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        initGui();
        validarCampo.monitorarCampo(nome);
        validarCampo.monitorarCampo(email);
        validarCampo.monitorarCampo(senha);
        validarCampo.monitorarCampo(universidade);
        validarCampo.monitorarCampo(anoIngresso);
        firebase = FirebaseAuth.getInstance();
        validarCampo = new FormFieldValidator(this);
        hideSystemUI();

    }


    /**
     * Método chamado quando a janela atual da activity ganha ou perde o foco, é utilizado para es-
     * conder novamente a barra de status e a navigation bar.
     * @param hasFocus - booleano que indica se a janela desta atividade tem foco.
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        hideSystemUI();
    }


    /**
     * Método utilizado para fazer com que a barra de status e a navigation bar não sejam exibidas
     * na activity. Caso o usuário queira visualizá-las, ele deve realizar um movimento de arrastar
     * para cima (na navigation bar), ou para baixo (na status bar), o que fará com que elas apare-
     * çam por um momento e depois sumam novamente.
     */
    private void hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
            WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
            if(controller != null) {
                controller.hide(WindowInsetsCompat.Type.statusBars());
                controller.hide(WindowInsetsCompat.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        }
        else {
            //noinspection deprecation
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }


    /**
     * Método utilizado para obter referências para os elementos da interface que serão
     * manipulados por essa activity via código, bem como setar algumas configurações
     * deles, como tags e quem responderá a interações com eles.
     */
    protected void initGui(){
        cadastrar = findViewById(R.id.cadastrarButton);
        cadastrar.setTag("CADASTRAR");
        cadastrar.setOnClickListener(this);
        voltar = findViewById(R.id.voltar);
        voltar.setTag("VOLTAR");
        voltar.setOnClickListener(this);
        nome = findViewById(R.id.nome);
        email = findViewById(R.id.email);
        senha = findViewById(R.id.senha);
        universidade = findViewById(R.id.universidade);
        anoIngresso = findViewById(R.id.anoIngresso);
        validarCampo = new FormFieldValidator(this);
    }


    /**
     * Método utilizado para lidar com os cliques nos botões da view de cadastro de usuário. Ele
     * obtém a tag do elemento que disparou esse evento (ou seja, foi clicado) e, a partir dela,
     * redireciona o código para o método que irá lidar com ele.
     * @param v - view que recebeu o clique.
     */
    @Override
    public void onClick(View v) {
        String tag = (String) v.getTag();
        switch (tag){
            case "CADASTRAR":
                if(checarTodosOsCampos()) {
                    novoUsuario();
                }
                break;
            case "VOLTAR": voltar(); break;
        }
    }


    /**
     * Método utilizado para cadastrar um novo usuário no firebase.
     */
    protected void novoUsuario(){
        String emailTxt = Objects.requireNonNull(email.getEditText()).getText().toString();
        String senhaTxt = Objects.requireNonNull(senha.getEditText()).getText().toString();
        firebase.createUserWithEmailAndPassword(emailTxt, senhaTxt).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

            /**
             * Método chamando quando a tentativa de cadastrar um novo usuário no Firebase
             * é finalizada. Caso se obtenha sucesso, ele cadastra as demais informações do
             * usuário no firebase. Caso haja algum erro, este é exibido para o usuário.
             * @param task - task resultante da solicitação feita.
             */
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                FirebaseUser user = firebase.getCurrentUser();
                FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                assert user != null;
                if (task.isSuccessful()) {
                    String dataConta = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
                    Map<String, Object> dadosUsuario = new HashMap<>();
                    dadosUsuario.put("nome", Objects.requireNonNull(nome.getEditText()).getText().toString());
                    dadosUsuario.put("univers", Objects.requireNonNull(universidade.getEditText()).getText().toString());
                    dadosUsuario.put("anoIng",  Objects.requireNonNull(anoIngresso.getEditText()).getText().toString());
                    dadosUsuario.put("dataConta", dataConta);
                    FirebaseMessaging.getInstance().getToken().addOnSuccessListener(s -> {
                        DocumentReference ref = FirebaseFirestore.getInstance().collection("usuarios").document(user.getUid());
                        dadosUsuario.put("registrationToken", s);
                        firestore.collection("usuarios").document(user.getUid()).set(dadosUsuario);
                        dadosUsuario.clear();
                        dadosUsuario.put("UID", user.getUid());
                        firestore.collection("tabelaAuxiliar").document(Objects.requireNonNull(user.getEmail())).set(dadosUsuario);
                        dadosUsuario.clear();
                        dadosUsuario.put("numPartidas", 0);
                        dadosUsuario.put("sisCardiopulmonar", new ArrayList<>(Arrays.asList(0, 0)));
                        dadosUsuario.put("sisDigestorio", new ArrayList<>(Arrays.asList(0, 0)));
                        dadosUsuario.put("sisOsteomuscular", new ArrayList<>(Arrays.asList(0, 0)));
                        dadosUsuario.put("sisReprodutor", new ArrayList<>(Arrays.asList(0, 0)));
                        dadosUsuario.put("vitorias", 0);
                        firestore.collection("desempenho").document(user.getUid()).set(dadosUsuario);
                        Intent troca = new Intent(SignUpActivity.this, MenuActivity.class);
                        startActivity(troca);
                    });
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                    builder.setMessage(Objects.requireNonNull(task.getException()).getMessage()).setNeutralButton(R.string.ok, null);
                    builder.show();
                }
            }
        });
    }


    /**
     * Método utilizado para checar o preenchimento de todos os campos do formulário de cadastro.
     * @return - true caso tudo esteja preenchido, e false caso algum deles esteja em branco.
     */
    protected boolean checarTodosOsCampos(){
        return validarCampo.preenchido(nome) && validarCampo.preenchido(email) && validarCampo.preenchido(senha)
                && validarCampo.preenchido(universidade) && validarCampo.preenchido(anoIngresso);
    }


    /**
     * Sobrescreve o método que define o que deve ser feito quando o botão voltar do celular for
     * pressionado. Como não quero que ele seja utilizado, o método fica vazio.
     */
    @Override
    public void onBackPressed() {
        // Não faz nada
    }


    /**
     * Método utilizado para retornar a activity de login.
     */
    protected void voltar(){
        Intent troca = new Intent(SignUpActivity.this, SignInActivity.class);
        startActivity(troca);
    }
}