package com.legendsayantan.screenery;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.SleepSegmentEvent;
import com.google.android.gms.location.SleepSegmentRequest;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.card.MaterialCardView;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class WakeFloatingService extends Service {
    LinearLayout wakeView;
    static WindowManager windowManager;
    static WakeFloatingService service;
    static PendingIntent pendingIntent;
    static SharedPreferences preferences;
    static int LAYOUT_TYPE;

    public WakeFloatingService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startMyOwnForeground();
        } else {
            startForeground(5, new Notification());
        }
        service = WakeFloatingService.this;
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        wakeView = new LinearLayout(getBaseContext());
        wakeView.setKeepScreenOn(true);
        wakeView.setBackgroundColor(Color.TRANSPARENT);
        wakeView.setGravity(Gravity.CENTER);
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_TYPE = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_TYPE = WindowManager.LayoutParams.TYPE_TOAST;
        }
        WindowManager.LayoutParams floatWindowLayoutParam;
        switch (preferences.getInt("wakeOverlay", 1)) {
            case 1:
                //dot
                floatWindowLayoutParam = new WindowManager.LayoutParams(
                        25,
                        25,
                        LAYOUT_TYPE,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                        PixelFormat.TRANSLUCENT
                );
                ImageView imageView = new ImageView(getApplicationContext());
                imageView.setImageDrawable(AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_outline_wb_sunny_24));
                imageView.setColorFilter(preferences.getInt("wakeColor", Color.BLACK));
                imageView.setKeepScreenOn(true);
                wakeView.addView(imageView);
                break;
            case 2:
                //floating menu
                floatWindowLayoutParam = new WindowManager.LayoutParams(
                        150,
                        150,
                        LAYOUT_TYPE,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                        PixelFormat.TRANSLUCENT
                );
                floatWindowLayoutParam.x = preferences.getInt("wakeX", 50);
                floatWindowLayoutParam.y = preferences.getInt("wakeY", 50);
                CardView cardView = new CardView(getApplicationContext());
                cardView.setRadius(50);
                cardView.setKeepScreenOn(true);
                ImageView imageView2 = new ImageView(getApplicationContext());
                imageView2.setImageDrawable(AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_outline_wb_sunny_24));
                imageView2.setColorFilter(preferences.getInt("wakeColor", Color.BLACK));
                imageView2.setKeepScreenOn(true);
                imageView2.setScaleX(0.75f);
                imageView2.setScaleY(0.75f);
                cardView.addView(imageView2);
                //Close button
                ImageView close = new ImageView(getApplicationContext());
                close.setImageDrawable(AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_baseline_close_24));
                close.setColorFilter(preferences.getInt("wakeColor", Color.WHITE));
                WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                        100,
                        100,
                        LAYOUT_TYPE,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT
                );
                params.gravity = Gravity.CENTER;
                DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                wakeView.setOnTouchListener((v, event) -> {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        wakeView.animate().scaleX(2f);
                        wakeView.animate().scaleY(2f);
                        windowManager.addView(close, params);
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        wakeView.animate().scaleX(1f);
                        wakeView.animate().scaleY(1f);
                        windowManager.removeView(close);
                        int a = (int) (event.getRawX() - displayMetrics.widthPixels / 2);
                        a = a > 0 ? a : -a;
                        int b = (int) (event.getRawY() - 100 - displayMetrics.heightPixels / 2);
                        b = b > 0 ? b : -b;
                        if (a < 100 && b < 100) {
                            stopForeground(true);
                            stopSelf();
                            return true;
                        }
                        preferences.edit().putInt("wakeX", floatWindowLayoutParam.x).putInt("wakeY", floatWindowLayoutParam.y).apply();
                    } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                        floatWindowLayoutParam.x = (int) event.getRawX() - 75;
                        floatWindowLayoutParam.y = (int) event.getRawY() - 175;
                        windowManager.updateViewLayout(wakeView, floatWindowLayoutParam);

                    }
                    return true;
                });
                wakeView.addView(cardView);
                break;
            default:
                //no indicator
                floatWindowLayoutParam = new WindowManager.LayoutParams(
                        5,
                        5,
                        LAYOUT_TYPE,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                        PixelFormat.TRANSLUCENT
                );
                break;
        }
        //Timer
        int finishMinute = minuteBlock(System.currentTimeMillis()) + preferences.getInt("wakeTime", 90);
        boolean update = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
        if (preferences.getInt("wakeSettings", 0) == 1) {
            new Timer().scheduleAtFixedRate(new TimerTask() {
                @SuppressLint("NewApi")
                @Override
                public void run() {
                    int remainTime = finishMinute - minuteBlock(System.currentTimeMillis());
                    if (remainTime <= 0) {
                        stopForeground(true);
                        stopSelf();
                    } else if (update) {
                        WakeTileService.requestListeningState(getApplicationContext(), new ComponentName(getApplicationContext(), WakeTileService.class));
                        WakeTileService.qsTile.setSubtitle(remainTime / 60 + "h " + remainTime % 60 + "min");
                        WakeTileService.qsTile.updateTile();
                    }
                }
            }, 0, 60000);
        }
        if (preferences.getBoolean("sleepDetect", false)) {
            Intent sleepIntent = new Intent(getApplicationContext(), SleepReciever.class);
            pendingIntent = PendingIntent.getBroadcast(this, 0, sleepIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_MUTABLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            ActivityRecognition.getClient(getApplicationContext()).requestSleepSegmentUpdates(pendingIntent, SleepSegmentRequest.getDefaultSleepSegmentRequest()).addOnSuccessListener(unused -> {
                System.out.println("Subscribed to sleep data");
            }).addOnFailureListener(e -> {
                System.out.println(e.getMessage());
            });

        }

        floatWindowLayoutParam.gravity = Gravity.TOP|Gravity.START;
        windowManager.addView(wakeView,floatWindowLayoutParam);
        wakeView.requestFocus();
        wakeView.setKeepScreenOn(true);
        super.onCreate();
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startMyOwnForeground()
    {
        String NOTIFICATION_CHANNEL_ID = getPackageName()+".active";
        String channelName = "Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(5, notification);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        try {
            windowManager.removeView(wakeView);
        }catch (Exception ignored){}
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            WakeTileService.requestListeningState(getApplicationContext(),new ComponentName(getApplicationContext(),WakeTileService.class));
            WakeTileService.disableTile();
        }
        try {
            ActivityRecognition.getClient(getApplicationContext()).removeSleepSegmentUpdates(pendingIntent);
        }catch (Exception e){}
        System.out.println("killed service");
        super.onDestroy();
    }
    public static void killSelf(){
        service.stopForeground(true);
        service.stopSelf();
    }
    private int minuteBlock(long time){
        return (int) (time/60000);
    }

    @SuppressLint("ClickableViewAccessibility")
    public static void sleepKill(){
        Timer timer = new Timer();
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                480,
                480,
                LAYOUT_TYPE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.CENTER;
        CardView mCardView = new CardView(service);
        CardView cardView = new CardView(service);
        mCardView.setPadding(15,15,15,15);
        cardView.setCardBackgroundColor(preferences.getInt("wakeColor",Color.WHITE));
        TextView textView = new TextView(service);
        Typeface typeface = ResourcesCompat.getFont(service.getApplicationContext(), R.font.font);
        textView.setGravity(Gravity.CENTER);
        textView.setText("Screenery has detected you sleeping and will disable itself in 2 minutes.\nClick on this window to prevent it.");
        textView.setTypeface(typeface);
        textView.setTextColor(preferences.getInt("wakeColor",Color.WHITE));
        mCardView.setCardBackgroundColor(Color.BLACK);
        mCardView.setRadius(100);
        cardView.addView(textView);
        mCardView.addView(cardView);
        mCardView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                windowManager.removeView(mCardView);
                timer.cancel();
                return true;
            }
        });
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                windowManager.removeView(mCardView);
                killSelf();
            }
        },120000);
        windowManager.addView(mCardView,params);
    }


}