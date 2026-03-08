package com.example.quicknotes;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;

/**
 * Application class — sets night mode as early as possible to prevent
 * theme flicker and reduce cold-start ANR risk.
 */
public class QuickNotesApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Apply night mode early (before any Activity creates its window)
        int theme = ThemeManager.getTheme(this);
        switch (theme) {
            case ThemeManager.THEME_DARK:
            case ThemeManager.THEME_AMOLED:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case ThemeManager.THEME_SEPIA:
            case ThemeManager.THEME_LIGHT:
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
        }
    }
}
