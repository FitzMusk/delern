/*
 * Copyright (C) 2017 Katarina Sheremet
 * This file is part of Delern.
 *
 * Delern is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * Delern is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with  Delern.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.dasfoo.delern;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.dasfoo.delern.listdecks.DelernMainActivity;
import org.dasfoo.delern.test.DeckPostfix;
import org.dasfoo.delern.test.FirebaseOperationInProgressRule;
import org.dasfoo.delern.test.FirebaseSignInRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.dasfoo.delern.test.BasicOperations.createCard;
import static org.dasfoo.delern.test.BasicOperations.createDeck;
import static org.dasfoo.delern.test.BasicOperations.deleteDeck;
import static org.dasfoo.delern.test.WaitView.bringToFront;
import static org.dasfoo.delern.test.WaitView.waitView;
import static org.hamcrest.core.AllOf.allOf;


/**
 * Tests creation and updating a card.
 */
@RunWith(AndroidJUnit4.class)
public class AddUpdateCardTest {
    @Rule
    public ActivityTestRule<DelernMainActivity> mActivityRule = new ActivityTestRule<>(
            DelernMainActivity.class);

    @Rule
    public TestName mName = new TestName();

    @Rule
    public FirebaseOperationInProgressRule mFirebaseRule =
            new FirebaseOperationInProgressRule(true);

    @Rule
    public FirebaseSignInRule mSignInRule = new FirebaseSignInRule(true);

    private String mDeckName;

    @Before
    public void createDeckBeforeTest() {
        mDeckName = mName.getMethodName() + DeckPostfix.getRandomNumber();
        createDeck(mDeckName);
    }

    @Test
    public void createReversedCard() {
        createCard("front", "back", /* reversed= */true);
        pressBack();
        // Check that deck with 2 card was created
        waitView(() -> onView(withText(mDeckName)).check(matches(hasSibling(withText("2")))));
    }

    @Test
    public void createCardToUpdateFromPreview() {
        String frontCard = "front";
        String backCard = "back";
        createCard(frontCard, backCard, /* reversed= */false);
        pressBack();
        waitView(() -> onView(withText(mDeckName)).check(matches(hasSibling(withText("1")))));
        onView(allOf(withId(R.id.deck_popup_menu), hasSibling(withText(mDeckName))))
                .perform(click());
        onView(withText(R.string.edit_cards_deck_menu)).perform(click());
        waitView(() -> onView(allOf(withText(frontCard), hasSibling(withText(backCard))))
                .perform(click()));
        waitView(() -> onView(withId(R.id.textFrontCardView)).check(matches(withText(frontCard))));
        onView(withId(R.id.textBackCardView)).check(matches(withText(backCard)));
        onView(withId(R.id.edit_card_button)).check(matches(isDisplayed())).perform(click());
        waitView(() -> onView(withId(R.id.front_side_text)).check(matches(withText(frontCard)))
                .perform(replaceText("front2"), closeSoftKeyboard()));
        onView(withId(R.id.back_side_text)).check(matches(withText(backCard)))
                .perform(replaceText("back2"), closeSoftKeyboard());
        pressBack();
        waitView(() -> onView(withId(R.id.textFrontCardView)).check(matches(withText("front2"))));
        onView(withId(R.id.textBackCardView)).check(matches(withText("back2")));
    }

    @Test
    public void createCardFromCardsList() {
        waitView(() -> onView(withId(R.id.add_card_to_db)).check(matches(isDisplayed()))
                .perform(closeSoftKeyboard()));
        pressBack();
        waitView(() -> onView(withText(mDeckName)).check(matches(hasSibling(withText("0")))));
        onView(allOf(withId(R.id.deck_popup_menu), hasSibling(withText(mDeckName))))
                .perform(click());
        onView(withText(R.string.edit_cards_deck_menu)).perform(click());
        waitView(() -> onView(withId(R.id.f_add_card_button)).perform(click()));
        String frontCard = "front";
        String backCard = "back";
        createCard(frontCard, backCard, /* reversed= */false);
        pressBack();
        waitView(() -> onView(withText(frontCard)).check(matches(hasSibling(withText(backCard)))));
    }

    @Test
    public void createCardToUpdateFromLearningShowingFront() {
        String frontCard = "front";
        String backCard = "back";
        createCard(frontCard, backCard, /* reversed= */false);
        pressBack();
        // Start Learning Activity
        waitView(() -> onView(allOf(withText(mDeckName), hasSibling(withText("1"))))
                .perform(click()));
        // Check that front side is correct
        waitView(() -> onView(withId(R.id.textFrontCardView)).check(matches(withText(frontCard))));

        // Open the options menu OR open the overflow menu, depending on whether
        // the device has a hardware or software overflow menu button.
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onView(withText(R.string.edit)).perform(click());
        waitView(() -> onView(withId(R.id.front_side_text)).check(matches(withText(frontCard)))
                .perform(replaceText("front2"), closeSoftKeyboard()));
        pressBack();
        // Check that front side in Learning Activity is correct
        waitView(() -> onView(withId(R.id.textFrontCardView)).check(matches(withText("front2"))));
    }

    @Test
    public void createCardToUpdateFromLearningShowingBack() {
        String frontCard = "front";
        String backCard = "back";
        createCard(frontCard, backCard, /* reversed= */false);
        pressBack();
        // Start Learning Activity
        waitView(() -> onView(allOf(withText(mDeckName), hasSibling(withText("1"))))
                .perform(click()));
        // Check that front side is correct
        waitView(() -> onView(withId(R.id.textFrontCardView)).check(matches(withText(frontCard))));
        // Flip card
        onView(withId(R.id.turn_card_button)).perform(click());
        // Check back side of card
        onView(withId(R.id.textBackCardView)).check(matches(withText(backCard)));
        // Open the options menu OR open the overflow menu, depending on whether
        // the device has a hardware or software overflow menu button.
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onView(withText(R.string.edit)).perform(click());

        waitView(() -> onView(withId(R.id.front_side_text)).check(matches(withText(frontCard)))
                .perform(replaceText("front2"), closeSoftKeyboard()));
        onView(withId(R.id.back_side_text)).check(matches(withText(backCard)))
                .perform(replaceText("back2"), closeSoftKeyboard());
        pressBack();
        // Check that front side in Learning Activity is correct
        waitView(() -> onView(withId(R.id.textFrontCardView)).check(matches(withText("front2"))));
        onView(withId(R.id.textBackCardView)).check(matches(withText("back2")));
    }

    @After
    public void delete() {
        bringToFront(mActivityRule);
        deleteDeck(mDeckName);
    }
}