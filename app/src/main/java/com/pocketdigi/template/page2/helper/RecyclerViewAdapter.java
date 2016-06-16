package com.pocketdigi.template.page2.helper;

import android.databinding.DataBindingUtil;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.common.logging.FLog;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.AbstractDraweeController;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.image.QualityInfo;
import com.pocketdigi.plib.core.PLog;
import com.pocketdigi.plib.view.CustomDraweeView;
import com.pocketdigi.template.R;
import com.pocketdigi.template.databinding.ItemRecyclerviewBinding;
import com.pocketdigi.template.datamodel.BaiduImageResult;

import java.util.List;

/**
 * Created by fhp on 16/5/17.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ImageViewHolder>{
    List<BaiduImageResult.Image> imageList;

    public RecyclerViewAdapter(List<BaiduImageResult.Image> imageList) {
        this.imageList = imageList;
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemRecyclerviewBinding recyclerviewBinding= DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_recyclerview,parent,false);
        return new ImageViewHolder(recyclerviewBinding.getRoot());
    }

    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {
        BaiduImageResult.Image image = imageList.get(position);
        ItemRecyclerviewBinding recyclerviewBinding = DataBindingUtil.bind(holder.view);
        recyclerviewBinding.setImage(image);
//        recyclerviewBinding.draweeView.setImageResource();
        if(image.getImageUrl()!=null) {
            AbstractDraweeController controller = Fresco.newDraweeControllerBuilder().setControllerListener(new ControllerListener(recyclerviewBinding.draweeView))
                    .setUri(Uri.parse(image.getImageUrl())).build();
            recyclerviewBinding.draweeView.setController(controller);
            recyclerviewBinding.draweeView.setTag(controller.getId());

        }

    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder{
        View  view;

        public ImageViewHolder(View itemView) {
            super(itemView);
            this.view=itemView;
        }
    }

    public static class ControllerListener extends BaseControllerListener<ImageInfo> {
        CustomDraweeView draweeView;

        public ControllerListener(CustomDraweeView draweeView) {
            this.draweeView = draweeView;
        }

        @Override
        public void onFinalImageSet(
                String id,
                @Nullable ImageInfo imageInfo,
                @Nullable Animatable anim) {
            if (imageInfo == null) {
                return;
            }
            QualityInfo qualityInfo = imageInfo.getQualityInfo();
            FLog.d("Final image received! " +
                            "Size %d x %d",
                    "Quality level %d, good enough: %s, full quality: %s",
                    imageInfo.getWidth(),
                    imageInfo.getHeight(),
                    qualityInfo.getQuality(),
                    qualityInfo.isOfGoodEnoughQuality(),
                    qualityInfo.isOfFullQuality());
            if(draweeView!=null&&draweeView.getTag().toString().equals(id)) {
                draweeView.setAspectRatio(imageInfo.getWidth()/(float)imageInfo.getHeight());
            }
            draweeView=null;
        }

        @Override
        public void onIntermediateImageSet(String id, @Nullable ImageInfo imageInfo) {
            PLog.d(this,"Intermediate image received");
        }

        @Override
        public void onFailure(String id, Throwable throwable) {
            FLog.e(getClass(), throwable, "Error loading %s", id);
            draweeView=null;
        }
    }


}
