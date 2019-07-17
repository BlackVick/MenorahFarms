package com.blackviking.menorahfarms.ViewHolders;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.blackviking.menorahfarms.R;

public class NotificationViewHolder extends RecyclerView.ViewHolder {

    public TextView notificationTopic, notificationMessage, notificationTime;

    public NotificationViewHolder(@NonNull View itemView) {
        super(itemView);

        notificationTopic = (TextView)itemView.findViewById(R.id.notificationTopic);
        notificationMessage = (TextView)itemView.findViewById(R.id.notificationMessage);
        notificationTime = (TextView)itemView.findViewById(R.id.notificationTime);
    }
}
