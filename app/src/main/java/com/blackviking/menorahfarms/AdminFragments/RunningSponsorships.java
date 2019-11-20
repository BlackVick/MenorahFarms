package com.blackviking.menorahfarms.AdminFragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.blackviking.menorahfarms.Common.CheckInternet;
import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.Interface.ItemClickListener;
import com.blackviking.menorahfarms.Models.RunningCycleModel;
import com.blackviking.menorahfarms.Models.UserModel;
import com.blackviking.menorahfarms.R;
import com.blackviking.menorahfarms.ViewHolders.AdminViewHolder;
import com.blackviking.menorahfarms.ViewHolders.RunningCycleViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class RunningSponsorships extends Fragment {

    private RecyclerView runningCycleRecycler;
    private LinearLayoutManager layoutManager;
    private FirebaseRecyclerAdapter<RunningCycleModel, RunningCycleViewHolder> adapter;
    private FirebaseRecyclerAdapter<RunningCycleModel, AdminViewHolder> adapter2;

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference userRef, sponsorshipRef, adminSponsorshipRef;

    private RelativeLayout noInternetLayout, controlLayout;
    private LinearLayout emptyLayout;
    private ImageView backButton;
    private EditText searchText;
    
    //classification
    private boolean isList = false;
    private boolean isExist = false;
    private ArrayList<RunningCycleModel> arrayList;


    public RunningSponsorships() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_running_sponsorships, container, false);


        /*---   FIREBASE   ---*/
        userRef = db.getReference("Users");
        sponsorshipRef = db.getReference("SponsoredFarms");
        adminSponsorshipRef = db.getReference("RunningCycles");


        /*---   WIDGETS   ---*/
        runningCycleRecycler = (RecyclerView)v.findViewById(R.id.runningCycleRecycler);
        noInternetLayout = v.findViewById(R.id.noInternetLayout);
        emptyLayout = v.findViewById(R.id.emptyLayout);
        controlLayout = v.findViewById(R.id.controlLayout);
        backButton = v.findViewById(R.id.backButton);
        searchText = v.findViewById(R.id.searchText);


        //execute network check async task
        CheckInternet asyncTask = (CheckInternet) new CheckInternet(getContext(), new CheckInternet.AsyncResponse(){
            @Override
            public void processFinish(Integer output) {

                //check all cases
                if (output == 1){

                    adminSponsorshipRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if (dataSnapshot.exists()){

                                noInternetLayout.setVisibility(View.GONE);
                                emptyLayout.setVisibility(View.GONE);
                                runningCycleRecycler.setVisibility(View.VISIBLE);
                                loadSponsorshipGroup();

                            } else {

                                noInternetLayout.setVisibility(View.GONE);
                                emptyLayout.setVisibility(View.VISIBLE);
                                runningCycleRecycler.setVisibility(View.GONE);

                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                } else

                if (output == 0){

                    //set layout
                    noInternetLayout.setVisibility(View.VISIBLE);
                    emptyLayout.setVisibility(View.GONE);
                    runningCycleRecycler.setVisibility(View.GONE);

                } else

                if (output == 2){

                    //set layout
                    noInternetLayout.setVisibility(View.VISIBLE);
                    emptyLayout.setVisibility(View.GONE);
                    runningCycleRecycler.setVisibility(View.GONE);

                }

            }
        }).execute();

        return v;
    }

    private void loadSponsorshipGroup() {

        //set view case
        isList = false;

        //setLayout
        controlLayout.setVisibility(View.GONE);

        runningCycleRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        runningCycleRecycler.setLayoutManager(layoutManager);

        adapter2 = new FirebaseRecyclerAdapter<RunningCycleModel, AdminViewHolder>(
                RunningCycleModel.class,
                R.layout.admin_item,
                AdminViewHolder.class,
                adminSponsorshipRef
        ) {
            @Override
            protected void populateViewHolder(final AdminViewHolder viewHolder, RunningCycleModel model, int position) {

                viewHolder.itemIdentification.setText(adapter2.getRef(viewHolder.getAdapterPosition()).getKey());


                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        loadRunningCycles(adapter2.getRef(viewHolder.getAdapterPosition()).getKey());
                    }
                });
            }
        };
        runningCycleRecycler.setAdapter(adapter2);

    }

    private void loadRunningCycles(final String farmId) {

        //set view case
        isList = true;

        //set layout
        controlLayout.setVisibility(View.VISIBLE);

        runningCycleRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        runningCycleRecycler.setLayoutManager(layoutManager);

        adapter = new FirebaseRecyclerAdapter<RunningCycleModel, RunningCycleViewHolder>(
                RunningCycleModel.class,
                R.layout.running_cycle_item,
                RunningCycleViewHolder.class,
                adminSponsorshipRef.child(farmId)
        ) {
            @Override
            protected void populateViewHolder(final RunningCycleViewHolder viewHolder, RunningCycleModel model, int position) {

                userRef.child(model.getUserId())
                        .addListenerForSingleValueEvent(
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        UserModel currentUser = dataSnapshot.getValue(UserModel.class);

                                        if (currentUser != null){

                                            viewHolder.cycleUserName.setText(currentUser.getFirstName() + " " + currentUser.getLastName());

                                        }

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                }
                        );

                long returnToLong = Long.parseLong(model.getSponsorReturn());

                viewHolder.cycleFarmType.setText(model.getSponsoredFarmType());
                viewHolder.cycleRefNumber.setText("Ref: " + model.getSponsorRefNumber());
                viewHolder.cycleUnits.setText(model.getSponsoredUnits() + " Units");
                viewHolder.cycleAmountPaid.setText("Amount Paid: " + Common.convertToPrice(getContext(), model.getTotalAmountPaid()));
                viewHolder.cycleReturn.setText("Return: " + Common.convertToPrice(getContext(), returnToLong));
                viewHolder.cycleStartDate.setText(model.getCycleStartDate());
                viewHolder.cycleEndDate.setText(model.getCycleEndDate());

            }
        };
        runningCycleRecycler.setAdapter(adapter);


        //back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadSponsorshipGroup();
            }
        });


        //search
        /*---   EDIT TEXT SEARCH   ---*/
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.toString().isEmpty()){
                    runningCycleRecycler.setVisibility(View.GONE);
                } else {
                    runningCycleRecycler.setVisibility(View.VISIBLE);

                }

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty()){

                    if (isAvailable(s.toString(), farmId)){
                        searchForSponsor(s.toString(), farmId);
                    } else {
                        runningCycleRecycler.setVisibility(View.GONE);
                    }


                } else {

                    runningCycleRecycler.setVisibility(View.VISIBLE);
                    loadRunningCycles(farmId);
                }
            }
        });

    }

    private void searchForSponsor(String searchParam, String farmId) {

        final Query searchQuery = adminSponsorshipRef.child(farmId)
                .orderByChild("sponsorRefNumber")
                .startAt(searchParam)
                .endAt(searchParam+"\uf8ff");
        arrayList = new ArrayList<>();

        runningCycleRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        runningCycleRecycler.setLayoutManager(layoutManager);

        searchQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChildren()){

                    arrayList.clear();

                    for (DataSnapshot object : dataSnapshot.getChildren()){

                        final RunningCycleModel topicModel = object.getValue(RunningCycleModel.class);
                        arrayList.add(topicModel);

                    }

                    CustomSearchAdapter searchAdapter = new CustomSearchAdapter(getContext(), arrayList, getActivity());
                    runningCycleRecycler.setAdapter(searchAdapter);
                    searchAdapter.notifyDataSetChanged();

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private boolean isAvailable (String searchParam, String farmId){

        adminSponsorshipRef.child(farmId)
                .orderByChild("sponsorRefNumber")
                .startAt(searchParam)
                .endAt(searchParam+"\uf8ff")
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){

                    isExist = true;

                } else {

                    isExist = false;

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return isExist;

    }

}
