package com.example.aashish.blogapp;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private RecyclerView blog_list_view;
    private List<Blogpost> blog_list;

    private FirebaseFirestore firebaseFirestore;
    private BlogRecyclerAdapter blogRecyclerAdapter;
    private FirebaseAuth firebaseAuth;

    private DocumentSnapshot lastVisible;
    private Boolean isFirstPageLoad=true; // It will be true for first time when we load the data.

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        blog_list = new ArrayList<>(); // We are going to send this list to our recylcer adapter
        blog_list_view = view.findViewById(R.id.blog_list_view);

        firebaseAuth = FirebaseAuth.getInstance();

        blogRecyclerAdapter = new BlogRecyclerAdapter(blog_list);
        blog_list_view.setLayoutManager(new LinearLayoutManager(container.getContext()));
        blog_list_view.setAdapter(blogRecyclerAdapter);

        if (firebaseAuth.getCurrentUser() != null) {

            firebaseFirestore = FirebaseFirestore.getInstance();

            blog_list_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);

                    Boolean isreachedbottom=!recyclerView.canScrollVertically(1);

                    if(isreachedbottom)
                    {
                        String desc=lastVisible.getString("desc");
                       // Toast.makeText(container.getContext(),"Reached : "+desc,Toast.LENGTH_LONG).show();

                        LoadMorePost();
                    }
                    else
                    {


                    }
                }
            });

            Query firstQuery =firebaseFirestore.collection("Posts").orderBy("timestamp", Query.Direction.DESCENDING).limit(3);
                    firstQuery.addSnapshotListener(getActivity(),new EventListener<QuerySnapshot>() {
                  /* We are passing here getactivity to associate snapshotlistener with activity
                       so that app doesnot crash.when we logout*/

                // We have retreived the data also in descending order.
                //add Snapshot listener is going to help retreive data is realtime.


                        @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                     if(isFirstPageLoad)
                     {
                     // checks if data is loaded for very frst time. or else it will perform below code as usual
                     lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                     }
                    for (DocumentChange document : documentSnapshots.getDocumentChanges()) {
                        if (document.getType() == DocumentChange.Type.ADDED) {

                            String BlogPostId= document.getDocument().getId();

                            // this below line will pass the id to model class(blogpost)
                            // using which we can retreive id from recycler adaper.
                            Blogpost blogpost = document.getDocument().toObject(Blogpost.class).withId(BlogPostId);

                            if(isFirstPageLoad)
                            {
                            blog_list.add(blogpost);
                            }
                            else
                            {
                            // this makes sure that new post is added at top of recycleer view
                            blog_list.add(0,blogpost);
                            }


                            blogRecyclerAdapter.notifyDataSetChanged();// to notify recyler view about data change.
                        }
                    }

                    isFirstPageLoad=false; // this implies our data is loaded once.

                }
            });

        }
        return view;
    }

    public void LoadMorePost()
    {
        Query nextQuery =firebaseFirestore.collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .startAfter(lastVisible)
                .limit(3);
        nextQuery.addSnapshotListener(getActivity(),new EventListener<QuerySnapshot>() {

            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if(!documentSnapshots.isEmpty()) {

                    lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                    for (DocumentChange document : documentSnapshots.getDocumentChanges()) {

                        if (document.getType() == DocumentChange.Type.ADDED) {

                            String BlogPostId= document.getDocument().getId();
                            Blogpost blogpost = document.getDocument().toObject(Blogpost.class).withId(BlogPostId);
                            blog_list.add(blogpost);

                            blogRecyclerAdapter.notifyDataSetChanged();// to notify recyler view about data change.
                        }
                    }

                }
            }
        });

    }
}
