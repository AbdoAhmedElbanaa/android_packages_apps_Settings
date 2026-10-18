/*
 * Copyright (C) 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.sound.dualaudio;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.Fragment;

/**
 * Legacy stub that redirects to the standalone DualAudio application.
 */
public class DualAudioSettings extends Fragment {
    private static final String TAG = "DualAudioSettings";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Activity activity = getActivity();
        if (activity != null) {
            try {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setClassName("org.rising.dualaudio", "org.rising.dualaudio.MainActivity");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Error redirecting to DualAudio app: " + e.getMessage(), e);
            }
            activity.finish();
        }
    }
}
