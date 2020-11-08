package com.example.histoquiz.fragments;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.histoquiz.R;
import com.example.histoquiz.adapters.FriendsAdapter;
import com.example.histoquiz.adapters.FriendshipRequestAdapter;
import com.example.histoquiz.model.Friend;
import com.example.histoquiz.model.FriendRequest;
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

/**
 * Classe utilizada para manipular a view "Amigos" que é exibida em "Minha conta", mostrando os
 * amigos desse usuário, as solicitações de amizade dele e dando a opção para ele enviar convite
 * para adicionar outro usuário.
 */
public class FriendsFragment extends Fragment {

    protected RecyclerView friendsList;
    protected RecyclerView friendshipRequestList;
    protected View friendView;
    protected Context context;
    protected ArrayList<Friend> friendsSource;
    protected ArrayList<FriendRequest> friendRequestSource;
    protected List<String> aux1, aux2;
    protected RecyclerView.LayoutManager RecyclerViewLayoutManager;
    protected FriendsAdapter adapter;
    protected FriendshipRequestAdapter adapter2;
    protected LinearLayoutManager HorizontalLayout1, HorizontalLayout2;
    protected Button sendFriendRequest;
    protected FormFieldValidator validateForm;
    protected TextInputLayout friendEmail;
    protected FirebaseUser user;
    protected FirebaseFirestore firestore;
    protected LinearLayout friendsRequestsPlace, friendsPlace;


    /**
     * Construtor da classe desse fragmento.
     * @param context - contexto da activity na qual ele está sendo criado.
     */
    public FriendsFragment(Context context) {
        this.context = context;
    }


    /**
     * Método chamado no instante que um fragment desse tipo é instanciado, para que ele instancie
     * a view correspondente a ele na interface do usuário.
     * @param inflater - LayoutInflater que pode ser usado para exibir a view desse fragmento.
     * @param container - Se não for nulo, é a view pai à qual a view desse fragmento deve ser ane-
     *                    xada.
     * @param savedInstanceState - Se não for nulo, este fragmento está sendo reconstruído a partir
     *                             de um estado salvo anteriormente, conforme indicado nesse Bundle.
     * @return - retorna a view criada para esse fragmento.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        friendView = inflater.inflate(R.layout.fragment_friends, container, false);
        initGui();
        return friendView;
    }


    /**
     * Método utilizado para obter uma referência para os elementos da view que está sendo exibida,
     * que serão utilizados para mudar algumas de suas configurações. Além disso, inicializa algumas
     * variáveis que serão utilizadas.
     */
    protected void initGui(){
        firestore = FirebaseFirestore.getInstance();
        sendFriendRequest = friendView.findViewById(R.id.enviarConviteButton);
        friendEmail = friendView.findViewById(R.id.adicionarAmigos);
        validateForm = new FormFieldValidator(context);
        validateForm.monitorarCampo(friendEmail);
        friendsList = friendView.findViewById(R.id.recyclerview);
        friendshipRequestList = friendView.findViewById(R.id.recyclerview2);
        RecyclerViewLayoutManager = new LinearLayoutManager(context);
        friendsRequestsPlace = friendView.findViewById(R.id.pedidosAmz);
        friendsPlace = friendView.findViewById(R.id.friendsPlace);
        HorizontalLayout1 = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        HorizontalLayout2 = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        user = FirebaseAuth.getInstance().getCurrentUser();
        configureSendFriendRequest();
        getFriends();
        getFriendRequests();
    }


