package com.pocketdigi.template.page1;

import android.os.Bundle;

import com.pocketdigi.core.PageManager;
import com.pocketdigi.core.SFragment;
import com.pocketdigi.plib.core.PLog;
import com.pocketdigi.template.R;
import com.pocketdigi.template.http.HttpDemoFragment_;
import com.pocketdigi.template.page2.Page2Fragment;
import com.pocketdigi.template.page2.Page2Fragment_;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;

/**
 * 第一个页面
 */
@EFragment(R.layout.fragment_page1)
public class Page1Fragment extends SFragment {

    @Click
    public void btn() {
        PageManager.getInstance().showPage(Page2Fragment_.class);
    }


    @Click
    public void btn4() {
        PageManager.getInstance().showPage(HttpDemoFragment_.class);
    }


    @Override
    protected void onReShow() {
        super.onReShow();
        Bundle resultArgs = getResultArgs();
        if(resultArgs!=null) {
            PLog.d(this, resultArgs.getString(Page2Fragment.BUNDLE_KEY_ARG));
        }
    }

    @Override
    public boolean isRootPage() {
        return true;
    }

}
