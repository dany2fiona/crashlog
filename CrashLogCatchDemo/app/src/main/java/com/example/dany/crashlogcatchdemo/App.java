package com.example.dany.crashlogcatchdemo;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.util.HashSet;

/**
 * Created by dan.y on 2017/3/29.
 */

public class App extends Application {
    //全局的context
    public static Context sContext ;

    public  RestartAppService mService;
    private ServiceConnection conn = new ServiceConnection() {

        /**
         * Called when a connection to the Service has been established,
         * with the android.os.IBinder of the communication channel to the Service.
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = ((RestartAppService.MyBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {}

    };

    public void startRestartTask() {
        if(mService != null){
            mService.startRestartTask(sContext);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = this;
        // 启动服务，用于定时重启app
        Intent intent = new Intent(this, RestartAppService.class);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);

        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(this);
    }

    public static App getContext(){
        return (App) sContext;
    }

}
