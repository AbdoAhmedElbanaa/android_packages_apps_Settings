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

public class FlashNotificationOnMsPreferenceController extends BasePreferenceController {

    private static final String SETTING_KEY = "flash_notification_on_ms";
    private static final int DEFAULT_ON_MS = 350;

    public FlashNotificationOnMsPreferenceController(Context context, String key) {
        super(context, key);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    private int getOnMs() {
        return Settings.System.getInt(mContext.getContentResolver(), SETTING_KEY, DEFAULT_ON_MS);
    }

    private void setOnMs(int ms, Preference preference) {
        Settings.System.putInt(mContext.getContentResolver(), SETTING_KEY, ms);
        updateState(preference);
    }

    @Override
    public CharSequence getSummary() {
        return getOnMs() + " " + mContext.getString(R.string.flash_notification_unit_ms);
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
                mContext.getString(R.string.flash_notification_on_ms_title),
                getOnMs(),
                mContext.getString(R.string.flash_notification_unit_ms),
                20, 3000,
                val -> setOnMs(val, preference)
        );
        return true;
    }
}
