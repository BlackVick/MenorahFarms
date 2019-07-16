package com.blackviking.menorahfarms.AdminDetails;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackviking.menorahfarms.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class StudentRequestDetails extends AppCompatActivity {

    private ImageView backButton, studentId;
    private TextView studentName, studentEmail, studentSchool, studentDepartment;
    private Button denyStudent, approveStudent;

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference userRef, studentRef;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_request_details);


        /*---   LOCAL INTENT   ---*/
        userId = getIntent().getStringExtra("UserId");


        /*---   FIREBASE   ---*/
        userRef = db.getReference("Users");
        studentRef = db.getReference("StudentDetails");


        /*---   WIDGETS   ---*/
        backButton = (ImageView)findViewById(R.id.backButton);
        studentId = (ImageView)findViewById(R.id.studentId);
        studentName = (TextView)findViewById(R.id.studentName);
        studentEmail = (TextView)findViewById(R.id.studentEmail);
        studentSchool = (TextView)findViewById(R.id.studentSchool);
        studentDepartment = (TextView)findViewById(R.id.studentDepartment);
        denyStudent = (Button)findViewById(R.id.denyStudent);
        approveStudent = (Button)findViewById(R.id.approveStudent);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        loadStudentRequest();
    }

    private void loadStudentRequest() {

        userRef.child(userId)
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                String firstName = dataSnapshot.child("firstName").getValue().toString();
                                String lastName = dataSnapshot.child("lastName").getValue().toString();
                                String email = dataSnapshot.child("email").getValue().toString();

                                studentName.setText(firstName + " " + lastName);
                                studentEmail.setText(email);

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        }
                );


        studentRef.child(userId)
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                String theSchool = dataSnapshot.child("schoolName").getValue().toString();
                                String theDepartment = dataSnapshot.child("department").getValue().toString();
                                final String theSchoolId = dataSnapshot.child("studentId").getValue().toString();
                                String theSchoolIdThumb = dataSnapshot.child("studentIdThumb").getValue().toString();


                                studentSchool.setText(theSchool);
                                studentDepartment.setText(theDepartment);


                                if (!theSchoolIdThumb.equalsIgnoreCase("")){

                                    Picasso.get()
                                            .load(theSchoolIdThumb)
                                            .into(studentId);

                                    studentId.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                            Uri uri =  Uri.parse(theSchoolId);

                                            Intent intent = new Intent();
                                            intent.setAction(Intent.ACTION_VIEW);
                                            intent.setDataAndType(uri, "image/*");
                                            startActivity(intent);

                                        }
                                    });

                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        }
                );


        approveStudent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                approveStudentRequest();
            }
        });

        denyStudent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                denyStudentRequest();
            }
        });
    }

    private void approveStudentRequest() {

        studentRef.child(userId)
                .child("approval")
                .setValue("approved")
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        sendApprovalNotification();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }

    private void denyStudentRequest() {

        studentRef.child(userId)
                .child("approval")
                .setValue("denied")
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        sendDenialNotification();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }

    private void sendApprovalNotification() {
    }

    private void sendDenialNotification() {
    }
}
