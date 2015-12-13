package com.library_lis.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.library_lis.AlbumHelper;
import com.library_lis.LocalImageLoader;
import com.library_lis.R;
import com.library_lis.adapter.ImageBucketAdapter;
import com.library_lis.adapter.ImageGridAdapter;
import com.library_lis.entity.ImageBucket;
import com.library_lis.entity.ImageItem;

import java.util.ArrayList;

public abstract class BaseListImgFragment extends Fragment {

    public static final String SELECT_IMG_FLAG = "select_Imgs";//上次选择的图片，再次选择时，需要把上次选择的进行初始化（标记已选择）
    private int maxSize = 9;
    private ArrayList<ImageItem> dataList = new ArrayList<>();//本地图片的数据源
    private ArrayList<String> selectImgs = new ArrayList<>();//先前选中的图片
    private ImageBucket selectedBucket;//先去选择的文件夹
    private ArrayList<ImageBucket> mBucketLists; //所有图片文件夹

    private AlbumHelper helper;
    private int itemWidth;//图片宽度
    private Handler alphaHandler;
    private boolean isChangeAlpha;
    private int mAlpha = 0;
    private OnResultListener listener;


    private GridView gridView;
    private ImageGridAdapter adapter;
    private FrameLayout bottomLayout;

    //pop
    private LinearLayout popLayout;
    private ListView popListview;
    private Animation popAnim;
    private boolean isPopShow;

    public interface OnResultListener {
        void onSelectImgs(ArrayList<String> result);
    }

    public abstract int getBottomLayoutId();

    public abstract void initChildView(View view, Bundle savedInstanceState);

    public abstract void onBucketSelect(String bucketName);

    public void setOnResultListener(OnResultListener listener) {
        this.listener = listener;
    }

