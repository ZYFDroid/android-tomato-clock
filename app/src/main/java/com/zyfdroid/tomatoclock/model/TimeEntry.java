package com.zyfdroid.tomatoclock.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by WorldSkills2020 on 2/23/2020.
 */

public class TimeEntry {
    String name;
    // 秒
    int duration;
    int backgroundColor;

    public TimeEntry(String name, int duration, int backgroundColor) {
        this.name = name;
        this.duration = duration;
        this.backgroundColor = backgroundColor;
    }

    public static TimeEntry fromJsonObject(JSONObject obj) throws JSONException {
        return new TimeEntry(obj.getString("name"),obj.getInt("duration"),obj.getInt("backgroundcolor"));
    }

    public JSONObject toJsonObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("name",name);
        obj.put("duration",duration);
        obj.put("backgroundcolor",backgroundColor);
        return  obj;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }
}
