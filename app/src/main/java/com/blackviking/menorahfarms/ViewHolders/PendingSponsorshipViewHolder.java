package com.blackviking.menorahfarms.ViewHolders;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blackviking.menorahfarms.R;

public class PendingSponsorshipViewHolder extends RecyclerView.ViewHolder {

    public ImageView userImg;
    public TextView userName, userSponsorRef, userAmountPaid;
    public RelativeLayout confimPending;

    public PendingSponsorshipViewHolder(@NonNull View itemView) {
        super(itemView);

        userImg = itemView.findViewById(R.id.userImg);
        userName = itemView.findViewById(R.id.userName);
        userSponsorRef = itemView.findViewById(R.id.userSponsorRef);
        userAmountPaid = itemView.findViewById(R.id.userAmountPaid);
        confimPending = itemView.findViewById(R.id.confimPending);

    }
}
