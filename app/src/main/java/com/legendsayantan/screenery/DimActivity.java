package com.legendsayantan.screenery;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.service.quicksettings.Tile;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author legendsayantan
 */

public class DimActivity extends AppCompatActivity {
    MaterialButton colorBtn;
    int ANIMATION_DURATION = 250;
    private boolean ANIMATION_IN_PROGRESS = false;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    static MaterialCardView hCard;
    ArrayList<RadioButton> radioButtons = new ArrayList<>();
    FloatingActionMenu menu;
    FloatingActionButton fab ;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dim);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        sharedPreferences  = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = sharedPreferences.edit();
        hCard = findViewById(R.id.hCard);
        radioButtons.add(findViewById(R.id.radioButton1));
        radioButtons.add(findViewById(R.id.radioButton2));
        radioButtons.add(findViewById(R.id.radioButton3));
        colorBtn = findViewById(R.id.button21);
        fab=findViewById(R.id.floatingActionButton);
        RadioGroup r2=findViewById(R.id.wakeOverlay);
        r2.check(r2.getChildAt(sharedPreferences.getInt("dimOverlay",1)).getId());
        r2.setOnCheckedChangeListener((group, checkedId) -> {
            editor.putInt("dimOverlay",r2.indexOfChild(findViewById(checkedId))).apply();
            if(hCard.getStrokeWidth()!=0)new CustomSnackbar(hCard,"Toggle Screen Dim to apply changes.", DimActivity.this,0);
        });
        if(sharedPreferences.getInt("dimColor", -1)==-1)
            editor.putInt("dimColor",ColourTheme.getAccentColor()).apply();
        colorBtn.setOnClickListener(v -> {
            runCloseAnimation(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    int[] color = new int[1];
                    color[0] = sharedPreferences.getInt("dimColor",ColourTheme.getAccentColor());
                    ColorPicker colorPicker = new ColorPicker(getApplicationContext());
                    CardView cardView = new CardView(getApplicationContext());
                    cardView.setRadius(100);
                    cardView.setCardBackgroundColor(ColourTheme.getSecondaryAccentColor());
                    cardView.setBackgroundResource(R.drawable.cardviewborder);
                    LinearLayout linearLayout = new LinearLayout(getApplicationContext());
                    linearLayout.setGravity(Gravity.CENTER);
                    linearLayout.setBackgroundColor(ColourTheme.getSecondaryAccentColor());
                    colorPicker.setColor(color[0]);
                    colorPicker.setOldCenterColor(color[0]);
                    linearLayout.addView(colorPicker);
                    colorPicker.setOnColorSelectedListener(color1 -> color[0] = color1);
                    cardView.addView(linearLayout);
                    Dialog dialog = new Dialog(DimActivity.this);
                    dialog.addContentView(cardView,
                            new ViewGroup.LayoutParams(
                                    (int) (getResources().getDisplayMetrics().widthPixels*0.5),
                                    (int) (getResources().getDisplayMetrics().widthPixels*0.5)
                            ));
                    dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                    dialog.show();
                    dialog.setOnDismissListener(dialog1 -> {
                        editor.putInt("dimColor",color[0]).apply();
                        colorBtn.setStrokeColor(ColorStateList.valueOf(color[0]));
                        if(hCard.getStrokeWidth()!=0)new CustomSnackbar(hCard,"Toggle Screen Dim to apply changes.", DimActivity.this,0);
                        runOpenAnimation(null);
                    });
                }
            });
        });
        findViewById(R.id.scrollView).setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            try {
                if(menu.isOpen())menu.close(true);
            }catch (Exception ignored){}
        });
    }

    private void dimSelect(int intensity){
        menu.close(true);
        editor.putInt("dimIntensity",intensity).apply();
        new CustomSnackbar(hCard,"Dim intensity set to "+intensity+"%.", DimActivity.this,0);
    }

    @Override
    protected void onPause() {
        try {
            menu.close(false);
        }catch (Exception e){}
        super.onPause();
    }

    @Override
    protected void onResume() {
        refreshTheme();
        runOpenAnimation(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    DimTileService.requestListeningState(getApplicationContext(),
                            new ComponentName(getApplicationContext(),DimTileService.class));
                    try {
                        dimCardToggle(DimTileService.qsTile.getState());
                    }catch (NullPointerException ignored){}
                }
            }
        });
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        if(ANIMATION_IN_PROGRESS)return;
        runCloseAnimation(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                startActivity(new Intent(getApplicationContext(),MainActivity.class)
                        .putExtra("action",-1)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                finish();
            }
        });
    }

    protected void refreshTheme(){
        ColourTheme.init(DimActivity.this);
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
                        DimFloatingService.killSelf();
                    }catch (Exception e){
                        DimTileService.disableTile();
                    }
                } else {
                    DimTileService.enableTile(getApplicationContext());
                }
            }else{
                if (hCard.getStrokeWidth() == 0) {
                    try {
                        DimFloatingService.killSelf();
                    }catch (Exception e){}
                } else {
                    startDim(getApplicationContext());
                }
            }
        });
        ColourTheme.initCard(findViewById(R.id.wakelockOverlayCard));
        ColourTheme.initTextView(findViewById(R.id.header));
        ColourTheme.initText(findViewById(R.id.textView21));
        int accent = ColourTheme.getAccentColor();
        int accent2 = ColourTheme.getSecondaryAccentColor();
        ColorStateList stateList = new ColorStateList(
                new int[][]{new int[]{-android.R.attr.state_checked}, new int[]{android.R.attr.state_checked}},
                new int[]{accent,accent}
        );
        for(RadioButton r : radioButtons){
            r.setTextColor(accent);
            r.setButtonTintList(stateList);
        }
        fab.setBackgroundTintList(ColorStateList.valueOf(accent2));
        fab.setRippleColor(accent);
        fab.setForegroundTintList(ColorStateList.valueOf(accent));
        colorBtn.setBackgroundTintList(stateList);
        colorBtn.setTextColor(accent2);
        colorBtn.setStrokeColor(ColorStateList.valueOf(sharedPreferences.getInt("dimColor", ColourTheme.getAccentColor())));
        colorBtn.setStrokeWidth(5);
        CustomSnackbar.setAccentColor(ColourTheme.getAccentColor());
        CustomSnackbar.setBgColor(ColourTheme.getSecondaryAccentColor());
        SubActionButton.Builder itemBuilder = new SubActionButton.Builder(this);
        ArrayList<SubActionButton> buttons = new ArrayList<>();
        int savedDim = sharedPreferences.getInt("dimIntensity",50);
        for(int i=8;i>=0;i--){
            int percentage = 10+(10*i);
            TextView textView = new TextView(getApplicationContext());
            textView.setText(String.valueOf(percentage));
            SubActionButton subActionButton = itemBuilder.setContentView(textView).build();
            subActionButton.setScaleX(1.5f);
            subActionButton.setScaleY(1.5f);
            subActionButton.setOnClickListener((v -> dimSelect(percentage)));
            ColourTheme.initTextView(textView);
            subActionButton.setBackgroundTintList(ColorStateList.valueOf(accent2));
            buttons.add(subActionButton);
        }
        menu = new FloatingActionMenu.Builder(this)
                .addSubActionView(buttons.get(0))
                .addSubActionView(buttons.get(1))
                .addSubActionView(buttons.get(2))
                .addSubActionView(buttons.get(3))
                .addSubActionView(buttons.get(4))
                .addSubActionView(buttons.get(5))
                .addSubActionView(buttons.get(6))
                .addSubActionView(buttons.get(7))
                .addSubActionView(buttons.get(8))
                .attachTo(fab)
                .setStartAngle(140)
                .setEndAngle(-180)
                .setStateChangeListener(new FloatingActionMenu.MenuStateChangeListener() {
                    @Override
                    public void onMenuOpened(FloatingActionMenu floatingActionMenu) {
                        for(FloatingActionMenu.Item item:floatingActionMenu.getSubActionItems()){
                            item.view.setForeground(null);
                        }
                        floatingActionMenu.getSubActionItems().get((100-sharedPreferences.getInt("dimIntensity",50))/10-1)
                                .view.setForeground(AppCompatResources.getDrawable(getApplicationContext(),R.drawable.ic_outline_circle_24));
                    }
                    @Override
                    public void onMenuClosed(FloatingActionMenu floatingActionMenu) {}
                })
                .build();
    }
    protected void runOpenAnimation(AnimatorListenerAdapter adapter){
        ANIMATION_IN_PROGRESS = true;
        int width = getResources().getDisplayMetrics().widthPixels;
        findViewById(R.id.hCard).setTranslationY(-500);
        fab.setScaleX(0);
        fab.setScaleY(0);
        findViewById(R.id.wakelockOverlayCard).setTranslationX(width);
        findViewById(R.id.hCard).animate().translationY(0).setDuration(ANIMATION_DURATION).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                findViewById(R.id.wakelockOverlayCard).animate().translationX(100).setDuration(ANIMATION_DURATION).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        fab.animate().scaleX(1).setDuration(ANIMATION_DURATION);
                        fab.animate().scaleY(1).setDuration(ANIMATION_DURATION).setListener(adapter);
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                runOnUiThread(()->menu.open(true));
                            }
                        },ANIMATION_DURATION);
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
        findViewById(R.id.hCard).setTranslationY(0);
        long delay = 0;
        if(menu.isOpen()){
            menu.close(true);
            delay=ANIMATION_DURATION* 2L;
        }
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                fab.animate().scaleX(0).setDuration(ANIMATION_DURATION);
                fab.animate().scaleY(0).setDuration(ANIMATION_DURATION).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        findViewById(R.id.wakelockOverlayCard).animate().translationX(width).setDuration(ANIMATION_DURATION).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                findViewById(R.id.hCard).animate().translationY(-500).setDuration(ANIMATION_DURATION).setListener(adapter);
                            }
                        });
                    }
                });
            }
        },delay);
        ANIMATION_IN_PROGRESS = false;
    }
    public static void dimCardToggle(int tileState){
        if(tileState==Tile.STATE_ACTIVE){
            hCard.setStrokeWidth(10);
        }else hCard.setStrokeWidth(0);
    }
    public static void startDim(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O )
            context.startForegroundService(new Intent(context, DimFloatingService.class));
        else
            context.startService(new Intent(context,DimFloatingService.class));
    }
}