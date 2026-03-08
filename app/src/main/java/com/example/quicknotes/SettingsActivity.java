package com.example.quicknotes;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class SettingsActivity extends AppCompatActivity {
    private ImageView bgPreview;

    private final ActivityResultLauncher<String[]> imagePicker =
        registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
            if (uri != null) {
                // Persist permission so URI survives reboots
                try {
                    getContentResolver().takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (Exception ignored) {}
                ThemeManager.setBackgroundUri(this, uri.toString());
                bgPreview.setImageURI(uri);
                bgPreview.setVisibility(android.view.View.VISIBLE);
                Toast.makeText(this, "Background set", Toast.LENGTH_SHORT).show();
            }
        });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.apply(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Settings");
        }

        // Theme radio buttons
        RadioGroup themeGroup = findViewById(R.id.themeRadioGroup);
        int current = ThemeManager.getTheme(this);
        switch (current) {
            case ThemeManager.THEME_DARK:   themeGroup.check(R.id.themeDark); break;
            case ThemeManager.THEME_AMOLED: themeGroup.check(R.id.themeAmoled); break;
            case ThemeManager.THEME_SEPIA:  themeGroup.check(R.id.themeSepia); break;
            default:                         themeGroup.check(R.id.themeLight); break;
        }
        themeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int theme;
            if (checkedId == R.id.themeDark)        theme = ThemeManager.THEME_DARK;
            else if (checkedId == R.id.themeAmoled) theme = ThemeManager.THEME_AMOLED;
            else if (checkedId == R.id.themeSepia)  theme = ThemeManager.THEME_SEPIA;
            else                                     theme = ThemeManager.THEME_LIGHT;
            ThemeManager.setTheme(this, theme);
            ThemeManager.apply(this);
            recreate();
        });

        // Background image
        bgPreview = findViewById(R.id.bgPreview);
        String existingUri = ThemeManager.getBackgroundUri(this);
        if (existingUri != null) {
            bgPreview.setImageURI(Uri.parse(existingUri));
            bgPreview.setVisibility(android.view.View.VISIBLE);
        }

        findViewById(R.id.chooseBgBtn).setOnClickListener(v ->
            imagePicker.launch(new String[]{"image/*"}));

        findViewById(R.id.removeBgBtn).setOnClickListener(v -> {
            ThemeManager.setBackgroundUri(this, null);
            bgPreview.setImageURI(null);
            bgPreview.setVisibility(android.view.View.GONE);
            Toast.makeText(this, "Background removed", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
