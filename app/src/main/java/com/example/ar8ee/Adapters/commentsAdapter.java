package com.example.ar8ee.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ar8ee.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class commentsAdapter extends RecyclerView.Adapter<commentsAdapter.commentHolder> {
    List<String> userNames;
    List<String> userImages;
    List<String> userIds;
    String postId;
    Context context;
    public commentsAdapter(List<String> userNames, List<String> userIds, List<String> userImages, String postId, Context context) {
        this.userNames = userNames;
        this.userImages = userImages;
        this.userIds = userIds;
        this.postId = postId;
        this.context = context;
    }

    public commentsAdapter() {
    }

    @NonNull
    @Override
    public commentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new commentHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_element,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull final commentHolder holder, int position) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("postsComments").child(postId).child(userIds.get(position));
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                holder.comment.setText(snapshot.getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        holder.userName.setText(userNames.get(position));
        if(!userImages.get(position).equals(""))
            Glide.with(context).load(userImages.get(position)).into(holder.userImage);
        else
            holder.userImage.setImageResource(R.mipmap.ic_launcher_round);
    }

    @Override
    public int getItemCount() {
        if(userNames == null)
            return 0;
        return userNames.size();
    }

    public class commentHolder extends RecyclerView.ViewHolder {
        CircleImageView userImage;
        TextView userName,comment;
        public commentHolder(@NonNull View itemView) {
            super(itemView);
            userImage = itemView.findViewById(R.id.comment_image);
            userName = itemView.findViewById(R.id.comment_username);
            comment = itemView.findViewById(R.id.comment_body);
        }
    }
}
