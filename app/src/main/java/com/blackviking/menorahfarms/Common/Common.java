package com.blackviking.menorahfarms.Common;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;

import com.blackviking.menorahfarms.Models.UserModel;
import com.blackviking.menorahfarms.Notification.APIService;
import com.blackviking.menorahfarms.Notification.RetrofitClient;

import java.text.NumberFormat;

import io.paperdb.Paper;

public class Common {

    /*---   ACCOUNT INFO   ---*/
    public static final String USER_ID = "User";
    public static final String SIGN_UP_CHOICE = "Choice";
    public static final String GOOD_TO_GO = "GoToGo";
    public static final String PROFILE_WARNING_COUNT = "ProfileWarningCount";
    public static final String PAPER_USER = "PAPER_USER";


    /*---   DATABASE NODES   ---*/
    public static final String USERS_NODE = "Users";
    public static final String AUTHED_USERS_NODE = "AuthedUsers";
    public static final String FARM_NODE = "Farms";
    public static final String CART_NODE = "Carts";
    public static final String BANKS_NODE = "Banks";
    public static final String FARM_UPDATES_NODE = "FarmUpdates";
    public static final String HISTORY_NODE = "History";
    public static final String ADMIN_HISTORY_NODE = "AdminHistory";
    public static final String ACCOUNT_MANAGERS_NODE = "AccountManagers";
    public static final String NOTIFICATIONS_NODE = "Notifications";
    public static final String SPONSORED_FARMS_NODE = "SponsoredFarms";
    public static final String FOLLOWED_FARMS_NODE = "FollowedFarms";
    public static final String FOLLOWED_FARMS_NOTIFICATION_NODE = "FollowedFarmsNotification";
    public static final String SPONSORED_FARMS_NOTIFICATION_NODE = "SponsoredFarmsNotification";
    public static final String RUNNING_CYCLE_NODE = "RunningCycles";
    public static final String DUE_SPONSORSHIPS_NODE = "DueSponsorships";
    public static final String SPONSORSHIP_DETAILS_NODE = "SponsorshipDetails";
    public static final String TRANSACTION_NODE = "TransactionHistory";
    public static final String TERMS_AND_CONDITIONS_NODE = "TermsAndConditions";


    /*---   FLUTTERWAVE   ---*/
    public static final String flutterWavePublicKey = "FLWPUBK-bb6cf62f9e07f4b7f1028699eaa58873-X";
    public static final String flutterWaveEncryptionKey = "0b17c443fa8aba1186a42910";


    /*---   FARM MANAGEMENT   ---*/
    public static String isFarmServiceRunning = "FarmMonitor";


    /*---   NOTIFICATION   ---*/
    public static String ADMIN_MESSAGE = "ADMIN_MESSAGE_TOPIC";
    public static final String GENERAL_NOTIFY = "menorahtopic";


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


    /*---   CHECK KYC    ---*/
    public static String checkKYC (Context context){

        String result = "";

        UserModel currentUser = Paper.book().read(Common.PAPER_USER);

        if (currentUser != null){

            if (currentUser.getPhone().equals("")
                    || currentUser.getGender().equals("") || currentUser.getBirthday().equals("")){

                result = "Personal details incomplete!";

            } else

            if (currentUser.getAddress().equals("") || currentUser.getCity().equals("")
                    || currentUser.getState().equals("")){

                result = "Contact details incomplete!";

            } else

            if (currentUser.getBank().equals("") || currentUser.getAccountName().equals("")
                    || currentUser.getAccountNumber().equals("")){

                result = "Bank details incomplete!";

            } else {

                result = "Profile Complete";

            }

        } else {

             result = "Null User";

        }

        return result;
    }
}
