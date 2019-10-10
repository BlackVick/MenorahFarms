package com.blackviking.menorahfarms.DashboardMenu;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blackviking.menorahfarms.Common.CheckInternet;
import com.blackviking.menorahfarms.R;

public class FarmUpdates extends AppCompatActivity {

    private ImageView backButton;
    private RelativeLayout noInternetLayout;
    private LinearLayout emptyLayout;
    private android.app.AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farm_updates);


        /*---   WIDGETS   ---*/
        backButton = (ImageView)findViewById(R.id.backButton);
        noInternetLayout = findViewById(R.id.noInternetLayout);
        emptyLayout = findViewById(R.id.emptyLayout);


        //show loading dialog
        showLoadingDialog("Loading farm updates . . .");


        //execute network check async task
        CheckInternet asyncTask = (CheckInternet) new CheckInternet(this, new CheckInternet.AsyncResponse(){
            @Override
            public void processFinish(Integer output) {

                //check all cases
                if (output == 1){

                    alertDialog.dismiss();
                    emptyLayout.setVisibility(View.VISIBLE);
                    noInternetLayout.setVisibility(View.GONE);

                } else

                if (output == 0){

                    //set layout
                    alertDialog.dismiss();
                    noInternetLayout.setVisibility(View.VISIBLE);
                    emptyLayout.setVisibility(View.GONE);

                } else

                if (output == 2){

                    //set layout
                    alertDialog.dismiss();
                    noInternetLayout.setVisibility(View.VISIBLE);
                    emptyLayout.setVisibility(View.GONE);

                }

            }
        }).execute();

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /*---   LOADING DIALOG   ---*/
    public void showLoadingDialog(String theMessage){

        alertDialog = new android.app.AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.loading_dialog,null);

        final TextView loadingText = viewOptions.findViewById(R.id.loadingText);

        alertDialog.setView(viewOptions);

        alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        loadingText.setText(theMessage);

        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });

        alertDialog.show();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
