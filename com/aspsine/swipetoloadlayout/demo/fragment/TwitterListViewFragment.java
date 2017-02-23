package com.aspsine.swipetoloadlayout.demo.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.GsonRequest;
import com.aspsine.swipetoloadlayout.OnLoadMoreListener;
import com.aspsine.swipetoloadlayout.OnRefreshListener;
import com.aspsine.swipetoloadlayout.SwipeToLoadLayout;
import com.aspsine.swipetoloadlayout.demo.App;
import com.aspsine.swipetoloadlayout.demo.Constants;
import com.aspsine.swipetoloadlayout.demo.R;
import com.aspsine.swipetoloadlayout.demo.adapter.LoopViewPagerAdapter;
import com.aspsine.swipetoloadlayout.demo.adapter.OnChildItemClickListener;
import com.aspsine.swipetoloadlayout.demo.adapter.OnChildItemLongClickListener;
import com.aspsine.swipetoloadlayout.demo.adapter.SectionAdapter;
import com.aspsine.swipetoloadlayout.demo.model.Character;
import com.aspsine.swipetoloadlayout.demo.model.SectionCharacters;

/**
 * A simple {@link Fragment} subclass.
 * 带有listview的
 */
public class TwitterListViewFragment extends Fragment implements OnRefreshListener, OnLoadMoreListener,
        OnChildItemClickListener<Character>,
        OnChildItemLongClickListener<Character> {
    //得到类名
    public static final String TAG = TwitterListViewFragment.class.getSimpleName();
    //整个下拉刷新上拉加载的容器布局
    private SwipeToLoadLayout swipeToLoadLayout;

    private ListView listView;

    private ViewPager viewPager;
    //点点
    private ViewGroup indicators;
    //listview的adapter
    private SectionAdapter mAdapter;
    //viewpager的adapter
    private LoopViewPagerAdapter mPagerAdapter;

    public TwitterListViewFragment() {
        // Required empty public constructor
    }
    //fragment创建
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new SectionAdapter();
        mAdapter.setOnChildItemClickListener(this);
        mAdapter.setOnChildItemLongClickListener(this);
    }
    //fragment视图创建
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_twitter_listview, container, false);
    }
    //视图创建完成
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //作为listview的头部
        View pagerView = LayoutInflater.from(view.getContext()).inflate(R.layout.layout_viewpager, listView, false);
        swipeToLoadLayout = (SwipeToLoadLayout) view.findViewById(R.id.swipeToLoadLayout);
        listView = (ListView) view.findViewById(R.id.swipe_target);
        viewPager = (ViewPager) pagerView.findViewById(R.id.viewPager);
        indicators = (ViewGroup) pagerView.findViewById(R.id.indicators);
        viewPager.addOnPageChangeListener(mPagerAdapter);
        listView.addHeaderView(pagerView);
        listView.setAdapter(mAdapter);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                //向下滑动
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    //看到的最后一个是view中最后一个，并且可以上下滑动
                    if (view.getLastVisiblePosition() == view.getCount() - 1 && !ViewCompat.canScrollVertically(view, 1)) {
                        swipeToLoadLayout.setLoadingMore(true);//加载更多
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem == 0) {
                    if (mPagerAdapter != null) {
                        mPagerAdapter.start();//开始轮转
                    }

                } else {
                    if (mPagerAdapter != null) {
                        mPagerAdapter.stop();//停止轮转
                    }
                }
            }
        });
        swipeToLoadLayout.setOnRefreshListener(this);
        swipeToLoadLayout.setOnLoadMoreListener(this);
    }
    //activity创建
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //延迟自动刷新
        swipeToLoadLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeToLoadLayout.setRefreshing(true);
            }
        });
    }
    //回复
    @Override
    public void onResume() {
        super.onResume();
        if (mPagerAdapter != null) {
            mPagerAdapter.start();
        }
    }
    //暂停
    @Override
    public void onPause() {
        super.onPause();
        App.getRequestQueue().cancelAll(TAG);
        //暂停所有加载
        if (swipeToLoadLayout.isRefreshing()) {
            swipeToLoadLayout.setRefreshing(false);
        }
        if (swipeToLoadLayout.isLoadingMore()) {
            swipeToLoadLayout.setLoadingMore(false);
        }
        if (mPagerAdapter != null) {
            mPagerAdapter.stop();
        }
    }
    //刷新
    @Override
    public void onRefresh() {
        //volley中的网络请求框架
        GsonRequest request = new GsonRequest<SectionCharacters>(Constants.API.CHARACTERS, SectionCharacters.class, new Response.Listener<SectionCharacters>() {
            @Override
            public void onResponse(SectionCharacters characters) {
                mAdapter.setList(characters.getSections());
                if (viewPager.getAdapter() == null) {
                    mPagerAdapter = new LoopViewPagerAdapter(viewPager, indicators);
                    viewPager.setAdapter(mPagerAdapter);
                    viewPager.addOnPageChangeListener(mPagerAdapter);
                    mPagerAdapter.setList(characters.getCharacters());
                    viewPager.setBackgroundDrawable(getResources().getDrawable(R.mipmap.bg_viewpager));
                } else {
                    mPagerAdapter = (LoopViewPagerAdapter) viewPager.getAdapter();
                    mPagerAdapter.setList(characters.getCharacters());
                }
                swipeToLoadLayout.setRefreshing(false);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                swipeToLoadLayout.setRefreshing(false);
                volleyError.printStackTrace();
            }
        });
        App.getRequestQueue().add(request).setTag(TAG);
    }
    //加载更过
    @Override
    public void onLoadMore() {
        swipeToLoadLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeToLoadLayout.setLoadingMore(false);
            }
        }, 1000);
    }
    //点击事件
    @Override
    public void onChildItemClick(int groupPosition, int childPosition, Character character, View view) {
        Toast.makeText(getActivity(), character.getName() + " Click", Toast.LENGTH_SHORT).show();
    }

    //长按事件
    @Override
    public boolean onClickItemLongClick(int groupPosition, int childPosition, Character character, View view) {
        Toast.makeText(getActivity(), character.getName() + " Long Click", Toast.LENGTH_SHORT).show();
        return true;
    }
}
