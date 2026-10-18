/*
 * Copyright (C) 2026 The Android Open Source Project
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

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.preferences.SystemSettingListPreference;
import com.android.settings.preferences.SystemSettingSeekBarPreference;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;
import com.android.settingslib.widget.ButtonPreference;
import com.android.settingslib.widget.MainSwitchPreference;

@SearchIndexable
public class CameraFlashNotificationCustomSettingsFragment extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

    private static final String KEY_MAIN_SWITCH = "camera_flash_notification_main_switch";
    private static final String KEY_REPEAT = "camera_flash_notification_repeat";
    private static final String KEY_RATE = "camera_flash_notification_rate";
    private static final String KEY_STRENGTH = "camera_flash_notification_strength";
    private static final String KEY_FIXED_INFO = "camera_flash_fixed_intensity_info";
    private static final String KEY_PREVIEW_BTN = "camera_flash_notification_preview_btn";
    private static final String KEY_RESET_BTN = "camera_flash_notification_reset_btn";

    private static final String KEY_PATTERN_CATEGORY = "camera_flash_pattern_category";
    private static final String KEY_INTENSITY_CATEGORY = "camera_flash_intensity_category";
    private static final String KEY_PREVIEW_CATEGORY = "camera_flash_preview_category";

    private MainSwitchPreference mMainSwitch;
    private SystemSettingListPreference mRepeatPref;
    private SystemSettingListPreference mRatePref;
    private SystemSettingSeekBarPreference mStrengthPref;
    private Preference mFixedStrengthPref;
    private ButtonPreference mPreviewBtn;
    private Preference mResetBtn;

    private PreferenceCategory mPatternCategory;
    private PreferenceCategory mIntensityCategory;
    private PreferenceCategory mPreviewCategory;

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.camera_flash_notification_settings;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.VIEW_UNKNOWN;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);

        mMainSwitch = findPreference(KEY_MAIN_SWITCH);
        mRepeatPref = findPreference(KEY_REPEAT);
        mRatePref = findPreference(KEY_RATE);
        mStrengthPref = findPreference(KEY_STRENGTH);
        mFixedStrengthPref = findPreference(KEY_FIXED_INFO);
        mPreviewBtn = findPreference(KEY_PREVIEW_BTN);
        mResetBtn = findPreference(KEY_RESET_BTN);

        mPatternCategory = findPreference(KEY_PATTERN_CATEGORY);
        mIntensityCategory = findPreference(KEY_INTENSITY_CATEGORY);
        mPreviewCategory = findPreference(KEY_PREVIEW_CATEGORY);

        // Setup Main Switch
        if (mMainSwitch != null) {
            final boolean isEnabled = Settings.System.getInt(getContentResolver(),
                    Settings.System.CAMERA_FLASH_NOTIFICATION, 0) != 0;
            mMainSwitch.setChecked(isEnabled);
            mMainSwitch.addOnSwitchChangeListener((switchWidget, isChecked) -> {
                Settings.System.putInt(getContentResolver(),
                        Settings.System.CAMERA_FLASH_NOTIFICATION, isChecked ? 1 : 0);
                updateEnablement(isChecked);
            });
            updateEnablement(isEnabled);
        }

        // Setup Repeat Preference
        if (mRepeatPref != null) {
            int currentRepeat = Settings.System.getInt(getContentResolver(), KEY_REPEAT, 2);
            updateRepeatSummary(currentRepeat);
            mRepeatPref.setOnPreferenceChangeListener(this);
        }

        // Setup Rate Preference
        if (mRatePref != null) {
            int currentRate = Settings.System.getInt(getContentResolver(), KEY_RATE, 2);
            updateRateSummary(currentRate);
            mRatePref.setOnPreferenceChangeListener(this);
        }

        // Setup Hardware Torch Strength Control
        setupTorchStrengthControl();

        // Setup Preview Button
        if (mPreviewBtn != null) {
            mPreviewBtn.setOnClickListener(v -> triggerFlashPreview());
        }

        // Setup Reset Button
        if (mResetBtn != null) {
            mResetBtn.setOnPreferenceClickListener(this);
        }
    }

    private void updateEnablement(boolean isEnabled) {
        if (mPatternCategory != null) mPatternCategory.setEnabled(isEnabled);
        if (mIntensityCategory != null) mIntensityCategory.setEnabled(isEnabled);
        if (mPreviewCategory != null) mPreviewCategory.setEnabled(isEnabled);
    }

    private void setupTorchStrengthControl() {
        final Context context = getContext();
        if (context == null) return;

        final CameraManager cameraManager = context.getSystemService(CameraManager.class);
        int maxStrengthLevel = 1;
        boolean supportsVariableStrength = false;

        if (cameraManager != null) {
            try {
                for (String cameraId : cameraManager.getCameraIdList()) {
                    CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
                    Boolean flashAvailable = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                    Integer lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING);
                    if (flashAvailable != null && lensFacing != null
                            && flashAvailable && lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                        Integer max = characteristics.get(CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL);
                        if (max != null && max > 1) {
                            maxStrengthLevel = max;
                            supportsVariableStrength = true;
                        }
                        break;
                    }
                }
            } catch (CameraAccessException | IllegalArgumentException ignored) {
            }
        }

        if (supportsVariableStrength && mStrengthPref != null) {
            mStrengthPref.setVisible(true);
            mStrengthPref.setMax(maxStrengthLevel);
            if (mFixedStrengthPref != null) mFixedStrengthPref.setVisible(false);
        } else {
            if (mStrengthPref != null) mStrengthPref.setVisible(false);
            if (mFixedStrengthPref != null) mFixedStrengthPref.setVisible(true);
        }
    }

    private void updateRepeatSummary(int repeat) {
        if (mRepeatPref == null) return;
        CharSequence[] values = mRepeatPref.getEntryValues();
        CharSequence[] entries = mRepeatPref.getEntries();
        boolean matched = false;
        if (values != null && entries != null) {
            for (int i = 0; i < values.length; i++) {
                if (String.valueOf(repeat).equals(values[i].toString())) {
                    mRepeatPref.setValue(values[i].toString());
                    mRepeatPref.setSummary(entries[i]);
                    matched = true;
                    break;
                }
            }
        }
        if (!matched) {
            mRepeatPref.setValue("-1");
            mRepeatPref.setSummary(getString(R.string.camera_flash_notification_custom_option)
                    + " (" + repeat + ")");
        }
    }

    private void updateRateSummary(int rate) {
        if (mRatePref == null) return;
        CharSequence[] values = mRatePref.getEntryValues();
        CharSequence[] entries = mRatePref.getEntries();
        boolean matched = false;
        if (values != null && entries != null) {
            for (int i = 0; i < values.length; i++) {
                if (String.valueOf(rate).equals(values[i].toString())) {
                    mRatePref.setValue(values[i].toString());
                    mRatePref.setSummary(entries[i]);
                    matched = true;
                    break;
                }
            }
        }
        if (!matched) {
            mRatePref.setValue("-1");
            mRatePref.setSummary(getString(R.string.camera_flash_notification_custom_option)
                    + " (" + rate + " Hz)");
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mRepeatPref) {
            int value = Integer.parseInt((String) newValue);
            if (value == -1) {
                showCustomRepeatDialog();
                return false;
            } else {
                Settings.System.putInt(getContentResolver(), KEY_REPEAT, value);
                updateRepeatSummary(value);
                return true;
            }
        } else if (preference == mRatePref) {
            int value = Integer.parseInt((String) newValue);
            if (value == -1) {
                showCustomRateDialog();
                return false;
            } else {
                Settings.System.putInt(getContentResolver(), KEY_RATE, value);
                updateRateSummary(value);
                return true;
            }
        }
        return true;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference == mResetBtn) {
            resetToDefaults();
            return true;
        }
        return false;
    }

    private void showCustomRepeatDialog() {
        final Context context = getContext();
        if (context == null) return;

        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setGravity(Gravity.CENTER);
        int currentRepeat = Settings.System.getInt(getContentResolver(), KEY_REPEAT, 2);
        input.setText(String.valueOf(currentRepeat));
        input.setSelectAllOnFocus(true);

        LinearLayout container = new LinearLayout(context);
        int padding = (int) (24 * getResources().getDisplayMetrics().density);
        container.setPadding(padding, padding / 2, padding, padding / 2);
        container.addView(input, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        new AlertDialog.Builder(context)
                .setTitle(R.string.camera_flash_notification_custom_repeat_title)
                .setMessage(R.string.camera_flash_notification_custom_repeat_hint)
                .setView(container)
                .setPositiveButton(R.string.camera_flash_notification_save, (dialog, which) -> {
                    String text = input.getText().toString().trim();
                    if (!TextUtils.isEmpty(text)) {
                        try {
                            int val = Integer.parseInt(text);
                            val = Math.max(1, Math.min(val, 20));
                            Settings.System.putInt(getContentResolver(), KEY_REPEAT, val);
                            updateRepeatSummary(val);
                        } catch (NumberFormatException ignored) {}
                    }
                })
                .setNegativeButton(R.string.camera_flash_notification_cancel, (dialog, which) -> {
                    int rep = Settings.System.getInt(getContentResolver(), KEY_REPEAT, 2);
                    updateRepeatSummary(rep);
                })
                .show();
    }

    private void showCustomRateDialog() {
        final Context context = getContext();
        if (context == null) return;

        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setGravity(Gravity.CENTER);
        int currentRate = Settings.System.getInt(getContentResolver(), KEY_RATE, 2);
        input.setText(String.valueOf(currentRate));
        input.setSelectAllOnFocus(true);

        LinearLayout container = new LinearLayout(context);
        int padding = (int) (24 * getResources().getDisplayMetrics().density);
        container.setPadding(padding, padding / 2, padding, padding / 2);
        container.addView(input, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        new AlertDialog.Builder(context)
                .setTitle(R.string.camera_flash_notification_custom_rate_title)
                .setMessage(R.string.camera_flash_notification_custom_rate_hint)
                .setView(container)
                .setPositiveButton(R.string.camera_flash_notification_save, (dialog, which) -> {
                    String text = input.getText().toString().trim();
                    if (!TextUtils.isEmpty(text)) {
                        try {
                            int val = Integer.parseInt(text);
                            val = Math.max(1, Math.min(val, 10));
                            Settings.System.putInt(getContentResolver(), KEY_RATE, val);
                            updateRateSummary(val);
                        } catch (NumberFormatException ignored) {}
                    }
                })
                .setNegativeButton(R.string.camera_flash_notification_cancel, (dialog, which) -> {
                    int rt = Settings.System.getInt(getContentResolver(), KEY_RATE, 2);
                    updateRateSummary(rt);
                })
                .show();
    }

    private void triggerFlashPreview() {
        final Context context = getContext();
        if (context == null) return;

        Intent intent = new Intent("com.android.internal.intent.action.FLASH_NOTIFICATION_START_PREVIEW");
        intent.putExtra("com.android.internal.intent.extra.FLASH_NOTIFICATION_PREVIEW_TYPE", 0);
        context.sendBroadcastAsUser(intent, UserHandle.SYSTEM);
    }

    private void resetToDefaults() {
        Settings.System.putInt(getContentResolver(), KEY_REPEAT, 2);
        Settings.System.putInt(getContentResolver(), KEY_RATE, 2);
        Settings.System.putInt(getContentResolver(), "camera_flash_notification_on_duration", 0);
        Settings.System.putInt(getContentResolver(), "camera_flash_notification_off_duration", 0);
        Settings.System.putInt(getContentResolver(), KEY_STRENGTH, 1);

        updateRepeatSummary(2);
        updateRateSummary(2);

        if (mStrengthPref != null) {
            mStrengthPref.setValue(1);
        }

        final Context context = getContext();
        if (context != null) {
            Toast.makeText(context, R.string.camera_flash_notification_reset_toast, Toast.LENGTH_SHORT).show();
        }
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.camera_flash_notification_settings);
}
