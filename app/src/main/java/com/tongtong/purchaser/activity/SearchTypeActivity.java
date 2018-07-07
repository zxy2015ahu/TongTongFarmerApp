package com.tongtong.purchaser.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.githang.statusbar.StatusBarCompat;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.model.ProduceType;
import com.tongtong.purchaser.utils.HistoryHelper;
import com.tongtong.purchaser.utils.ProduceTypesHelper;
import com.tongtong.purchaser.view.EditTextWithDeleteButton;
import com.tongtong.purchaser.widget.AlertDialog;

import net.lucode.hackware.magicindicator.buildins.UIUtil;

import java.util.List;

import co.lujun.androidtagview.TagContainerLayout;
import co.lujun.androidtagview.TagView;

/**
 * Created by Administrator on 2018-04-30.
 */

public class SearchTypeActivity extends BaseActivity implements View.OnClickListener,
        TextWatcher,TextView.OnEditorActionListener,AdapterView.OnItemClickListener{
    private EditTextWithDeleteButton search;
    private InputMethodManager inputMethodManager;
    private TagContainerLayout history_tags,hot_search_tags;
    private View line,history,search_badge;
    private SharedPreferences sp;
    private ListView list;
    private MyAdapter adapter;
    private String[] hints;
    private static SearchTypeActivity instance;
    public static SearchTypeActivity getInstance(){
        return instance;
    }
    private static final int REQUEST_SEARCH=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance=this;
        setContentView(R.layout.search_type_layout);
        sp=getSharedPreferences("location", Context.MODE_PRIVATE);
        StatusBarCompat.setStatusBarColor(this, Color.WHITE,true);
        findViewById(R.id.back_bn).setOnClickListener(this);
        search=(EditTextWithDeleteButton) findViewById(R.id.search);
        findViewById(R.id.search_bn).setOnClickListener(this);
        findViewById(R.id.delete).setOnClickListener(this);
        inputMethodManager=(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        search.setOnEditorActionListener(this);
        search.addTextChangedListener(this);
        history_tags=(TagContainerLayout)findViewById(R.id.history_1);
        hot_search_tags=(TagContainerLayout)findViewById(R.id.hot_search_tags);
        history_tags.setBackgroundColor(ContextCompat.getColor(this,android.R.color.white));
        history_tags.setBorderColor(Color.TRANSPARENT);
        history_tags.setTagTextColor(ContextCompat.getColor(this,R.color.aliwx_common_text_color2));
        history_tags.setTagBorderColor(ContextCompat.getColor(this,R.color.line_color));
        history_tags.setTagBackgroundColor(ContextCompat.getColor(this,R.color.aliwx_white));
        history_tags.setTagBorderRadius(UIUtil.dip2px(this,5));

        hot_search_tags.setBackgroundColor(ContextCompat.getColor(this,android.R.color.white));
        hot_search_tags.setBorderColor(Color.TRANSPARENT);
        hot_search_tags.setTagTextColor(ContextCompat.getColor(this,R.color.aliwx_common_text_color2));
        hot_search_tags.setTagBorderColor(ContextCompat.getColor(this,R.color.line_color));
        hot_search_tags.setTagBackgroundColor(ContextCompat.getColor(this,R.color.aliwx_white));
        hot_search_tags.setTagBorderRadius(UIUtil.dip2px(this,5));
        hot_search_tags.setOnTagClickListener(new TagView.OnTagClickListener() {
            @Override
            public void onTagClick(int position, String text) {
                HistoryHelper.addSearchHistory(SearchTypeActivity.this,text);
                Intent intent=new Intent();
                intent.setClass(SearchTypeActivity.this,SearchResultActivity.class);
                intent.putExtra("keyword",text);
                startActivityForResult(intent,REQUEST_SEARCH);
            }
            @Override
            public void onTagLongClick(int position, String text) {

            }
            @Override
            public void onTagCrossClick(int position) {

            }
        });
        line=findViewById(R.id.line);
        history=findViewById(R.id.history);
        search_badge=findViewById(R.id.search_badge);
        list=(ListView) findViewById(R.id.list);
        list.setOnItemClickListener(this);
        adapter=new MyAdapter(this);
        list.setAdapter(adapter);
        String hot_search=sp.getString("hot_search","");
        if(!TextUtils.isEmpty(hot_search)){
            hints=hot_search.split(",");
            hot_search_tags.setTags(hints);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ProduceType produce=adapter.getItem(position);
        HistoryHelper.addSearchHistory(this,produce.getName());
        Intent intent=new Intent();
        intent.putExtra("produce",produce);
        intent.setClass(this,SearchResultActivity.class);
        startActivityForResult(intent,REQUEST_SEARCH);
    }
    @Override
    protected void onResume() {
        super.onResume();
        List<String> his_tags= HistoryHelper.getSearchHistory(this);
        if(his_tags.size()>0){
            line.setVisibility(View.VISIBLE);
            history.setVisibility(View.VISIBLE);
            history_tags.removeAllTags();
            history_tags.setTags(his_tags);
            history_tags.setOnTagClickListener(new TagView.OnTagClickListener() {
                @Override
                public void onTagClick(int position, String text) {
                    HistoryHelper.addSearchHistory(SearchTypeActivity.this,text);
                    Intent intent=new Intent();
                    intent.setClass(SearchTypeActivity.this,SearchResultActivity.class);
                    intent.putExtra("keyword",text);
                    startActivityForResult(intent,REQUEST_SEARCH);
                }
                @Override
                public void onTagLongClick(int position, String text) {

                }

                @Override
                public void onTagCrossClick(int position) {

                }
            });
        }else{
            line.setVisibility(View.GONE);
            history.setVisibility(View.GONE);
        }
        if(hints!=null&&hints.length>0){
            search.setHint(hints[(int)(Math.random()*hints.length)]);
        }
        search.postDelayed(new Runnable() {
            @Override
            public void run() {
                inputMethodManager.showSoftInput(search,InputMethodManager.SHOW_FORCED);
            }
        },200);
    }
    private class MyAdapter extends ArrayAdapter<ProduceType>{
        public MyAdapter(Context context){
            super(context,0);
        }
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if(convertView==null){
                convertView=getLayoutInflater().inflate(R.layout.simple_text_view,null);
            }
            TextView item_name=(TextView) convertView.findViewById(R.id.item_name);
            ProduceType produce=getItem(position);
            item_name.setText(TextUtils.isEmpty(produce.getParent_name())?produce.getName():(produce.getParent_name()+">"+produce.getName()));
            return convertView;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        inputMethodManager.hideSoftInputFromWindow(search.getWindowToken(),0);
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.back_bn){
            onBackPressed();
        }else if(v.getId()==R.id.search_bn){
            String keyword=search.getText().toString().trim();
            if(TextUtils.isEmpty(search.getText().toString().trim())){
                keyword=search.getHint().toString().trim();
            }
            HistoryHelper.addSearchHistory(this,keyword);
            Intent intent=new Intent();
            intent.setClass(this,SearchResultActivity.class);
            intent.putExtra("keyword",keyword);
            startActivityForResult(intent,REQUEST_SEARCH);
        }else if(v.getId()==R.id.delete){
            inputMethodManager.hideSoftInputFromWindow(search.getWindowToken(),0);
            AlertDialog dialog=new AlertDialog(this).builder();
            dialog.setTitle("提示");
            dialog.setMsg("确定清空搜索历史？");
            dialog.setNegativeButton("取消", new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            dialog.setPositiveButton("确定", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    HistoryHelper.deleteSearchHistory(SearchTypeActivity.this);
                    line.setVisibility(View.GONE);
                    history.setVisibility(View.GONE);
                }
            });
            dialog.show();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        String search_content=s.toString().trim();
        if(!TextUtils.isEmpty(search_content)){
            ProduceTypesHelper.getSearch(SearchTypeActivity.this, search_content, new ProduceTypesHelper.OnDataRecieveListener() {
                @Override
                public void onRecieveData(List<ProduceType> produceTypes) {
                    search_badge.setVisibility(View.GONE);
                    list.setVisibility(View.VISIBLE);
                    adapter.clear();
                    adapter.addAll(produceTypes);
                    adapter.notifyDataSetChanged();
                }
            });
        }else{
            search_badge.setVisibility(View.VISIBLE);
            list.setVisibility(View.GONE);
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH){
            String keyword=search.getText().toString().trim();
            if(TextUtils.isEmpty(search.getText().toString().trim())){
                keyword=search.getHint().toString().trim();
            }
            HistoryHelper.addSearchHistory(this,keyword);
            Intent intent=new Intent();
            intent.setClass(this,SearchResultActivity.class);
            intent.putExtra("keyword",keyword);
            startActivityForResult(intent,REQUEST_SEARCH);
            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_SEARCH&&resultCode==RESULT_OK){
            if(data!=null){
                String keyword=data.getStringExtra("keyword");
                search.setText(keyword);
                Selection.setSelection(search.getText(),search.getText().length());
            }
        }
    }
}
