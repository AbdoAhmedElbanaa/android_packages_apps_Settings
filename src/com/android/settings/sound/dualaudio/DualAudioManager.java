/*
 * Copyright (C) 2024 The Android Open Source Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.sound.dualaudio;

import android.content.Context;
import android.media.AudioManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;

import com.android.settings.R;

public class DualAudioManager {
    private static final String TAG = "DualAudioManager";

    // Settings.System keys
    public static final String KEY_DUAL_AUDIO_ENABLED = "dual_audio_enabled";
    public static final String KEY_DUAL_AUDIO_PRESET = "dual_audio_preset";
    public static final String KEY_DUAL_AUDIO_GAIN = "dual_audio_earpiece_gain";
    public static final String KEY_DUAL_AUDIO_BALANCE = "dual_audio_speaker_balance";

    // System properties for HAL and kernel level persistence
    public static final String PROP_SYS_DUAL_AUDIO = "persist.sys.audio.dual_audio";
    public static final String PROP_SYS_DUAL_AUDIO_PRESET = "persist.sys.audio.dual_audio_preset";
    public static final String PROP_SYS_DUAL_AUDIO_GAIN = "persist.sys.audio.dual_audio_ear_gain";
    public static final String PROP_SYS_DUAL_AUDIO_BALANCE = "persist.sys.audio.dual_audio_balance";

    public static final String PROP_VENDOR_DUAL_AUDIO = "persist.vendor.audio.dual_audio";
    public static final String PROP_VENDOR_DUAL_AUDIO_PRESET = "persist.vendor.audio.dual_audio_preset";
    public static final String PROP_VENDOR_DUAL_AUDIO_GAIN = "persist.vendor.audio.dual_audio_ear_gain";
    public static final String PROP_VENDOR_DUAL_AUDIO_BALANCE = "persist.vendor.audio.dual_audio_balance";

    public static final String PROP_DUAL_AUDIO = "persist.audio.dual_audio";
    public static final String PROP_DUAL_AUDIO_PRESET = "persist.audio.dual_audio_preset";
    public static final String PROP_DUAL_AUDIO_GAIN = "persist.audio.dual_audio_ear_gain";
    public static final String PROP_DUAL_AUDIO_BALANCE = "persist.audio.dual_audio_balance";

    // Presets
    public static final String PRESET_BALANCED = "balanced";
    public static final String PRESET_VOCAL = "vocal";
    public static final String PRESET_DYNAMIC = "dynamic";
    public static final String PRESET_BASS = "bass";
    public static final String PRESET_AMBIENT = "ambient";
    public static final String PRESET_CUSTOM = "custom";

    // Defaults
    public static final String DEFAULT_PRESET = PRESET_BALANCED;
    public static final String DEFAULT_GAIN = "3";
    public static final String DEFAULT_BALANCE = "balanced";

    private static void setAllProps(String sysKey, String vendorKey, String legacyKey, String val) {
        setProp(sysKey, val);
        setProp(vendorKey, val);
        setProp(legacyKey, val);
    }

    private static void setProp(String key, String val) {
        try {
            SystemProperties.set(key, val);
        } catch (Exception e) {
            Log.e(TAG, "Error setting property " + key + ": " + e.getMessage(), e);
        }
    }

    public static boolean isDualAudioSupported(Context context) {
        try {
            return context.getResources().getBoolean(R.bool.config_support_dual_speaker);
        } catch (Exception e) {
            Log.e(TAG, "Error checking dual audio support: " + e.getMessage(), e);
            return false;
        }
    }

    public static boolean isDualAudioEnabled(Context context) {
        try {
            return Settings.System.getInt(context.getContentResolver(), KEY_DUAL_AUDIO_ENABLED, 0) == 1;
        } catch (Exception e) {
            Log.e(TAG, "Error checking dual audio enabled state: " + e.getMessage(), e);
            return false;
        }
    }

    public static void setDualAudioEnabled(Context context, boolean enabled) {
        try {
            Settings.System.putInt(context.getContentResolver(), KEY_DUAL_AUDIO_ENABLED, enabled ? 1 : 0);
            String val = enabled ? "1" : "0";
            setAllProps(PROP_SYS_DUAL_AUDIO, PROP_VENDOR_DUAL_AUDIO, PROP_DUAL_AUDIO, val);

            AudioManager am = context.getSystemService(AudioManager.class);
            if (am != null) {
                am.setParameters("dual_audio=" + val);
                String preset = getPreset(context);
                am.setParameters("dual_audio_preset=" + preset);
                if (PRESET_CUSTOM.equals(preset)) {
                    am.setParameters("dual_audio_gain=" + getEarpieceGain(context));
                    am.setParameters("dual_audio_balance=" + getSpeakerBalance(context));
                }
                am.setParameters("restart_audio=1");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error toggling dual audio: " + e.getMessage(), e);
        }
    }

    public static String getPreset(Context context) {
        try {
            String preset = Settings.System.getString(context.getContentResolver(), KEY_DUAL_AUDIO_PRESET);
            if (preset != null && !preset.isEmpty()) {
                return preset;
            }
            String prop = SystemProperties.get(PROP_SYS_DUAL_AUDIO_PRESET, "");
            if (!prop.isEmpty()) return prop;
            prop = SystemProperties.get(PROP_VENDOR_DUAL_AUDIO_PRESET, "");
            if (!prop.isEmpty()) return prop;
            return DEFAULT_PRESET;
        } catch (Exception e) {
            Log.e(TAG, "Error reading dual audio preset: " + e.getMessage(), e);
            return DEFAULT_PRESET;
        }
    }

    public static void setPreset(Context context, String preset) {
        try {
            Settings.System.putString(context.getContentResolver(), KEY_DUAL_AUDIO_PRESET, preset);
            setAllProps(PROP_SYS_DUAL_AUDIO_PRESET, PROP_VENDOR_DUAL_AUDIO_PRESET, PROP_DUAL_AUDIO_PRESET, preset);

            AudioManager am = context.getSystemService(AudioManager.class);
            if (am != null) {
                am.setParameters("dual_audio_preset=" + preset);
                if (PRESET_CUSTOM.equals(preset)) {
                    am.setParameters("dual_audio_gain=" + getEarpieceGain(context));
                    am.setParameters("dual_audio_balance=" + getSpeakerBalance(context));
                }
                am.setParameters("restart_audio=1");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting dual audio preset: " + e.getMessage(), e);
        }
    }

    public static String getEarpieceGain(Context context) {
        try {
            String gain = Settings.System.getString(context.getContentResolver(), KEY_DUAL_AUDIO_GAIN);
            if (gain != null && !gain.isEmpty()) {
                return gain;
            }
            String prop = SystemProperties.get(PROP_SYS_DUAL_AUDIO_GAIN, "");
            if (!prop.isEmpty()) return prop;
            prop = SystemProperties.get(PROP_VENDOR_DUAL_AUDIO_GAIN, "");
            if (!prop.isEmpty()) return prop;
            return DEFAULT_GAIN;
        } catch (Exception e) {
            Log.e(TAG, "Error reading earpiece gain: " + e.getMessage(), e);
            return DEFAULT_GAIN;
        }
    }

    public static void setEarpieceGain(Context context, String gain) {
        try {
            Settings.System.putString(context.getContentResolver(), KEY_DUAL_AUDIO_GAIN, gain);
            setAllProps(PROP_SYS_DUAL_AUDIO_GAIN, PROP_VENDOR_DUAL_AUDIO_GAIN, PROP_DUAL_AUDIO_GAIN, gain);

            AudioManager am = context.getSystemService(AudioManager.class);
            if (am != null) {
                am.setParameters("dual_audio_gain=" + gain);
                am.setParameters("restart_audio=1");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting earpiece gain: " + e.getMessage(), e);
        }
    }

    public static String getSpeakerBalance(Context context) {
        try {
            String balance = Settings.System.getString(context.getContentResolver(), KEY_DUAL_AUDIO_BALANCE);
            if (balance != null && !balance.isEmpty()) {
                return balance;
            }
            String prop = SystemProperties.get(PROP_SYS_DUAL_AUDIO_BALANCE, "");
            if (!prop.isEmpty()) return prop;
            prop = SystemProperties.get(PROP_VENDOR_DUAL_AUDIO_BALANCE, "");
            if (!prop.isEmpty()) return prop;
            return DEFAULT_BALANCE;
        } catch (Exception e) {
            Log.e(TAG, "Error reading speaker balance: " + e.getMessage(), e);
            return DEFAULT_BALANCE;
        }
    }

    public static void setSpeakerBalance(Context context, String balance) {
        try {
            Settings.System.putString(context.getContentResolver(), KEY_DUAL_AUDIO_BALANCE, balance);
            setAllProps(PROP_SYS_DUAL_AUDIO_BALANCE, PROP_VENDOR_DUAL_AUDIO_BALANCE, PROP_DUAL_AUDIO_BALANCE, balance);

            AudioManager am = context.getSystemService(AudioManager.class);
            if (am != null) {
                am.setParameters("dual_audio_balance=" + balance);
                am.setParameters("restart_audio=1");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting speaker balance: " + e.getMessage(), e);
        }
    }

    /**
     * Re-applies all Dual Audio configuration to settings, system properties, and audio HAL.
     */
    public static boolean applySettings(Context context) {
        try {
            boolean enabled = isDualAudioEnabled(context);
            String preset = getPreset(context);
            String gain = getEarpieceGain(context);
            String balance = getSpeakerBalance(context);

            String enVal = enabled ? "1" : "0";
            Settings.System.putInt(context.getContentResolver(), KEY_DUAL_AUDIO_ENABLED, enabled ? 1 : 0);
            Settings.System.putString(context.getContentResolver(), KEY_DUAL_AUDIO_PRESET, preset);
            Settings.System.putString(context.getContentResolver(), KEY_DUAL_AUDIO_GAIN, gain);
            Settings.System.putString(context.getContentResolver(), KEY_DUAL_AUDIO_BALANCE, balance);

            setAllProps(PROP_SYS_DUAL_AUDIO, PROP_VENDOR_DUAL_AUDIO, PROP_DUAL_AUDIO, enVal);
            setAllProps(PROP_SYS_DUAL_AUDIO_PRESET, PROP_VENDOR_DUAL_AUDIO_PRESET, PROP_DUAL_AUDIO_PRESET, preset);
            setAllProps(PROP_SYS_DUAL_AUDIO_GAIN, PROP_VENDOR_DUAL_AUDIO_GAIN, PROP_DUAL_AUDIO_GAIN, gain);
            setAllProps(PROP_SYS_DUAL_AUDIO_BALANCE, PROP_VENDOR_DUAL_AUDIO_BALANCE, PROP_DUAL_AUDIO_BALANCE, balance);

            AudioManager am = context.getSystemService(AudioManager.class);
            if (am != null) {
                am.setParameters("dual_audio=" + enVal);
                am.setParameters("dual_audio_preset=" + preset);
                am.setParameters("dual_audio_gain=" + gain);
                am.setParameters("dual_audio_balance=" + balance);
                am.setParameters("restart_audio=1");
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error applying dual audio settings: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Re-applies all Dual Audio settings and restarts audio service / HAL.
     */
    public static boolean restartAudioService(Context context) {
        applySettings(context);
        try {
            SystemProperties.set("ctl.restart", "audioserver");
        } catch (Exception e) {
            Log.e(TAG, "ctl.restart audioserver: " + e.getMessage());
        }
        return true;
    }

    public static CharSequence getPresetLabel(Context context, String presetValue) {
        try {
            String[] values = context.getResources().getStringArray(R.array.dual_audio_preset_values);
            String[] entries = context.getResources().getStringArray(R.array.dual_audio_preset_entries);
            for (int i = 0; i < values.length && i < entries.length; i++) {
                if (values[i].equals(presetValue)) {
                    return entries[i];
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting preset label: " + e.getMessage(), e);
        }
        return presetValue;
    }

    public static CharSequence getGainLabel(Context context, String gainValue) {
        try {
            String[] values = context.getResources().getStringArray(R.array.dual_audio_gain_values);
            String[] entries = context.getResources().getStringArray(R.array.dual_audio_gain_entries);
            for (int i = 0; i < values.length && i < entries.length; i++) {
                if (values[i].equals(gainValue)) {
                    return entries[i];
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting gain label: " + e.getMessage(), e);
        }
        return gainValue;
    }

    public static CharSequence getBalanceLabel(Context context, String balanceValue) {
        try {
            String[] values = context.getResources().getStringArray(R.array.dual_audio_balance_values);
            String[] entries = context.getResources().getStringArray(R.array.dual_audio_balance_entries);
            for (int i = 0; i < values.length && i < entries.length; i++) {
                if (values[i].equals(balanceValue)) {
                    return entries[i];
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting balance label: " + e.getMessage(), e);
        }
        return balanceValue;
    }
}
