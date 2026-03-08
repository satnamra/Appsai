package com.example.quicknotes;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatDelegate;

public class ThemeManager {
    private static final String PREF_NAME = "theme_prefs";
    private static final String KEY_THEME = "theme_mode";
    private static final String KEY_BG_URI = "bg_image_uri";

    public static final int THEME_LIGHT = 0;
    public static final int THEME_DARK = 1;
    public static final int THEME_AMOLED = 2;
    public static final int THEME_SEPIA = 3;
    public static final int THEME_STEEL = 4;
    public static final int THEME_ROSE = 5;
    public static final int THEME_BUBBLEGUM = 6;
    public static final int THEME_OCEAN = 7;

    /** Call BEFORE super.onCreate() in each Activity */
    public static void apply(Activity activity) {
        int theme = getTheme(activity);
        switch (theme) {
            case THEME_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case THEME_AMOLED:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                activity.setTheme(R.style.Theme_QuickNotes_AMOLED);
                break;
            case THEME_SEPIA:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                activity.setTheme(R.style.Theme_QuickNotes_Sepia);
                break;
            case THEME_STEEL:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                activity.setTheme(R.style.Theme_QuickNotes_Steel);
                break;
            case THEME_ROSE:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                activity.setTheme(R.style.Theme_QuickNotes_Rose);
                break;
            case THEME_BUBBLEGUM:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                activity.setTheme(R.style.Theme_QuickNotes_Bubblegum);
                break;
            case THEME_OCEAN:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                activity.setTheme(R.style.Theme_QuickNotes_Ocean);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
        }
    }

    public static void applyBackground(Activity activity, ImageView bgView) {
        if (bgView == null) return;
        String uriStr = getBackgroundUri(activity);
        if (uriStr != null) {
            try {
                bgView.setImageURI(Uri.parse(uriStr));
                bgView.setVisibility(android.view.View.VISIBLE);
            } catch (Exception e) {
                bgView.setVisibility(android.view.View.GONE);
            }
        } else {
            bgView.setVisibility(android.view.View.GONE);
        }
    }

    public static void setTheme(Context ctx, int theme) {
        prefs(ctx).edit().putInt(KEY_THEME, theme).apply();
    }

    public static int getTheme(Context ctx) {
        return prefs(ctx).getInt(KEY_THEME, THEME_LIGHT);
    }

    public static void setBackgroundUri(Context ctx, String uri) {
        prefs(ctx).edit().putString(KEY_BG_URI, uri).apply();
    }

    public static String getBackgroundUri(Context ctx) {
        return prefs(ctx).getString(KEY_BG_URI, null);
    }

    private static SharedPreferences prefs(Context ctx) {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
}
