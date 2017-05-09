package com.example.ofk.asdemo.activity;

import android.content.Context;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.ofk.asdemo.R;
import com.example.ofk.asdemo.bean.GirlBean;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.CropCircleTransformation;
import jp.wasabeef.glide.transformations.CropSquareTransformation;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements AbsListView.OnScrollListener {

    private static final String TAG = "MainActivity";
    private ListView mListview;
    private Context mContext;
    private List<GirlBean.ResultsBean> mList;
    private MyAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        initView();
        initData();
        //sendSyncRequest();
        sendAyncRequest();
        setListener();
    }

    private void setListener() {
        mListview.setOnScrollListener(MainActivity.this);
    }

    //异步网络请求
    private void sendAyncRequest() {
        String url = "http://gank.io/api/data/%E7%A6%8F%E5%88%A9/10/1";
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder().get().url(url).build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                Gson mGson = new Gson();
                GirlBean girlBean = mGson.fromJson(result, GirlBean.class);
                Log.d(TAG, "时间：" + girlBean.getResults().get(0).getPublishedAt() + "  url:" + girlBean.getResults().get(0).getUrl());
                mList.addAll(girlBean.getResults());
            }
        });
    }

    //加载更多
    private void loadMoreRes() {
        String url = "http://gank.io/api/data/%E7%A6%8F%E5%88%A9/10/" + (mList.size() / 10 + 1);
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().get().url(url).build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                Gson mGson = new Gson();
                GirlBean girlBean = mGson.fromJson(result, GirlBean.class);
                Log.d(TAG, "结果：" + girlBean.getResults().get(0).getUrl());
                mList.addAll(girlBean.getResults());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    //同步网络请求
    private void sendSyncRequest() {
        final String url = "http://gank.io/api/data/%E7%A6%8F%E5%88%A9/10/1";
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient mHttpClient = new OkHttpClient();
                Request mRequest = new Request.Builder().get().url(url).build();

                try {
                    Response mResponse = mHttpClient.newCall(mRequest).execute();
                    String jsonFile = mResponse.body().string();
                    Log.d(TAG, jsonFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    private void initData() {
        mList = new ArrayList<GirlBean.ResultsBean>();
        mAdapter = new MyAdapter(mContext, mList);
        mListview.setAdapter(mAdapter);
    }

    //初始化控件
    private void initView() {
        mListview = (ListView) findViewById(R.id.lv_mlist);
    }

    private class MyAdapter extends ArrayAdapter<GirlBean.ResultsBean> {

        public MyAdapter(Context context, List<GirlBean.ResultsBean> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder mViewHolder = null;

            if (convertView == null) {
                mViewHolder = new ViewHolder();
                convertView = View.inflate(mContext, R.layout.view_girl_item, null);
                mViewHolder.mIvGirl = (ImageView) convertView.findViewById(R.id.iv_girl);
                mViewHolder.mData = (TextView) convertView.findViewById(R.id.tv_date);
                convertView.setTag(mViewHolder);
            } else {
                mViewHolder = (ViewHolder) convertView.getTag();
            }

            GirlBean.ResultsBean resultsBean = mList.get(position);
            String publishedAt = resultsBean.getPublishedAt();
            String url = resultsBean.getUrl();
            // mViewHolder.mIvGirl.setImageURI(url);
            mViewHolder.mData.setText(publishedAt);
            Glide.with(mContext).load(url).bitmapTransform(new CropCircleTransformation(mContext)).into(mViewHolder.mIvGirl);
            return convertView;

        }
    }

    private static class ViewHolder {
        ImageView mIvGirl;
        TextView mData;
    }

    //ListView滚动监听
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_IDLE) {
            if (view.getLastVisiblePosition() == mList.size() - 1) {
                loadMoreRes();
                Log.d(TAG, "加载。。。");
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }

}
