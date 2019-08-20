package com.blackviking.menorahfarms.CartAndHistory;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.HomeActivities.Dashboard;
import com.blackviking.menorahfarms.Models.HistoryModel;
import com.blackviking.menorahfarms.Models.SponsoredFarmModel;
import com.blackviking.menorahfarms.R;
import com.blackviking.menorahfarms.Services.CheckForSponsorship;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * A simple {@link Fragment} subclass.
 */
public class HistorySummary extends Fragment {

    private TextView summaryReturns, summaryCycleEnd, summarySponsored, summaryCollected;

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference historyRef, sponsoredFarmRef;

    private String currentUid;

    public HistorySummary() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_history_summary, container, false);


        /*---   FIREBASE   ---*/
        historyRef = db.getReference("History");
        sponsoredFarmRef = db.getReference("SponsoredFarms");
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();


        /*---   WIDGETS   ---*/
        summaryReturns = (TextView)v.findViewById(R.id.summaryReturns);
        summaryCycleEnd = (TextView)v.findViewById(R.id.summaryCycleEnd);
        summarySponsored = (TextView)v.findViewById(R.id.summarySponsored);
        summaryCollected = (TextView)v.findViewById(R.id.summaryCollected);


        loadSummary();

        return v;
    }

    private void loadSummary() {

        /*---   NEXT END OF CYCLE   ---*/
        sponsoredFarmRef.child(currentUid)
                .limitToFirst(1)
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists()) {

                                    for (DataSnapshot snap : dataSnapshot.getChildren()){

                                        SponsoredFarmModel currentSponsor = snap.getValue(SponsoredFarmModel.class);

                                        if (currentSponsor != null) {
                                            summaryCycleEnd.setText(currentSponsor.getCycleEndDate());
                                        }

                                    }

                                } else {

                                    summaryCycleEnd.setText("No current sponsorships");

                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        }
                );


        /*---   SPONSORED TOTAL RETURN   ---*/
        sponsoredFarmRef.child(currentUid)
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists()){

                                    long totalReturn = 0;

                                    for (DataSnapshot user : dataSnapshot.getChildren()){

                                        SponsoredFarmModel currentSponsoredTotal = user.getValue(SponsoredFarmModel.class);

                                        if (currentSponsoredTotal != null) {

                                            long theReturn = Long.parseLong(currentSponsoredTotal.getSponsorReturn());
                                            totalReturn = totalReturn + theReturn;

                                        }

                                    }

                                    summaryReturns.setText(Common.convertToPrice(getContext(), totalReturn));

                                } else {

                                    summaryReturns.setText("No current sponsorships");

                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        }
                );


        historyRef.child(currentUid)
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists()) {

                                    int historyCount = (int) dataSnapshot.getChildrenCount();

                                    summarySponsored.setText(String.valueOf(historyCount));

                                    long totalCollected = 0;

                                    for (DataSnapshot user : dataSnapshot.getChildren()){

                                        HistoryModel currentHistory = user.getValue(HistoryModel.class);

                                        if (currentHistory != null) {

                                            long theReturn = Long.parseLong(currentHistory.getSponsorReturn());
                                            totalCollected = totalCollected + theReturn;

                                        }

                                    }

                                    summaryCollected.setText(Common.convertToPrice(getContext(), totalCollected));

                                } else {

                                    summarySponsored.setText("0");
                                    summaryCollected.setText(Common.convertToPrice(getContext(), 0));

                                }


                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        }
                );
    }

}
