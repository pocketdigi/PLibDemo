package com.pocketdigi.template.imagelist.helper;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pocketdigi.template.R;
import com.pocketdigi.template.databinding.ItemImageListBinding;
import com.pocketdigi.template.datamodel.BaiduImageResult;

import java.util.List;

/**
 * Created by Exception on 16/6/23.
 */
public class ImageListAdapter extends RecyclerView.Adapter<ImageListAdapter.ViewHolder>{
    List<BaiduImageResult.Image> imageList;

    public ImageListAdapter(List<BaiduImageResult.Image> imageList) {
        this.imageList = imageList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemImageListBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_image_list, parent, false);
        View view = binding.getRoot();
        view.setTag(binding);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        View itemView = holder.itemView;
        ItemImageListBinding binding = (ItemImageListBinding)itemView.getTag();
        binding.setBaiduImage(imageList.get(position));
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
