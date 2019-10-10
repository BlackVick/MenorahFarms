package com.blackviking.menorahfarms.Sponsorship;

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
import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.Models.SponsoredFarmModel;
import com.blackviking.menorahfarms.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SponsorshipDetails extends AppCompatActivity {

    private ImageView backButton;
    private TextView sponsoredFarmType, sponsoredFarmUnitPrice, sponsoredFarmUnits, sponsoredFarmROI,
            sponsoredFarmDuration, sponsoredFarmStartDate, sponsoredFarmEndDate, sponsoredFarmTotalReturn,
            sponsoredFarmRefNumber, sponsoredFarmTotalPrice;

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference farmref, sponsoredFarmRef;
    private String currentUid, sponsorshipId;

    private android.app.AlertDialog alertDialog;
    private RelativeLayout noInternetLayout;
    private LinearLayout contentLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sponsorship_details);


        /*---   INTENT DATA   ---*/
        sponsorshipId = getIntent().getStringExtra("SponsorshipId");


        /*---   FIREBASE   ---*/
        farmref = db.getReference("Farms");
        sponsoredFarmRef = db.getReference("SponsoredFarms");
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();


        /*---   WIDGETS   ---*/
        backButton = (ImageView)findViewById(R.id.backButton);
        sponsoredFarmType = (TextView)findViewById(R.id.sponsoredFarmType);
        sponsoredFarmUnitPrice = (TextView)findViewById(R.id.sponsoredFarmUnitPrice);
        sponsoredFarmUnits = (TextView)findViewById(R.id.sponsoredFarmUnits);
        sponsoredFarmROI = (TextView)findViewById(R.id.sponsoredFarmROI);
        sponsoredFarmDuration = (TextView)findViewById(R.id.sponsoredFarmDuration);
        sponsoredFarmStartDate = (TextView)findViewById(R.id.sponsoredFarmStartDate);
        sponsoredFarmEndDate = (TextView)findViewById(R.id.sponsoredFarmEndDate);
        sponsoredFarmTotalReturn = (TextView)findViewById(R.id.sponsoredFarmTotalReturn);
        sponsoredFarmRefNumber = (TextView)findViewById(R.id.sponsoredFarmRefNumber);
        sponsoredFarmTotalPrice = (TextView)findViewById(R.id.sponsoredFarmTotalPrice);
        noInternetLayout = findViewById(R.id.noInternetLayout);
        contentLayout = findViewById(R.id.contentLayout);


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        //show loading dialog
        showLoadingDialog("Loading sponsorship details . . .");

        //execute network check async task
        CheckInternet asyncTask = (CheckInternet) new CheckInternet(this, new CheckInternet.AsyncResponse(){
            @Override
            public void processFinish(Integer output) {

                //check all cases
                if (output == 1){

                    noInternetLayout.setVisibility(View.GONE);
                    contentLayout.setVisibility(View.VISIBLE);
                    loadSponsorshipDetails();

                } else

                if (output == 0){

                    //set layout
                    alertDialog.dismiss();
                    noInternetLayout.setVisibility(View.VISIBLE);
                    contentLayout.setVisibility(View.GONE);

                } else

                if (output == 2){

                    //set layout
                    alertDialog.dismiss();
                    noInternetLayout.setVisibility(View.VISIBLE);
                    contentLayout.setVisibility(View.GONE);

                }

            }
        }).execute();


    }

    private void loadSponsorshipDetails() {

        //remove dalog
        alertDialog.dismiss();


        sponsoredFarmRef.child(currentUid)
                .child(sponsorshipId)
                .addValueEventListener(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                SponsoredFarmModel currentSponsorship = dataSnapshot.getValue(SponsoredFarmModel.class);

                                sponsoredFarmType.setText(currentSponsorship.getSponsoredFarmType());
                                sponsoredFarmUnits.setText(currentSponsorship.getSponsoredUnits());
                                sponsoredFarmROI.setText(currentSponsorship.getSponsoredFarmRoi() + "%");
                                sponsoredFarmDuration.setText(currentSponsorship.getSponsorshipDuration() + " Months");
                                sponsoredFarmStartDate.setText(currentSponsorship.getCycleStartDate());
                                sponsoredFarmEndDate.setText(currentSponsorship.getCycleEndDate());
                                sponsoredFarmRefNumber.setText(currentSponsorship.getSponsorRefNumber());


                                long unitPriceToLong = Long.parseLong(currentSponsorship.getUnitPrice());
                                long totalReturnToLong = Long.parseLong(currentSponsorship.getSponsorReturn());


                                sponsoredFarmUnitPrice.setText(Common.convertToPrice(SponsorshipDetails.this, unitPriceToLong));
                                sponsoredFarmTotalReturn.setText(Common.convertToPrice(SponsorshipDetails.this, totalReturnToLong));
                                sponsoredFarmTotalPrice.setText(Common.convertToPrice(SponsorshipDetails.this, currentSponsorship.getTotalAmountPaid()));




                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        }
                );

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
