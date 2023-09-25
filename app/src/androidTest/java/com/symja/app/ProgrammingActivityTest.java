package com.symja.app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

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
        check("Solve({x^2==4,x+y^2==6}, {x,y})",
                "{{x->-2,y->-2*Sqrt(2)},{x->-2,y->2*Sqrt(2)},{x->2,y->-2},{x->2,y->2}}");
    }

    @Test
    public void testFactor() {
        check("Factor(1+2*x+x^2, x)",
                "(1+x)^2");
    }


    private void check(String input, String result) {
        onView(withId(R.id.input_field)).perform(new CodeEditorReplaceTextAction(""))
                .perform(typeText(input));

        onView(withId(R.id.btn_calc)).perform(click());

        waitForAnimation(5000);
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