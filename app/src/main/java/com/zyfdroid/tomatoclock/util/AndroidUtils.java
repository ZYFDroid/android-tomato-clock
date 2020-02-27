package com.zyfdroid.tomatoclock.util;

import android.content.Context;

/**
 * Created by WorldSkills2020 on 2/23/2020.
 */

public class AndroidUtils {

    public static int dip2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
