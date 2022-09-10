package com.legendsayantan.screenery;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.card.MaterialCardView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    ConstraintLayout background;
    MaterialCardView wake;
    MaterialCardView dim;
    MaterialCardView frame;
    MaterialCardView cWake;
    MaterialCardView cDim;
    MaterialCardView cFrame;
    boolean ANIMATION_IN_PROGRESS = false;
    int ANIMATION_DURATION = 250;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        background = findViewById(R.id.back);
        wake=findViewById(R.id.scrWake);
        dim=findViewById(R.id.scrDim);
        frame=findViewById(R.id.scrFrame);
        cWake=findViewById(R.id.customWake);
        cDim=findViewById(R.id.customDim);
        cFrame=findViewById(R.id.customFrame);
        cWake.setOnClickListener(v -> {
            if(ANIMATION_IN_PROGRESS)return;
            if(checkOverlay()){
                runCloseAnimation(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        startActivity(new Intent(getApplicationContext(),WakeActivity.class)
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
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
                        });
                    }
                });
            }
        });

    }
    @Override
    protected void onResume() {
        findViewById(R.id.dialog).setVisibility(View.GONE);
        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
            refreshTheme();
            runOpenAnimation(null);
        }else{
            runCloseAnimation(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    showDialog("Storage Access is necessary for wallpaper based app theme.", v -> {
                        if(ANIMATION_IN_PROGRESS)return;
                        closeDialog(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},10);
                                super.onAnimationEnd(animation);
                            }
                        });
                    });
                }
            });

        }
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
        ColourTheme.init(MainActivity.this);
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ColourTheme.getDominantColor());
        ColourTheme.initContainer(background);
        ColourTheme.initCardToggle(wake, () -> {

        });
        ColourTheme.initCardToggle(dim, () -> {

        });
        ColourTheme.initCardToggle(frame, () -> {

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
        wake.animate().translationX(0).setDuration(ANIMATION_DURATION);
        cWake.animate().translationX(125).setDuration(ANIMATION_DURATION).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                dim.animate().translationX(0).setDuration(ANIMATION_DURATION);
                cDim.animate().translationX(125).setDuration(ANIMATION_DURATION).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        frame.animate().translationX(0).setDuration(ANIMATION_DURATION);
                        cFrame.animate().translationX(125).setDuration(ANIMATION_DURATION).setListener(listener);
                    }
                });
            }
        });
        ANIMATION_IN_PROGRESS = false;
    }
    protected void runCloseAnimation(AnimatorListenerAdapter adapter){
        ANIMATION_IN_PROGRESS = true;
        int width = getResources().getDisplayMetrics().widthPixels;
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
        ANIMATION_IN_PROGRESS = false;
    }
    protected void showDialog(String message,View.OnClickListener onContinueClick){
        ANIMATION_IN_PROGRESS = true;
        CardView cardView = findViewById(R.id.dialog);
        TextView textView = findViewById(R.id.dialogtext);
        TextView continueBtn = findViewById(R.id.dialog_btn);
        textView.setText(message);
        int height = getResources().getDisplayMetrics().heightPixels;
        cardView.setTranslationY(height);
        cardView.setVisibility(View.VISIBLE);
        cardView.animate().translationY(0).setDuration(ANIMATION_DURATION* 2L).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                continueBtn.setOnClickListener(onContinueClick);
            }
        });
        ANIMATION_IN_PROGRESS = false;
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
            }
        },ANIMATION_DURATION* 2L);
        ANIMATION_IN_PROGRESS = false;
    }

}