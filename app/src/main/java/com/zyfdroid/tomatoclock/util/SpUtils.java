package com.zyfdroid.tomatoclock.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.zyfdroid.tomatoclock.model.TimeEntry;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by WorldSkills2020 on 2/23/2020.
 */

public class SpUtils {
    private static SharedPreferences sp = null;
    private static Context global;
    public static void init(Context ctx){
        global = ctx.getApplicationContext();
        sp = global.getSharedPreferences("default",Context.MODE_PRIVATE);
    }
    
    public static List<TimeEntry> getCurrent(){
        ArrayList<TimeEntry> al = new ArrayList<>();
        try {
            JSONArray jarr = new JSONArray(sp.getString("current","[]"));
            for (int i = 0; i < jarr.length(); i++) {
                al.add(TimeEntry.fromJsonObject(jarr.getJSONObject(i)));
            }
        } catch (JSONException e) {
            //不可能出现的情况
            Toast.makeText(global, "这不可能，你一定是做了什么修改，你这个肮脏的黑客", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        return al;
    }

    public static void saveCurrent(List<TimeEntry> data){
        JSONArray jsonArray = new JSONArray();
        try {
            for (int i = 0; i < data.size(); i++) {
                jsonArray.put(data.get(i).toJsonObject());
            }
        } catch (JSONException e) {
            //不可能出现的情况
            Toast.makeText(global, "不可能的情况发生了", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        sp.edit().putString("current",jsonArray.toString()).commit();
    }

    public static boolean getStatus(){
        return sp.getBoolean("on",false);
    }
    public static void setStatus(boolean value){
        sp.edit().putBoolean("on",value).commit();
    }

    public static long getStartTime(){
        return sp.getLong("startTime",-1);
    }
    public static void setStartTime(long value){
        sp.edit().putLong("startTime",value).commit();
    }
}
