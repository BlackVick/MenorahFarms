package com.blackviking.menorahfarms;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blackviking.menorahfarms.AdminFragments.AdminHistory;
import com.blackviking.menorahfarms.AdminFragments.AdminNotify;
import com.blackviking.menorahfarms.AdminFragments.DueSponsorships;
import com.blackviking.menorahfarms.AdminFragments.StudentRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminDash extends AppCompatActivity {

    private ImageView backButton;
    private TextView fragmentName;

    private RelativeLayout dueSponsorshipLayout, studentApplyLayout, adminNotifyLayout, adminHistoryLayout;
    private ImageView dueSponsorshipImage, studentApplyImage, adminNotifyImage, adminHistoryImage;
    private TextView dueSponsorshipCounter, studentApplyCounter;

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference studentRef, dueSponsorshipRef, notificationRef, userRef, adminHistoryRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dash);


        /*---   FIREBASE   ---*/
        studentRef = db.getReference("StudentDetails");
        dueSponsorshipRef = db.getReference("DueSponsorships");
        notificationRef = db.getReference("Notifications");
        userRef = db.getReference("Users");
        adminHistoryRef = db.getReference("AdminHistory");


        /*---   WIDGETS   ---*/
        backButton = (ImageView)findViewById(R.id.backButton);
        fragmentName = (TextView)findViewById(R.id.fragmentName);

        dueSponsorshipLayout = (RelativeLayout)findViewById(R.id.dueSponsorshipLayout);
        studentApplyLayout = (RelativeLayout)findViewById(R.id.studentApplyLayout);
        adminNotifyLayout = (RelativeLayout)findViewById(R.id.adminNotifyLayout);
        adminHistoryLayout = (RelativeLayout)findViewById(R.id.adminHistoryLayout);
        dueSponsorshipImage = (ImageView)findViewById(R.id.dueSponsorshipImage);
        studentApplyImage = (ImageView)findViewById(R.id.studentApplyImage);
        adminNotifyImage = (ImageView)findViewById(R.id.adminNotifyImage);
        adminHistoryImage = (ImageView)findViewById(R.id.adminHistoryImage);
        dueSponsorshipCounter = (TextView)findViewById(R.id.dueSponsorshipCounter);
        studentApplyCounter = (TextView)findViewById(R.id.studentApplyCounter);



        /*---   FRAGMENTS   ---*/
        final DueSponsorships dueSponsorships = new DueSponsorships();
        final StudentRequest studentRequest = new StudentRequest();
        final AdminNotify adminNotify = new AdminNotify();
        final AdminHistory adminHistory = new AdminHistory();


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

                adminHistoryLayout.setBackgroundResource(R.color.white);
                adminHistoryImage.setImageResource(R.drawable.ic_history);

                setFragment(adminNotify);

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

        adminHistoryLayout.setBackgroundResource(R.color.white);
        adminHistoryImage.setImageResource(R.drawable.ic_history);

    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.adminFrame, fragment);
        fragmentTransaction.commitAllowingStateLoss();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
