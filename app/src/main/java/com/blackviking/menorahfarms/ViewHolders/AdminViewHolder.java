package com.blackviking.menorahfarms.ViewHolders;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.blackviking.menorahfarms.Interface.ItemClickListener;
import com.blackviking.menorahfarms.R;

public class AdminViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private ItemClickListener itemClickListener;
    public TextView itemIdentification;

    public AdminViewHolder(@NonNull View itemView) {
        super(itemView);

        itemIdentification = (TextView)itemView.findViewById(R.id.itemIdentification);

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
