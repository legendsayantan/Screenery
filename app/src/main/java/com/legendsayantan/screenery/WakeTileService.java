package com.legendsayantan.screenery;

import android.app.Service;
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

import androidx.annotation.RequiresApi;

/**
 * @author legendsayantan
 */

@RequiresApi(api = Build.VERSION_CODES.N)
public class WakeTileService extends android.service.quicksettings.TileService {
    static SharedPreferences preferences;
    static Tile qsTile;
    public WakeTileService() {
    }

    @Override
    public void onClick() {
        super.onClick();
        Tile tile = getQsTile();
        if (tile.getState() == Tile.STATE_INACTIVE) {
            enableTile();
        } else {
            disableTile();
        }
        try {
            MainActivity.wakeCardToggle(tile.getState());
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
        if(!checkOverlay()){
            startActivity(new Intent(getApplicationContext(),MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .putExtra("action",0));
            return;
        }
        if(getQsTile().getState()==Tile.STATE_UNAVAILABLE)getQsTile().setState(Tile.STATE_INACTIVE);
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
        super.onTileAdded();
    }
    public boolean checkOverlay() {
        return Settings.canDrawOverlays(getApplicationContext());
    }
    @Override
    public void onTileRemoved() {
        System.out.println("OnTileRemoved");
        super.onTileRemoved();
    }
    private void updateTileState(int state) {
        Tile tile = getQsTile();
        if (tile != null) {
            tile.setState(state);
            Icon icon = tile.getIcon();
            switch (state) {
                case Tile.STATE_ACTIVE:
                    icon.setTint(Color.WHITE);
                    break;
                case Tile.STATE_INACTIVE:
                case Tile.STATE_UNAVAILABLE:
                default:
                    icon.setTint(Color.GRAY);
                    break;
            }
            tile.updateTile();
        }
    }
    public static void enableTile(){
        qsTile.setState(Tile.STATE_ACTIVE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (preferences.getInt("wakeSettings", 0) == 1) {
                qsTile.setSubtitle("Custom timer");
            }else{
                qsTile.setSubtitle("Enabled");
            }
        }
        qsTile.updateTile();
    }
    public static void disableTile(){
        qsTile.setState(Tile.STATE_INACTIVE);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
            qsTile.setSubtitle("Disabled");
        }
        qsTile.updateTile();
    }
}