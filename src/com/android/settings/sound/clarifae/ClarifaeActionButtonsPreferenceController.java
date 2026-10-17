/*
 * Copyright (C) 2026 The Android Open Source Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.sound.clarifae;

import android.content.Context;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settingslib.widget.ActionButtonsPreference;

public class ClarifaeActionButtonsPreferenceController extends BasePreferenceController {
    private static final String TAG = "ClarifaeActionBtnsCtrl";

    public ClarifaeActionButtonsPreferenceController(Context context, String key) {
        super(context, key);
    }

    @Override
    public int getAvailabilityStatus() {
        return ClarifaeManager.isClarifaeSupported(mContext) ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        if (screen == null) return;
        Preference pref = screen.findPreference(getPreferenceKey());
        if (pref instanceof ActionButtonsPreference) {
            ActionButtonsPreference buttons = (ActionButtonsPreference) pref;
            buttons.setButton1Text(R.string.audio_apply_changes)
                    .setButton1Icon(R.drawable.ic_check_24dp)
                    .setButton1OnClickListener(v -> onApplyClicked())
                    .setButton1Enabled(true)
                    .setButton2Text(R.string.audio_restart_service)
                    .setButton2Icon(R.drawable.ic_restore)
                    .setButton2OnClickListener(v -> onRestartClicked())
                    .setButton2Enabled(true);
        }
    }

    private void onApplyClicked() {
        ClarifaeManager.applySettings(mContext);
        Toast.makeText(mContext, R.string.clarifae_applied_toast, Toast.LENGTH_SHORT).show();
    }

    private void onRestartClicked() {
        ClarifaeManager.restartAudioService(mContext);
        Toast.makeText(mContext, R.string.clarifae_restarted_toast, Toast.LENGTH_SHORT).show();
    }
}
