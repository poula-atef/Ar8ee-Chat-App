package com.example.ar8ee.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ar8ee.UI.Chat;
import com.example.ar8ee.Classes.ChatClass;
import com.example.ar8ee.Classes.UserClass;
import com.example.ar8ee.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private Context context;
    private List<UserClass> userClasses;
    private ViewHolder viewHolder = null;
    private boolean ischat,modeVal;
    String Lastmessage;
    String sender;
    String seen;
    public UserAdapter(Context context, List<UserClass> userClasses, boolean ischat,boolean modVal) {
        this.context = context;
        this.userClasses = userClasses;
        this.ischat = ischat;
        this.modeVal = modVal;
    }

    @Override
    public ViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item,parent,false);
        viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder( ViewHolder holder, int position) {
       final UserClass userClass = userClasses.get(position);
        holder.username.setText(userClass.getUsername());
        if(userClass.getImageURL().equals("default"))
            holder.image.setImageResource(R.mipmap.ic_launcher_round);
        else
            Glide.with(context).load(userClass.getImageURL()).into(holder.image);

        if(ischat){
            if(userClass.getStatus().equals("online")){
                holder.image_on.setVisibility(View.VISIBLE);
                holder.image_off.setVisibility(View.GONE);
                holder.image_ready.setVisibility(View.GONE);
            }
            else if(userClass.getStatus().equals("offline")){
                holder.image_off.setVisibility(View.VISIBLE);
                holder.image_on.setVisibility(View.GONE);
                holder.image_ready.setVisibility(View.GONE);
            }
            else if(userClass.getStatus().equals("ready")){
                holder.image_ready.setVisibility(View.VISIBLE);
                holder.image_on.setVisibility(View.GONE);
                holder.image_off.setVisibility(View.GONE);
            }
        }

        if(modeVal){
            holder.last_message.setTextColor(context.getResources().getColor(R.color.bodyColorLight));
            holder.username.setTextColor(context.getResources().getColor(R.color.bodyColorLight));
        }
        else{
            holder.last_message.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
            holder.username.setTextColor(context.getResources().getColor(R.color.bodyColorDark));
        }

        if(ischat){
            get_lastmessage(userClass.getId(),holder.last_message,holder.seen_icon,userClass.getImageURL());
        }
        else
            holder.last_message.setVisibility(View.GONE);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, Chat.class);
                i.putExtra("userid", userClass.getId());
                context.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userClasses.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder{
        public TextView username;
        public ImageView image;
        public ImageView image_on;
        public ImageView image_off;
        public ImageView image_ready;
        public TextView last_message;
        public ImageView seen_icon;

        public ViewHolder( View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            image = itemView.findViewById(R.id.profile_image);
            seen_icon = itemView.findViewById(R.id.seen_icon);
            image_on = itemView.findViewById(R.id.image_on);
            image_off = itemView.findViewById(R.id.image_off);
            image_ready = itemView.findViewById(R.id.image_ready);
            last_message = itemView.findViewById(R.id.last_message);
        }

    }

    private void get_lastmessage(final String userid, final TextView view, final ImageView seen_icon,final String imageUrl){
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Lastmessage = "default";
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    ChatClass ch = snapshot.getValue(ChatClass.class);
                    if(firebaseUser!=null && ch.getSender().equals(firebaseUser.getUid()) && ch.getRecever().equals(userid)
                            ||firebaseUser!=null && ch.getRecever().equals(firebaseUser.getUid()) && ch.getSender().equals(userid))
                    {

                        Lastmessage = ch.getMessage();
                        sender = ch.getSender();
                        seen = ch.getSeen();
                    }
                }

                if(Lastmessage == "default") {
                    view.setText("No Messages");
                    seen_icon.setVisibility(View.GONE);
                }
                else {
                    view.setText(Lastmessage);
                    if(!sender.equals(userid)){
                        if(seen.equals("true")) {
                            seen_icon.setVisibility(View.VISIBLE);
                            Glide.with(context).load(imageUrl).into(seen_icon);
                        }
                        else
                            seen_icon.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
