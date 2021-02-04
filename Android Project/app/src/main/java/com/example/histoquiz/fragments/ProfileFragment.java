package com.example.histoquiz.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.histoquiz.BuildConfig;
import com.example.histoquiz.R;
import com.example.histoquiz.activities.MyAccountActivity;
import com.example.histoquiz.model.User;
import com.example.histoquiz.util.GlideApp;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.common.io.Files;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

/**
 * Classe utilizada para exibir o fragmente que possui as informações pessoais relativas ao usuário
 * logado no momento (email, nome, senha, etc...)
 */
public class ProfileFragment extends Fragment {

    protected TextInputLayout nome, email, senha, universidade, anoIngresso;
    protected FirebaseUser usuario;
    protected FirebaseFirestore database;
    protected View perfilView;
    protected ImageView selectImage;
    private StorageReference mStorageRef;
    protected Uri selectedImage;
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private static final int REQUEST_PERMISSION = 200;
    protected Uri photoUri;
    protected MyAccountActivity activity;
    protected User dadosUser;
    protected Button saveChanges;
    protected String password;



    /**
     * Método construtor da classe
     */
    public ProfileFragment(MyAccountActivity activity) {
        this.activity = activity;
    }


    /**
     * Método chamado para a criação da view associada a esse fragment
     * @param inflater - objeto do tipo LayoutInflater, onde a view desse fragment será exibida
     * @param container - uma view opcional que será utilizada como a raiz (root) da view que será
     *                    inflada
     * @param savedInstanceState - contém o estado anteriormente salvo da atividade (pode ser nulo)
     * @return - retorna a view criada para esse fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        perfilView = inflater.inflate(R.layout.fragment_profile, container, false);
        initGui();
        setUserInfos();
        return perfilView;
    }


    /**
     * Método utilizado para obter a referência para certos objetos da view que serão manipulados
     * por essa classe e para inicializar alguns outros objetos utilizados aqui no back-end
     */
    protected void initGui(){
        nome = perfilView.findViewById(R.id.nome);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        email = perfilView.findViewById(R.id.email);
        senha = perfilView.findViewById(R.id.senha);
        universidade = perfilView.findViewById(R.id.universidade);
        anoIngresso = perfilView.findViewById(R.id.anoIngresso);
        selectImage = perfilView.findViewById(R.id.selectImage);
        saveChanges = perfilView.findViewById(R.id.salvarButton);
        selectImage.setOnClickListener(v -> selectImage());
        usuario = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseFirestore.getInstance();
        configureSave();
    }

    @Override
    public void onResume() {
        super.onResume();
        //setUserInfos();
    }

    /**
     * Método utilizado para setar nos editText as informações desse usuário da maneira que estão
     * cadastradas no banco de dados atualmente
     */
    protected void setUserInfos(){
        Objects.requireNonNull(email.getEditText()).setText(usuario.getEmail());
        Objects.requireNonNull(senha.getEditText()).setText(getString(R.string.senha));
        Task<DocumentSnapshot> userData = database.collection("usuarios").document(usuario.getUid()).get();
        userData.addOnSuccessListener(documentSnapshot -> {
            dadosUser = documentSnapshot.toObject(User.class);
            assert dadosUser != null;
            Objects.requireNonNull(nome.getEditText()).setText(dadosUser.getNome());
            Objects.requireNonNull(universidade.getEditText()).setText(dadosUser.getUnivers());
            Objects.requireNonNull(anoIngresso.getEditText()).setText(dadosUser.getAnoIng());
            if(!dadosUser.getPhotoName().equals("semFoto")){
                StorageReference photo = mStorageRef.child(String.format("profilePics/%s", dadosUser.getPhotoName()));
                Glide.with(activity).load(photo).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(selectImage);
            }
        });
    }


