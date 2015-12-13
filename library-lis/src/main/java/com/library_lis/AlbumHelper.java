package com.library_lis;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore.Images.Media;

import com.library_lis.entity.ImageBucket;
import com.library_lis.entity.ImageItem;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;


public class AlbumHelper {
    private String MY_BUCKET = "我的图片";
    private Context context;
    private ContentResolver cr;
    private static AlbumHelper instance;
    private boolean hasBuildBucketList = false;
    HashMap<String, ImageBucket> bucketMap = new HashMap<String, ImageBucket>();
    private ArrayList<ImageItem> imageList = new ArrayList<ImageItem>();

    public static AlbumHelper getHelper() {
        if (instance == null) {
            synchronized (AlbumHelper.class) {
                if (instance == null) {
                    instance = new AlbumHelper();
                }
            }
        }
        return instance;
    }

    public void init(Context context) {
        if (this.context == null)
            this.context = context;
        MY_BUCKET = context.getResources().getString(R.string.all_item_bucket);
        this.cr = context.getContentResolver();
    }


    /**
     * 获取sd卡中的图片，并创建图片文件夹的列表
     */
    private void buildImagesBucketList() {
        File file;
        String[] columns = {Media._ID, Media.BUCKET_ID, Media.DATA, Media.BUCKET_DISPLAY_NAME};
        Cursor cur = cr.query(Media.EXTERNAL_CONTENT_URI, columns, null, null,
                "_id DESC");
        if (cur.moveToFirst()) {
            int photoIdIndex = cur.getColumnIndexOrThrow(Media._ID);
            int photoPathIndex = cur.getColumnIndexOrThrow(Media.DATA);//图片路径字段
            int bucketDisplayNameIndex = cur.getColumnIndexOrThrow(Media.BUCKET_DISPLAY_NAME);
            int bucketIdIndex = cur.getColumnIndexOrThrow(Media.BUCKET_ID);
            do {
                String id = cur.getString(photoIdIndex);
                String path = cur.getString(photoPathIndex);
                String bucketId = cur.getString(bucketIdIndex);
                String bucketname = cur.getString(bucketDisplayNameIndex);
                file = new File(path);
                if (file.exists() && file.length() == 0)
                    continue;
                ImageItem i = new ImageItem();
                i.imageId = id;
                i.imagePath = path;
                imageList.add(i);
                ImageBucket bucket = null;
                if (this.bucketMap.containsKey(bucketId))
                    bucket = this.bucketMap.get(bucketId);
                else {
                    bucket = new ImageBucket();
                    bucketMap.put(bucketId, bucket);
                    bucket.bucketName = bucketname;
                    bucket.imageList = new ArrayList<ImageItem>();
                }
                bucket.count++;
                bucket.imageList.add(i);
            } while (cur.moveToNext());
        }
        cur.close();
    }

    /**
     * 获取图片的文件夹列表
     *
     * @param refresh
     * @return
     */
    public ArrayList<ImageBucket> getImageBucketList(boolean refresh) {
        if (refresh || (!refresh && !hasBuildBucketList)) {
            clear();
            buildImagesBucketList();
        }
        //声明所有文件夹列表集合
        ArrayList<ImageBucket> bucketList = new ArrayList<ImageBucket>();
        ImageBucket allBucket = new ImageBucket();
        allBucket.bucketName = MY_BUCKET;
        allBucket.imageList = imageList;
        allBucket.count = imageList.size();
        allBucket.isSelected = true;
        bucketList.add(allBucket);
        Iterator<Entry<String, ImageBucket>> itr = bucketMap.entrySet()
                .iterator();
        while (itr.hasNext()) {
            Entry<String, ImageBucket> entry = itr.next();
            bucketList.add(entry.getValue());
        }
        return bucketList;

    }

    public ArrayList<ImageItem> getAllImageList(boolean refresh) {
        if (refresh || (!refresh && !hasBuildBucketList)) {
            clear();
            buildImagesBucketList();
        }
        return imageList;
    }


    public void clear() {
        bucketMap.clear();
        imageList.clear();
    }

}
