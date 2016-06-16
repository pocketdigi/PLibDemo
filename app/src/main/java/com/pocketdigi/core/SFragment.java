package com.pocketdigi.core;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pocketdigi.plib.core.PFragment;
import com.pocketdigi.plib.core.PLog;
import com.pocketdigi.plib.util.DeviceUtils;
import com.umeng.analytics.MobclickAgent;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;


/**
 * Fragment
 * Created by fhp on 14-9-5.
 */
@EFragment
public abstract class SFragment extends PFragment {
    Bundle resultArgs;
    int resultCode;
    public static final String BUNDLE_KEY_REFRESH = "refresh_page";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(this.getClass().getSimpleName()); //统计页面
    }
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(this.getClass().getSimpleName());
    }

    /**
     * 当Fragment在栈中，从其他Fragment返回到该Fragment时，通过该方法传递参数，类同onActivityResult
     * 请在onReShow方法中取值
     * @param resultCode 可以通过resultCode判断数据来源
     * @param args 参数
     */
    public final void onResult(int resultCode,Bundle args){
        this.resultArgs=args;
        this.resultCode =resultCode;
    }

    @Override
    public void onDestroyView() {
        View view = getView();
        if(view!=null)
            DeviceUtils.hideSoftInput(view);
        super.onDestroyView();
        resultArgs=null;
        resultCode =0;
//        LoadingDialog.dismissDialog();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }
    /**
     * 设置topBar
     */
    @AfterViews
    public void setTopBar(){};

    /**
     * 获取返回时携带的参数
     * @return
     */
    public Bundle getResultArgs() {
        return resultArgs;
    }

    /**
     * 获取RequestCode
     * @return
     */
    public int getResultCode() {
        return resultCode;
    }

    /**
     * 是否一级页面，默认否
     * @return
     */
    public boolean isRootPage(){
        return false;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        PLog.d(this, hidden ? "隐藏" : "显示");
        if(hidden) {
            onHide();
        }else{
            onReShow();
        }
    }

    /**
     * 隐藏
     */
    protected void onHide() {
        unregisterListerOrReceiver();
        View view = getView();
        if(view!=null)
            DeviceUtils.hideSoftInput(view);
        resultArgs=null;
        resultCode =0;

    }

    /**
     * 重新显示
     */
    protected void onReShow(){
        registerListenerOrReceiver();
        Bundle resultArgs = getResultArgs();
        if(resultArgs!=null) {
            boolean refresh = resultArgs.getBoolean(BUNDLE_KEY_REFRESH, false);
            if(refresh) {
                //需要刷新
                needRefresh();
            }
        }
    }

    /**
     * 需要刷新,在上一页backRefresh时调用
     */
    protected void needRefresh() {
        PLog.d(this, "刷新页面");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        RefWatcher refWatcher = SApplication.getInstance().getRefWatcher();
//        refWatcher.watch(this);
    }
}