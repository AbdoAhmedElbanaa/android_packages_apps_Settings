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
 * A controller that shows the remaining battery charge in mAh (real-time from fuel gauge).
 */
public class BatteryChargeCounterPreferenceController extends BasePreferenceController {

    private final BatteryManager mBatteryManager;

    public BatteryChargeCounterPreferenceController(Context context, String preferenceKey) {
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
        int microAmpHours = mBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER);
        if (microAmpHours <= 0) {
            return mContext.getString(R.string.battery_info_not_available);
        }
        int milliAmpHours = microAmpHours / 1000;
        return String.format("%d mAh", milliAmpHours);
    }
}
