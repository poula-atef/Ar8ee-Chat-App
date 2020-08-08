package com.example.ar8ee.UI;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
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
import com.rengwuxian.materialedittext.MaterialEditText;

public class LoginActivity extends AppCompatActivity {

    MaterialEditText email,password;
    Button login;
    TextView forget_password,logo;
    boolean mode;
    FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setComponents();
        setToolBar();
        refreshComponents();
        auth = FirebaseAuth.getInstance();
        setForgetPassword();
        setLogin();

    }

    private void setLogin() {
        final Context context = this;
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String e_mail = email.getText().toString();
                String pass = password.getText().toString();

                if(TextUtils.isEmpty(e_mail))
                    Toast.makeText(LoginActivity.this,"Enter Your Email",Toast.LENGTH_SHORT).show();
                else if(TextUtils.isEmpty(pass))
                    Toast.makeText(LoginActivity.this,"Enter Your Password",Toast.LENGTH_SHORT).show();
                else
                    auth.signInWithEmailAndPassword(e_mail,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Intent i = new Intent(LoginActivity.this,InnerActivity.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
                                editor.putBoolean("startedToUse",true);
                                editor.commit();
                                startActivity(i);
                                finish();
                            }
                            else
                                Toast.makeText(LoginActivity.this,"Failed To LOGIN!!",Toast.LENGTH_SHORT).show();

                        }
                    });
            }
        });
    }

    private void setForgetPassword() {
        forget_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
            }
        });
    }

    private void refreshComponents() {
        mode = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("mode_key",false);

        if(mode){
            findViewById(R.id.login_const_layout).setBackgroundColor(getResources().getColor(R.color.bodyColorDark));
            findViewById(R.id.login_toolBar).setBackgroundColor(getResources().getColor(R.color.barColorDark));
            logo.setTextColor(getResources().getColor(R.color.bodyColorLight));
            login.setTextColor(getResources().getColor(R.color.bodyColorDark));
            login.setBackgroundColor(getResources().getColor(R.color.messageColor));
            email.setPrimaryColor(getResources().getColor(R.color.bodyColorLight));
            email.setTextColor(getResources().getColor(R.color.bodyColorLight));
            email.setHintTextColor(getResources().getColor(R.color.bodyColorLight));
            password.setPrimaryColor(getResources().getColor(R.color.bodyColorLight));
            password.setTextColor(getResources().getColor(R.color.bodyColorLight));
            password.setHintTextColor(getResources().getColor(R.color.bodyColorLight));
        }
        else{
            findViewById(R.id.login_const_layout).setBackgroundColor(getResources().getColor(R.color.bodyColorLight));
            findViewById(R.id.login_toolBar).setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            login.setTextColor(getResources().getColor(R.color.bodyColorLight));
            login.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            email.setPrimaryColor(getResources().getColor(R.color.colorPrimary));
            email.setTextColor(getResources().getColor(R.color.bodyColorDark));
            email.setHintTextColor(getResources().getColor(R.color.colorPrimary));
            password.setPrimaryColor(getResources().getColor(R.color.colorPrimary));
            password.setTextColor(getResources().getColor(R.color.bodyColorDark));
            password.setHintTextColor(getResources().getColor(R.color.colorPrimary));
        }

    }

    private void setToolBar() {
        Toolbar toolbar = findViewById(R.id.login_toolBar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.bodyColorLight));
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("LogIn");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setComponents() {
        email = (MaterialEditText)findViewById(R.id.email);
        password = (MaterialEditText)findViewById(R.id.password);
        login = (Button)findViewById(R.id.login);
        forget_password = (TextView)findViewById(R.id.forget_password);
        logo = (TextView)findViewById(R.id.logo);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();
        makeAnimation();
    }

    private void makeAnimation(){
        Animation animation = AnimationUtils.loadAnimation(this,R.anim.uptodown);
        login.startAnimation(animation);

        animation = AnimationUtils.loadAnimation(this,R.anim.bounce);
        logo.startAnimation(animation);

        animation = AnimationUtils.loadAnimation(this,R.anim.fadein);
        email.startAnimation(animation);

        animation = AnimationUtils.loadAnimation(this,R.anim.fadein);
        password.startAnimation(animation);

        animation = AnimationUtils.loadAnimation(this,R.anim.fadein);
        forget_password.startAnimation(animation);
        if(mode)
            forget_password.setTextColor(getResources().getColor(R.color.bodyColorLight));
        else
            forget_password.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
    }
}
