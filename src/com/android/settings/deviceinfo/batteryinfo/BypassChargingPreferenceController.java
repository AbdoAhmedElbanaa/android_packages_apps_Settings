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
import android.util.Log;

import com.android.settings.R;
import com.android.settings.core.TogglePreferenceController;
import com.android.settingslib.fuelgauge.BatteryUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

/**
 * Controls Bypass Charging (Direct Power Mode):
 * Suspends charging current to the battery while keeping external power
 * connected directly to the device components.
 *
 * Uses kernel power_supply sysfs nodes:
 *   /sys/class/power_supply/battery/charging_enabled (write 0 to stop charging battery, 1 to resume)
 *   /sys/class/power_supply/battery/battery_charging_enabled
 *   /sys/class/power_supply/battery/input_suspend (kept 0 so external power stays connected)
 */
public class BypassChargingPreferenceController extends TogglePreferenceController {

    private static final String TAG = "BypassChargingCtrl";
    public static final String[] CHARGING_ENABLE_PATHS = {
            "/sys/class/power_supply/battery/charging_enabled",
            "/sys/class/power_supply/battery/battery_charging_enabled",
    };
    public static final String SYSFS_INPUT_SUSPEND =
            "/sys/class/power_supply/battery/input_suspend";

    public static final String SETTING_BYPASS_ACTIVE = "bypass_charging_active";
    public static final String PROP_BYPASS_ACTIVE = "persist.sys.bypass_charging";

    public BypassChargingPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        for (String path : CHARGING_ENABLE_PATHS) {
            if (new File(path).exists()) {
                return AVAILABLE;
            }
        }
        if (new File(SYSFS_INPUT_SUSPEND).exists()) {
            return AVAILABLE;
        }
        return UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public boolean isChecked() {
        int active = Settings.System.getInt(mContext.getContentResolver(), SETTING_BYPASS_ACTIVE, -1);
        if (active != -1) {
            return active == 1;
        }
        return SystemProperties.getBoolean(PROP_BYPASS_ACTIVE, false);
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        // 1. Update sysfs hardware charging state:
        // When bypass is ON: charging_enabled = 0 (stop pumping power into battery)
        // When bypass is OFF: charging_enabled = 1 (charge battery normally)
        for (String path : CHARGING_ENABLE_PATHS) {
            if (new File(path).exists()) {
                writeSysfs(path, isChecked ? "0" : "1");
            }
        }

        // Always ensure input_suspend is "0" so charger power is delivered to device
        if (new File(SYSFS_INPUT_SUSPEND).exists()) {
            writeSysfs(SYSFS_INPUT_SUSPEND, "0");
        }

        // 2. Persist state in Settings and SystemProperties
        Settings.System.putInt(mContext.getContentResolver(), SETTING_BYPASS_ACTIVE, isChecked ? 1 : 0);
        try {
            SystemProperties.set(PROP_BYPASS_ACTIVE, isChecked ? "1" : "0");
        } catch (RuntimeException e) {
            Log.e(TAG, "Failed to set " + PROP_BYPASS_ACTIVE, e);
        }

        return true;
    }

    @Override
    public CharSequence getSummary() {
        if (getAvailabilityStatus() == UNSUPPORTED_ON_DEVICE) {
            return mContext.getString(R.string.bypass_charging_unavailable);
        }
        final Intent batteryIntent = BatteryUtils.getBatteryIntent(mContext);
        final int plugged = batteryIntent != null ? batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) : 0;
        if (plugged == 0) {
            return isChecked()
                    ? mContext.getString(R.string.bypass_charging_summary_on) + " (" + mContext.getString(R.string.bypass_charging_summary_unplugged) + ")"
                    : mContext.getString(R.string.bypass_charging_summary_unplugged);
        }
        return isChecked()
                ? mContext.getString(R.string.bypass_charging_summary_on)
                : mContext.getString(R.string.bypass_charging_summary_off);
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return R.string.menu_key_battery;
    }

    private String readSysfs(String path) {
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line = reader.readLine();
            return (line != null) ? line.trim() : null;
        } catch (IOException e) {
            Log.e(TAG, "Failed to read sysfs: " + path, e);
            return null;
        }
    }

    private boolean writeSysfs(String path, String value) {
        try (FileOutputStream fos = new FileOutputStream(path)) {
            fos.write(value.getBytes());
            fos.flush();
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Failed to write sysfs: " + path + " = " + value, e);
            return false;
        }
    }
}
