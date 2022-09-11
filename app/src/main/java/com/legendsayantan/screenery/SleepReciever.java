package com.legendsayantan.screenery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.location.SleepClassifyEvent;
import com.google.android.gms.location.SleepSegmentEvent;

import java.io.Serializable;
import java.util.List;

public class SleepReciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(SleepClassifyEvent.hasEvents(intent)){
            List<SleepClassifyEvent> events = SleepClassifyEvent.extractEvents(intent);
            System.out.println("Sleep event "+events.get(events.size()-1).getConfidence());
            if(events.get(events.size()-1).getLight()<=1)
                if(events.get(events.size()-1).getMotion()<=1){
                    if(events.get(events.size()-1).getConfidence()>75){
                        WakeFloatingService.sleepKill();
                    }
                }
        }
    }
}