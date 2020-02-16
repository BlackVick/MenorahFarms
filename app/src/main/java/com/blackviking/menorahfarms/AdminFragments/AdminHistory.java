package com.blackviking.menorahfarms.AdminFragments;


import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.blackviking.menorahfarms.CartAndHistory.HistoryDetails;
import com.blackviking.menorahfarms.Common.CheckInternet;
import com.blackviking.menorahfarms.Interface.ItemClickListener;
import com.blackviking.menorahfarms.Models.HistoryModel;
import com.blackviking.menorahfarms.R;
import com.blackviking.menorahfarms.ViewHolders.AdminViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * A simple {@link Fragment} subclass.
 */
public class AdminHistory extends Fragment {

    private RecyclerView adminHistoryRecycler;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference adminHistoryRef;

    private LinearLayoutManager layoutManager;
    private FirebaseRecyclerAdapter<HistoryModel, AdminViewHolder> adapter;

    private RelativeLayout noInternetLayout;
    private LinearLayout emptyLayout;

    public AdminHistory() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_admin_history, container, false);


        /*---   FIREBASE   ---*/
        adminHistoryRef = db.getReference("AdminHistory");


        /*---   WIDGETS    ---*/
        adminHistoryRecycler = v.findViewById(R.id.adminHistoryRecycler);
        noInternetLayout = v.findViewById(R.id.noInternetLayout);
        emptyLayout = v.findViewById(R.id.emptyLayout);


        //run network check
        new CheckInternet(getContext(), output -> {

            //check all cases
            if (output == 1){

                adminHistoryRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()){

                            noInternetLayout.setVisibility(View.GONE);
                            emptyLayout.setVisibility(View.GONE);
                            adminHistoryRecycler.setVisibility(View.VISIBLE);
                            loadHistory();

                        } else {

                            noInternetLayout.setVisibility(View.GONE);
                            emptyLayout.setVisibility(View.VISIBLE);
                            adminHistoryRecycler.setVisibility(View.GONE);

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
                adminHistoryRecycler.setVisibility(View.GONE);

            } else

            if (output == 2){

                //set layout
                noInternetLayout.setVisibility(View.VISIBLE);
                emptyLayout.setVisibility(View.GONE);
                adminHistoryRecycler.setVisibility(View.GONE);

            }

        }).execute();

        return v;
    }

    private void loadHistory() {

        adminHistoryRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        adminHistoryRecycler.setLayoutManager(layoutManager);

        adapter = new FirebaseRecyclerAdapter<HistoryModel, AdminViewHolder>(
                HistoryModel.class,
                R.layout.admin_item,
                AdminViewHolder.class,
                adminHistoryRef.limitToLast(25)
        ) {
            @Override
            protected void populateViewHolder(final AdminViewHolder viewHolder, HistoryModel model, int position) {

                viewHolder.itemIdentification.setText(model.getSponsorRefNumber());


                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent historyDetailIntent = new Intent(getContext(), HistoryDetails.class);
                        historyDetailIntent.putExtra("HistoryId", adapter.getRef(viewHolder.getAdapterPosition()).getKey());
                        startActivity(historyDetailIntent);
                        getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_in);
                    }
                });
            }
        };
        adminHistoryRecycler.setAdapter(adapter);

    }

}
