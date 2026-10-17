/*
 * Copyright (C) 2024 The Android Open Source Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.sound.dualaudio;

import android.content.Context;
import android.util.Log;

import androidx.preference.ListPreference;
import androidx.preference.Preference;

import com.android.settings.core.BasePreferenceController;

public class DualAudioBalancePreferenceController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {
    private static final String TAG = "DualAudioBalancePrefCtrl";

    public DualAudioBalancePreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return DualAudioManager.isDualAudioSupported(mContext) ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            try {
                String balance = DualAudioManager.getSpeakerBalance(mContext);
                listPreference.setValue(balance);
            } catch (Exception e) {
                Log.e(TAG, "Error updating balance preference state: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        try {
            String balance = (String) newValue;
            DualAudioManager.setSpeakerBalance(mContext, balance);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error changing speaker balance: " + e.getMessage(), e);
            return false;
        }
    }
}
