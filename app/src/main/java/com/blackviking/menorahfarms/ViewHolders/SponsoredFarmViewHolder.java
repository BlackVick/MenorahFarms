package com.blackviking.menorahfarms.ViewHolders;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackviking.menorahfarms.Interface.ItemClickListener;
import com.blackviking.menorahfarms.R;

public class SponsoredFarmViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private ItemClickListener itemClickListener;
    public ImageView sponsoredFarmImage;
    public TextView sponsoredFarmType, sponsoredFarmPriceUnit, sponsoredFarmDate, sponsoredFarmRefNumber, sponsoredFarmROI;

    public SponsoredFarmViewHolder(@NonNull View itemView) {
        super(itemView);

        sponsoredFarmImage = (ImageView)itemView.findViewById(R.id.sponsoredFarmImage);
        sponsoredFarmType = (TextView)itemView.findViewById(R.id.sponsoredFarmType);
        sponsoredFarmPriceUnit = (TextView)itemView.findViewById(R.id.sponsoredFarmPriceUnit);
        sponsoredFarmDate = (TextView)itemView.findViewById(R.id.sponsoredFarmDate);
        sponsoredFarmRefNumber = (TextView)itemView.findViewById(R.id.sponsoredFarmRefNumber);
        sponsoredFarmROI = (TextView)itemView.findViewById(R.id.sponsoredFarmROI);

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
