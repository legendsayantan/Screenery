package com.legendsayantan.screenery;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class WakeActivity extends AppCompatActivity {
    CheckBox checkBox;
    Button timerBtn,colorBtn;
    int ANIMATION_DURATION = 250;
    ArrayList<RadioButton> radioButtons = new ArrayList<>();
    private boolean ANIMATION_IN_PROGRESS = false;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wake);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        sharedPreferences  = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = sharedPreferences.edit();
        radioButtons.add(findViewById(R.id.radioButton1));
        radioButtons.add(findViewById(R.id.radioButton2));
        radioButtons.add(findViewById(R.id.radioButton3));
        radioButtons.add(findViewById(R.id.radioButton11));
        radioButtons.add(findViewById(R.id.radioButton21));
        checkBox = findViewById(R.id.checkBox);
        timerBtn = findViewById(R.id.button2);
        colorBtn = findViewById(R.id.button21);
        checkBox.setChecked(sharedPreferences.getBoolean("sleepDetect",false));
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> editor.putBoolean("sleepDetect",isChecked).apply());
        RadioGroup r1=findViewById(R.id.wakeSettings);
        radioButtons.get(1).setChecked(true);
        radioButtons.get(3).setChecked(true);
        r1.check(sharedPreferences.getInt("wakeSettings",0));
        r1.setOnCheckedChangeListener((group, checkedId) -> editor.putInt("wakeSettings",checkedId).apply());
        RadioGroup r2=findViewById(R.id.wakeOverlay);
        r2.check(sharedPreferences.getInt("wakeOverlay",0));
        r2.setOnCheckedChangeListener((group, checkedId) -> editor.putInt("wakeOverlay",checkedId).apply());


    }

    @Override
    protected void onPause() {
        System.out.println("Onpause");
        super.onPause();
    }

    @Override
    protected void onResume() {
        refreshTheme();
        runOpenAnimation(null);
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        if(ANIMATION_IN_PROGRESS)return;
        runCloseAnimation(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                startActivity(new Intent(getApplicationContext(),MainActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                finish();
            }
        });
    }

    protected void refreshTheme(){
        ColourTheme.init(WakeActivity.this);
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ColourTheme.getDominantColor());
        ColourTheme.initContainer(findViewById(R.id.back));
        ColourTheme.initCardToggle(findViewById(R.id.hCard), () -> {

        });
        ColourTheme.initCard(findViewById(R.id.wakelockCard));
        ColourTheme.initCard(findViewById(R.id.wakelockOverlayCard));
        ColourTheme.initTextView(findViewById(R.id.header));
        ColourTheme.initText(findViewById(R.id.textView2));
        ColourTheme.initText(findViewById(R.id.textView21));

        int accent = ColourTheme.getAccentColor();
        int accent2 = ColourTheme.getReverseAccentColor();
        ColorStateList stateList = new ColorStateList(
                new int[][]{new int[]{-android.R.attr.state_checked}, new int[]{android.R.attr.state_checked}},
                new int[]{accent,accent}
        );
        for (RadioButton r :radioButtons){
            r.setTextColor(accent);
            r.setButtonTintList(stateList);
        }
        checkBox.setTextColor(accent);
        checkBox.setButtonTintList(stateList);
        timerBtn.setBackgroundTintList(stateList);
        timerBtn.setTextColor(accent2);
        colorBtn.setBackgroundTintList(stateList);
        colorBtn.setTextColor(accent2);


    }
    protected void runOpenAnimation(AnimatorListenerAdapter adapter){
        ANIMATION_IN_PROGRESS = true;
        int width = getResources().getDisplayMetrics().widthPixels;
        findViewById(R.id.hCard).setTranslationY(-500);
        findViewById(R.id.wakelockCard).setTranslationX(-width);
        findViewById(R.id.wakelockOverlayCard).setTranslationX(width);
        findViewById(R.id.hCard).animate().translationY(0).setDuration(ANIMATION_DURATION).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                findViewById(R.id.wakelockCard).animate().translationX(-100).setDuration(ANIMATION_DURATION).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        findViewById(R.id.wakelockOverlayCard).animate().translationX(100).setDuration(ANIMATION_DURATION).setListener(adapter);
                    }
                });
            }
        });
        ANIMATION_IN_PROGRESS = false;
    }
    protected void runCloseAnimation(AnimatorListenerAdapter adapter){
        ANIMATION_IN_PROGRESS = true;
        int width = getResources().getDisplayMetrics().widthPixels;
        findViewById(R.id.wakelockOverlayCard).setTranslationX(100);
        findViewById(R.id.wakelockCard).setTranslationX(-100);
        findViewById(R.id.hCard).setTranslationY(0);
        findViewById(R.id.wakelockOverlayCard).animate().translationX(width).setDuration(ANIMATION_DURATION).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                findViewById(R.id.wakelockCard).animate().translationX(-width).setDuration(ANIMATION_DURATION).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        findViewById(R.id.hCard).animate().translationY(-500).setDuration(ANIMATION_DURATION).setListener(adapter);
                    }
                });
            }
        });
        ANIMATION_IN_PROGRESS = false;
    }
}