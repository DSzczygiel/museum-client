package com.museumsystem.museumclient;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.action.GeneralLocation;
import android.support.test.espresso.action.GeneralSwipeAction;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Swipe;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.museumsystem.museumclient.loginScreen.LoginActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.action.ViewActions.click;

@RunWith(AndroidJUnit4.class)
public class StartScreenTest {

    @Rule
    public ActivityTestRule<LoginActivity> activityRule = new ActivityTestRule(LoginActivity.class);

    @Test
    public void cardSwipeTest(){
        Espresso.onView(ViewMatchers.withId(R.id.newsinfo_recyclerview)).perform(pagerSwipeLeft());
        Espresso.onView(ViewMatchers.withId(R.id.scannerView)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withId(R.id.scannerView)).perform(pagerSwipeRight());
        Espresso.onView(ViewMatchers.withId(R.id.newsinfo_recyclerview)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withId(R.id.newsinfo_recyclerview)).perform(pagerSwipeRight());
        Espresso.onView(ViewMatchers.withId(R.id.login_form_scrollview)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withId(R.id.login_form_scrollview)).perform(pagerSwipeLeft());
        Espresso.onView(ViewMatchers.withId(R.id.newsinfo_recyclerview)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void menuTest(){
        Espresso.onView(ViewMatchers.withId(R.id.info_menu_infos)).perform(click());
        Espresso.onView(ViewMatchers.withId(R.id.newsinfo_linearlayout)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withId(R.id.info_menu_news)).perform(click());
        Espresso.onView(ViewMatchers.withId(R.id.newsinfo_recyclerview)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }


    private GeneralSwipeAction pagerSwipeRight(){
        return new GeneralSwipeAction(Swipe.SLOW, GeneralLocation.CENTER_LEFT,
                GeneralLocation.CENTER_RIGHT, Press.FINGER);
    }

    private GeneralSwipeAction pagerSwipeLeft(){
        return new GeneralSwipeAction(Swipe.SLOW, GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT, Press.FINGER);
    }
}
