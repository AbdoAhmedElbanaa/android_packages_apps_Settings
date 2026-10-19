package com.android.settings.biometrics.eye;

import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.Settings;
import com.android.settings.Utils;
import com.android.settings.core.BasePreferenceController;

public class EyeStatusPreferenceController extends BasePreferenceController
        implements LifecycleObserver {

    public static final String KEY_EYE_SETTINGS = "eye_settings";
    protected Preference mPreference;

    public EyeStatusPreferenceController(Context context) {
        this(context, KEY_EYE_SETTINGS, null /* lifecycle */);
    }

    public EyeStatusPreferenceController(Context context, String key) {
        this(context, key, null /* lifecycle */);
    }

    public EyeStatusPreferenceController(Context context, Lifecycle lifecycle) {
        this(context, KEY_EYE_SETTINGS, lifecycle);
    }

    public EyeStatusPreferenceController(Context context, String key, Lifecycle lifecycle) {
        super(context, key);
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
    }

    @Override
    public int getAvailabilityStatus() {
        return Utils.hasFaceHardware(mContext) ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mPreference = screen.findPreference(getPreferenceKey());
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume() {
        if (mPreference != null) {
            updateState(mPreference);
        }
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        preference.setSummary(EyeUtils.getSummary(mContext, UserHandle.myUserId()));
    }

    @Override
    public boolean handlePreferenceTreeClick(@NonNull Preference preference) {
        if (!getPreferenceKey().equals(preference.getKey())) {
            return false;
        }
        final Intent intent = new Intent(mContext, Settings.EyeSettingsActivity.class);
        mContext.startActivity(intent);
        return true;
    }
}
