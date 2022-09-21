package com.legendsayantan.screenery;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;

import java.util.Timer;
import java.util.TimerTask;

public class FrameActivity extends AppCompatActivity {

    private static final long ANIMATION_DURATION = 250;
    private static boolean ANIMATION_IN_PROGRESS = false;
    private SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    MaterialCardView hCard, btn1, btn2, btn3;
    TextView defRate;
    EditText text ;
    Button applyButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frame);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        sharedPreferences  = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = sharedPreferences.edit();
        hCard = findViewById(R.id.hCard);
        btn1 =findViewById(R.id.fps60);
        btn2 =findViewById(R.id.fps90);
        btn3 =findViewById(R.id.fps120);
        defRate=findViewById(R.id.defRate);
        text = findViewById(R.id.editText);
        applyButton = findViewById(R.id.button);
        if(sharedPreferences.getInt("frame",-1)==-1)editor.putInt("frame", (int) Float.parseFloat(screenFrame())).apply();
        defRate.setText(sharedPreferences.getInt("frame",60)==0?"MAX":sharedPreferences.getInt("frame",60)+" ");
        btn1.setOnClickListener(v -> {
            setFrameRate(60);
        });
        btn2.setOnClickListener(v -> {
            setFrameRate(90);
        });
        btn3.setOnClickListener(v -> {
            setFrameRate(0);
        });
    }
    @Override
    protected void onResume() {
        refreshTheme();
        runOpenAnimation(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {

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
                FrameActivity.super.onBackPressed();
            }
        });
    }

    protected void refreshTheme(){
        ColourTheme.init(FrameActivity.this);
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
        int median = ColourTheme.getMedianColour();
        findViewById(R.id.card1).setBackgroundColor(accent2);
        findViewById(R.id.card2).setBackgroundColor(accent2);
        findViewById(R.id.cardControl1).setBackgroundColor(median);
        findViewById(R.id.cardControl2).setBackgroundColor(median);
        ColourTheme.initCard(btn1);
        ColourTheme.initCard(btn2);
        ColourTheme.initCard(btn3);
        ColourTheme.initText(defRate);
        ColourTheme.initText(findViewById(R.id.text1));
        ColourTheme.initText(findViewById(R.id.text2));
        ColourTheme.initText(findViewById(R.id.text3));
        ColourTheme.initText(findViewById(R.id.text4));
        ColourTheme.initText(findViewById(R.id.text5));
        ColourTheme.initText(findViewById(R.id.text6));
        ColourTheme.initText(findViewById(R.id.text7));
        ColorStateList stateList = new ColorStateList(
                new int[][]{new int[]{-android.R.attr.state_checked}, new int[]{android.R.attr.state_checked}},
                new int[]{accent,accent}
        );
        text.setBackgroundTintList(stateList);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            text.setTextCursorDrawable(null);
        }
        text.setTextColor(accent);
        applyButton.setBackgroundTintList(stateList);
        applyButton.setTextColor(accent2);
        CustomSnackbar.setAccentColor(accent);
        CustomSnackbar.setBgColor(accent2);
    }
    protected void runOpenAnimation(AnimatorListenerAdapter adapter){
        ANIMATION_IN_PROGRESS = true;
        int width = getResources().getDisplayMetrics().widthPixels;
        hCard.setTranslationY(-500);
        findViewById(R.id.card1).setTranslationX(-width/2f);
        findViewById(R.id.card2).setTranslationX(width/2f);
        findViewById(R.id.cardControl1).setTranslationX(-width);
        findViewById(R.id.cardControl2).setTranslationX(width);
        findViewById(R.id.hCard).animate().translationY(0).setDuration(ANIMATION_DURATION).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                findViewById(R.id.card1).animate().translationX(0).setDuration(ANIMATION_DURATION);
                findViewById(R.id.card2).animate().translationX(0).setDuration(ANIMATION_DURATION).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        findViewById(R.id.cardControl1).animate().translationX(0).setDuration(ANIMATION_DURATION);
                        findViewById(R.id.cardControl2).animate().translationX(0).setDuration(ANIMATION_DURATION).setListener(adapter);

                    }
                });
            }
        });
        ANIMATION_IN_PROGRESS = false;
    }
    protected void runCloseAnimation(AnimatorListenerAdapter adapter){
        ANIMATION_IN_PROGRESS = true;
        int width = getResources().getDisplayMetrics().widthPixels;
        findViewById(R.id.cardControl1).animate().translationX(-width).setDuration(ANIMATION_DURATION);
        findViewById(R.id.cardControl2).animate().translationX(width).setDuration(ANIMATION_DURATION).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                findViewById(R.id.card1).animate().translationX(-width/2f).setDuration(ANIMATION_DURATION);
                findViewById(R.id.card2).animate().translationX(width/2f).setDuration(ANIMATION_DURATION).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        findViewById(R.id.hCard).animate().translationY(-500).setDuration(ANIMATION_DURATION).setListener(adapter);
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
    public void setFrameRate(int fps){
        if(!Settings.System.canWrite(getApplicationContext())){
            new CustomSnackbar(applyButton,"PERMISSION ERROR",FrameActivity.this,0);
            return;
        }
        String initialframerate = screenFrame();
        try {
            if(Settings.System.getString(getContentResolver(),"peak_refresh_rate")!=null){
                runCloseAnimation(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        Settings.System.putString(getContentResolver(),"peak_refresh_rate", String.valueOf(fps));
                        Timer timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Settings.System.putString(getContentResolver(),"user_refresh_rate", initialframerate);
                            }
                        },5000);
                        showDialog("THIS IS A DEMO\nPress Continue to apply permanently.\nDisplay will be reverted back after 5 seconds.", v -> {
                            timer.cancel();
                        });
                    }
                });
            }else{
                runCloseAnimation(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        Settings.System.putString(getContentResolver(),"user_refresh_rate", String.valueOf(fps));
                        Timer timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Settings.System.putString(getContentResolver(),"user_refresh_rate", initialframerate);
                            }
                        },5000);
                        showDialog("THIS IS A DEMO\nPress Continue to apply permanently.\nDisplay will be reverted back after 5 seconds.", v -> {
                            timer.cancel();
                        });
                    }
                });
            }
        }catch (Exception e){
            Settings.System.putString(getContentResolver(),"user_refresh_rate", initialframerate);
        }
    }
}