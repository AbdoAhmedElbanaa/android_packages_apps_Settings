/*
 * Copyright (C) 2026 RisingOS Android Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.deviceinfo.batteryinfo;

import android.content.Context;
import android.os.BatteryManager;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

/**
 * A controller that shows the real-time battery current (charge/discharge rate).
 * Positive = charging, Negative = discharging.
 */
public class BatteryCurrentPreferenceController extends BasePreferenceController {

    private final BatteryManager mBatteryManager;

    public BatteryCurrentPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
        mBatteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public CharSequence getSummary() {
        if (mBatteryManager == null) {
            return mContext.getString(R.string.battery_info_not_available);
        }
        int microAmps = mBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
        if (microAmps == Integer.MIN_VALUE) {
            return mContext.getString(R.string.battery_info_not_available);
        }
        float milliAmps = microAmps / 1000f;
        String sign = (microAmps >= 0) ? "+" : "";
        return String.format("%s%.0f mA", sign, milliAmps);
    }
}
