package com.legendsayantan.screenery;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.service.quicksettings.Tile;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ShareActionProvider;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;

import org.w3c.dom.Text;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author legendsayantan
 */

public class MainActivity extends AppCompatActivity {
    ConstraintLayout background;
    static MaterialCardView wake;
    static MaterialCardView dim;
    static MaterialCardView frame;
    MaterialCardView cWake;
    MaterialCardView cDim;
    MaterialCardView cFrame;
    boolean ANIMATION_IN_PROGRESS = false;
    int ANIMATION_DURATION = 200;
    Runnable actionRunnable  = () -> {};
    SharedPreferences sharedPreferences ;
    ImageView info,theme,battery;
    TextView bottomText;
    PowerManager pm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        if(sharedPreferences==null) sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        background = findViewById(R.id.back);
        wake=findViewById(R.id.scrWake);
        dim=findViewById(R.id.scrDim);
        frame=findViewById(R.id.scrFrame);
        cWake=findViewById(R.id.customWake);
        cDim=findViewById(R.id.customDim);
        cFrame=findViewById(R.id.customFrame);
        info = (ImageView)findViewById(R.id.info);
        theme = (ImageView)findViewById(R.id.theme);
        battery = (ImageView)findViewById(R.id.battery);
        bottomText = findViewById(R.id.version);
        bottomText.setText(getResources().getString(R.string.app_name)+" v"+BuildConfig.VERSION_NAME);
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        cWake.setOnClickListener(v -> {
            if(ANIMATION_IN_PROGRESS)return;
            if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED
            && sharedPreferences.getBoolean("storageAsk",true)) {
                askForStorage();
                return;
            }
            if(checkOverlay()){
                runCloseAnimation(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        startActivity(new Intent(getApplicationContext(),WakeActivity.class));
                    }
                });
            }else{
                runCloseAnimation(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        showDialog("Screen overlay permission is necessary to keep screen awake.\n\nFind Screenery and enable this permission in the next screen.", v1 -> {
                            closeDialog(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    getOverlayPerm();
                                }
                            });
                        },null);
                    }
                });
            }
        });
        cDim.setOnClickListener(v -> {
            if(ANIMATION_IN_PROGRESS)return;
            if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED
                    && sharedPreferences.getBoolean("storageAsk",true)) {
                askForStorage();
                return;
            }
            if(checkOverlay()){
                runCloseAnimation(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        startActivity(new Intent(getApplicationContext(),DimActivity.class));
                    }
                });
            }else{
                runCloseAnimation(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        showDialog("Screen overlay permission is necessary to dim screen brightness.\n\nFind Screenery and enable this permission in the next screen.", v1 -> {
                            closeDialog(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    getOverlayPerm();
                                }
                            });
                        },null);
                    }
                });
            }
        });
        cFrame.setOnClickListener(v -> {
            new CustomSnackbar(cFrame,"Suggest for new feature at github.",MainActivity.this,0);
        });
        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runCloseAnimation(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        showDialog("Screenery is a simple app to keep your screen awake or dim it's brightness.\n\nIt is open source and you can find it on github.", v1 -> {
                            closeDialog(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/legendsayantan/Screenery")));
                                }
                            });
                        },v1-> closeDialog(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                runOpenAnimation(null);
                            }
                        }));
                    }
                });
            };
        });

        theme.setOnClickListener(v -> askForStorage());
        actionRunnable = () -> {};
        if(getIntent().getIntExtra("action",-1)==0){
            actionRunnable = () -> cWake.callOnClick();
        }if(getIntent().getIntExtra("action",-1)==1){
            actionRunnable = () -> cDim.callOnClick();
        }
    }
    @Override
    protected void onResume() {
        ColourTheme.setFallBackDrawable(AppCompatResources.getDrawable(getApplicationContext(),R.drawable.fallbacktheme));
        findViewById(R.id.dialog).setVisibility(View.GONE);
        refreshTheme();
        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
            theme.setVisibility(View.GONE);
            runOpenAnimation(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        WakeTileService.requestListeningState(getApplicationContext(),
                                new ComponentName(getApplicationContext(),WakeTileService.class));
                        try {
                            wakeCardToggle(WakeTileService.qsTile.getState());
                        }catch (NullPointerException n){}
                        DimTileService.requestListeningState(getApplicationContext(),
                                new ComponentName(getApplicationContext(),DimTileService.class));
                        try {
                            dimCardToggle(DimTileService.qsTile.getState());
                        }catch (NullPointerException n){}
                    }
                    actionRunnable.run();
                }
            });
        }else{
            theme.setVisibility(View.VISIBLE);
            if(sharedPreferences.getBoolean("storageAsk",true))askForStorage();
            else runOpenAnimation(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        WakeTileService.requestListeningState(getApplicationContext(),
                                new ComponentName(getApplicationContext(),WakeTileService.class));
                        try {
                            wakeCardToggle(WakeTileService.qsTile.getState());
                        }catch (NullPointerException n){}
                        DimTileService.requestListeningState(getApplicationContext(),
                                new ComponentName(getApplicationContext(),DimTileService.class));
                        try {
                            dimCardToggle(DimTileService.qsTile.getState());
                        }catch (NullPointerException n){}
                    }
                    actionRunnable.run();
                }
            });
        }
        if (!pm.isIgnoringBatteryOptimizations(getPackageName())) {
            battery.setVisibility(View.VISIBLE);
            battery.setOnClickListener(v -> askBatteryOptimisations());
        } else battery.setVisibility(View.GONE);
        super.onResume();

    }

    @Override
    public void onBackPressed() {
        if(ANIMATION_IN_PROGRESS)return;
        if(findViewById(R.id.dialog).getVisibility()==View.VISIBLE){
            closeDialog(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    runOpenAnimation(null);
                }
            });
            return;
        }
        runCloseAnimation(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                finishAffinity();
                finish();
            }
        });
    }

    protected void refreshTheme(){
        if(sharedPreferences==null) sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        ColourTheme.init(MainActivity.this);
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ColourTheme.getDominantColor());
        ColourTheme.initContainer(background);
        info.setColorFilter(ColourTheme.getLightColor());
        theme.setColorFilter(ColourTheme.getLightColor());
        battery.setColorFilter(ColourTheme.getLightColor());
        bottomText.setTextColor(ColourTheme.getLightColor());
        ColourTheme.initCardToggle(wake, () -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ) {
                WakeTileService.requestListeningState(getApplicationContext(),
                        new ComponentName(getApplicationContext(),WakeTileService.class));
                if (wake.getStrokeWidth() == 0) {
                    try {
                        WakeFloatingService.killSelf();
                    }catch (Exception e){
                        WakeTileService.disableTile();
                    }
                } else {
                    WakeTileService.enableTile(getApplicationContext());
                }
            }else{
                if (wake.getStrokeWidth() == 0) {
                    try {
                        WakeFloatingService.killSelf();
                    }catch (Exception ignored){}
                } else {
                    startService(new Intent(getApplicationContext(),DimFloatingService.class));
                }
            }
        });
        ColourTheme.initCardToggle(dim, () -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ) {
                DimTileService.requestListeningState(getApplicationContext(),
                        new ComponentName(getApplicationContext(),DimTileService.class));
                if (dim.getStrokeWidth() == 0) {
                    try {
                        DimFloatingService.killSelf();
                    }catch (Exception e){
                        DimTileService.disableTile();
                    }
                } else {
                    DimTileService.enableTile(getApplicationContext());
                }
            }else{
                if (dim.getStrokeWidth() == 0) {
                    try {
                        DimFloatingService.killSelf();
                    }catch (Exception ignored){}
                } else {
                    startService(new Intent(getApplicationContext(),DimFloatingService.class));
                }
            }
        });
        ColourTheme.initCardToggle(frame, () -> {

            new CustomSnackbar(frame,"Empty space for future updates",MainActivity.this,0);
            frame.setStrokeWidth(0);
        });
        ColourTheme.initCard(cWake);
        ColourTheme.initCard(cDim);
        ColourTheme.initCard(cFrame);
        ColourTheme.initImageView(findViewById(R.id.wake));
        ColourTheme.initImageView(findViewById(R.id.dim));
        ColourTheme.initImageView(findViewById(R.id.frame));
        ColourTheme.initImageView(findViewById(R.id.ar1));
        ColourTheme.initImageView(findViewById(R.id.ar2));
        ColourTheme.initImageView(findViewById(R.id.ar3));
        ColourTheme.initText(findViewById(R.id.t1));
        ColourTheme.initText(findViewById(R.id.t2));
        ColourTheme.initText(findViewById(R.id.t3));
        ColourTheme.initCard(findViewById(R.id.dialog));
        TextView t = findViewById(R.id.textViewDialog);
        t.setTextColor(ColourTheme.getAccentColor());
        ColourTheme.initText(findViewById(R.id.dialogtext));
        ColourTheme.initText(findViewById(R.id.dialog_btn));
        ColourTheme.initText(findViewById(R.id.dialog_back));
        CustomSnackbar.setAccentColor(ColourTheme.getAccentColor());
        CustomSnackbar.setBgColor(ColourTheme.getSecondaryAccentColor());

        sharedPreferences.edit().putInt("acntColor",ColourTheme.getAccentColor()).apply();
        sharedPreferences.edit().putInt("bgColor",ColourTheme.getSecondaryAccentColor()).apply();
    }

    public void getOverlayPerm() {
        startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:"+getPackageName()))
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    public boolean checkOverlay() {
        return Settings.canDrawOverlays(getApplicationContext());
    }
    protected void runOpenAnimation(AnimatorListenerAdapter listener){
        ANIMATION_IN_PROGRESS = true;
        int width = getResources().getDisplayMetrics().widthPixels;
        wake.setTranslationX(-400);
        dim.setTranslationX(-400);
        frame.setTranslationX(-400);
        cWake.setTranslationX(width-150);
        cDim.setTranslationX(width-150);
        cFrame.setTranslationX(width-150);
        info.setTranslationY(100);
        theme.setTranslationY(100);
        battery.setTranslationY(100);
        bottomText.setTranslationY(100);
        wake.animate().translationX(0).setDuration(ANIMATION_DURATION);
        cWake.animate().translationX(125).setDuration(ANIMATION_DURATION).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                dim.animate().translationX(0).setDuration(ANIMATION_DURATION);
                cDim.animate().translationX(125).setDuration(ANIMATION_DURATION).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        frame.animate().translationX(0).setDuration(ANIMATION_DURATION);
                        cFrame.animate().translationX(125).setDuration(ANIMATION_DURATION).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                showBottomBar(listener);
                            }
                        });
                    }
                });
            }
        });
        ANIMATION_IN_PROGRESS = false;
    }
    protected void runCloseAnimation(AnimatorListenerAdapter adapter){
        ANIMATION_IN_PROGRESS = true;
        int width = getResources().getDisplayMetrics().widthPixels;
        hideBottomBar(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                frame.animate().translationX(-400).setDuration(ANIMATION_DURATION);
                cFrame.animate().translationX(width-150).setDuration(ANIMATION_DURATION).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        dim.animate().translationX(-400).setDuration(ANIMATION_DURATION);
                        cDim.animate().translationX(width-150).setDuration(ANIMATION_DURATION).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                wake.animate().translationX(-400).setDuration(ANIMATION_DURATION);
                                cWake.animate().translationX(width-150).setDuration(ANIMATION_DURATION).setListener(adapter);
                            }
                        });
                    }
                });
            }
        });
        ANIMATION_IN_PROGRESS = false;
    }
    protected void showDialog(String message,View.OnClickListener onContinueClick, View.OnClickListener onCancelClick){
        ANIMATION_IN_PROGRESS = true;
        CardView cardView = findViewById(R.id.dialog);
        TextView textView = findViewById(R.id.dialogtext);
        TextView continueBtn = findViewById(R.id.dialog_btn);
        TextView cancelBtn = findViewById(R.id.dialog_back);
        textView.setText(message);
        int height = getResources().getDisplayMetrics().heightPixels;
        cardView.setTranslationY(height);
        cardView.setVisibility(View.VISIBLE);
        if(onCancelClick==null)cancelBtn.setVisibility(View.GONE);
        else {
            cancelBtn.setVisibility(View.VISIBLE);
            cancelBtn.setOnClickListener(onCancelClick);
        }
        cardView.animate().translationY(0).setDuration(ANIMATION_DURATION* 2L).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                ANIMATION_IN_PROGRESS = false;
                continueBtn.setOnClickListener(onContinueClick);
            }
        });
    }
    protected void closeDialog(AnimatorListenerAdapter adapter){
        ANIMATION_IN_PROGRESS = true;
        CardView cardView = findViewById(R.id.dialog);
        TextView continueBtn = findViewById(R.id.dialog_btn);
        continueBtn.setOnClickListener(null);
        int height = getResources().getDisplayMetrics().heightPixels;
        cardView.animate().translationY(height).setDuration(ANIMATION_DURATION* 2L).setListener(adapter);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> cardView.setVisibility(View.GONE));
                ANIMATION_IN_PROGRESS = false;
            }
        },ANIMATION_DURATION* 2L);
    }
    private void askForStorage(){
        runCloseAnimation(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                showDialog("Do you want to use wallpaper based theme?\nPress Continue for wallpaper theme, Press Back to use the app in default theme.", v -> {
                    if (ANIMATION_IN_PROGRESS) return;
                    closeDialog(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 10);
                        }
                    });
                }, v -> {
                    sharedPreferences.edit().putBoolean("storageAsk",false).apply();
                    if (ANIMATION_IN_PROGRESS) return;
                    closeDialog(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation1) {
                            runOpenAnimation(null);
                        }
                    });
                });
            }
        });
    }
    private void askBatteryOptimisations(){
        runCloseAnimation(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                showDialog("Do you want to let this app run in background?\nIt will improve the reliability of this app.", v -> {
                    if(ANIMATION_IN_PROGRESS)return;
                    closeDialog(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            startActivity(new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:"+getPackageName())));
                        }
                    });
                },v -> {
                    if(ANIMATION_IN_PROGRESS)return;
                    closeDialog(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation1) {
                            runOpenAnimation(null);
                        }
                    });
                });
            }
        });
    }
    public static void wakeCardToggle(int tileState){
        if(tileState==Tile.STATE_ACTIVE){
            wake.setStrokeWidth(10);
        }else wake.setStrokeWidth(0);
    }
    public static void dimCardToggle(int tileState) {
        if(tileState==Tile.STATE_ACTIVE){
            dim.setStrokeWidth(10);
        }else dim.setStrokeWidth(0);
    }
    public void hideInfo(AnimatorListenerAdapter adapter){
        info.animate().translationY(100).setDuration(50).setListener(adapter);
    }
    public void hideTheme(AnimatorListenerAdapter adapter){
        theme.animate().translationY(100).setDuration(50).setListener(adapter);
    }
    public void hideBattery(AnimatorListenerAdapter adapter){
        battery.animate().translationY(100).setDuration(50).setListener(adapter);
    }
    public void hideBottomText(AnimatorListenerAdapter adapter){
        bottomText.animate().translationY(100).setDuration(50).setListener(adapter);
    }
    public void showInfo(AnimatorListenerAdapter adapter){
        info.animate().translationY(0).setDuration(50).setListener(adapter);
    }
    public void showTheme(AnimatorListenerAdapter adapter){
        theme.animate().translationY(0).setDuration(50).setListener(adapter);
    }
    public void showBattery(AnimatorListenerAdapter adapter){
        battery.animate().translationY(0).setDuration(50).setListener(adapter);
    }
    public void showBottomText(AnimatorListenerAdapter adapter){
        bottomText.animate().translationY(0).setDuration(50).setListener(adapter);
    }
    public void hideBottomBar(AnimatorListenerAdapter adapter){
        hideInfo(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if(theme.getVisibility()==View.VISIBLE)hideTheme(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if(battery.getVisibility()==View.VISIBLE)hideBattery(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                hideBottomText(adapter);
                            }
                        });
                        else hideBottomText(adapter);
                    }
                });
                else hideBattery(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        hideBottomText(adapter);
                    }
                });
            }
        });
    }
    public void showBottomBar(AnimatorListenerAdapter adapter){
        showInfo(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if(theme.getVisibility()==View.VISIBLE)showTheme(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if(battery.getVisibility()==View.VISIBLE)showBattery(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                showBottomText(adapter);
                            }
                        });
                        else showBottomText(adapter);
                    }
                });
                else showBattery(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        showBottomText(adapter);
                    }
                });
            }
        });
    }
}