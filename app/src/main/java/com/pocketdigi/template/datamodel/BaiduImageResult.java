package com.pocketdigi.template.datamodel;

import java.util.List;

/**
 * Created by fhp on 16/5/17.
 */
public class BaiduImageResult {

    List<Image> imgs;

    public List<Image> getImgs() {
        return imgs;
    }

    public static class Image {
        String title, imageUrl,thumbnailUrl;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public String getThumbnailUrl() {
            return thumbnailUrl;
        }

        public void setThumbnailUrl(String thumbnailUrl) {
            this.thumbnailUrl = thumbnailUrl;
        }
    }
}
