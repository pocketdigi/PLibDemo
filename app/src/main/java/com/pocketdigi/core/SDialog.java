package com.pocketdigi.core;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import com.pocketdigi.template.MainActivity;
import com.pocketdigi.template.MyApplication;
import com.pocketdigi.template.R;


/**
 * Created by fhp on 15/1/29.
 */
public abstract class SDialog extends Dialog {
    public SDialog(Context context) {
        super(context, R.style.DefaultDialog);
    }

    public SDialog(Context context, int theme) {
        super(context, theme);
    }

    /**
     * 设置参数
     * @param bundle
     */
    public void setArgs(Bundle bundle){

    }

    @Override
    public void show() {
        super.show();
        MyApplication.getInstance().registerEventSubscriber(this);
    }

    @Override
    public void dismiss() {
        if(MainActivity.getInstance()!=null) {
            super.dismiss();
            MyApplication.getInstance().unregisterEventSubscriber(this);
        }
    }

    public void onEvent(DialogCloseEvent event) {
        dismiss();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();

    }
}
