package com.example.ar8ee.UI;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ar8ee.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    MaterialEditText username,email,password;
    Button signup;
    TextView logo;
    FirebaseAuth auth;
    DatabaseReference reference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        setComponents();
        setToolBar();
        refreshComponents();
        auth = FirebaseAuth.getInstance();
        setSignupAction();
    }

    private void setSignupAction() {
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = username.getText().toString();
                String e_mail = email.getText().toString();
                String pass = password.getText().toString();

                if(TextUtils.isEmpty(user) || TextUtils.isEmpty(e_mail) ||TextUtils.isEmpty(pass))
                    Toast.makeText(RegisterActivity.this,"All Fields Are Required",Toast.LENGTH_SHORT).show();

                else if(pass.length() < 6)
                    Toast.makeText(RegisterActivity.this,"Password Should Be At Least 6 Characters",Toast.LENGTH_SHORT).show();

                else
                    register(user,e_mail,pass);

            }
        });
    }

    private void refreshComponents() {
        boolean mode = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("mode_key",false);

        if(mode){
            username.setTextColor(getResources().getColor(R.color.bodyColorLight));
            username.setPrimaryColor(getResources().getColor(R.color.bodyColorLight));
            username.setHintTextColor(getResources().getColor(R.color.bodyColorLight));
            email.setTextColor(getResources().getColor(R.color.bodyColorLight));
            email.setHintTextColor(getResources().getColor(R.color.bodyColorLight));
            email.setPrimaryColor(getResources().getColor(R.color.bodyColorLight));
            password.setTextColor(getResources().getColor(R.color.bodyColorLight));
            password.setHintTextColor(getResources().getColor(R.color.bodyColorLight));
            password.setPrimaryColor(getResources().getColor(R.color.bodyColorLight));
            signup.setTextColor(getResources().getColor(R.color.bodyColorDark));
            signup.setBackgroundColor(getResources().getColor(R.color.messageColor));
            logo.setTextColor(getResources().getColor(R.color.bodyColorLight));
            findViewById(R.id.register_toolBar).setBackgroundColor(getResources().getColor(R.color.barColorDark));
            findViewById(R.id.const_register_layout).setBackgroundColor(getResources().getColor(R.color.bodyColorDark));

        }
        else{
            username.setTextColor(getResources().getColor(R.color.bodyColorDark));
            username.setHintTextColor(getResources().getColor(R.color.colorPrimary));
            username.setPrimaryColor(getResources().getColor(R.color.colorPrimary));
            email.setTextColor(getResources().getColor(R.color.bodyColorDark));
            email.setHintTextColor(getResources().getColor(R.color.colorPrimary));
            email.setPrimaryColor(getResources().getColor(R.color.colorPrimary));
            password.setTextColor(getResources().getColor(R.color.bodyColorDark));
            password.setHintTextColor(getResources().getColor(R.color.colorPrimary));
            password.setPrimaryColor(getResources().getColor(R.color.colorPrimary));
            signup.setTextColor(getResources().getColor(R.color.bodyColorLight));
            signup.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            logo.setTextColor(getResources().getColor(R.color.colorPrimary));
            findViewById(R.id.register_toolBar).setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }
    }

    private void setToolBar() {
        Toolbar toolbar = findViewById(R.id.register_toolBar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.bodyColorLight));
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("SignUp");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setComponents() {
        username = (MaterialEditText)findViewById(R.id.username);
        email = (MaterialEditText)findViewById(R.id.email);
        password = (MaterialEditText)findViewById(R.id.password);
        signup = (Button)findViewById(R.id.login);
        logo = (TextView)findViewById(R.id.logo);
    }

    @Override
    protected void onResume() {
        super.onResume();
        makeAnimation();
    }

    private void register(final String username , String email, String password){
        auth.createUserWithEmailAndPassword(email,password).
                addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser firebaseuser = auth.getCurrentUser();
                            assert firebaseuser != null;
                            String id = firebaseuser.getUid();

                            reference = FirebaseDatabase.getInstance().getReference("Users").child(id);

                            HashMap<String,String>map = new HashMap<>();
                            map.put("id",id);
                            map.put("username",username);
                            map.put("imageURL","default");
                            map.put("status","offline");
                            map.put("search",username.toLowerCase());

                            reference.setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Intent i = new Intent(RegisterActivity.this, InnerActivity.class);
                                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(i);
                                        finish();
                                    }
                                }
                            });
                        }
                        else
                            Toast.makeText(RegisterActivity.this,"Can't SignUp With That Email !!",Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void makeAnimation(){
        Animation animation = AnimationUtils.loadAnimation(this,R.anim.uptodown);
        signup.startAnimation(animation);

        animation = AnimationUtils.loadAnimation(this,R.anim.bounce);
        logo.startAnimation(animation);

        animation = AnimationUtils.loadAnimation(this,R.anim.fadein);
        username.startAnimation(animation);

        animation = AnimationUtils.loadAnimation(this,R.anim.fadein);
        email.startAnimation(animation);

        animation = AnimationUtils.loadAnimation(this,R.anim.fadein);
        password.startAnimation(animation);
    }
}
