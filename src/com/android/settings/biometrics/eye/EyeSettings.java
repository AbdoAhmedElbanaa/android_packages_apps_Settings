package com.android.settings.biometrics.eye;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.SwitchPreferenceCompat;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.password.ChooseLockGeneric;
import com.android.settings.password.ChooseLockSettingsHelper;

import java.util.List;

public class EyeSettings extends DashboardFragment {

    private static final String TAG = "EyeSettings";
    private static final int CONFIRM_REQUEST = 1001;
    private static final int ENROLL_REQUEST = 1002;
    private static final int CHOOSE_LOCK_REQUEST = 1003;

    private static final String PREF_KEY_ENROLLED_CATEGORY = "security_settings_eye_enrolled_category";
    private static final String PREF_KEY_KEYGUARD_TOGGLE = "security_settings_eye_keyguard";
    private static final String PREF_KEY_REQUIRE_GAZE_TOGGLE = "security_settings_eye_require_gaze";
    private static final String PREF_KEY_BIOMETRIC_MODE = "security_settings_eye_biometric_mode";
    private static final String PREF_KEY_ADD_EYE = "security_settings_eye_add_button";

    private PreferenceCategory mEnrolledCategory;
    private Preference mAddEyePreference;
    private SwitchPreferenceCompat mKeyguardToggle;
    private SwitchPreferenceCompat mRequireGazeToggle;
    private ListPreference mBiometricModePref;

    private byte[] mToken;
    private boolean mConfirmingPassword;
    private int mUserId;

