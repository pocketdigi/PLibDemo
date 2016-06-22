package com.pocketdigi.template.page2;

import android.os.Bundle;

import com.pocketdigi.core.PageManager;
import com.pocketdigi.core.SFragment;
import com.pocketdigi.template.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.api.bundle.BundleHelper;

/**
 * Created by fhp on 16/5/9.
 */
@EFragment(R.layout.fragment_page2)
public class Page2Fragment extends SFragment {
    public static final String BUNDLE_KEY_ARG="arg";
    @AfterViews
    public void afterViews() {

    }

    @Click
    public void button() {
        Bundle bundle=new Bundle();
        bundle.putString(BUNDLE_KEY_ARG,"带回的参数");
        PageManager.getInstance().back(1,bundle);
    }

}
