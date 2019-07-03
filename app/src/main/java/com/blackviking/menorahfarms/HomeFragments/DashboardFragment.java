package com.blackviking.menorahfarms.HomeFragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blackviking.menorahfarms.Home;
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
public class DashboardFragment extends Fragment {

    private TextView welcome, sponsorCycle, totalReturnsText, nextEndOfCycleDate;
    private ImageView cartButton;
    private CircleImageView userAvatar;
    private RelativeLayout goToFarmstoreButton;
    private LinearLayout sponsoredFarmsLayout, farmsToWatchLayout, farmUpdatesLayout, allFarmsLayout, projectManagerLayout, faqLayout;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference userRef, sponsoredRef;
    private String currentUid;

    public DashboardFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dashboard, container, false);


        /*---   FIREBASE   ---*/
        userRef = db.getReference("Users");
        sponsoredRef = db.getReference("SponsoredFarms");
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();


        /*---   WIDGETS   ---*/
        sponsorCycle = (TextView)v.findViewById(R.id.userSponsorCycle);
        welcome = (TextView)v.findViewById(R.id.userWelcome);
        totalReturnsText = (TextView)v.findViewById(R.id.totalReturns);
        nextEndOfCycleDate = (TextView)v.findViewById(R.id.nextEndOfCycleDate);
        cartButton = (ImageView)v.findViewById(R.id.userCart);
        userAvatar = (CircleImageView)v.findViewById(R.id.userAvatar);
        goToFarmstoreButton = (RelativeLayout)v.findViewById(R.id.goToFarmstoreButton);
        sponsoredFarmsLayout = (LinearLayout)v.findViewById(R.id.sponsoredFarmsLayout);
        farmsToWatchLayout = (LinearLayout)v.findViewById(R.id.farmsToWatchLayout);
        farmUpdatesLayout = (LinearLayout)v.findViewById(R.id.farmUpdatesLayout);
        allFarmsLayout = (LinearLayout)v.findViewById(R.id.allFarmsLayout);
        projectManagerLayout = (LinearLayout)v.findViewById(R.id.projectManagerLayout);
        faqLayout = (LinearLayout)v.findViewById(R.id.faqLayout);


        /*---   TEST   ---*/
        final Home home = new Home();
        final FarmshopFragment farmshopFragment = new FarmshopFragment();


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

                                    long totalReturn = 0;
                                    int sponsorCount = (int) dataSnapshot.getChildrenCount();
                                    sponsorCycle.setText("Running Cycles : " + String.valueOf(sponsorCount));

                                    for (DataSnapshot user : dataSnapshot.getChildren()){

                                        long theReturn = Long.parseLong(user.child("sponsorReturn").getValue().toString());
                                        totalReturn = totalReturn + theReturn;

                                    }

                                    totalReturnsText.setText("₦ " + String.valueOf(totalReturn));

                                } else {

                                    sponsorCycle.setText("Running Cycles : 0");
                                    totalReturnsText.setText("₦ 0.00");

                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        }
                );

        /*---   NEXT END OF CYCLE   ---*/
        sponsoredRef.child(currentUid)
                .limitToFirst(1)
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists()) {

                                    String date = dataSnapshot.child("cycleEndDate").getValue().toString();
                                    nextEndOfCycleDate.setText(date);

                                } else {

                                    nextEndOfCycleDate.setText("Not Available");

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


        /*---   GO TO SHOP   ---*/
        goToFarmstoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        /*---   MAIN MENU   ---*/
        sponsoredFarmsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        farmsToWatchLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        farmUpdatesLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        allFarmsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        projectManagerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        faqLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        return v;
    }

}
