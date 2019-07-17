package com.blackviking.menorahfarms.HomeActivities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blackviking.menorahfarms.CartAndHistory.Cart;
import com.blackviking.menorahfarms.Common.GetTimeAgo;
import com.blackviking.menorahfarms.Models.NewsModel;
import com.blackviking.menorahfarms.NewsDetail;
import com.blackviking.menorahfarms.R;
import com.blackviking.menorahfarms.ViewHolders.NewsViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
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

public class Home extends AppCompatActivity {

    private LinearLayout homeSwitch, dashboardSwitch, farmstoreSwitch, accountSwitch;
    private TextView homeText, dashboardText, farmstoreText, accountText;

    private TextView welcome, sponsorCycle;
    private ImageView cartButton;
    private CircleImageView userAvatar;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference userRef, sponsoredRef, newsFeedRef;
    private String currentUid;
    private RecyclerView newsFeedRecycler;
    private LinearLayoutManager layoutManager;
    private FirebaseRecyclerAdapter<NewsModel, NewsViewHolder> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        /*---   FIREBASE   ---*/
        userRef = db.getReference("Users");
        sponsoredRef = db.getReference("SponsoredFarms");
        newsFeedRef = db.getReference("NewsFeed");
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();


        /*---   WIDGETS   ---*/
        homeSwitch = (LinearLayout)findViewById(R.id.homeLayout);
        dashboardSwitch = (LinearLayout)findViewById(R.id.dashboardLayout);
        farmstoreSwitch = (LinearLayout)findViewById(R.id.farmShopLayout);
        accountSwitch = (LinearLayout)findViewById(R.id.accountLayout);
        homeText = (TextView)findViewById(R.id.homeText);
        dashboardText = (TextView)findViewById(R.id.dashboardText);
        farmstoreText = (TextView)findViewById(R.id.farmShopText);
        accountText = (TextView)findViewById(R.id.accountText);


        sponsorCycle = (TextView)findViewById(R.id.userSponsorCycle);
        welcome = (TextView)findViewById(R.id.userWelcome);
        cartButton = (ImageView)findViewById(R.id.userCart);
        newsFeedRecycler = (RecyclerView)findViewById(R.id.newsFeedRecycler);
        userAvatar = (CircleImageView)findViewById(R.id.userAvatar);



        /*---   CURRENT USER   ---*/
        userRef.child(currentUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String userFirstName = dataSnapshot.child("firstName").getValue().toString();
                        final String profilePicture = dataSnapshot.child("profilePictureThumb").getValue().toString();

                        welcome.setText("Hi, "+userFirstName);

                        if (!profilePicture.equalsIgnoreCase("")){

                            Picasso.get()
                                    .load(profilePicture)
                                    .networkPolicy(NetworkPolicy.OFFLINE)
                                    .placeholder(R.drawable.profile)
                                    .into(userAvatar, new Callback() {
                                        @Override
                                        public void onSuccess() {

                                        }

                                        @Override
                                        public void onError(Exception e) {
                                            Picasso.get()
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

                Intent cartIntent = new Intent(Home.this, Cart.class);
                startActivity(cartIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_in);

            }
        });

        /*---   BOTTOM NAV CONTROL   ---*/
        homeText.setTextColor(getResources().getColor(R.color.colorPrimary));
        dashboardText.setTextColor(getResources().getColor(R.color.black));
        farmstoreText.setTextColor(getResources().getColor(R.color.black));
        accountText.setTextColor(getResources().getColor(R.color.black));


        dashboardSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent dashboardIntent = new Intent(Home.this, Dashboard.class);
                startActivity(dashboardIntent);
                finish();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_in);

            }
        });
        farmstoreSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent farmstoreIntent = new Intent(Home.this, FarmShop.class);
                startActivity(farmstoreIntent);
                finish();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_in);

            }
        });
        accountSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent accountIntent = new Intent(Home.this, Account.class);
                startActivity(accountIntent);
                finish();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_in);

            }
        });


        loadNews();

    }

    private void loadNews() {

        newsFeedRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        newsFeedRecycler.setLayoutManager(layoutManager);

        adapter = new FirebaseRecyclerAdapter<NewsModel, NewsViewHolder>(
                NewsModel.class,
                R.layout.news_item,
                NewsViewHolder.class,
                newsFeedRef
        ) {
            @Override
            protected void populateViewHolder(final NewsViewHolder viewHolder, final NewsModel model, int position) {

                /*---   GET TIME AGO ALGORITHM   ---*/
                GetTimeAgo getTimeAgo = new GetTimeAgo();
                long lastTime = model.getNewsTime();
                final String lastSeenTime = getTimeAgo.getTimeAgo(lastTime, getApplicationContext());

                viewHolder.newsTitle.setText(model.getNewsTopic());
                viewHolder.newsTime.setText(lastSeenTime);

                if (!model.getNewsImageThumb().equalsIgnoreCase("")){

                    Picasso.get()
                            .load(model.getNewsImageThumb())
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.menorah_placeholder)
                            .into(viewHolder.newsImage, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError(Exception e) {
                                    Picasso.get()
                                            .load(model.getNewsImageThumb())
                                            .placeholder(R.drawable.menorah_placeholder)
                                            .into(viewHolder.newsImage);
                                }
                            });

                }

                viewHolder.newsInfoBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent newsInfoIntent = new Intent(Home.this, NewsDetail.class);
                        newsInfoIntent.putExtra("NewsId", adapter.getRef(viewHolder.getAdapterPosition()).getKey());
                        startActivity(newsInfoIntent);
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_in);
                    }
                });

            }
        };
        newsFeedRecycler.setAdapter(adapter);

    }

}
