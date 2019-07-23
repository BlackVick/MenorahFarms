package com.blackviking.menorahfarms.DashboardMenu;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.R;

public class Faq extends AppCompatActivity {

    private WebView webView;
    private ImageView backButton;
    private String faqLink = "https://menorahfarms.com/faqs/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);


        /*---   WIDGETS   ---*/
        webView = (WebView) findViewById(R.id.faqWebview);
        backButton = (ImageView)findViewById(R.id.backButton);



        /*---   LOAD PAGE   ---*/
        if (Common.isConnectedToInternet(getBaseContext())) {

            CookieSyncManager.createInstance(this);
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
                    Common.showErrorDialog(Faq.this, "Error Communicating With Server !", Faq.this);
                }
            });

        } else {

            Common.showErrorDialog(Faq.this, "No Internet Access !", Faq.this);

        }


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webView.canGoBack()){

                    webView.goBack();

                } else {

                    finish();
                }
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
