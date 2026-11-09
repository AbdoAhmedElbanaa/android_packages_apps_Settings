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

import android.content.Context;
import android.provider.Settings;
import androidx.preference.Preference;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

public class FlashNotificationOffMsPreferenceController extends BasePreferenceController {

    private static final String SETTING_KEY = "flash_notification_off_ms";
    private static final int DEFAULT_OFF_MS = 250;

    public FlashNotificationOffMsPreferenceController(Context context, String key) {
        super(context, key);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    private int getOffMs() {
        return Settings.System.getInt(mContext.getContentResolver(), SETTING_KEY, DEFAULT_OFF_MS);
    }

    private void setOffMs(int ms, Preference preference) {
        Settings.System.putInt(mContext.getContentResolver(), SETTING_KEY, ms);
        updateState(preference);
    }

    @Override
    public CharSequence getSummary() {
        return getOffMs() + " " + mContext.getString(R.string.flash_notification_unit_ms);
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

        FlashNotificationCustomInputDialog.show(
                mContext,
                mContext.getString(R.string.flash_notification_off_ms_title),
                getOffMs(),
                mContext.getString(R.string.flash_notification_unit_ms),
                20, 3000,
                val -> setOffMs(val, preference)
        );
        return true;
    }
}
