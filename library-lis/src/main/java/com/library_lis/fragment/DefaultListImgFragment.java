package com.library_lis.fragment;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.library_lis.R;

import java.util.ArrayList;

/**
 * Created by Administrator on 2015/12/12.
 */
public class DefaultListImgFragment extends BaseListImgFragment implements View.OnTouchListener, View.OnClickListener {
    private Button chooseBucketBtn;
    private ImageView bottomIcon;

    @Override
    public int getBottomLayoutId() {
        return R.layout.fragment_default_img;
    }

    @Override
    public void initChildView(View view, Bundle savedInstanceState) {
        chooseBucketBtn = (Button) view.findViewById(R.id.fragment_default_bucket_btn);
        bottomIcon = (ImageView) view.findViewById(R.id.fragment_default_bucket_icon);
        chooseBucketBtn.setText(R.string.all_item_bucket);
        chooseBucketBtn.setOnClickListener(this);
        chooseBucketBtn.setOnTouchListener(this);
    }

    @Override
    public void onBucketSelect(String bucketName) {
        chooseBucketBtn.setText(bucketName);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int id = v.getId();
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (id == R.id.fragment_default_bucket_btn) {
                chooseBucketBtn.setTextColor(getResources().getColor(com.library_lis.R.color.dark_gray));
                bottomIcon.setImageResource(com.library_lis.R.drawable.ic_bottom_mark_press);
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (id == R.id.fragment_default_bucket_btn) {
                bottomIcon.setImageResource(com.library_lis.R.drawable.ic_bottom_mark_normal);
                chooseBucketBtn.setTextColor(getResources().getColor(com.library_lis.R.color.gray));
            }
        }
        return false;
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fragment_default_bucket_btn) {
            if (isPopShow())
                hidePop();
            else
                showPop();
        }
    }

    public static DefaultListImgFragment newInstance(ArrayList<String> selectImgs) {
        DefaultListImgFragment fragment = new DefaultListImgFragment();
        initArguments(fragment, selectImgs);
        return fragment;
    }
}
