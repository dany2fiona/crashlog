package com.example.dany.crashlogcatchdemo;

import android.content.Context;
import android.content.SharedPreferences;


/**
 * Created by dan.y on 2016/3/29.
 */

public class AppCookie {
    private SharedPreferences cookie;

    private final String Tag_crashLog = "tag_crashlog";

    private SharedPreferences.Editor _editor;

    public AppCookie(SharedPreferences cookie){
        this.cookie = cookie;
    }

    public AppCookie getEditor(){
        if (this._editor == null){
            this._editor = cookie.edit();
        }
        return this;
    }

    public void commit(){
        this._editor.commit();
        this._editor = null;
    }

    public void putCrashLog(boolean crashLog){
        this.getEditor();
            this._editor.putBoolean(Tag_crashLog, crashLog);
            commit();
    }

    public boolean getCrashLog(){
        Boolean crashLog = cookie.getBoolean(Tag_crashLog, false);
        return crashLog;
    }

    /*
	 * Singleton Implementation
	 */
    public static AppCookie getInstance()
    {
        return Singleton.Instance;
    }

    private static class Singleton
    {
        static final AppCookie Instance = new AppCookie(App.getContext().getSharedPreferences("app_cookie", Context.MODE_PRIVATE));

        private Singleton(){
        }
    }

}
