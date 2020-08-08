package com.example.ar8ee.UI;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ar8ee.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.rengwuxian.materialedittext.MaterialEditText;

public class ResetPasswordActivity extends AppCompatActivity {
    MaterialEditText email;
    Button reset;
    TextView note;
    FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        setComponents();
        setToolBar();
        refreshComponents();
        setResetAction();

    }

    private void setResetAction() {
        firebaseAuth = FirebaseAuth.getInstance();
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String e_mail = email.getText().toString();
                if(e_mail.equals("")){
                    Toast.makeText(ResetPasswordActivity.this, "Your Email is Required !!", Toast.LENGTH_SHORT).show();
                }
                else
                    firebaseAuth.sendPasswordResetEmail(e_mail).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(ResetPasswordActivity.this,"Check your Email",Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(ResetPasswordActivity.this, LoginActivity.class));
                            }
                            else
                            {
                                Toast.makeText(ResetPasswordActivity.this,task.getException().getMessage().toString(),Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
            }
        });
    }

    private void setComponents() {
        email = (MaterialEditText)findViewById(R.id.email);
        reset = findViewById(R.id.reset);
        note = findViewById(R.id.forget_password);
    }

    private void setToolBar() {
        Toolbar toolbar = findViewById(R.id.toolbar_reset);
        toolbar.setTitleTextColor(getResources().getColor(R.color.bodyColorLight));
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Reset Password");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void refreshComponents() {
        boolean mode = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("mode_key",false);

        if(mode){
            findViewById(R.id.toolbar_reset).setBackgroundColor(getResources().getColor(R.color.barColorDark));
            findViewById(R.id.reset_const_layout).setBackgroundColor(getResources().getColor(R.color.bodyColorDark));
            email.setTextColor(getResources().getColor(R.color.bodyColorLight));
            email.setHintTextColor(getResources().getColor(R.color.bodyColorLight));
            email.setPrimaryColor(getResources().getColor(R.color.bodyColorLight));
            reset.setBackgroundColor(getResources().getColor(R.color.messageColor));
            reset.setTextColor(getResources().getColor(R.color.bodyColorDark));
            note.setTextColor(getResources().getColor(R.color.bodyColorLight));
        }
        else{
            findViewById(R.id.toolbar_reset).setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            findViewById(R.id.reset_const_layout).setBackgroundColor(getResources().getColor(R.color.bodyColorLight));
            email.setTextColor(getResources().getColor(R.color.bodyColorDark));
            email.setHintTextColor(getResources().getColor(R.color.colorPrimary));
            email.setPrimaryColor(getResources().getColor(R.color.colorPrimary));
            reset.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            reset.setTextColor(getResources().getColor(R.color.bodyColorLight));
            note.setTextColor(getResources().getColor(R.color.bodyColorDark));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        makeAnimation();
    }

    private void makeAnimation(){
        Animation animation = AnimationUtils.loadAnimation(this,R.anim.fadein);
        email.startAnimation(animation);
        animation = AnimationUtils.loadAnimation(this,R.anim.uptodown);
        reset.startAnimation(animation);
        animation = AnimationUtils.loadAnimation(this,R.anim.uptodown);
        note.startAnimation(animation);
    }
}
