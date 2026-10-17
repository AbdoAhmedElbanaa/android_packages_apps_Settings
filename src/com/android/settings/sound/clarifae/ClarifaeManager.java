/*
 * Copyright (C) 2026 The Android Open Source Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.sound.clarifae;

import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;

import java.io.File;

public class ClarifaeManager {
    private static final String TAG = "ClarifaeManager";

    // System properties for HAL and audio effect configuration
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

    private static void setProp(String key, String val) {
        try {
            SystemProperties.set(key, val);
        } catch (Exception e) {
            Log.e(TAG, "Error setting property " + key + ": " + e.getMessage(), e);
        }
    }

    private static boolean getPropBool(String key, boolean def) {
        return SystemProperties.getBoolean(key, def);
    }

    private static int getPropInt(String key, int def) {
        return SystemProperties.getInt(key, def);
    }

    public static boolean isClarifaeSupported(Context context) {
        File lib64 = new File("/vendor/lib64/soundfx/libclarifae.so");
        File lib32 = new File("/vendor/lib/soundfx/libclarifae.so");
        return lib64.exists() || lib32.exists()
                || "miatoll".equalsIgnoreCase(SystemProperties.get("ro.build.product", ""))
                || "miatoll".equalsIgnoreCase(SystemProperties.get("ro.product.device", ""));
    }

    public static boolean isEnabled() {
        return getPropBool(PROP_ENABLED, DEFAULT_ENABLED);
    }

    public static void setEnabled(boolean enabled) {
        setProp(PROP_ENABLED, enabled ? "1" : "0");
    }

    public static boolean isInEnabled() {
        return getPropBool(PROP_IN_ENABLED, DEFAULT_IN_ENABLED);
    }

    public static void setInEnabled(boolean enabled) {
        setProp(PROP_IN_ENABLED, enabled ? "1" : "0");
    }

    public static boolean isOutEnabled() {
        return getPropBool(PROP_OUT_ENABLED, DEFAULT_OUT_ENABLED);
    }

    public static void setOutEnabled(boolean enabled) {
        setProp(PROP_OUT_ENABLED, enabled ? "1" : "0");
    }

    public static int getStrength() {
        return getPropInt(PROP_STRENGTH, DEFAULT_STRENGTH);
    }

    public static void setStrength(int strength) {
        setProp(PROP_STRENGTH, String.valueOf(Math.max(0, Math.min(100, strength))));
    }

    public static int getOutStrength() {
        return getPropInt(PROP_OUT_STRENGTH, DEFAULT_OUT_STRENGTH);
    }

    public static void setOutStrength(int strength) {
        setProp(PROP_OUT_STRENGTH, String.valueOf(Math.max(0, Math.min(100, strength))));
    }

    public static int getAttenDb() {
        return getPropInt(PROP_ATTEN_DB, DEFAULT_ATTEN_DB);
    }

    public static void setAttenDb(int attenDb) {
        setProp(PROP_ATTEN_DB, String.valueOf(Math.max(0, Math.min(60, attenDb))));
    }

    public static int getVad() {
        return getPropInt(PROP_VAD, DEFAULT_VAD);
    }

    public static void setVad(int vad) {
        setProp(PROP_VAD, String.valueOf(Math.max(0, Math.min(100, vad))));
    }

    public static boolean isHighpassEnabled() {
        return getPropBool(PROP_HIGHPASS, DEFAULT_HIGHPASS);
    }

    public static void setHighpassEnabled(boolean enabled) {
        setProp(PROP_HIGHPASS, enabled ? "1" : "0");
    }

    public static boolean isAutogainEnabled() {
        return getPropBool(PROP_AUTOGAIN, DEFAULT_AUTOGAIN);
    }

    public static void setAutogainEnabled(boolean enabled) {
        setProp(PROP_AUTOGAIN, enabled ? "1" : "0");
    }
}
