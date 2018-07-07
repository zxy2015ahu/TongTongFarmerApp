package com.tongtong.purchaser.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.mapapi.model.LatLng;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.adapter.PoiAdapter;
import com.tongtong.purchaser.bean.PoiBean;
import com.tongtong.purchaser.bean.SearchAddressInfo;
import com.tongtong.purchaser.utils.CommonUtils;
import com.tongtong.purchaser.utils.HttpTask;
import com.tongtong.purchaser.utils.ToastUtil;
import com.tongtong.purchaser.utils.UrlUtil;
import com.tongtong.purchaser.view.AutoListView;

import java.util.ArrayList;
import java.util.List;


/**
 * poi关键字搜索
 * Created by yufs on 2017/3/1.
 */

public class SearchAddressActivity extends BaseActivity implements AutoListView.OnRefreshListener, AutoListView.OnLoadListener,
        TextWatcher,HttpTask.HttpTaskHandler,View.OnClickListener{
    EditText et_search;
    LinearLayout ll_loading;
    AutoListView lv_list;
    TextView tv_no_data;
    private int currentPage = 0;

    private List<PoiBean> poiData = new ArrayList<>();
    private PoiAdapter mAdapter;
    private String mKeyWord;
    private View backBn;
    //private String city;
    private LatLng lp;
    private InputMethodManager imm;
    private SharedPreferences sp;
    private ImageButton delete;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_address);
        initView();
        setListener();
    }

    private void initView() {
        LatLng position = getIntent().getParcelableExtra("position");
        // city = getIntent().getStringExtra("city");
        lp = new LatLng(position.latitude, position.longitude);
        et_search= (EditText) findViewById(R.id.search);
        ll_loading= (LinearLayout) findViewById(R.id.ll_loading);
        lv_list= (AutoListView) findViewById(R.id.lv_list);
        tv_no_data= (TextView) findViewById(R.id.tv_no_data);
        delete=(ImageButton) findViewById(R.id.delete_image_btn);
        backBn=findViewById(R.id.back_bn);
        TextView titleText = (TextView) findViewById(R.id.title_text);
        titleText.setText("搜索地址");
        imm=(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        titleText.postDelayed(new Runnable() {
            @Override
            public void run() {
                imm.showSoftInput(et_search,InputMethodManager.SHOW_FORCED);
            }
        },500);
        sp=getSharedPreferences("location", Context.MODE_PRIVATE);
    }

    private void setListener() {
        mAdapter = new PoiAdapter(this, poiData);
        lv_list.setAdapter(mAdapter);
        lv_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PoiBean poiItem = (PoiBean)mAdapter.getItem(position-1);
                Intent intent = new Intent();
                SearchAddressInfo addressInfo = new SearchAddressInfo(poiItem.getTitleName(),poiItem.getSnippet(),true,poiItem.getPoint(),poiItem.getCityName());
                intent.putExtra("position", addressInfo);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        lv_list.setPageSize(10);
        backBn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        et_search.addTextChangedListener(this);
        delete.setOnClickListener(this);
        et_search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH){
                    search();
                }
                return false;
            }
        });
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.delete_image_btn){
            imm.showSoftInput(et_search,InputMethodManager.SHOW_FORCED);
            et_search.getText().clear();
            poiData.clear();
            mAdapter.notifyDataSetChanged();
            lv_list.onLoadComplete();
            taskFailed(0);
        }
    }

    public  void search() {
        if (TextUtils.isEmpty(mKeyWord)) {
            ToastUtil.showShortToast(this, "请输入您要查找的地点");
            return;
        }
        poiData.clear();
        currentPage=0;
        lv_list.setOnRefreshListener(this);
        lv_list.setOnLoadListener(this);
        ll_loading.setVisibility(View.VISIBLE);// 隐藏对话框
        lv_list.setVisibility(View.VISIBLE);
        tv_no_data.setVisibility(View.GONE);
        doSearchQuery(mKeyWord);
    }


    protected void doSearchQuery(String keyWord) {
        HttpTask httpTask=new HttpTask(this);
        httpTask.setTaskHandler(this);
        JsonObject dataJson = new JsonObject();
        dataJson.addProperty("region",sp.getString("city","全国"));
        dataJson.addProperty("page_num",currentPage);
        dataJson.addProperty("keyword",keyWord);
        httpTask.execute(UrlUtil.GET_SUGEESET_ADDRESS,dataJson.toString());
    }

    @Override
    public void taskStart(int code) {
        if(currentPage==0) {
            ll_loading.setVisibility(View.VISIBLE);// 显示进度框
            lv_list.setVisibility(View.GONE);
        }
    }

    @Override
    public void taskFailed(int code) {
        //没有结果
        ll_loading.setVisibility(View.GONE);// 隐藏对话框
        lv_list.setVisibility(View.GONE);
        tv_no_data.setVisibility(View.VISIBLE);
    }

    @Override
    public void taskSuccessful(String str, int code) {
        final JsonObject selectResultJson = new JsonParser().parse(str)
                .getAsJsonObject();
        int selectResultCode = selectResultJson.get("code").getAsInt();
        if(verification(selectResultCode)){
            if(selectResultJson.has("addressInfo")) {
                final JsonObject addressInfo = selectResultJson.get("addressInfo").getAsJsonObject();
                JsonArray results = addressInfo.get("results").getAsJsonArray();
                if (currentPage == 0 && results.size() > 0) {
                    ll_loading.setVisibility(View.GONE);// 显示进度框
                    lv_list.setVisibility(View.VISIBLE);
                    tv_no_data.setVisibility(View.GONE);
                }
                for (int i = 0; i < results.size(); i++) {
                    JsonObject jo = results.get(i).getAsJsonObject();
                    PoiBean bean = new PoiBean();
                    bean.setTitleName(jo.get("name").getAsString());
                    bean.setCityName(jo.has("city") ? jo.get("city").getAsString() : "");
                    bean.setAd(jo.has("address") ? jo.get("address").getAsString() : "");
                    bean.setSnippet(jo.has("address") ? jo.get("address").getAsString() : jo.get("name").getAsString());
                    if (jo.has("location")) {
                        JsonObject location = jo.get("location").getAsJsonObject();
                        LatLng point = new LatLng(location.get("lat").getAsDouble(), location.get("lng").getAsDouble());
                        bean.setPoint(point);
                    } else {
                        bean.setPoint(new LatLng(0, 0));
                    }
                    poiData.add(bean);
                }
                mAdapter.notifyDataSetChanged();
                lv_list.setResultSize(results.size());
                lv_list.onLoadComplete();
                imm.hideSoftInputFromWindow(et_search.getWindowToken(), 0);
            }else{
                mAdapter.notifyDataSetChanged();
                lv_list.onLoadComplete();
                taskFailed(0);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        imm.hideSoftInputFromWindow(et_search.getWindowToken(),0);
    }



    //刷新
    @Override
    public void onRefresh() {
        lv_list.onRefreshComplete();
    }
    //加载下一页数据
    @Override
    public void onLoad() {
        currentPage++;
        doSearchQuery(mKeyWord);
    }

    @Override
    public void afterTextChanged(Editable editable) {
        mKeyWord= CommonUtils.replaceBlank(editable.toString());
        if(mKeyWord.length()>0){
            delete.setVisibility(View.VISIBLE);
            search();
        }else{
            delete.setVisibility(View.GONE);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
