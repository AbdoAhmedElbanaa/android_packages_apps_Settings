package com.android.settings.biometrics.eye;

import org.json.JSONException;
import org.json.JSONObject;

public class EyeTemplate {
    private final int mId;
    private final String mName;
    private final long mTimestamp;

    public EyeTemplate(int id, String name, long timestamp) {
        mId = id;
        mName = name;
        mTimestamp = timestamp;
    }

    public int getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("templateId", mId);
        obj.put("name", mName);
        obj.put("timestamp", mTimestamp);
        return obj;
    }

    public static EyeTemplate fromJson(JSONObject obj) {
        int id = obj.optInt("templateId", obj.optInt("id", 1));
        String name = obj.optString("name", "");
        long timestamp = obj.optLong("timestamp", System.currentTimeMillis());
        return new EyeTemplate(id, name, timestamp);
    }
}
