package com.symja.app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;

import android.view.View;

import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.symja.programming.ProgrammingActivity;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import testutils.CodeEditorReplaceTextAction;

@RunWith(AndroidJUnit4.class)
public class ProgrammingActivityTest {

    @Rule
    public ActivityScenarioRule<ProgrammingActivity> activityRule = new ActivityScenarioRule<>(ProgrammingActivity.class);

    @Test
    public void testSolve() {
        switchConsoleTab();
        check("Solve({x^2==4,x+y^2==6}, {x,y})",
                "{{x->-2,y->-2*Sqrt(2)},{x->-2,y->2*Sqrt(2)},{x->2,y->-2},{x->2,y->2}}");
    }

    @Test
    public void testFactor() {
        switchConsoleTab();
        check("Factor(1+2*x+x^2, x)",
                "(1+x)^2");
    }

    private void switchConsoleTab() {
        onView(withText("Console")).perform(click());
    }


    private void check(String input, String result) {
        onVisibleView(withContentDescription("Input"))
                .perform(new CodeEditorReplaceTextAction(""))
                .perform(typeText(input));

        onVisibleView(withContentDescription("Submit"))
                .perform(click());

        waitForAnimation(5000);

        // TODO: update test

//        onVisibleView(withContentDescription("ResultListView"))
//                .check(matches(RecyclerViewMatcher.atPosition(0, withId(R.id.editint))))
//        onView(withText("Result")).perform(click()); // result tab
        //onView(withId(R.id.result_label)).check(matches(withText(result)));

    }

    private ViewInteraction onVisibleView(Matcher<View> viewMatcher) {
        return onView(allOf(isDisplayed(), viewMatcher));
    }

    private void waitForAnimation(int millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}