    /**
     * Método utilizado para solicitar ao usuário que selecione uma imagem para seu perfil já
     * presente no celular ou que tire uma foto
     */
    private void selectImage() {
        final CharSequence[] options = {"Tirar foto", "Selecionar da galeria", "Cancelar"};

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Selecione sua foto de perfil.");

        builder.setItems(options, (dialog, item) -> {
            if (options[item].equals("Tirar foto")) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (activity.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.CAMERA},
                                MY_CAMERA_REQUEST_CODE);
                    }
                    else{
                        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        if(activity.getPackageManager() != null){
                            //Create a file to store the image
                            File photoFile = null;
                            try {
                                photoFile = createImageFile();
                            } catch (IOException ex) {
                                // Error occurred while creating the File
                            }
                            if (photoFile != null) {
                                photoUri = FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID + ".provider", photoFile);
                                intent.putExtra(MediaStore.EXTRA_OUTPUT,
                                        photoUri);
                                startActivityForResult(intent, 0);
                            }
                        }
                    }
                }
                else{
                    Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    if(activity.getPackageManager() != null){
                        //Create a file to store the image
                        File photoFile = null;
                        try {
                            photoFile = createImageFile();
                        } catch (IOException ex) {
                            // Error occurred while creating the File
                        }
                        if (photoFile != null) {
                            photoUri = FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID + ".provider", photoFile);
                            intent.putExtra(MediaStore.EXTRA_OUTPUT,
                                    photoUri);
                            startActivityForResult(intent, 0);
                        }
                    }
                }

            } else if (options[item].equals("Selecionar da galeria")) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (activity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
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
                if(activity.getPackageManager() != null){
                    //Create a file to store the image
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                    }
                    if (photoFile != null) {
                        photoUri = FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID + ".provider", photoFile);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                                photoUri);
                        startActivityForResult(intent, 0);
                    }
                }
            } else {
                Toast.makeText(activity, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
        else if(requestCode == REQUEST_PERMISSION){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto , 1);
            }else {
                Toast.makeText(activity, "storage permission denied", Toast.LENGTH_LONG).show();
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != Activity.RESULT_CANCELED) {
            switch (requestCode) {
                case 0:
                    if (resultCode == Activity.RESULT_OK) {
                        selectedImage = photoUri;
                        Glide.with(activity).load(selectedImage).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(selectImage);
                    }
                    break;
                case 1:
                    if (resultCode == Activity.RESULT_OK && data != null) {
                        selectedImage =  data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        if (selectedImage != null) {
                            Cursor cursor = activity.getContentResolver().query(selectedImage,
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
        File storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName,".jpg", storageDir);
    }


    protected void configureSave(){
        saveChanges.setOnClickListener(v -> {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
                    alertDialog.setTitle("Salvar dados");
                    alertDialog.setMessage("Informe sua senha atual");

                    final EditText input = new EditText(activity);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                    input.setLayoutParams(lp);
                    alertDialog.setView(input);
                    alertDialog.setIcon(R.drawable.ic_password);
                    alertDialog.setPositiveButton("Confirmar",
                            (dialog, which) -> {
                                password = input.getText().toString();
                                updateForReal();
                            });

                    alertDialog.setNegativeButton("Cancelar",
                            (dialog, which) -> dialog.cancel());

                    alertDialog.show();
                });
    }

    protected void updateForReal(){
        reAuth();
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(Objects.requireNonNull(nome.getEditText()).getText().toString()).build();
        usuario.updateProfile(profileUpdates);
        dadosUser.setAnoIng(Objects.requireNonNull(anoIngresso.getEditText()).getText().toString());
        dadosUser.setNome(Objects.requireNonNull(nome.getEditText()).getText().toString());
        dadosUser.setUnivers(Objects.requireNonNull(universidade.getEditText()).getText().toString());
        if(selectedImage != null){
            dadosUser.setPhotoName(usuario.getUid());
            StorageReference riversRef = mStorageRef.child("profilePics/" + usuario.getUid());
            riversRef.putFile(selectedImage)
                    .addOnSuccessListener(taskSnapshot -> riversRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        final Uri downloadUrl = uri;
                    }))
                    .addOnFailureListener(exception -> {
                        // Handle unsuccessful uploads
                        // ...
                    });
        }
        database.collection("usuarios").document(usuario.getUid()).update("anoIng", dadosUser.getAnoIng(),
                "nome", dadosUser.getNome(), "univers", dadosUser.getUnivers(), "photoName", dadosUser.getPhotoName());
        usuario.updateEmail(Objects.requireNonNull(email.getEditText()).getText().toString());
        usuario.updatePassword(Objects.requireNonNull(senha.getEditText()).getText().toString());
    }

    protected void reAuth(){
        AuthCredential credential = EmailAuthProvider.getCredential(Objects.requireNonNull(usuario.getEmail()), password);
        Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).reauthenticate(credential).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Toast.makeText(activity, "Dados atualizados com sucesso!", Toast.LENGTH_LONG).show();
            }
        });
    }
}