/*
 * Copyright (C) 2024 The Android Open Source Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.sound.dualaudio;

import android.content.Context;
import android.util.Log;

import androidx.preference.Preference;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

public class DualAudioParentPreferenceController extends BasePreferenceController {
    private static final String TAG = "DualAudioParentPrefCtrl";

    public DualAudioParentPreferenceController(Context context, String key) {
        super(context, key);
    }

    @Override
    public int getAvailabilityStatus() {
        return DualAudioManager.isDualAudioSupported(mContext) ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public CharSequence getSummary() {
        try {
            if (DualAudioManager.isDualAudioEnabled(mContext)) {
                String preset = DualAudioManager.getPreset(mContext);
                CharSequence label = DualAudioManager.getPresetLabel(mContext, preset);
                return mContext.getString(R.string.dual_audio_summary_on, label);
            } else {
                return mContext.getString(R.string.dual_audio_summary_off);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error generating dual audio parent summary: " + e.getMessage(), e);
            return mContext.getString(R.string.dual_audio_summary);
        }
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        if (preference != null) {
            preference.setSummary(getSummary());
        }
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        if (getPreferenceKey().equals(preference.getKey())) {
            try {
                android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_MAIN);
                intent.setClassName("org.rising.dualaudio", "org.rising.dualaudio.MainActivity");
                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Could not start DualAudio app: " + e.getMessage(), e);
            }
        }
        return super.handlePreferenceTreeClick(preference);
    }
}
