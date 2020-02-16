package com.blackviking.menorahfarms.AdminDetails;

import android.content.DialogInterface;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blackviking.menorahfarms.Common.CheckInternet;
import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.Models.FarmModel;
import com.blackviking.menorahfarms.Models.RunningCycleModel;
import com.blackviking.menorahfarms.Models.SponsorshipDetailsModel;
import com.blackviking.menorahfarms.R;
import com.blackviking.menorahfarms.Services.AdminMonitorService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.jcminarro.roundkornerlayout.RoundKornerRelativeLayout;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import io.paperdb.Paper;

public class FarmManagementDetail extends AppCompatActivity {

    //widgets
    private ImageView farmManageDetailImage, decreaseHourNumber, increaseHourNumber, backButton, editButton;
    private TextView farmManageDetailsType, farmManageDetailLocation, farmManageDetailROI,
                        hourNumber, farmManageDetailStatus;
    private Button activateFarm, endSponsorship, confirmActivation, deactivateFarm;
    private RoundKornerRelativeLayout activateFarmLayout;
    private EditText unitsEdt;
    private Button changeFarmStatus;
    private TextView currentSponsorships, currentSponsoredUnits;

    //firebase
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference farmRef, runningCycleRef, sponsorshipDetailRef;
    private FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();

    //values
    private String farmId;
    private int hourInt = 0;
    private boolean isMonitorRunning;

