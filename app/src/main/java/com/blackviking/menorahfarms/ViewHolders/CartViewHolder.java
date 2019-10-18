package com.blackviking.menorahfarms.ViewHolders;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackviking.menorahfarms.R;

public class CartViewHolder extends RecyclerView.ViewHolder {

    public ImageView cartItemImage;
    public TextView cartItemType, cartItemLocation, cartItemROI, cartItemDuration, cartItemPrice, cartItemUnits, removeFromCart;
    public Button checkout;

    public CartViewHolder(@NonNull View itemView) {
        super(itemView);

        cartItemImage = (ImageView)itemView.findViewById(R.id.cartItemImage);
        removeFromCart = (TextView) itemView.findViewById(R.id.removeFromCart);
        cartItemType = (TextView)itemView.findViewById(R.id.cartItemType);
        cartItemLocation = (TextView)itemView.findViewById(R.id.cartItemLocation);
        cartItemROI = (TextView)itemView.findViewById(R.id.cartItemROI);
        cartItemDuration = (TextView)itemView.findViewById(R.id.cartItemDuration);
        cartItemPrice = (TextView)itemView.findViewById(R.id.cartItemPrice);
        cartItemUnits = (TextView)itemView.findViewById(R.id.cartItemUnits);
        checkout = (Button)itemView.findViewById(R.id.checkout);

    }
}
