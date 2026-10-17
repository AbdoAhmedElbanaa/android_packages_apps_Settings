/*
 * Copyright (C) 2026 RisingOS Android Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.deviceinfo.batteryinfo;

import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.text.TextUtils;

import androidx.preference.Preference;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settingslib.fuelgauge.BatteryUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * A controller that shows the charging type (Standard, Quick Charge, Power Delivery, etc.)
 * by reading from sysfs power_supply nodes with robust fallback to BatteryManager.
 */
public class BatteryChargingTypePreferenceController extends BasePreferenceController {

    public BatteryChargingTypePreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        if (preference != null) {
            preference.setSummary(getSummary());
        }
    }

    @Override
    public CharSequence getSummary() {
        final Intent batteryIntent = BatteryUtils.getBatteryIntent(mContext);
        final int plugged = batteryIntent != null
                ? batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) : 0;
        final int status = batteryIntent != null
                ? batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN)
                : BatteryManager.BATTERY_STATUS_UNKNOWN;

        if (plugged == 0 && status != BatteryManager.BATTERY_STATUS_CHARGING) {
            return mContext.getString(R.string.battery_charge_type_none);
        }

        // 1. Try reading real_type from USB or Main (Qualcomm smb5 sysfs)
        for (String path : new String[]{
                "/sys/class/power_supply/usb/real_type",
                "/sys/class/power_supply/main/real_type",
                "/sys/class/power_supply/usb/type",
                "/sys/class/power_supply/main/type"}) {
            String val = readSysfs(path);
            if (!TextUtils.isEmpty(val)) {
                CharSequence parsed = parseTypeString(val);
                if (parsed != null) {
                    return parsed;
                }
            }
        }

        // 2. Try reading quick_charge_type (Xiaomi SM6250 / CAF enum)
        for (String path : new String[]{
                "/sys/class/power_supply/usb/quick_charge_type",
                "/sys/class/power_supply/main/quick_charge_type"}) {
            String val = readSysfs(path);
            if (!TextUtils.isEmpty(val)) {
                try {
                    int qc = Integer.parseInt(val);
                    CharSequence parsed = parseQcInt(qc);
                    if (parsed != null) {
                        return parsed;
                    }
                } catch (NumberFormatException ignored) {
                    CharSequence parsed = parseTypeString(val);
                    if (parsed != null) {
                        return parsed;
                    }
                }
            }
        }

        // 3. Try reading fastcharge_mode
        String fastMode = readSysfs("/sys/class/power_supply/battery/fastcharge_mode");
        if ("1".equals(fastMode) || "2".equals(fastMode)) {
            return mContext.getString(R.string.battery_charge_type_qc3);
        }

        // 4. Fallback based on BatteryManager plugged source & charging power
        if (plugged == BatteryManager.BATTERY_PLUGGED_AC) {
            if (batteryIntent != null) {
                int maxCurrent = batteryIntent.getIntExtra("max_charging_current", -1);
                if (maxCurrent >= 2500000) {
                    return mContext.getString(R.string.battery_charge_type_pd);
                } else if (maxCurrent >= 1800000) {
                    return mContext.getString(R.string.battery_charge_type_qc3);
                }
            }
            return mContext.getString(R.string.battery_charge_type_dcp);
        } else if (plugged == BatteryManager.BATTERY_PLUGGED_USB) {
            return mContext.getString(R.string.battery_charge_type_sdp);
        } else if (plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS) {
            return mContext.getString(R.string.battery_plugged_wireless);
        }

        if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
            return mContext.getString(R.string.battery_charge_type_dcp);
        }

        return mContext.getString(R.string.battery_charge_type_none);
    }

    private CharSequence parseTypeString(String raw) {
        String val = raw.toUpperCase();
        if (val.contains("HVDCP_3P5") || val.contains("QC3.5") || val.contains("TURBO")) {
            return mContext.getString(R.string.battery_charge_type_qc3p5);
        } else if (val.contains("HVDCP_3") || val.contains("QC3")) {
            return mContext.getString(R.string.battery_charge_type_qc3);
        } else if (val.contains("HVDCP") || val.contains("QC2")) {
            return mContext.getString(R.string.battery_charge_type_qc2);
        } else if (val.contains("PD")) {
            return mContext.getString(R.string.battery_charge_type_pd);
        } else if (val.contains("DCP")) {
            return mContext.getString(R.string.battery_charge_type_dcp);
        } else if (val.contains("CDP")) {
            return mContext.getString(R.string.battery_charge_type_cdp);
        } else if (val.contains("SDP") || "USB".equals(val)) {
            return mContext.getString(R.string.battery_charge_type_sdp);
        } else if (val.contains("FLOAT")) {
            return mContext.getString(R.string.battery_charge_type_float);
        } else if (val.contains("OCP")) {
            return mContext.getString(R.string.battery_charge_type_ocp);
        } else if (val.contains("WIRELESS")) {
            return mContext.getString(R.string.battery_plugged_wireless);
        }
        return null;
    }

    private CharSequence parseQcInt(int qc) {
        // Xiaomi SM6250 enum: 1=FAST, 2=FLASH(18W/QC3), 3=TURBE(33W/PD)
        // CAF enum: 1=SDP, 2=CDP, 3=DCP, 6=QC2, 7=QC3, 8=QC3.5, 9=PD
        switch (qc) {
            case 9:
                return mContext.getString(R.string.battery_charge_type_pd);
            case 8:
                return mContext.getString(R.string.battery_charge_type_qc3p5);
            case 7:
            case 2: // Xiaomi FLASH / QC3.0
                return mContext.getString(R.string.battery_charge_type_qc3);
            case 6:
                return mContext.getString(R.string.battery_charge_type_qc2);
            case 3: // Xiaomi TURBE / or CAF DCP
                return mContext.getString(R.string.battery_charge_type_qc3p5);
            case 1: // Xiaomi FAST / or CAF SDP
                return mContext.getString(R.string.battery_charge_type_dcp);
            default:
                return null;
        }
    }

    private String readSysfs(String path) {
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line = reader.readLine();
            if (line != null) {
                return line.trim();
            }
        } catch (IOException ignored) {
        }
        return null;
    }
}
