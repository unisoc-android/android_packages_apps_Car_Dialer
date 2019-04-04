/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.car.dialer.ui.dialpad;

import static com.google.common.truth.Truth.assertThat;

import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.car.dialer.CarDialerRobolectricTestRunner;
import com.android.car.dialer.FragmentTestActivity;
import com.android.car.dialer.R;
import com.android.car.dialer.TestDialerApplication;
import com.android.car.dialer.telecom.UiCallManager;
import com.android.car.dialer.testutils.ShadowCallLogCalls;
import com.android.car.dialer.ui.activecall.InCallFragment;

import com.android.car.telephony.common.TelecomUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.robolectric.annotation.Config;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;

@RunWith(CarDialerRobolectricTestRunner.class)
@Config(shadows = {ShadowCallLogCalls.class})
public class DialpadFragmentTest {
    private static final String DIAL_NUMBER = "6505551234";
    private static final String DIAL_NUMBER_LONG = "650555123465055512346505551234";
    private static final String SINGLE_DIGIT = "0";
    private static final String SPEC_CHAR = "123=_=%^&";

    private DialpadFragment mDialpadFragment;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ((TestDialerApplication) RuntimeEnvironment.application).initUiCallManager();
    }

    @After
    public void tearDown() {
        UiCallManager.get().tearDown();
    }

    @Test
    public void testOnCreateView_modeDialWithNormalDialNumber() {
        mDialpadFragment = DialpadFragment.newPlaceCallDialpad();
        startPlaceCallActivity();
        mDialpadFragment.setDialedNumber(DIAL_NUMBER);

        verifyButtonVisibility(View.VISIBLE);
        verifyTitleText(DIAL_NUMBER);
    }

    @Test
    public void testOnCreateView_modeDialWithLongDialNumber() {
        mDialpadFragment = DialpadFragment.newPlaceCallDialpad();
        startPlaceCallActivity();
        mDialpadFragment.setDialedNumber(DIAL_NUMBER_LONG);

        verifyButtonVisibility(View.VISIBLE);
        verifyTitleText(DIAL_NUMBER_LONG);
    }

    @Test
    public void testOnCreateView_modeDialWithNullDialNumber() {
        mDialpadFragment = DialpadFragment.newPlaceCallDialpad();
        startPlaceCallActivity();
        mDialpadFragment.setDialedNumber(null);

        verifyButtonVisibility(View.VISIBLE);
        verifyTitleText(mDialpadFragment.getContext().getString(R.string.dial_a_number));
    }

    @Test
    public void testOnCreateView_modeDialWithEmptyDialNumber() {
        mDialpadFragment = DialpadFragment.newPlaceCallDialpad();
        startPlaceCallActivity();
        mDialpadFragment.setDialedNumber("");

        verifyButtonVisibility(View.VISIBLE);
        verifyTitleText(mDialpadFragment.getContext().getString(R.string.dial_a_number));
    }

    @Test
    public void testOnCreateView_modeDialWithSpecialChar() {
        mDialpadFragment = DialpadFragment.newPlaceCallDialpad();
        startPlaceCallActivity();
        mDialpadFragment.setDialedNumber(SPEC_CHAR);

        verifyButtonVisibility(View.VISIBLE);
        verifyTitleText(SPEC_CHAR);
    }

    @Test
    public void testOnCreateView_modeInCall() {
        startInCallActivity();

        verifyButtonVisibility(View.GONE);
        verifyTitleText("");
    }

    @Test
    public void testDeleteButton_normalString() {
        mDialpadFragment = DialpadFragment.newPlaceCallDialpad();
        startPlaceCallActivity();
        mDialpadFragment.setDialedNumber(DIAL_NUMBER);

        ImageButton deleteButton = mDialpadFragment.getView().findViewById(R.id.delete_button);
        deleteButton.performClick();

        verifyTitleText(DIAL_NUMBER.substring(0, DIAL_NUMBER.length() - 1));
    }

    @Test
    public void testDeleteButton_oneDigit() {
        mDialpadFragment = DialpadFragment.newPlaceCallDialpad();
        startPlaceCallActivity();
        mDialpadFragment.setDialedNumber(SINGLE_DIGIT);

        ImageButton deleteButton = mDialpadFragment.getView().findViewById(R.id.delete_button);
        deleteButton.performClick();
        verifyTitleText(mDialpadFragment.getContext().getString(R.string.dial_a_number));
    }

    @Test
    public void testDeleteButton_emptyString() {
        mDialpadFragment = DialpadFragment.newPlaceCallDialpad();
        startPlaceCallActivity();
        mDialpadFragment.setDialedNumber("");

        ImageButton deleteButton = mDialpadFragment.getView().findViewById(R.id.delete_button);
        deleteButton.performClick();
        verifyTitleText(mDialpadFragment.getContext().getString(R.string.dial_a_number));
    }

    @Test
    public void testLongPressDeleteButton() {
        mDialpadFragment = DialpadFragment.newPlaceCallDialpad();
        startPlaceCallActivity();
        mDialpadFragment.setDialedNumber(DIAL_NUMBER);

        ImageButton deleteButton = mDialpadFragment.getView().findViewById(R.id.delete_button);

        deleteButton.performLongClick();
        verifyTitleText(mDialpadFragment.getContext().getString(R.string.dial_a_number));
    }

    @Test
    public void testCallButton_emptyString() {
        ShadowCallLogCalls.setLastOutgoingCall(DIAL_NUMBER);

        mDialpadFragment = DialpadFragment.newPlaceCallDialpad();
        startPlaceCallActivity();
        mDialpadFragment.setDialedNumber("");

        ImageButton callButton = mDialpadFragment.getView().findViewById(R.id.call_button);
        callButton.performClick();
        verifyTitleText(DIAL_NUMBER);
    }

    @Test
    public void testOnKeyLongPressed_KeyCode0() {
        mDialpadFragment = DialpadFragment.newPlaceCallDialpad();
        startPlaceCallActivity();
        mDialpadFragment.setDialedNumber(DIAL_NUMBER);

        mDialpadFragment.onKeyLongPressed(KeyEvent.KEYCODE_0);
        verifyTitleText(DIAL_NUMBER.substring(0, DIAL_NUMBER.length() - 1) + "+");
    }

    private void startPlaceCallActivity() {
        FragmentTestActivity fragmentTestActivity;
        fragmentTestActivity = Robolectric.buildActivity(FragmentTestActivity.class)
                .create().start().resume().get();
        fragmentTestActivity.setFragment(mDialpadFragment);
    }

    private void startInCallActivity() {
        mDialpadFragment = DialpadFragment.newInCallDialpad();
        InCallFragment inCallFragment = InCallFragment.newInstance();
        FragmentTestActivity fragmentTestActivity = Robolectric.buildActivity(
                FragmentTestActivity.class).create().start().resume().get();
        fragmentTestActivity.setFragment(inCallFragment);
        inCallFragment.getChildFragmentManager().beginTransaction().replace(R.id.dialpad_container,
                mDialpadFragment).commit();
    }

    private void verifyButtonVisibility(int expectedVisibility) {
        ImageButton callButton = mDialpadFragment.getView().findViewById(R.id.call_button);
        ImageButton deleteButton = mDialpadFragment.getView().findViewById(R.id.delete_button);

        assertThat(callButton.getVisibility()).isEqualTo(expectedVisibility);
        assertThat(deleteButton.getVisibility()).isEqualTo(expectedVisibility);
    }

    private void verifyTitleText(String expectedText) {
        expectedText = TelecomUtils.getFormattedNumber(mDialpadFragment.getContext(), expectedText);
        TextView mTitleView = mDialpadFragment.getView().findViewById(R.id.title);
        TelecomUtils.getFormattedNumber(mDialpadFragment.getContext(), null);
        assertThat(mTitleView.getText()).isEqualTo(expectedText);
    }
}
