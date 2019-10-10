package com.blackviking.menorahfarms.DashboardMenu;

import android.content.ContentProviderOperation;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
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
import com.blackviking.menorahfarms.HomeActivities.FarmShop;
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

import org.w3c.dom.Text;

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
    private DatabaseReference userRef, accountManagerRef;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String currentUid;

    private android.app.AlertDialog alertDialog;
    private UserModel paperUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_manager);


        /*---   FIREBASE   ---*/
        userRef = db.getReference("Users");
        accountManagerRef = db.getReference("AccountManagers");
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();


        /*---   WIDGETS   ---*/
        backButton = (ImageView)findViewById(R.id.backButton);
        accountManagerAvatar = (CircleImageView) findViewById(R.id.accountManagerAvatar);
        accountManagerName = (TextView) findViewById(R.id.accountManagerName);
        whatsappManagerButton = (Button)findViewById(R.id.whatsappManagerButton);
        notEmptyLayout = (RelativeLayout) findViewById(R.id.notEmptyLayout);
        emptyLayout = (LinearLayout) findViewById(R.id.emptyLayout);
        noInternetLayout = findViewById(R.id.noInternetLayout);


        //show loading dialog
        showLoadingDialog("Loading farm manager . . .");


        //current user
        paperUser = Paper.book().read(Common.PAPER_USER);

        if (!paperUser.getAccountManager().equalsIgnoreCase("")){

            //execute network check async task
            CheckInternet asyncTask = (CheckInternet) new CheckInternet(this, new CheckInternet.AsyncResponse(){
                @Override
                public void processFinish(Integer output) {

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

                }
            }).execute();

        } else {

            alertDialog.dismiss();
            notEmptyLayout.setVisibility(View.GONE);
            emptyLayout.setVisibility(View.VISIBLE);
            noInternetLayout.setVisibility(View.GONE);

        }




        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
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

                            accountManagerName.setText("Hi there, my name is " + theName + " and I would be your Project Manager.");

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

                            whatsappManagerButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    Intent i = new Intent(Intent.ACTION_VIEW);
                                    i.setData(Uri.parse(theWhatsapp));
                                    startActivity(i);

                                }
                            });

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }

    private void addTelegramContact(String theWhatsapp, String theName) {

        String DisplayName = theName;
        String MobileNumber = theWhatsapp;
        String company = "Menorah Farms";
        String jobTitle = "Project Manager";

        ArrayList<ContentProviderOperation> ops = new ArrayList < ContentProviderOperation > ();

        ops.add(ContentProviderOperation.newInsert(
                ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());

        //------------------------------------------------------ Names
        if (DisplayName != null) {
            ops.add(ContentProviderOperation.newInsert(
                    ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(
                            ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                            DisplayName).build());
        }

        //------------------------------------------------------ Mobile Number
        if (MobileNumber != null) {
            ops.add(ContentProviderOperation.
                    newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, MobileNumber)
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                            ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                    .build());
        }

        //------------------------------------------------------ Organization
        if (!company.equals("") && !jobTitle.equals("")) {
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, company)
                    .withValue(ContactsContract.CommonDataKinds.Organization.TYPE, ContactsContract.CommonDataKinds.Organization.TYPE_WORK)
                    .withValue(ContactsContract.CommonDataKinds.Organization.TITLE, jobTitle)
                    .withValue(ContactsContract.CommonDataKinds.Organization.TYPE, ContactsContract.CommonDataKinds.Organization.TYPE_WORK)
                    .build());
        }

        // Asking the Contact provider to create a new contact
        try {
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(AccountManager.this, "Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

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
