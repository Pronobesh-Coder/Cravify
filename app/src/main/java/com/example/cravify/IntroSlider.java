package com.example.cravify;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class IntroSlider extends AppCompatActivity {

    private ViewPager screenPager;
    private IntroViewPagerAdapter introViewPagerAdapter;
    private TabLayout tabIndicator;
    private Button btnNext;
    private int position = 0;
    private Button btnGetStarted;
    private Animation btnAnim;
    private TextView tvSkip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_intro_slider);

        if (restorePrefData()) {
            Intent userLoginActivity = new Intent(getApplicationContext(), UserLogin.class);
            startActivity(userLoginActivity);
            finish();
        }

        btnNext = findViewById(R.id.btn_next);
        btnGetStarted = findViewById(R.id.btn_get_started);
        tabIndicator = findViewById(R.id.tab_indicator);
        btnAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.button_animation);
        tvSkip = findViewById(R.id.tv_skip);

        final List<ScreenItem> mList = new ArrayList<>();
        mList.add(new ScreenItem("Fresh Food, Always!", "Savor the taste of quality with every meal. Handpicked ingredients ensure your food is fresh and delicious, every single time!", R.drawable.slider_img1));
        mList.add(new ScreenItem("Lightning-Fast Delivery", "From our kitchen to your doorstep in record time. Enjoy piping hot meals delivered swiftly, right when you need them.", R.drawable.slider_img2));
        mList.add(new ScreenItem("Effortless Payments", "Seamless transactions for a stress-free experience. Our secure and easy payment options make ordering a breeze!", R.drawable.slider_img3));

        // setup viewpager
        screenPager = findViewById(R.id.screen_viewpager);
        introViewPagerAdapter = new IntroViewPagerAdapter(this, mList);
        screenPager.setAdapter(introViewPagerAdapter);

        tabIndicator.setupWithViewPager(screenPager);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                position = screenPager.getCurrentItem();
                if (position < mList.size()) {
                    position++;
                    screenPager.setCurrentItem(position);
                }
                if (position == mList.size() - 1) {
                    loadLastScreen();
                }
            }
        });
        tabIndicator.addOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == mList.size() - 1) {
                    loadLastScreen();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        btnGetStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent userLoginActivity = new Intent(getApplicationContext(), UserLogin.class);
                startActivity(userLoginActivity);
                savePrefsData();
                finish();
            }
        });
        tvSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                screenPager.setCurrentItem(mList.size());
            }
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private boolean restorePrefData() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("myPrefs", MODE_PRIVATE);
        return pref.getBoolean("isIntroOpnend", false);
    }

    private void savePrefsData() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("myPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("isIntroOpnend", true);
        editor.apply();
    }
    private void launchLoginScreen() {
        SharedPreferences preferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isFirstRun", false);
        editor.apply();

        Intent loginIntent = new Intent(IntroSlider.this, UserLogin.class);
        startActivity(loginIntent);
        finish();
    }

    private void loadLastScreen() {
        btnNext.setVisibility(View.INVISIBLE);
        btnGetStarted.setVisibility(View.VISIBLE);
        tvSkip.setVisibility(View.INVISIBLE);
        tabIndicator.setVisibility(View.INVISIBLE);
        btnGetStarted.setAnimation(btnAnim);
    }
}
