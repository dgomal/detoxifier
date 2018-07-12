package com.testbirds.detoxifier.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import com.rvalerio.fgchecker.AppChecker;
import com.testbirds.detoxifier.R;


public class BlacklistService extends Service {

    private final static int NOTIFICATION_ID = 1234;
    private final static String STOP_SERVICE = BlacklistService.class.getPackage() + ".stop";

    private BroadcastReceiver stopServiceReceiver;
    private AppChecker appChecker;

    public static void start(Context context) {
        System.out.println("::: start");
        context.startService(new Intent(context, BlacklistService.class));
    }

    public static void stop(Context context) {
        System.out.println("::: stop");
        context.stopService(new Intent(context, BlacklistService.class));
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("::: onCreate");
        registerReceivers();
        startChecker();
        createStickyNotification();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopChecker();
        removeNotification();
        unregisterReceivers();
        stopSelf();
    }

    private void startChecker() {
        System.out.println("::: startChecker");
        appChecker = new AppChecker();
        appChecker
                .when(getPackageName(), new AppChecker.Listener() {
                    @Override
                    public void onForeground(String packageName) {
                        Toast.makeText(getBaseContext(), "::: Our app is in the foreground.", Toast.LENGTH_SHORT).show();
                    }
                })
                .whenOther(new AppChecker.Listener() {
                    @Override
                    public void onForeground(String packageName) {
                        Toast.makeText(getBaseContext(), "::: Foreground: " + packageName, Toast.LENGTH_SHORT).show();
                    }
                })
                .timeout(5000)
                .start(this);
    }

    private void stopChecker() {
        appChecker.stop();
    }

    private void registerReceivers() {
        stopServiceReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                stopSelf();
            }
        };
        registerReceiver(stopServiceReceiver, new IntentFilter(STOP_SERVICE));
    }

    private void unregisterReceivers() {
        unregisterReceiver(stopServiceReceiver);
    }

    private Notification createStickyNotification() {
        NotificationManager manager = ((NotificationManager) getSystemService(NOTIFICATION_SERVICE));
        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setAutoCancel(false)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.stop_detoxifier))
                .setContentIntent(PendingIntent.getBroadcast(this, 0, new Intent(STOP_SERVICE), PendingIntent.FLAG_UPDATE_CURRENT))
                .setWhen(0)
                .build();
        manager.notify(NOTIFICATION_ID, notification);
        return notification;
    }

    private void removeNotification() {
        NotificationManager manager = ((NotificationManager) getSystemService(NOTIFICATION_SERVICE));
        manager.cancel(NOTIFICATION_ID);
    }

}