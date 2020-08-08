package com.example.ar8ee.notification;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.RemoteInput;

import com.bumptech.glide.Glide;
import com.example.ar8ee.R;

import static com.example.ar8ee.notification.MyFirebaseMessaging.REPLY_ACTION_REQ_CODE;
import static com.example.ar8ee.notification.MyFirebaseMessaging.REPLY_KEY;
import static com.example.ar8ee.notification.MyFirebaseMessaging.REPLY_LABEL;
import static com.example.ar8ee.notification.MyFirebaseMessaging.SEEN_KEY;
import static com.example.ar8ee.notification.MyFirebaseMessaging.SEEN_LABEL;
import static com.example.ar8ee.notification.MyFirebaseMessaging.SENDER_ID;
import static com.example.ar8ee.notification.MyFirebaseMessaging.NOTI_ID;

public class highNotification extends ContextWrapper {

    private static final String CHANNEL_ID = "HIGH_CHANNEL_ID";
    private static final String CHANNEL_NAME = "HIGH_CHANNEL_NAME";
    private NotificationManager notificationManager = null;

    public highNotification(Context base) {
        super(base);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createChannel();
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        channel.enableLights(true);
        channel.enableVibration(true);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        createManager().createNotificationChannel(channel);
    }

    public NotificationManager createManager(){
        if(notificationManager == null)
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        return notificationManager;
    }


    @TargetApi(Build.VERSION_CODES.O)
    public NotificationCompat.Builder getHighNotification(String icon,String title,String body,Intent intent, int UserID){
        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        SENDER_ID = intent.getStringExtra("userid");
        PendingIntent pendingIntent = PendingIntent.getActivity(this,UserID,intent,PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(),CHANNEL_ID)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
                .setSmallIcon(R.drawable.send_dark_mode)
                .setContentTitle(title)
                .setContentText(body)
                .setLargeIcon(prepareLargeIcon(icon))
                .addAction(ReplyAction())
                .addAction(SeenAction())
                .setSound(defaultSound)
                .setContentIntent(pendingIntent);
        return builder;
    }


    private NotificationCompat.Action ReplyAction(){

        RemoteInput remoteInput = new RemoteInput.Builder(REPLY_KEY)
                .setLabel(REPLY_LABEL)
                .build();

        Intent intent = new Intent(this,MyIntentService.class);
        intent.setAction(REPLY_KEY);
        intent.putExtra("id",NOTI_ID);
        intent.putExtra("tag","tag");
        intent.putExtra("recever",SENDER_ID);
        PendingIntent pendingIntent = PendingIntent.getService(this,REPLY_ACTION_REQ_CODE,intent,PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Action replyAction =
                new NotificationCompat.Action.Builder(R.drawable.send_dark_mode, REPLY_LABEL, pendingIntent)
                        .addRemoteInput(remoteInput)
                        .setAllowGeneratedReplies(true)
                        .build();
        return replyAction;
    }

    private NotificationCompat.Action SeenAction(){

        Intent intent = new Intent(this,MyIntentService.class);
        intent.setAction(SEEN_KEY);
        intent.putExtra("id",NOTI_ID);
        intent.putExtra("tag","tag");
        intent.putExtra("recever",SENDER_ID);
        PendingIntent pendingIntent = PendingIntent.getService(this,REPLY_ACTION_REQ_CODE,intent,PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Action seenAction =
                new NotificationCompat.Action(R.drawable.send_dark_mode, SEEN_LABEL, pendingIntent);
        return seenAction;
    }

    private Bitmap prepareLargeIcon(String icon){
        Bitmap bitmap = null;

        try {
            bitmap = Glide.with(getApplicationContext())
                    .asBitmap()
                    .load(icon)
                    .submit(512, 512)
                    .get();
        }
        catch (Exception e){

        }

        if(bitmap == null)
            bitmap = BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher);

        return bitmap;
    }


}
