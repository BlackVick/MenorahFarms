package com.blackviking.menorahfarms.DashboardMenu;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.blackviking.menorahfarms.R;

public class FarmUpdateFullscreen extends AppCompatActivity {

    private WebView updateFullScreen;

    private String link;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farm_update_fullscreen);

        link = getIntent().getStringExtra("VideoUrl");

        updateFullScreen = findViewById(R.id.updateFullScreen);

        updateFullScreen.setWebViewClient(new WebViewClient());
        updateFullScreen.setWebChromeClient(new WebChromeClient());
        updateFullScreen.getSettings().setJavaScriptEnabled(true);

        updateFullScreen.loadUrl(link);
    }
}
