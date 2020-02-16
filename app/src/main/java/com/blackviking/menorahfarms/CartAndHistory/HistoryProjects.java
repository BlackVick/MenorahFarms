package com.blackviking.menorahfarms.CartAndHistory;


import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blackviking.menorahfarms.Common.CheckInternet;
import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.Interface.ItemClickListener;
import com.blackviking.menorahfarms.Models.FarmModel;
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
import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 */
public class HistoryProjects extends Fragment {

    private RecyclerView historyRecycler;
    private LinearLayout emptyLayout;
    private LinearLayoutManager layoutManager;
    private FirebaseRecyclerAdapter<HistoryModel, HistoryViewHolder> adapter;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference farmRef, sponsorshipRef, historyRef;

    private RelativeLayout noInternetLayout;
    private String currentUid;

    private android.app.AlertDialog alertDialog;

    public HistoryProjects() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_history_projects, container, false);


        /*---   FIREBASE   ---*/
        farmRef = db.getReference(Common.FARM_NODE);
        sponsorshipRef = db.getReference(Common.SPONSORED_FARMS_NODE);
        historyRef = db.getReference(Common.HISTORY_NODE);
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();


        /*---   WIDGET   ---*/
        emptyLayout = v.findViewById(R.id.emptyLayout);
        historyRecycler = v.findViewById(R.id.projectRecycler);
        noInternetLayout = v.findViewById(R.id.noInternetLayout);


        //show loading dialog
        showLoadingDialog("Loading history by projects . . .");


        //run network check
        new CheckInternet(getContext(), output -> {

            //check all cases
            if (output == 1){

                /*---   CHECK   ---*/
                historyRef.child(currentUid)
                        .addListenerForSingleValueEvent(
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        if (dataSnapshot.exists()){

                                            noInternetLayout.setVisibility(View.GONE);
                                            historyRecycler.setVisibility(View.VISIBLE);
                                            emptyLayout.setVisibility(View.GONE);
                                            loadHistory();

                                        } else {

                                            alertDialog.dismiss();
                                            noInternetLayout.setVisibility(View.GONE);
                                            historyRecycler.setVisibility(View.GONE);
                                            emptyLayout.setVisibility(View.VISIBLE);

                                        }

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                }
                        );

            } else

            if (output == 0){

                //set layout
                alertDialog.dismiss();
                noInternetLayout.setVisibility(View.VISIBLE);
                historyRecycler.setVisibility(View.GONE);
                emptyLayout.setVisibility(View.GONE);

            } else

            if (output == 2){

                //set layout
                alertDialog.dismiss();
                noInternetLayout.setVisibility(View.VISIBLE);
                historyRecycler.setVisibility(View.GONE);
                emptyLayout.setVisibility(View.GONE);

            }

        }).execute();

        return v;
    }

    private void loadHistory() {

        historyRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        historyRecycler.setLayoutManager(layoutManager);

        adapter = new FirebaseRecyclerAdapter<HistoryModel, HistoryViewHolder>(
                HistoryModel.class,
                R.layout.project_history_layout,
                HistoryViewHolder.class,
                historyRef.child(currentUid)
        ) {
            @Override
            protected void populateViewHolder(final HistoryViewHolder viewHolder, final HistoryModel model, int position) {

                //remove loading
                alertDialog.dismiss();

                farmRef.child(model.getFarmId())
                        .addListenerForSingleValueEvent(
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        FarmModel currentFarm = dataSnapshot.getValue(FarmModel.class);

                                        if (currentFarm != null){

                                            if (!currentFarm.getFarmImageThumb().equalsIgnoreCase("")){

                                                Picasso.get()
                                                        .load(currentFarm.getFarmImageThumb())
                                                        .into(viewHolder.historyProjectImage);

                                            }

                                            viewHolder.historyProjectDets.setText(currentFarm.getFarmName()
                                            + " yields a " + currentFarm.getFarmRoi() + "% return over a period of "
                                            + currentFarm.getSponsorDuration() + " months.");

                                            viewHolder.historyProjectNickName.setText(currentFarm.getFarmType());
                                            viewHolder.historyProjectLocation.setText(currentFarm.getFarmLocation()
                                            + "-" + model.getCycleStartDate());


                                        }

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                }
                        );

                viewHolder.historyProjectUnits.setText(model.getSponsoredUnits());
                viewHolder.historyProjectPrice.setText(Common.convertToPrice(getContext(), model.getTotalAmountPaid()));

                long returnToLong = Long.parseLong(model.getSponsorReturn());

                viewHolder.historyProjectReturn.setText(Common.convertToPrice(getContext(), returnToLong));

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
        historyRecycler.setAdapter(adapter);

    }

    /*---   LOADING DIALOG   ---*/
    public void showLoadingDialog(String theMessage){

        alertDialog = new android.app.AlertDialog.Builder(getContext()).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.loading_dialog,null);

        final TextView loadingText = viewOptions.findViewById(R.id.loadingText);

        alertDialog.setView(viewOptions);

        alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        loadingText.setText(theMessage);

        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                getActivity().finish();
            }
        });

        alertDialog.show();

    }
}
