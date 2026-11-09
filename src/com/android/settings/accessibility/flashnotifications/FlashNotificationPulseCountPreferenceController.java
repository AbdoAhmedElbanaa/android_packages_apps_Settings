/*
 * Copyright (C) 2026 The RisingOS Android Project
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

package com.android.settings.accessibility.flashnotifications;

import android.app.AlertDialog;
import android.content.Context;
import android.provider.Settings;
import androidx.preference.Preference;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

public class FlashNotificationPulseCountPreferenceController extends BasePreferenceController {

    private static final String SETTING_KEY = "flash_notification_pulse_count";
    private static final int DEFAULT_PULSE_COUNT = 2;
    private static final int[] PRESET_COUNTS = {1, 2, 3, 4, 5, 10};

    public FlashNotificationPulseCountPreferenceController(Context context, String key) {
        super(context, key);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    private int getPulseCount() {
        return Settings.System.getInt(mContext.getContentResolver(), SETTING_KEY, DEFAULT_PULSE_COUNT);
    }

    private void setPulseCount(int count, Preference preference) {
        Settings.System.putInt(mContext.getContentResolver(), SETTING_KEY, count);
        updateState(preference);
    }

    @Override
    public CharSequence getSummary() {
        int count = getPulseCount();
        if (count == 1) {
            return mContext.getString(R.string.flash_notification_pulse_single);
        } else if (count == 2) {
            return mContext.getString(R.string.flash_notification_pulse_double);
        } else {
            return mContext.getString(R.string.flash_notification_pulse_multiple, count);
        }
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        if (preference != null) {
            preference.setSummary(getSummary());
        }
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!getPreferenceKey().equals(preference.getKey())) {
            return false;
        }

        final String[] items = new String[PRESET_COUNTS.length + 1];
        int currentCount = getPulseCount();
        int selectedIndex = -1;

        for (int i = 0; i < PRESET_COUNTS.length; i++) {
            int cnt = PRESET_COUNTS[i];
            items[i] = mContext.getString(R.string.flash_notification_pulse_multiple, cnt);
            if (cnt == currentCount) {
                selectedIndex = i;
            }
        }
        items[PRESET_COUNTS.length] = mContext.getString(R.string.flash_notification_custom_value);
        if (selectedIndex == -1) {
            selectedIndex = PRESET_COUNTS.length;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.flash_notification_pulse_count_title);
        builder.setSingleChoiceItems(items, selectedIndex, (dialog, which) -> {
            dialog.dismiss();
            if (which < PRESET_COUNTS.length) {
                setPulseCount(PRESET_COUNTS[which], preference);
            } else {
                FlashNotificationCustomInputDialog.show(
                        mContext,
                        mContext.getString(R.string.flash_notification_pulse_count_title),
                        getPulseCount(),
                        mContext.getString(R.string.flash_notification_unit_pulses),
                        1, 50,
                        val -> setPulseCount(val, preference)
                );
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.show();
        return true;
    }
}
