package com.example.ar8ee.notification;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.RemoteInput;

import com.example.ar8ee.Classes.ChatClass;
import com.example.ar8ee.Classes.UserClass;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import com.example.ar8ee.fragments.APIService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyIntentService extends IntentService {
    private FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private String msg;
    private APIService apiservice;
    private String myName;
    private String myImage;
    private String recever;
    private String sender;
    public MyIntentService() {
        super("MyIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        NotificationManager manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        manager.cancelAll();
        String action = intent.getAction();
        recever = intent.getStringExtra("recever");
        sender = firebaseUser.getUid();
        if(action.equals(MyFirebaseMessaging.REPLY_KEY)){
            msg = getMessageText(intent).toString();
            sendMessage(sender,recever,msg);
        }
        else if(action.equals(MyFirebaseMessaging.SEEN_KEY)){
            seenMessage(recever);
        }
    }

    private CharSequence getMessageText(Intent intent) {
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null) {
            return remoteInput.getCharSequence(MyFirebaseMessaging.REPLY_KEY);
        }
        return null;
    }

    private void sendMessage(final String sender, final String recever, String message){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String,String> map = new HashMap<>();
        map.put("sender",sender);
        map.put("recever",recever);
        map.put("message",message);
        map.put("seen","false");
        reference.child("Chats").push().setValue(map);

        final DatabaseReference dbr = FirebaseDatabase.getInstance().getReference("ChatList").child(sender).child(recever);
        dbr.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    dbr.child("id").setValue(recever);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        final DatabaseReference dbr1 = FirebaseDatabase.getInstance().getReference("ChatList").child(recever).child(sender);
        dbr1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    dbr1.child("id").setValue(sender);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserClass user = dataSnapshot.getValue(UserClass.class);
                    sendNotification(recever, firebaseUser.getUid(), msg);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendNotification(final String recever, final String sender, final String message){


        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    UserClass userClass = dataSnapshot1.getValue(UserClass.class);
                    assert userClass != null;
                    if (userClass.getId().equals(firebaseUser.getUid())){
                        myName = userClass.getUsername().toString();
                        myImage = userClass.getImageURL().toString();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

  ////////////////////////////////////////////////////////////////////////////////////////////////////

        apiservice = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
        DatabaseReference dbr = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = dbr.orderByKey().equalTo(recever);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snapshot1 : snapshot.getChildren()){
                    Token token = snapshot1.getValue(Token.class);
                    Data data = new Data(firebaseUser.getUid(),myImage,
                            myName + ":" + message,myName + " is talking to you",recever);
                    Sender sender1 = new Sender(data,token.getToken());

                    apiservice.sendNotification(sender1).enqueue(new Callback<MyResponse>() {
                        @Override
                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                            if(response.code() == 200 && response.body().success != 1)
                                Toast.makeText(getApplicationContext(), "Failed!!", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Call<MyResponse> call, Throwable t) {

                        }

                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void seenMessage(final String userid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    ChatClass ch = snapshot1.getValue(ChatClass.class);
                    if (ch.getRecever().equals(sender) && ch.getSender().equals(userid)) {
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("seen", "true");
                        snapshot1.getRef().updateChildren(map);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
