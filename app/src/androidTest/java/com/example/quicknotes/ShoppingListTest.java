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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShoppingListTest {

    static Intent makeIntent() {
        Context ctx = ApplicationProvider.getApplicationContext();
        ctx.getSharedPreferences("quicknotes_prefs", Context.MODE_PRIVATE)
           .edit().putBoolean("onboarding_done", true).apply();
        return new Intent(ctx, ShoppingListActivity.class);
    }

    @Rule
    public ActivityScenarioRule<ShoppingListActivity> rule =
            new ActivityScenarioRule<>(makeIntent());

    @Before
    public void clearData() {
        Context ctx = ApplicationProvider.getApplicationContext();
        NoteDatabase.getInstance(ctx).shoppingItemDao().deleteAll();
    }

    @Test
    public void shoppingList_inputVisible() {
        Espresso.onView(ViewMatchers.withId(R.id.shoppingInput))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void shoppingList_addBtnVisible() {
        Espresso.onView(ViewMatchers.withId(R.id.addItemBtn))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void shoppingList_micBtnVisible() {
        Espresso.onView(ViewMatchers.withId(R.id.micButton))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void shoppingList_addItem_appearsInList() {
        Espresso.onView(ViewMatchers.withId(R.id.shoppingInput))
                .perform(ViewActions.typeText("Milk"),
                         ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.addItemBtn))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withText("Milk"))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void shoppingList_emptyInput_doesNotAdd() {
        Espresso.onView(ViewMatchers.withId(R.id.addItemBtn))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.shoppingRecyclerView))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }
}
