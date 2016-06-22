package com.pocketdigi.core;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;


import com.pocketdigi.template.MainActivity;
import com.pocketdigi.template.R;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;


/**
 * Page管理器,必须是单Activity
 * PageManager在整个Activity生命周期中只能实例化一次,在Activity的onDestory中销毁
 * 销毁后不允许再调任何方法
 */
public class PageManager {
    private static PageManager instance;
    private static FragmentManager fragmentManager;
    ArrayList<SFragment> fragmentStack;
    //最后显示的根页面
    SFragment lastRootPage;

    PageShowListener pageShowListener;
    public static final String BUNDLE_REFRESH="refresh";

    private PageManager() {
        fragmentManager = MainActivity.getInstance().getSupportFragmentManager();
        fragmentStack = new ArrayList<>();
    }

    /**
     * 返回单例PageManager，如果Activity已经销毁，返回null
     *
     * @return
     */
    public static PageManager getInstance() {
        if (instance == null)
            instance = new PageManager();
        return instance;
    }

    /**
     * 显示页面,
     * 如果栈里有同页面，从栈里找，没有就new
     *
     * @param pageClass RFragment类
     */
    public SFragment showPage(Class<? extends SFragment> pageClass, int resultCode, Bundle args) {
        try {
            SFragment fragment = pageClass.newInstance();
            showPage(fragment, resultCode, args);
            return fragment;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public SFragment showPage(Class<? extends SFragment> pageClass) {
        return showPage(pageClass,0,null);
    }

    public void showPage(SFragment fragment, int resultCode, Bundle args) {
        //如果在根页面切换，隐藏所有的根页面,显示或添加指定页
        if (isOnRoot() && fragment.isRootPage()) {
            switchInRoot(fragment, resultCode, args);
        } else if (!isOnRoot() && fragment.isRootPage()) {
            //不在根页面，回到根页面，即回到前n页（根页面一定会在栈底，因为从非根页面跳根页面(new)，必须backToTop）
            //因为根页面可能有多个，且切换过
            //栈中最后的那个根页面不一定是最后显示的根页面
            //但应该避免调用非当前分支的根页面
            if (fragment != lastRootPage) {
                throw new IllegalArgumentException("禁止显示非当前分支的根页面");
            }
            int rootPageCount = getRootPageCount();
            int deep = fragmentStack.size() - rootPageCount;
            back(deep, resultCode, args);

        } else if (isOnRoot() && !fragment.isRootPage()) {
            //从根页面，跳到非根页面
            showNewNormalPage(fragment, args);
        } else {
            //不在根页面，也不是回到根页面，普通页面间跳转
            int index = fragmentStack.indexOf(fragment);
            if (index > -1) {
                //栈中存在，回到前n页
                int deep = fragmentStack.size() - 1 - index;
                back(deep, resultCode, args);
            } else {
                //不存在，开新页面
                showNewNormalPage(fragment, args);
            }
        }
        if (pageShowListener != null) {
            pageShowListener.onPageShow(fragment);
        }
    }

    /**
     * 根页面间切换
     * 与普通页面不同，根页面同一页面只允许一个实例，切换
     *
     * @param fragment
     */
    private void switchInRoot(SFragment fragment, int resultCode, Bundle args) {
        if (!fragment.isRootPage()) {
            throw new IllegalAccessError("禁止调用此方法显示非Root页面");
        }
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.fragment_fade_in, R.anim.fragment_fade_out, R.anim.fragment_fade_in, R.anim.fragment_fade_out);
        boolean fragmentInStack = false;
        for (SFragment sFragment : fragmentStack) {
            if (sFragment == fragment) {
                transaction.show(sFragment);
                fragmentInStack = true;
            } else {
                transaction.hide(sFragment);
            }
        }
        //如果fragment不在栈里，用add
        if (!fragmentInStack) {
            transaction.add(R.id.container, fragment, fragment.getClass().getName());
            fragmentStack.add(fragment);
        }
        lastRootPage = fragment;
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.addToBackStack(fragment.getClass().getName());
        transaction.commitAllowingStateLoss();
        fragment.onResult(resultCode, args);
    }

    /**
     * 显示栈中的页面，如果栈中没有该页面的实例，new
     *
     * @param pageClass
     * @param resultCode
     * @param args
     */
    public SFragment showPageInStack(Class<? extends SFragment> pageClass, int resultCode, Bundle args) {
        SFragment sFragment = null;
        for (SFragment fragment : fragmentStack) {
            if (fragment.getClass() == pageClass) {
                sFragment = fragment;
            }
        }
        if (sFragment == null) {
            showPage(pageClass, resultCode, args);
        } else {
            showPage(sFragment, resultCode, args);
        }
        return sFragment;
    }

    /**
     * 开新页面，普通页面，非root
     *
     * @param fragment
     * @param args
     */
    private void showNewNormalPage(SFragment fragment, Bundle args) {
        if (fragment.isRootPage())
            throw new IllegalArgumentException("禁止调用此方法显示非root页面");
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.fragment_fade_in, R.anim.fragment_fade_out, R.anim.fragment_fade_in, R.anim.fragment_fade_out);
        transaction.add(R.id.container, fragment, fragment.getClass().getName());
        //隐藏上一个页面
        if (lastRootPage != null && lastRootPage.isVisible()) {
            transaction.hide(lastRootPage);
        }
        //隐藏上一页
        SFragment currentFragment = getCurrentFragment();
        if(currentFragment!=null&&!currentFragment.isRootPage()){
            transaction.hide(currentFragment);
        }


        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.addToBackStack(fragment.getClass().getName());

        transaction.commitAllowingStateLoss();
        fragment.setArguments(args);
        fragmentStack.add(fragment);
    }


