/*
 * Copyright (C) 2026 The Android Open Source Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.sound.clarifae;

import android.content.Context;

import com.android.settings.R;
import com.android.settings.core.TogglePreferenceController;

public class ClarifaeHighpassPreferenceController extends TogglePreferenceController {

    public ClarifaeHighpassPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return ClarifaeManager.isClarifaeSupported(mContext) ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public boolean isChecked() {
        return ClarifaeManager.isHighpassEnabled();
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        ClarifaeManager.setHighpassEnabled(isChecked);
        ClarifaeManager.applySettings(mContext);
        return true;
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return R.string.menu_key_sound;
    }
}
