package com.example.aashish.blogapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class NewPostActivity extends AppCompatActivity {

    private static final int MAX_LENGTH =100 ;
    private Toolbar newPostToolbar;

    private ImageView newPostImage;
    private EditText newPostDesc;
    private Button newPostBtn;
    private ProgressBar newPostProgress;

    private Uri post_image_uri=null;

    private StorageReference storageReference;// for Storage
    private FirebaseFirestore firebaseFirestore;// For  Database.
    private FirebaseAuth firebaseAuth;

    private String current_user_id;

    private Bitmap compressedImageFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        storageReference= FirebaseStorage.getInstance().getReference();
        firebaseFirestore=FirebaseFirestore.getInstance();
        firebaseAuth=FirebaseAuth.getInstance();

        current_user_id=firebaseAuth.getCurrentUser().getUid();

        newPostToolbar=findViewById(R.id.new_post_toolbar);
        setSupportActionBar(newPostToolbar);
        getSupportActionBar().setTitle("Add New Post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        newPostImage=findViewById(R.id.new_post_image);
        newPostDesc=findViewById(R.id.new_post_desc);
        newPostBtn=findViewById(R.id.post_button);
        newPostProgress=findViewById(R.id.new_post_progress);

        newPostProgress.setVisibility(View.INVISIBLE);

        newPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512,512)
                        .setAspectRatio(1,1)
                        .start(NewPostActivity.this);
            }
        });


        newPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String desc=newPostDesc.getText().toString();
                if(!TextUtils.isEmpty(desc) && post_image_uri!=null)
                {
                 // We check there is image and description.
                 // Further We show the progressbar.
                 newPostProgress.setVisibility(View.VISIBLE);
                 //Now we will start Uploading Image to storage.

                  final String randomName= UUID.randomUUID().toString();
                  // We define the path of our images to be posted.
                    StorageReference filepath=storageReference.child("Post_Images").child(randomName + ".jpg");
                    filepath.putFile(post_image_uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {

                            final String download_uri=task.getResult().getDownloadUrl().toString();

                            if(task.isSuccessful())
                            {

                            /*
                                Step 1- Till now We have stored  only Images in Our Firebase Storage.
                                Now we need to store the image along with username in firebase database.
                             */

                            /*
                             Step-2 We will try to post thumbnail  of our image to store a low quality image also
                            */
                            File newImageFile=new File(post_image_uri.getPath());

                                try {

                                    compressedImageFile = new Compressor(NewPostActivity.this)
                                            .setMaxHeight(2100)
                                            .setMaxWidth(100)
                                            .setQuality(2)
                                            .compressToBitmap(newImageFile);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                byte[] thumbdata = baos.toByteArray();

                               UploadTask uploadTask=storageReference.child("post_images/thums").child(randomName+" .jpg").putBytes(thumbdata);

                               uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                   @Override
                                   public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                       //

                                       String downloadthumbUri=task.getResult().getDownloadUrl().toString();

                                       Map<String,Object> postMap=new HashMap<>();
                                       postMap.put("image_url",download_uri);
                                       postMap.put("image_thumb",downloadthumbUri);
                                       postMap.put("desc",desc);
                                       postMap.put("user_id", current_user_id);
                                       postMap.put("timestamp",FieldValue.serverTimestamp());

                                       firebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                           @Override
                                           public void onComplete(@NonNull Task<DocumentReference> task) {
                                               if(task.isSuccessful())
                                               {

                                                   Toast.makeText(NewPostActivity.this,"Post was added",Toast.LENGTH_LONG).show();
                                                   Intent mainIntent=new Intent(NewPostActivity.this,MainActivity.class);
                                                   startActivity(mainIntent);
                                                   finish();// We dont want user to be here now
                                               }
                                               else
                                               {
                                                   String errorMessage=task.getException().getMessage();
                                                   Toast.makeText(NewPostActivity.this,"FireStore Error :"+errorMessage,Toast.LENGTH_LONG).show();
                                               }
                                               newPostProgress.setVisibility(View.INVISIBLE);

                                           }
                                       });
                                   }
                               }).addOnFailureListener(new OnFailureListener() {
                                   @Override
                                   public void onFailure(@NonNull Exception e) {
                                    // Error handling
                                   }
                               });



                            }
                            else
                            {
                            newPostProgress.setVisibility(View.INVISIBLE);
                            }
                        }
                    });


                }
            }
        });
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK)
            {

                post_image_uri=result.getUri();
                newPostImage.setImageURI(post_image_uri);

            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
            {
                Exception error = result.getError();
            }
        }
    }


}
