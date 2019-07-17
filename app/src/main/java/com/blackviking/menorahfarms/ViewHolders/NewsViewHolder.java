package com.blackviking.menorahfarms.ViewHolders;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackviking.menorahfarms.R;

public class NewsViewHolder extends RecyclerView.ViewHolder {

    public TextView newsTime, newsTitle;
    public Button newsInfoBtn;
    public ImageView newsImage;

    public NewsViewHolder(@NonNull View itemView) {
        super(itemView);

        newsTime = (TextView)itemView.findViewById(R.id.newsTime);
        newsTitle = (TextView)itemView.findViewById(R.id.newsTitle);
        newsInfoBtn = (Button)itemView.findViewById(R.id.newsInfoBtn);
        newsImage = (ImageView)itemView.findViewById(R.id.newsImage);

    }
}
