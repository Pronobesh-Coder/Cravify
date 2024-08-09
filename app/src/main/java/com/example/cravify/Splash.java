package com.example.cravify;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class Splash extends AppCompatActivity {
    VideoView v_view;

    private static final int SPLASH_DISPLAY_LENGTH = 3500;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        EdgeToEdge.enable(this);

        v_view = findViewById(R.id.animate);
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.animate);
        v_view.setVideoURI(videoUri);

        v_view.setOnCompletionListener(mp -> {
            if (restorePrefData()) {
                Intent mainActivity = new Intent(Splash.this, UserLogin.class);
                startActivity(mainActivity);
            } else {
                Intent introSlider = new Intent(Splash.this, IntroSlider.class);
                startActivity(introSlider);
            }
            finish();
        });

        v_view.start();

        new Handler().postDelayed(() -> {
            if (v_view.isPlaying()) {
                v_view.stopPlayback();
                if (restorePrefData()) {
                    Intent mainActivity = new Intent(Splash.this, UserLogin.class);
                    startActivity(mainActivity);
                } else {
                    Intent introSlider = new Intent(Splash.this, IntroSlider.class);
                    startActivity(introSlider);
                }
                finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }

    private boolean restorePrefData() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("myPrefs", MODE_PRIVATE);
        return pref.getBoolean("isIntroOpnend", false);
    }
}