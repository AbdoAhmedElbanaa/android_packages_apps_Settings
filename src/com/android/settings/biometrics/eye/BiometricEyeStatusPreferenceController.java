package com.android.settings.biometrics.eye;

import android.content.Context;

import androidx.lifecycle.Lifecycle;

public class BiometricEyeStatusPreferenceController extends EyeStatusPreferenceController {

    public static final String KEY_BIOMETRIC_EYE_SETTINGS = "biometric_eye_settings";

    public BiometricEyeStatusPreferenceController(Context context) {
        super(context, KEY_BIOMETRIC_EYE_SETTINGS);
    }

    public BiometricEyeStatusPreferenceController(Context context, String key) {
        super(context, key);
    }

    public BiometricEyeStatusPreferenceController(Context context, Lifecycle lifecycle) {
        super(context, KEY_BIOMETRIC_EYE_SETTINGS, lifecycle);
    }

    public BiometricEyeStatusPreferenceController(Context context, String key, Lifecycle lifecycle) {
        super(context, key, lifecycle);
    }
}
