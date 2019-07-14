package com.blackviking.menorahfarms.CartAndHistory;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.Models.CartModel;
import com.blackviking.menorahfarms.R;
import com.blackviking.menorahfarms.ViewHolders.CartViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Cart extends AppCompatActivity {

    private ImageView backButton;
    private LinearLayout emptyLayout;
    private RecyclerView cartRecycler;
    private FirebaseRecyclerAdapter<CartModel, CartViewHolder> adapter;
    private LinearLayoutManager layoutManager;

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference cartRef, userRef, farmRef, sponsorshipRef;
    private String currentuid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);


        /*---   FIREBASE   ---*/
        cartRef = db.getReference("Carts");
        userRef = db.getReference("Users");
        farmRef = db.getReference("Farms");
        sponsorshipRef = db.getReference("SponsoredFarms");
        if (mAuth.getCurrentUser() != null)
            currentuid = mAuth.getCurrentUser().getUid();


        /*---   WIDGETS   ---*/
        backButton = (ImageView)findViewById(R.id.backButton);
        emptyLayout = (LinearLayout)findViewById(R.id.emptyLayout);
        cartRecycler = (RecyclerView)findViewById(R.id.cartRecycler);


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        /*---   CHECK IF USER CART EMPTY   ---*/
        cartRef.child(currentuid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()){

                            emptyLayout.setVisibility(View.GONE);
                            loadCart();

                        } else {

                            emptyLayout.setVisibility(View.VISIBLE);

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void loadCart() {

        cartRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        cartRecycler.setLayoutManager(layoutManager);

        adapter = new FirebaseRecyclerAdapter<CartModel, CartViewHolder>(
                CartModel.class,
                R.layout.cart_item,
                CartViewHolder.class,
                cartRef.child(currentuid)
        ) {
            @Override
            protected void populateViewHolder(final CartViewHolder viewHolder, final CartModel model, int position) {

                viewHolder.cartItemUnits.setText("X " + String.valueOf(model.getUnits()));

                farmRef.child(model.getFarmId())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                final String theFarmType = dataSnapshot.child("farmType").getValue().toString();
                                String theFarmLocation = dataSnapshot.child("farmLocation").getValue().toString();
                                final String theFarmROI = dataSnapshot.child("farmRoi").getValue().toString();
                                final String theFarmUnitPrice = dataSnapshot.child("pricePerUnit").getValue().toString();
                                final String theFarmSponsorDuration = dataSnapshot.child("sponsorDuration").getValue().toString();
                                final String theFarmImage = dataSnapshot.child("farmImageThumb").getValue().toString();

                                long priceToLong = Long.parseLong(theFarmUnitPrice);

                                viewHolder.cartItemType.setText(theFarmType);
                                viewHolder.cartItemLocation.setText(theFarmLocation);
                                viewHolder.cartItemROI.setText("Return on investment: " + theFarmROI);
                                viewHolder.cartItemDuration.setText("Duration: " + theFarmSponsorDuration);
                                viewHolder.cartItemPrice.setText(Common.convertToPrice(Cart.this, priceToLong));

                                if (!theFarmImage.equalsIgnoreCase("")){

                                    Picasso.with(getBaseContext())
                                            .load(theFarmImage)
                                            .networkPolicy(NetworkPolicy.OFFLINE)
                                            .placeholder(R.drawable.menorah_placeholder)
                                            .into(viewHolder.cartItemImage, new Callback() {
                                                @Override
                                                public void onSuccess() {

                                                }

                                                @Override
                                                public void onError() {
                                                    Picasso.with(getBaseContext())
                                                            .load(theFarmImage)
                                                            .placeholder(R.drawable.menorah_placeholder)
                                                            .into(viewHolder.cartItemImage);
                                                }
                                            });

                                } else {

                                    viewHolder.cartItemImage.setImageResource(R.drawable.menorah_placeholder);

                                }


                                viewHolder.removeFromCart.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        openDeleteDialog(adapter.getRef(viewHolder.getAdapterPosition()).getKey());
                                    }
                                });


                                viewHolder.checkout.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        checkoutAndPay(theFarmType, theFarmROI, theFarmUnitPrice, theFarmSponsorDuration, model.getTotalPayout(), model.getUnits(), model.getFarmId());
                                    }
                                });

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

            }
        };
        cartRecycler.setAdapter(adapter);

    }

    private void checkoutAndPay(String theFarmType, String theFarmROI, String theFarmUnitPrice, String theFarmSponsorDuration, long totalPayout, int units, String farmId) {

        /*---   DURATION   ---*/
        int theDuration = Integer.parseInt(theFarmSponsorDuration);
        int theDays = 30 * theDuration;

        Date todayDate = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        String todayString = formatter.format(todayDate);

        Calendar cal = Calendar.getInstance();
        cal.setTime(todayDate);
        cal.add(Calendar.DATE, theDays);
        Date futureDate = cal.getTime();
        SimpleDateFormat formatterFuture = new SimpleDateFormat("dd-MM-yyyy");
        String futureString = formatterFuture.format(futureDate);


        Map<String, Object> sponsorshipMap = new HashMap<>();
        sponsorshipMap.put("sponsorReturn", String.valueOf(totalPayout));
        sponsorshipMap.put("cycleEndDate", futureString);
        sponsorshipMap.put("cycleStartDate", todayString);
        sponsorshipMap.put("sponsorRefNumber", "Black-Viking-001");
        sponsorshipMap.put("unitPrice", theFarmUnitPrice);
        sponsorshipMap.put("sponsoredUnits", String.valueOf(units));
        sponsorshipMap.put("sponsoredFarmType", theFarmType);
        sponsorshipMap.put("sponsoredFarmRoi", theFarmROI);
        sponsorshipMap.put("sponsorshipDuration", theFarmSponsorDuration);
        sponsorshipMap.put("startPoint", ServerValue.TIMESTAMP);
        sponsorshipMap.put("farmId", farmId);

        sponsorshipRef.child(currentuid)
                .push()
                .setValue(sponsorshipMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(Cart.this, "Success", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Cart.this, "Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void getFutureDate(Date currentDate, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(currentDate);
        cal.add(Calendar.DATE, days);

        Date futureDate = cal.getTime();
    }

    private void openDeleteDialog(final String key) {

        AlertDialog alertDialog = new AlertDialog.Builder(Cart.this)
                .setTitle("Delete Update !")
                .setIcon(R.drawable.ic_remove_from_cart)
                .setMessage("Are You Sure You Want To Remove This Item From Your Cart?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        cartRef.child(currentuid)
                                .child(key)
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        Toast.makeText(Cart.this, "Item Removed From cart !", Toast.LENGTH_SHORT).show();
                                    }
                                });

                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();

        alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;

        alertDialog.show();

    }
}
