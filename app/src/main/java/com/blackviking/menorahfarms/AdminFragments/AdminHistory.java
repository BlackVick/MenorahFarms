package com.blackviking.menorahfarms.AdminFragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blackviking.menorahfarms.CartAndHistory.HistoryDetails;
import com.blackviking.menorahfarms.CartAndHistory.SponsorshipHistory;
import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.Interface.ItemClickListener;
import com.blackviking.menorahfarms.Models.HistoryModel;
import com.blackviking.menorahfarms.R;
import com.blackviking.menorahfarms.ViewHolders.AdminViewHolder;
import com.blackviking.menorahfarms.ViewHolders.HistoryViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * A simple {@link Fragment} subclass.
 */
public class AdminHistory extends Fragment {

    private RecyclerView adminHistoryRecycler;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference adminHistoryRef;

    private LinearLayoutManager layoutManager;
    private FirebaseRecyclerAdapter<HistoryModel, AdminViewHolder> adapter;

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
        adminHistoryRecycler = (RecyclerView)v.findViewById(R.id.adminHistoryRecycler);


        loadHistory();
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
                adminHistoryRef
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
