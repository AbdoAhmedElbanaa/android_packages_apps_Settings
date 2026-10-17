/*
 * Copyright (C) 2026 The Android Open Source Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.sound.clarifae;

import android.content.Context;
import android.util.Log;

import androidx.preference.Preference;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

public class ClarifaeParentPreferenceController extends BasePreferenceController {
    private static final String TAG = "ClarifaeParentPrefCtrl";

    public ClarifaeParentPreferenceController(Context context, String key) {
        super(context, key);
    }

    @Override
    public int getAvailabilityStatus() {
        return ClarifaeManager.isClarifaeSupported(mContext) ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public CharSequence getSummary() {
        try {
            if (ClarifaeManager.isEnabled()) {
                String target = ClarifaeManager.isInEnabled()
                        ? mContext.getString(R.string.clarifae_in_enabled_title)
                        : mContext.getString(R.string.clarifae_title);
                return mContext.getString(R.string.clarifae_summary_on, target);
            } else {
                return mContext.getString(R.string.clarifae_summary_off);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error generating summary: " + e.getMessage(), e);
            return mContext.getString(R.string.clarifae_summary);
        }
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        if (preference != null) {
            preference.setSummary(getSummary());
        }
    }
}
