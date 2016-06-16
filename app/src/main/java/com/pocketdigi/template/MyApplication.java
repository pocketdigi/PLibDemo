package com.pocketdigi.template;

import com.pocketdigi.plib.core.PApplication;
import com.pocketdigi.plib.core.PLog;
import com.umeng.analytics.MobclickAgent;


public class MyApplication extends PApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        //设置默认时区，如果您的应用非为中国用户开发,请注释掉下面一行
//        TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));
        MobclickAgent.openActivityDurationTrack(false);
        PLog.DEBUG = BuildConfig.DEBUG;
    }


    public static MyApplication getInstance() {
        return (MyApplication) PApplication.getInstance();
    }


}
