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

public class RegisterActivity extends AppCompatActivity {

    private EditText reg_email_field;
    private EditText reg_pass_field;
    private EditText reg_confirm_pass_field;
    private Button reg_button;
    private Button reg_login_button;
    private ProgressBar reg_progress;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth=FirebaseAuth.getInstance();

        reg_email_field=findViewById(R.id.reg_email);
        reg_pass_field=findViewById(R.id.reg_password);
        reg_confirm_pass_field=findViewById(R.id.reg_confirm_pass);
        reg_button=findViewById(R.id.reg_button);
        reg_login_button=findViewById(R.id.reg_login_button);
        reg_progress=findViewById(R.id.reg_progress);


        // Register Login Button signifies that You already have ana ccount So wee sedn the user back to login page.
        reg_login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();// We came here through intent and now we finish intent to go back to login page
            }
        });

        reg_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=reg_email_field.getText().toString();
                String pass=reg_pass_field.getText().toString();
                String confirm_pass=reg_confirm_pass_field.getText().toString();

                if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pass) && !TextUtils.isEmpty(confirm_pass))
                {
                    //We also need to chk if pass and confirm pass match then only we should proceed.
                    if(pass.equals(confirm_pass))
                    {
                    reg_progress.setVisibility(View.VISIBLE);
                    mAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                           if(task.isSuccessful())
                           {
                            //SendToMain();
                            //Rather than Directly send it to main we send him to creating a username.

                            Intent intent=new Intent(RegisterActivity.this,SetupActivity.class);
                            startActivity(intent);
                            finish();
                           }
                           else
                           {
                               String errorMessage=task.getException().getMessage();
                               Toast.makeText(RegisterActivity.this,"Error :"+errorMessage,Toast.LENGTH_LONG).show();
                           }
                    reg_progress.setVisibility(View.INVISIBLE);

                        }
                    });
                    }
                    else
                    {
                        Toast.makeText(RegisterActivity.this,"Error : Confirm Password and Passord Field doesnot  Match",Toast.LENGTH_LONG).show();
                    }
                }
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentuser=mAuth.getCurrentUser();
        if(currentuser!=null)
        {
        // user is logged in we want to send him to main
        
        SendToMain();    
        }
    }

    private void SendToMain() {
        Intent intent=new Intent(RegisterActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
    }
}
