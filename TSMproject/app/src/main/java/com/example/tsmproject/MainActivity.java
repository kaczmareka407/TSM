package com.example.tsmproject;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback;

import java.sql.Timestamp;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements OnUserEarnedRewardListener {

    public static final String LAST_CLOSE_COUNTER_VALUE = "lastCloseCounterValue";
    public static final String LAST_CLOSE_DATE = "lastCloseDate";
    public static final int TIME_PERIOD = 1000;
    public static final String MY_PREFS = "myPrefs";
    public static final String FREEZE_TIME = "freezeTime";
    public static final String FREEZE_COUNTER = "freezeCounter";
    public static final int SECONDS = 1;
    public static final int COUNTER_DEFAULT_VALUE = SECONDS * 100;
    public static final int FREEZE_COUNTER_DEFAULT_VALUE = 1 * 20;
    public static final String REWARDER_INTERSTITIAL_AD_ID = "ca-app-pub-3940256099942544/5354046379";
    public static final int REWARD_COLOR = 0xff0047a3;
    public static final int DEFAULT_COLOR = 0xff004400;
    public static final int DEFEAT_COLOR = 0xff6b0404;
    public static final String MAGIC_WATER_MESSAGE = "If you watch an ad your plant will not lose HP for some time";
    public static final String NEW_PLANT_MESSAGE = "If you watch an ad you will get a new fresh plant";

    private final float healthBarRGB[] = {0, 1.f, 0};

    RewardedInterstitialAd rewardedInterstitialAd;
    ProgressBar pb;
    AdView adView;
    AdRequest adRequest;
    Button buttonWater;
    Button buttonMagicWater;
    SharedPreferences sharedPreferences;
    TextView freezeTimerTextField;
    ImageView imageView;

    int counter;
    int freezeCounter;
    boolean freezeTime = false;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        adView = (AdView) findViewById(R.id.adView);
        MobileAds.initialize(this);
        adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        loadAd();
        loadData();

        int elapsedTime = calculateElapsedTime();
        if (freezeTime) {
            freezeCounter -= elapsedTime;
            setFreezeTimerTextValue();
            pb.setProgress(counter);
            setPremiumRewardUiColors();
        } else {
            setDefaultUiColors();
            if (elapsedTime >= COUNTER_DEFAULT_VALUE) {
                counter = 0;
            } else {
                counter = counter - elapsedTime;
            }
        }
        if (counter < 1) {
            imageView.setImageResource(R.drawable.dead_flower);
            setDefeatUiColors();
        }
        prog();
    }

    public void prog() {


        final Timer t = new Timer();
        TimerTask tt = new TimerTask() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                if (freezeTime) {
                    if (freezeCounter == 0) {
                        freezeTime = false;
                        freezeTimerTextField.setText("");
                        setDefaultUiColors();
                    } else {
                        freezeCounter--;
                        setFreezeTimerTextValue();
                    }
                } else {
                    pb.setProgress(counter);
                    if (counter == 0) {
                        setDefeatUiColors();
                        imageView.setImageResource(R.drawable.dead_flower);
                    } else {
                        counter--;
                    }
                    updateHPBarColor();
                }
            }
        };
        t.schedule(tt, 0, TIME_PERIOD);
    }

    @Override
    protected void onPause() {
        // jak sie wyłącza apke to sie zapisuje stan
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String timeStamp = new Timestamp(System.currentTimeMillis()).toString();
        System.out.println(timeStamp);
//        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());

        editor.putString(LAST_CLOSE_DATE, timeStamp); // zapis aktualnej daty
        editor.putInt(LAST_CLOSE_COUNTER_VALUE, counter); // zapis aktualnego stanu licznika
        editor.putBoolean(FREEZE_TIME, freezeTime);
        editor.putInt(FREEZE_COUNTER, freezeCounter);
        editor.apply();

        super.onPause();
    }

    //prawdopodobnie niepotrzebne
    @Override
    protected void onResume() {
        sharedPreferences = getApplicationContext().getSharedPreferences(MY_PREFS, MODE_PRIVATE);
        //oblicz czas ktory przeleciał z pauzy

        super.onResume();
    }

    private int calculateElapsedTime() {
        String lastTimestamp = sharedPreferences.getString(LAST_CLOSE_DATE, null);
        if (lastTimestamp == null) {
            return 0;
        }
        String currentTimestamp = new Timestamp(System.currentTimeMillis()).toString();
        System.out.println(currentTimestamp);
//        String currentTimestamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
        long time1 = Timestamp.valueOf(lastTimestamp).getTime();
        long time2 = Timestamp.valueOf(currentTimestamp).getTime();

        //czas w sekundach
        return (int) ((time2 - time1) / TIME_PERIOD);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void updateHPBarColor() {
        healthBarRGB[0] = 1 - (counter / 100.f);
        healthBarRGB[1] = counter / 100.f;
        pb.getProgressDrawable().setColorFilter(
                Color.valueOf(healthBarRGB[0], healthBarRGB[1], healthBarRGB[2], 1.f).toArgb(),
                android.graphics.PorterDuff.Mode.SRC_IN
        );
    }

    private void loadAd() {
        RewardedInterstitialAd.load(this, REWARDER_INTERSTITIAL_AD_ID, adRequest, new RewardedInterstitialAdLoadCallback() {
            @Override
            public void onRewardedInterstitialAdLoaded(@NonNull RewardedInterstitialAd ad) {
                rewardedInterstitialAd = ad;
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void magicWater(View view) {
        if (!freezeTime) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true);
            builder.setTitle("Ad reward confirmation");
            builder.setMessage(counter>0? MAGIC_WATER_MESSAGE : NEW_PLANT_MESSAGE);
            builder.setPositiveButton("Confirm",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            loadAd();
                            if (counter > 0) {
                                freezeTime = true;
                            }
                            rewardedInterstitialAd.show(MainActivity.this, MainActivity.this);
                            adRequest = new AdRequest.Builder().build();
                        }
                    });
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();

        }
    }

    public void water(View view) {
        if (!freezeTime && counter > 0) {
            counter += 30;
            if (counter > 100) counter = 100;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
        if (counter > 0) {
//            System.out.println("NAGRODA");
            freezeTime = true;
            freezeCounter = FREEZE_COUNTER_DEFAULT_VALUE;
            setPremiumRewardUiColors();
        } else {
            counter = 100;
            freezeTime = false;
            setDefaultUiColors();
            imageView.setImageResource(R.drawable.alive_flower);
        }

    }

    private void setFreezeTimerTextValue() {
        String s = freezeCounter / 3600 + "h " + (freezeCounter % 3600) / 60 + "m " + (freezeCounter % 3600) % 60 + "s";
        freezeTimerTextField.setText(s);
    }

    private void loadData() {
        sharedPreferences = getSharedPreferences(MY_PREFS, MODE_PRIVATE);

        counter = sharedPreferences.getInt(LAST_CLOSE_COUNTER_VALUE, COUNTER_DEFAULT_VALUE);
        freezeTime = sharedPreferences.getBoolean(FREEZE_TIME, false);
        freezeCounter = sharedPreferences.getInt(FREEZE_COUNTER, FREEZE_COUNTER_DEFAULT_VALUE);
        pb = (ProgressBar) findViewById(R.id.pb);
        buttonMagicWater = findViewById(R.id.button_magic_water);
        buttonWater = findViewById(R.id.button_water);
        freezeTimerTextField = (TextView) findViewById(R.id.freezeTimerTextField);
        imageView = findViewById(R.id.flowerImage);
    }

    private void setPremiumRewardUiColors() {
        buttonWater.setBackgroundColor(REWARD_COLOR);

        buttonMagicWater.setBackgroundColor(REWARD_COLOR);

        pb.getProgressDrawable().setColorFilter(REWARD_COLOR, PorterDuff.Mode.SRC_IN);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setDefaultUiColors() {
        buttonWater.setBackgroundColor(DEFAULT_COLOR);
        buttonWater.setText("WATER");
        buttonMagicWater.setBackgroundColor(DEFAULT_COLOR);
        buttonMagicWater.setText("MAGIC WATER");
        updateHPBarColor();
    }

    private void setDefeatUiColors() {
        buttonWater.setBackgroundColor(DEFEAT_COLOR);
        buttonWater.setText("-");
        buttonMagicWater.setBackgroundColor(DEFEAT_COLOR);
        buttonMagicWater.setText("NEW PLANT");
    }
}