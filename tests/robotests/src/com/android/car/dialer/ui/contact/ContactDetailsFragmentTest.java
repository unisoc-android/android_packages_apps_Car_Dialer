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

package com.android.car.dialer.ui.contact;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.TextView;

import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.android.car.dialer.CarDialerRobolectricTestRunner;
import com.android.car.dialer.FragmentTestActivity;
import com.android.car.dialer.R;
import com.android.car.dialer.testutils.ShadowViewModelProvider;
import com.android.car.telephony.common.Contact;
import com.android.car.telephony.common.PhoneNumber;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

import java.util.Arrays;

@Config(shadows = {ShadowViewModelProvider.class})
@RunWith(CarDialerRobolectricTestRunner.class)
public class ContactDetailsFragmentTest {
    private static final String DISPLAY_NAME = "NAME";
    private static final String[] RAW_NUMBERS = {"6505550000", "6502370000"};

    private ContactDetailsFragment mContactDetailsFragment;
    private FragmentTestActivity mFragmentTestActivity;
    private RecyclerView mListView;
    @Mock
    private ContactDetailsViewModel mMockContactDetailsViewModel;
    @Mock
    private Uri mMockContactLookupUri;
    @Mock
    private Contact mMockContact;
    @Mock
    private PhoneNumber mMockPhoneNumber1;
    @Mock
    private PhoneNumber mMockPhoneNumber2;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(mMockContact.getDisplayName()).thenReturn(DISPLAY_NAME);
        when(mMockPhoneNumber1.getRawNumber()).thenReturn(RAW_NUMBERS[0]);
        when(mMockPhoneNumber2.getRawNumber()).thenReturn(RAW_NUMBERS[1]);
        when(mMockContact.getNumbers()).thenReturn(
                Arrays.asList(mMockPhoneNumber1, mMockPhoneNumber2));

        MutableLiveData<Contact> contactDetails = new MutableLiveData<>();
        contactDetails.setValue(mMockContact);
        ShadowViewModelProvider.add(ContactDetailsViewModel.class, mMockContactDetailsViewModel);
        when(mMockContactDetailsViewModel.getContactDetails(mMockContactLookupUri)).thenReturn(
                contactDetails);
    }

    @Test
    public void testCreateWithContact() {
        when(mMockContact.getLookupUri()).thenReturn(mMockContactLookupUri);
        mContactDetailsFragment = ContactDetailsFragment.newInstance(mMockContact, null);

        setUpFragment();

        verifyHeader();
        verifyPhoneNumber(1);
        verifyPhoneNumber(2);
    }

    private void setUpFragment() {
        mFragmentTestActivity = Robolectric.buildActivity(
                FragmentTestActivity.class).create().resume().get();
        mFragmentTestActivity.setFragment(mContactDetailsFragment);

        mListView = mContactDetailsFragment.getView().findViewById(R.id.list_view);
        // Set up layout for recyclerView
        mListView.layout(0, 0, 100, 1000);
    }

    /**
     * Verify the title of the Contact
     */
    private void verifyHeader() {
        View firstChild = mListView.getChildAt(0);
        assertThat(((TextView) firstChild.findViewById(R.id.title)).getText().toString()).isEqualTo(
                DISPLAY_NAME);
        assertThat(firstChild.hasOnClickListeners()).isFalse();
    }

    /**
     * Verify the phone numbers for the Contact
     */
    private void verifyPhoneNumber(int position) {
        View child = mListView.getChildAt(position);

        assertThat(((TextView) child.findViewById(R.id.title)).getText().toString()).isEqualTo(
                RAW_NUMBERS[position - 1]);
        assertThat(child.hasOnClickListeners()).isTrue();

        child.performClick();

        Intent startedIntent = shadowOf(mFragmentTestActivity).getNextStartedActivity();
        assertThat(startedIntent.getAction()).isEqualTo(Intent.ACTION_CALL);
        assertThat(startedIntent.getData()).isEqualTo(
                Uri.parse(ContactDetailsAdapter.TELEPHONE_URI_PREFIX + RAW_NUMBERS[position - 1]));
    }
}