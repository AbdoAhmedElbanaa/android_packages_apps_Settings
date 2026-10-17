/*
 * Copyright (C) 2026 RisingOS Android Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.fuelgauge;

import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.SystemProperties;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.deviceinfo.batteryinfo.BypassChargingPreferenceController;
import com.android.settingslib.fuelgauge.BatteryUtils;

import java.io.File;

/**
 * Controls the visibility and summary of the Bypass Charging entry
 * in the main Battery settings page (power_usage_summary).
 */
public class BypassChargingEntryPreferenceController extends BasePreferenceController {

    public BypassChargingEntryPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        for (String path : BypassChargingPreferenceController.CHARGING_ENABLE_PATHS) {
            if (new File(path).exists()) {
                return AVAILABLE;
            }
        }
        if (new File(BypassChargingPreferenceController.SYSFS_INPUT_SUSPEND).exists()) {
            return AVAILABLE;
        }
        return UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public CharSequence getSummary() {
        final Intent batteryIntent = BatteryUtils.getBatteryIntent(mContext);
        final int plugged = batteryIntent != null ? batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) : 0;
        if (plugged == 0) {
            return isEnabled()
                    ? mContext.getString(R.string.bypass_charging_entry_summary_on) + " (" + mContext.getString(R.string.bypass_charging_summary_unplugged) + ")"
                    : mContext.getString(R.string.bypass_charging_entry_summary_off);
        }
        return isEnabled()
                ? mContext.getString(R.string.bypass_charging_entry_summary_on)
                : mContext.getString(R.string.bypass_charging_entry_summary_off);
    }

    private boolean isEnabled() {
        int active = Settings.System.getInt(mContext.getContentResolver(),
                BypassChargingPreferenceController.SETTING_BYPASS_ACTIVE, -1);
        if (active != -1) {
            return active == 1;
        }
        return SystemProperties.getBoolean(BypassChargingPreferenceController.PROP_BYPASS_ACTIVE, false);
    }
}
