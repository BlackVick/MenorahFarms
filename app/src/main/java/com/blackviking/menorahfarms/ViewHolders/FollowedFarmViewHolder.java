package com.blackviking.menorahfarms.ViewHolders;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackviking.menorahfarms.Interface.ItemClickListener;
import com.blackviking.menorahfarms.R;

public class FollowedFarmViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private ItemClickListener itemClickListener;
    public ImageView followedFarmImage;
    public TextView followedFarmType, followedFarmPrice, followedFarmState, followedFarmROI;

    public FollowedFarmViewHolder(@NonNull View itemView) {
        super(itemView);

        followedFarmImage = (ImageView)itemView.findViewById(R.id.followedFarmImage);
        followedFarmType = (TextView) itemView.findViewById(R.id.followedFarmType);
        followedFarmPrice = (TextView) itemView.findViewById(R.id.followedFarmPrice);
        followedFarmState = (TextView) itemView.findViewById(R.id.followedFarmState);
        followedFarmROI = (TextView) itemView.findViewById(R.id.followedFarmROI);

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
