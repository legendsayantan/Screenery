package com.legendsayantan.screenery;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.SleepSegmentRequest;

import java.util.Timer;
import java.util.TimerTask;

public class WakeFloatingService extends Service {
    LinearLayout wakeView;
    static WindowManager windowManager;
    static WakeFloatingService service;
    static PendingIntent pendingIntent;
    static SharedPreferences preferences;
    static int LAYOUT_TYPE;
    private static CardView mCardView;
    int statusBarHeight;
    Timer customTimer = new Timer();


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

        statusBarHeight = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if(statusBarHeight>0)statusBarHeight=getResources().getDimensionPixelSize(statusBarHeight);
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

                DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                floatWindowLayoutParam.x = preferences.getInt("wakeX", 50);
                floatWindowLayoutParam.y = preferences.getInt("wakeY", 50);
                CardView cardView = new CardView(getApplicationContext());
                cardView.setRadius(50);
                cardView.setKeepScreenOn(true);
                cardView.setCardBackgroundColor(getColor(R.color.ic_launcher_background));
                ImageView imageView2 = new ImageView(getApplicationContext());
                imageView2.setImageDrawable(AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_outline_wb_sunny_24));
                imageView2.setColorFilter(preferences.getInt("wakeColor", Color.BLACK));
                imageView2.setKeepScreenOn(true);
                imageView2.setScaleX(0.75f);
                imageView2.setScaleY(0.75f);
                cardView.addView(imageView2);
                //Close button
                LinearLayout closeLayout = new LinearLayout(getApplicationContext());
                closeLayout.setMinimumHeight(150);
                closeLayout.setMinimumWidth(350);
                closeLayout.setOrientation(LinearLayout.HORIZONTAL);
                ImageView settings = new ImageView(getApplicationContext());
                settings.setImageDrawable(AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_baseline_settings_24));
                settings.setColorFilter(preferences.getInt("wakeColor", Color.WHITE));
                Space space = new Space(getApplicationContext());
                space.setMinimumWidth(50);
                ImageView close = new ImageView(getApplicationContext());
                close.setImageDrawable(AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_baseline_close_24));
                close.setColorFilter(preferences.getInt("wakeColor", Color.WHITE));
                settings.setScaleX(1.25f);
                settings.setScaleY(1.25f);
                close.setScaleX(1.5f);
                close.setScaleY(1.5f);
                WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                        350,
                        150,
                        LAYOUT_TYPE,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT
                );
                params.gravity=Gravity.CENTER;
                params.x=params.width/4;
                params.y=params.height/4;
                closeLayout.addView(settings);
                closeLayout.addView(space);
                closeLayout.addView(close);
                wakeView.setOnTouchListener((v, event) -> {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        wakeView.animate().scaleX(2f);
                        wakeView.animate().scaleY(2f);
                        try {
                            windowManager.addView(closeLayout, params);
                        }catch (Exception ignored){}
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        wakeView.animate().scaleX(1f);
                        wakeView.animate().scaleY(1f);
                        windowManager.removeView(closeLayout);
                        int a = (int) (event.getRawX() - displayMetrics.widthPixels / 2);
                        int b = (int) (event.getRawY() - statusBarHeight - displayMetrics.heightPixels / 2);
                        b = b > 0 ? b : -b;
                        if ( b < 75)
                        if (0 < a && a <= 150) {
                            stopForeground(true);
                            stopSelf();
                            return true;
                        }else if(0 > a && a >= -150){
                            floatWindowLayoutParam.x = preferences.getInt("wakeX", 50);
                            floatWindowLayoutParam.y = preferences.getInt("wakeY", 50);
                            windowManager.updateViewLayout(wakeView, floatWindowLayoutParam);
                            startActivity(new Intent(getApplicationContext(),WakeActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        }
                        preferences.edit().putInt("wakeX", floatWindowLayoutParam.x).putInt("wakeY", floatWindowLayoutParam.y).apply();
                    } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                        floatWindowLayoutParam.x = (int) event.getRawX() - 75;
                        floatWindowLayoutParam.y = (int) event.getRawY() - 75 - statusBarHeight;
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
            customTimer.scheduleAtFixedRate(new TimerTask() {
                @SuppressLint("NewApi")
                @Override
                public void run() {
                    int remainTime = finishMinute - minuteBlock(System.currentTimeMillis());
                    if (remainTime <= 0) {
                        stopForeground(true);
                        stopSelf();
                    } else if (update) {
                        try {
                            WakeTileService.requestListeningState(getApplicationContext(), new ComponentName(getApplicationContext(), WakeTileService.class));
                            WakeTileService.qsTile.setSubtitle(remainTime / 60 + "h " + remainTime % 60 + "min");
                            WakeTileService.qsTile.updateTile();
                        }catch (Exception ignored){};
                    }
                }
            }, 0, 60000);
        }
        if (preferences.getBoolean("sleepDetect", false)||preferences.getBoolean("sleepMedia",false)) {
            Intent sleepIntent = new Intent(getApplicationContext(), SleepReceiver.class);
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
        try {
            windowManager.removeView(mCardView);
        }catch (Exception ignored){}
        try {
            customTimer.cancel();
        }catch (Exception ignored){}
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            WakeTileService.requestListeningState(getApplicationContext(),new ComponentName(getApplicationContext(),WakeTileService.class));
            WakeTileService.disableTile();
        }
        try {
            ActivityRecognition.getClient(getApplicationContext()).removeSleepSegmentUpdates(pendingIntent);
        }catch (Exception e){}
        System.out.println("killed service");
        service=null;
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
        DisplayMetrics displayMetrics = service.getResources().getDisplayMetrics();
        int orientation = service.getResources().getConfiguration().orientation;
        int size = orientation==Configuration.ORIENTATION_PORTRAIT?displayMetrics.widthPixels:displayMetrics.heightPixels;
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                (int) (size*0.75),
                (int) (size*0.60),
                LAYOUT_TYPE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.CENTER;
        mCardView = new CardView(service);
        mCardView.setBackgroundResource(R.drawable.cardviewborder);
        TextView textView = new TextView(service);
        Typeface typeface = ResourcesCompat.getFont(service.getApplicationContext(), R.font.font);
        textView.setGravity(Gravity.CENTER);
        String text = "Screenery has detected you are sleeping.\n";
        text = preferences.getBoolean("sleepDetect", false)?text+"Screen wake ":text;
        text = preferences.getBoolean("sleepDetect", false)&&preferences.getBoolean("sleepMedia", false)?
                text+"and ":text;
        text = preferences.getBoolean("sleepMedia", false)?text+"Device media ":text;
        text=text+"will be disabled within 2 minutes.\nClick here to cancel.";
        textView.setText(text);
        textView.setTypeface(typeface);
        textView.setTextColor(preferences.getInt("wakeColor",Color.WHITE));
        textView.setPadding(size/15,size/15,size/15,size/15);
        mCardView.setCardBackgroundColor(preferences.getInt("wakeColor",Color.WHITE));
        mCardView.setRadius(100);
        mCardView.addView(textView);
        mCardView.setPadding(size/10,size/10,size/10,size/10);
        mCardView.setOnTouchListener((v, event) -> {
            windowManager.removeView(mCardView);
            timer.cancel();
            return true;
        });
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(preferences.getBoolean("sleepMedia", false)){
                    windowManager.removeView(mCardView);
                    ((AudioManager)service.getSystemService(Context.AUDIO_SERVICE)).requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
                }
                if(preferences.getBoolean("sleepDetect", false)){
                    killSelf();
                }
            }
        },120000);
        windowManager.addView(mCardView,params);
    }


}