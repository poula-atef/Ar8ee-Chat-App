package com.example.ar8ee.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ar8ee.R;
import com.example.ar8ee.Classes.postClass;
import com.example.ar8ee.Adapters.postsAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import com.example.ar8ee.UI.addPost;

public class postsFragment extends Fragment {
    private List<postClass> posts;
    private FloatingActionButton fab;
    private RecyclerView rec_view;
    private SwipeRefreshLayout srl;
    private String type = "default";

    public postsFragment() {
    }

    public postsFragment(String type) {
        this.type = type;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View retView = inflater.inflate(R.layout.fragment_posts, container, false);
        retView.setBackgroundColor(getActivity().getResources().getColor(R.color.colorPrimaryLight));
        rec_view = retView.findViewById(R.id.rec_view_posts);
        srl = retView.findViewById(R.id.swip_layout);

        refreshComponents(retView);

        setFloatingActionButtonAction();
        posts = new ArrayList<>();

        if(type == "default")
            getPosts();
        else
            getMyPosts();
        setSwipToRefrishAction();
        return retView;
    }

    private void getMyPosts() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("posts").child(firebaseUser.getUid());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                posts.clear();
                for(DataSnapshot data:snapshot.getChildren()){
                    postClass post = data.getValue(postClass.class);
                    posts.add(post);
                }

                postsAdapter adapter = new postsAdapter(getContext(),"myPosts");
                adapter.setPosts(posts);
                rec_view.setHasFixedSize(true);
                rec_view.setLayoutManager(new LinearLayoutManager(getContext()));
                rec_view.setAdapter(adapter);
                srl.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void setSwipToRefrishAction() {
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(type == "default")
                    getPosts();
                else
                    getMyPosts();
            }
        });
    }

    private void setFloatingActionButtonAction() {
        if(!type.equals("default"))
            fab.setVisibility(View.GONE);
        else
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getContext(),addPost.class));
                   }
            });
    }

    private void getPosts() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("allPosts");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                posts.clear();
                for(DataSnapshot data:snapshot.getChildren()){
                    postClass post = data.getValue(postClass.class);
                    posts.add(post);
                }

                postsAdapter adapter = new postsAdapter(getContext());
                adapter.setPosts(posts);
                rec_view.setHasFixedSize(true);
                rec_view.setLayoutManager(new LinearLayoutManager(getContext()));
                rec_view.setAdapter(adapter);
                srl.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void refreshComponents(View view){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(view.getContext());
        boolean mode = sharedPreferences.getBoolean("mode_key",false);

        if(mode){
            fab = view.findViewById(R.id.add_post_dark);
            view.findViewById(R.id.posts_background).setBackgroundColor(getResources().getColor(R.color.bodyColorDark));
        }
        else{
            fab = view.findViewById(R.id.add_post_light);
            view.findViewById(R.id.posts_background).setBackgroundColor(getResources().getColor(R.color.colorPrimaryLight));
        }
        fab.setVisibility(View.VISIBLE);
    }

}