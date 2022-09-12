package com.legendsayantan.screenery;

import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.service.quicksettings.Tile;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

/**
 * @author legendsayantan
 */

@RequiresApi(api = Build.VERSION_CODES.N)
public class WakeTileService extends android.service.quicksettings.TileService {
    static SharedPreferences preferences;
    static Tile qsTile;
    private static JobScheduler jobScheduler;

    public WakeTileService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        requestListeningState(getApplicationContext(),new ComponentName(getApplicationContext(),WakeTileService.class));
        return super.onBind(intent);
    }

    @Override
    public void onClick() {
        if(getQsTile().getState()==Tile.STATE_UNAVAILABLE)getQsTile().setState(Tile.STATE_INACTIVE);
        Tile tile = getQsTile();
        qsTile=getQsTile();
        if (tile.getState() == Tile.STATE_INACTIVE) {
            enableTile(getApplicationContext());
        } else {
            try {
                WakeFloatingService.killSelf();
            }catch (Exception e){
                disableTile();
            }
        }
        try {
            MainActivity.wakeCardToggle(tile.getState());
        }catch (NullPointerException exception){}
        try {
            WakeActivity.wakeCardToggle(tile.getState());
        }catch (NullPointerException exception){}
    }

    @Override
    public void onDestroy() {
        System.out.println("OnDestroy");
        super.onDestroy();
    }

    @Override
    public void onStartListening() {
        System.out.println("Listening ");
        qsTile=getQsTile();
        preferences= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        super.onStartListening();
    }

    @Override
    public void onStopListening() {
        System.out.println("OnStopListening");
        super.onStopListening();
    }

    @Override
    public void onTileAdded() {
        System.out.println("OnTileAdded");
        requestListeningState(getApplicationContext(),new ComponentName(getApplicationContext(),WakeTileService.class));
        qsTile=getQsTile();
        WakeTileService.qsTile.updateTile();
        super.onTileAdded();
    }
    public static boolean checkOverlay(Context context) {
        return Settings.canDrawOverlays(context);
    }
    @Override
    public void onTileRemoved() {
        System.out.println("OnTileRemoved");
        super.onTileRemoved();
    }


    public static void enableTile(Context context){
        if(!checkOverlay(context)){
            context.startActivity(new Intent(context,WakeActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra("action",0));
            return;
        }
        if(qsTile==null){
            System.out.println("nulltile");
            Toast.makeText(context.getApplicationContext(), "Quick settings tile error",Toast.LENGTH_LONG).show();
            return;
        }
        qsTile.setState(Tile.STATE_ACTIVE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (preferences.getInt("wakeSettings", 0) == 1) {
                qsTile.setSubtitle(preferences.getInt("wakeTime",90)/60+"h "+preferences.getInt("wakeTime",90)%60+"min");
            }else{
                qsTile.setSubtitle("Enabled");
            }
        }
        qsTile.updateTile();
        startWake(context);
    }
    public static void disableTile(){
        if(qsTile==null)return;
        qsTile.setState(Tile.STATE_INACTIVE);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
            qsTile.setSubtitle("Disabled");
        }
        qsTile.updateTile();
        try {
            MainActivity.wakeCardToggle(qsTile.getState());
        }catch (NullPointerException exception){}
        try {
            WakeActivity.wakeCardToggle(qsTile.getState());
        }catch (NullPointerException exception){}
    }

    public static void startWake(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O )
            context.startForegroundService(new Intent(context, WakeFloatingService.class));
        else
            context.startService(new Intent(context,WakeFloatingService.class));
    }
}