package com.blackviking.menorahfarms.FarmshopFragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.blackviking.menorahfarms.Interface.ItemClickListener;
import com.blackviking.menorahfarms.Models.FarmModel;
import com.blackviking.menorahfarms.R;
import com.blackviking.menorahfarms.ViewHolders.FarmStoreViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * A simple {@link Fragment} subclass.
 */
public class OpeningSoonFragment extends Fragment {

    private RecyclerView openingSoonRecycler;
    private LinearLayout emptyLayout;
    private LinearLayoutManager layoutManager;
    private FirebaseRecyclerAdapter<FarmModel, FarmStoreViewHolder> adapter;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference userRef, farmRef;
    private String currentUid, userType;

    public OpeningSoonFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_opening_soon, container, false);


        /*---   FIREBASE   ---*/
        userRef = db.getReference("Users");
        farmRef = db.getReference("Farms");
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();


        /*---   WIDGETS   ---*/
        emptyLayout = (LinearLayout)v.findViewById(R.id.emptyOpeningSoonLayout);
        openingSoonRecycler = (RecyclerView)v.findViewById(R.id.openingSoonRecycler);


        /*---   CURRENT USER   ---*/
        userRef.child(currentUid)
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {



                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        }
                );

        checkNowSelling();


        return v;
    }

    private void checkNowSelling() {

        farmRef.orderByChild("farmState")
                .equalTo("Opening Soon")
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists()){

                                    emptyLayout.setVisibility(View.GONE);
                                    loadOpeningSoonFarms();

                                } else {

                                    emptyLayout.setVisibility(View.VISIBLE);

                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        }
                );

    }

    private void loadOpeningSoonFarms() {

        openingSoonRecycler.setHasFixedSize(true);
        layoutManager = new GridLayoutManager(getContext(), 2);
        openingSoonRecycler.setLayoutManager(layoutManager);

        adapter = new FirebaseRecyclerAdapter<FarmModel, FarmStoreViewHolder>(
                FarmModel.class,
                R.layout.farm_item,
                FarmStoreViewHolder.class,
                farmRef.orderByChild("farmState")
                        .equalTo("Opening Soon")
        ) {
            @Override
            protected void populateViewHolder(FarmStoreViewHolder viewHolder, final FarmModel model, int position) {

                if (model.getPackaged().equalsIgnoreCase("true")){

                    viewHolder.farmPackage.setVisibility(View.VISIBLE);

                    viewHolder.farmPackage.setText(model.getPackagedType());
                    viewHolder.farmType.setText(model.getFarmType());
                    viewHolder.farmLocation.setText(model.getFarmLocation());
                    viewHolder.farmUnitPrice.setText(model.getPricePerUnit());
                    viewHolder.farmROI.setText("Returns " + model.getFarmRoi() + "% in " + model.getSponsorDuration() + " months");
                    viewHolder.farmName.setText(model.getFarmName());

                } else {

                    viewHolder.farmPackage.setVisibility(View.GONE);

                    viewHolder.farmType.setText(model.getFarmType());
                    viewHolder.farmLocation.setText(model.getFarmLocation());
                    viewHolder.farmUnitPrice.setText(model.getPricePerUnit());
                    viewHolder.farmROI.setText("Returns " + model.getFarmRoi() + "% in " + model.getSponsorDuration() + " months");
                    viewHolder.farmName.setText(model.getFarmName());

                }

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Toast.makeText(getContext(), ""+model.getFarmName(), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        };
        openingSoonRecycler.setAdapter(adapter);

    }

}
