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

public class DimFloatingService extends Service {
    LinearLayout dimView;
    static WindowManager windowManager;
    static DimFloatingService service;
    static SharedPreferences preferences;
    static int LAYOUT_TYPE;
    private static CardView mCardView;
    int statusBarHeight;


    public DimFloatingService() {
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
        service = DimFloatingService.this;
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        dimView = new LinearLayout(getBaseContext());
        dimView.setKeepScreenOn(true);
        dimView.setBackgroundColor(Color.TRANSPARENT);
        dimView.setGravity(Gravity.CENTER);
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_TYPE = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_TYPE = WindowManager.LayoutParams.TYPE_TOAST;
        }

        statusBarHeight = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if(statusBarHeight>0)statusBarHeight=getResources().getDimensionPixelSize(statusBarHeight);
        WindowManager.LayoutParams floatWindowLayoutParam;
        switch (preferences.getInt("dimOverlay", 1)) {
            case 1:
                //dot
                floatWindowLayoutParam = new WindowManager.LayoutParams(
                        25,
                        25,
                        LAYOUT_TYPE,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                                | WindowManager.LayoutParams.FLAG_DIM_BEHIND,
                        PixelFormat.TRANSLUCENT
                );
                floatWindowLayoutParam.x=floatWindowLayoutParam.x+25;
                ImageView imageView = new ImageView(getApplicationContext());
                imageView.setImageDrawable(AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_outline_mode_night_24));
                imageView.setColorFilter(preferences.getInt("dimColor", Color.BLACK));
                imageView.setKeepScreenOn(true);
                dimView.addView(imageView);
                break;
            case 2:
                //floating menu
                floatWindowLayoutParam = new WindowManager.LayoutParams(
                        150,
                        150,
                        LAYOUT_TYPE,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                                | WindowManager.LayoutParams.FLAG_DIM_BEHIND,
                        PixelFormat.TRANSLUCENT
                );

                DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                floatWindowLayoutParam.x = preferences.getInt("dimX", 50);
                floatWindowLayoutParam.y = preferences.getInt("dimY", 50);
                CardView cardView = new CardView(getApplicationContext());
                cardView.setRadius(50);
                cardView.setKeepScreenOn(true);
                cardView.setCardBackgroundColor(preferences.getInt("bgColor",Color.TRANSPARENT));
                ImageView imageView2 = new ImageView(getApplicationContext());
                imageView2.setImageDrawable(AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_outline_mode_night_24));
                imageView2.setColorFilter(preferences.getInt("dimColor", preferences.getInt("acntColor",Color.TRANSPARENT)));
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
                settings.setColorFilter(preferences.getInt("dimColor", Color.WHITE));
                Space space = new Space(getApplicationContext());
                space.setMinimumWidth(50);
                ImageView close = new ImageView(getApplicationContext());
                close.setImageDrawable(AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_baseline_close_24));
                close.setColorFilter(preferences.getInt("dimColor", Color.WHITE));
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
                dimView.setOnTouchListener((v, event) -> {
                    int orientation = service.getResources().getConfiguration().orientation;
                    float x = event.getRawX()-(orientation==Configuration.ORIENTATION_PORTRAIT?0:statusBarHeight);
                    float y = event.getRawY()-(orientation==Configuration.ORIENTATION_PORTRAIT?statusBarHeight:0);
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        dimView.animate().scaleX(2f);
                        dimView.animate().scaleY(2f);
                        try {
                            windowManager.addView(closeLayout, params);
                        }catch (Exception ignored){}
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        dimView.animate().scaleX(1f);
                        dimView.animate().scaleY(1f);
                        windowManager.removeView(closeLayout);
                        int a = (int) (x - displayMetrics.widthPixels / 2);
                        int b = (int) (y - displayMetrics.heightPixels / 2);
                        b = b > 0 ? b : -b;
                        if ( b < 75)
                        if (0 < a && a <= 150) {
                            stopForeground(true);
                            stopSelf();
                            return true;
                        }else if(0 > a && a >= -150){
                            floatWindowLayoutParam.x = preferences.getInt("dimX", 50);
                            floatWindowLayoutParam.y = preferences.getInt("dimY", 50);
                            windowManager.updateViewLayout(dimView, floatWindowLayoutParam);
                            startActivity(new Intent(getApplicationContext(),DimActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        }
                        preferences.edit().putInt("dimX", floatWindowLayoutParam.x).putInt("dimY", floatWindowLayoutParam.y).apply();
                    } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                        floatWindowLayoutParam.x = (int) x - 75;
                        floatWindowLayoutParam.y = (int) y - 75;
                        windowManager.updateViewLayout(dimView, floatWindowLayoutParam);

                    }
                    return true;
                });
                dimView.addView(cardView);
                break;
            default:
                //no indicator
                floatWindowLayoutParam = new WindowManager.LayoutParams(
                        5,
                        5,
                        LAYOUT_TYPE,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                                | WindowManager.LayoutParams.FLAG_DIM_BEHIND,
                        PixelFormat.TRANSLUCENT
                );
                break;
        }
        floatWindowLayoutParam.gravity = Gravity.TOP|Gravity.START;
        floatWindowLayoutParam.dimAmount=preferences.getInt("dimIntensity",50)/100f;
        windowManager.addView(dimView,floatWindowLayoutParam);
        dimView.requestFocus();
        dimView.setKeepScreenOn(true);
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
            windowManager.removeView(dimView);
        }catch (Exception ignored){}
        try {
            windowManager.removeView(mCardView);
        }catch (Exception e){}
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            DimTileService.requestListeningState(getApplicationContext(),new ComponentName(getApplicationContext(),DimTileService.class));
            DimTileService.disableTile();
        }
        System.out.println("killed service");
        service=null;
        super.onDestroy();
    }
    public static void killSelf(){
        service.stopForeground(true);
        service.stopSelf();
    }
}