    /**
     * 是否在根页面
     *
     * @return
     */
    public boolean isOnRoot() {
        return getRootPageCount() == fragmentStack.size();
    }

    /**
     * 根页面数量
     *
     * @return
     */
    private int getRootPageCount() {
        int count = 0;
        for (SFragment fragment : fragmentStack) {
            if (fragment.isRootPage()) {
                count++;
            } else {
                break;
            }
        }
        return count;
    }

    /**
     * 回到前n页，带回参数 ，
     * 如果目标页是根页面，无法处理参数
     *
     * @param deep       跳回的深度,1为上一级，2为上二级
     * @param args       带回的参数
     * @param resultCode 返回结果码
     * @return
     */
    public boolean back(int deep, int resultCode, Bundle args) {
        int rootPageCount = getRootPageCount();
        if(rootPageCount==fragmentStack.size()){
            //在根页面点返回
            return false;
        }
        if (deep > fragmentStack.size() - rootPageCount) {
            throw new IllegalArgumentException("不允许跳到非当前分支的rootPage");
        }
        int targetIndex=fragmentStack.size()-1-deep;
        //targetIndex等于-1时为回到顶部，没有任何页面
        if(targetIndex<-1){
            throw new IllegalArgumentException("back太深了，deep="+deep);
        }
        if(targetIndex>=0){
            SFragment targetFragment=fragmentStack.get(targetIndex);
            targetFragment.onResult(resultCode,args);
            if (pageShowListener != null) {
                pageShowListener.onPageShow(targetFragment);
            }
        }
        for (int i = 0; i < deep; i++) {
            fragmentManager.popBackStack();
            fragmentStack.remove(fragmentStack.size() - 1);
        }
        return true;
    }

    private SFragment getCurrentFragment(){
        SFragment currentFragment=null;
        if(fragmentStack.size()>0){
            currentFragment = fragmentStack.get(fragmentStack.size() - 1);
        }
        return currentFragment;
    }

    /**
     * 跳回到指定级
     *
     * @param deep 跳回的深度,1为上一级，2为上二级
     * @return
     */
    public boolean back(int deep) {
        return back(deep, null);
    }

    public boolean back(int deep, Bundle args) {
        return back(deep, 0, args);
    }

    public boolean back() {
        return back(1);
    }


    /**
     * 回到上一页，并刷新
     *
     * @return
     */
    public boolean backRefresh() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(BUNDLE_REFRESH, true);
        return back(1, bundle);
    }

    public boolean backRefresh(int deep) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(BUNDLE_REFRESH, true);
        return back(deep, bundle);

    }

    /**
     * @return
     */
    public boolean backToTop() {
        back(fragmentStack.size());
        lastRootPage = null;
        return true;
    }

    /**
     * 当前是否在第一级
     *
     * @return
     */
    public boolean inFirstPage() {
        return fragmentStack.size() == 1;
    }


    /**
     * 显示Dialog,必须有一个context参数的构造方法
     *
     * @param dialogClass
     */
    public SDialog showDialog(Class<? extends SDialog> dialogClass) {
        return showDialog(dialogClass, null);
    }

    /**
     * 带参数显示Dialog,必须有一个context参数的构造方法
     *
     * @param dialogClass
     * @param args
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public SDialog showDialog(Class<? extends SDialog> dialogClass, Bundle args) {
        try {
            Class<?>[] argTypes = {Context.class}; //指明所要调用的构造方法的形参
            Constructor<?> constructor = dialogClass.getConstructor(argTypes);//获取指定参数的构造方法
            SDialog dialog = (SDialog) constructor.newInstance(MainActivity.getInstance());
            if (args != null) {
                dialog.setArgs(args);
            }
            dialog.show();
            return dialog;
        } catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;

    }

    public PageShowListener getPageShowListener() {
        return pageShowListener;
    }

    public void setPageShowListener(PageShowListener pageShowListener) {
        this.pageShowListener = pageShowListener;
    }

    /**
     * Activity销毁时调用
     */
    public void destory() {
        instance = null;
        fragmentStack.clear();
        lastRootPage = null;
    }


    /**
     * page显示调用
     */
    public interface PageShowListener {
        void onPageShow(SFragment sFragment);
    }

}