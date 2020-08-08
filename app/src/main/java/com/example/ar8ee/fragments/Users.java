package com.example.ar8ee.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.ar8ee.R;
import com.example.ar8ee.Classes.UserClass;
import com.example.ar8ee.Adapters.UserAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class Users extends Fragment {
    RecyclerView recyclerView;
    UserAdapter userAdapter;
    View view;
    HashMap<String,String>hmap;
    ArrayList<UserClass> userClasses;
    EditText search;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_users, container, false);
        search = (EditText)view.findViewById(R.id.search);
        recyclerView = (RecyclerView) view.findViewById(R.id.rec_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        userClasses = new ArrayList<>();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean mode = sharedPreferences.getBoolean("mode_key",false);
        if(mode){
            search.setHintTextColor(getResources().getColor(R.color.bodyColorLight));
            search.setTextColor(getResources().getColor(R.color.bodyColorLight));
        }
        else{
            search.setHintTextColor(getResources().getColor(R.color.hintDarkColor));
            search.setTextColor(getResources().getColor(R.color.bodyColorDark));
        }
        getUsers();

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUsers(s.toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void searchUsers(String s) {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("search")
                .startAt(s).endAt(s + "\uf8ff");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userClasses.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    UserClass userClass = snapshot.getValue(UserClass.class);
                    if(!userClass.getId().equals(firebaseUser.getUid()))
                        userClasses.add(userClass);
                }

                hmap = new HashMap<String,String>();
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("status").child(firebaseUser.getUid());
                ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String key = snapshot.getKey().toString();
                            String value = snapshot.getValue().toString();
                            hmap.put(key,value);
                        }
                        for(int i = 0;i<userClasses.size();i++){
                            for(String key:hmap.keySet()) {
                                if(userClasses.get(i).getId().equals(key)){
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

    private void getUsers(){
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (search.getText().toString().equals("")) {
                    userClasses.clear();
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        UserClass userClass = dataSnapshot1.getValue(UserClass.class);
                        assert userClass != null;
                        if (!userClass.getId().equals(firebaseUser.getUid()))
                            userClasses.add(userClass);
                    }
                    hmap = new HashMap<String,String>();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("status").child(firebaseUser.getUid());
                    ref.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                String key = snapshot.getKey().toString();
                                String value = snapshot.getValue().toString();
                                hmap.put(key,value);
                            }
                            for(int i = 0;i<userClasses.size();i++){
                                for(String key:hmap.keySet()) {
                                    if(userClasses.get(i).getId().equals(key)){
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
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
