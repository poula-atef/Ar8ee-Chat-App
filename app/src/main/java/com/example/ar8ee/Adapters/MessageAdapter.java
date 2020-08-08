package com.example.ar8ee.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ar8ee.Classes.ChatClass;
import com.example.ar8ee.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    FirebaseUser firebaseUser;
    public static final int TYPE_MESSAGE_LEFT = 0;
    public static final int TYPE_MESSAGE_RIGHT = 1;
    private int Last;
    private Context context;
    private List<ChatClass> messages;
    private String imageurl;
    private boolean mode;
    private MessageAdapter.ViewHolder viewHolder = null;

    public MessageAdapter(Context context, List<ChatClass> messages,String imageurl,boolean mode) {
        this.context = context;
        this.messages = messages;
        this.mode = mode;
        this.imageurl = imageurl;
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        getLastIndex();
    }

    private void getLastIndex() {
        int count = 0;
        for(ChatClass ch:messages){
            if(ch.getRecever().equals(firebaseUser.getUid())){
                Last = count;
            }
            count++;
        }
    }

    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == TYPE_MESSAGE_RIGHT)
            if(mode)
                view = LayoutInflater.from(context).inflate(R.layout.chat_item_right_dark,parent,false);
            else
                view = LayoutInflater.from(context).inflate(R.layout.chat_item_right,parent,false);
        else
            view = LayoutInflater.from(context).inflate(R.layout.chat_item_left,parent,false);

        viewHolder = new MessageAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MessageAdapter.ViewHolder holder, int position) {
        final ChatClass chat = messages.get(position);
        holder.massage.setText(chat.getMessage());
        holder.massage.setMaxWidth(650);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser.getUid().equals(chat.getRecever()) && position == Last){
        if(imageurl.equals("default"))
            holder.image.setImageResource(R.mipmap.ic_launcher_round);
        else
            Glide.with(context).load(imageurl).into(holder.image);
    }

    if(position == messages.size() - 1){
        if(chat.getSeen().equals("true")){
            holder.seen.setText("Seen");
        }
        else{
            holder.seen.setText("Delivered");
        }
    }
    else
        holder.seen.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder{
        public TextView massage;
        public ImageView image;
        private TextView seen;

        public ViewHolder(View itemView) {
            super(itemView);
            massage = itemView.findViewById(R.id.show_message);
            image = itemView.findViewById(R.id.profile_image);
            seen = itemView.findViewById(R.id.seen);
        }
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(messages.get(position).getSender().equals(firebaseUser.getUid()))
            return TYPE_MESSAGE_RIGHT;
        else
            return TYPE_MESSAGE_LEFT;

    }
}
