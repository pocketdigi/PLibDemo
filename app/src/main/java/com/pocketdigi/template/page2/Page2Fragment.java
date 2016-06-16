package com.pocketdigi.template.page2;

import android.graphics.Color;
import android.os.Looper;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

import com.pocketdigi.core.SFragment;
import com.pocketdigi.plib.core.PLog;
import com.pocketdigi.plib.http.PRequest;
import com.pocketdigi.plib.http.PResponseListener;
import com.pocketdigi.template.R;
import com.pocketdigi.template.client.Client;
import com.pocketdigi.template.datamodel.BaiduImageResult;
import com.pocketdigi.template.page2.helper.RecyclerViewAdapter;
import com.pocketdigi.template.page2.helper.SpacesItemDecoration;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fhp on 16/5/9.
 */
@EFragment(R.layout.fragment_page2)
public class Page2Fragment extends SFragment implements SwipeRefreshLayout.OnRefreshListener {
    @ViewById
    SwipeRefreshLayout swipeRefreshLayout;
    @ViewById
    RecyclerView recyclerView;
    List<BaiduImageResult.Image> imageList;
    RecyclerViewAdapter recyclerViewAdapter;
    int page=1;
    int[] lastVisibleItems=new int[2];
    @AfterViews
    public void afterViews() {
        swipeRefreshLayout.setColorSchemeColors(Color.BLUE,Color.CYAN,Color.GREEN);
        imageList=new ArrayList<>();
        recyclerViewAdapter=new RecyclerViewAdapter(imageList);
        final StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        staggeredGridLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        recyclerView.setAdapter(recyclerViewAdapter);
        SpacesItemDecoration decoration=new SpacesItemDecoration(16);
        recyclerView.addItemDecoration(decoration);
        swipeRefreshLayout.setOnRefreshListener(this);
        getImageList();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView,
                                             int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                int index1=lastVisibleItems[0];
                int index2=lastVisibleItems[1];
                int max = Math.max(index1, index2);
                if (newState == RecyclerView.SCROLL_STATE_IDLE
                        && (max+ 1 == recyclerViewAdapter.getItemCount())){
                    swipeRefreshLayout.setRefreshing(true);
                    page++;
                    getImageList();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                staggeredGridLayoutManager.findLastVisibleItemPositions(lastVisibleItems);
            }
        });
    }

    /**
     * 读图
     */
    public void getImageList() {
        Client.getBaiduImageList(new PResponseListener<BaiduImageResult>() {
            @Override
            public void onResponse(PRequest request, BaiduImageResult response) {
                imageList.addAll(response.getImgs());
                recyclerViewAdapter.notifyDataSetChanged();
                int index1=lastVisibleItems[0];
                int index2=lastVisibleItems[1];
                int max = Math.max(index1, index2);
                recyclerViewAdapter.notifyItemRangeChanged(max+1,recyclerViewAdapter.getItemCount()-max);
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onError(PRequest request, Exception e) {

            }

        },page);

    }

    @Override
    public void onRefresh() {
        page=1;
        imageList.clear();
        recyclerViewAdapter.notifyDataSetChanged();
        getImageList();
    }
}
