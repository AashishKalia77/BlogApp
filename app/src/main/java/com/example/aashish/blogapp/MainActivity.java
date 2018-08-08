package com.example.aashish.blogapp;

import android.app.Fragment;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

public class MainActivity extends AppCompatActivity {


    private FloatingActionButton addPostBtn;
    private BottomNavigationView mainBottomNav;

    private String current_userid;

    private Toolbar mainToolbar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;


    private HomeFragment homeFragment;
    private NotificationFragment notificationFragment;
    private AccountFragment accountFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        mainToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setTitle("Photo Blog");


        if (mAuth.getCurrentUser() !=null) {

            mainBottomNav = findViewById(R.id.mainBottomNav);

            // Initializing The Fragment.
            homeFragment = new HomeFragment();
            notificationFragment = new NotificationFragment();
            accountFragment = new AccountFragment();

            ReplaceFragment(homeFragment);  // We load the home frament first.

            mainBottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.bottom_action_home:
                            ReplaceFragment(homeFragment);
                            return true;
                        case R.id.bottom_action_notif:
                            ReplaceFragment(notificationFragment);
                            return true;
                        case R.id.bottom_action_account:
                            ReplaceFragment(accountFragment);
                            return true;
                        default:
                            return false;
                    }

                }
            });


            addPostBtn = findViewById(R.id.add_post_button);
            addPostBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent newPostIntent = new Intent(MainActivity.this, NewPostActivity.class);
                    startActivity(newPostIntent);
                }
            });
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser= FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser==null)
        {
         //User is not logged in we send him to Login Page
            SendToLogin();
        }
        else
        {
        current_userid=mAuth.getCurrentUser().getUid();
        firebaseFirestore.collection("Users").document(current_userid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

            if(task.isSuccessful())
            {
                 if(!task.getResult().exists())
                 {
                 //we want user on setup activity not on main activity.
                     Intent setUpintent=new Intent(MainActivity.this,SetupActivity.class);
                     startActivity(setUpintent);
                     finish();

                 }
            }
            else
            {
                String errorMessage=task.getException().getMessage();
                Toast.makeText(MainActivity.this,"MainActivityError :"+errorMessage,Toast.LENGTH_LONG).show();
            }

            }
        });
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {

            case R.id.action_logout_button:
                logout();
                return  true;

            case R.id.action_settings_button:
                Intent SettingIntent=new Intent(MainActivity.this,SetupActivity.class);
                startActivity(SettingIntent);
                return true;

            default:
                return false;
        }
    }

    private void logout() {
    // We Logout from our account and send user to Login Page
    mAuth.signOut();
    SendToLogin();
    }

    private void SendToLogin() {
        Intent intent=new Intent(MainActivity.this,LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void ReplaceFragment(android.support.v4.app.Fragment fragment)
    {
        // This will basically start a fragment Transaction.
        FragmentTransaction fragmentTransaction=getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_container,fragment); // here fragemnt---> denotes the fragment that will replace the container object
        fragmentTransaction.commit();
    }
}
