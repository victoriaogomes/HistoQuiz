package com.example.histoquiz.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.histoquiz.R;
import com.example.histoquiz.adapters.FriendsAdapter;
import com.example.histoquiz.adapters.FriendshipRequestAdapter;
import com.example.histoquiz.util.FormFieldValidator;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;


public class FriendsFragment extends Fragment {

    protected RecyclerView friendsList;
    protected RecyclerView friendshipRequestList;
    protected View friendView;
    protected Context context;
    // Array list for recycler view data source
    protected ArrayList<String> friendsSource, friendRequestSource;
    protected List<String> aux1, aux2;
    // Layout Manager
    protected RecyclerView.LayoutManager RecyclerViewLayoutManager;
    // adapter class object
    protected FriendsAdapter adapter;
    protected FriendshipRequestAdapter adapter2;
    // Linear Layout Manager
    protected LinearLayoutManager HorizontalLayout1, HorizontalLayout2;
    protected Button sendFriendRequest;
    protected FormFieldValidator validateForm;
    protected TextInputLayout friendEmail;
    protected FirebaseUser user;
    protected FirebaseFirestore firestore;

    public FriendsFragment(Context context) {
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        friendView = inflater.inflate(R.layout.fragment_friends, container, false);
        initGui();
        return friendView;
    }

    protected void initGui(){
        firestore = FirebaseFirestore.getInstance();
        sendFriendRequest = friendView.findViewById(R.id.enviarConviteButton);
        friendEmail = friendView.findViewById(R.id.adicionarAmigos);
        validateForm = new FormFieldValidator(context);
        validateForm.monitorarCampo(friendEmail);
        friendsList = friendView.findViewById(R.id.recyclerview);
        friendshipRequestList = friendView.findViewById(R.id.recyclerview2);
        RecyclerViewLayoutManager = new LinearLayoutManager(context);
        friendsList.setLayoutManager(RecyclerViewLayoutManager);
        friendsSource = new ArrayList<>();
        friendRequestSource = new ArrayList<>();
        HorizontalLayout1 = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        friendsList.setLayoutManager(HorizontalLayout1);
        HorizontalLayout2 = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        friendshipRequestList.setLayoutManager(HorizontalLayout2);
        friendshipRequestList.setAdapter(adapter2);
        user = FirebaseAuth.getInstance().getCurrentUser();
        configureSendFriendRequest();
        getFriends();
        getFriendRequests();
    }


    /**
     * Método utilizado para configurar como resposta ao botão de convidar amigo uma função que
     * salva no firebase o convite enviado para um usuário, para adicioná-lo a sua lista de amigos
     */
    protected void configureSendFriendRequest(){
        sendFriendRequest.setOnClickListener(v -> {
            if(validateForm.preenchido(friendEmail)){
                Task<DocumentSnapshot> friendUID = firestore.collection("tabelaAuxiliar").document(Objects.requireNonNull(friendEmail.getEditText()).getText().toString()).get();
                friendUID.addOnSuccessListener(documentSnapshot -> firestore.document("amizades/solicitacoes").collection(Objects.requireNonNull(documentSnapshot.get("UID")).toString()).document(user.getUid()).set(new HashMap<String, Object>()));
            }
        });
    }


    /**
     * Método utilizado para obter do firebase os amigos desse usuário e, após obtê-los, setar o
     * adapter que será utilizado para exibi-los
     */
    protected void getFriends(){
        aux1 = new ArrayList<>();
        Task<QuerySnapshot> friends = firestore.document("amizades/amigos").collection(user.getUid()).get();
        friends.addOnSuccessListener(queryDocumentSnapshots -> {
            CollectionReference usuarios = firestore.collection("usuarios");
            for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                aux1.add(documentSnapshot.getId());
            }
            usuarios.whereIn(FieldPath.documentId(), aux1).get().addOnSuccessListener(queryDocumentSnapshots1 -> {
                for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots1){
                    String name = Objects.requireNonNull(documentSnapshot.get("nome")).toString();
                    String[] separatedName = name.split(" ");
                    if(separatedName.length > 1){
                        name = separatedName[0] + " " + separatedName[1];
                    }
                    else {
                        name = separatedName[0];
                    }
                    friendsSource.add(name);
                }
                adapter = new FriendsAdapter(friendsSource);
                friendsList.setAdapter(adapter);
            });
        });
    }


    /**
     * Método utilizado para obter do firebase as solicitações de amizade cadastradas para esse
     * usuário e, após obtê-las, setar o adapter que será utilizado para exibi-las
     */
    protected void getFriendRequests(){
        aux2 = new ArrayList<>();
        Task<QuerySnapshot> friends = firestore.document("amizades/solicitacoes").collection(user.getUid()).get();
        friends.addOnSuccessListener(queryDocumentSnapshots -> {
            CollectionReference usuarios = firestore.collection("usuarios");
            for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                aux2.add(documentSnapshot.getId());
            }
            usuarios.whereIn(FieldPath.documentId(), aux2).get().addOnSuccessListener(queryDocumentSnapshots1 -> {
                for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots1){
                    String name = Objects.requireNonNull(documentSnapshot.get("nome")).toString();
                    String[] separatedName = name.split(" ");
                    if(separatedName.length > 1){
                        name = separatedName[0] + " " + separatedName[separatedName.length-1];
                    }
                    else {
                        name = separatedName[0];
                    }
                    friendRequestSource.add(name);
                }
                adapter2 = new FriendshipRequestAdapter(friendRequestSource);
                friendshipRequestList.setAdapter(adapter2);
            });
        });
    }

}