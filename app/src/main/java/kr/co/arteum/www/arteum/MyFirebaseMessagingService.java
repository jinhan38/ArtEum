package kr.co.arteum.www.arteum;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    String title = "";
    String message = "";
    String type = "";
    String userNum = "";
    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.e("NEW_TOKEN",s);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String[] activePackages;
        //if (Build.VERSION.SDK_INT <= 20) {//Build.VERSION_CODES.KITKAT_WATCH
        //    activePackages = getActivePackages();
        //} else {
        final String title = remoteMessage.getNotification().getTitle();
        final String message = remoteMessage.getNotification().getBody();
        final String type = remoteMessage.getData().get("type");
        final String userNum = remoteMessage.getData().get("userNum");


        Log.e("app Test", title + message + type + userNum);
        activePackages = getActivePackagesCompat();
        if (activePackages != null) {
            for (String activePackage : activePackages) {
                if (activePackage.equals("kr.co.arteum.www.arteum")) {//현재 앱이 화면에 보이는 경우
                    if (Util.CURRENT_CONTEXT != null) {
                        Log.e("app Test", "앱이 실행중");
                        if (remoteMessage != null && remoteMessage.getData().size() > 0) {
                            sendNotification(remoteMessage);
                        }
                        android.os.Handler mHandler = new android.os.Handler(Looper.getMainLooper());
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                               Toast.makeText(Util.CURRENT_CONTEXT.getApplicationContext(), title, Toast.LENGTH_LONG).show();
                                //Log.e("app Test", "로그인이 되었음" + getApplicationContext().toString() + Util.CURRENT_CONTEXT.toString() + Util.CURRENT_CONTEXT.getApplicationContext().toString());

                            }
                        }, 0);
                    }
                } else {//현재 화면에 앱이 안보이는 경우
                    Log.e("app Test", activePackage.toString());
                    if (Util.CURRENT_CONTEXT != null) {//실행은 되었지만 화면 활성화가 안된 경우


                        Log.e("app Test", "액티비티 활성화는 안되어 있지만 백그라운드로 돌아갈때"+Util.CURRENT_CONTEXT.toString());
                    } else {//앱자체가 실행이 안된 경우
//                        SettingSQL settingSQL = new SettingSQL(getBaseContext());//DB에서 세팅값을 불러와 UTIL에 담는다.
//                        if(settingSQL.getPushAllSetting(getBaseContext())) {
                        if (remoteMessage != null && remoteMessage.getData().size() > 0) {
                            sendNotification(remoteMessage);
                        }
//                        }
                        Log.e("app Test", "앱 종료");
                    }

                }
            }
        }


    }

     /** * remoteMessage 메세지 안애 getData와 getNotification이 있습니다. * **/
    private void sendNotification(RemoteMessage remoteMessage) {

        final String title = remoteMessage.getNotification().getTitle();
        final String message = remoteMessage.getNotification().getBody();
        final String type = remoteMessage.getData().get("type");
        final String userNum = remoteMessage.getData().get("userNum");

        boolean hasType = false;
        String url = "";
        switch (type)
        {
            case "message":
                hasType = true;
                url = "/Mobile/Views/Note/NoteList_m";
                break;
            case "exhibition":
                hasType = false;

                break;
            case "picturework":
                hasType = false;

                break;
            case "blog":
                hasType = false;

                break;
            case "create": //신규요청시
                hasType = true;
                url = "/Mobile/Views/Friends/FriendsList_m";
                break;
            case "receipt":
                hasType = true;
                url = "/Mobile/Views/Friends/FriendsList_m";
                break;

        }
        if(hasType) {
            //pendingIntent 구현할 것
        }
        /**
         * 오레오 버전부터는 Notification Channel이 없으면 푸시가 생성되지 않는 현상이 있습니다.
         * **/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            String channel = "아트이음";
            String channel_nm = "작가";

            NotificationManager notichannel = (android.app.NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channelMessage = new NotificationChannel(channel, channel_nm,
                    android.app.NotificationManager.IMPORTANCE_DEFAULT);
            channelMessage.setDescription("알림 푸쉬");
            channelMessage.enableLights(true);
            channelMessage.enableVibration(true);
            channelMessage.setShowBadge(false);
            channelMessage.setVibrationPattern(new long[]{100, 200, 100, 200});
            notichannel.createNotificationChannel(channelMessage);

            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(this, channel)
                            .setSmallIcon(R.drawable.ic_stat_name)
                            .setContentTitle(title)
                            .setColor(Color.rgb(77,228,255))
                            .setContentText(message)
                            .setChannelId(channel)
                            .setAutoCancel(true)
                            .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(9999, notificationBuilder.build());


        } else {
            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(this, "")
                            .setSmallIcon(R.drawable.ic_stat_name)
                            .setColor(Color.rgb(77,228,255))
                            .setContentTitle(title)
                            .setContentText(message)
                            .setAutoCancel(true)
                            .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(9999, notificationBuilder.build());

        }
    }

    public boolean isServiceRunning()
    {
        ActivityManager manager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if ("kr.co.arteum.www.arteum".equals(service.service.getClassName()))
                return true;
        }
        return false;
    }
    String[] getActivePackagesCompat() {
        ActivityManager mgr = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningTaskInfo> taskInfo = mgr.getRunningTasks(1);
        final ComponentName componentName = taskInfo.get(0).topActivity;
        final String[] activePackages = new String[1];
        activePackages[0] = componentName.getPackageName();
        return activePackages;
    }

    String[] getActivePackages() {
        final Set<String> activePackages = new HashSet<String>();
        ActivityManager mgr = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningAppProcessInfo> processInfos = mgr.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo processInfo : processInfos) {
            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                activePackages.addAll(Arrays.asList(processInfo.pkgList));
            }
        }
        return activePackages.toArray(new String[activePackages.size()]);
    }
}