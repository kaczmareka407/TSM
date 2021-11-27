package com.example.tsmproject;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

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
    public static final int TIME_PERIOD = 100;
    public static final String MY_PREFS = "myPrefs";

    private final float healthBarRGB[] = {0, 1.f, 0};

    private RewardedInterstitialAd rewardedInterstitialAd;


    ProgressBar pb;
    AdView adView;
    AdRequest adRequest;
    int counter = 99;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        adView = (AdView) findViewById(R.id.adView);

        MobileAds.initialize(this);
        adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        loadAd();


        sharedPreferences = getSharedPreferences(MY_PREFS, MODE_PRIVATE);

//        sharedPreferences.edit().clear();
//        sharedPreferences.edit().commit();

        counter = sharedPreferences.getInt(LAST_CLOSE_COUNTER_VALUE, 100);
        int elapsedTime = calculateElapsedTime();

        if (elapsedTime >= 100) {
            counter = 0;
        } else {
            counter = counter - elapsedTime;
        }
        prog();
    }

    public void prog() {
        pb = (ProgressBar) findViewById(R.id.pb);

        final Timer t = new Timer();
        TimerTask tt = new TimerTask() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                pb.setProgress(counter);

                if (counter == 0) {
//                    t.cancel();
                    counter = 99;
                    healthBarRGB[0] = 0;
                    healthBarRGB[1] = 1.f;
                    //roslinka zdycha
                } else {
                    counter--;

                }
                updateHPBarColor();
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
        return (int) ((time2 - time1) / 1000);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void updateHPBarColor() {
        healthBarRGB[0] = 1 - (counter/100.f);
        healthBarRGB[1] = counter/100.f;
        pb.getProgressDrawable().setColorFilter(
                Color.valueOf(healthBarRGB[0], healthBarRGB[1], healthBarRGB[2],1.f).toArgb(),
                android.graphics.PorterDuff.Mode.SRC_IN
        );
    }

    private void loadAd() {
        RewardedInterstitialAd.load(this, "ca-app-pub-3940256099942544/5354046379", adRequest, new RewardedInterstitialAdLoadCallback(){
            @Override
            public void onRewardedInterstitialAdLoaded(@NonNull RewardedInterstitialAd ad) {
                rewardedInterstitialAd = ad;
            }
        });
    }

    public void magicWater(View view) {
        rewardedInterstitialAd.show(this,this);
        adRequest = new AdRequest.Builder().build();
        loadAd();
    }

    @Override
    public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
//        TIME_PERIOD = 10000;
        System.out.println("NAGRODA");
    }

}