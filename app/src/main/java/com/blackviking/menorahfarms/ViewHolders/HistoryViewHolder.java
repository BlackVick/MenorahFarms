package com.blackviking.menorahfarms.ViewHolders;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackviking.menorahfarms.Interface.ItemClickListener;
import com.blackviking.menorahfarms.R;

public class HistoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private ItemClickListener itemClickListener;
    public ImageView historyProjectImage;
    public TextView historyProjectDets, historyProjectNickName, historyProjectLocation, historyProjectUnits, historyProjectPrice, historyProjectReturn;

    public HistoryViewHolder(@NonNull View itemView) {
        super(itemView);

        historyProjectImage = (ImageView)itemView.findViewById(R.id.historyProjectImage);
        historyProjectDets = (TextView)itemView.findViewById(R.id.historyProjectDets);
        historyProjectNickName = (TextView)itemView.findViewById(R.id.historyProjectNickName);
        historyProjectLocation = (TextView)itemView.findViewById(R.id.historyProjectLocation);
        historyProjectUnits = (TextView)itemView.findViewById(R.id.historyProjectUnits);
        historyProjectPrice = (TextView)itemView.findViewById(R.id.historyProjectPrice);
        historyProjectReturn = (TextView)itemView.findViewById(R.id.historyProjectReturn);

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
