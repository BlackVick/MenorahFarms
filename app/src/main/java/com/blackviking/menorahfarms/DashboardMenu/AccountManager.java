package com.blackviking.menorahfarms.DashboardMenu;

import android.content.ContentProviderOperation;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.provider.ContactsContract;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blackviking.menorahfarms.Common.CheckInternet;
import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.Models.ProjectManagerModel;
import com.blackviking.menorahfarms.Models.UserModel;
import com.blackviking.menorahfarms.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import io.paperdb.Paper;

public class AccountManager extends AppCompatActivity {

    private ImageView backButton;
    private CircleImageView accountManagerAvatar;
    private TextView accountManagerName;
    private Button whatsappManagerButton;
    private RelativeLayout notEmptyLayout;
    private RelativeLayout noInternetLayout;
    private LinearLayout emptyLayout;

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference accountManagerRef;

    private android.app.AlertDialog alertDialog;
    private boolean isLoading = false;
    private UserModel paperUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_manager);


        /*---   FIREBASE   ---*/
        accountManagerRef = db.getReference(Common.ACCOUNT_MANAGERS_NODE);


        /*---   WIDGETS   ---*/
        backButton = findViewById(R.id.backButton);
        accountManagerAvatar = findViewById(R.id.accountManagerAvatar);
        accountManagerName = findViewById(R.id.accountManagerName);
        whatsappManagerButton = findViewById(R.id.whatsappManagerButton);
        notEmptyLayout = findViewById(R.id.notEmptyLayout);
        emptyLayout = findViewById(R.id.emptyLayout);
        noInternetLayout = findViewById(R.id.noInternetLayout);


        //show loading dialog
        showLoadingDialog("Loading farm manager . . .");


        //current user
        paperUser = Paper.book().read(Common.PAPER_USER);

        if (!paperUser.getAccountManager().equalsIgnoreCase("")){

            //run network check
            new CheckInternet(this, output -> {

                //check all cases
                if (output == 1){

                    notEmptyLayout.setVisibility(View.VISIBLE);
                    emptyLayout.setVisibility(View.GONE);
                    noInternetLayout.setVisibility(View.GONE);

                    loadAccountManager(paperUser.getAccountManager());

                } else

                if (output == 0){

                    //set layout
                    alertDialog.dismiss();
                    noInternetLayout.setVisibility(View.VISIBLE);
                    notEmptyLayout.setVisibility(View.GONE);
                    emptyLayout.setVisibility(View.GONE);

                } else

                if (output == 2){

                    //set layout
                    alertDialog.dismiss();
                    noInternetLayout.setVisibility(View.VISIBLE);
                    notEmptyLayout.setVisibility(View.GONE);
                    emptyLayout.setVisibility(View.GONE);

                }

            }).execute();

        } else {

            alertDialog.dismiss();
            notEmptyLayout.setVisibility(View.GONE);
            emptyLayout.setVisibility(View.VISIBLE);
            noInternetLayout.setVisibility(View.GONE);

        }

        //back button
        backButton.setOnClickListener(v -> finish());
    }

    private void loadAccountManager(String theManager) {

        accountManagerRef.child(theManager)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        ProjectManagerModel currentManager = dataSnapshot.getValue(ProjectManagerModel.class);

                        if (currentManager != null){

                            alertDialog.dismiss();

                            final String theName = currentManager.getName();
                            final String theProfilePic = currentManager.getProfilePicture();
                            final String theWhatsapp = currentManager.getWhatsapp();

                            //manager name
                            accountManagerName.setText("Hi there, my name is " + theName + " and I would be your Project Manager.");

                            //manager avatar
                            if (!theProfilePic.equalsIgnoreCase("")){

                                Picasso.get()
                                        .load(theProfilePic)
                                        .networkPolicy(NetworkPolicy.OFFLINE)
                                        .placeholder(R.drawable.profile)
                                        .into(accountManagerAvatar, new Callback() {
                                            @Override
                                            public void onSuccess() {

                                            }

                                            @Override
                                            public void onError(Exception e) {
                                                Picasso.get()
                                                        .load(theProfilePic)
                                                        .placeholder(R.drawable.profile)
                                                        .into(accountManagerAvatar);
                                            }
                                        });

                            }

                            whatsappManagerButton.setOnClickListener(v -> {

                                Intent i = new Intent(Intent.ACTION_VIEW);
                                i.setData(Uri.parse(theWhatsapp));
                                startActivity(i);

                            });

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

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
