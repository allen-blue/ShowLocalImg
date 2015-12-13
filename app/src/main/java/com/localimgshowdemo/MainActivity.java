package com.localimgshowdemo;

import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.library_lis.fragment.BaseListImgFragment;
import com.library_lis.fragment.DefaultListImgFragment;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements BaseListImgFragment.OnResultListener {
    private FragmentManager fm;
    private TextView tv;
    DefaultListImgFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = (TextView) findViewById(R.id.tv);
        fm = getSupportFragmentManager();
        fragment = DefaultListImgFragment.newInstance(null);
        fragment.setOnResultListener(this);
        fragment.setMaxSize(5);
        fragment.setIsPreView(false);
        fm.beginTransaction().replace(R.id.content, fragment).commit();
    }

    @Override
    public void onBackPressed() {
        if (fragment.isPopShow()) {
            fragment.hidePop();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onSelectImgs(ArrayList<String> result) {
        tv.setText(result.size() + " ");
    }
}
