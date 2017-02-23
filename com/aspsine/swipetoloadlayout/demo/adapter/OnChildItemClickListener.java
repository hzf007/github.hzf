package com.aspsine.swipetoloadlayout.demo.adapter;

import android.view.View;

/**
 * Created by aspsine on 16/8/9.
 * listview中item点击的的接口
 */

public interface OnChildItemClickListener<T> {
    void onChildItemClick(int groupPosition, int childPosition, T c, View view);
}
