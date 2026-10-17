/*
 * Copyright (C) 2026 The Android Open Source Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.sound.clarifae;

import android.content.Context;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.core.SliderPreferenceController;
import com.android.settings.widget.SeekBarPreference;

public class ClarifaeAttenPreferenceController extends SliderPreferenceController {

    private SeekBarPreference mPreference;

    public ClarifaeAttenPreferenceController(Context context, String key) {
        super(context, key);
    }

    @Override
    public int getAvailabilityStatus() {
        return ClarifaeManager.isClarifaeSupported(mContext) ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        Preference pref = screen.findPreference(getPreferenceKey());
        if (pref instanceof SeekBarPreference) {
            mPreference = (SeekBarPreference) pref;
            mPreference.setMin(getMin());
            mPreference.setMax(getMax());
            updateSummary(mPreference, getSliderPosition());
        }
    }

    @Override
    public boolean setSliderPosition(int position) {
        ClarifaeManager.setAttenDb(position);
        if (mPreference != null) {
            updateSummary(mPreference, position);
        }
        return true;
    }

    @Override
    public int getSliderPosition() {
        return ClarifaeManager.getAttenDb();
    }

    @Override
    public int getMin() {
        return 0;
    }

    @Override
    public int getMax() {
        return 60;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        if (preference instanceof SeekBarPreference) {
            SeekBarPreference seek = (SeekBarPreference) preference;
            seek.setProgress(getSliderPosition());
            updateSummary(seek, getSliderPosition());
        }
    }

    private void updateSummary(Preference preference, int position) {
        preference.setSummary(mContext.getString(R.string.clarifae_atten_db_summary, position));
    }
}
