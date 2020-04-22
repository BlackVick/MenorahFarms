package com.blackviking.menorahfarms.ViewHolders;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blackviking.menorahfarms.R;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PendingSponsorshipViewHolder extends RecyclerView.ViewHolder {

    public TextView userName, userSponsorRef, userAmountPaid;
    public ImageView userImg;
    public RelativeLayout confimPending;

    public PendingSponsorshipViewHolder(@NonNull View itemView) {
        super(itemView);

        userName = itemView.findViewById(R.id.userName);
        userSponsorRef = itemView.findViewById(R.id.userSponsorRef);
        userAmountPaid = itemView.findViewById(R.id.userAmountPaid);
        userImg = itemView.findViewById(R.id.userImg);
        confimPending = itemView.findViewById(R.id.confimPending);
    }
}
