package com.blackviking.menorahfarms;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blackviking.menorahfarms.AdminFragments.AdminHistory;
import com.blackviking.menorahfarms.AdminFragments.AdminNotify;
import com.blackviking.menorahfarms.AdminFragments.DueSponsorships;
import com.blackviking.menorahfarms.AdminFragments.FarmManagement;
import com.blackviking.menorahfarms.AdminFragments.RunningSponsorships;
import com.blackviking.menorahfarms.Common.CheckInternet;
import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.Models.UserModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.paperdb.Paper;

public class AdminDash extends AppCompatActivity {

    private ImageView backButton;
    private TextView fragmentName;

    private RelativeLayout dueSponsorshipLayout, adminNotifyLayout,
                            runningSponsorshipLayout, adminHistoryLayout, farmManagementLayout;
    private ImageView dueSponsorshipImage, adminNotifyImage, runningSponsorshipImage,
                            adminHistoryImage, farmManagementImage;
    private TextView dueSponsorshipCounter, runningSponsorshipCounter;

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference dueSponsorshipRef,  adminSponsorshipRef;


    private RelativeLayout noInternetLayout, adminBottomNav;
    private View line;
    private FrameLayout adminFrame;

    //loading
    private android.app.AlertDialog alertDialog;
    private boolean isLoading = false;

