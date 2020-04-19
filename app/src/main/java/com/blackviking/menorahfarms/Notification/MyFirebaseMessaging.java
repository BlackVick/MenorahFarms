package com.blackviking.menorahfarms.Notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import com.blackviking.menorahfarms.DashboardMenu.Notifications;
import com.blackviking.menorahfarms.HomeActivities.Dashboard;
import com.blackviking.menorahfarms.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import static com.blackviking.menorahfarms.Common.ApplicationClass.CHANNEL_2_ID;

public class MyFirebaseMessaging extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getData() != null) {

            sendNotificationAPI26(remoteMessage);

        }
    }

    private void sendNotificationAPI26(RemoteMessage remoteMessage) {

        Map<String, String> data = remoteMessage.getData();
        String title = data.get("title");
        String message = data.get("message");


        /*---   MAIN NOTIFICATION LOGIC   ---*/
        if (title.equalsIgnoreCase("Sponsorship")) {

            Intent notificationsIntent = new Intent(this, Dashboard.class);
            PendingIntent contentIntent = PendingIntent.getActivity(this,
                    0, notificationsIntent, 0);


            Notification notification = new NotificationCompat.Builder(this, CHANNEL_2_ID)
                    .setSmallIcon(R.drawable.ic_sponsored_notification)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setColor(getResources().getColor(R.color.colorPrimaryDark))
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true)
                    .build();

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(2, notification);

        } else

            if (title.equalsIgnoreCase("Menorah Farms")) {

            Intent notificationsIntent = new Intent(this, Notifications.class);
            PendingIntent contentIntent = PendingIntent.getActivity(this,
                    0, notificationsIntent, 0);


            Notification notification = new NotificationCompat.Builder(this, CHANNEL_2_ID)
                    .setSmallIcon(R.drawable.ic_menorah_notification)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setColor(getResources().getColor(R.color.colorPrimaryDark))
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true)
                    .build();

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(1, notification);

        } else if (title.equalsIgnoreCase("Sponsorship End")) {

            Intent notificationsIntent = new Intent(this, Dashboard.class);
            PendingIntent contentIntent = PendingIntent.getActivity(this,
                    0, notificationsIntent, 0);


            Notification notification = new NotificationCompat.Builder(this, CHANNEL_2_ID)
                    .setSmallIcon(R.drawable.ic_sponsored_notification)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setColor(getResources().getColor(R.color.colorPrimaryDark))
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true)
                    .build();

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(4, notification);

        } else if (title.equalsIgnoreCase("Sponsorship Start")) {

            Intent notificationsIntent = new Intent(this, Dashboard.class);
            PendingIntent contentIntent = PendingIntent.getActivity(this,
                    0, notificationsIntent, 0);


            Notification notification = new NotificationCompat.Builder(this, CHANNEL_2_ID)
                    .setSmallIcon(R.drawable.ic_sponsored_notification)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setColor(getResources().getColor(R.color.colorPrimaryDark))
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true)
                    .build();

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(5, notification);

        } else if (title.equalsIgnoreCase("Admin")) {

            Intent notificationsIntent = new Intent(this, Dashboard.class);
            PendingIntent contentIntent = PendingIntent.getActivity(this,
                    0, notificationsIntent, 0);


            Notification notification = new NotificationCompat.Builder(this, CHANNEL_2_ID)
                    .setSmallIcon(R.drawable.ic_admin_notification)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setColor(getResources().getColor(R.color.colorPrimaryDark))
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true)
                    .setOnlyAlertOnce(true)
                    .build();

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(6, notification);

        } else if (title.equalsIgnoreCase("Followed Farms")) {

                Intent notificationsIntent = new Intent(this, Dashboard.class);
                PendingIntent contentIntent = PendingIntent.getActivity(this,
                        0, notificationsIntent, 0);


                Notification notification = new NotificationCompat.Builder(this, CHANNEL_2_ID)
                        .setSmallIcon(R.drawable.ic_followed_notification)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setColor(getResources().getColor(R.color.colorPrimaryDark))
                        .setContentIntent(contentIntent)
                        .setAutoCancel(true)
                        .build();

                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(7, notification);

        } else if (title.equalsIgnoreCase("Sponsored Farms")) {

                Intent notificationsIntent = new Intent(this, Dashboard.class);
                PendingIntent contentIntent = PendingIntent.getActivity(this,
                        0, notificationsIntent, 0);


                Notification notification = new NotificationCompat.Builder(this, CHANNEL_2_ID)
                        .setSmallIcon(R.drawable.ic_sponsored_notification)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setColor(getResources().getColor(R.color.colorPrimaryDark))
                        .setContentIntent(contentIntent)
                        .setAutoCancel(true)
                        .build();

                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(8, notification);

        }

    }

}
