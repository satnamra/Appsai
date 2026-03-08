package com.example.quicknotes;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class NoteActivityTest {

    static Intent makeIntent() {
        Context ctx = ApplicationProvider.getApplicationContext();
        ctx.getSharedPreferences("quicknotes_prefs", Context.MODE_PRIVATE)
           .edit().putBoolean("onboarding_done", true).apply();
        return new Intent(ctx, NoteActivity.class);
    }

    @Rule
    public ActivityScenarioRule<NoteActivity> rule =
            new ActivityScenarioRule<>(makeIntent());

    @Test
    public void noteActivity_titleFieldVisible() {
        Espresso.onView(ViewMatchers.withId(R.id.titleEditText))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void noteActivity_contentFieldVisible() {
        Espresso.onView(ViewMatchers.withId(R.id.contentEditText))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void noteActivity_charCounterUpdates() {
        Espresso.onView(ViewMatchers.withId(R.id.contentEditText))
                .perform(ViewActions.typeText("Hello"),
                         ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.characterCounter))
                .check(ViewAssertions.matches(ViewMatchers.withText("5 / 1000")));
    }

    @Test
    public void noteActivity_saveBtnVisible() {
        Espresso.onView(ViewMatchers.withId(R.id.saveButton))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void noteActivity_discardBtnVisible() {
        Espresso.onView(ViewMatchers.withId(R.id.discardButton))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void noteActivity_saveWithoutTitle_showsError() {
        Espresso.onView(ViewMatchers.withId(R.id.saveButton))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.titleEditText))
                .check(ViewAssertions.matches(
                    ViewMatchers.hasErrorText("Title is required")));
    }

    @Test
    public void noteActivity_colorPickerVisible() {
        Espresso.onView(ViewMatchers.withId(R.id.colorPicker))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void noteActivity_tagsFieldVisible() {
        Espresso.onView(ViewMatchers.withId(R.id.tagsEditText))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void noteActivity_charCounter_multipleChars() {
        String text = "Hello World";
        Espresso.onView(ViewMatchers.withId(R.id.contentEditText))
                .perform(ViewActions.typeText(text),
                         ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.characterCounter))
                .check(ViewAssertions.matches(
                    ViewMatchers.withText(text.length() + " / 1000")));
    }
}