    @Override
    public int getMetricsCategory() {
        return 0;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.security_settings_eye;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserId = getActivity().getIntent().getIntExtra(Intent.EXTRA_USER_ID, UserHandle.myUserId());

        if (savedInstanceState != null) {
            mToken = savedInstanceState.getByteArray("token");
            mConfirmingPassword = savedInstanceState.getBoolean("confirming_password");
        }

        mEnrolledCategory = findPreference(PREF_KEY_ENROLLED_CATEGORY);
        mKeyguardToggle = findPreference(PREF_KEY_KEYGUARD_TOGGLE);
        mRequireGazeToggle = findPreference(PREF_KEY_REQUIRE_GAZE_TOGGLE);
        mBiometricModePref = findPreference(PREF_KEY_BIOMETRIC_MODE);

        setupOptionsPreferences();
        updateEnrolledEyesList();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putByteArray("token", mToken);
        outState.putBoolean("confirming_password", mConfirmingPassword);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mToken == null && !mConfirmingPassword) {
            launchConfirmPassword();
        } else {
            updateEnrolledEyesList();
        }
    }

    private void launchConfirmPassword() {
        final ChooseLockSettingsHelper.Builder builder =
                new ChooseLockSettingsHelper.Builder(getActivity(), this);
        final boolean launched = builder.setRequestCode(CONFIRM_REQUEST)
                .setTitle(getString(R.string.security_settings_eye_preference_title))
                .setRequestGatekeeperPasswordHandle(true)
                .setUserId(mUserId)
                .setForegroundOnly(true)
                .setReturnCredentials(true)
                .show();

        mConfirmingPassword = true;
        if (!launched) {
            final Intent intent = new Intent();
            intent.setClassName("com.android.settings", ChooseLockGeneric.class.getName());
            intent.putExtra(ChooseLockGeneric.ChooseLockGenericFragment.HIDE_INSECURE_OPTIONS, true);
            intent.putExtra(Intent.EXTRA_USER_ID, mUserId);
            startActivityForResult(intent, CHOOSE_LOCK_REQUEST);
        }
    }

    private void setupOptionsPreferences() {
        if (mKeyguardToggle != null) {
            boolean enabled = Settings.Secure.getIntForUser(
                    getContentResolver(),
                    EyeUtils.SETTINGS_KEY_EYE_KEYGUARD_ENABLED,
                    1,
                    mUserId) == 1;
            mKeyguardToggle.setChecked(enabled);
            mKeyguardToggle.setOnPreferenceChangeListener((pref, newValue) -> {
                boolean val = (Boolean) newValue;
                Settings.Secure.putIntForUser(
                        getContentResolver(),
                        EyeUtils.SETTINGS_KEY_EYE_KEYGUARD_ENABLED,
                        val ? 1 : 0,
                        mUserId);
                return true;
            });
        }

        if (mRequireGazeToggle != null) {
            boolean enabled = Settings.Secure.getIntForUser(
                    getContentResolver(),
                    EyeUtils.SETTINGS_KEY_EYE_REQUIRE_GAZE,
                    1,
                    mUserId) == 1;
            mRequireGazeToggle.setChecked(enabled);
            mRequireGazeToggle.setOnPreferenceChangeListener((pref, newValue) -> {
                boolean val = (Boolean) newValue;
                Settings.Secure.putIntForUser(
                        getContentResolver(),
                        EyeUtils.SETTINGS_KEY_EYE_REQUIRE_GAZE,
                        val ? 1 : 0,
                        mUserId);
                return true;
            });
        }

        if (mBiometricModePref != null) {
            int currentMode = Settings.Secure.getIntForUser(
                    getContentResolver(),
                    EyeUtils.SETTINGS_KEY_BIOMETRIC_MODE,
                    1,
                    mUserId);
            mBiometricModePref.setValue(String.valueOf(currentMode));
            mBiometricModePref.setOnPreferenceChangeListener((pref, newValue) -> {
                int mode = Integer.parseInt((String) newValue);
                Settings.Secure.putIntForUser(
                        getContentResolver(),
                        EyeUtils.SETTINGS_KEY_BIOMETRIC_MODE,
                        mode,
                        mUserId);
                return true;
            });
        }
    }

    private void updateEnrolledEyesList() {
        if (mEnrolledCategory == null) return;
        mEnrolledCategory.removeAll();

        final List<EyeTemplate> eyes = EyeUtils.getEnrolledEyes(getContext(), mUserId);

        for (int i = 0; i < eyes.size(); i++) {
            final EyeTemplate eye = eyes.get(i);
            final EyePreference pref = new EyePreference(getContext(), p -> {
                showConfirmDeleteDialog(p.getEye());
            });
            pref.setKey("security_settings_eye_" + eye.getId());
            String title = eye.getName();
            if (title == null || title.isEmpty()) {
                title = getString(R.string.eye_default_name, (i + 1));
            }
            pref.setTitle(title);
            pref.setIcon(R.drawable.ic_eye_24dp);
            pref.setEye(eye);
            pref.setPersistent(false);
            mEnrolledCategory.addPreference(pref);
        }

        if (eyes.size() < EyeUtils.MAX_EYE_ENROLLMENTS) {
            if (mAddEyePreference == null) {
                mAddEyePreference = new Preference(getContext());
                mAddEyePreference.setKey(PREF_KEY_ADD_EYE);
                mAddEyePreference.setTitle(R.string.security_settings_eye_add_title);
                mAddEyePreference.setIcon(R.drawable.ic_add_24dp);
                mAddEyePreference.setOnPreferenceClickListener(preference -> {
                    startEyeEnrollment();
                    return true;
                });
            }
            mEnrolledCategory.addPreference(mAddEyePreference);
        }
    }

    private void startEyeEnrollment() {
        final Intent intent = new Intent("co.aospa.sense.action.ENROLL_EYE");
        intent.setPackage("co.aospa.sense");
        intent.putExtra(Intent.EXTRA_USER_ID, mUserId);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(intent, ENROLL_REQUEST);
        } else {
            // Internal fallback enrollment
            final Intent fallback = new Intent(getContext(), EyeEnrollActivity.class);
            fallback.putExtra(Intent.EXTRA_USER_ID, mUserId);
            startActivityForResult(fallback, ENROLL_REQUEST);
        }
    }

    private void showConfirmDeleteDialog(EyeTemplate eye) {
        if (eye == null || getActivity() == null) return;
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.security_settings_eye_remove_dialog_title)
                .setMessage(R.string.security_settings_eye_remove_dialog_message)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    EyeUtils.removeEye(getContext(), eye.getId(), mUserId);
                    updateEnrolledEyesList();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CONFIRM_REQUEST || requestCode == CHOOSE_LOCK_REQUEST) {
            mConfirmingPassword = false;
            if (resultCode == Activity.RESULT_OK && data != null) {
                mToken = data.getByteArrayExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE_TOKEN);
                updateEnrolledEyesList();
            } else {
                finish();
            }
        } else if (requestCode == ENROLL_REQUEST) {
            updateEnrolledEyesList();
        }
    }
}
