package com.example.aashish.blogapp;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class BlogRecyclerAdapter extends RecyclerView.Adapter<BlogRecyclerAdapter.ViewHolder> {

    public List<Blogpost> blog_list;
    public Context context;

    public FirebaseFirestore firebaseFirestore;
    public FirebaseAuth firebaseAuth;

    public BlogRecyclerAdapter(List<Blogpost> blog_list)
    {
        this.blog_list=blog_list;
    }

    //These 3 methods are recquired for  adapter
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //We need to inflate layout with layout we have created.
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_list_item,parent,false);
        context=parent.getContext();
        firebaseFirestore=FirebaseFirestore.getInstance();
        firebaseAuth=FirebaseAuth.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        holder.setIsRecyclable(false);

        final String blogPostId=blog_list.get(position).BlogPostId;
        final String currentUserId=firebaseAuth.getCurrentUser().getUid();

        String desc_text=blog_list.get(position).getDesc();
        holder.SetText(desc_text);

        String image_url=blog_list.get(position).getImage_url();
        holder.SetBlogImage(image_url);


        String user_id=blog_list.get(position).getUser_id();
        firebaseFirestore.collection("Users").document(user_id)
                         .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if(task.isSuccessful())
                {
                holder.SetUserDescription(task.getResult().getString("name"), task.getResult().getString("image"));
                }
                else
                {
                    String error=task.getException().getMessage().toString();
                    Toast.makeText(context,"Error : "+error, Toast.LENGTH_SHORT).show();
                }
            }
        });

        long milliseconds=blog_list.get(position).getTimestamp().getTime();
        String dateString= DateFormat.format("MM /dd/yyyy",new Date(milliseconds)).toString();
        holder.SetTime(dateString);

        //Get Likes Count
        firebaseFirestore.collection("Posts/" + blogPostId + "/Likes")
                .addSnapshotListener(((MainActivity) context),new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if(!documentSnapshots.isEmpty())
                {
                    // There are some likes in here
                    int count=documentSnapshots.size();
                    holder.updateLikeCount(count);
                }
                else
                {
                // there are no likes
                holder.updateLikeCount(0);
                }
               }
           });


        //get Likes
        // we are going to check if the current user has liked the post or not.
        firebaseFirestore.collection("Posts/" + blogPostId + "/Likes")
                .document(currentUserId).addSnapshotListener(((MainActivity) context),new EventListener<DocumentSnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if(documentSnapshot.exists())
                {
                holder.blogLikeBtn.setImageDrawable(context.getDrawable(R.mipmap.action_like_accent));
                }
                else
                {
                    holder.blogLikeBtn.setImageDrawable(context.getDrawable(R.mipmap.action_like_gray));
                }
            }
        });


        // Adding Likes Feature
        holder.blogLikeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                firebaseFirestore.collection("Posts/" + blogPostId + "/Likes")
                        .document(currentUserId).get().addOnCompleteListener(((MainActivity) context),new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if(!task.getResult().exists())
                        {
                            Map<String,Object> likemap=new HashMap<>();
                            likemap.put("timestamp", FieldValue.serverTimestamp());
                            firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).set(likemap);
                        }
                        else
                        {
                            firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).delete();
                        }

                    }
                });

            }
        });

    }

    @Override
    public int getItemCount() {
        return blog_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        private  View mView;
        private TextView descView;
        private ImageView  blogImage;

        private TextView Username;
        private CircleImageView Userimage;

        private TextView Blogdate;
        private ImageView blogLikeBtn;
        private  TextView blogLikeCount;


        //constructor is recqd. for ViewHolder
        public ViewHolder(View itemView) {
            super(itemView);
            mView=itemView;

            //since we need to have onclick for our like button.
            blogLikeBtn=mView.findViewById(R.id.blog_like_button);
        }

        public void SetText(String DescText)
        {
        descView=mView.findViewById(R.id.blog_desc);
        descView.setText(DescText);
        }

        public void SetBlogImage(String downloadUri)
        {
        blogImage=mView.findViewById(R.id.blog_image);

            RequestOptions placeholderoptions=new RequestOptions();
            placeholderoptions.placeholder(R.drawable.ic_launcher_background);
        Glide.with(context).applyDefaultRequestOptions(placeholderoptions).load(downloadUri).into(blogImage);
        }



        public void SetUserDescription(String username,String UserImageurl)
        {
            Username=mView.findViewById(R.id.blog_username);
            Username.setText(username);

            Userimage=mView.findViewById(R.id.blog_user_image);

            RequestOptions placeholderoptions=new RequestOptions();
            placeholderoptions.placeholder(R.drawable.account_circle);
            Glide.with(context).applyDefaultRequestOptions(placeholderoptions).load(UserImageurl).into(Userimage);
        }

        public void SetTime(String date)
        {
           Blogdate =mView.findViewById(R.id.blog_date);
           Blogdate.setText(date);
        }

        public void updateLikeCount(int count)
        {
            blogLikeCount=mView.findViewById(R.id.blog_like_count);
            blogLikeCount.setText(count +" Likes");
        }

    }

}
