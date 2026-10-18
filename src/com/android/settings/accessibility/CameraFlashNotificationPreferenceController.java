/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.accessibility;

import static com.android.settings.accessibility.AccessibilityUtil.State.OFF;
import static com.android.settings.accessibility.AccessibilityUtil.State.ON;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.core.TogglePreferenceController;
import com.android.settings.overlay.FeatureFactory;

/**
 * Controller for Camera flash notification.
 */
public class CameraFlashNotificationPreferenceController extends TogglePreferenceController
        implements DefaultLifecycleObserver {

    private final CameraFlashSettingsContentObserver mSettingsObserver;
    private Preference mPreference;

    public CameraFlashNotificationPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
        mSettingsObserver = new CameraFlashSettingsContentObserver(
                new Handler(mContext.getMainLooper()));
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        mSettingsObserver.register(mContext);
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        mSettingsObserver.unregister(mContext);
    }

    @Override
    public int getAvailabilityStatus() {
        return FlashNotificationsUtil.isTorchAvailable(mContext)
                ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public boolean isChecked() {
        return Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.CAMERA_FLASH_NOTIFICATION, OFF) != OFF;
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        FeatureFactory.getFeatureFactory().getMetricsFeatureProvider().changed(
                getMetricsCategory(), getPreferenceKey(), isChecked ? 1 : 0);
        boolean result = Settings.System.putInt(mContext.getContentResolver(),
                Settings.System.CAMERA_FLASH_NOTIFICATION, (isChecked ? ON : OFF));
        refreshSummary();
        return result;
    }

    @Override
    public CharSequence getSummary() {
        if (!isChecked()) {
            return mContext.getText(R.string.switch_off_text);
        }
        int repeat = Settings.System.getInt(mContext.getContentResolver(),
                "camera_flash_notification_repeat", 2);
        int rate = Settings.System.getInt(mContext.getContentResolver(),
                "camera_flash_notification_rate", 2);
        return mContext.getString(R.string.camera_flash_notification_summary, repeat, rate);
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mPreference = screen.findPreference(getPreferenceKey());
        refreshSummary();
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        if (getPreferenceKey().equals(preference.getKey())) {
            new SubSettingLauncher(mContext)
                    .setDestination(CameraFlashNotificationCustomSettingsFragment.class.getName())
                    .setSourceMetricsCategory(getMetricsCategory())
                    .setTitleRes(R.string.camera_flash_notification_title)
                    .launch();
            return true;
        }
        return super.handlePreferenceTreeClick(preference);
    }

    private void refreshSummary() {
        if (mPreference != null) {
            mPreference.setSummary(getSummary());
        }
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return R.string.menu_key_accessibility;
    }

    private final class CameraFlashSettingsContentObserver extends ContentObserver {
        private final Uri mCameraUri = Settings.System.getUriFor(
                Settings.System.CAMERA_FLASH_NOTIFICATION);
        private final Uri mRepeatUri = Settings.System.getUriFor(
                "camera_flash_notification_repeat");
        private final Uri mRateUri = Settings.System.getUriFor(
                "camera_flash_notification_rate");

        CameraFlashSettingsContentObserver(Handler handler) {
            super(handler);
        }

        public void register(@NonNull Context context) {
            context.getContentResolver().registerContentObserver(
                    mCameraUri, false, this);
            context.getContentResolver().registerContentObserver(
                    mRepeatUri, false, this);
            context.getContentResolver().registerContentObserver(
                    mRateUri, false, this);
        }

        public void unregister(@NonNull Context context) {
            context.getContentResolver().unregisterContentObserver(this);
        }

        @Override
        public void onChange(boolean selfChange, @Nullable Uri uri) {
            refreshSummary();
        }
    }
}
