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

package com.android.car.dialer.entity;

import android.database.Cursor;
import android.provider.CallLog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Entity class for call logs of a phone number. This call log may contains multiple call
 * records.
 */
public class PhoneCallLog {
    /** Call log record. */
    public static class Record implements Comparable<Record> {
        private final long mCallEndTimestamp;
        private final int mCallType;

        public Record(long callEndTimestamp, int callType) {
            mCallEndTimestamp = callEndTimestamp;
            mCallType = callType;
        }

        /** Returns the timestamp on when the call occured, in milliseconds since the epoch */
        public long getCallEndTimestamp() {
            return mCallEndTimestamp;
        }

        /**
         * Returns the type of this record. For example, missed call, outbound call. Allowed values
         * are defined in {@link CallLog.Calls#TYPE}.
         *
         * @see CallLog.Calls#TYPE
         */
        public int getCallType() {
            return mCallType;
        }

        @Override
        public int compareTo(Record otherRecord) {
            return (int) (mCallEndTimestamp - otherRecord.getCallEndTimestamp());
        }
    }

    private String mPhoneNumberString;
    private List<Record> mCallRecords = new ArrayList<>();

    /**
     * Creates a {@link PhoneCallLog} from a {@link Cursor}.
     */
    public static PhoneCallLog fromCursor(Cursor cursor) {
        int numberColumn = cursor.getColumnIndex(CallLog.Calls.NUMBER);
        int dateColumn = cursor.getColumnIndex(CallLog.Calls.DATE);
        int callTypeColumn = cursor.getColumnIndex(CallLog.Calls.TYPE);

        PhoneCallLog phoneCallLog = new PhoneCallLog();
        phoneCallLog.mPhoneNumberString = cursor.getString(numberColumn);
        Record record = new Record(cursor.getLong(dateColumn), cursor.getInt(callTypeColumn));
        phoneCallLog.mCallRecords.add(record);
        return phoneCallLog;
    }

    /** Returns the phone number of this log. */
    public String getPhoneNumberString() {
        return mPhoneNumberString;
    }

    /**
     * Returns the last call end timestamp of this number. Returns -1 if there's no call log
     * records.
     */
    public long getLastCallEndTimestamp() {
        if (!mCallRecords.isEmpty()) {
            return mCallRecords.get(mCallRecords.size() - 1).getCallEndTimestamp();
        }
        return -1;
    }

    /**
     * Returns the number of call records in this call log.
     */
    public int getNumberOfCallRecords() {
        return mCallRecords.size();
    }

    /**
     * Returns a copy of records from the phone number. Logs are sorted from most recent to least
     * recent call end time.
     */
    public List<Record> getAllCallRecords() {
        return new ArrayList<>(mCallRecords);
    }

    /**
     * Merges all call records with this call log's call records if they are representing the same
     * phone number.
     */
    public void merge(PhoneCallLog phoneCallLog) {
        if (mPhoneNumberString.equals(phoneCallLog.mPhoneNumberString)) {
            mCallRecords.addAll(phoneCallLog.mCallRecords);
            Collections.sort(mCallRecords);
            Collections.reverse(mCallRecords);
        }
    }
}