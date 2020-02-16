package com.blackviking.menorahfarms.AdminFragments;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.Models.UserModel;
import com.blackviking.menorahfarms.R;
import com.blackviking.menorahfarms.Models.RunningCycleModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CustomSearchAdapter extends RecyclerView.Adapter<CustomSearchAdapter.CustomSearchAdapterViewHolder> {

    public Context c;
    public ArrayList<RunningCycleModel> arrayList;
    public Activity activity;

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference userRef;


    public CustomSearchAdapter(Context c, ArrayList<RunningCycleModel> arrayList, Activity activity){
        this.c = c;
        this.arrayList = arrayList;
        this.activity = activity;
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public CustomSearchAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.running_cycle_item, viewGroup, false);

        return new CustomSearchAdapterViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final CustomSearchAdapterViewHolder viewHolder, int i) {

        final RunningCycleModel runningCycleModel = arrayList.get(i);
        /*---   FIREBASE   ---*/
        userRef = db.getReference("Users");

        userRef.child(runningCycleModel.getUserId())
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

        long returnToLong = Long.parseLong(runningCycleModel.getSponsorReturn());

        viewHolder.cycleFarmType.setText(runningCycleModel.getSponsoredFarmType());
        viewHolder.cycleRefNumber.setText("Ref: " + runningCycleModel.getSponsorRefNumber());
        viewHolder.cycleUnits.setText(runningCycleModel.getSponsoredUnits() + " Units");
        viewHolder.cycleAmountPaid.setText("Amount Paid: " + Common.convertToPrice(c, runningCycleModel.getTotalAmountPaid()));
        viewHolder.cycleReturn.setText("Return: " + Common.convertToPrice(c, returnToLong));
        viewHolder.cycleStartDate.setText(runningCycleModel.getCycleStartDate());
        viewHolder.cycleEndDate.setText(runningCycleModel.getCycleEndDate());


    }

    public class CustomSearchAdapterViewHolder extends RecyclerView.ViewHolder {

        public TextView cycleUserName, cycleFarmType, cycleRefNumber, cycleUnits,
                cycleAmountPaid, cycleReturn, cycleStartDate, cycleEndDate;

        public CustomSearchAdapterViewHolder(@NonNull View itemView) {
            super(itemView);

            cycleUserName = itemView.findViewById(R.id.cycleUserName);
            cycleFarmType = itemView.findViewById(R.id.cycleFarmType);
            cycleRefNumber = itemView.findViewById(R.id.cycleRefNumber);
            cycleUnits = itemView.findViewById(R.id.cycleUnits);
            cycleAmountPaid = itemView.findViewById(R.id.cycleAmountPaid);
            cycleReturn = itemView.findViewById(R.id.cycleReturn);
            cycleStartDate = itemView.findViewById(R.id.cycleStartDate);
            cycleEndDate = itemView.findViewById(R.id.cycleEndDate);
        }

    }


}
