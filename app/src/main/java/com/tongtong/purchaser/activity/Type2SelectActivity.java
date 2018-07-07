package com.tongtong.purchaser.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.frament.Produce1Fragment;
import com.tongtong.purchaser.model.ProduceType;
import com.tongtong.purchaser.utils.ProduceTypesHelper;
import com.tongtong.purchaser.view.EditTextWithDeleteButton;

import net.lucode.hackware.magicindicator.buildins.UIUtil;
import java.util.ArrayList;
import java.util.List;
import co.lujun.androidtagview.TagContainerLayout;
import co.lujun.androidtagview.TagView;
/**
 * Created by Administrator on 2018-05-04.
 */

public class Type2SelectActivity extends BaseActivity implements View.OnClickListener,
        TextWatcher,AdapterView.OnItemClickListener{
    private TagContainerLayout tags;
    private View histoty_select;
    private ProduceType produce;
    private int p1,p2,p3;
    private EditTextWithDeleteButton search;
    private TextView tv_no_data;
    private ListView list;
    private View root,search_badge;
    private MyAdapter adapter;
    private InputMethodManager imm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.type2_select_layout);
        ((TextView)findViewById(R.id.title_text)).setText("选择分类");
        tags=(TagContainerLayout) findViewById(R.id.history_1);
        histoty_select=findViewById(R.id.histoty_select);
        tags.setBackgroundColor(ContextCompat.getColor(this,R.color.city_search_bg));
        tags.setBorderColor(Color.TRANSPARENT);
        imm=(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        tags.setTagTextColor(ContextCompat.getColor(this,R.color.aliwx_common_text_color2));
        tags.setTagBorderColor(ContextCompat.getColor(this,R.color.line_color));
        tags.setTagBackgroundColor(ContextCompat.getColor(this,R.color.aliwx_white));
        tags.setTagBorderRadius(UIUtil.dip2px(this,5));
        if(getIntent().hasExtra("produce")){
            produce=(ProduceType) getIntent().getSerializableExtra("produce");
            if(produce!=null) {
                if (produce.getLevel() == 2) {
                    p2 = produce.getId();
                    ProduceType parent = ProduceTypesHelper.getParentProduce(this, p2);
                    p1 = parent.getId();
                    p3=0;
                } else if (produce.getLevel() == 3) {
                    p3 = produce.getId();
                    ProduceType parent = ProduceTypesHelper.getParentProduce(this, p3);
                    p2 = parent.getId();
                    parent = ProduceTypesHelper.getParentProduce(this, p2);
                    p1 = parent.getId();
                }
            }else{
                p1=-1;
                p2=-1;
                p3=-1;
            }
        }
        ProduceTypesHelper.getHistoryProduce(this, new ProduceTypesHelper.OnDataRecieveListener() {
            @Override
            public void onRecieveData(final List<ProduceType> produceTypes) {
                if(produceTypes.size()>0){
                    histoty_select.setVisibility(View.VISIBLE);
                    List<String> mtags=new ArrayList<String>();
                    for(ProduceType pt:produceTypes){
                        mtags.add(pt.getName());
                    }
                    tags.setTags(mtags);
                    tags.setOnTagClickListener(new TagView.OnTagClickListener() {
                        @Override
                        public void onTagClick(int position, String text) {
                            Intent intent=new Intent();
                            intent.putExtra("produce",produceTypes.get(position));
                            setResult(RESULT_OK,intent);
                            finish();
                        }
                        @Override
                        public void onTagLongClick(int position, String text) {

                        }
                        @Override
                        public void onTagCrossClick(int position) {

                        }
                    });
                }else{
                    histoty_select.setVisibility(View.GONE);
                }
            }
        });
        findViewById(R.id.back_bn).setOnClickListener(this);
        root=findViewById(R.id.root);
        search_badge=findViewById(R.id.search_badge);
        search=(EditTextWithDeleteButton) findViewById(R.id.search);
        search.addTextChangedListener(this);
        list=(ListView) findViewById(android.R.id.list);
        list.setOnItemClickListener(this);
        tv_no_data=(TextView) findViewById(R.id.tv_no_data);
        list.setEmptyView(tv_no_data);
        adapter=new MyAdapter(this);
        Produce1Fragment produce1Fragment=new Produce1Fragment();
        Bundle bundle=new Bundle();
        bundle.putInt("p1",p1);
        bundle.putInt("p2",p2);
        bundle.putInt("p3",p3);
        produce1Fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().add(R.id.tv_choose_city,produce1Fragment).commit();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ProduceType produce=adapter.getItem(position);
        ProduceTypesHelper.addHistotyProduce(this,produce);
        Intent data=new Intent();
        data.putExtra("produce",produce);
        setResult(RESULT_OK,data);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        imm.hideSoftInputFromWindow(search.getWindowToken(),0);
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.back_bn){
            onBackPressed();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if(s.toString().trim().length()>0){
            ProduceTypesHelper.getSearchWithoutParent(this, s.toString().trim(), new ProduceTypesHelper.OnDataRecieveListener() {
                @Override
                public void onRecieveData(List<ProduceType> produceTypes) {
                    search_badge.setVisibility(View.VISIBLE);
                    root.setVisibility(View.GONE);
                    adapter.clear();
                    adapter.addAll(produceTypes);
                    list.setAdapter(adapter);
                }
            });
        }else{
            root.setVisibility(View.VISIBLE);
            search_badge.setVisibility(View.GONE);
        }
    }
    private class MyAdapter extends ArrayAdapter<ProduceType> {
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
}
