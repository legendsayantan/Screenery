package com.legendsayantan.screenery;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.service.quicksettings.Tile;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

/**
 * @author legendsayantan
 */

@RequiresApi(api = Build.VERSION_CODES.N)
public class DimTileService extends android.service.quicksettings.TileService {
    static SharedPreferences preferences;
    static Tile qsTile;

    public DimTileService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        requestListeningState(getApplicationContext(),new ComponentName(getApplicationContext(), DimTileService.class));
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
                DimFloatingService.killSelf();
            }catch (Exception e){
                disableTile();
            }
        }
        try {
            MainActivity.dimCardToggle(tile.getState());
        }catch (NullPointerException exception){}
        try {
            DimActivity.dimCardToggle(tile.getState());
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
        requestListeningState(getApplicationContext(),new ComponentName(getApplicationContext(), DimTileService.class));
        qsTile=getQsTile();
        DimTileService.qsTile.updateTile();
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
            context.startActivity(new Intent(context,MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra("action",1));
            return;
        }
        if(qsTile==null){
            System.out.println("nulltile");
            requestListeningState(context,new ComponentName(context,DimTileService.class));
            Toast.makeText(context.getApplicationContext(), "Use the quick settings tile as a shortcut.",Toast.LENGTH_LONG).show();
            startDim(context);
            return;
        }
        qsTile.setState(Tile.STATE_ACTIVE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            qsTile.setSubtitle("Enabled - "+preferences.getInt("dimIntensity",50)+"%");
        }
        qsTile.updateTile();
        startDim(context);
    }
    public static void disableTile(){
        if(qsTile==null)return;
        qsTile.setState(Tile.STATE_INACTIVE);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
            qsTile.setSubtitle("Disabled");
        }
        qsTile.updateTile();
        try {
            MainActivity.dimCardToggle(qsTile.getState());
        }catch (NullPointerException exception){}
        try {
            DimActivity.dimCardToggle(qsTile.getState());
        }catch (NullPointerException exception){}
    }

    public static void startDim(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O )
            context.startForegroundService(new Intent(context, DimFloatingService.class));
        else
            context.startService(new Intent(context,DimFloatingService.class));
    }
}