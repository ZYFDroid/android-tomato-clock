package com.zyfdroid.tomatoclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

public class TimeOutReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(null==ClockActivity.mInstance){
            Intent i = new Intent(context,ClockActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra("notification",true);
            context.startActivity(i);
        }
        else{
            ClockActivity.mInstance.requireNotification();
        }
    }
}
