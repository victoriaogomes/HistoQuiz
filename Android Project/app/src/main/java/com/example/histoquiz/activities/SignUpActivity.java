package com.example.histoquiz.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.histoquiz.BuildConfig;
import com.example.histoquiz.R;
import com.example.histoquiz.util.FormFieldValidator;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.common.io.Files;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
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
    protected ImageView selectImage;
    private StorageReference mStorageRef;
    protected Uri selectedImage;
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private static final int REQUEST_PERMISSION = 200;
    protected Uri photoUri;


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
        mStorageRef = FirebaseStorage.getInstance().getReference();
        cadastrar = findViewById(R.id.cadastrarButton);
        cadastrar.setTag("CADASTRAR");
        selectImage = findViewById(R.id.selectImage);
        selectImage.setTag("SELECIONAR_IMAGEM");
        selectImage.setOnClickListener(this);
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
     * Método utilizado para solicitar ao usuário que selecione uma imagem para seu perfil já
     * presente no celular ou que tire uma foto
     */
    private void selectImage() {
        final CharSequence[] options = {"Tirar foto", "Selecionar da galeria", "Cancelar"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selecione sua foto de perfil.");

        builder.setItems(options, (dialog, item) -> {

            if (options[item].equals("Tirar foto")) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.CAMERA},
                                MY_CAMERA_REQUEST_CODE);
                    }
                    else{
                        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        if(getPackageManager() != null){
                            //Create a file to store the image
                            File photoFile = null;
                            try {
                                photoFile = createImageFile();
                            } catch (IOException ex) {
                                // Error occurred while creating the File
                            }
                            if (photoFile != null) {
                                photoUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", photoFile);
                                intent.putExtra(MediaStore.EXTRA_OUTPUT,
                                        photoUri);
                                startActivityForResult(intent, 0);
                            }
                        }
                    }
                }
                else{
                    Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    if(getPackageManager() != null){
                        //Create a file to store the image
                        File photoFile = null;
                        try {
                            photoFile = createImageFile();
                        } catch (IOException ex) {
                            // Error occurred while creating the File
                        }
                        if (photoFile != null) {
                            photoUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", photoFile);
                            intent.putExtra(MediaStore.EXTRA_OUTPUT,
                                    photoUri);
                            startActivityForResult(intent, 0);
                        }
                    }
                }

            } else if (options[item].equals("Selecionar da galeria")) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                REQUEST_PERMISSION);
                    }
                    else{
                        Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(pickPhoto , 1);
                    }
                }
                else{
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto , 1);
                }

            } else if (options[item].equals("Cancelar")) {
                dialog.dismiss();
            }
        });
        builder.show();
    }


    /**
     * Método utilizado para abrir a câmera, após o usuário permitir
     * @param requestCode - código da requisição
     * @param permissions - resultado da permissão
     * @param grantResults -
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                if(getPackageManager() != null){
                    //Create a file to store the image
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                    }
                    if (photoFile != null) {
                        photoUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", photoFile);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                                photoUri);
                        startActivityForResult(intent, 0);
                    }
                }
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
        else if(requestCode == REQUEST_PERMISSION){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto , 1);
            }else {
                Toast.makeText(this, "storage permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }


    /**
     * Método utilizado para lidar com o arquivo de imagem fornecido pelo usuário para a sua foto
     * de perfil
     * @param requestCode - código da operação
     * @param resultCode - código indicando o resultado da operação
     * @param data - dados resultantes da operação realizada (nesse caso, fornecimento de uma imagem)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case 0:
                    if (resultCode == RESULT_OK) {
                        selectedImage = photoUri;
                        Glide.with(this).load(photoUri).into(selectImage);
                    }
                    break;
                case 1:
                    if (resultCode == RESULT_OK && data != null) {
                        selectedImage =  data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        if (selectedImage != null) {
                            Cursor cursor = getContentResolver().query(selectedImage,
                                    filePathColumn, null, null, null);
                            if (cursor != null) {
                                cursor.moveToFirst();
                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                String picturePath = cursor.getString(columnIndex);
                                selectImage.setImageBitmap(BitmapFactory.decodeFile(picturePath));
                                cursor.close();
                            }
                        }

                    }
                    break;
            }
        }
    }


    /**
     *
     * @return
     * @throws IOException
     */
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName,".jpg", storageDir);
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
            case "SELECIONAR_IMAGEM": selectImage();
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
                if (task.isSuccessful()) {
                    FirebaseUser user = firebase.getCurrentUser();
                    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                    assert user != null;
                    String dataConta = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
                    Map<String, Object> dadosUsuario = new HashMap<>();
                    dadosUsuario.put("nome", Objects.requireNonNull(nome.getEditText()).getText().toString());
                    dadosUsuario.put("univers", Objects.requireNonNull(universidade.getEditText()).getText().toString());
                    dadosUsuario.put("anoIng",  Objects.requireNonNull(anoIngresso.getEditText()).getText().toString());
                    dadosUsuario.put("dataConta", dataConta);
                    if(selectedImage != null){
                        StorageReference riversRef = mStorageRef.child("profilePics/" + user.getUid());
                        riversRef.putFile(selectedImage)
                                .addOnSuccessListener(taskSnapshot -> riversRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                    final Uri downloadUrl = uri;
                                }))
                                .addOnFailureListener(exception -> {
                                    // Handle unsuccessful uploads
                                    // ...
                                });
                        dadosUsuario.put("photoName", user.getUid());
                    }
                    else{
                        dadosUsuario.put("photoName", "semFoto");
                    }
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
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(Objects.requireNonNull(nome.getEditText()).getText().toString()).build();
                        user.updateProfile(profileUpdates);
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