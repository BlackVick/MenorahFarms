package com.blackviking.menorahfarms.ViewHolders;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.blackviking.menorahfarms.R;

public class RunningCycleViewHolder extends RecyclerView.ViewHolder {

    public TextView cycleUserName, cycleFarmType, cycleRefNumber, cycleUnits,
            cycleAmountPaid, cycleReturn, cycleStartDate, cycleEndDate;

    public RunningCycleViewHolder(@NonNull View itemView) {
        super(itemView);

        cycleUserName = (TextView)itemView.findViewById(R.id.cycleUserName);
        cycleFarmType = (TextView)itemView.findViewById(R.id.cycleFarmType);
        cycleRefNumber = (TextView)itemView.findViewById(R.id.cycleRefNumber);
        cycleUnits = (TextView)itemView.findViewById(R.id.cycleUnits);
        cycleAmountPaid = (TextView)itemView.findViewById(R.id.cycleAmountPaid);
        cycleReturn = (TextView)itemView.findViewById(R.id.cycleReturn);
        cycleStartDate = (TextView)itemView.findViewById(R.id.cycleStartDate);
        cycleEndDate = (TextView)itemView.findViewById(R.id.cycleEndDate);
    }
}
