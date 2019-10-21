package com.blackviking.menorahfarms.ViewHolders;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackviking.menorahfarms.R;

public class FarmUpdateViewHolder extends RecyclerView.ViewHolder {

    public WebView youtubeView;
    public TextView youtubeTitle;
    public ImageView playInFullScreen;

    public FarmUpdateViewHolder(@NonNull View itemView) {
        super(itemView);

        youtubeView = itemView.findViewById(R.id.youtubeView);
        youtubeTitle = itemView.findViewById(R.id.youtubeTitle);
        playInFullScreen = itemView.findViewById(R.id.playInFullScreen);
        youtubeView.setWebViewClient(new WebViewClient());
        youtubeView.setWebChromeClient(new WebChromeClient());
        youtubeView.getSettings().setJavaScriptEnabled(true);
    }
}
