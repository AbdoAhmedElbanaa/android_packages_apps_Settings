package com.android.settings.biometrics.eye;

import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.android.settings.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class EyeUtils {
    private static final String TAG = "EyeUtils";
    public static final String SETTINGS_KEY_EYE_TEMPLATES = "enrolled_eye_templates";
    public static final String SETTINGS_KEY_EYE_KEYGUARD_ENABLED = "eye_unlock_keyguard_enabled";
    public static final String SETTINGS_KEY_EYE_REQUIRE_GAZE = "eye_unlock_require_gaze";
    public static final String SETTINGS_KEY_BIOMETRIC_MODE = "face_unlock_biometric_mode";
    public static final int MAX_EYE_ENROLLMENTS = 5;

    public static List<EyeTemplate> getEnrolledEyes(Context context, int userId) {
        List<EyeTemplate> list = new ArrayList<>();
        if (context == null) return list;
        String raw = Settings.Secure.getStringForUser(
                context.getContentResolver(), SETTINGS_KEY_EYE_TEMPLATES, userId);
        if (TextUtils.isEmpty(raw)) {
            return list;
        }
        try {
            JSONArray arr = new JSONArray(raw);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.optJSONObject(i);
                if (obj != null) {
                    list.add(EyeTemplate.fromJson(obj));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse eye templates", e);
        }
        return list;
    }

    public static void saveEnrolledEyes(Context context, List<EyeTemplate> eyes, int userId) {
        if (context == null) return;
        JSONArray arr = new JSONArray();
        for (EyeTemplate eye : eyes) {
            try {
                arr.put(eye.toJson());
            } catch (Exception e) {
                Log.e(TAG, "Failed to serialize eye", e);
            }
        }
        Settings.Secure.putStringForUser(
                context.getContentResolver(), SETTINGS_KEY_EYE_TEMPLATES, arr.toString(), userId);
    }

    public static void addEye(Context context, int templateId, String name, int userId) {
        if (context == null) return;
        List<EyeTemplate> list = getEnrolledEyes(context, userId);
        list.removeIf(e -> e.getId() == templateId);
        list.add(new EyeTemplate(templateId, name, System.currentTimeMillis()));
        saveEnrolledEyes(context, list, userId);
    }

    public static void removeEye(Context context, int templateId, int userId) {
        if (context == null) return;
        String raw = Settings.Secure.getStringForUser(
                context.getContentResolver(), SETTINGS_KEY_EYE_TEMPLATES, userId);
        if (TextUtils.isEmpty(raw)) return;
        try {
            JSONArray arr = new JSONArray(raw);
            JSONArray updated = new JSONArray();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.optJSONObject(i);
                if (obj != null) {
                    int id = obj.optInt("templateId", obj.optInt("id", 1));
                    if (id != templateId) {
                        updated.put(obj);
                    }
                }
            }
            Settings.Secure.putStringForUser(
                    context.getContentResolver(), SETTINGS_KEY_EYE_TEMPLATES, updated.toString(), userId);
        } catch (Exception e) {
            Log.e(TAG, "Failed to delete eye template", e);
        }
    }

    public static boolean hasEnrolledEyes(Context context, int userId) {
        return !getEnrolledEyes(context, userId).isEmpty();
    }

    public static String getSummary(Context context, int userId) {
        if (context == null) return "";
        List<EyeTemplate> eyes = getEnrolledEyes(context, userId);
        if (eyes.isEmpty()) {
            return context.getString(R.string.security_settings_eye_preference_summary_none);
        } else if (eyes.size() == 1) {
            return context.getString(R.string.security_settings_eye_preference_summary_single);
        } else {
            return context.getString(R.string.security_settings_eye_preference_summary_multiple, eyes.size());
        }
    }
}
