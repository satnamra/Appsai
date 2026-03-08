package com.example.quicknotes;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.button.MaterialButton;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private MaterialButton btnNext;
    private TextView btnSkip;
    private LinearLayout indicatorLayout;
    private ImageView[] dots;
    private static final int PAGE_COUNT = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("quicknotes_prefs", MODE_PRIVATE);
        if (prefs.getBoolean("onboarding_done", false)) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.viewPager);
        btnNext = findViewById(R.id.btnNext);
        btnSkip = findViewById(R.id.btnSkip);
        indicatorLayout = findViewById(R.id.indicatorLayout);

        viewPager.setAdapter(new OnboardingAdapter());
        setupDots(0);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateDots(position);
                if (position == PAGE_COUNT - 1) {
                    btnNext.setText("Get Started");
                    btnSkip.setVisibility(View.INVISIBLE);
                } else {
                    btnNext.setText("Next");
                    btnSkip.setVisibility(View.VISIBLE);
                }
            }
        });

        btnNext.setOnClickListener(v -> {
            int current = viewPager.getCurrentItem();
            if (current < PAGE_COUNT - 1) {
                viewPager.setCurrentItem(current + 1);
            } else {
                finishOnboarding();
            }
        });

        btnSkip.setOnClickListener(v -> finishOnboarding());
    }

    private void setupDots(int current) {
        indicatorLayout.removeAllViews();
        dots = new ImageView[PAGE_COUNT];
        int dpSize = (int) (8 * getResources().getDisplayMetrics().density);
        int dpMargin = (int) (6 * getResources().getDisplayMetrics().density);

        for (int i = 0; i < PAGE_COUNT; i++) {
            dots[i] = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dpSize, dpSize);
            params.setMargins(dpMargin, 0, dpMargin, 0);
            dots[i].setLayoutParams(params);
            dots[i].setBackgroundResource(i == current
                ? R.drawable.dot_active
                : R.drawable.dot_inactive);
            indicatorLayout.addView(dots[i]);
        }
    }

    private void updateDots(int current) {
        for (int i = 0; i < dots.length; i++) {
            dots[i].setBackgroundResource(i == current
                ? R.drawable.dot_active
                : R.drawable.dot_inactive);
        }
    }

    private void finishOnboarding() {
        SharedPreferences prefs = getSharedPreferences("quicknotes_prefs", MODE_PRIVATE);
        prefs.edit().putBoolean("onboarding_done", true).apply();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
