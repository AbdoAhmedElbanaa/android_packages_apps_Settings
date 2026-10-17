/*
 * Copyright (C) 2026 RisingOS Android Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.fuelgauge;

import android.app.settings.SettingsEnums;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

/**
 * Settings page for Bypass Charging.
 *
 * Bypass Charging routes power directly from the USB adapter to the device,
 * keeping the battery at its current level (idle). This reduces heat and
 * battery degradation during extended plugged-in sessions.
 */
@SearchIndexable
public class BypassChargingSettings extends DashboardFragment {

    public static final String TAG = "BypassChargingSettings";

    /** Refreshes all preference summaries when a battery change event arrives. */
    private final BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                updatePreferenceStates();
            }
        }
    };

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.PAGE_UNKNOWN;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.bypass_charging_settings;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    public void onResume() {
        super.onResume();
        getContext().registerReceiver(mBatteryReceiver,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    @Override
    public void onPause() {
        super.onPause();
        getContext().unregisterReceiver(mBatteryReceiver);
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.bypass_charging_settings);
}
