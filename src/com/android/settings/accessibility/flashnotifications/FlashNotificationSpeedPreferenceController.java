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

public class FlashNotificationSpeedPreferenceController extends BasePreferenceController {

    private static final String SETTING_KEY_ON_MS = "flash_notification_on_ms";
    private static final String SETTING_KEY_OFF_MS = "flash_notification_off_ms";

    private static final int[][] PRESETS = {
        {500, 500}, // Slow
        {350, 250}, // Normal
        {150, 150}, // Fast
        {100, 100}, // Very Fast
        {50, 50}    // Strobe / Hyper
    };

    private static final int[] PRESET_STRING_IDS = {
        R.string.flash_notification_speed_slow,
        R.string.flash_notification_speed_normal,
        R.string.flash_notification_speed_fast,
        R.string.flash_notification_speed_very_fast,
        R.string.flash_notification_speed_strobe
    };

    public FlashNotificationSpeedPreferenceController(Context context, String key) {
        super(context, key);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    private int getOnMs() {
        return Settings.System.getInt(mContext.getContentResolver(), SETTING_KEY_ON_MS, 350);
    }

    private int getOffMs() {
        return Settings.System.getInt(mContext.getContentResolver(), SETTING_KEY_OFF_MS, 250);
    }

    private void setSpeed(int onMs, int offMs, Preference preference) {
        Settings.System.putInt(mContext.getContentResolver(), SETTING_KEY_ON_MS, onMs);
        Settings.System.putInt(mContext.getContentResolver(), SETTING_KEY_OFF_MS, offMs);
        updateState(preference);
    }

    private int getCurrentPresetIndex() {
        int on = getOnMs();
        int off = getOffMs();
        for (int i = 0; i < PRESETS.length; i++) {
            if (PRESETS[i][0] == on && PRESETS[i][1] == off) {
                return i;
            }
        }
        return -1; // Custom
    }

    @Override
    public CharSequence getSummary() {
        int index = getCurrentPresetIndex();
        if (index >= 0) {
            return mContext.getString(PRESET_STRING_IDS[index]);
        }
        return mContext.getString(R.string.flash_notification_custom_timing_summary, getOnMs(), getOffMs());
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
            items[i] = mContext.getString(PRESET_STRING_IDS[i]);
        }
        items[PRESET_STRING_IDS.length] = mContext.getString(R.string.flash_notification_custom_value);

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.flash_notification_speed_title);
        builder.setSingleChoiceItems(items, selectedIndex, (dialog, which) -> {
            dialog.dismiss();
            if (which < PRESET_STRING_IDS.length) {
                setSpeed(PRESETS[which][0], PRESETS[which][1], preference);
            } else {
                // Show custom on duration first, then off duration
                FlashNotificationCustomInputDialog.show(
                        mContext,
                        mContext.getString(R.string.flash_notification_on_ms_title),
                        getOnMs(),
                        mContext.getString(R.string.flash_notification_unit_ms),
                        20, 3000,
                        newOn -> FlashNotificationCustomInputDialog.show(
                                mContext,
                                mContext.getString(R.string.flash_notification_off_ms_title),
                                getOffMs(),
                                mContext.getString(R.string.flash_notification_unit_ms),
                                20, 3000,
                                newOff -> setSpeed(newOn, newOff, preference)
                        )
                );
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.show();
        return true;
    }
}