    //loading
    private AlertDialog alertDialog, endSponsorshipDialog;
    private AlertDialog editFarmDialog;
    private boolean isLoading = false;
    private boolean isDisplayingDialog = false;
    private boolean isEditing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farm_management_detail);


        //intent data
        farmId = getIntent().getStringExtra("FarmId");


        //Firebase
        farmRef = db.getReference(Common.FARM_NODE);
        runningCycleRef = db.getReference(Common.RUNNING_CYCLE_NODE);
        sponsorshipDetailRef = db.getReference(Common.SPONSORSHIP_DETAILS_NODE);


        //check service run
        if (Paper.book().read(Common.isFarmServiceRunning) == null){

            Paper.book().write(Common.isFarmServiceRunning, false);
            isMonitorRunning = Paper.book().read(Common.isFarmServiceRunning);

        } else {

            isMonitorRunning = Paper.book().read(Common.isFarmServiceRunning);

        }


        //widgets
        farmManageDetailImage = findViewById(R.id.farmManageDetailImage);
        decreaseHourNumber = findViewById(R.id.decreaseHourNumber);
        increaseHourNumber = findViewById(R.id.increaseHourNumber);
        farmManageDetailsType = findViewById(R.id.farmManageDetailsType);
        farmManageDetailLocation = findViewById(R.id.farmManageDetailLocation);
        farmManageDetailROI = findViewById(R.id.farmManageDetailROI);
        hourNumber = findViewById(R.id.hourNumber);
        farmManageDetailStatus = findViewById(R.id.farmManageDetailStatus);
        activateFarm = findViewById(R.id.activateFarm);
        confirmActivation = findViewById(R.id.confirmActivation);
        activateFarmLayout = findViewById(R.id.activateFarmLayout);
        backButton = findViewById(R.id.backButton);
        deactivateFarm = findViewById(R.id.deactivateFarm);
        endSponsorship = findViewById(R.id.endSponsorship);
        unitsEdt = findViewById(R.id.unitsEdt);
        changeFarmStatus = findViewById(R.id.changeFarmStatus);
        currentSponsorships = findViewById(R.id.currentSponsorships);
        currentSponsoredUnits = findViewById(R.id.currentSponsoredUnits);
        editButton = findViewById(R.id.editButton);


        //run network check
        new CheckInternet(this, output -> {

            //check all cases
            if (output == 1){

                loadFarmDetails(farmId);

            } else

            if (output == 0){

                //set layout
                Toast.makeText(FarmManagementDetail.this, "No internet access", Toast.LENGTH_SHORT).show();

            } else

            if (output == 2){

                //set layout
                Toast.makeText(FarmManagementDetail.this, "No network connection detected", Toast.LENGTH_SHORT).show();
            }

        }).execute();


        //back
        backButton.setOnClickListener(v -> {

            if (isEditing){

                editFarmDialog.dismiss();
            }
            finish();

        });

    }

    private void loadFarmDetails(final String farmId) {

        farmRef.child(farmId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        FarmModel currentFarm = dataSnapshot.getValue(FarmModel.class);

                        if (currentFarm != null){

                            setFarmDetails(currentFarm, farmId);

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }

    private void setFarmDetails(final FarmModel currentFarm, final String farmId) {

        //check for sponsorships
        runningCycleRef.orderByKey().equalTo(currentFarm.getFarmNotiId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (!dataSnapshot.exists()){

                            endSponsorship.setVisibility(View.GONE);

                        } else {

                            endSponsorship.setVisibility(View.VISIBLE);

                            //set click event
                            endSponsorship.setOnClickListener(v -> {

                                //display dialog
                                endSponsorshipDialog = new AlertDialog.Builder(FarmManagementDetail.this)
                                        .setTitle("End Sponsorship!")
                                        .setMessage("Are you sure you want to end " + currentFarm.getFarmName() + "'s sponsorship run?")
                                        .setNegativeButton("NO", (dialog, which) -> dialog.dismiss())
                                        .setPositiveButton("YES", (dialog, which) -> {
                                            //show dialog
                                            showLoadingDialog("Ending sponsorships . . .");

                                            endSponsorship(currentFarm.getFarmNotiId())
                                                    .addOnCompleteListener(task -> {

                                                        if (!task.isSuccessful()) {

                                                            Exception e = task.getException();
                                                            if (e instanceof FirebaseFunctionsException) {
                                                                FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
                                                                FirebaseFunctionsException.Code code = ffe.getCode();
                                                                Object details = ffe.getDetails();

                                                            }

                                                            //close dialog
                                                            alertDialog.dismiss();

                                                        } else {

                                                            //close dialog
                                                            alertDialog.dismiss();

                                                            //close activity
                                                            finish();

                                                        }

                                                    });
                                        })
                                        .setIcon(R.drawable.dash_sponsored_farms)
                                        .setOnCancelListener(dialogInterface -> isDisplayingDialog = false)
                                        .setOnDismissListener(dialogInterface -> isDisplayingDialog = false)
                                        .show();

                            });

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        //check sponsorships
        DatabaseReference newRunningCycleRef = db.getReference(Common.RUNNING_CYCLE_NODE)
                .child(currentFarm.getFarmNotiId());
        newRunningCycleRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                int sponsorships = 0;
                int sponsoredUnits = 0;
                for (DataSnapshot snap : dataSnapshot.getChildren()){


                    //sponsorships
                    sponsorships++;

                    //sponsored units
                    RunningCycleModel theCycles = snap.getValue(RunningCycleModel.class);

                    int unitSponsored = Integer.parseInt(theCycles.getSponsoredUnits());
                    sponsoredUnits = unitSponsored + sponsoredUnits;

                }

                //set texts
                currentSponsorships.setText(String.valueOf(sponsorships));
                currentSponsoredUnits.setText(String.valueOf(sponsoredUnits));

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //farm image
        if (!currentFarm.getFarmImageThumb().equalsIgnoreCase("")){

            Picasso.get()
                    .load(currentFarm.getFarmImageThumb())
                    .placeholder(R.drawable.menorah_placeholder)
                    .into(farmManageDetailImage);

        }

        //farm details
        farmManageDetailsType.setText(currentFarm.getFarmType());
        farmManageDetailLocation.setText(currentFarm.getFarmLocation());
        farmManageDetailROI.setText("Returns " + currentFarm.getFarmRoi() + "% in " + currentFarm.getSponsorDuration() + " months");
        farmManageDetailStatus.setText(currentFarm.getFarmState());



        if (currentFarm.getFarmState().equalsIgnoreCase("Now Selling")){

            activateFarmLayout.setVisibility(View.GONE);
            activateFarm.setVisibility(View.GONE);
            deactivateFarm.setVisibility(View.VISIBLE);
            changeFarmStatus.setVisibility(View.GONE);

            deactivateFarm.setOnClickListener(v -> deactivateTheFarm(currentFarm));

        } else {

            activateFarmLayout.setVisibility(View.GONE);
            activateFarm.setVisibility(View.VISIBLE);
            deactivateFarm.setVisibility(View.GONE);
            changeFarmStatus.setVisibility(View.VISIBLE);

            //change farm status
            changeFarmStatus.setOnClickListener(v -> {

                PopupMenu popup = new PopupMenu(FarmManagementDetail.this, changeFarmStatus);
                popup.inflate(R.menu.farm_status_menu);
                popup.setOnMenuItemClickListener(item -> {

                    switch (item.getItemId()) {

                        case R.id.action_now_selling:

                            updateStatus(currentFarm.getFarmNotiId(), "Now Selling");
                            return true;

                        case R.id.action_sold_out:

                            updateStatus(currentFarm.getFarmNotiId(), "Sold Out");
                            return true;

                        case R.id.action_opening_soon:

                            updateStatus(currentFarm.getFarmNotiId(), "Opening Soon");
                            return true;

                        default:
                            return false;
                    }
                });

                popup.show();

            });

            //activate farm
            activateFarm.setOnClickListener(v -> {

                activateFarmLayout.setVisibility(View.VISIBLE);
                activateFarm.setVisibility(View.GONE);
                deactivateFarm.setVisibility(View.GONE);

            });


            //increase hour(s)
            hourNumber.setText(String.valueOf(hourInt));
            increaseHourNumber.setOnClickListener(v -> {

                hourInt++;
                hourNumber.setText(String.valueOf(hourInt));

            });

            //decrease hour(s)
            decreaseHourNumber.setOnClickListener(v -> {

                if (hourInt == 0){



                } else {

                    hourInt --;
                    hourNumber.setText(String.valueOf(hourInt));

                }

            });

            //edit farm
            editButton.setOnClickListener(view -> showEditDialog(currentFarm));

            //confirm activation
            confirmActivation.setOnClickListener(v -> {

                String theUnitsString = unitsEdt.getText().toString().trim();
                int theUnitsInt = 0;

                if (!theUnitsString.isEmpty()){

                    theUnitsInt = Integer.parseInt(theUnitsString);

                }


                if (theUnitsString.isEmpty()){

                    unitsEdt.requestFocus();
                    unitsEdt.setError("Required");

                } else

                if (theUnitsInt < 1){

                    unitsEdt.requestFocus();
                    unitsEdt.setError("Invalid");

                } else

                if (hourInt < 1) {

                    Toast.makeText(this, "Hour cannot be less than 1", Toast.LENGTH_LONG).show();

                } else {

                    startTheCountdownService(farmId, hourInt, currentFarm, theUnitsInt);

                }

            });

        }

    }

    private void updateStatus(final String farmNotiId, final String newStatus) {

        //show loading
        showLoadingDialog("Updating farm status . . .");

        //run network check
        new CheckInternet(this, output -> {

            //check all cases
            if (output == 1){

                //update status
                farmRef.orderByChild("farmNotiId").equalTo(farmNotiId)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                //check
                                if (dataSnapshot.exists()){

                                    for (DataSnapshot snap : dataSnapshot.getChildren()){

                                        //get key
                                        String farmIdKey = snap.getKey();

                                        //update
                                        farmRef.child(farmIdKey).child("farmState").setValue(newStatus);
                                        farmRef.child(farmIdKey).child("unitsAvailable").setValue("0");


                                    }

                                    //stop loading
                                    alertDialog.dismiss();

                                } else {

                                    //stop loading
                                    alertDialog.dismiss();

                                    //error
                                    Toast.makeText(FarmManagementDetail.this, "Error occurred", Toast.LENGTH_SHORT).show();

                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

            } else

            if (output == 0){

                //stop loading
                alertDialog.dismiss();

                //set layout
                Toast.makeText(FarmManagementDetail.this, "No internet access", Toast.LENGTH_SHORT).show();

            } else

            if (output == 2){

                //stop loading
                alertDialog.dismiss();

                //set layout
                Toast.makeText(FarmManagementDetail.this, "No network connection detected", Toast.LENGTH_SHORT).show();
            }

        }).execute();

    }

    private Task<String> endSponsorship(final String farmNotiId) {

        // Create the arguments to the callable function.
        Map<String, Object> data = new HashMap<>();
        data.put("farm", farmNotiId);

        return mFunctions
                .getHttpsCallable("endSponsorships")
                .call(data)
                .continueWith(task -> {
                    // This continuation runs on either success or failure, but if the task
                    // has failed then getResult() will throw an Exception which will be
                    // propagated down.
                    String result = (String) task.getResult().getData();
                    Log.e("EndSponsExep", result);

                    return result;
                });
    }

    private void deactivateTheFarm(FarmModel currentFarm) {

        //run network check
        new CheckInternet(this, output -> {

            //check all cases
            if (output == 1){

                //update status
                updateStatus(currentFarm.getFarmNotiId(), "Sold Out");


                //end service
                if (Paper.book().read(Common.isFarmServiceRunning)){

                    Intent serviceIntent = new Intent(FarmManagementDetail.this, AdminMonitorService.class);
                    stopService(serviceIntent);

                }

                //buttons visibilities
                activateFarmLayout.setVisibility(View.GONE);
                activateFarm.setVisibility(View.VISIBLE);
                deactivateFarm.setVisibility(View.GONE);
                changeFarmStatus.setVisibility(View.VISIBLE);

            } else

            if (output == 0){

                //set layout
                Toast.makeText(FarmManagementDetail.this, "No internet access", Toast.LENGTH_SHORT).show();

            } else

            if (output == 2){

                //set layout
                Toast.makeText(FarmManagementDetail.this, "No network connection detected", Toast.LENGTH_SHORT).show();
            }

        }).execute();

    }

    private void startTheCountdownService(final String farmId, final int hourInt, final FarmModel currentFarm, final int unitsPlanned) {

        //run network check
        new CheckInternet(this, output -> {

            //check all cases
            if (output == 1){

                //update sponsorship details
                SponsorshipDetailsModel theModel = new SponsorshipDetailsModel(currentFarm.getFarmNotiId(), 0, unitsPlanned, unitsPlanned);
                sponsorshipDetailRef.setValue(theModel)
                        .addOnCompleteListener(task -> {

                            if (task.isSuccessful()){

                                //update status
                                updateStatus(currentFarm.getFarmNotiId(), "Now Selling");

                                //buttons visibilities
                                activateFarmLayout.setVisibility(View.GONE);
                                activateFarm.setVisibility(View.GONE);
                                deactivateFarm.setVisibility(View.VISIBLE);
                                changeFarmStatus.setVisibility(View.GONE);

                                //start service
                                Intent adminMonitorIntent = new Intent(FarmManagementDetail.this, AdminMonitorService.class);
                                adminMonitorIntent.putExtra("DurationHours", String.valueOf(hourInt));
                                adminMonitorIntent.putExtra("FarmType", currentFarm.getFarmNotiId());
                                adminMonitorIntent.putExtra("FarmId", farmId);
                                ContextCompat.startForegroundService(FarmManagementDetail.this, adminMonitorIntent);

                            } else {

                                //show error
                                Toast.makeText(this, "Activation unsuccessful!", Toast.LENGTH_LONG).show();

                            }

                        });

            } else

            if (output == 0){

                //set layout
                Toast.makeText(FarmManagementDetail.this, "No internet access", Toast.LENGTH_SHORT).show();

            } else

            if (output == 2){

                //set layout
                Toast.makeText(FarmManagementDetail.this, "No network connection detected", Toast.LENGTH_SHORT).show();
            }

        }).execute();

    }

    private void showEditDialog(final FarmModel currentFarm) {

        //value
        isEditing = true;

        //show dialog
        editFarmDialog = new AlertDialog.Builder(FarmManagementDetail.this, R.style.DialogTheme).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.edit_farm_details,null);

        //widgets
        final EditText roiEdt = viewOptions.findViewById(R.id.roiEdt);
        final EditText durationEdt = viewOptions.findViewById(R.id.durationEdt);
        final RelativeLayout cancelButton = viewOptions.findViewById(R.id.cancelButton);
        final RelativeLayout setButton = viewOptions.findViewById(R.id.setButton);
        final TextView setText = viewOptions.findViewById(R.id.setText);
        final ProgressBar setProgress = viewOptions.findViewById(R.id.setProgress);


        //set dialog properties
        editFarmDialog.setView(viewOptions);
        editFarmDialog.getWindow().getAttributes().windowAnimations = R.style.SlideDialogAnimation;
        editFarmDialog.getWindow().setGravity(Gravity.BOTTOM);
        editFarmDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams layoutParams = editFarmDialog.getWindow().getAttributes();
        editFarmDialog.getWindow().setAttributes(layoutParams);


        //set values
        roiEdt.setText(currentFarm.getFarmRoi());
        durationEdt.setText(currentFarm.getSponsorDuration());


        //cancel
        cancelButton.setOnClickListener(v -> editFarmDialog.dismiss());


        //set update
        setButton.setOnClickListener(v -> {


            //lock dialog
            editFarmDialog.setCancelable(false);
            editFarmDialog.setCanceledOnTouchOutside(false);


            //string value
            final String theRoi = roiEdt.getText().toString().trim();
            final String theDuration = durationEdt.getText().toString().trim();


            //check params

            if (theRoi.isEmpty()){

                roiEdt.requestFocus();
                roiEdt.setError("Required");

                //unlock dialog
                editFarmDialog.setCancelable(true);
                editFarmDialog.setCanceledOnTouchOutside(true);

            } else

            if (theDuration.isEmpty()){

                durationEdt.requestFocus();
                durationEdt.setError("Required");

                //unlock dialog
                editFarmDialog.setCancelable(true);
                editFarmDialog.setCanceledOnTouchOutside(true);

            } else {

                //loading
                setButton.setEnabled(false);
                cancelButton.setEnabled(false);
                setText.setVisibility(View.GONE);
                setProgress.setVisibility(View.VISIBLE);


                //execute network check
                new CheckInternet(FarmManagementDetail.this, output -> {

                    //check all cases
                    if (output == 1) {

                        final Map<String, Object> farmUpdateMap = new HashMap<>();
                        farmUpdateMap.put("farmRoi", theRoi);
                        farmUpdateMap.put("sponsorDuration", theDuration);

                        //get farm
                        farmRef.orderByChild("farmNotiId").equalTo(currentFarm.getFarmNotiId())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        for (DataSnapshot snap : dataSnapshot.getChildren()){

                                            //get key
                                            String theKey = snap.getKey();

                                            //push update to firebase
                                            farmRef.child(theKey)
                                                    .updateChildren(farmUpdateMap);

                                        }

                                        //finish
                                        Toast.makeText(FarmManagementDetail.this, "SUCCESS", Toast.LENGTH_SHORT).show();

                                        //dismiss dialog
                                        editFarmDialog.dismiss();

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                    } else

                    if (output == 0) {

                        //unlock dialog
                        editFarmDialog.setCancelable(true);
                        editFarmDialog.setCanceledOnTouchOutside(true);

                        //loading
                        setButton.setEnabled(true);
                        cancelButton.setEnabled(true);
                        setProgress.setVisibility(View.GONE);
                        setText.setVisibility(View.VISIBLE);

                        //no internet
                        Toast.makeText(FarmManagementDetail.this, "No internet access", Toast.LENGTH_SHORT).show();

                    } else

                    if (output == 2) {

                        //unlock dialog
                        editFarmDialog.setCancelable(true);
                        editFarmDialog.setCanceledOnTouchOutside(true);

                        //loading
                        setButton.setEnabled(true);
                        cancelButton.setEnabled(true);
                        setProgress.setVisibility(View.GONE);
                        setText.setVisibility(View.VISIBLE);

                        //no internet
                        Toast.makeText(FarmManagementDetail.this, "Not connected to a network", Toast.LENGTH_SHORT).show();

                    }

                }).execute();

            }

        });

        //listeners
        editFarmDialog.setOnCancelListener(dialogInterface -> isEditing = false);
        editFarmDialog.setOnDismissListener(dialogInterface -> isEditing = false);

        editFarmDialog.show();

    }

    /*---   LOADING DIALOG   ---*/
    public void showLoadingDialog(String theMessage){

        //loading
        isLoading = true;

        alertDialog = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.loading_dialog,null);

        final TextView loadingText = viewOptions.findViewById(R.id.loadingText);

        alertDialog.setView(viewOptions);

        alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        loadingText.setText(theMessage);

        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                isLoading = false;
            }
        });
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                isLoading = false;
            }
        });

        alertDialog.show();

    }

    @Override
    public void onBackPressed() {
        if (isLoading){

            alertDialog.dismiss();

        }
        if (isDisplayingDialog) {


            endSponsorshipDialog.dismiss();
        }
        if (isEditing){

            editFarmDialog.dismiss();
        }
        finish();
    }
}
