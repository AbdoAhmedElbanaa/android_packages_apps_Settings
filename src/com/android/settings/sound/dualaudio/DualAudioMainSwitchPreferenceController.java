/*
 * Copyright (C) 2024 The Android Open Source Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.sound.dualaudio;

import android.content.Context;
import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.core.TogglePreferenceController;
import com.android.settingslib.widget.MainSwitchPreference;

public class DualAudioMainSwitchPreferenceController extends TogglePreferenceController {
    private static final String TAG = "DualAudioSwitchPrefCtrl";

    private MainSwitchPreference mPreference;
    private PreferenceCategory mPresetsCategory;
    private PreferenceCategory mTuningCategory;

    public DualAudioMainSwitchPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return DualAudioManager.isDualAudioSupported(mContext) ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public boolean isChecked() {
        return DualAudioManager.isDualAudioEnabled(mContext);
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        try {
            DualAudioManager.setDualAudioEnabled(mContext, isChecked);
            updateDependentPreferences(isChecked);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error in setChecked: " + e.getMessage(), e);
            return false;
        }
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return R.string.menu_key_sound;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        if (screen != null) {
            Preference pref = screen.findPreference(getPreferenceKey());
            if (pref instanceof MainSwitchPreference) {
                mPreference = (MainSwitchPreference) pref;
            }
            mPresetsCategory = screen.findPreference("dual_audio_presets_category");
            mTuningCategory = screen.findPreference("dual_audio_tuning_category");
            updateDependentPreferences(isChecked());
        }
    }

    private void updateDependentPreferences(boolean isEnabled) {
        if (mPresetsCategory != null) {
            mPresetsCategory.setEnabled(isEnabled);
        }
        if (mTuningCategory != null) {
            mTuningCategory.setEnabled(isEnabled);
        }
    }
}
