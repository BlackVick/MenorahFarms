package com.blackviking.menorahfarms.ViewHolders;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackviking.menorahfarms.Interface.ItemClickListener;
import com.blackviking.menorahfarms.R;

public class FarmStoreViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private ItemClickListener itemClickListener;
    public ImageView farmImage;
    public TextView farmType, farmLocation, farmPackage, farmUnitPrice, farmROI, farmName;

    public FarmStoreViewHolder(@NonNull View itemView) {
        super(itemView);

        farmImage = (ImageView)itemView.findViewById(R.id.farmImage);
        farmType = (TextView)itemView.findViewById(R.id.farmType);
        farmLocation = (TextView)itemView.findViewById(R.id.farmLocation);
        farmPackage = (TextView)itemView.findViewById(R.id.farmPackage);
        farmUnitPrice = (TextView)itemView.findViewById(R.id.farmUnitPrice);
        farmROI = (TextView)itemView.findViewById(R.id.farmROI);
        farmName = (TextView)itemView.findViewById(R.id.farmName);

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