    private UserModel paperUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dash);


        /*---   FIREBASE   ---*/
        dueSponsorshipRef = db.getReference(Common.DUE_SPONSORSHIPS_NODE);
        adminSponsorshipRef = db.getReference(Common.RUNNING_CYCLE_NODE);


        //paper User
        paperUser = Paper.book().read(Common.PAPER_USER);

        /*---   WIDGETS   ---*/
        backButton = findViewById(R.id.backButton);
        fragmentName = findViewById(R.id.fragmentName);

        dueSponsorshipLayout = findViewById(R.id.dueSponsorshipLayout);
        adminNotifyLayout = findViewById(R.id.adminNotifyLayout);
        runningSponsorshipLayout = findViewById(R.id.runningSponsorshipLayout);
        adminHistoryLayout = findViewById(R.id.adminHistoryLayout);
        dueSponsorshipImage = findViewById(R.id.dueSponsorshipImage);
        adminNotifyImage = findViewById(R.id.adminNotifyImage);
        runningSponsorshipImage = findViewById(R.id.runningSponsorshipImage);
        adminHistoryImage = findViewById(R.id.adminHistoryImage);
        dueSponsorshipCounter = findViewById(R.id.dueSponsorshipCounter);
        runningSponsorshipCounter = findViewById(R.id.runningSponsorshipCounter);

        noInternetLayout = findViewById(R.id.noInternetLayout);
        adminBottomNav = findViewById(R.id.adminBottomNav);
        adminFrame = findViewById(R.id.adminFrame);
        line = findViewById(R.id.line);

        farmManagementLayout = findViewById(R.id.farmManagementLayout);
        farmManagementImage = findViewById(R.id.farmManagementImage);



        /*---   FRAGMENTS   ---*/
        final DueSponsorships dueSponsorships = new DueSponsorships();
        final AdminNotify adminNotify = new AdminNotify();
        final RunningSponsorships runningSponsorships = new RunningSponsorships();
        final AdminHistory adminHistory = new AdminHistory();
        final FarmManagement farmManagement = new FarmManagement();


        //show loading dialog
        showLoadingDialog("Loading admin dashboard . . .");

        //run network check
        new CheckInternet(this, output -> {

            //check all cases
            if (output == 1){

                noInternetLayout.setVisibility(View.GONE);
                adminBottomNav.setVisibility(View.VISIBLE);
                adminFrame.setVisibility(View.VISIBLE);
                line.setVisibility(View.VISIBLE);
                runAllChecks();

            } else

            if (output == 0){

                //set layout
                alertDialog.dismiss();
                noInternetLayout.setVisibility(View.VISIBLE);
                adminBottomNav.setVisibility(View.GONE);
                adminFrame.setVisibility(View.GONE);
                line.setVisibility(View.GONE);

            } else

            if (output == 2){

                //set layout
                alertDialog.dismiss();
                noInternetLayout.setVisibility(View.VISIBLE);
                adminBottomNav.setVisibility(View.GONE);
                adminFrame.setVisibility(View.GONE);
                line.setVisibility(View.GONE);

            }

        }).execute();



        /*---   BOTTOM NAV   ---*/
        dueSponsorshipLayout.setOnClickListener(v -> {

            fragmentName.setText("Due Sponsorships");

            dueSponsorshipLayout.setBackgroundResource(R.color.colorPrimaryDark);
            dueSponsorshipImage.setImageResource(R.drawable.ic_due_sponsorships_white);
            dueSponsorshipCounter.setTextColor(getResources().getColor(R.color.white));

            adminNotifyLayout.setBackgroundResource(R.color.white);
            adminNotifyImage.setImageResource(R.drawable.ic_notifications);

            runningSponsorshipLayout.setBackgroundResource(R.color.white);
            runningSponsorshipImage.setImageResource(R.drawable.ic_current_sponsorships_green);
            runningSponsorshipCounter.setTextColor(getResources().getColor(R.color.red));

            adminHistoryLayout.setBackgroundResource(R.color.white);
            adminHistoryImage.setImageResource(R.drawable.ic_history);

            farmManagementLayout.setBackgroundResource(R.color.white);
            farmManagementImage.setImageResource(R.drawable.ic_farm_management_green);

            setFragment(dueSponsorships);
        });

        adminNotifyLayout.setOnClickListener(v -> {

            fragmentName.setText("Send Notifications");

            dueSponsorshipLayout.setBackgroundResource(R.color.white);
            dueSponsorshipImage.setImageResource(R.drawable.ic_due_sponsorships);
            dueSponsorshipCounter.setTextColor(getResources().getColor(R.color.red));

            adminNotifyLayout.setBackgroundResource(R.color.colorPrimaryDark);
            adminNotifyImage.setImageResource(R.drawable.ic_notifications_white);

            runningSponsorshipLayout.setBackgroundResource(R.color.white);
            runningSponsorshipImage.setImageResource(R.drawable.ic_current_sponsorships_green);
            runningSponsorshipCounter.setTextColor(getResources().getColor(R.color.red));

            adminHistoryLayout.setBackgroundResource(R.color.white);
            adminHistoryImage.setImageResource(R.drawable.ic_history);

            farmManagementLayout.setBackgroundResource(R.color.white);
            farmManagementImage.setImageResource(R.drawable.ic_farm_management_green);

            setFragment(adminNotify);

        });

        runningSponsorshipLayout.setOnClickListener(v -> {

            fragmentName.setText("Running Cycles");

            dueSponsorshipLayout.setBackgroundResource(R.color.white);
            dueSponsorshipImage.setImageResource(R.drawable.ic_due_sponsorships);
            dueSponsorshipCounter.setTextColor(getResources().getColor(R.color.red));

            adminNotifyLayout.setBackgroundResource(R.color.white);
            adminNotifyImage.setImageResource(R.drawable.ic_notifications);

            runningSponsorshipLayout.setBackgroundResource(R.color.colorPrimaryDark);
            runningSponsorshipImage.setImageResource(R.drawable.ic_current_sponsorships_white);
            runningSponsorshipCounter.setTextColor(getResources().getColor(R.color.white));

            adminHistoryLayout.setBackgroundResource(R.color.white);
            adminHistoryImage.setImageResource(R.drawable.ic_history);

            farmManagementLayout.setBackgroundResource(R.color.white);
            farmManagementImage.setImageResource(R.drawable.ic_farm_management_green);

            setFragment(runningSponsorships);

        });

        adminHistoryLayout.setOnClickListener(v -> {

            fragmentName.setText("History");

            dueSponsorshipLayout.setBackgroundResource(R.color.white);
            dueSponsorshipImage.setImageResource(R.drawable.ic_due_sponsorships);
            dueSponsorshipCounter.setTextColor(getResources().getColor(R.color.red));

            adminNotifyLayout.setBackgroundResource(R.color.white);
            adminNotifyImage.setImageResource(R.drawable.ic_notifications);

            runningSponsorshipLayout.setBackgroundResource(R.color.white);
            runningSponsorshipImage.setImageResource(R.drawable.ic_current_sponsorships_green);
            runningSponsorshipCounter.setTextColor(getResources().getColor(R.color.red));

            adminHistoryLayout.setBackgroundResource(R.color.colorPrimaryDark);
            adminHistoryImage.setImageResource(R.drawable.ic_history_white);

            farmManagementLayout.setBackgroundResource(R.color.white);
            farmManagementImage.setImageResource(R.drawable.ic_farm_management_green);

            setFragment(adminHistory);

        });

        farmManagementLayout.setOnClickListener(v -> {

            if (paperUser.getEmail().equalsIgnoreCase("bv.softwares@gmail.com")
                    || paperUser.getEmail().equalsIgnoreCase("simonrileyindustries@gmail.com")){

                fragmentName.setText("Farm Management");

                dueSponsorshipLayout.setBackgroundResource(R.color.white);
                dueSponsorshipImage.setImageResource(R.drawable.ic_due_sponsorships);
                dueSponsorshipCounter.setTextColor(getResources().getColor(R.color.red));

                adminNotifyLayout.setBackgroundResource(R.color.white);
                adminNotifyImage.setImageResource(R.drawable.ic_notifications);

                runningSponsorshipLayout.setBackgroundResource(R.color.white);
                runningSponsorshipImage.setImageResource(R.drawable.ic_current_sponsorships_green);
                runningSponsorshipCounter.setTextColor(getResources().getColor(R.color.red));

                adminHistoryLayout.setBackgroundResource(R.color.white);
                adminHistoryImage.setImageResource(R.drawable.ic_history);

                farmManagementLayout.setBackgroundResource(R.color.colorPrimaryDark);
                farmManagementImage.setImageResource(R.drawable.ic_farm_management_white);

                setFragment(farmManagement);

            } else {

                Toast.makeText(AdminDash.this, "Only developer is allowed here", Toast.LENGTH_LONG).show();

            }



        });

        backButton.setOnClickListener(v -> finish());


        fragmentName.setText("Due Sponsorships");
        setBaseFragment(dueSponsorships);

    }

    private void runAllChecks() {

        //remove dialog
        alertDialog.dismiss();

        /*---    COUNTER   ---*/
        dueSponsorshipRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                int dueSponsorCount = (int) dataSnapshot.getChildrenCount();
                dueSponsorshipCounter.setText(String.valueOf(dueSponsorCount));

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        /*---    COUNTER   ---*/
        adminSponsorshipRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                int dueSponsorCount = (int) dataSnapshot.getChildrenCount();
                runningSponsorshipCounter.setText(String.valueOf(dueSponsorCount));

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void setBaseFragment(DueSponsorships dueSponsorships) {

        setFragment(dueSponsorships);

        dueSponsorshipLayout.setBackgroundResource(R.color.colorPrimaryDark);
        dueSponsorshipImage.setImageResource(R.drawable.ic_due_sponsorships_white);
        dueSponsorshipCounter.setTextColor(getResources().getColor(R.color.white));

        adminNotifyLayout.setBackgroundResource(R.color.white);
        adminNotifyImage.setImageResource(R.drawable.ic_notifications);

        runningSponsorshipLayout.setBackgroundResource(R.color.white);
        runningSponsorshipImage.setImageResource(R.drawable.ic_current_sponsorships_green);
        runningSponsorshipCounter.setTextColor(getResources().getColor(R.color.red));

        adminHistoryLayout.setBackgroundResource(R.color.white);
        adminHistoryImage.setImageResource(R.drawable.ic_history);

        farmManagementLayout.setBackgroundResource(R.color.white);
        farmManagementImage.setImageResource(R.drawable.ic_farm_management_green);

    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.adminFrame, fragment);
        fragmentTransaction.commitAllowingStateLoss();
    }

    /*---   LOADING DIALOG   ---*/
    public void showLoadingDialog(String theMessage){

        //loading
        isLoading = true;

        alertDialog = new android.app.AlertDialog.Builder(this).create();
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
        finish();
    }
}
