package com.blackviking.menorahfarms;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.Models.FarmModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FarmDescription extends AppCompatActivity {

    private ImageView backButton;
    private TextView farmDescUnits, farmDescROI, farmDescDuration, farmDescPrice, farmDescLocation, farmDescTerms;

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference farmRef;
    private String farmId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farm_description);


        /*---   LOCAL INTENT   ---*/
        farmId = getIntent().getStringExtra("FarmId");


        /*---   FIREBASE   ---*/
        farmRef = db.getReference(Common.FARM_NODE);


        /*---   WIDGETS   ---*/
        backButton = findViewById(R.id.backButton);
        farmDescUnits = findViewById(R.id.farmDescUnits);
        farmDescROI = findViewById(R.id.farmDescROI);
        farmDescDuration = findViewById(R.id.farmDescDuration);
        farmDescPrice = findViewById(R.id.farmDescPrice);
        farmDescLocation = findViewById(R.id.farmDescLocation);
        farmDescTerms = findViewById(R.id.farmDescTerms);

        backButton.setOnClickListener(v -> finish());

        loadCurrentFarm();
    }

    private void loadCurrentFarm() {

        farmRef.child(farmId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        FarmModel currentFarm = dataSnapshot.getValue(FarmModel.class);

                        if (currentFarm != null){

                            //set farm details
                            farmDescDuration.setText(currentFarm.getSponsorDuration() + " Months");
                            farmDescLocation.setText(currentFarm.getFarmLocation());
                            farmDescROI.setText(currentFarm.getFarmRoi() + "%");
                            farmDescUnits.setText(currentFarm.getUnitsAvailable());
                            farmDescTerms.setText(currentFarm.getFarmDescription());

                            long priceToLong = Long.parseLong(currentFarm.getPricePerUnit());
                            farmDescPrice.setText(Common.convertToPrice(FarmDescription.this, priceToLong));

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
