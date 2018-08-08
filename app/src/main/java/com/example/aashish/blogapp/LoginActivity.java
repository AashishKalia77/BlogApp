package com.example.aashish.blogapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText loginEmialtext;
    private EditText loginPasstext;
    private Button loginBtn;
    private Button loginRegBtn;
    private ProgressBar loginProgress;

    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth=FirebaseAuth.getInstance();

        loginEmialtext=findViewById(R.id.Login_Email);
        loginPasstext=findViewById(R.id.Login_Pass);
        loginBtn=findViewById(R.id.login_button);
        loginRegBtn=findViewById(R.id.Login_reg_button);
        loginProgress=findViewById(R.id.Login_progress);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            String loginEmail=loginEmialtext.getText().toString();
            String loginPass=loginPasstext.getText().toString();

            if(!TextUtils.isEmpty(loginEmail) && !TextUtils.isEmpty(loginPass))
            {
                loginProgress.setVisibility(View.VISIBLE);
                mAuth.signInWithEmailAndPassword(loginEmail,loginPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful())
                        {
                        sendToMain();
                        }
                        else
                        {
                        String errorMessage=task.getException().getMessage();
                        Toast.makeText(LoginActivity.this,"Error :"+errorMessage,Toast.LENGTH_LONG).show();

                        }
                        loginProgress.setVisibility(View.INVISIBLE);

                    }
                });

            }


            }
        });



        loginRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser=mAuth.getCurrentUser();
        if(currentUser!=null)
        {
         //implies user is logged  in we don't need him in this activity
         // we send the user back to mainActicity.
            sendToMain();

        }
    }

    private void sendToMain() {
        Intent intent=new Intent(LoginActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
    }
}
