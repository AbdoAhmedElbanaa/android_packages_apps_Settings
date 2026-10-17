/*
 * Copyright (C) 2026 RisingOS Android Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.deviceinfo.batteryinfo;

import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.SystemProperties;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settingslib.fuelgauge.BatteryUtils;

/**
 * A controller that shows the real-time battery charging status and plugged type,
 * including direct bypass power mode.
 */
public class BatteryChargingStatusPreferenceController extends BasePreferenceController {

    public BatteryChargingStatusPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public CharSequence getSummary() {
        final Intent batteryIntent = BatteryUtils.getBatteryIntent(mContext);
        if (batteryIntent == null) {
            return mContext.getString(R.string.battery_status_unknown);
        }
        final int status = batteryIntent.getIntExtra(
                BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN);
        final int plugged = batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);

        // Append plugged type
        String pluggedStr = "";
        switch (plugged) {
            case BatteryManager.BATTERY_PLUGGED_AC:
                pluggedStr = " (" + mContext.getString(R.string.battery_plugged_ac) + ")";
                break;
            case BatteryManager.BATTERY_PLUGGED_USB:
                pluggedStr = " (" + mContext.getString(R.string.battery_plugged_usb) + ")";
                break;
            case BatteryManager.BATTERY_PLUGGED_WIRELESS:
                pluggedStr = " (" + mContext.getString(R.string.battery_plugged_wireless) + ")";
                break;
            default:
                break;
        }

        // Check if Bypass Charging is active
        boolean isBypassActive = Settings.System.getInt(mContext.getContentResolver(),
                "bypass_charging_active", 0) == 1
                || SystemProperties.getBoolean("persist.sys.bypass_charging", false);
        if (plugged != 0 && isBypassActive) {
            return mContext.getString(R.string.bypass_charging_status_direct) + pluggedStr;
        }

        String statusStr;
        switch (status) {
            case BatteryManager.BATTERY_STATUS_CHARGING:
                statusStr = mContext.getString(R.string.battery_status_charging);
                break;
            case BatteryManager.BATTERY_STATUS_DISCHARGING:
                return mContext.getString(R.string.battery_status_discharging);
            case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                return mContext.getString(R.string.battery_status_not_charging);
            case BatteryManager.BATTERY_STATUS_FULL:
                statusStr = mContext.getString(R.string.battery_status_full);
                break;
            default:
                return mContext.getString(R.string.battery_status_unknown);
        }

        return statusStr + pluggedStr;
    }
}
