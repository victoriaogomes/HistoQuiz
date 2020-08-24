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

public class FriendshipRequestAdapter extends RecyclerView.Adapter<FriendshipRequestAdapter.FriendshipRequestHolder> {

    private List<FriendRequest> list;
    private FriendsFragment manager;

    public FriendshipRequestAdapter(List<FriendRequest> horizontalList, FriendsFragment manager){
        this.manager = manager;
        this.list = horizontalList;
    }

    @NonNull
    @Override
    public FriendshipRequestAdapter.FriendshipRequestHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend, parent, false);
        return new FriendshipRequestAdapter.FriendshipRequestHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendshipRequestAdapter.FriendshipRequestHolder holder, int position) {
        holder.textView.setText(list.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

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

        protected void configureAcceptFriendship(){
            acceptRequest.setOnClickListener(view -> {
                for (FriendRequest request : list) {
                   if(request.getName().contentEquals(textView.getText())){
                       FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                       firestore.document("amizades/solicitacoes/" + Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid() + "/" + request.getUID()).delete();
                       firestore.document("amizades/amigos").collection(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).document(request.getUID()).set(new HashMap<String, Object>());
                       list.remove(getAdapterPosition());
                       notifyItemRemoved(getAdapterPosition());
                       notifyItemRangeChanged(getAdapterPosition(),list.size());
                       manager.getFriends();
                       break;
                   }
                }
            });
        }

        protected void configureRejectFriendship(){
            removeRequest.setOnClickListener(view -> {
                for (FriendRequest request : list) {
                    if(request.getName().contentEquals(textView.getText())){
                        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                        firestore.document("amizades/solicitacoes/" + Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid() + "/" + request.getUID()).delete();
                        list.remove(getAdapterPosition());
                        notifyItemRemoved(getAdapterPosition());
                        notifyItemRangeChanged(getAdapterPosition(),list.size());
                        break;
                    }
                }
            });
        }
    }
}
