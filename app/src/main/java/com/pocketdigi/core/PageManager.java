package com.pocketdigi.core;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.pocketdigi.plib.core.PLog;
import com.pocketdigi.template.MainActivity;
import com.pocketdigi.template.R;


import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import dalvik.system.DexClassLoader;


/**
 * Page管理器,必须是单Activity
 * PageManager在整个Activity生命周期中只能实例化一次,在Activity的onDestory中销毁
 * 销毁后不允许再调任何方法
 */
public class PageManager {
    private static PageManager instance;
    private static FragmentManager fragmentManager;
    PageShowListener pageShowListener;
    HashMap<String, DexClassLoader> classLoaderHashMap;

    SFragment lastRootPage, currentPage;
    //提交的id,pop时用
    List<Integer> commitIds;

    private PageManager() {
        fragmentManager = MainActivity.getInstance().getSupportFragmentManager();
        classLoaderHashMap = new HashMap<>();
        commitIds = new ArrayList<>();
    }

    /**
     * 创建新实例，只能且必须在Activity的onCreate方法里调用
     *
     * @return
     */
    public static PageManager newInstance() {
        if (instance == null)
            instance = new PageManager();
        return instance;
    }

    /**
     * 返回单例PageManager，如果Activity已经销毁，返回null
     *
     * @return
     */
    public static PageManager getInstance() {
        return instance;
    }

    /**
     * 显示页面,
     * 如果栈里有同页面，从栈里找，没有就new
     *
     * @param pageClass RFragment类
     */
    public void showPage(Class<? extends SFragment> pageClass) {
        showPage(pageClass, 0, null);
    }

    /**
     * 显示页面,
     * 如果栈里有同页面，从栈里找，没有就new
     *
     * @param pageClass RFragment类
     * @param args      在栈里没有该页面实例时，会调用setArguments传给Fragment,如果有实例，
     *                  会调用onResult，Fragment需要覆盖该方法实现相关操作
     */
    public void showPage(Class<? extends SFragment> pageClass, Bundle args) {
        showPage(pageClass, 0, args);
    }

