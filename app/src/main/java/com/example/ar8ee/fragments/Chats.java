package com.example.ar8ee.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ar8ee.UI.ChatList;
import com.example.ar8ee.R;
import com.example.ar8ee.Classes.UserClass;
import com.example.ar8ee.Adapters.UserAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.example.ar8ee.notification.Token;

public class Chats extends Fragment {
    View view;
    private UserAdapter userAdapter;
    private RecyclerView recyclerView;
    private List<UserClass> userClasses;
    private HashMap<String,String>hmap;
    private List<ChatList> usersList;
    private FirebaseUser firebaseUser;
    private DatabaseReference reference;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_chats, container, false);

        setRecyclerView();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userClasses = new ArrayList<>();
        usersList = new ArrayList<>();
        if (firebaseUser != null)
        {
            setChatsList();
            updateToken(FirebaseInstanceId.getInstance().getToken());
        }
        return view;
    }

    private void setRecyclerView() {
        recyclerView = (RecyclerView)view.findViewById(R.id.rec_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void setChatsList() {
        reference = FirebaseDatabase.getInstance().getReference("ChatList").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    ChatList chat = snapshot.getValue(ChatList.class);
                    usersList.add(chat);
                }
                chatlist();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void updateToken(String tokenParam){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token = new Token(tokenParam);
        reference.child(firebaseUser.getUid()).setValue(token);
    }

    private void chatlist() {
        userClasses = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               userClasses.clear();
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    UserClass userClass = snapshot.getValue(UserClass.class);
                    for(ChatList ch : usersList)   {
                        if(userClass.getId().equals(ch.getId())){
                            userClasses.add(userClass);
                        }
                    }
                }

                hmap = new HashMap<String,String>();
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("status").child(firebaseUser.getUid());
                ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String key = snapshot.getKey().toString();
                            String value = snapshot.getValue().toString();
                            hmap.put(key, value);
                        }
                        for (int i = 0; i < userClasses.size(); i++) {
                            for (String key : hmap.keySet()) {
                                if (userClasses.get(i).getId().equals(key)) {
                                    userClasses.get(i).setStatus(hmap.get(key));
                                }
                            }
                        }
                        Context c = getContext();
                        if(c !=null) {
                            boolean b = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("mode_key", false);
                            userAdapter = new UserAdapter(c, userClasses, true, b);
                            recyclerView.setAdapter(userAdapter);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
