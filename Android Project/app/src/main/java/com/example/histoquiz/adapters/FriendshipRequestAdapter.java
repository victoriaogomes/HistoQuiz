package com.example.histoquiz.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.histoquiz.R;

import java.util.List;

public class FriendshipRequestAdapter extends RecyclerView.Adapter<FriendshipRequestAdapter.FriendshipRequestHolder> {
    private List<String> list;

    public FriendshipRequestAdapter(List<String> horizontalList){
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
        holder.textView.setText(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class FriendshipRequestHolder extends RecyclerView.ViewHolder{
        TextView textView;
        public FriendshipRequestHolder(View itemView){
            super(itemView);
            textView = (TextView)itemView
                    .findViewById(R.id.textview);
        }
    }
}
