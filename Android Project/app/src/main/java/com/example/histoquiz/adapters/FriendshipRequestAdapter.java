package com.example.histoquiz.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.histoquiz.R;
import com.example.histoquiz.fragments.FriendsFragment;
import com.example.histoquiz.model.FriendRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * Classe utilizada para auxiliar na exibição da lista de solicitações de amizade que o usuário
 * logado no momento possui
 */
public class FriendshipRequestAdapter extends RecyclerView.Adapter<FriendshipRequestAdapter.FriendshipRequestHolder> {

    private final List<FriendRequest> list;
    private final FriendsFragment manager;


    /**
     * Método construtor da classe, recebe a lista de solicitações de amizade desse usuário que deve
     * ser exibida
     * @param horizontalList - lista de solicitações de amizade do usuário logado no momento
     * @param manager - fragmento que onde essa lista será exibida
     */
    public FriendshipRequestAdapter(List<FriendRequest> horizontalList, FriendsFragment manager){
        this.manager = manager;
        this.list = horizontalList;
    }


    /**
     * Método chamado quando o RecyclerView precisa de um novo ViewHolder para listar mais um
     * item (nesse caso, mais uma solicitação de amizade)
     * @param parent - ViewGroup no qual a nova View será adicionada após ser vinculada a uma
     *                 posição do adapter.
     * @param viewType - O tipo da nova view
     * @return - um novo ViewHolder do tipo FriendshipRequestHolder
     */
    @NonNull
    @Override
    public FriendshipRequestHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend, parent, false);
        return new FriendshipRequestAdapter.FriendshipRequestHolder(itemView);
    }


    /**
     * Método chamado pelo RecyclerView para exibir os dados na posição especificada. Esse método
     * deve atualizar o conteúdo do amigo exibido para refletir a solicitação de amizade presente
     * na posição especificada da lista.
     * @param holder - o usuário que deve ser atualizado para representar o conteúdo
     * @param position - A posição do item no conjunto de dados do adapter
     */
    @Override
    public void onBindViewHolder(@NonNull FriendshipRequestAdapter.FriendshipRequestHolder holder, int position) {
        holder.textView.setText(list.get(position).getName());
    }


    /**
     * Método que retorna o número de solicitações de amizade que o usuário logado atualmente possui
     * @return - número de solicitações de amizade
     */
    @Override
    public int getItemCount() {
        return list.size();
    }


    /**
     * Classe interna utilizada para exibir cada uma das solicitações de amizade desse usuário, e
     * lidar com os cliques no botão de aceitar ou recusar solicitação de amizade
     */
    public class FriendshipRequestHolder extends RecyclerView.ViewHolder{
        TextView textView;
        protected ImageButton removeRequest, acceptRequest;
        public FriendshipRequestHolder(View itemView){
            super(itemView);
            textView = itemView.findViewById(R.id.textview);
            acceptRequest = itemView.findViewById(R.id.aceitarAmigo);
            removeRequest = itemView.findViewById(R.id.recusarAmigo);
            configureAcceptFriendship();
            configureRejectFriendship();
        }


        /**
         * Método utilizado para aceitar a solicitação de amizade de um usuário
         */
        protected void configureAcceptFriendship(){
            acceptRequest.setOnClickListener(view -> {
                for (FriendRequest request : list) {
                   if(request.getName().contentEquals(textView.getText())){
                       FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                       firestore.document("amizades/solicitacoes/" + Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid() + "/" + request.getUID()).delete();
                       firestore.document("amizades/amigos").collection(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).document(request.getUID()).set(new HashMap<String, Object>());
                       firestore.document("amizades/amigos").collection(request.getUID()).document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).set(new HashMap<String, Object>());
                       list.remove(getAdapterPosition());
                       notifyItemRemoved(getAdapterPosition());
                       notifyItemRangeChanged(getAdapterPosition(),list.size());
                       manager.getFriends();
                       manager.getFriendRequests();
                       break;
                   }
                }
            });
        }


        /**
         * Método utilizado para rejeitar uma solicitação de amizade feita por um usuário
         */
        protected void configureRejectFriendship(){
            removeRequest.setOnClickListener(view -> {
                for (FriendRequest request : list) {
                    if(request.getName().contentEquals(textView.getText())){
                        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                        firestore.document("amizades/solicitacoes/" + Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid() + "/" + request.getUID()).delete();
                        list.remove(getAdapterPosition());
                        notifyItemRemoved(getAdapterPosition());
                        notifyItemRangeChanged(getAdapterPosition(),list.size());
                        manager.getFriendRequests();
                        break;
                    }
                }
            });
        }
    }
}
