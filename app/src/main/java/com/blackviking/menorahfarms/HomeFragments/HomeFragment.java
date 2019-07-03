package com.blackviking.menorahfarms.HomeFragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackviking.menorahfarms.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private TextView welcome, sponsorCycle;
    private ImageView cartButton;
    private CircleImageView userAvatar;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference userRef, sponsoredRef;
    private String currentUid;
    private RecyclerView newsFeedRecycler;
    private LinearLayoutManager layoutManager;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);


        /*---   FIREBASE   ---*/
        userRef = db.getReference("Users");
        sponsoredRef = db.getReference("SponsoredFarms");
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();


        /*---   WIDGETS   ---*/
        sponsorCycle = (TextView)v.findViewById(R.id.userSponsorCycle);
        welcome = (TextView)v.findViewById(R.id.userWelcome);
        cartButton = (ImageView)v.findViewById(R.id.userCart);
        newsFeedRecycler = (RecyclerView)v.findViewById(R.id.newsFeedRecycler);
        userAvatar = (CircleImageView)v.findViewById(R.id.userAvatar);


        /*---   CURRENT USER   ---*/
        userRef.child(currentUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String userFirstName = dataSnapshot.child("firstName").getValue().toString();
                        final String profilePicture = dataSnapshot.child("profilePictureThumb").getValue().toString();

                        welcome.setText("Hi, "+userFirstName);

                        if (!profilePicture.equalsIgnoreCase("")){

                            Picasso.with(getContext())
                                    .load(profilePicture)
                                    .networkPolicy(NetworkPolicy.OFFLINE)
                                    .placeholder(R.drawable.profile)
                                    .into(userAvatar, new Callback() {
                                        @Override
                                        public void onSuccess() {

                                        }

                                        @Override
                                        public void onError() {
                                            Picasso.with(getContext())
                                                    .load(profilePicture)
                                                    .placeholder(R.drawable.profile)
                                                    .into(userAvatar);
                                        }
                                    });

                        } else {

                            userAvatar.setImageResource(R.drawable.profile);

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


        /*---   SPONSORED CYCLE   ---*/
        sponsoredRef.child(currentUid)
                .addValueEventListener(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists()){

                                    int sponsorCount = (int) dataSnapshot.getChildrenCount();
                                    sponsorCycle.setText("Running Cycles : " + String.valueOf(sponsorCount));

                                } else {

                                    sponsorCycle.setText("Running Cycles : 0");

                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        }
                );


        /*---   CART   ---*/
        cartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        loadNewsFeed();

        return v;
    }

    private void loadNewsFeed() {
    }

}
