package com.legendsayantan.screenery;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.service.quicksettings.Tile;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.larswerkman.holocolorpicker.ColorPicker;

import java.util.ArrayList;

/**
 * @author legendsayantan
 */

public class WakeActivity extends AppCompatActivity {
    CheckBox checkBox;
    MaterialButton timerBtn,colorBtn;
    int ANIMATION_DURATION = 250;
    ArrayList<RadioButton> radioButtons = new ArrayList<>();
    private boolean ANIMATION_IN_PROGRESS = false;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    static MaterialCardView hCard;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wake);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        sharedPreferences  = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = sharedPreferences.edit();
        hCard = findViewById(R.id.hCard);
        radioButtons.add(findViewById(R.id.radioButton1));
        radioButtons.add(findViewById(R.id.radioButton2));
        radioButtons.add(findViewById(R.id.radioButton3));
        radioButtons.add(findViewById(R.id.radioButton11));
        radioButtons.add(findViewById(R.id.radioButton21));
        checkBox = findViewById(R.id.checkBox);
        timerBtn = findViewById(R.id.button2);
        colorBtn = findViewById(R.id.button21);
        checkBox.setChecked(sharedPreferences.getBoolean("sleepDetect",false));
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED){
                    editor.putBoolean("sleepDetect", isChecked).apply();
                }
                else {
                    buttonView.setChecked(false);
                    new CustomSnackbar(buttonView, "Grant activity recognition for sleep detection.", WakeActivity.this, 0);
                    runCloseAnimation(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 230);
                        }
                    });
                }
            }else{
                requestPermissions(new String[]{"com.google.android.gms.permission.ACTIVITY_RECOGNITION"},230);
                new CustomSnackbar(buttonView, "Sleep detection is supported on Android 10 and later versions.", WakeActivity.this, 0);
                buttonView.setChecked(false);
            }
        });
        RadioGroup r1=findViewById(R.id.wakeSettings);
        r1.check(r1.getChildAt(sharedPreferences.getInt("wakeSettings",0)).getId());
        r1.setOnCheckedChangeListener((group, checkedId) -> editor.putInt("wakeSettings",r1.indexOfChild(findViewById(checkedId))).apply());
        RadioGroup r2=findViewById(R.id.wakeOverlay);
        r2.check(r2.getChildAt(sharedPreferences.getInt("wakeOverlay",1)).getId());
        r2.setOnCheckedChangeListener((group, checkedId) -> editor.putInt("wakeOverlay",r2.indexOfChild(findViewById(checkedId))).apply());
        timerBtn.setText(sharedPreferences.getInt("wakeTime",90)/60+"h "+sharedPreferences.getInt("wakeTime",90)%60+"min");
        timerBtn.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    (view, hourOfDay, minute) -> {
                        if((hourOfDay*60+minute)<5){
                            new CustomSnackbar(hCard,"Warning : Timer too small.",WakeActivity.this,0);
                        }
                        editor.putInt("wakeTime",(hourOfDay*60+minute)).apply();
                        timerBtn.setText(hourOfDay+"h "+minute+"min");
                    },
                    sharedPreferences.getInt("wakeTime",90)/60,
                    sharedPreferences.getInt("wakeTime",90)%60,
                    true);
            timePickerDialog.show();
        });
        if(sharedPreferences.getInt("wakeColor", -1)==-1)
            editor.putInt("wakeColor",ColourTheme.getAccentColor()).apply();
        colorBtn.setOnClickListener(v -> {
            int[] color = new int[1];
            color[0] = sharedPreferences.getInt("wakeColor",ColourTheme.getAccentColor());
            ColorPicker colorPicker = new ColorPicker(getApplicationContext());
            CardView cardView = new CardView(getApplicationContext());
            cardView.setRadius(100);
            cardView.setBackgroundColor(getColor(R.color.zero));
            LinearLayout linearLayout = new LinearLayout(getApplicationContext());
            linearLayout.setGravity(Gravity.CENTER);
            linearLayout.setBackgroundColor(ColourTheme.getSecondaryAccentColor());
            colorPicker.setColor(color[0]);
            colorPicker.setOldCenterColor(color[0]);
            linearLayout.addView(colorPicker);
            colorPicker.setOnColorSelectedListener(color1 -> color[0] = color1);
            cardView.addView(linearLayout);
            Dialog dialog = new Dialog(WakeActivity.this);
            dialog.addContentView(cardView,
                    new ViewGroup.LayoutParams(
                            (int) (getResources().getDisplayMetrics().widthPixels*0.5),
                            (int) (getResources().getDisplayMetrics().widthPixels*0.5)
                    ));
            dialog.show();
            dialog.setOnDismissListener(dialog1 -> {
                editor.putInt("wakeColor",color[0]).apply();
                colorBtn.setStrokeColor(ColorStateList.valueOf(color[0]));
            });
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        refreshTheme();
        runOpenAnimation(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    WakeTileService.requestListeningState(getApplicationContext(),
                            new ComponentName(getApplicationContext(),WakeTileService.class));
                    try {
                        wakeCardToggle(WakeTileService.qsTile.getState());
                    }catch (NullPointerException ignored){}
                }
            }
        });
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        if(ANIMATION_IN_PROGRESS)return;
        if(hCard.getStrokeWidth()!=0){
            new CustomSnackbar(hCard,"Toggle Screen Wake to apply changes.",WakeActivity.this,ANIMATION_DURATION* 2L);
        }
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ) {
                WakeTileService.requestListeningState(getApplicationContext(),
                        new ComponentName(getApplicationContext(),WakeTileService.class));
                if (hCard.getStrokeWidth() == 0) {
                    try {
                        WakeFloatingService.killSelf();
                    }catch (Exception e){
                        WakeTileService.disableTile();
                    }
                } else {
                    WakeTileService.enableTile(getApplicationContext());
                }
            }else{
                if (hCard.getStrokeWidth() == 0) {
                    try {
                        WakeFloatingService.killSelf();
                    }catch (Exception e){}
                } else {
                    startWake(getApplicationContext());
                }
            }
        });
        ColourTheme.initCard(findViewById(R.id.wakelockCard));
        ColourTheme.initCard(findViewById(R.id.wakelockOverlayCard));
        ColourTheme.initTextView(findViewById(R.id.header));
        ColourTheme.initText(findViewById(R.id.textView2));
        ColourTheme.initText(findViewById(R.id.textView21));

        int accent = ColourTheme.getAccentColor();
        int accent2 = ColourTheme.getSecondaryAccentColor();
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
        colorBtn.setStrokeColor(ColorStateList.valueOf(sharedPreferences.getInt("wakeColor", ColourTheme.getAccentColor())));
        colorBtn.setStrokeWidth(5);
        CustomSnackbar.setAccentColor(ColourTheme.getAccentColor());
        CustomSnackbar.setBgColor(ColourTheme.getSecondaryAccentColor());
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
    public static void wakeCardToggle(int tileState){
        if(tileState==Tile.STATE_ACTIVE){
            hCard.setStrokeWidth(10);
        }else hCard.setStrokeWidth(0);
    }
    public static void startWake(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O )
            context.startForegroundService(new Intent(context, WakeFloatingService.class));
        else
            context.startService(new Intent(context,WakeFloatingService.class));
    }
}