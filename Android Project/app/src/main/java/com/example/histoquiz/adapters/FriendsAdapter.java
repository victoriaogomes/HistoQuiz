package com.example.histoquiz.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.histoquiz.R;
import com.example.histoquiz.model.Friend;
import com.example.histoquiz.model.FriendRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Objects;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendsHolder> {

    private List<Friend> list;

    public FriendsAdapter(List<Friend> horizontalList){
        this.list = horizontalList;
    }

    @NonNull
    @Override
    public FriendsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend, parent, false);
        return new FriendsHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendsHolder holder, int position) {
        holder.textView.setText(list.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

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

        protected void configureRemoveFriend(){
            removeFriend.setOnClickListener(v -> {
                for (Friend friend : list) {
                    if(friend.getName().contentEquals(textView.getText())){
                        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                        firestore.document("amizades/amigos/" + Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid() + "/" + friend.getUID()).delete();
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