    public boolean isPopShow() {
        return isPopShow;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_base_listimg, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        selectImgs = getArguments().getStringArrayList(SELECT_IMG_FLAG);
        initView(getView());
        initChildView(getView(), savedInstanceState);
        setUpView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalImageLoader.getInstance().clearChache();
    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    popLayout.getBackground().setAlpha(0);
                    break;
                case 255:
                    popLayout.getBackground().setAlpha(255);
                    break;
                case 1:
                    popLayout.getBackground().setAlpha(mAlpha);
                    break;
            }
            return true;
        }
    });
    private Runnable upAlphaRunnable = new Runnable() {
        @Override
        public void run() {
            if (mAlpha > 255 || !isChangeAlpha) {
                mHandler.sendEmptyMessage(255);
                alphaHandler.removeCallbacks(upAlphaRunnable);
            } else {
                mHandler.sendEmptyMessage(1);
                alphaHandler.postDelayed(upAlphaRunnable, 15);
            }
            mAlpha += 8;
        }
    };

    private Runnable downAlphaRunnable = new Runnable() {
        @Override
        public void run() {
            if (mAlpha < 0 || !isChangeAlpha) {
                mHandler.sendEmptyMessage(0);
                alphaHandler.removeCallbacks(downAlphaRunnable);
            } else {
                mHandler.sendEmptyMessage(1);
                alphaHandler.postDelayed(downAlphaRunnable, 15);
            }
            mAlpha -= 8;
        }
    };

    //更改透明度
    private void changeAlpha(final boolean isIncrement) {
        if (!isChangeAlpha) {
            return;
        }
        final HandlerThread coverAlphaThread = new HandlerThread("changeAlphaThreadd");
        coverAlphaThread.start();
        alphaHandler = new Handler(coverAlphaThread.getLooper());
        if (!isIncrement) {
            mAlpha = 255;
            alphaHandler.post(downAlphaRunnable);
        } else {
            mAlpha = 0;
            alphaHandler.post(upAlphaRunnable);
        }
    }

    private void initView(View view) {
        itemWidth = getResources().getDisplayMetrics().widthPixels / 3;
        gridView = (GridView) view.findViewById(R.id.fragment_base_listimg_gv);
        gridView.setColumnWidth(itemWidth);
        popLayout = (LinearLayout) view.findViewById(R.id.layout_bucket_pop_container);
        popListview = (ListView) view.findViewById(R.id.layout_bucket_pop_listview);
        bottomLayout = (FrameLayout) view.findViewById(R.id.fragment_base_listimg_bottom);
        final View bottomView = LayoutInflater.from(getActivity()).inflate(getBottomLayoutId(), bottomLayout, false);
        bottomLayout.addView(bottomView, bottomView.getLayoutParams());
        popLayout.getBackground().setAlpha(0);
        popLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPopShow) {
                    hidePop();
                }
            }
        });
    }

    private void setUpView() {
        helper = AlbumHelper.getHelper();
        helper.init(getActivity());
        mBucketLists = helper.getImageBucketList();
        if (mBucketLists == null || mBucketLists.size() == 0) {
            Toast.makeText(getActivity(), "该终端不存在图片资源", Toast.LENGTH_SHORT).show();
            getActivity().finish();
            return;
        }
        initPop();
        dataList.clear();
        dataList.addAll(mBucketLists.get(0).imageList);
        adapter = new ImageGridAdapter(getActivity(), dataList, itemWidth);
        adapter.setMaxSelect(maxSize);
        gridView.setAdapter(adapter);
        LocalImageLoader.getInstance().setFlingStopLoading(gridView);
        if (selectImgs != null && selectImgs.size() > 0)
            adapter.setSelectResultsAndRefresh(selectImgs);
        adapter.setResultCallBack(new ImageGridAdapter.ResultCallBack() {
            @Override
            public void onListen(ArrayList<String> result) {
                if (listener != null)
                    listener.onSelectImgs(result);
            }
        });
    }

    private void changeBucket(ArrayList<ImageItem> imageList) {
        dataList.clear();
        dataList.addAll(imageList);
        gridView.smoothScrollToPosition(0);
        adapter.refresh();
    }

    public void setMaxSize(int maxSize) {
        if (maxSize <= 1) {
            this.maxSize = 1;
        } else {
            this.maxSize = maxSize;
        }
    }

    public void setIsPreView(boolean flag) {
        ImageGridAdapter.setIsPreView(flag);
    }

    public void showPop() {
        isPopShow = true;
        popAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.bucket_pop_in);
        popLayout.setVisibility(View.VISIBLE);
        popAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                isChangeAlpha = true;
                changeAlpha(true);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                isChangeAlpha = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        popListview.startAnimation(popAnim);
    }

    public void hidePop() {
        isPopShow = false;
        popAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.bucket_pop_out);
        popAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                isChangeAlpha = true;
                changeAlpha(false);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                isChangeAlpha = false;
                popLayout.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        popListview.startAnimation(popAnim);
    }

    private void initPop() {
        selectedBucket = mBucketLists.get(0);
        final ImageBucketAdapter bucketAdapter = new ImageBucketAdapter(getActivity(), mBucketLists);
        popListview.setAdapter(bucketAdapter);
        popListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final ImageBucket curBucket = mBucketLists.get(position);
                if (curBucket != selectedBucket) {
                    ImageView selectBucketFlagIv = (ImageView) view
                            .findViewById(R.id.img_bucket_item_choose_flag);
                    selectBucketFlagIv.setImageResource(R.drawable.ic_selected);
                    curBucket.isSelected = true;
                    selectedBucket.isSelected = false;
                    selectedBucket = curBucket;
                    onBucketSelect(curBucket.bucketName);
                    changeBucket(curBucket.imageList);
                    bucketAdapter.notifyDataSetChanged();
                }
                hidePop();
            }
        });
    }

    protected static void initArguments(Fragment fragment, ArrayList<String> hadSelectImgs) {
        Bundle bundle = new Bundle();
        if (hadSelectImgs != null) {
            bundle.putStringArrayList(SELECT_IMG_FLAG, hadSelectImgs);
        }
        fragment.setArguments(bundle);
    }
}
