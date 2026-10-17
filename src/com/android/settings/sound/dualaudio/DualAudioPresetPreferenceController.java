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

public class DualAudioPresetPreferenceController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {
    private static final String TAG = "DualAudioPresetPrefCtrl";

    public DualAudioPresetPreferenceController(Context context, String preferenceKey) {
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
                String preset = DualAudioManager.getPreset(mContext);
                listPreference.setValue(preset);
            } catch (Exception e) {
                Log.e(TAG, "Error updating preset preference state: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        try {
            String preset = (String) newValue;
            DualAudioManager.setPreset(mContext, preset);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error changing dual audio preset: " + e.getMessage(), e);
            return false;
        }
    }
}
