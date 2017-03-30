package com.example.dany.crashlogcatchdemo;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dan.y on 2017/3/29.
 */

public class CrashHandler implements Thread.UncaughtExceptionHandler {
    public static final String TAG = "CrashHandler";
    //系统默认的UncaughtException处理类
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    //CrashHandler实例
    private static CrashHandler instance = new CrashHandler();
    //程序的Context对象
    private Context mContext;
    //用来存储设备信息和异常信息
    private Map<String,String> infos = new HashMap<String,String>();
    //用于格式化日期，作为日志文件名的一部分
    private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    private String nameString;
    private final static String CrashLogDir = Environment.getExternalStorageDirectory()+"/CrashLogCatchDemo"+"/log/";

    /**
     * 单例模式保证只有一个CrashHandler实例
     */
    private CrashHandler(){
    }
    public static CrashHandler getInstance(){

        return instance;
    }

    /**
     * 初始化
     * @param context
     */
    public void init(Context context){
        mContext = context;
        //获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        //设置该CrashHandler为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
        nameString = "defaultuser";
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     * @param t
     * @param e
     */
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        if(!handleException(e) && mDefaultHandler != null){
            // 如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler.uncaughtException(t, e);
        }else{
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e1) {
                Log.e(TAG, "error : ", e1);
            }
//            //----------------一个Activity时的-------------------退出程序
//            Intent startMain = new Intent(Intent.ACTION_MAIN);
//            startMain.addCategory(Intent.CATEGORY_HOME);
//            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            mContext.startActivity(startMain);
//            System.exit(0);//退出程序
            //-----------------多个Activity时的--------------------重启程序 --(有延时)--
//            Intent startMain = new Intent(mContext,MainActivity.class);
//            startMain.addCategory(Intent.CATEGORY_HOME);
//            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            PendingIntent restartIntent = PendingIntent.getActivity(mContext,0,startMain,PendingIntent.FLAG_CANCEL_CURRENT);
//            AlarmManager mgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
//            long triggerAtTime = SystemClock.elapsedRealtime() +  1000;   //1秒
//            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)//1秒钟后重启应用
//            {
//                mgr.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,restartIntent);
////            }else if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP_MR1){}
//            }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
//                mgr.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,restartIntent);
//            }else{
//                mgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,restartIntent);
//            }
//            App.getContext().closeMainActivity();
//            System.exit(0);//退出程序

//            //-----------------多个Activity时的--------------------关闭程序 ----
//            Intent startMain = new Intent(Intent.ACTION_MAIN);
//            startMain.addCategory(Intent.CATEGORY_HOME);
//            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            mContext.startActivity(startMain);
//            App.getContext().closeMainActivity();
//            System.exit(0);//退出程序

            //使用Service定时启动app
            App.getContext().startRestartTask();
        }
    }

    /**
     * 自定义错误处理，收集错误信息，发送错误报告等操作均在此完成.
     * @param ex
     * @return true 如果处理了该异常信息；否则返回false.
     */
    private boolean handleException(Throwable ex){
        Log.i("dan.y","--------------------handleException---------------------");
        if(ex == null){
            return false;
        }
        AppCookie.getInstance().putCrashLog(true);// 每次进入应用检查，是否有log，有则上传
        //使用Toast来显示异常信息
        new Thread(){
            @Override
            public void run() {
                Looper.prepare();
                Toast.makeText(mContext,"很抱歉,程序出现异常,正在收集日志，即将退出", Toast.LENGTH_LONG).show();//并重启
                Looper.loop();
            }
        }.start();
        //收集设备参数信息
        collectDeviceInfo(mContext);
        //保存日志文件
        String fileName = saveCrashInfo2File(ex);
        return true;
    }

    public void collectDeviceInfo(Context context){
        infos.put("platform","android");
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(),PackageManager.GET_ACTIVITIES);
            if(pi != null){
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = pi.versionCode + "";
                infos.put("versionName",versionName);
                infos.put("versionCode",versionCode);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "an error occured when collect package info", e);
        }
        Field[] fields = Build.class.getDeclaredFields();//获取关于产品的参数，例如固件版本号，product 名字，板子名字等等
        for (Field field : fields){
            field.setAccessible(true);
            try {
                infos.put(field.getName(),field.get(null).toString());
                Log.d(TAG, field.getName() + " : " + field.get(null));
            } catch (IllegalAccessException e) {
                Log.e(TAG, "an error occured when collect crash info", e);
            }
        }
    }

    /**
     * 保存错误信息到文件中
     * @param ex
     * @return 返回文件名称,便于将文件传送到服务器
     */
    private String saveCrashInfo2File(Throwable ex){
        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String,String> entry:infos.entrySet()){
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key + "=" + value +"\n");
        }

        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null){
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result);
        try {
            long timestamp = System.currentTimeMillis();
            String time = formatter.format(new Date());
            String fileName = nameString +"-"+time+"-"+timestamp+".log";
            if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                String path = CrashLogDir;
                File dir = new File(path);
                if(!dir.exists()){
                    dir.mkdirs();
                }
                FileOutputStream fos = new FileOutputStream(path + fileName);
                fos.write(sb.toString().getBytes());
                fos.close();
            }
            return fileName;
        } catch (Exception e) {
            Log.e(TAG, "an error occured while writing file...", e);
        }
        return null;
    }


}
