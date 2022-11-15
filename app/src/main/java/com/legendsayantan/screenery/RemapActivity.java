package com.legendsayantan.screenery;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;

import java.util.Timer;
import java.util.TimerTask;

public class RemapActivity extends AppCompatActivity {

    private static final long ANIMATION_DURATION = 250;
    private static boolean ANIMATION_IN_PROGRESS = false;
    private SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    MaterialCardView hCard;
    RadioGroup buttons,triggers,actions;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remap);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        sharedPreferences  = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = sharedPreferences.edit();
        hCard = findViewById(R.id.hCard);
        buttons = findViewById(R.id.groupButton);
        triggers = findViewById(R.id.groupTrigger);
        actions = findViewById(R.id.groupAction);
        buttons.setOnCheckedChangeListener((group, checkedId) -> {
            editor.putInt("button",group.indexOfChild(findViewById(checkedId))).apply();
            RadioButton btn = (RadioButton) triggers.getChildAt(0);
            if(group.indexOfChild(findViewById(checkedId))==0) btn.setText("Single Click");
            else btn.setText("Double Click");
        });
        triggers.setOnCheckedChangeListener((group, checkedId) -> {
            editor.putInt("trigger",group.indexOfChild(findViewById(checkedId))).apply();
        });
        actions.setOnCheckedChangeListener((group, checkedId) -> {
            editor.putInt("action",group.indexOfChild(findViewById(checkedId))).apply();
        });
    }
    @Override
    protected void onResume() {
        refreshTheme();
        runOpenAnimation(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                RadioButton temp = (RadioButton) buttons.getChildAt(sharedPreferences.getInt("button",0));
                temp.setChecked(true);
                temp = (RadioButton) triggers.getChildAt(sharedPreferences.getInt("trigger",0));
                temp.setChecked(true);
                temp = (RadioButton) actions.getChildAt(sharedPreferences.getInt("action",0));
                temp.setChecked(true);
            }
        });
        super.onResume();
    }

    @Override
    public void onBackPressed() {
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
                RemapActivity.super.onBackPressed();
            }
        });
    }

    protected void refreshTheme(){
        ColourTheme.init(RemapActivity.this);
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ColourTheme.getDominantColor());
        ColourTheme.initContainer(findViewById(R.id.back));
        ColourTheme.initCardToggle(findViewById(R.id.hCard), () -> {

        });
        ColourTheme.initTextView(findViewById(R.id.header));
        int accent = ColourTheme.getAccentColor();
        int accent2 = ColourTheme.getSecondaryAccentColor();
        findViewById(R.id.cardButton).setBackgroundColor(accent2);
        findViewById(R.id.cardTrigger).setBackgroundColor(accent2);
        ColourTheme.initCard(findViewById(R.id.cardAction));
        ColourTheme.initText(findViewById(R.id.text1));
        ColourTheme.initText(findViewById(R.id.text2));
        ColourTheme.initText(findViewById(R.id.text3));
        ColorStateList stateList = new ColorStateList(
                new int[][]{new int[]{-android.R.attr.state_checked}, new int[]{android.R.attr.state_checked}},
                new int[]{accent,accent}
        );

        CustomSnackbar.setAccentColor(accent);
        CustomSnackbar.setBgColor(accent2);
        for(int i =0;i<buttons.getChildCount();i++){
            RadioButton button = (RadioButton) buttons.getChildAt(i);
            button.setTextColor(accent);
            button.setButtonTintList(stateList);
        }
        for(int i =0;i<triggers.getChildCount();i++){
            RadioButton button = (RadioButton) triggers.getChildAt(i);
            button.setTextColor(accent);
            button.setButtonTintList(stateList);
        }
        for(int i =0;i<actions.getChildCount();i++){
            RadioButton button = (RadioButton) actions.getChildAt(i);
            button.setTextColor(accent);
            button.setButtonTintList(stateList);
        }
    }
    protected void runOpenAnimation(AnimatorListenerAdapter adapter){
        ANIMATION_IN_PROGRESS = true;
        int width = getResources().getDisplayMetrics().widthPixels;
        int height =getResources().getDisplayMetrics().heightPixels;
        hCard.setTranslationY(-500);
        findViewById(R.id.cardButton).setTranslationX(-width/2f);
        findViewById(R.id.cardTrigger).setTranslationX(width/2f);
        findViewById(R.id.cardAction).setTranslationY(height-50);
        findViewById(R.id.hCard).animate().translationY(0).setDuration(ANIMATION_DURATION).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                findViewById(R.id.cardButton).animate().translationX(0).setDuration(ANIMATION_DURATION);
                findViewById(R.id.cardTrigger).animate().translationX(0).setDuration(ANIMATION_DURATION).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        findViewById(R.id.cardAction).animate().translationY(0).setDuration(ANIMATION_DURATION).setListener(adapter);
                        ANIMATION_IN_PROGRESS = false;
                    }
                });
            }
        });

    }
    protected void runCloseAnimation(AnimatorListenerAdapter adapter){
        ANIMATION_IN_PROGRESS = true;
        int width = getResources().getDisplayMetrics().widthPixels;
        int height =getResources().getDisplayMetrics().heightPixels;
        findViewById(R.id.cardAction).animate().translationY(height-50).setDuration(ANIMATION_DURATION).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                findViewById(R.id.cardButton).animate().translationX(-width/2f).setDuration(ANIMATION_DURATION);
                findViewById(R.id.cardTrigger).animate().translationX(width/2f).setDuration(ANIMATION_DURATION).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        findViewById(R.id.hCard).animate().translationY(-500).setDuration(ANIMATION_DURATION).setListener(adapter);
                        ANIMATION_IN_PROGRESS = false;
                    }
                });
            }
        });
    }
    public String screenFrame(){
        String frame;
        frame = Settings.System.getString(getContentResolver(),"peak_refresh_rate");
        if(frame==null)frame = Settings.System.getString(getContentResolver(),"user_refresh_rate");
        return frame;
    }
    protected void showDialog(String message, View.OnClickListener onContinueClick){
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