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

public class FlashNotificationCallSpeedPreferenceController extends BasePreferenceController {

    private static final String SETTING_KEY_CALL_ON_MS = "flash_notification_call_on_ms";
    private static final String SETTING_KEY_CALL_OFF_MS = "flash_notification_call_off_ms";
    private static final int DEFAULT_CALL_MS = 700;

    private static final int[] PRESET_DURATIONS = {1000, 700, 400, 200};
    private static final int[] PRESET_STRING_IDS = {
        R.string.flash_notification_speed_slow,
        R.string.flash_notification_speed_normal,
        R.string.flash_notification_speed_fast,
        R.string.flash_notification_speed_very_fast
    };

    public FlashNotificationCallSpeedPreferenceController(Context context, String key) {
        super(context, key);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    private int getCallOnMs() {
        return Settings.System.getInt(mContext.getContentResolver(), SETTING_KEY_CALL_ON_MS, DEFAULT_CALL_MS);
    }

    private int getCallOffMs() {
        return Settings.System.getInt(mContext.getContentResolver(), SETTING_KEY_CALL_OFF_MS, DEFAULT_CALL_MS);
    }

    private void setCallDuration(int ms, Preference preference) {
        Settings.System.putInt(mContext.getContentResolver(), SETTING_KEY_CALL_ON_MS, ms);
        Settings.System.putInt(mContext.getContentResolver(), SETTING_KEY_CALL_OFF_MS, ms);
        updateState(preference);
    }

    private int getCurrentPresetIndex() {
        int on = getCallOnMs();
        int off = getCallOffMs();
        if (on != off) return -1;
        for (int i = 0; i < PRESET_DURATIONS.length; i++) {
            if (PRESET_DURATIONS[i] == on) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public CharSequence getSummary() {
        int index = getCurrentPresetIndex();
        if (index >= 0) {
            return mContext.getString(PRESET_STRING_IDS[index]) + " (" + PRESET_DURATIONS[index] + " "
                    + mContext.getString(R.string.flash_notification_unit_ms) + ")";
        }
        return getCallOnMs() + " " + mContext.getString(R.string.flash_notification_unit_ms);
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

        final String[] items = new String[PRESET_STRING_IDS.length + 1];
        int currentIndex = getCurrentPresetIndex();
        int selectedIndex = currentIndex >= 0 ? currentIndex : PRESET_STRING_IDS.length;

        for (int i = 0; i < PRESET_STRING_IDS.length; i++) {
            items[i] = mContext.getString(PRESET_STRING_IDS[i]) + " (" + PRESET_DURATIONS[i] + " "
                    + mContext.getString(R.string.flash_notification_unit_ms) + ")";
        }
        items[PRESET_STRING_IDS.length] = mContext.getString(R.string.flash_notification_custom_value);

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.flash_notification_call_speed_title);
        builder.setSingleChoiceItems(items, selectedIndex, (dialog, which) -> {
            dialog.dismiss();
            if (which < PRESET_DURATIONS.length) {
                setCallDuration(PRESET_DURATIONS[which], preference);
            } else {
                FlashNotificationCustomInputDialog.show(
                        mContext,
                        mContext.getString(R.string.flash_notification_call_speed_title),
                        getCallOnMs(),
                        mContext.getString(R.string.flash_notification_unit_ms),
                        50, 5000,
                        val -> setCallDuration(val, preference)
                );
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.show();
        return true;
    }
}
