 package com.example.aashish.blogapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

 public class SetupActivity extends AppCompatActivity {


     private Uri mainImageUri=null;
     private CircleImageView setUpImage;
     private EditText SetupName;
     private Button SetupButton;
     private ProgressBar setup_progress;
     private String user_id;
     private boolean Ischanged=false;


     private StorageReference storageReference;
     private FirebaseAuth firebaseAuth; // uSed to get user id bcz image will be stored along with name of user.
     private FirebaseFirestore firebaseFirestore;
     @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);


        Toolbar setUpToolbar=findViewById(R.id.setup_toolbar);
         setSupportActionBar(setUpToolbar);
         getSupportActionBar().setTitle("Account Settings");


         firebaseAuth=FirebaseAuth.getInstance();
         user_id=firebaseAuth.getCurrentUser().getUid();


         storageReference= FirebaseStorage.getInstance().getReference();
         firebaseFirestore=FirebaseFirestore.getInstance();

        setUpImage=findViewById(R.id.set_up_image);
        SetupName=findViewById(R.id.setup_name);
        SetupButton=findViewById(R.id.setup_button);
        setup_progress=findViewById(R.id.setup_progress);

        setup_progress.setVisibility(View.VISIBLE);
        SetupButton.setEnabled(false);
        /* To see if the image and account name exists
            that is we chk if the data exists or not.
        */

        firebaseFirestore.collection("Users").document(user_id).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful())
                {
                   // Toast.makeText(SetupActivity.this,"hello",Toast.LENGTH_LONG).show();
                    if(task.getResult().exists())
                    {
                    // This implies that current user exists.
                    Toast.makeText(SetupActivity.this,"Data Exists",Toast.LENGTH_LONG).show();
                    String name=task.getResult().getString("name");
                    String image=task.getResult().getString("image");

                    mainImageUri= Uri.parse(image);

                    SetupName.setText(name);

                    RequestOptions placeholderrequest=new RequestOptions();
                    placeholderrequest.placeholder(R.drawable.action_search);
                    Glide.with(SetupActivity.this).setDefaultRequestOptions(placeholderrequest).load(image).into(setUpImage);
                    }
                    else
                    {
                        Toast.makeText(SetupActivity.this,"Data doesnot exist",Toast.LENGTH_LONG).show();
                    }

                }
                else
                {
                    String error=task.getException().getMessage();
                    Toast.makeText(SetupActivity.this,"FireStore retreival Error"+error,Toast.LENGTH_LONG).show();
                }
                        setup_progress.setVisibility(View.INVISIBLE);
                        SetupButton.setEnabled(true);
            }
        });








        SetupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String user_name = SetupName.getText().toString();
                if(Ischanged)
                {
                    // If the image is changed then will store that.
                    if (!TextUtils.isEmpty(user_name) && mainImageUri != null) {
                        setup_progress.setVisibility(View.VISIBLE);
                        StorageReference image_path = storageReference.child("Profile_Image")
                                                                      .child(user_id + ".jpg");


                        //in next step we store file path uri
                        image_path.putFile(mainImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful())
                                {
                                /*
                                Till now We have stored  only Images in Our Firebase Storage.
                                Now we need to store the image along with username in firebase database.
                                 */
                                storeFirestore(task, user_name);
                                }
                                else
                                {
                                String error = task.getException().getMessage();
                                Toast.makeText(SetupActivity.this, "Image Error :" + error, Toast.LENGTH_LONG).show();
                                setup_progress.setVisibility(View.INVISIBLE);
                                }

                            }
                        });

                    }
                }
                else
                {
                // if the image was not changed wee pass null to task.
                storeFirestore(null,user_name);

                }

            }

            private void storeFirestore(@NonNull Task<UploadTask.TaskSnapshot> task,String user_name) {
                Uri download_uri;
                //Toast.makeText(SetupActivity.this,"The  Image is Uploaded",Toast.LENGTH_LONG).show();
                if(task!=null)
                {
                // Image was chnaged we get the download uri
                download_uri=task.getResult().getDownloadUrl();
                }
                else
                {
                // We get the orignal image uri. bcz image was not changed.
                download_uri=mainImageUri;
                }

                Map<String,String> usermap=new HashMap<>();
                usermap.put("name",user_name);
                usermap.put("image",download_uri.toString());

                firebaseFirestore.collection("Users").document(user_id).set(usermap)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful())
                                {
                                    Toast.makeText(SetupActivity.this,"User Settings are Updated",Toast.LENGTH_LONG).show();
                                    // We don't want user to be here anymore.
                                    Intent intent=new Intent(SetupActivity.this,MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                                else
                                {
                                    String errorMessage=task.getException().getMessage();
                                    Toast.makeText(SetupActivity.this,"FireStore Error :"+errorMessage,Toast.LENGTH_LONG).show();
                                }
                                setup_progress.setVisibility(View.INVISIBLE);
                            }
                        });

            }
        });






        setUpImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            // We check for perissions whether the user is running above Marshmallow or not.
                if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.M)
                {
                   // chk if permissionis granted or not.
                   if(ContextCompat.checkSelfPermission(SetupActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
                   {
                       Toast.makeText(SetupActivity.this,"Permission Denied",Toast.LENGTH_LONG).show();
                       ActivityCompat.requestPermissions(SetupActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                   }
                   else
                   {
                       //Toast.makeText(SetupActivity.this,"You Already have Permissions",Toast.LENGTH_LONG).show();


                       //this will send user to crop activity either from camera or gallery
                     BringImagePicker();
                   }
                }

                else
                {
                // For Permissions below marsha,ellow we dont need runtime permissions.
                BringImagePicker();
                }
            }
        });

    }


     private void BringImagePicker() {
         CropImage.activity()
                 .setGuidelines(CropImageView.Guidelines.ON)
                 .setAspectRatio(1,1)
                 .start(SetupActivity.this);
     }

     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);

         if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
             CropImage.ActivityResult result = CropImage.getActivityResult(data);
             if (resultCode == RESULT_OK)
             {
                 mainImageUri  = result.getUri();//This will store crop image Uri
                 setUpImage.setImageURI(mainImageUri);
                 Ischanged=true;
             }
             else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
             {
                 Exception error = result.getError();
             }
         }
     }
 }
