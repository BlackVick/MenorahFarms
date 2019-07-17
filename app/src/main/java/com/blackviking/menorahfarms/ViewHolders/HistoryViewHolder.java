package com.blackviking.menorahfarms.ViewHolders;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.blackviking.menorahfarms.Interface.ItemClickListener;
import com.blackviking.menorahfarms.R;

public class HistoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private ItemClickListener itemClickListener;
    public TextView historyFarmType, historyRefNumber, historyFarmReturn, historyStartDate, historyEndDate;

    public HistoryViewHolder(@NonNull View itemView) {
        super(itemView);

        historyFarmType = (TextView)itemView.findViewById(R.id.historyFarmType);
        historyRefNumber = (TextView)itemView.findViewById(R.id.historyRefNumber);
        historyFarmReturn = (TextView)itemView.findViewById(R.id.historyFarmReturn);
        historyStartDate = (TextView)itemView.findViewById(R.id.historyStartDate);
        historyEndDate = (TextView)itemView.findViewById(R.id.historyEndDate);

        itemView.setOnClickListener(this);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View v) {
        itemClickListener.onClick(v, getAdapterPosition(), false);
    }
}
