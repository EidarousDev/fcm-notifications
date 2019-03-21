package com.eidarousdev.clashblog.notifications;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.eidarousdev.clashblog.R;
import com.eidarousdev.clashblog.Utils.ProgressCheck;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by E on 4/11/2018.
 */

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder> {

    public Context context;
    public List<NotificationsFields> notificationsFields;
    public String current_user_id, userName, userPhoto, postId;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;
    private long milliseconds;


    public NotificationsAdapter(Context context, List<NotificationsFields> notificationsFields) {

        this.context = context;
        this.notificationsFields = notificationsFields;

    }

    boolean internet_connection(){
        //Check if connected to internet, output accordingly
        ConnectivityManager cm =
                (ConnectivityManager)this.context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notifications_card, parent, false);

        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        //fix query delay in the recyclerView
        holder.setIsRecyclable(false);

        // retrieve data and set to their proper views
        final String commentText = notificationsFields.get(position).getComment_text();
        String commentUID = notificationsFields.get(position).getCommentUID();
        postId = notificationsFields.get(position).getPost_id();


        mFirestore.collection("Users").document(commentUID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    userName = task.getResult().getString("name");
                    userPhoto = task.getResult().getString("thumb_image");

                    holder.setUserData(userName, userPhoto);

                }else{

                    String errorMessage = task.getException().getMessage();
                    Toast.makeText(context, "Error: " + errorMessage, Toast.LENGTH_LONG).show();

                }
            }
        });

        // Retrieving the timestamp

        try{
            milliseconds = notificationsFields.get(position).getComment_time().getTime();
        }catch (NullPointerException e){
            Log.d("E", "Error");
        }

        String timeFormat = holder.getTimeAgo(milliseconds, context);

        //String date = DateFormat.format("dd-MM-yyyy hh:mm", milliseconds).toString();
        holder.setNotificationTime(timeFormat);

        final ProgressCheck obj = new ProgressCheck(context, postId, holder.mProgress);

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("POSTID", ""  + postId);
                obj.execute();
            }
        });


    }

    @Override
    public int getItemCount() {
        return notificationsFields.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;
        private CardView cardView;
        private TextView notificationTextView, notificationDateTextView;
        private CircleImageView userImageView;
        private ProgressBar mProgress;


        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            cardView = mView.findViewById(R.id.notificationCardView);
            notificationTextView = mView.findViewById(R.id.card_notification_desc);
            notificationDateTextView = mView.findViewById(R.id.card_notification_date);
            userImageView = mView.findViewById(R.id.notification_user_profile_pic);
            mProgress = mView.findViewById(R.id.card_notification_progress);
            mProgress.setVisibility(View.INVISIBLE);

        }

        private void setUserData(String name, String image){

            notificationTextView.setText(name + " commented on your post.");

            Glide.with(context.getApplicationContext()).load(image).into(userImageView);

        }

        private void setNotificationTime(String time){

            notificationDateTextView.setText(time);
        }

        public String getTimeAgo(long time, Context ctx) {
            if (time < 1000000000000L) {
                // if timestamp given in seconds, convert to millis
                time *= 1000;
            }

            long now = System.currentTimeMillis();
            if (time > now || time <= 0) {
                return null;
            }

            // TODO: localize
            final long diff = now - time;
            if (diff < MINUTE_MILLIS) {
                return "just now";
            } else if (diff < 2 * MINUTE_MILLIS) {
                return "a minute ago";
            } else if (diff < 50 * MINUTE_MILLIS) {
                return diff / MINUTE_MILLIS + " minutes ago";
            } else if (diff < 90 * MINUTE_MILLIS) {
                return "an hour ago";
            } else if (diff < 24 * HOUR_MILLIS) {
                return diff / HOUR_MILLIS + " hours ago";
            } else if (diff < 48 * HOUR_MILLIS) {
                return "yesterday";
            } else {
                return diff / DAY_MILLIS + " days ago";
            }
        }

    }
}
