package com.blackviking.menorahfarms.FarmshopFragments;


import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.FarmDetails;
import com.blackviking.menorahfarms.Models.FarmModel;
import com.blackviking.menorahfarms.Models.UserModel;
import com.blackviking.menorahfarms.R;
import com.blackviking.menorahfarms.ViewHolders.FarmStoreViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import io.paperdb.Paper;

/**
 * A simple {@link Fragment} subclass.
 */
public class OpeningSoonFragment extends Fragment {

    private RecyclerView openingSoonRecycler;
    private LinearLayout emptyLayout;
    private LinearLayoutManager layoutManager;
    private FirebaseRecyclerAdapter<FarmModel, FarmStoreViewHolder> adapter;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference farmRef;

    public OpeningSoonFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_opening_soon, container, false);


        /*---   FIREBASE   ---*/
        farmRef = db.getReference(Common.FARM_NODE);


        /*---   WIDGETS   ---*/
        emptyLayout = v.findViewById(R.id.emptyOpeningSoonLayout);
        openingSoonRecycler = v.findViewById(R.id.openingSoonRecycler);


        checkOpeningSoon();


        return v;
    }

    private void checkOpeningSoon() {

        farmRef.orderByChild("farmState")
                .equalTo("Opening Soon")
                .addValueEventListener(
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
            protected void populateViewHolder(final FarmStoreViewHolder viewHolder, final FarmModel model, int position) {

                long priceToLong = Long.parseLong(model.getPricePerUnit());

                viewHolder.farmPackage.setVisibility(View.GONE);

                viewHolder.farmType.setText(model.getFarmType());
                viewHolder.farmLocation.setText(model.getFarmLocation());
                viewHolder.farmUnitPrice.setText(Common.convertToPrice(getContext(), priceToLong));
                viewHolder.farmROI.setText("Returns " + model.getFarmRoi() + "% in " + model.getSponsorDuration() + " months");
                viewHolder.farmName.setText(model.getFarmName());

                viewHolder.setItemClickListener((view, position13, isLongClick) -> {
                    Intent farmDetailIntent = new Intent(getContext(), FarmDetails.class);
                    farmDetailIntent.putExtra("FarmId", adapter.getRef(viewHolder.getAdapterPosition()).getKey());
                    startActivity(farmDetailIntent);
                    getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_in);
                });


                if (!model.getFarmImageThumb().equalsIgnoreCase("")){

                    Picasso.get()
                            .load(model.getFarmImageThumb())
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.menorah_placeholder)
                            .into(viewHolder.farmImage, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError(Exception e) {
                                    Picasso.get()
                                            .load(model.getFarmImageThumb())
                                            .placeholder(R.drawable.menorah_placeholder)
                                            .into(viewHolder.farmImage);
                                }
                            });

                }
            }
        };
        openingSoonRecycler.setAdapter(adapter);

    }

}
