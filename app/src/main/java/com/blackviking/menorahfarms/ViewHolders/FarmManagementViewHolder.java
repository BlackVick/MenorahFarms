package com.blackviking.menorahfarms.ViewHolders;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackviking.menorahfarms.Interface.ItemClickListener;
import com.blackviking.menorahfarms.R;

public class FarmManagementViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private ItemClickListener itemClickListener;
    public ImageView farmManageImage;
    public TextView farmManageType, farmManageStatus;

    public FarmManagementViewHolder(@NonNull View itemView) {
        super(itemView);

        farmManageImage = itemView.findViewById(R.id.farmManageImage);
        farmManageType = itemView.findViewById(R.id.farmManageType);
        farmManageStatus = itemView.findViewById(R.id.farmManageStatus);

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
