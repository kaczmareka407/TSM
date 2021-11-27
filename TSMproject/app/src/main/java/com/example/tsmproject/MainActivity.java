package com.example.tsmproject;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.os.Build;
import android.os.Bundle;
import android.widget.ProgressBar;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    public static final String LAST_CLOSE_COUNTER_VALUE = "lastCloseCounterValue";
    public static final String LAST_CLOSE_DATE = "lastCloseDate";
    public static final int TIME_PERIOD = 100;
    public static final String MY_PREFS = "myPrefs";

    private final float healthBarRGB[] = {0, 255, 0};

    ProgressBar pb;
    int counter = 99;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                    healthBarRGB[1] = 255;
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
        if (counter < 25) {
            healthBarRGB[1] = 128 * ((counter % 25.f)/25);
        } else if (counter < 50) {
            healthBarRGB[0] = 255 - (128 * ((counter % 25.f)/25));
        } else if (counter < 75) {
            healthBarRGB[1] = 128 + (128 * ((counter % 25.f)/25));
        } else {
            healthBarRGB[0] = 128 - (128 * ((counter % 25.f)/25));
        }
        pb.getProgressDrawable().setColorFilter(
                Color.valueOf(healthBarRGB[0]/255.f, healthBarRGB[1]/255.f, healthBarRGB[2]/255.f,1.f).toArgb(),
                android.graphics.PorterDuff.Mode.SRC_IN
        );
    }

}