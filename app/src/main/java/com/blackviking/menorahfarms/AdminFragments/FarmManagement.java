package com.blackviking.menorahfarms.AdminFragments;


import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.blackviking.menorahfarms.AdminDetails.FarmManagementDetail;
import com.blackviking.menorahfarms.Common.CheckInternet;
import com.blackviking.menorahfarms.Interface.ItemClickListener;
import com.blackviking.menorahfarms.Models.FarmModel;
import com.blackviking.menorahfarms.R;
import com.blackviking.menorahfarms.ViewHolders.FarmManagementViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 */
public class FarmManagement extends Fragment {

    private RelativeLayout noInternetLayout;
    private RecyclerView farmsRecycler;

    private LinearLayoutManager layoutManager;
    private FirebaseRecyclerAdapter<FarmModel, FarmManagementViewHolder> adapter;

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference farmRef;

    public FarmManagement() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_farm_management, container, false);


        //Firebase
        farmRef = db.getReference("Farms");


        //widgets
        noInternetLayout = v.findViewById(R.id.noInternetLayout);
        farmsRecycler = v.findViewById(R.id.farmsRecycler);


        //execute network check async task
        new CheckInternet(getContext(), output -> {

            //check all cases
            if (output == 1){

                loadFarms();

            } else

            if (output == 0){

                //set layout
                noInternetLayout.setVisibility(View.VISIBLE);
                farmsRecycler.setVisibility(View.GONE);

            } else

            if (output == 2){

                //set layout
                noInternetLayout.setVisibility(View.VISIBLE);
                farmsRecycler.setVisibility(View.GONE);
            }

        }).execute();

        return v;
    }

    private void loadFarms() {

        //set layout
        noInternetLayout.setVisibility(View.GONE);
        farmsRecycler.setVisibility(View.VISIBLE);


        farmsRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        farmsRecycler.setLayoutManager(layoutManager);


        adapter = new FirebaseRecyclerAdapter<FarmModel, FarmManagementViewHolder>(
                FarmModel.class,
                R.layout.farm_management_item,
                FarmManagementViewHolder.class,
                farmRef.orderByChild("packagedType")
                .equalTo("Worker")
        ) {
            @Override
            protected void populateViewHolder(FarmManagementViewHolder viewHolder, FarmModel model, int position) {

                final String farmId = adapter.getRef(viewHolder.getAdapterPosition()).getKey();

                if (!model.getFarmImageThumb().equalsIgnoreCase("")){

                    Picasso.get()
                            .load(model.getFarmImageThumb())
                            .placeholder(R.drawable.menorah_placeholder)
                            .into(viewHolder.farmManageImage);

                }

                viewHolder.farmManageType.setText(model.getFarmType());
                viewHolder.farmManageStatus.setText(model.getFarmState());


                viewHolder.setItemClickListener((view, position1, isLongClick) -> {

                    Intent farmmanageIntent = new Intent(getContext(), FarmManagementDetail.class);
                    farmmanageIntent.putExtra("FarmId", farmId);
                    startActivity(farmmanageIntent);
                    getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_in);

                });

            }
        };
        farmsRecycler.setAdapter(adapter);

    }

}
