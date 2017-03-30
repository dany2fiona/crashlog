package com.example.dany.crashlogcatchdemo;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by dan.y on 2017/3/30.
 */

public class RestartAppService extends Service {
    private static final long RESTART_DELAY = 1 * 1000; // 多少时间后重启
    private MyBinder mBinder;

    /**
     * 启动app重启任务
     */
    public void startRestartTask(final Context context) {

        TimerTask task = new TimerTask() {

            @Override
            public void run() {
                // restart
//                Intent intent = new Intent(context,MainActivity.class);
                Intent intent = getPackageManager().getLaunchIntentForPackage(getApplication().getPackageName());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                android.os.Process.killProcess(android.os.Process.myPid());
//                System.exit(0);
            }
        };

        Timer timer = new Timer();
        timer.schedule(task, RESTART_DELAY);
    }


    @Override
    public IBinder onBind(Intent intent) {
        // Create MyBinder object
        if (mBinder == null) {
            mBinder = new MyBinder();
        }
        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    // 此对象用于绑定的service与调用者之间的通信
    public class MyBinder extends Binder {

        /**
         * 获取service实例
         *
         * @return
         */
        public RestartAppService getService() {
            return RestartAppService.this;
        }
    }
}
