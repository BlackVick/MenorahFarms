package com.blackviking.menorahfarms.DashboardMenu;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.blackviking.menorahfarms.Common.CheckInternet;
import com.blackviking.menorahfarms.R;

public class Faq extends AppCompatActivity {

    private WebView webView;
    private ImageView backButton;
    private String faqLink = "http://menorahfarms.com/faq.html";
    private RelativeLayout noInternetLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);


        /*---   WIDGETS   ---*/
        webView = findViewById(R.id.faqWebview);
        backButton = findViewById(R.id.backButton);
        noInternetLayout = findViewById(R.id.noInternetLayout);


        //run network check
        new CheckInternet(this, output -> {

            //check all cases
            if (output == 1){

                //set layout
                noInternetLayout.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);

                CookieSyncManager.createInstance(Faq.this);
                CookieManager.getInstance().setAcceptCookie(true);

                webView.getSettings().setJavaScriptEnabled(true);
                webView.setFocusable(true);
                webView.setFocusableInTouchMode(true);
                webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
                webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
                webView.getSettings().setDomStorageEnabled(true);
                webView.getSettings().setDatabaseEnabled(true);
                webView.getSettings().setAppCacheEnabled(true);
                webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
                webView.loadUrl(faqLink);
                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                        Toast.makeText(Faq.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                });

            } else

            if (output == 0){

                //set layout
                noInternetLayout.setVisibility(View.VISIBLE);
                webView.setVisibility(View.GONE);

            } else

            if (output == 2){

                //set layout
                noInternetLayout.setVisibility(View.VISIBLE);
                webView.setVisibility(View.GONE);

            }

        }).execute();

        //back button
        backButton.setOnClickListener(v -> {

            if (webView.canGoBack()){

                webView.goBack();

            } else {

                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {

        if (webView.canGoBack()){

            webView.goBack();

        } else {

            finish();
        }
    }

}
