package com.pocketdigi.template.imagelist;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import org.androidannotations.annotations.EFragment;

import com.pocketdigi.core.SFragment;
import com.pocketdigi.plib.http.PRequest;
import com.pocketdigi.plib.http.PResponseListener;
import com.pocketdigi.template.R;
import com.pocketdigi.template.client.Client;
import com.pocketdigi.template.datamodel.BaiduImageResult;
import com.pocketdigi.template.imagelist.helper.ImageListAdapter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Exception on 16/6/23.
 */
@EFragment(R.layout.fragment_image_list)
public class ImageListFragment extends SFragment implements PResponseListener<BaiduImageResult>{
    @ViewById
    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;
    ImageListAdapter imageListAdapter;
    List<BaiduImageResult.Image> imageList;
    int page=1;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        layoutManager=new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        imageList=new ArrayList<>();
        imageListAdapter=new ImageListAdapter(imageList);
    }

    @AfterViews
    public void afterViews() {
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(imageListAdapter);
        Client.getBaiduImageList(this,page);
    }

    @Override
    public void onResponse(PRequest request, BaiduImageResult response) {
        int sizeBeforeAdd = imageList.size();
        List<BaiduImageResult.Image> list = response.getImgs();
        imageList.addAll(list);
        imageListAdapter.notifyItemRangeChanged(sizeBeforeAdd, list.size());

    }

    @Override
    public void onError(PRequest request, Exception e) {

    }
}