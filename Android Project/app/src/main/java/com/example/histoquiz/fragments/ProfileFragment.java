package com.example.histoquiz.fragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.histoquiz.R;
import com.example.histoquiz.model.User;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
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


    /**
     * Método construtor da classe
     */
    public ProfileFragment() {

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
        email = perfilView.findViewById(R.id.email);
        senha = perfilView.findViewById(R.id.senha);
        universidade = perfilView.findViewById(R.id.universidade);
        anoIngresso = perfilView.findViewById(R.id.anoIngresso);
        usuario = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseFirestore.getInstance();
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
            User dadosUser = documentSnapshot.toObject(User.class);
            assert dadosUser != null;
            Objects.requireNonNull(nome.getEditText()).setText(dadosUser.getNome());
            Objects.requireNonNull(universidade.getEditText()).setText(dadosUser.getUnivers());
            Objects.requireNonNull(anoIngresso.getEditText()).setText(dadosUser.getAnoIng());
        });
    }
}