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

public class FlashNotificationStrengthPreferenceController extends BasePreferenceController {

    private static final String SETTING_KEY = "flash_notification_strength";
    private static final int DEFAULT_STRENGTH = 100;
    private static final int[] PRESET_STRENGTHS = {25, 50, 75, 100};

    public FlashNotificationStrengthPreferenceController(Context context, String key) {
        super(context, key);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    private int getStrength() {
        return Settings.System.getInt(mContext.getContentResolver(), SETTING_KEY, DEFAULT_STRENGTH);
    }

    private void setStrength(int strength, Preference preference) {
        Settings.System.putInt(mContext.getContentResolver(), SETTING_KEY, strength);
        updateState(preference);
    }

    @Override
    public CharSequence getSummary() {
        int strength = getStrength();
        if (strength == 100) {
            return mContext.getString(R.string.flash_notification_strength_max);
        }
        return strength + "%";
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

        final String[] items = new String[PRESET_STRENGTHS.length + 1];
        int currentStrength = getStrength();
        int selectedIndex = -1;

        for (int i = 0; i < PRESET_STRENGTHS.length; i++) {
            int st = PRESET_STRENGTHS[i];
            items[i] = st + "%" + (st == 100 ? " (" + mContext.getString(R.string.flash_notification_strength_max_label) + ")" : "");
            if (st == currentStrength) {
                selectedIndex = i;
            }
        }
        items[PRESET_STRENGTHS.length] = mContext.getString(R.string.flash_notification_custom_value);
        if (selectedIndex == -1) {
            selectedIndex = PRESET_STRENGTHS.length;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.flash_notification_strength_title);
        builder.setSingleChoiceItems(items, selectedIndex, (dialog, which) -> {
            dialog.dismiss();
            if (which < PRESET_STRENGTHS.length) {
                setStrength(PRESET_STRENGTHS[which], preference);
            } else {
                FlashNotificationCustomInputDialog.show(
                        mContext,
                        mContext.getString(R.string.flash_notification_strength_title),
                        getStrength(),
                        "%",
                        1, 100,
                        val -> setStrength(val, preference)
                );
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.show();
        return true;
    }
}
