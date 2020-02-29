package com.zyfdroid.tomatoclock;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zyfdroid.tomatoclock.model.TimeEntry;
import com.zyfdroid.tomatoclock.util.SpUtils;

import java.util.Date;
import java.util.List;

public class ClockActivity extends Activity {

    static ClockActivity mInstance = null;

    FrameLayout activity_clock = null;
    LinearLayout clockBackground = null;
    ProgressBar progressTime = null;
    TextView txtClockTime = null;
    TextView txtClockDescription = null;

    void initUi(){
        activity_clock = (FrameLayout)findViewById(R.id.activity_clock);
        clockBackground = (LinearLayout)findViewById(R.id.clockBackground);
        progressTime = (ProgressBar)findViewById(R.id.progressTime);
        txtClockTime = (TextView)findViewById(R.id.txtClockTime);
        txtClockDescription = (TextView)findViewById(R.id.txtClockDescription);
    }


    public void onStopClick(View v){
        new AlertDialog.Builder(this).setTitle("停止番茄钟").setMessage("是否停止番茄钟?")
                .setPositiveButton("是的", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SpUtils.setStatus(false);
                        SpUtils.setStartTime(-1);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(ClockActivity.this, 0, new Intent(ClockActivity.this, TimeOutReceiver.class), 0);
                        AlarmManager.AlarmClockInfo next = alarmManager.getNextAlarmClock();
                        if (null != next) {
                            alarmManager.cancel(pendingIntent);
                        }
                        startActivity(new Intent(ClockActivity.this,MainActivity.class));
                        finish();
                    }
                }).setNegativeButton("不是",null).create().show();
    }

    Handler hWnd = new Handler();
    Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            updateTime();
            hWnd.postDelayed(updateRunnable,16);
        }
    };

    AlarmManager alarmManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mInstance = this;
        setContentView(R.layout.activity_clock);
        initUi();
        alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        startTime = SpUtils.getStartTime();//获取开始时间，作为计算当前阶段的参考
        entries = SpUtils.getCurrent();
        hWnd.postDelayed(updateRunnable,16);
        if(getIntent().getBooleanExtra("notification",false)){
            requireNotification();
        }
        doUpdateAlarmClock();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hWnd.removeCallbacks(updateRunnable);
        mInstance = null;
    }

    public void requireNotification(){
        TimeEntry entry = getCurrentTimeEntry();
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notification = new Notification.Builder(this)
                .setContentTitle("番茄钟")  //设置标题
                .setContentText(entry.getName()+"的时间到了！") //设置内容
                .setWhen(System.currentTimeMillis())  //设置时间
                .setSmallIcon(R.drawable.ic_stat_clock)  //设置小图标  只能使用alpha图层的图片进行设置
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.ic_launcher))   //设置大图标
                //.setContentIntent(pi)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setShowWhen(true)
                .build();
        manager.notify(1,notification);
        wakeUpAndUnlock(this);
        doUpdateAlarmClock();
    }

    long startTime = -1;
    List<TimeEntry> entries = null;

    long cycleDuration = -1;



    TimeEntry getCurrentTimeEntry(){
        long elapsedTime = System.currentTimeMillis() -  startTime;
        if(cycleDuration <0){
            cycleDuration=0;//获取周期总长度
            for (TimeEntry entry:
                 entries) {
                cycleDuration += entry.getDuration() * 1000L;
            }

        }
        long currentCycleElapsed = elapsedTime % cycleDuration;//当前周期的时间段
        long temp=0;
        TimeEntry last = entries.get(0);
        for (TimeEntry entry:
                entries) {
            temp += entry.getDuration() * 1000L;
            if(temp > currentCycleElapsed){
                return entry;
            }
            last = entry;
        }
        return last;
    }

    //获取当前事件剩余时间
    long getEliminateTime(){
        long elapsedTime = System.currentTimeMillis() -  startTime;
        if(cycleDuration <0){
            cycleDuration=0;//获取周期总长度
            for (TimeEntry entry:
                    entries) {
                cycleDuration += entry.getDuration() * 1000L;
            }

        }
        long currentCycleElapsed = elapsedTime % cycleDuration;//当前周期的时间段
        long temp = currentCycleElapsed;
        for (TimeEntry entry:
                entries) {
            temp -= entry.getDuration() * 1000L;
            if(temp < 0){
                return -temp;
            }
        }
        return -temp;
    }

    String long2TimeText(long l){
        long totalseconds = l / 1000;
        long minute = totalseconds / 60;
        long second = totalseconds % 60;
        return String.format("%02d:%02d",minute,second);
    }

    Interpolator accelerate = new AccelerateDecelerateInterpolator();
    Interpolator deaccelerate = new DecelerateInterpolator();

    public void updateTime(){
        TimeEntry current = getCurrentTimeEntry();
        long eliminate = getEliminateTime();
        long elapsedTime = current.getDuration() * 1000L - eliminate;
        txtClockDescription.setText(current.getName());
        clockBackground.setBackgroundColor(current.getBackgroundColor());
        txtClockTime.setText(long2TimeText(eliminate));

        progressTime.setMax(current.getDuration() * 100);
        progressTime.setProgress(current.getDuration() * 100 - (int) (eliminate / 10));

        float animationX = 1;
        float animationSize=1;
        if(eliminate<667){
            animationX =(eliminate) / 667f;
            animationSize =2f- accelerate.getInterpolation(animationX);
        }
        if(elapsedTime < 667){
            animationX =(elapsedTime)  / 667f;
            animationSize = 0.5f + 0.5f * deaccelerate.getInterpolation(animationX);
        }

        Interpolator ip = new AccelerateDecelerateInterpolator();

        clockBackground.setAlpha(animationX);
        clockBackground.setScaleX(animationSize);
        clockBackground.setScaleY(animationSize);
    }


    void updateAlarmClock(){
        synchronized (alarmManager) {
            long nextTime = getEliminateTime() + System.currentTimeMillis();
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(this, TimeOutReceiver.class), 0);
            AlarmManager.AlarmClockInfo next = alarmManager.getNextAlarmClock();
            if (null != next) {
                if (nextTime == next.getTriggerTime()) {
                    return;
                } else {
                    alarmManager.cancel(pendingIntent);
                }
            }
            AlarmManager.AlarmClockInfo current = new AlarmManager.AlarmClockInfo(nextTime, pendingIntent);
            Log.d("MyAlarm","Set an alarm clock at "+new Date(nextTime).toString());
            alarmManager.setAlarmClock(current, pendingIntent);
        }
    }

    void doUpdateAlarmClock(){
        new Thread(){
            @Override
            public void run() {
                updateAlarmClock();
            }
        }.start();
    }


    public static void wakeUpAndUnlock(Context context){

        //获取电源管理器对象
        PowerManager pm=(PowerManager) context.getSystemService(Context.POWER_SERVICE);
        //获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP |
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK,"bright");
        //点亮屏幕
        wl.acquire();
        //释放
        wl.release();
    }



}
