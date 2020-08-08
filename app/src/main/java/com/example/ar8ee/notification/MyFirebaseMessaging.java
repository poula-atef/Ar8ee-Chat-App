package com.example.ar8ee.notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.RemoteInput;

import com.bumptech.glide.Glide;
import com.example.ar8ee.UI.Chat;
import com.example.ar8ee.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessaging extends FirebaseMessagingService {

    public static String REPLY_KEY = "reply_key";
    public static String SEEN_KEY = "seen_key";
    public static String REPLY_LABEL = "Reply";
    public static String SEEN_LABEL = "Mark as seen";
    public static int REPLY_ACTION_REQ_CODE = 40;
    public static int NOTI_ID = -1;
    public static String SENDER_ID = "";
    @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String sented = remoteMessage.getData().get("sented");
        String user = remoteMessage.getData().get("user");

        SharedPreferences sharedPreferences = getSharedPreferences("user",MODE_PRIVATE);
        String currentUser = sharedPreferences.getString("currentUser","noUser");

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser!=null && sented.equals(firebaseUser.getUid()) && !user.equals(currentUser)){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                sendHighNotification(remoteMessage);
            else
                sendNotification(remoteMessage);
        }
    }

    private void sendHighNotification(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int userParsed = Integer.parseInt(user.replaceAll("[\\D]",""));
        Intent intent = new Intent(this, Chat.class);
        intent.putExtra("userid",user);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);


        int userId = 0;
        if(userParsed > 0)
            userId = userParsed;
        highNotification highNotification = new highNotification(this);
        NOTI_ID = userId;
        SENDER_ID = user;
        NotificationCompat.Builder builder = highNotification.getHighNotification(icon,title,body,intent,userParsed);
        highNotification.createManager().notify(userId,builder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
    private void sendNotification(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");


        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int userParsed = Integer.parseInt(user.replaceAll("[\\D]",""));
        Intent intent = new Intent(this, Chat.class);
        intent.putExtra("userid",user);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,userParsed,intent,PendingIntent.FLAG_ONE_SHOT);


        int userId = 0;
        if(userParsed > 0)
            userId = userParsed;

        NOTI_ID = userId;
        SENDER_ID = user;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
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
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        manager.notify(userId,builder.build());
    }

    private NotificationCompat.Action ReplyAction(){

        RemoteInput remoteInput = new RemoteInput.Builder(REPLY_KEY)
                .setLabel(REPLY_LABEL)
                .build();

        Intent intent = new Intent(this,MyIntentService.class);
        intent.setAction(REPLY_KEY);
        intent.putExtra("id",NOTI_ID);
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
