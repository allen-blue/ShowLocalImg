package com.library_lis.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.library_lis.Constant;
import com.library_lis.LocalImageLoader;
import com.library_lis.R;
import com.library_lis.entity.ImageItem;

import java.util.ArrayList;


public class ImageGridAdapter extends BaseAdapter {
    private final String ACTION = "com.library_loadinglocalimg.preview_ACTION";
    final String FILTER_COLOR = "#88000000";
    private int maxSize = 9;
    private ResultCallBack resultCallBack = null;
    // 选择后的图片的路径集合
    private ArrayList<String> selectPicPaths = new ArrayList<String>();
    private ArrayList<ImageItem> dataList;// 数据源
    private Context context;
    private RelativeLayout.LayoutParams lp = null;
    private static boolean isPreView;

    public void setMaxSelect(int maxSize) {
        this.maxSize = maxSize;
    }

    public static void setIsPreView(boolean flag) {
        isPreView = flag;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public interface ResultCallBack {
        public void onListen(ArrayList<String> result);
    }

    public ArrayList<String> getSelectResults() {
        return selectPicPaths;
    }

    public void setSelectResultsAndRefresh(ArrayList<String> result) {
        this.selectPicPaths.addAll(result);
        refresh();
    }

    public void setResultCallBack(ResultCallBack resultCallBack) {
        this.resultCallBack = resultCallBack;
    }


    public ImageGridAdapter(Context context, ArrayList<ImageItem> dataList, int imgSize) {
        this.dataList = dataList;
        this.context = context;
        lp = new RelativeLayout.LayoutParams(imgSize, imgSize);
    }

    public void refresh() {
        initDataList();
        notifyDataSetChanged();
    }

    //TODO 优化
    private void initDataList() {
        int size = selectPicPaths.size();
        for (ImageItem img : dataList) {
            for (int i = 0; i < size; i++) {
                if ((img.imagePath != null && img.imagePath
                        .equals(selectPicPaths.get(i)))) {
                    img.isSelected = true;
                    break;
                }
            }
        }
    }

    @Override
    public int getCount() {
        return dataList == null ? 0 : dataList.size();
    }

    @Override
    public ImageItem getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Holder holder;
        final ImageItem item = dataList.get(position);
        if (convertView == null) {
            holder = new Holder();
            convertView = LayoutInflater.from(context).inflate(
                    R.layout.gridview_select_pic_item, parent, false);
            holder.iv = (ImageView) convertView
                    .findViewById(R.id.select_pic_item_iv_img);
            holder.selected = (ImageView) convertView
                    .findViewById(R.id.select_pic_item_iv_check);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }
        holder.iv.setLayoutParams(lp);
        if (maxSize == 1) {
            holder.selected.setVisibility(View.INVISIBLE);
        } else {
            holder.selected.setVisibility(View.VISIBLE);
            if (item.isSelected) {
                holder.selected.setImageResource(R.drawable.ic_select_yes);
                //设置选中变暗
                holder.iv.setColorFilter(Color.parseColor(FILTER_COLOR));
            } else {
                holder.selected.setImageResource(R.drawable.ic_select_no);
                holder.iv.setColorFilter(null);
            }
        }
        holder.iv.setImageResource(R.drawable.default_img);
        LocalImageLoader.getInstance().loadImage(item.imagePath, holder.iv);
        setClickListener(holder, item, position);
        return convertView;
    }

    private void setClickListener(final Holder holder, final ImageItem item, final int position) {
        holder.selected.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                doSelectClicked(item, holder);
            }
        });
        holder.iv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPreView) {
                    doPreViewImgClicked(position);
                } else {
                    doSelectClicked(item, holder);
                }
            }
        });

    }

    private void doPreViewImgClicked(int position) {
        Intent intent = new Intent(ACTION);
        intent.putExtra(Constant.PRE_IMG_START_POSITION, position);
        intent.putStringArrayListExtra(Constant.PRE_SELECT_IMG_DATAS, selectPicPaths);
        ArrayList<String> preListData = new ArrayList<String>();
        for (int i = 0; i < dataList.size(); i++) {
            preListData.add(dataList.get(i).imagePath);
        }
        intent.putStringArrayListExtra(Constant.PRE_IMG_DATAS, preListData);
        context.startActivity(intent);
    }

    private void doSelectClicked(ImageItem item, Holder holder) {
        if (item.isSelected) {
            holder.selected.setImageResource(R.drawable.ic_select_no);
            selectPicPaths.remove(item.imagePath);
            holder.iv.setColorFilter(null);
            item.isSelected = false;
        } else {
            if (selectPicPaths.size() >= maxSize) {
                Toast.makeText(context, "最多选择" + maxSize + "张图片", Toast.LENGTH_SHORT).show();
            } else {
                holder.selected.setImageResource(R.drawable.ic_select_yes);
                holder.iv.setColorFilter(Color.parseColor(FILTER_COLOR));
                selectPicPaths.add(item.imagePath);
                item.isSelected = true;
            }
        }
        if (resultCallBack != null)
            resultCallBack.onListen(selectPicPaths);
    }

    final static class Holder {
        private ImageView iv;
        private ImageView selected;
    }

}
