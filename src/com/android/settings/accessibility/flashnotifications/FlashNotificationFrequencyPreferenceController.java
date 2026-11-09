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

import java.util.Locale;

public class FlashNotificationFrequencyPreferenceController extends BasePreferenceController {

    private static final String SETTING_KEY_ON_MS = "flash_notification_on_ms";
    private static final String SETTING_KEY_OFF_MS = "flash_notification_off_ms";
    private static final int[] PRESET_HZ = {1, 2, 3, 4, 5, 8, 10};

    public FlashNotificationFrequencyPreferenceController(Context context, String key) {
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

    private double getFrequencyHz() {
        int totalMs = getOnMs() + getOffMs();
        if (totalMs <= 0) totalMs = 600;
        return 1000.0 / totalMs;
    }

    private void setFrequencyHz(int hz, Preference preference) {
        if (hz <= 0) hz = 1;
        int periodMs = 1000 / hz;
        int onMs = periodMs / 2;
        int offMs = periodMs - onMs;
        Settings.System.putInt(mContext.getContentResolver(), SETTING_KEY_ON_MS, onMs);
        Settings.System.putInt(mContext.getContentResolver(), SETTING_KEY_OFF_MS, offMs);
        updateState(preference);
    }

    @Override
    public CharSequence getSummary() {
        double hz = getFrequencyHz();
        return String.format(Locale.getDefault(), "%.1f %s", hz, mContext.getString(R.string.flash_notification_unit_hz));
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

        final String[] items = new String[PRESET_HZ.length + 1];
        int currentHzRound = (int) Math.round(getFrequencyHz());
        int selectedIndex = -1;

        for (int i = 0; i < PRESET_HZ.length; i++) {
            items[i] = PRESET_HZ[i] + " " + mContext.getString(R.string.flash_notification_unit_hz);
            if (PRESET_HZ[i] == currentHzRound) {
                selectedIndex = i;
            }
        }
        items[PRESET_HZ.length] = mContext.getString(R.string.flash_notification_custom_value);
        if (selectedIndex == -1) {
            selectedIndex = PRESET_HZ.length;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.flash_notification_frequency_title);
        builder.setSingleChoiceItems(items, selectedIndex, (dialog, which) -> {
            dialog.dismiss();
            if (which < PRESET_HZ.length) {
                setFrequencyHz(PRESET_HZ[which], preference);
            } else {
                FlashNotificationCustomInputDialog.show(
                        mContext,
                        mContext.getString(R.string.flash_notification_frequency_title),
                        currentHzRound > 0 ? currentHzRound : 2,
                        mContext.getString(R.string.flash_notification_unit_hz),
                        1, 20,
                        val -> setFrequencyHz(val, preference)
                );
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.show();
        return true;
    }
}