    /**
     * 显示页面,
     * 如果栈里有同页面，从栈里找，没有就new
     * <p/>
     * 不存在以下情况:
     * 1、从非RootPage跳到RootPage(此时应该用back()方法)
     *
     * @param pageClass  RFragment类
     * @param args       在栈里没有该页面实例时，会调用setArguments传给Fragment,如果有实例，
     *                   会调用onResult，Fragment需要覆盖该方法实现相关操作
     * @param resultCode 标记参数来源用
     */
    public void showPage(Class<? extends SFragment> pageClass, int resultCode, Bundle args) {
        SFragment targetFragment = (SFragment) fragmentManager.findFragmentByTag(pageClass.getName());
        if (targetFragment != null) {
            //如果在历史栈中，弹出
            targetFragment.onResult(resultCode, args);
            //判断是否root间切换
            SFragment currentFragment = getCurrentFragment();
            //如果跳到当前页面，不处理
            if (targetFragment == currentFragment)
                return;
            if (currentFragment != null && (currentFragment.isRootPage() && targetFragment.isRootPage())) {
                //在rootpage间切换，用隐藏旧的，显示新的
                showRootPage(targetFragment);
            } else if (currentFragment != null && targetFragment.isRootPage()) {
                //从二级页面跳到RootPage,禁止这么调
                PLog.e(this, "请使用back方法返回到一级页面" + currentFragment.getClass().getName());
//                throw new IllegalAccessError();
                back();
                return;

            } else {
                //从一级页面，跳到二级页面，没有这个可能。因为二级页面不可能在栈中
                //只可能是N级跳N-1级，并且N>2
                fragmentManager.popBackStack(pageClass.getName(), 0);
            }
            currentPage = targetFragment;
        } else {
            //显示新的页面
            try {
                PLog.d(this, "显示新页面" + pageClass.getName());
                SFragment fragment = pageClass.newInstance();
                if (args != null)
                    fragment.setArguments(args);
                showNewPage(fragment);
                currentPage = fragment;
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        executePending();
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
    public void showDialog(SDialog dialog) {
        showDialog(dialog,null);
    }
    public void showDialog(SDialog dialog, Bundle args) {
        if (args != null) {
            dialog.setArgs(args);
        }
        dialog.show();
    }


    private void showNewPage(SFragment sFragment) {
        if (sFragment.isRootPage()) {
            showRootPage(sFragment);
            return;
        } else {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setCustomAnimations(R.anim.fragment_fade_in, R.anim.fragment_fade_out, R.anim.fragment_fade_in, R.anim.fragment_fade_out);

            transaction.add(R.id.container, sFragment, sFragment.getClass().getName());
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.addToBackStack(sFragment.getClass().getName());
            //如果只在一级页面才显示tab,非一级页面隐藏，根据自身情况调整
//            MainActivity.getInstance().hideTabBar();
            if (currentPage != null) {
                transaction.hide(currentPage);
            }
            commitIds.add(transaction.commitAllowingStateLoss());
        }
        if (pageShowListener != null)
            pageShowListener.onPageShow(sFragment);
    }


    /**
     * 返回上一个page
     *
     * @return
     */
    public boolean back() {
        //当前是否在第一级，第一级不能back，返回false
        int backStackEntryCount = fragmentManager.getBackStackEntryCount();
        if (!inFirstPage()) {
            FragmentManager.BackStackEntry backStackEntryAt = fragmentManager.getBackStackEntryAt(backStackEntryCount - 2);
            String targetClassName = backStackEntryAt.getName();
            //找到上一个Fragment,这里不一定是准确的，有可能不是上次显示的RootPage,
            //但如果这个Fragment是rootPage,那么pop后一定是rootPage,因为所有的RootPage平级
            SFragment fragmentByTag = (SFragment) fragmentManager.findFragmentByTag(targetClassName);
            if (fragmentByTag.isRootPage()) {
                //如果是一级页面才显示tab,非一级页面隐藏，此处要显示，根据自身情况调整
//                MainActivity.getInstance().showTabBar();
                currentPage = lastRootPage;
            } else {
                currentPage = fragmentByTag;
            }
            fragmentManager.popBackStack();
            if(commitIds.size()>=1) {
                commitIds.remove(commitIds.size() - 1);
            }
            executePending();
            return true;
        }
        return false;
    }

    /**
     * 跳回到指定级
     *
     * @param deep 跳回的深度,1为上一级，2为上二级
     *             @param args 带回的参数
     * @return
     */
    public boolean back(int deep,Bundle args) {
        int backStackEntryCount = fragmentManager.getBackStackEntryCount();
        if (deep >= backStackEntryCount)
            return false;
        FragmentManager.BackStackEntry backStackEntryAt = fragmentManager.getBackStackEntryAt(backStackEntryCount - 1- deep);
        String targetClassName = backStackEntryAt.getName();
        SFragment targetFragment = (SFragment) fragmentManager.findFragmentByTag(targetClassName);
        PLog.e(this,"targetFragment: "+targetFragment.getClass().getName());
        if (targetFragment.isRootPage()) {
            //如果是一级页面才显示tab,非一级页面隐藏，此处要显示，根据自身情况调整
//            MainActivity.getInstance().showTabBar();
             currentPage = lastRootPage;
        }else{
            currentPage = targetFragment;
        }
        if(args!=null){
            currentPage.onResult(0,args);
        }
        for(int i=0;i<deep;i++){
            fragmentManager.popBackStack();
            commitIds.remove(commitIds.size() - 1);
        }
        executePending();
        return true;
    }
    /**
     * 跳回到指定级
     *
     * @param deep 跳回的深度,1为上一级，2为上二级
     * @return
     */
    public boolean back(int deep){
        return back(deep,null);
    }

    /**
     * 返回到上一个Fragment,并带参数
     *
     * @param resultCode
     * @param args
     * @return
     */
    public boolean backWithResult(int resultCode, Bundle args) {
        //当前是否在第一级，第一级不能back，返回false
        int backStackEntryCount = fragmentManager.getBackStackEntryCount();
        if (!inFirstPage()) {
            FragmentManager.BackStackEntry backStackEntryAt = fragmentManager.getBackStackEntryAt(backStackEntryCount - 2);
            String targetClassName = backStackEntryAt.getName();
            //找到上一个Fragment,在上级是RootPage时，这里不一定是准确的，有可能不是上次显示的RootPage(因为在RootPage间切换是用hide/show的，不会压到backstack),
            //但如果这个Fragment是rootPage,那么pop后一定是rootPage,因为所有的RootPage平级
            SFragment fragmentByTag = (SFragment) fragmentManager.findFragmentByTag(targetClassName);
            fragmentManager.popBackStack();
            commitIds.remove(commitIds.size() - 1);
            if (fragmentByTag.isRootPage()) {
                //如果是一级页面才显示tab,非一级页面隐藏，此处要显示，根据自身情况调整
//                MainActivity.getInstance().showTabBar();
                lastRootPage.onResult(resultCode, args);
                currentPage = lastRootPage;
            } else {
                fragmentByTag.onResult(resultCode, args);
                currentPage = fragmentByTag;
            }
            executePending();

        }
        return false;


    }

    /**
     * 回到上一页，并刷新
     *
     * @return
     */
    public boolean backRefresh() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(SFragment.BUNDLE_KEY_REFRESH, true);
        return PageManager.getInstance().backWithResult(0, bundle);
    }

    public boolean backRefresh(int deep){
        Bundle bundle = new Bundle();
        bundle.putBoolean(SFragment.BUNDLE_KEY_REFRESH, true);
        return back(deep,bundle);

    }

    /**
     * 当前是否在第一级
     *
     * @return
     */
    public boolean inFirstPage() {
        boolean isFirstPage = fragmentManager.getBackStackEntryCount() == 1;
        if (!isFirstPage) {
            boolean inFirstLevel = true;
            List<Fragment> fragments = fragmentManager.getFragments();
            if (fragments != null) {
                for (Fragment fragment : fragments) {
                    if (fragment instanceof SFragment) {
                        SFragment sFragment = (SFragment) fragment;
                        if (!sFragment.isRootPage()) {
                            inFirstLevel = false;
                        }
                    }
                }
            } else {
                inFirstLevel = false;
            }
            isFirstPage = inFirstLevel;
        }
        return isFirstPage;
    }

    /**
     * 返回到顶,只有一个UserCenter
     *
     * @return
     */
    public boolean backToTop() {
        if (fragmentManager.getBackStackEntryCount() > 0) {
            try {
                if (commitIds.size() > 0) {
                    fragmentManager.popBackStackImmediate(commitIds.get(0), FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    commitIds.clear();
                    executePending();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
        currentPage = null;
        lastRootPage = null;
        return false;
    }

    /**
     * 在Activity的onDestory方法里调用
     */
    public void destory() {
        pageShowListener = null;
        fragmentManager=null;
        instance = null;

    }

    public void setPageShowListener(PageShowListener pageShowListener) {
        this.pageShowListener = pageShowListener;
    }

    /**
     * page显示调用
     */
    public interface PageShowListener {
        void onPageShow(SFragment sFragment);
    }

    /**
     * 加载补丁，返回对应的SFragment类
     *
     * @param pathPatch
     * @param patchClassName 要显示的SFragment完整类名
     * @return
     */
    private Class<? extends SFragment> loadPatch(String pathPatch, String patchClassName) {
        DexClassLoader dexClassLoader;
        if (classLoaderHashMap.containsKey(patchClassName)) {
            dexClassLoader = classLoaderHashMap.get(patchClassName);
        } else {
            final File optimizedDexOutputPath = new File(pathPatch);
            dexClassLoader = new DexClassLoader(optimizedDexOutputPath.getAbsolutePath(),
                    MainActivity.getInstance().getFilesDir().toString(), null, MainActivity.getInstance().getClassLoader());
            classLoaderHashMap.put(patchClassName, dexClassLoader);
        }
        try {
            return (Class<? extends SFragment>) dexClassLoader.loadClass(patchClassName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private SFragment getCurrentFragment() {
        int backStackEntryCount = fragmentManager.getBackStackEntryCount();
        if (backStackEntryCount > 0) {
            FragmentManager.BackStackEntry currentBackStackEntry = fragmentManager.getBackStackEntryAt(backStackEntryCount - 1);
            SFragment fragmentByTag = (SFragment) fragmentManager.findFragmentByTag(currentBackStackEntry.getName());
            if (fragmentByTag.isRootPage()) {
                return lastRootPage;
            }
            return fragmentByTag;
        }
        return null;
    }

    /**
     * 显示 RootPage
     *
     * @param rootFragment
     */
    private void showRootPage(SFragment rootFragment) {
        //如果是一级页面才显示tab,非一级页面隐藏，此处要显示，根据自身情况调整
//                MainActivity.getInstance().showTabBar();
        List<Fragment> fragments = fragmentManager.getFragments();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        boolean isRootFragmentInStack = false;
        if(fragments!=null) {
            for (Fragment fragment : fragments) {
                if (fragment != null) {
                    SFragment sFragment = (SFragment) fragment;
                    if (sFragment.isRootPage()) {
                        transaction.hide(sFragment);
                    }
                    //如果在栈中
                    if (rootFragment == sFragment) {
                        isRootFragmentInStack = true;
                    }
                }
            }
        }
        if (isRootFragmentInStack) {
            transaction.show(rootFragment);
        } else {
            transaction.add(R.id.container, rootFragment, rootFragment.getClass().getName());
            transaction.addToBackStack(rootFragment.getClass().getName());
        }
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        commitIds.add(transaction.commitAllowingStateLoss());
        lastRootPage = rootFragment;

    }

    /**
     * 上一页是否是rootPage
     *
     * @return
     */
    public boolean isLastPageRootPage() {
        int backStackEntryCount = fragmentManager.getBackStackEntryCount();
        if (backStackEntryCount > 1) {
            FragmentManager.BackStackEntry currentBackStackEntry = fragmentManager.getBackStackEntryAt(backStackEntryCount - 2);
            return ((SFragment) fragmentManager.findFragmentByTag(currentBackStackEntry.getName())).isRootPage();
        }
        return false;
    }

    /**
     * 执行未执行的操作
     */
    private void executePending() {
        Class<? extends FragmentManager> fragmentManagerClass = fragmentManager.getClass();
        try {
            Field mExecutingActionsField = fragmentManagerClass.getDeclaredField("mExecutingActions");
            mExecutingActionsField.setAccessible(true);
            boolean mExecutingActions = mExecutingActionsField.getBoolean(fragmentManager);
            if(!mExecutingActions)
                fragmentManager.executePendingTransactions();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}