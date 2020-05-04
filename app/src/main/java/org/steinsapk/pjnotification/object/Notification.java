package org.steinsapk.pjnotification.object;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.steinsapk.pjnotification.R;
import org.steinsapk.pjnotification.activity.SplashActivity;

public class Notification {
    /*
    static private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_MAX;
            NotificationChannel channel = new NotificationChannel("Notice", "Notification", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notification Test");
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    */

    static void makeNotification(String courseName, String noticeTitle, Context context, boolean isNotice, String itemName, String boardName) {
        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(context, SplashActivity.class);
        // 추가 데이터 삽입 - 어떤 수업을 클릭했는가
        intent.putExtra("courseName", courseName);
        intent.putExtra("isNotice", isNotice);
        intent.putExtra("itemName", itemName);
        intent.putExtra("boardName", boardName);
        Log.e("TAG", courseName);


        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, noticeTitle.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = null;

        if (isNotice) {
            builder = new NotificationCompat.Builder(context, "Notice")
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.pj_logo_blue))
                    .setSmallIcon(R.drawable.baseline_announcement_black_24dp)
                    .setContentTitle(courseName)
                    .setContentText(noticeTitle)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    // Set the intent that will fire when the user taps the notification
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);
        } else {
            builder = new NotificationCompat.Builder(context, "Notice")
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.pj_logo_white))
                    .setSmallIcon(R.drawable.baseline_announcement_black_24dp)
                    .setContentTitle(courseName)
                    .setContentText(noticeTitle)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    // Set the intent that will fire when the user taps the notification
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(noticeTitle.hashCode(), builder.build());
    }
}
