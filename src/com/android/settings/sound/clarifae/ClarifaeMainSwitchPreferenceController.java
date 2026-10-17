/*
 * Copyright (C) 2026 The Android Open Source Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.sound.clarifae;

import android.content.Context;
import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.core.TogglePreferenceController;
import com.android.settingslib.widget.MainSwitchPreference;

public class ClarifaeMainSwitchPreferenceController extends TogglePreferenceController {
    private static final String TAG = "ClarifaeMainSwitchCtrl";

    private MainSwitchPreference mPreference;
    private PreferenceCategory mMicCategory;
    private PreferenceCategory mPlaybackCategory;
    private PreferenceCategory mAdvancedCategory;

    public ClarifaeMainSwitchPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return ClarifaeManager.isClarifaeSupported(mContext) ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public boolean isChecked() {
        return ClarifaeManager.isEnabled();
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        try {
            ClarifaeManager.setEnabled(isChecked);
            updateDependentPreferences(isChecked);
            ClarifaeManager.applySettings(mContext);
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
            mMicCategory = screen.findPreference("clarifae_mic_category");
            mPlaybackCategory = screen.findPreference("clarifae_playback_category");
            mAdvancedCategory = screen.findPreference("clarifae_advanced_category");
            updateDependentPreferences(isChecked());
        }
    }

    private void updateDependentPreferences(boolean isEnabled) {
        if (mMicCategory != null) {
            mMicCategory.setEnabled(isEnabled);
        }
        if (mPlaybackCategory != null) {
            mPlaybackCategory.setEnabled(isEnabled);
        }
        if (mAdvancedCategory != null) {
            mAdvancedCategory.setEnabled(isEnabled);
        }
    }
}
