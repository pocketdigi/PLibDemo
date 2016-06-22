package com.pocketdigi.template;

import android.os.Bundle;

import com.pocketdigi.core.PageManager;
import com.pocketdigi.core.SFragment;
import com.pocketdigi.plib.core.PFragmentActivity;
import com.pocketdigi.template.page1.Page1Fragment_;
import com.umeng.analytics.MobclickAgent;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;

@EActivity(R.layout.activity_main)
public class MainActivity extends PFragmentActivity implements PageManager.PageShowListener{
    private static MainActivity instance;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        PageManager.getInstance().setPageShowListener(this);
        MobclickAgent.setDebugMode(BuildConfig.DEBUG);
        //umeng
        MobclickAgent.UMAnalyticsConfig config=new MobclickAgent.UMAnalyticsConfig(this,"umengkey",BuildConfig.FLAVOR);
        MobclickAgent.startWithConfigure(config);

    }
    @AfterViews
    public void afterViews() {
        PageManager.getInstance().showPage(Page1Fragment_.class);
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (!backProcessed) {
            if (!PageManager.getInstance().back()) {
                if (PageManager.getInstance().isOnRoot()) {
                    //当前在根页面
                    finish();
                }
            }
        }
    }

    public static MainActivity getInstance() {
        return instance;
    }

    @Override
    public void onPageShow(SFragment sFragment) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }


    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PageManager.getInstance().destory();
        instance = null;
    }
}
