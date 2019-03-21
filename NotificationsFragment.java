package com.eidarousdev.clashblog.notifications;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.eidarousdev.clashblog.R;
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
public class NotificationsFragment extends Fragment {

    String from_name, from_message, current_user_id;
    TextView textView;

    private ProgressBar mProgress;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private RecyclerView notificationsRecyclerView;
    private List<NotificationsFields> notificationsList;
    private NotificationsAdapter notificationsAdapter;
    private Boolean isFirstTimeLoad = true;
    private DocumentSnapshot lastVisible; // a member variable that stores the last post visible at the current page query

    public NotificationsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        textView = view.findViewById(R.id.notifications_textView);
        mProgress = view.findViewById(R.id.notifications_progress);
        notificationsList = new ArrayList<>();
        notificationsRecyclerView = view.findViewById(R.id.notifications_recycler_view);
        notificationsAdapter = new NotificationsAdapter(container.getContext(), notificationsList);
        notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        notificationsRecyclerView.setHasFixedSize(true);
        notificationsRecyclerView.setAdapter(notificationsAdapter);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        current_user_id = mAuth.getCurrentUser().getUid();

        notificationsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                Boolean isPostsLimit = !recyclerView.canScrollVertically(1);

                if (isPostsLimit){
//
//                        String desc = lastVisible.getString("description");
//                        Toast.makeText(container.getContext(), "Reached: " + desc, Toast.LENGTH_LONG).show();

                    queryOlderNotifications();

                }
            }
        });

        Query notificationsQuery = mFirestore.collection("Users").document(current_user_id).collection("Notifications")
                .orderBy("comment_time", Query.Direction.DESCENDING)
                .limit(10);
        notificationsQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                try {
                    if (!documentSnapshots.isEmpty()) {

                        mProgress.setVisibility(View.VISIBLE);
                        if (isFirstTimeLoad) {
                            /* Check if the data is loaded for the very first time
                            * Only then we change last visible to the last visible post in page 1
                            * Otherwise, do NOT change the lastVisible, so if a new post is added say by another user
                            * it will be sent to this first query not the other query at the bottom, hence the post
                            * shall appear at the top and the lastVisible won't be re-stored for the second time,
                            * avoiding posts repetition.
                            * */
                            lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1); // get the last post

                        }
                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                            if (doc.getType() == DocumentChange.Type.ADDED) {

                                    /*
                                    * Save the current post id in a variable
                                    * then we will store this variable in a separate extending class
                                    * to retrieve it later in the adapter
                                    */

                                NotificationsFields notificationsFields = doc.getDocument().toObject(NotificationsFields.class);

                                if (isFirstTimeLoad) {
                                    //Log.d("post", "" + postFields);
                                    notificationsList.add(notificationsFields);

                                }else{
                                        /*
                                        * if this is the first time load, just add the posts as a list
                                        * else, if a post was published by another user, add that post
                                        * to position 0 (On top of other posts)
                                        * */
//                                        Animation fadeIn = new AlphaAnimation(0, 1);
//                                        fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
//                                        fadeIn.setDuration(1000);
//                                        AnimationSet animation = new AnimationSet(false); //change to false
//                                        animation.addAnimation(fadeIn);
//                                        container.setAnimation(animation);
                                    //Log.d("post", "" + postFields);
                                    notificationsList.add(0, notificationsFields);
                                }
                                notificationsAdapter.notifyDataSetChanged();

                            }
                        }

                        isFirstTimeLoad = false;

                    }else{

                        textView.setText("You don't have any notifications yet!");

                    }
                }catch (NullPointerException ex){

                    Toast.makeText(container.getContext(), "Error: " + ex, Toast.LENGTH_LONG).show();

                }
                mProgress.setVisibility(View.INVISIBLE);
            }
        });
        // Inflate the layout for this fragment




//        Intent intent = getActivity().getIntent();
//        if(intent.hasExtra("from_user_id") &&
//                intent.hasExtra("message")){
//            from_name = intent.getExtras().getString("from_user_id");
//            from_message = intent.getExtras().getString("message");
//            textView.setText("From: " + from_name + "\nMessage: " + from_message);
//
//        }else{
//            textView.setText("You don't have any notifications yet!");
//        }


        // Inflate the layout for this fragment
        return view;

    }

    private void queryOlderNotifications() {
        Query notificationsQuery = mFirestore.collection("Users").document(current_user_id).collection("Notifications")
                .orderBy("comment_time", Query.Direction.DESCENDING)
                .startAfter(lastVisible)
                .limit(10);
        notificationsQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                try{
                    if (!documentSnapshots.isEmpty()) {

                        mProgress.setVisibility(View.VISIBLE);

                        lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1); // get the last post

                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                            if (doc.getType() == DocumentChange.Type.ADDED) {

                            /*
                            * Save the current post id in a variable
                            * then we will store this variable in a separate extending class
                            * to retrieve it later in the adapter
                            */
                                NotificationsFields notificationsFields = doc.getDocument().toObject(NotificationsFields.class);
                                notificationsList.add(notificationsFields);

                                notificationsAdapter.notifyDataSetChanged();

                            }
                        }
                    }
                }catch(NullPointerException exception){

                    textView.setText("You don't have any notifications yet.");

                }
                mProgress.setVisibility(View.INVISIBLE);
            }
        });
    }

}
