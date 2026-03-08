package com.example.quicknotes;

import android.content.Context;
import android.content.Intent;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    static Intent makeIntent() {
        Context ctx = ApplicationProvider.getApplicationContext();
        ctx.getSharedPreferences("quicknotes_prefs", Context.MODE_PRIVATE)
           .edit().putBoolean("onboarding_done", true).apply();
        return new Intent(ctx, MainActivity.class);
    }

    @Rule
    public ActivityScenarioRule<MainActivity> rule =
            new ActivityScenarioRule<>(makeIntent());

    @Test
    public void mainScreen_greetingVisible() {
        Espresso.onView(ViewMatchers.withId(R.id.greetingText))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void mainScreen_fabVisible() {
        Espresso.onView(ViewMatchers.withId(R.id.fab))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void mainScreen_searchVisible() {
        Espresso.onView(ViewMatchers.withId(R.id.searchEditText))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void mainScreen_settingsBtnVisible() {
        Espresso.onView(ViewMatchers.withId(R.id.settingsBtn))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void fab_showsBottomSheet() {
        Espresso.onView(ViewMatchers.withId(R.id.fab))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.cardNote))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void fab_noteCard_opensNoteActivity() {
        Espresso.onView(ViewMatchers.withId(R.id.fab))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.cardNote))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.titleEditText))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void searchField_filtersNotes() {
        Espresso.onView(ViewMatchers.withId(R.id.searchEditText))
                .perform(ViewActions.typeText("xyznonexistent"),
                         ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.notesCounter))
                .check(ViewAssertions.matches(ViewMatchers.withText("0")));
    }

    @Test
    public void fab_shoppingCard_opensShoppingList() {
        Espresso.onView(ViewMatchers.withId(R.id.fab))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.cardShopping))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.shoppingInput))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void settingsBtn_opensSettings() {
        Espresso.onView(ViewMatchers.withId(R.id.settingsBtn))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.themeRadioGroup))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }
}
