package com.example.ar8ee.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.ar8ee.Classes.UserClass;
import com.example.ar8ee.Classes.postClass;
import com.example.ar8ee.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class postsAdapter extends RecyclerView.Adapter<postsAdapter.postViewHolder> {
    List<postClass> posts;
    Context context;
    int w,h,operation,position;
    String type = "default";
    boolean undo,isClicked = false;
    public postsAdapter(Context context) {
        this.context = context;
    }

    public postsAdapter(Context context, String type) {
        this.context = context;
        this.type = type;
    }
    public postsAdapter() {
    }

    @NonNull
    @Override
    public postViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new postViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_element,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull final postViewHolder holder, int position) {
        if(posts.get(position).getPostImage() != null){
        if(type.equals("default")){
            holder.deletePostBtn.setVisibility(View.GONE);
        }
        holder.body.setText(posts.get(position).getBody());
        holder.postId.setText(posts.get(position).getPostId());
        if(!posts.get(position).getLikes().equals("0"))
            holder.likeBtn.setText("(" + posts.get(position).getLikes() + ") like");
        else
            holder.likeBtn.setText("like");
        holder.userName.setText(posts.get(position).getUserName());
        Glide.with(context).load(posts.get(position).getUserImage()).into(holder.userImage);
        if(!posts.get(position).getPostImage().equals("")) {

            Glide.with(context)
                    .asBitmap()
                    .load(posts.get(position).getPostImage())
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap bitmap,
                                                    Transition<? super Bitmap> transition) {
                            w = bitmap.getWidth();
                            h = bitmap.getHeight();
                            Log.d("log","width is =====> " + w);
                            Log.d("log","height is =====> " + h);
                        }
                    });
            Glide.with(context)
                    .load(posts.get(position).getPostImage())
                    .into(holder.postImage);

            if(h <= dpToPx(300,context) || w <= dpToPx(300,context)){
                holder.postImage.getLayoutParams().height = h;
                holder.postImage.getLayoutParams().width = w;
            }
            else{
                holder.postImage.getLayoutParams().height = dpToPx(h/(w/300),context);
                holder.postImage.getLayoutParams().width = dpToPx(300,context);
            }
        }
        else
            holder.postImage.setVisibility(View.GONE);

        Log.d("log1","postPhoto inside =====> " + posts.get(position).getPostImage());

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            final boolean mode = sharedPreferences.getBoolean("mode_key",false);


            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("postsLikes").child(posts.get(position).getPostId());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
                HashMap map = (HashMap) snapshot.getValue();
                if(map != null && fuser!= null && map.containsKey(fuser.getUid())){
                    holder.likeBtn.setTextColor(context.getResources().getColor(R.color.bodyColorLight));
                    if(!mode) {
                        holder.likeBtn.setBackground(context.getResources().getDrawable(R.drawable.background_like));
                        holder.btns_back.setBackgroundColor(context.getResources().getColor(R.color.colorPrimaryLight));
                    }
                    else{
                        holder.likeBtn.setBackground(context.getResources().getDrawable(R.drawable.background_like_dark));
                        holder.btns_back.setBackgroundColor(context.getResources().getColor(R.color.bodyColorDark));
                    }
                }
                else{
                    if(!mode) {
                        holder.likeBtn.setTextColor(context.getResources().getColor(R.color.colorPrimary));
                        holder.btns_back.setBackgroundColor(context.getResources().getColor(R.color.colorPrimaryLight));
                    }
                    else {
                        holder.likeBtn.setTextColor(context.getResources().getColor(R.color.bodyColorDark));
                        holder.btns_back.setBackgroundColor(context.getResources().getColor(R.color.bodyColorDark));
                    }
                    holder.likeBtn.setBackground(context.getResources().getDrawable(R.drawable.background));

                }

                if(!mode){
                    holder.commentMainBtn.setTextColor(context.getResources().getColor(R.color.colorPrimary));
                    holder.seeAllBtn.setTextColor(context.getResources().getColor(R.color.colorPrimary));
                    holder.commentBtn.setImageResource(R.drawable.ic_action_name);
                }
                else{
                    holder.commentMainBtn.setTextColor(context.getResources().getColor(R.color.bodyColorDark));
                    holder.seeAllBtn.setTextColor(context.getResources().getColor(R.color.bodyColorDark));
                    holder.commentBtn.setImageResource(R.drawable.send_black);
                }
                holder.commentMainBtn.setBackground(context.getResources().getDrawable(R.drawable.background));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    }

    public static int dpToPx(int dp, Context context) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
    @Override
    public int getItemCount() {
        if(posts == null)
            return 0;
        return posts.size();
    }

    public void setPosts(List<postClass> posts) {
        this.posts = posts;
    }

    public class postViewHolder extends RecyclerView.ViewHolder {
        TextView userName,body,postId;
        CircleImageView userImage;
        ImageView postImage;
        Button likeBtn;
        Button seeAllBtn;
        Button commentMainBtn;
        ImageButton commentBtn;
        EditText commentEt;
        LottieAnimationView deletePostBtn;
        LinearLayout btns_back;
        public postViewHolder(@NonNull final View itemView) {
            super(itemView);
            userName = (TextView)itemView.findViewById(R.id.username_post);
            postImage = (ImageView)itemView.findViewById(R.id.post_image);
            userImage = (CircleImageView) itemView.findViewById(R.id.profile_image);
            body = (TextView)itemView.findViewById(R.id.body_post);
            postId = (TextView)itemView.findViewById(R.id.post_id);
            likeBtn = itemView.findViewById(R.id.likeBtn);
            commentBtn = itemView.findViewById(R.id.comment_btn);
            seeAllBtn = itemView.findViewById(R.id.see_all_btn);
            commentEt = itemView.findViewById(R.id.comment_et);
            commentMainBtn = itemView.findViewById(R.id.comment_main_btn);
            deletePostBtn = itemView.findViewById(R.id.delete_post_btn);
            btns_back = itemView.findViewById(R.id.btns_back);

            setLikeBtnAction();

            setCommentMainBtnAction();

            setSeeAllBtnAction();

            setCommentBtnAction();

            deletePostBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    if(!isClicked){
                        isClicked = true;
                        deletePostBtn.playAnimation();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                final int pos = getLayoutPosition();
                                final postClass postClass = posts.get(pos);
                                posts.remove(getLayoutPosition());
                                notifyItemRemoved(getLayoutPosition());
                                undo = false;
                                Snackbar.make(v,"you just deleted a post",Snackbar.LENGTH_LONG)
                                        .setAction("Undo", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        posts.add(pos, postClass);
                                        notifyItemInserted(pos);
                                        undo = true;
                                    }
                                }).show();
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(!undo) {
                                            FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
                                            Log.i("log", "post deleted forever ====================>");
                                            if(!postClass.getPostImage().equals(""))
                                            {
                                                    FirebaseStorage.getInstance().getReferenceFromUrl(postClass.getPostImage())
                                                        .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                          Toast.makeText(context, "Removed Successfully !!", Toast.LENGTH_SHORT).show();
                                                    }
                                                     });
                                            }
                                            FirebaseDatabase.getInstance().getReference("allPosts").child(postClass.getPostId()).getRef().removeValue();
                                            FirebaseDatabase.getInstance().getReference("posts").child(fuser.getUid()).child(postClass.getPostId()).getRef().removeValue();
                                            FirebaseDatabase.getInstance().getReference("postsComments").child(postClass.getPostId()).getRef().removeValue();
                                            FirebaseDatabase.getInstance().getReference("postsLikes").child(postClass.getPostId()).getRef().removeValue();
                                            isClicked = false;
                                        }
                                    }
                                }, 3500);
                            }
                        },1500);
                    }
                }
            });
        }

        private void setCommentBtnAction() {
            commentBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();

                    if(!commentEt.getText().equals(""))
                    {
                        FirebaseDatabase.getInstance().getReference("postsComments")
                                .child(posts.get(position).getPostId())
                                .child(fuser.getUid())
                                .setValue(commentEt.getText().toString());
                        commentEt.setText("");
                    }
                    else
                        Toast.makeText(context,"Can't send an empty comment!!",Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void setSeeAllBtnAction() {
            seeAllBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                    final boolean mode = sharedPreferences.getBoolean("mode_key",false);
                    position = getLayoutPosition();
                    Log.i("log","position is ====> " + position);
                    if(seeAllBtn.getCurrentTextColor() == itemView.getResources().getColor(R.color.colorPrimary) ||
                        seeAllBtn.getCurrentTextColor() == itemView.getResources().getColor(R.color.bodyColorDark)){

                        seeAllBtn.setTextColor(itemView.getResources().getColor(R.color.bodyColorLight));
                        if(!mode) {
                            seeAllBtn.setBackground(itemView.getResources().getDrawable(R.drawable.background_like));
                        }
                        else{
                            seeAllBtn.setBackground(itemView.getResources().getDrawable(R.drawable.background_like_dark));
                        }
                        final RecyclerView comments_rec = itemView.findViewById(R.id.comments_rc);

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                        final List<UserClass>allUsers = new ArrayList<>();
                        reference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for(DataSnapshot data:snapshot.getChildren()){
                                    UserClass userClass = data.getValue(UserClass.class);
                                    allUsers.add(userClass);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        final List<String>userNames = new ArrayList<>();
                        final List<String>userIds = new ArrayList<>();
                        final List<String>userImages = new ArrayList<>();
                        reference = FirebaseDatabase.getInstance().getReference("postsComments").child(posts.get(position).getPostId());
                        reference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for(DataSnapshot data:snapshot.getChildren()){
                                    for(UserClass userClass : allUsers){
                                        if(data.getKey().equals(userClass.getId())){
                                            userNames.add(userClass.getUsername());
                                            userImages.add(userClass.getImageURL());
                                            userIds.add(userClass.getId());
                                        }
                                    }
                                }
                                commentsAdapter adapter = new commentsAdapter(userNames,userIds,userImages,posts.get(position).getPostId(),context);
                                comments_rec.setAdapter(adapter);
                                comments_rec.setHasFixedSize(true);
                                comments_rec.setLayoutManager(new LinearLayoutManager(context));
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        itemView.findViewById(R.id.comments_area).setVisibility(View.VISIBLE);
                    }
                    else{
                        itemView.findViewById(R.id.comments_area).setVisibility(View.GONE);

                        if(!mode)
                            seeAllBtn.setTextColor(itemView.getResources().getColor(R.color.colorPrimary));
                        else
                            seeAllBtn.setTextColor(itemView.getResources().getColor(R.color.bodyColorDark));
                        seeAllBtn.setBackground(itemView.getResources().getDrawable(R.drawable.background));
                    }
                }
            });

        }

        private void setCommentMainBtnAction() {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            final boolean mode = sharedPreferences.getBoolean("mode_key",false);
            commentMainBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(commentMainBtn.getCurrentTextColor() == itemView.getResources().getColor(R.color.colorPrimary) ||
                       commentMainBtn.getCurrentTextColor() == itemView.getResources().getColor(R.color.bodyColorDark)){
                        itemView.findViewById(R.id.comment_area).setVisibility(View.VISIBLE);
                        if(!mode){
                            commentMainBtn.setBackground(itemView.getResources().getDrawable(R.drawable.background_like));
                        }
                        else{
                            commentMainBtn.setBackground(itemView.getResources().getDrawable(R.drawable.background_like_dark));
                        }
                        commentMainBtn.setTextColor(itemView.getResources().getColor(R.color.bodyColorLight));
                    }
                    else{
                        itemView.findViewById(R.id.comment_area).setVisibility(View.GONE);
                        if(!mode){
                            commentMainBtn.setTextColor(itemView.getResources().getColor(R.color.colorPrimary));
                        }
                        else{
                            commentMainBtn.setTextColor(itemView.getResources().getColor(R.color.bodyColorDark));
                        }
                        commentMainBtn.setBackground(itemView.getResources().getDrawable(R.drawable.background));
                     }
                }
            });
        }

        private void setLikeBtnAction() {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            final boolean mode = sharedPreferences.getBoolean("mode_key",false);

            likeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    position = getLayoutPosition();
                    final FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
                    if(likeBtn.getCurrentTextColor() == itemView.getResources().getColor(R.color.colorPrimary) ||
                       likeBtn.getCurrentTextColor() == itemView.getResources().getColor(R.color.bodyColorDark)){
                        operation = 1;
                        likeBtn.setTextColor(context.getResources().getColor(R.color.bodyColorLight));
                        if(!mode) {
                            likeBtn.setBackground(context.getResources().getDrawable(R.drawable.background_like));
                        }
                        else{
                            likeBtn.setBackground(context.getResources().getDrawable(R.drawable.background_like_dark));
                        }
                        Log.d("log2","you pressed like ===> ");
                        FirebaseDatabase.getInstance().getReference("postsLikes").child(posts.get(position).getPostId()).child(fuser.getUid()).setValue("true");
                    }
                    else{
                        operation = -1;
                        if(!mode) {
                            likeBtn.setBackground(context.getResources().getDrawable(R.drawable.background));
                            likeBtn.setTextColor(context.getResources().getColor(R.color.colorPrimary));
                        }
                        else {
                            likeBtn.setBackground(context.getResources().getDrawable(R.drawable.background));
                            likeBtn.setTextColor(context.getResources().getColor(R.color.bodyColorDark));
                        }
                        Log.d("log2","you pressed dislike ===> ");
                        FirebaseDatabase.getInstance().getReference("postsLikes").child(posts.get(position).getPostId()).child(fuser.getUid()).getRef().removeValue();
                    }

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("allPosts");
                    reference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for(DataSnapshot data : snapshot.getChildren()){
                                postClass postClass = data.getValue(com.example.ar8ee.Classes.postClass.class);
                                if(posts.get(position).getPostId().equals(postClass.getPostId())){
                                    String num = "" + (Integer.parseInt(postClass.getLikes()) + operation);
                                    if(!num.equals("0"))
                                        likeBtn.setText("(" + num + ")" + "like");
                                    else
                                        likeBtn.setText("like");
                                    FirebaseDatabase.getInstance().getReference("allPosts").child(postClass.getPostId()).child("likes").setValue(num);
                                    FirebaseDatabase.getInstance().getReference("posts").child(postClass.getUserId()).child(postClass.getPostId()).child("likes").setValue(num);
                                    break;
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
            });


        }

    }
}

