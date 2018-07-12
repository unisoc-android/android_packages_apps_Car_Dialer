/*
 * Copyright (C) 2018 The Android Open Source Project
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
package com.android.car.dialer.livedata;

import android.content.Context;

import com.android.car.dialer.telecom.PhoneLoader;

/**
 * Live data which loads missed call history.
 */
public class MissedCallHistoryLiveData extends CallHistoryLiveData {

    public MissedCallHistoryLiveData(Context context) {
        super(context);
    }

    @Override
    protected int getCallType() {
        return PhoneLoader.MISSED_TYPE;
    }
}
