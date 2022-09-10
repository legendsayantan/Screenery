package com.legendsayantan.screenery;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.graphics.ColorUtils;
import androidx.palette.graphics.Palette;

import com.google.android.material.card.MaterialCardView;

public class ColourTheme {
    private static Drawable drawable;
    private static int lightColor;
    private static int darkColor;
    private static int dominantColor;
    private static int vibrantColor;
    private static boolean nightUi;
    private static Activity activity;

    public static void init(Activity activity) {
        System.out.println("preparing theme");
        Drawable drawable1 = null;
        ColourTheme.activity = activity;
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) { return; }
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(activity);
        drawable1 = wallpaperManager.peekDrawable();
        if (drawable1 == null) {
            System.out.println("Null returned at peekDrawable");
            drawable1 = wallpaperManager.getDrawable();
        }
        if(ColourTheme.drawable==drawable1)return;
        drawable = drawable1;
        Bitmap iconBitmap = ((BitmapDrawable) drawable).getBitmap();
        Palette iconPalette = Palette.from(iconBitmap).maximumColorCount(16).generate();
        lightColor = iconPalette.getLightVibrantColor(
                ColorUtils.blendARGB(
                        iconPalette.getDominantColor(iconPalette.getVibrantColor(0x000000)),
                        activity.getResources().getColor(R.color.softwhite),
                        0.5F));
        darkColor = iconPalette.getDarkVibrantColor(
                ColorUtils.blendARGB(
                        lightColor,
                        activity.getResources().getColor(R.color.softblack),
                        0.5F));
        if(getDistance(lightColor,darkColor)<=5.0){
            lightColor = ColorUtils.blendARGB(lightColor,activity.getResources().getColor(R.color.softwhite),0.5F);
        }
        dominantColor = iconPalette.getDominantColor(iconPalette.getVibrantColor(0x000000));
        if(getDistance(dominantColor,iconPalette.getVibrantColor(0x000000))<=5.0){
            dominantColor = ColorUtils.blendARGB(darkColor,activity.getResources().getColor(R.color.softblack),0.5F);
        }
        int uiFlags =
                activity.getResources().getConfiguration().uiMode &
                        Configuration.UI_MODE_NIGHT_MASK;
        nightUi = uiFlags == Configuration.UI_MODE_NIGHT_YES;
        vibrantColor = iconPalette.getVibrantColor(
                ColorUtils.blendARGB(
                        vibrantColor,
                        nightUi?
                                iconPalette.getVibrantColor(activity.getColor(R.color.softblack)):
                                iconPalette.getVibrantColor(activity.getColor(R.color.softwhite)),
                        0.5F));
    }

    public static Drawable getWallpaper() {
        return drawable;
    }

    public static int getLightColor() {
        return lightColor;
    }

    public static int getDarkColor() {
        return darkColor;
    }

    public static int getDominantColor() {
        return dominantColor;
    }

    public static int getVibrantColor() {
        return vibrantColor;
    }

    public static void setActivity(Activity activity) {
        ColourTheme.activity = activity;
    }

    public static void initView(View view){
        view.setBackgroundColor(getSecondaryAccentColor());
    }
    @SuppressLint("ClickableViewAccessibility")
    public static void initCard(MaterialCardView cardView){
        activity.runOnUiThread(() -> {
            cardView.setStrokeColor(getAccentColor());
            cardView.setCardBackgroundColor(getSecondaryAccentColor());
            cardView.setOnTouchListener((v, event) -> {
                if(event.getAction()==MotionEvent.ACTION_DOWN){
                    cardView.setStrokeWidth(10);
                }if(event.getAction()==MotionEvent.ACTION_UP){
                    cardView.setStrokeWidth(0);
                    cardView.callOnClick();
                }
                return true;
            });
        });
    }
    @SuppressLint("ClickableViewAccessibility")
    public static void initCardToggle(MaterialCardView cardView,Runnable onToggle){
        activity.runOnUiThread(() -> {
            initCard(cardView);
            cardView.setOnTouchListener((v, event) -> {
                if(event.getAction()==MotionEvent.ACTION_DOWN){
                    if(cardView.getStrokeWidth()==0){
                        cardView.setStrokeWidth(10);
                    }else cardView.setStrokeWidth(0);
                    onToggle.run();
                }
                return true;
            });
        });
    }

    public static void initText(TextView view){
        view.setTextColor(getAccentColor());
    }
    public static void initTextView(TextView view){
        initText(view);
        view.setBackgroundColor(getSecondaryAccentColor());
    }
    public static void initImageView(ImageView view){
        view.setColorFilter(getAccentColor());
        view.setBackgroundColor(getSecondaryAccentColor());
    }
    public static int getAccentColor() {
        if (nightUi)return lightColor;
        else return darkColor;
    }
    public static int getSecondaryAccentColor(){
        if (!nightUi)return lightColor;
        else return darkColor;
    }
    public static void initContainer(View parent){
        activity.runOnUiThread(() -> {
            parent.animate().alpha(0);
            parent.setBackground(drawable);
            parent.animate().alpha(1).setDuration(1000);
        });
    }
    private static float getHue(int color) {
        int R = (color >> 16) & 0xff;
        int G = (color >>  8) & 0xff;
        int B = (color      ) & 0xff;
        float[] colorHue = new float[3];
        ColorUtils.RGBToHSL(R, G, B, colorHue);
        return colorHue[0];
    }
    private static float getDistance(int color1, int color2) {
        float avgHue = (getHue(color1) + getHue(color2))/2;
        return Math.abs(getHue(color1) - avgHue);
    }
}