    /**
     * Método utilizado para configurar como resposta ao botão de convidar amigo uma função que
     * salva no firebase o convite enviado para um usuário, para adicioná-lo a sua lista de amigos.
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
     * adapter que será utilizado para exibi-los. Caso não hajam amigos para esse usuário, exibe
     * uma mensagem informando isso.
     */
    public void getFriends(){
        aux1 = new ArrayList<>();
        friendsSource = new ArrayList<>();
        Task<QuerySnapshot> friends = firestore.document("amizades/amigos").collection(user.getUid()).get();
        friends.addOnSuccessListener(queryDocumentSnapshots -> {
            if(!queryDocumentSnapshots.isEmpty()) {
                friendsList = new RecyclerView(context);
                friendsList.setLayoutManager(RecyclerViewLayoutManager);
                friendsList.setLayoutManager(HorizontalLayout1);
                friendsPlace.removeViewAt(2);
                friendsPlace.addView(friendsList);
                CollectionReference usuarios = firestore.collection("usuarios");
                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    aux1.add(documentSnapshot.getId());
                }
                usuarios.whereIn(FieldPath.documentId(), aux1).get().addOnSuccessListener(queryDocumentSnapshots1 -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots1) {
                        String name = Objects.requireNonNull(documentSnapshot.get("nome")).toString();
                        String[] separatedName = name.split(" ");
                        if (separatedName.length > 1) {
                            name = separatedName[0] + " " + separatedName[1];
                        } else {
                            name = separatedName[0];
                        }
                        friendsSource.add(new Friend(name, documentSnapshot.getId()));
                    }
                    adapter = new FriendsAdapter(friendsSource, this);
                    friendsList.setAdapter(adapter);
                });
            }
            else{
                TextView message = new TextView(context);
                message.setText(Objects.requireNonNull(getActivity()).getResources().getString(R.string.semAmg));
                message.setTextSize(20);
                message.setTextColor(getActivity().getResources().getColor(R.color.white));

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(20,10,10,10);
                message.setLayoutParams(params);
                message.setGravity(Gravity.CENTER);
                message.setPadding(10,20,10,20);
                Typeface tp = ResourcesCompat.getFont(context, R.font.pinkchicken);
                message.setTypeface(tp);
                friendsPlace.removeView(friendsList);
                friendsPlace.addView(message);
            }
        });
    }


    /**
     * Método utilizado para obter do firebase as solicitações de amizade cadastradas para esse
     * usuário e, após obtê-las, setar o adapter que será utilizado para exibi-las. Caso não hajam
     * solicitações, exibe um texto informando isso ao usuário
     */
    public void getFriendRequests(){
        aux2 = new ArrayList<>();
        friendRequestSource = new ArrayList<>();
        Task<QuerySnapshot> friends = firestore.document("amizades/solicitacoes").collection(user.getUid()).get();
        friends.addOnSuccessListener(queryDocumentSnapshots -> {
            if(!queryDocumentSnapshots.isEmpty()) {
                friendshipRequestList = new RecyclerView(context);
                friendshipRequestList.setLayoutManager(RecyclerViewLayoutManager);
                friendshipRequestList.setLayoutManager(HorizontalLayout2);
                friendsRequestsPlace.removeViewAt(2);
                friendsRequestsPlace.addView(friendshipRequestList);
                CollectionReference usuarios = firestore.collection("usuarios");
                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    aux2.add(documentSnapshot.getId());
                }
                usuarios.whereIn(FieldPath.documentId(), aux2).get().addOnSuccessListener(queryDocumentSnapshots1 -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots1) {
                        String name = Objects.requireNonNull(documentSnapshot.get("nome")).toString();
                        String[] separatedName = name.split(" ");
                        if (separatedName.length > 1) {
                            name = separatedName[0] + " " + separatedName[separatedName.length - 1];
                        } else {
                            name = separatedName[0];
                        }
                        friendRequestSource.add(new FriendRequest(name, documentSnapshot.getId()));
                    }
                    adapter2 = new FriendshipRequestAdapter(friendRequestSource, this);
                    friendshipRequestList.setAdapter(adapter2);
                });
            }
            else{
                TextView message = new TextView(context);
                message.setText(Objects.requireNonNull(getActivity()).getResources().getString(R.string.semSolic));
                message.setTextSize(20);
                message.setTextColor(getActivity().getResources().getColor(R.color.white));

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(20,10,10,10);
                message.setLayoutParams(params);
                message.setGravity(Gravity.CENTER);
                message.setPadding(10,20,10,20);
                Typeface tp = ResourcesCompat.getFont(context, R.font.pinkchicken);
                message.setTypeface(tp);
                friendsRequestsPlace.removeView(friendshipRequestList);
                friendsRequestsPlace.addView(message);
            }
        });
    }
}