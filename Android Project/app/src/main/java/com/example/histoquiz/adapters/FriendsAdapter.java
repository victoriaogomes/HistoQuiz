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
import com.example.histoquiz.model.Friend;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;
import java.util.Objects;


/**
 * Classe utilizada para auxiliar na exibição da lista de amigos que o usuário logado no momento
 * possui
 */
public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendsHolder> {

    private final List<Friend> list;
    private final FriendsFragment manager;


    /**
     * Método construtor da classe, recebe a lista de amigos desse usuário que deve ser exibida
     * @param horizontalList - lista de amigos do usuário logado no momento
     * @param manager - fragmento que onde essa lista será exibida
     */
    public FriendsAdapter(List<Friend> horizontalList, FriendsFragment manager){
        this.manager = manager;
        this.list = horizontalList;
    }


    /**
     * Método chamado quando o RecyclerView precisa de um novo ViewHolder para listar mais um
     * item (nesse caso, mais um usuário)
     * @param parent - ViewGroup no qual a nova View será adicionada após ser vinculada a uma
     *                 posição do adapter.
     * @param viewType - O tipo da nova view
     * @return - um novo ViewHolder do tipo FriendsHolder
     */
    @NonNull
    @Override
    public FriendsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend, parent, false);
        return new FriendsHolder(itemView);
    }


    /**
     * Método chamado pelo RecyclerView para exibir os dados na posição especificada. Esse método
     * deve atualizar o conteúdo do amigo exibido para refletir o usuário na posição especificada da
     * lista.
     * @param holder - o usuário que deve ser atualizado para representar o conteúdo
     * @param position - A posição do item no conjunto de dados do adapter
     */
    @Override
    public void onBindViewHolder(@NonNull FriendsHolder holder, int position) {
        holder.textView.setText(list.get(position).getName());
    }


    /**
     * Método que retorna o número de amigos que o usuário logado atualmente possui
     * @return - número de amigos
     */
    @Override
    public int getItemCount() {
        return list.size();
    }


    /**
     * Classe interna utilizada para exibir cada um dos amigos desse usuário, e lidar com os cliques
     * no botão de remover amigo
     */
    public class FriendsHolder extends RecyclerView.ViewHolder{
        protected TextView textView;
        protected ImageButton accept, removeFriend;
        public FriendsHolder(View itemView){
            super(itemView);
            textView = itemView.findViewById(R.id.textview);
            accept = itemView.findViewById(R.id.aceitarAmigo);
            accept.setVisibility(View.GONE);
            removeFriend = itemView.findViewById(R.id.recusarAmigo);
            configureRemoveFriend();
        }


        /**
         * Método utilizado para remover um usuário da sua lista de amigos
         */
        protected void configureRemoveFriend(){
            removeFriend.setOnClickListener(v -> {
                for (Friend friend : list) {
                    if(friend.getName().contentEquals(textView.getText())){
                        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                        firestore.document("amizades/amigos/" + Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid() + "/" + friend.getUID()).delete();
                        list.remove(getAdapterPosition());
                        notifyItemRemoved(getAdapterPosition());
                        notifyItemRangeChanged(getAdapterPosition(),list.size());
                        manager.getFriends();
                        break;
                    }
                }
            });
        }
    }
}
