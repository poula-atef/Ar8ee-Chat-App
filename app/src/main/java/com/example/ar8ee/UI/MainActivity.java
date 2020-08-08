package com.example.ar8ee.UI;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.example.ar8ee.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    Button login,signup;
    FirebaseUser firebaseUser;
    TextView logo;
    @Override
    protected void onStart() {
        super.onStart();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if(firebaseUser != null){
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            boolean go = sharedPreferences.getBoolean("startedToUse",false);
            if(go){
                Intent i = new Intent(MainActivity.this,InnerActivity.class);
                startActivity(i);
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        makeAnimation();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setComponents();
        refreshComponents();
        setLogin();
        setSignup();

    }

    private void setSignup() {
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RegisterActivity.class));
            }
        });
    }

    private void setLogin() {
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,LoginActivity.class));
            }
        });
    }

    private void setComponents() {
        logo = (TextView) findViewById(R.id.app_label);
        login = (Button)findViewById(R.id.login);
        signup = (Button)findViewById(R.id.signup);
    }

    private void refreshComponents() {
        boolean mode = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("mode_key",false);

        if(mode){
            login.setBackgroundColor(getResources().getColor(R.color.messageColor));
            signup.setBackgroundColor(getResources().getColor(R.color.messageColor));
            signup.setTextColor(getResources().getColor(R.color.bodyColorDark));
            login.setTextColor(getResources().getColor(R.color.bodyColorDark));
            logo.setTextColor(getResources().getColor(R.color.bodyColorLight));
            findViewById(R.id.main_layout).setBackgroundColor(getResources().getColor(R.color.bodyColorDark));
        }
        else{
            login.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            signup.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            signup.setTextColor(getResources().getColor(R.color.bodyColorLight));
            login.setTextColor(getResources().getColor(R.color.bodyColorLight));
            logo.setTextColor(getResources().getColor(R.color.colorPrimary));
            findViewById(R.id.main_layout).setBackgroundColor(getResources().getColor(R.color.bodyColorLight));
        }
    }

    private void makeAnimation(){
        Animation animation = AnimationUtils.loadAnimation(this,R.anim.bounce);
        logo.startAnimation(animation);

        animation = AnimationUtils.loadAnimation(this,R.anim.lefttoright);
        login.startAnimation(animation);

        animation = AnimationUtils.loadAnimation(this,R.anim.righttoleft);
        signup.startAnimation(animation);
    }
}
