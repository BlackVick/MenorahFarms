package com.blackviking.menorahfarms.Common;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.blackviking.menorahfarms.AccountMenus.StudentDetails;
import com.blackviking.menorahfarms.Notification.APIService;
import com.blackviking.menorahfarms.Notification.RetrofitClient;
import com.blackviking.menorahfarms.R;

import java.text.NumberFormat;
import java.util.Locale;

public class Common {

    /*---   ACCOUNT INFO   ---*/
    public static final String USER_ID = "User";
    public static final String SIGN_UP_CHOICE = "Choice";
    public static final String GOOD_TO_GO = "GoToGo";
    public static final String PROFILE_WARNING_COUNT = "ProfileWarningCount";


    /*---   FLUTTERWAVE   ---*/
    public static final String flutterWavePublicKey = "FLWPUBK-bb6cf62f9e07f4b7f1028699eaa58873-X";
    public static final String flutterWaveEncryptionKey = "0b17c443fa8aba1186a42910";


    /*---   SUBSCRIPTION TOGGLE   ---*/
    public static String isSponsorshipMonitorRunning = "SponsorMonitor";


    /*---   CONTEXT MENU   ---*/
    public static final String DELETE_BOTH = "Retract Message";
    public static final String DELETE_SINGLE = "Delete Message";


    /*---   DEVICE ONLINE OFFLINE   ---*/
    public static final String APP_STATE = "State";


    /*---   NOTIFICATION   ---*/
    public static final String NOTIFICATION_STATE = "Notification";
    public static String FEED_NOTIFICATION_STATE = "Feed";
    public static String MY_FEED_NOTIFICATION_STATE = "MyFeed";
    public static String GAMERS_NOTIFICATION_STATE = "Gamers";
    public static String SKIT_NOTIFICATION_STATE = "Skit";


    public static String ADMIN_MESSAGE = "ADMIN_MESSAGE_TOPIC";
    public static final String GENERAL_NOTIFY = "menorahtopic";
    public static String FEED_NOTIFICATION_TOPIC = "Feed";
    public static String GAMERS_NOTIFICATION_TOPIC = "Gamers";
    public static String SKIT_NOTIFICATION_TOPIC = "Skits";


    private static final String BASE_URL = "https://fcm.googleapis.com/";
    public static APIService getFCMService()    {
        return RetrofitClient.getClient(BASE_URL).create(APIService.class);
    }


    /*---   CONVERT LONG TO PRICE   ---*/
    public static String convertToPrice(Context context, long thePrice){

        NumberFormat n = NumberFormat.getCurrencyInstance(new java.util.Locale("en","ng"));
        String convertedPrice = n.format(thePrice);
        return convertedPrice;

    }


    /*---   CHECK FOR INTERNET   ---*/
    public static boolean isConnectedToInternet(Context context)    {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null)
        {
            NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
            if (info != null)
            {
                for (int i = 0; i<info.length; i++)
                {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                        return true;
                }
            }
        }
        return false;
    }


}
