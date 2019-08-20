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
import android.widget.Toast;

import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.Models.StudentModel;
import com.blackviking.menorahfarms.Models.UserModel;
import com.blackviking.menorahfarms.Notification.APIService;
import com.blackviking.menorahfarms.Notification.DataMessage;
import com.blackviking.menorahfarms.Notification.MyResponse;
import com.blackviking.menorahfarms.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

public class StudentRequestDetails extends AppCompatActivity {

    private ImageView backButton, studentId;
    private TextView studentName, studentEmail, studentSchool, studentDepartment;
    private Button denyStudent, approveStudent;

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference userRef, studentRef, notificationRef;
    private String userId;
    private APIService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_request_details);


        /*---   LOCAL INTENT   ---*/
        userId = getIntent().getStringExtra("UserId");


        /*---   FCM   ---*/
        mService = Common.getFCMService();


        /*---   FIREBASE   ---*/
        userRef = db.getReference("Users");
        studentRef = db.getReference("StudentDetails");
        notificationRef = db.getReference("Notifications");


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

                                UserModel currentUser = dataSnapshot.getValue(UserModel.class);

                                if (currentUser != null){

                                    studentName.setText(currentUser.getFirstName() + " " + currentUser.getLastName());
                                    studentEmail.setText(currentUser.getEmail());

                                }

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

                                final StudentModel currentStudent = dataSnapshot.getValue(StudentModel.class);

                                if (currentStudent != null){

                                    studentSchool.setText(currentStudent.getSchoolName());
                                    studentDepartment.setText(currentStudent.getDepartment());


                                    if (!currentStudent.getStudentIdThumb().equalsIgnoreCase("")){

                                        Picasso.get()
                                                .load(currentStudent.getStudentIdThumb())
                                                .into(studentId);

                                        studentId.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                                Uri uri =  Uri.parse(currentStudent.getStudentIdThumb());

                                                Intent intent = new Intent();
                                                intent.setAction(Intent.ACTION_VIEW);
                                                intent.setDataAndType(uri, "image/*");
                                                startActivity(intent);

                                            }
                                        });

                                    }

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
                        userRef.child(userId)
                                .child("userPackage")
                                .setValue("Student")
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

        final Date todayDate = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy  hh:mm");
        String todayString = formatter.format(todayDate);

        Map<String, Object> notificationMap = new HashMap<>();
        notificationMap.put("topic", "Student Approval");
        notificationMap.put("message", "Your student details have been confirmed. You can now sponsor AcadaCash farms as an undergraduate.");
        notificationMap.put("time", todayString);


        notificationRef.child(userId)
                .push()
                .setValue(notificationMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Map<String, String> dataSend = new HashMap<>();
                        dataSend.put("title", "Student");
                        dataSend.put("message", "Your student details have been confirmed. You can now sponsor AcadaCash farms as an undergraduate.");
                        DataMessage dataMessage = new DataMessage(new StringBuilder("/topics/").append(userId).toString(), dataSend);

                        mService.sendNotification(dataMessage)
                                .enqueue(new retrofit2.Callback<MyResponse>() {
                                    @Override
                                    public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {

                                    }

                                    @Override
                                    public void onFailure(Call<MyResponse> call, Throwable t) {
                                    }
                                });
                    }
                });

        finish();

    }

    private void sendDenialNotification() {

        final Date todayDate = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy  hh:mm");
        String todayString = formatter.format(todayDate);

        Map<String, Object> notificationMap = new HashMap<>();
        notificationMap.put("topic", "Student Denial");
        notificationMap.put("message", "Your student details have been denied due to invalid documents. Please contact us if you feel this is wrong.");
        notificationMap.put("time", todayString);


        notificationRef.child(userId)
                .push()
                .setValue(notificationMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Map<String, String> dataSend = new HashMap<>();
                        dataSend.put("title", "Student");
                        dataSend.put("message", "Your student details have been denied due to invalid documents. Please contact us if you feel this is wrong.");
                        DataMessage dataMessage = new DataMessage(new StringBuilder("/topics/").append(userId).toString(), dataSend);

                        mService.sendNotification(dataMessage)
                                .enqueue(new retrofit2.Callback<MyResponse>() {
                                    @Override
                                    public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {

                                    }

                                    @Override
                                    public void onFailure(Call<MyResponse> call, Throwable t) {
                                    }
                                });
                    }
                });

        finish();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
