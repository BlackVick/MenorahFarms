package com.blackviking.menorahfarms;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blackviking.menorahfarms.Common.GetTimeAgo;
import com.blackviking.menorahfarms.Models.NewsModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class NewsDetail extends AppCompatActivity {

    private ImageView backButton, shareButton, newsDetailImage;
    private TextView newsDetailTitle, newDetailTime, newDetailCreator, newsDetailContent;

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference newsFeedRef;
    private String currentNewsId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);


        /*---   INTENT DATA   ---*/
        currentNewsId = getIntent().getStringExtra("NewsId");


        /*---   FIREBASE   ---*/
        newsFeedRef = db.getReference("NewsFeed");


        /*---   WIDGETS   ---*/
        backButton = (ImageView)findViewById(R.id.backButton);
        shareButton = (ImageView)findViewById(R.id.shareButton);
        newsDetailImage = (ImageView)findViewById(R.id.newsDetailImage);
        newsDetailTitle = (TextView)findViewById(R.id.newsDetailTitle);
        newDetailTime = (TextView)findViewById(R.id.newDetailTime);
        newDetailCreator = (TextView)findViewById(R.id.newDetailCreator);
        newsDetailContent = (TextView)findViewById(R.id.newsDetailContent);


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        loadCurrentFeed();

    }

    private void loadCurrentFeed() {

        newsFeedRef.child(currentNewsId)
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                final NewsModel currentNews = dataSnapshot.getValue(NewsModel.class);

                                /*---   GET TIME AGO ALGORITHM   ---*/
                                GetTimeAgo getTimeAgo = new GetTimeAgo();
                                long lastTime = currentNews.getNewsTime();
                                final String lastSeenTime = getTimeAgo.getTimeAgo(lastTime, getApplicationContext());


                                newsDetailTitle.setText(currentNews.getNewsTopic());
                                newsDetailContent.setText(currentNews.getNewsContent());
                                newDetailTime.setText(lastSeenTime);
                                newDetailCreator.setText(currentNews.getNewsCreator());

                                if (!currentNews.getNewsImage().equalsIgnoreCase("")){

                                    Picasso.get()
                                            .load(currentNews.getNewsImage())
                                            .networkPolicy(NetworkPolicy.OFFLINE)
                                            .placeholder(R.drawable.menorah_placeholder)
                                            .into(newsDetailImage, new Callback() {
                                                @Override
                                                public void onSuccess() {

                                                }

                                                @Override
                                                public void onError(Exception e) {
                                                    Picasso.get()
                                                            .load(currentNews.getNewsImage())
                                                            .networkPolicy(NetworkPolicy.OFFLINE)
                                                            .placeholder(R.drawable.menorah_placeholder)
                                                            .into(newsDetailImage);
                                                }
                                            });

                                }

                                shareButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        if (!currentNews.getNewsLink().equalsIgnoreCase("")) {

                                            Intent i = new Intent(android.content.Intent.ACTION_SEND);
                                            i.setType("text/plain");
                                            i.putExtra(android.content.Intent.EXTRA_SUBJECT, "Menorah Article Share");
                                            i.putExtra(android.content.Intent.EXTRA_TEXT, "Get Updated with the Menorah Monthly Farm Articles. \nRead more here >>  " + currentNews.getNewsLink());
                                            startActivity(Intent.createChooser(i, "Share via"));

                                        } else {

                                            Toast.makeText(NewsDetail.this, "News has no link yet, try again later", Toast.LENGTH_SHORT).show();

                                        }

                                    }
                                });

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        }
                );

    }
}
