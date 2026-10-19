/*
 * Copyright (C) 2026 The Android Open Source Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.sound.clarifae;

import android.content.Context;
import android.media.AudioManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;

import java.io.File;

public class ClarifaeManager {
    private static final String TAG = "ClarifaeManager";

    // System property prefixes
    public static final String PREFIX_SYS = "persist.sys.clarifae.";
    public static final String PREFIX_VENDOR = "persist.vendor.clarifae.";
    public static final String PREFIX_LEGACY = "persist.clarifae.";

    // Property keys
    public static final String KEY_ENABLED = "enabled";
    public static final String KEY_IN_ENABLED = "in_enabled";
    public static final String KEY_OUT_ENABLED = "out_enabled";
    public static final String KEY_MODE = "mode";
    public static final String KEY_STRENGTH = "strength";
    public static final String KEY_OUT_STRENGTH = "out_strength";
    public static final String KEY_ATTEN_DB = "atten_db";
    public static final String KEY_VAD = "vad";
    public static final String KEY_HIGHPASS = "highpass";
    public static final String KEY_AUTOGAIN = "autogain";

    // Legacy vendor property constants (kept for backwards compatibility)
    public static final String PROP_ENABLED = "persist.vendor.clarifae.enabled";
    public static final String PROP_IN_ENABLED = "persist.vendor.clarifae.in_enabled";
    public static final String PROP_OUT_ENABLED = "persist.vendor.clarifae.out_enabled";
    public static final String PROP_MODE = "persist.vendor.clarifae.mode";
    public static final String PROP_STRENGTH = "persist.vendor.clarifae.strength";
    public static final String PROP_OUT_STRENGTH = "persist.vendor.clarifae.out_strength";
    public static final String PROP_ATTEN_DB = "persist.vendor.clarifae.atten_db";
    public static final String PROP_VAD = "persist.vendor.clarifae.vad";
    public static final String PROP_HIGHPASS = "persist.vendor.clarifae.highpass";
    public static final String PROP_AUTOGAIN = "persist.vendor.clarifae.autogain";

    // Defaults
    public static final boolean DEFAULT_ENABLED = true;
    public static final boolean DEFAULT_IN_ENABLED = true;
    public static final boolean DEFAULT_OUT_ENABLED = false;
    public static final int DEFAULT_STRENGTH = 70;
    public static final int DEFAULT_OUT_STRENGTH = 50;
    public static final int DEFAULT_ATTEN_DB = 24;
    public static final int DEFAULT_VAD = 50;
    public static final boolean DEFAULT_HIGHPASS = true;
    public static final boolean DEFAULT_AUTOGAIN = false;

    private static void setAllProps(String key, String val) {
        setProp(PREFIX_SYS + key, val);
        setProp(PREFIX_VENDOR + key, val);
        setProp(PREFIX_LEGACY + key, val);
    }

    private static void setProp(String key, String val) {
        try {
            SystemProperties.set(key, val);
        } catch (Exception e) {
            Log.e(TAG, "Error setting property " + key + ": " + e.getMessage(), e);
        }
    }

    private static boolean getPropBool(String key, boolean def) {
        String val = SystemProperties.get(PREFIX_SYS + key, "");
        if (val.isEmpty()) {
            val = SystemProperties.get(PREFIX_VENDOR + key, "");
        }
        if (val.isEmpty()) {
            val = SystemProperties.get(PREFIX_LEGACY + key, "");
        }
        if (val.isEmpty()) {
            val = readConfigKey(key);
        }
        if (val.isEmpty()) {
            return def;
        }
        return "1".equals(val) || "true".equalsIgnoreCase(val) || "on".equalsIgnoreCase(val);
    }

    private static String readConfigKey(String key) {
        String[] paths = {"/data/misc/clarifae/config.conf", "/data/vendor/clarifae/config.conf", "/data/adb/clarifae/config.conf", "/vendor/etc/clarifae/config.conf"};
        for (String p : paths) {
            File f = new File(p);
            if (f.exists() && f.canRead()) {
                try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(f))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        line = line.trim();
                        if (line.startsWith(key + "=")) {
                            return line.substring(key.length() + 1).trim();
                        }
                    }
                } catch (Exception ignored) {}
            }
        }
        // If no config file found, create default one
        ensureDefaultConfigFile();
        return "";
    }

    private static void ensureDefaultConfigFile() {
        try {
            File f = new File("/data/misc/clarifae/config.conf");
            if (!f.exists()) {
                File dir = f.getParentFile();
                if (dir != null && !dir.exists()) {
                    dir.mkdirs();
                }
                try (java.io.FileWriter fw = new java.io.FileWriter(f)) {
                    fw.write("enabled=1\nin_enabled=1\nout_enabled=0\nmode=1\nstrength=70\nout_strength=60\natten_db=24\nvad=50\ntargets=mic,voice_comm,camcorder,voice_rec\nout_targets=music,voice_call,system\nhighpass=1\nautogain=0\nloglevel=1\n");
                }
                f.setReadable(true, false);
                f.setWritable(true, false);
            }
        } catch (Exception ignored) {}
    }

    private static int getPropInt(String key, int def) {
        String val = SystemProperties.get(PREFIX_SYS + key, "");
        if (val.isEmpty()) {
            val = SystemProperties.get(PREFIX_VENDOR + key, "");
        }
        if (val.isEmpty()) {
            val = SystemProperties.get(PREFIX_LEGACY + key, "");
        }
        if (val.isEmpty()) {
            return def;
        }
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static boolean isPackageInstalled(Context context, String pkg) {
        if (context == null || pkg == null) return false;
        try {
            context.getPackageManager().getPackageInfo(pkg, 0);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isClarifaeSupported(Context context) {
        if (!isPackageInstalled(context, "org.rising.clarifae")) {
            return false;
        }
        File lib64 = new File("/vendor/lib64/soundfx/libclarifae.so");
        File lib32 = new File("/vendor/lib/soundfx/libclarifae.so");
        return lib64.exists() || lib32.exists();
    }

    public static boolean isEnabled() {
        return getPropBool(KEY_ENABLED, DEFAULT_ENABLED);
    }

    public static void setEnabled(boolean enabled) {
        setAllProps(KEY_ENABLED, enabled ? "1" : "0");
    }

    public static boolean isInEnabled() {
        return getPropBool(KEY_IN_ENABLED, DEFAULT_IN_ENABLED);
    }

    public static void setInEnabled(boolean enabled) {
        setAllProps(KEY_IN_ENABLED, enabled ? "1" : "0");
    }

    public static boolean isOutEnabled() {
        return getPropBool(KEY_OUT_ENABLED, DEFAULT_OUT_ENABLED);
    }

    public static void setOutEnabled(boolean enabled) {
        setAllProps(KEY_OUT_ENABLED, enabled ? "1" : "0");
    }

    public static int getStrength() {
        return getPropInt(KEY_STRENGTH, DEFAULT_STRENGTH);
    }

    public static void setStrength(int strength) {
        setAllProps(KEY_STRENGTH, String.valueOf(Math.max(0, Math.min(100, strength))));
    }

    public static int getOutStrength() {
        return getPropInt(KEY_OUT_STRENGTH, DEFAULT_OUT_STRENGTH);
    }

    public static void setOutStrength(int strength) {
        setAllProps(KEY_OUT_STRENGTH, String.valueOf(Math.max(0, Math.min(100, strength))));
    }

    public static int getAttenDb() {
        return getPropInt(KEY_ATTEN_DB, DEFAULT_ATTEN_DB);
    }

    public static void setAttenDb(int attenDb) {
        setAllProps(KEY_ATTEN_DB, String.valueOf(Math.max(0, Math.min(60, attenDb))));
    }

    public static int getVad() {
        return getPropInt(KEY_VAD, DEFAULT_VAD);
    }

    public static void setVad(int vad) {
        setAllProps(KEY_VAD, String.valueOf(Math.max(0, Math.min(100, vad))));
    }

    public static boolean isHighpassEnabled() {
        return getPropBool(KEY_HIGHPASS, DEFAULT_HIGHPASS);
    }

    public static void setHighpassEnabled(boolean enabled) {
        setAllProps(KEY_HIGHPASS, enabled ? "1" : "0");
    }

    public static boolean isAutogainEnabled() {
        return getPropBool(KEY_AUTOGAIN, DEFAULT_AUTOGAIN);
    }

    public static void setAutogainEnabled(boolean enabled) {
        setAllProps(KEY_AUTOGAIN, enabled ? "1" : "0");
    }

    /**
     * Applies all current voice clarity parameters to system properties and audio HAL.
     */
    public static boolean applySettings(Context context) {
        try {
            boolean enabled = isEnabled();
            boolean inEnabled = isInEnabled();
            boolean outEnabled = isOutEnabled();
            int strength = getStrength();
            int outStrength = getOutStrength();
            int attenDb = getAttenDb();
            int vad = getVad();
            boolean highpass = isHighpassEnabled();
            boolean autogain = isAutogainEnabled();

            setAllProps(KEY_ENABLED, enabled ? "1" : "0");
            setAllProps(KEY_IN_ENABLED, inEnabled ? "1" : "0");
            setAllProps(KEY_OUT_ENABLED, outEnabled ? "1" : "0");
            setAllProps(KEY_STRENGTH, String.valueOf(strength));
            setAllProps(KEY_OUT_STRENGTH, String.valueOf(outStrength));
            setAllProps(KEY_ATTEN_DB, String.valueOf(attenDb));
            setAllProps(KEY_VAD, String.valueOf(vad));
            setAllProps(KEY_HIGHPASS, highpass ? "1" : "0");
            setAllProps(KEY_AUTOGAIN, autogain ? "1" : "0");

            if (context != null) {
                try {
                    Settings.System.putInt(context.getContentResolver(), "clarifae_enabled", enabled ? 1 : 0);
                    Settings.System.putInt(context.getContentResolver(), "clarifae_in_enabled", inEnabled ? 1 : 0);
                    Settings.System.putInt(context.getContentResolver(), "clarifae_out_enabled", outEnabled ? 1 : 0);
                    Settings.System.putInt(context.getContentResolver(), "clarifae_strength", strength);
                    Settings.System.putInt(context.getContentResolver(), "clarifae_out_strength", outStrength);
                    Settings.System.putInt(context.getContentResolver(), "clarifae_atten_db", attenDb);
                    Settings.System.putInt(context.getContentResolver(), "clarifae_vad", vad);
                    Settings.System.putInt(context.getContentResolver(), "clarifae_highpass", highpass ? 1 : 0);
                    Settings.System.putInt(context.getContentResolver(), "clarifae_autogain", autogain ? 1 : 0);
                } catch (Exception e) {
                    Log.e(TAG, "Error saving clarifae to Settings.System: " + e.getMessage(), e);
                }

                AudioManager am = context.getSystemService(AudioManager.class);
                if (am != null) {
                    am.setParameters("clarifae_enabled=" + (enabled ? "1" : "0"));
                    am.setParameters("restart_audio=1");
                }
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error applying clarifae settings: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Re-applies all settings and triggers an audio service / HAL reload.
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
}
