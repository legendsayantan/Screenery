package com.legendsayantan.screenery;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author legendsayantan
 */

public class CustomSnackbar {
    Snackbar snackbar;
    static int bgColor;
    static int accentColor;
    public CustomSnackbar(View v, String message, Context activityContext,long dismissDelay) {
        snackbar = Snackbar.make(v,message,Snackbar.LENGTH_SHORT);
        snackbar.setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE);
        snackbar.getView().setBackgroundColor(Color.TRANSPARENT);
        Snackbar.SnackbarLayout snackbarLayout = (Snackbar.SnackbarLayout) snackbar.getView();
        snackbarLayout.setPadding(25,15,25,15);
        MaterialCardView mCardView = new MaterialCardView(activityContext);
        mCardView.setStrokeWidth(5);
        mCardView.setStrokeColor(accentColor);
        TextView textView = new TextView(activityContext);
        Typeface typeface = ResourcesCompat.getFont(activityContext, R.font.font);
        textView.setGravity(Gravity.CENTER);
        textView.setText(message);
        textView.setTypeface(typeface);
        textView.setTextColor(accentColor);
        mCardView.setCardBackgroundColor(bgColor);
        mCardView.setRadius(100);
        mCardView.addView(textView);
        snackbarLayout.addView(mCardView);
        snackbar.show();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                snackbar.dismiss();
            }
        },dismissDelay);
    }

    public static void setBgColor(int bgColor) {
        CustomSnackbar.bgColor = bgColor;
    }

    public static void setAccentColor(int accentColor) {
        CustomSnackbar.accentColor = accentColor;
    }
}
