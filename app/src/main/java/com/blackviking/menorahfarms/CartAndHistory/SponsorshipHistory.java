package com.blackviking.menorahfarms.CartAndHistory;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.Interface.ItemClickListener;
import com.blackviking.menorahfarms.Models.HistoryModel;
import com.blackviking.menorahfarms.R;
import com.blackviking.menorahfarms.ViewHolders.HistoryViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SponsorshipHistory extends AppCompatActivity {

    private ImageView backButton;
    private LinearLayout emptyLayout;
    private RecyclerView historyRecycler;
    private LinearLayoutManager layoutManager;
    private FirebaseRecyclerAdapter<HistoryModel, HistoryViewHolder> adapter;

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference historyRef;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String currentUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sponsorship_history);


        /*---   FIREBASE   ---*/
        historyRef = db.getReference("History");
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();


        /*---   WIDGETS    ---*/
        backButton = (ImageView)findViewById(R.id.backButton);
        emptyLayout = (LinearLayout)findViewById(R.id.emptyLayout);
        historyRecycler = (RecyclerView)findViewById(R.id.historyRecycler);


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        historyRef.child(currentUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()){

                            emptyLayout.setVisibility(View.GONE);
                            loadHistory();

                        } else {

                            emptyLayout.setVisibility(View.VISIBLE);

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void loadHistory() {

        historyRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        historyRecycler.setLayoutManager(layoutManager);

        adapter = new FirebaseRecyclerAdapter<HistoryModel, HistoryViewHolder>(
                HistoryModel.class,
                R.layout.history_item,
                HistoryViewHolder.class,
                historyRef.child(currentUid)
        ) {
            @Override
            protected void populateViewHolder(final HistoryViewHolder viewHolder, HistoryModel model, int position) {

                long totalToLong = Long.parseLong(model.getSponsorReturn());

                viewHolder.historyFarmType.setText(model.getSponsoredFarmType());
                viewHolder.historyRefNumber.setText(model.getSponsorRefNumber());
                viewHolder.historyFarmReturn.setText("Sponsorship Returned: " + Common.convertToPrice(SponsorshipHistory.this, totalToLong));
                viewHolder.historyStartDate.setText("From: " + model.getCycleStartDate());
                viewHolder.historyEndDate.setText("To: " + model.getCycleEndDate());


                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent historyDetailIntent = new Intent(SponsorshipHistory.this, HistoryDetails.class);
                        historyDetailIntent.putExtra("HistoryId", adapter.getRef(viewHolder.getAdapterPosition()).getKey());
                        startActivity(historyDetailIntent);
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_in);
                    }
                });
            }
        };
        historyRecycler.setAdapter(adapter);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
