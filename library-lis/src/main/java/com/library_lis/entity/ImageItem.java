package com.library_lis.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 图片实体类
 */
public class ImageItem implements Parcelable {
    public String imageId;
    public String imagePath;//原图的路径
    public boolean isSelected;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(imageId);
        dest.writeString(imagePath);
        dest.writeByte((byte) (isSelected ? 1 : 0));
    }

    public static final Parcelable.Creator<ImageItem> CREATOR = new Parcelable.Creator<ImageItem>() {
        @Override
        public ImageItem createFromParcel(Parcel source) {
            ImageItem item = new ImageItem();
            item.isSelected = (source.readByte() != 0);
            item.imagePath = source.readString();
            item.imageId = source.readString();
            return item;
        }

        @Override
        public ImageItem[] newArray(int size) {
            return new ImageItem[size];
        }
    };
}
