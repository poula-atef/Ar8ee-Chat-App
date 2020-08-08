package com.example.ar8ee.UI;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ar8ee.Adapters.MessageAdapter;
import com.example.ar8ee.Classes.ChatClass;
import com.example.ar8ee.Classes.UserClass;
import com.example.ar8ee.R;
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
import java.util.Timer;
import java.util.TimerTask;

import com.example.ar8ee.fragments.APIService;
import com.example.ar8ee.notification.Client;
import com.example.ar8ee.notification.Data;
import com.example.ar8ee.notification.MyResponse;
import com.example.ar8ee.notification.Sender;
import com.example.ar8ee.notification.Token;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Chat extends AppCompatActivity {
    MessageAdapter messageAdapter;
    ArrayList<ChatClass> list;
    RecyclerView recyclerView;
    CircleImageView image_on,image_off,image_ready;
    CircleImageView profile_image;
    TextView username;
    TextView typing;
    FirebaseUser firebaseUser;
    String userid;
    DatabaseReference reference;
    ImageButton send_btn;
    EditText send_edt;
    APIService apiservice;
    String msg="";
    String myName = "";
    String myImage = "";
    boolean notify = false;
    private ValueEventListener seenListener;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        if(getIntent().hasExtra("userid"))
            userid = getIntent().getStringExtra("userid");
        if(getIntent().hasExtra("myName"))
            myName = getIntent().getStringExtra("myName");


        apiservice = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        setToolBar();
        setComponents();
        getUserStatus(userid);
        refreshComponents();
        setUserImage();
        setSendButtonAction();
        setRecyclerView();
        setTypingStatue();
        getTypingStatue();
    }

    private void setRecyclerView() {
        recyclerView = (RecyclerView)findViewById(R.id.rec_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutmanager = new LinearLayoutManager(getApplicationContext());
        linearLayoutmanager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutmanager);
    }

    private void setComponents() {
        profile_image = (CircleImageView)findViewById(R.id.profile_image);
        username = (TextView)findViewById(R.id.chat_uname);
        typing = (TextView)findViewById(R.id.typing);
        send_btn = (ImageButton)findViewById(R.id.btn_send);
        send_edt = (EditText)findViewById(R.id.text_send);
        image_on = (CircleImageView) findViewById(R.id.image_on);
        image_off = (CircleImageView) findViewById(R.id.image_off);
        image_ready = (CircleImageView) findViewById(R.id.image_ready);
    }

    private void setToolBar() {
        Toolbar tool = (Toolbar)findViewById(R.id.Chat_toolBar);
        setSupportActionBar(tool);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tool.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setTypingStatue() {
        send_edt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                startTyping();
            }

            @Override
            public void afterTextChanged(Editable s) {
                TimerTask timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        endTyping();
                    }
                };
                new Timer().schedule(timerTask,3000);
            }
        });
    }

    private void getTypingStatue() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("typing").child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot data:snapshot.getChildren()){
                    String key =  data.getKey();
                    String value =  data.getValue().toString();
                    if(key.equals(firebaseUser.getUid())){
                        if(value.equals("true"))
                            typing.setText("typing..");
                        else
                            typing.setText("");
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void endTyping() {
        FirebaseDatabase.getInstance().getReference("typing")
                .child(firebaseUser.getUid()).child(userid).setValue("false");
    }

    private void startTyping() {
        FirebaseDatabase.getInstance().getReference("typing")
                .child(firebaseUser.getUid()).child(userid).setValue("true");
    }

    private void sendMessage(final String sender, final String recever, String message){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String,String>map = new HashMap<>();
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
                if(notify) {
                    sendNotification(recever, firebaseUser.getUid(), msg);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendNotification(String recever, final String sender, final String message){
        DatabaseReference dbr = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = dbr.orderByKey().equalTo(recever);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snapshot1 : snapshot.getChildren()){
                    Token token = snapshot1.getValue(Token.class);
                    Data data = new Data(firebaseUser.getUid(),myImage,
                            myName + ":" + message,myName + " is talking to you",userid);
                    Sender sender1 = new Sender(data,token.getToken());

                    apiservice.sendNotification(sender1).enqueue(new Callback<MyResponse>() {
                        @Override
                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                            if(response.code() == 200 && response.body().success != 1)
                                Toast.makeText(Chat.this, "Failed!!", Toast.LENGTH_SHORT).show();
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

    private void readMessages(final String myid, final String userid, final String imageurl){
        list = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                list.clear();
                for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                    ChatClass ch = dataSnapshot1.getValue(ChatClass.class);
                    if(ch.getSender().equals(myid) && ch.getRecever().equals(userid)
                      || ch.getSender().equals(userid) && ch.getRecever().equals(myid)){
                        list.add(ch);
                    }
                }
                messageAdapter = new MessageAdapter(Chat.this,list,imageurl,PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("mode_key",false));
                recyclerView.setAdapter(messageAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        seenMessage(userid);
}

    private void seenMessage(final String userid){
        reference = FirebaseDatabase.getInstance().getReference("Chats");

        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    ChatClass ch = snapshot.getValue(ChatClass.class);
                    if(ch.getRecever().equals(firebaseUser.getUid()) && ch.getSender().equals(userid) && InnerActivity.where){
                        HashMap<String,Object>map = new HashMap<>();
                        map.put("seen","true");
                        snapshot.getRef().updateChildren(map);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void status(String status){
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid()).child("status");
        reference.setValue(status);
    }

    private void setStatus(final String status,final String userid){
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("status");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    final HashMap<Object,Object> stat = (HashMap<Object, Object>) snapshot.getValue();
                    for(Object obj:stat.keySet()){
                        String str = obj.toString();
                        if(str.equals(userid))
                            ref.child(str).child(firebaseUser.getUid()).setValue(status);
                        else
                            ref.child(str).child(firebaseUser.getUid()).setValue("ready");
                    }
                }
                if(dataSnapshot.getValue() == null){
                    ref.child(firebaseUser.getUid())
                            .child(firebaseUser.getUid())
                            .child(firebaseUser.getUid())
                            .setValue(status);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


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
    }

    private void getUserStatus(final String userId){
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("status").child(firebaseUser.getUid());
                ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String key = snapshot.getKey().toString();
                            if(userId.equals(key)) {
                                String currentStatus = snapshot.getValue().toString();
                                if(currentStatus.equals("online")){
                                    image_on.setVisibility(View.VISIBLE);
                                    image_off.setVisibility(View.GONE);
                                    image_ready.setVisibility(View.GONE);
                                }
                                else if(currentStatus.equals("offline")){
                                    image_off.setVisibility(View.VISIBLE);
                                    image_on.setVisibility(View.GONE);
                                    image_ready.setVisibility(View.GONE);
                                }
                                else if(currentStatus.equals("ready")){
                                    image_ready.setVisibility(View.VISIBLE);
                                    image_on.setVisibility(View.GONE);
                                    image_off.setVisibility(View.GONE);
                                }
                            }
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

    private void currentUser(String user){
        SharedPreferences.Editor editor = getSharedPreferences("user",MODE_PRIVATE).edit();
        editor.putString("currentUser",user);
        editor.apply();
    }

    private void refreshComponents(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean mode = sharedPreferences.getBoolean("mode_key",false);

        if(mode){
            findViewById(R.id.chat_background).setBackgroundColor(getResources().getColor(R.color.bodyColorDark));
            findViewById(R.id.Chat_toolBar).setBackgroundColor(getResources().getColor(R.color.barColorDark));
            findViewById(R.id.bottom).setBackgroundColor(getResources().getColor(R.color.barColorDark));
            findViewById(R.id.btn_send).setBackground(getResources().getDrawable(R.drawable.send_dark_mode));
            ((EditText) findViewById(R.id.text_send)).setTextColor(getResources().getColor(R.color.bodyColorLight));
            ((EditText) findViewById(R.id.text_send)).setHintTextColor(getResources().getColor(R.color.bodyColorLight));
        }
        else{
            findViewById(R.id.chat_background).setBackgroundColor(getResources().getColor(R.color.ChatBackgroundColor));
            findViewById(R.id.Chat_toolBar).setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            findViewById(R.id.bottom).setBackgroundColor(getResources().getColor(R.color.bodyColorLight));
            findViewById(R.id.btn_send).setBackground(getResources().getDrawable(R.drawable.ic_action_name));
            ((EditText) findViewById(R.id.text_send)).setTextColor(getResources().getColor(R.color.bodyColorDark));
            ((EditText) findViewById(R.id.text_send)).setHintTextColor(getResources().getColor(R.color.hintDarkColor));
        }
    }

    private void setUserImage(){
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserClass usr = dataSnapshot.getValue(UserClass.class);
                username.setText(usr.getUsername().toString());
                if(!usr.getImageURL().equals("default"))
                    Glide.with(getApplicationContext()).load(usr.getImageURL()).into(profile_image);
                else
                    profile_image.setImageResource(R.mipmap.ic_launcher_round);

                readMessages(firebaseUser.getUid(),userid,usr.getImageURL());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setSendButtonAction(){
        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify = true;
                String content = send_edt.getText().toString();
                if(content.equals("")){
                    Toast.makeText(Chat.this, "You can't send an empty message !!", Toast.LENGTH_SHORT).show();
                }
                else {
                    msg = send_edt.getText().toString();
                    sendMessage(firebaseUser.getUid(),userid,send_edt.getText().toString());
                    send_edt.setText("");
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        InnerActivity.where = true;
        status("online");
        setStatus("online",userid);
        currentUser(userid);
    }

    @Override
    protected void onPause() {
        super.onPause();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Chats");
        ref.removeEventListener(seenListener);
        InnerActivity.where = false;
        status("offline");
        setStatus("offline",userid);
        currentUser("noUser");
    }

}
