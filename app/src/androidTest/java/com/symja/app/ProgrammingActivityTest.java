package com.symja.app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.allOf;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.symja.programming.ProgrammingActivity;

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

    private void switchConsoleTab() {
        onView(withText("Console")).perform(click());
    }

    @Test
    public void testFactor() {
        switchConsoleTab();
        check("Factor(1+2*x+x^2, x)",
                "(1+x)^2");
    }


    private void check(String input, String result) {
        onView(allOf(withId(com.symja.programming.R.id.symja_prgm_edit_input), isDisplayed()))
                .perform(new CodeEditorReplaceTextAction(""))
                .perform(typeText(input));

        onView(allOf(withId(com.symja.programming.R.id.btn_run), isDisplayed()))
                .perform(click());

        waitForAnimation(5000);

        // TODO: update test

        onView(withText("Result")).perform(click()); // result tab
        onView(withId(R.id.result_label)).check(matches(withText(result)));

    }

    private void waitForAnimation(int millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}