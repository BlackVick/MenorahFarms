package com.blackviking.menorahfarms.DashboardMenu;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blackviking.menorahfarms.Common.CheckInternet;
import com.blackviking.menorahfarms.Common.Common;
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
        webView = (WebView) findViewById(R.id.faqWebview);
        backButton = (ImageView)findViewById(R.id.backButton);
        noInternetLayout = findViewById(R.id.noInternetLayout);


        //execute network check async task
        CheckInternet asyncTask = (CheckInternet) new CheckInternet(this, new CheckInternet.AsyncResponse(){
            @Override
            public void processFinish(Integer output) {

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
                            showErrorDialog("Error Communicating With Server !");
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

            }
        }).execute();

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

    /*---   WARNING DIALOG   ---*/
    public void showErrorDialog(String theWarning){

        final android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.dialog_layout,null);

        final TextView message = (TextView) viewOptions.findViewById(R.id.dialogMessage);
        final Button okButton = (Button) viewOptions.findViewById(R.id.dialogButton);

        alertDialog.setView(viewOptions);

        alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        message.setText(theWarning);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        alertDialog.show();

    }
}
