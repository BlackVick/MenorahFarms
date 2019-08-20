package com.blackviking.menorahfarms.AdminFragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blackviking.menorahfarms.AdminDetails.DueSponsorshipDetail;
import com.blackviking.menorahfarms.AdminDetails.StudentRequestDetails;
import com.blackviking.menorahfarms.Interface.ItemClickListener;
import com.blackviking.menorahfarms.Models.StudentModel;
import com.blackviking.menorahfarms.Models.UserModel;
import com.blackviking.menorahfarms.R;
import com.blackviking.menorahfarms.ViewHolders.AdminViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * A simple {@link Fragment} subclass.
 */
public class StudentRequest extends Fragment {

    private RecyclerView studentRequestRecycler;

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference studentRequestRef, userRef;

    private LinearLayoutManager layoutManager;
    private FirebaseRecyclerAdapter<StudentModel, AdminViewHolder> adapter;

    public StudentRequest() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_student_request, container, false);


        /*---   FIREBASE   ---*/
        studentRequestRef = db.getReference("StudentDetails");
        userRef = db.getReference("Users");


        /*---   WIDGETS   ---*/
        studentRequestRecycler = (RecyclerView)v.findViewById(R.id.studentRequestRecycler);


        loadStudentRequests();

        return v;
    }

    private void loadStudentRequests() {

        studentRequestRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        studentRequestRecycler.setLayoutManager(layoutManager);


        adapter = new FirebaseRecyclerAdapter<StudentModel, AdminViewHolder>(
                StudentModel.class,
                R.layout.admin_item,
                AdminViewHolder.class,
                studentRequestRef.orderByChild("approval")
                .equalTo("pending")
        ) {
            @Override
            protected void populateViewHolder(final AdminViewHolder viewHolder, StudentModel model, int position) {

                final String theUserId = adapter.getRef(viewHolder.getAdapterPosition()).getKey();

                userRef.child(theUserId)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                UserModel currentUser = dataSnapshot.getValue(UserModel.class);

                                if (currentUser != null){

                                    viewHolder.itemIdentification.setText(currentUser.getEmail());

                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent intent = new Intent(getContext(), StudentRequestDetails.class);
                        intent.putExtra("UserId", theUserId);
                        startActivity(intent);
                    }
                });

            }
        };
        studentRequestRecycler.setAdapter(adapter);
    }

}
