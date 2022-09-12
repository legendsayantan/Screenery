package com.legendsayantan.screenery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.google.android.gms.location.SleepClassifyEvent;
import com.google.android.gms.location.SleepSegmentEvent;

import java.io.Serializable;
import java.util.List;

public class SleepReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(SleepClassifyEvent.hasEvents(intent)){
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            List<SleepClassifyEvent> events = SleepClassifyEvent.extractEvents(intent);
            SleepClassifyEvent event = events.get(events.size()-1);
            int confidence = event.getConfidence();
            int motion = event.getMotion();
            int light = event.getLight();
            System.out.println("Sleep event "+confidence+" - "+motion+" - "+light);
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if(preferences.getBoolean("sleepDetect",false)||
                    (preferences.getBoolean("sleepMedia",false)&&audioManager.isMusicActive()))
            if(light<=2&&motion<=2)
                if(confidence-(motion*10)-(light*5)>=70){
                    WakeFloatingService.sleepKill();
                }
        }
    }
}