package com.blackviking.menorahfarms;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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
import com.blackviking.menorahfarms.AdminFragments.RunningSponsorships;
import com.blackviking.menorahfarms.AdminFragments.StudentRequest;
import com.blackviking.menorahfarms.Common.CheckInternet;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminDash extends AppCompatActivity {

    private ImageView backButton;
    private TextView fragmentName;

    private RelativeLayout dueSponsorshipLayout, studentApplyLayout, adminNotifyLayout, runningSponsorshipLayout, adminHistoryLayout;
    private ImageView dueSponsorshipImage, studentApplyImage, adminNotifyImage, runningSponsorshipImage, adminHistoryImage;
    private TextView dueSponsorshipCounter, studentApplyCounter, runningSponsorshipCounter;

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference studentRef, dueSponsorshipRef, notificationRef, userRef, adminSponsorshipRef;


    private RelativeLayout noInternetLayout, adminBottomNav;
    private View line;
    private FrameLayout adminFrame;

    private android.app.AlertDialog alertDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dash);


        /*---   FIREBASE   ---*/
        studentRef = db.getReference("StudentDetails");
        dueSponsorshipRef = db.getReference("DueSponsorships");
        notificationRef = db.getReference("Notifications");
        userRef = db.getReference("Users");
        adminSponsorshipRef = db.getReference("RunningCycles");


        /*---   WIDGETS   ---*/
        backButton = (ImageView)findViewById(R.id.backButton);
        fragmentName = (TextView)findViewById(R.id.fragmentName);

        dueSponsorshipLayout = (RelativeLayout)findViewById(R.id.dueSponsorshipLayout);
        studentApplyLayout = (RelativeLayout)findViewById(R.id.studentApplyLayout);
        adminNotifyLayout = (RelativeLayout)findViewById(R.id.adminNotifyLayout);
        runningSponsorshipLayout = (RelativeLayout)findViewById(R.id.runningSponsorshipLayout);
        adminHistoryLayout = (RelativeLayout)findViewById(R.id.adminHistoryLayout);
        dueSponsorshipImage = (ImageView)findViewById(R.id.dueSponsorshipImage);
        studentApplyImage = (ImageView)findViewById(R.id.studentApplyImage);
        adminNotifyImage = (ImageView)findViewById(R.id.adminNotifyImage);
        runningSponsorshipImage = (ImageView)findViewById(R.id.runningSponsorshipImage);
        adminHistoryImage = (ImageView)findViewById(R.id.adminHistoryImage);
        dueSponsorshipCounter = (TextView)findViewById(R.id.dueSponsorshipCounter);
        studentApplyCounter = (TextView)findViewById(R.id.studentApplyCounter);
        runningSponsorshipCounter = (TextView)findViewById(R.id.runningSponsorshipCounter);

        noInternetLayout = findViewById(R.id.noInternetLayout);
        adminBottomNav = findViewById(R.id.adminBottomNav);
        adminFrame = findViewById(R.id.adminFrame);
        line = findViewById(R.id.line);



        /*---   FRAGMENTS   ---*/
        final DueSponsorships dueSponsorships = new DueSponsorships();
        final StudentRequest studentRequest = new StudentRequest();
        final AdminNotify adminNotify = new AdminNotify();
        final RunningSponsorships runningSponsorships = new RunningSponsorships();
        final AdminHistory adminHistory = new AdminHistory();


        //show loading dialog
        showLoadingDialog("Loading admin dashboard . . .");

        //execute network check async task
        CheckInternet asyncTask = (CheckInternet) new CheckInternet(this, new CheckInternet.AsyncResponse(){
            @Override
            public void processFinish(Integer output) {

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

            }
        }).execute();



        /*---   BOTTOM NAV   ---*/
        dueSponsorshipLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                fragmentName.setText("Due Sponsorships");

                dueSponsorshipLayout.setBackgroundResource(R.color.colorPrimaryDark);
                dueSponsorshipImage.setImageResource(R.drawable.ic_due_sponsorships_white);
                dueSponsorshipCounter.setTextColor(getResources().getColor(R.color.white));

                studentApplyLayout.setBackgroundResource(R.color.white);
                studentApplyImage.setImageResource(R.drawable.ic_student_request);
                studentApplyCounter.setTextColor(getResources().getColor(R.color.red));

                adminNotifyLayout.setBackgroundResource(R.color.white);
                adminNotifyImage.setImageResource(R.drawable.ic_notifications);

                runningSponsorshipLayout.setBackgroundResource(R.color.white);
                runningSponsorshipImage.setImageResource(R.drawable.ic_current_sponsorships_green);
                runningSponsorshipCounter.setTextColor(getResources().getColor(R.color.red));

                adminHistoryLayout.setBackgroundResource(R.color.white);
                adminHistoryImage.setImageResource(R.drawable.ic_history);

                setFragment(dueSponsorships);
            }
        });

        studentApplyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                fragmentName.setText("Student Request");

                dueSponsorshipLayout.setBackgroundResource(R.color.white);
                dueSponsorshipImage.setImageResource(R.drawable.ic_due_sponsorships);
                dueSponsorshipCounter.setTextColor(getResources().getColor(R.color.red));

                studentApplyLayout.setBackgroundResource(R.color.colorPrimaryDark);
                studentApplyImage.setImageResource(R.drawable.ic_student_request_white);
                studentApplyCounter.setTextColor(getResources().getColor(R.color.white));

                adminNotifyLayout.setBackgroundResource(R.color.white);
                adminNotifyImage.setImageResource(R.drawable.ic_notifications);

                runningSponsorshipLayout.setBackgroundResource(R.color.white);
                runningSponsorshipImage.setImageResource(R.drawable.ic_current_sponsorships_green);
                runningSponsorshipCounter.setTextColor(getResources().getColor(R.color.red));

                adminHistoryLayout.setBackgroundResource(R.color.white);
                adminHistoryImage.setImageResource(R.drawable.ic_history);

                setFragment(studentRequest);

            }
        });

        adminNotifyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                fragmentName.setText("Send Notifications");

                dueSponsorshipLayout.setBackgroundResource(R.color.white);
                dueSponsorshipImage.setImageResource(R.drawable.ic_due_sponsorships);
                dueSponsorshipCounter.setTextColor(getResources().getColor(R.color.red));

                studentApplyLayout.setBackgroundResource(R.color.white);
                studentApplyImage.setImageResource(R.drawable.ic_student_request);
                studentApplyCounter.setTextColor(getResources().getColor(R.color.red));

                adminNotifyLayout.setBackgroundResource(R.color.colorPrimaryDark);
                adminNotifyImage.setImageResource(R.drawable.ic_notifications_white);

                runningSponsorshipLayout.setBackgroundResource(R.color.white);
                runningSponsorshipImage.setImageResource(R.drawable.ic_current_sponsorships_green);
                runningSponsorshipCounter.setTextColor(getResources().getColor(R.color.red));

                adminHistoryLayout.setBackgroundResource(R.color.white);
                adminHistoryImage.setImageResource(R.drawable.ic_history);

                setFragment(adminNotify);

            }
        });

        runningSponsorshipLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                fragmentName.setText("Running Cycles");

                dueSponsorshipLayout.setBackgroundResource(R.color.white);
                dueSponsorshipImage.setImageResource(R.drawable.ic_due_sponsorships);
                dueSponsorshipCounter.setTextColor(getResources().getColor(R.color.red));

                studentApplyLayout.setBackgroundResource(R.color.white);
                studentApplyImage.setImageResource(R.drawable.ic_student_request);
                studentApplyCounter.setTextColor(getResources().getColor(R.color.red));

                adminNotifyLayout.setBackgroundResource(R.color.white);
                adminNotifyImage.setImageResource(R.drawable.ic_notifications);

                runningSponsorshipLayout.setBackgroundResource(R.color.colorPrimaryDark);
                runningSponsorshipImage.setImageResource(R.drawable.ic_current_sponsorships_white);
                runningSponsorshipCounter.setTextColor(getResources().getColor(R.color.white));

                adminHistoryLayout.setBackgroundResource(R.color.white);
                adminHistoryImage.setImageResource(R.drawable.ic_history);

                setFragment(runningSponsorships);

            }
        });

        adminHistoryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                fragmentName.setText("History");

                dueSponsorshipLayout.setBackgroundResource(R.color.white);
                dueSponsorshipImage.setImageResource(R.drawable.ic_due_sponsorships);
                dueSponsorshipCounter.setTextColor(getResources().getColor(R.color.red));

                studentApplyLayout.setBackgroundResource(R.color.white);
                studentApplyImage.setImageResource(R.drawable.ic_student_request);
                studentApplyCounter.setTextColor(getResources().getColor(R.color.red));

                adminNotifyLayout.setBackgroundResource(R.color.white);
                adminNotifyImage.setImageResource(R.drawable.ic_notifications);

                runningSponsorshipLayout.setBackgroundResource(R.color.white);
                runningSponsorshipImage.setImageResource(R.drawable.ic_current_sponsorships_green);
                runningSponsorshipCounter.setTextColor(getResources().getColor(R.color.red));

                adminHistoryLayout.setBackgroundResource(R.color.colorPrimaryDark);
                adminHistoryImage.setImageResource(R.drawable.ic_history_white);

                setFragment(adminHistory);

            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


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

        studentRef.orderByChild("approval")
                .equalTo("pending")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        int studentCount = (int) dataSnapshot.getChildrenCount();
                        studentApplyCounter.setText(String.valueOf(studentCount));

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

        studentApplyLayout.setBackgroundResource(R.color.white);
        studentApplyImage.setImageResource(R.drawable.ic_student_request);
        studentApplyCounter.setTextColor(getResources().getColor(R.color.red));

        adminNotifyLayout.setBackgroundResource(R.color.white);
        adminNotifyImage.setImageResource(R.drawable.ic_notifications);

        runningSponsorshipLayout.setBackgroundResource(R.color.white);
        runningSponsorshipImage.setImageResource(R.drawable.ic_current_sponsorships_green);
        runningSponsorshipCounter.setTextColor(getResources().getColor(R.color.red));

        adminHistoryLayout.setBackgroundResource(R.color.white);
        adminHistoryImage.setImageResource(R.drawable.ic_history);

    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.adminFrame, fragment);
        fragmentTransaction.commitAllowingStateLoss();
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
        finish();
    }
}
