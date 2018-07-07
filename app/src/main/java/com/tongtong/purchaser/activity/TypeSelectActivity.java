package com.tongtong.purchaser.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.model.ProduceType;
import com.tongtong.purchaser.utils.HistoryHelper;
import com.tongtong.purchaser.utils.ProduceTypesHelper;
import com.tongtong.purchaser.utils.UrlUtil;
import com.tongtong.purchaser.widget.FilterImageView;
import com.tongtong.purchaser.widget.NoScrollGridView;

import net.lucode.hackware.magicindicator.buildins.UIUtil;

import java.util.ArrayList;
import java.util.List;

import cdflynn.android.library.scroller.BubbleScroller;
import cdflynn.android.library.scroller.ScrollerListener;
import co.lujun.androidtagview.TagContainerLayout;
import co.lujun.androidtagview.TagView;

/**
 * Created by Administrator on 2018-04-27.
 */

public class TypeSelectActivity extends BaseActivity implements View.OnClickListener,
        AdapterView.OnItemClickListener,ScrollerListener,AbsListView.OnScrollListener{
    private ListView nav;
    private ListView content;
    private BubbleScroller scroller;
    private MyLeftAdapter leftAdapter;
    private int select_position=0;
    private MyRightAdapter rightAdapter;
    private int grid_width;
    private View list_content,history_content;
    private TagContainerLayout tags;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.type_select_layout);
        ((TextView)findViewById(R.id.title_text)).setText("分类选择");
        ((ImageView)findViewById(R.id.phone)).setImageResource(R.drawable.search_white);
        findViewById(R.id.right_bn).setOnClickListener(this);
        findViewById(R.id.back_bn).setOnClickListener(this);
        nav=(ListView) findViewById(R.id.nav);
        nav.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        content=(ListView) findViewById(R.id.content);
        content.setOnScrollListener(this);
        list_content=findViewById(R.id.line1);
        history_content=findViewById(R.id.line3);
        View footer=getLayoutInflater().inflate(R.layout.autolistview_footer,null);
        footer.findViewById(R.id.more).setVisibility(View.GONE);
        TextView loadFull=(TextView) footer.findViewById(R.id.loadFull);
        loadFull.setVisibility(View.VISIBLE);
        tags=(TagContainerLayout) findViewById(R.id.history_1);
        tags.setBackgroundColor(ContextCompat.getColor(this,R.color.city_search_bg));
        tags.setBorderColor(Color.TRANSPARENT);
        tags.setTagTextColor(ContextCompat.getColor(this,R.color.aliwx_common_text_color2));
        tags.setTagBorderColor(ContextCompat.getColor(this,R.color.line_color));
        tags.setTagBackgroundColor(ContextCompat.getColor(this,R.color.aliwx_white));
        tags.setTagBorderRadius(UIUtil.dip2px(this,5));
        loadFull.setText("---已经到底了---");
        footer.findViewById(R.id.loading).setVisibility(View.GONE);
        footer.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,UIUtil.dip2px(this,44f)));
        content.addFooterView(footer);
        scroller=(BubbleScroller)findViewById(R.id.smily_scroller);
        scroller.setScrollerListener(this);
        leftAdapter=new MyLeftAdapter(this);
        nav.setAdapter(leftAdapter);
        DisplayMetrics dm=new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        grid_width=(dm.widthPixels-(getResources().getDimensionPixelSize(R.dimen.aliwx_activity_vertical_margin))-UIUtil.dip2px(this,170f))/3;
        ProduceTypesHelper.getParentType(this, new ProduceTypesHelper.OnDataRecieveListener() {
            @Override
            public void onRecieveData(final List<ProduceType> produceTypes) {
                ProduceTypesHelper.getHistoryProduce(TypeSelectActivity.this, new ProduceTypesHelper.OnDataRecieveListener() {
                    @Override
                    public void onRecieveData(List<ProduceType> produce) {
                        if(produce.size()>0){
                            ProduceType pt=new ProduceType();
                            pt.setId(-1);
                            pt.setName("常用选择");
                            produceTypes.add(0,pt);
                        }
                        leftAdapter.addAll(produceTypes);
                        if(produceTypes.size()>0){
                            int id=produceTypes.get(0).getId();
                            if(id==-1){
                                history_content.setVisibility(View.VISIBLE);
                                list_content.setVisibility(View.GONE);
                                ProduceTypesHelper.getHistoryProduce(TypeSelectActivity.this, new ProduceTypesHelper.OnDataRecieveListener() {
                                    @Override
                                    public void onRecieveData(final List<ProduceType> produceTypes) {
                                        List<String> mtags=new ArrayList<String>();
                                        for(ProduceType pro:produceTypes){
                                            mtags.add(pro.getName());
                                        }
                                        tags.setTags(mtags);
                                        tags.setOnTagClickListener(new TagView.OnTagClickListener() {
                                            @Override
                                            public void onTagClick(int position, String text) {
                                                if("search".equals(getIntent().getStringExtra("flag"))){
                                                    ProduceType pro= produceTypes.get(position);
                                                    HistoryHelper.addSearchHistory(TypeSelectActivity.this,pro.getName());
                                                    Intent intent=new Intent();
                                                    intent.putExtra("produce",pro);
                                                    intent.setClass(TypeSelectActivity.this,SearchResultActivity.class);
                                                    startActivity(intent);
                                                }
                                            }
                                            @Override
                                            public void onTagLongClick(int position, String text) {

                                            }

                                            @Override
                                            public void onTagCrossClick(int position) {

                                            }
                                        });
                                    }
                                });
                            }else{
                                history_content.setVisibility(View.GONE);
                                list_content.setVisibility(View.VISIBLE);
                                ProduceTypesHelper.getSubType(id, TypeSelectActivity.this, new ProduceTypesHelper.OnDataRecieveListener() {
                                    @Override
                                    public void onRecieveData(List<ProduceType> produceTypes) {
                                        rightAdapter=new MyRightAdapter(TypeSelectActivity.this);
                                        rightAdapter.addAll(produceTypes);
                                        content.setAdapter(rightAdapter);
                                    }
                                });
                            }

                        }
                    }
                });

            }
        });
        nav.setOnItemClickListener(this);
    }
    private class MyRightAdapter extends ArrayAdapter<ProduceType>{
        public MyRightAdapter(Context context){
            super(context,0);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if(convertView==null){
                convertView=getLayoutInflater().inflate(R.layout.right_item,null);
            }

            ProduceType produce=getItem(position);
           TextView title=(TextView) convertView.findViewById(R.id.title);
           NoScrollGridView grid=(NoScrollGridView) convertView.findViewById(R.id.gridGallery);
            title.setText(produce.getFirst_letter());
           final MyGridAdapter adapter=new MyGridAdapter(produce.getProduceTypes());
            grid.setAdapter(adapter);
            grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if("search".equals(getIntent().getStringExtra("flag"))){
                        ProduceType pro=(ProduceType) adapter.getItem(position);
                        HistoryHelper.addSearchHistory(TypeSelectActivity.this,pro.getName());
                        Intent intent=new Intent();
                        intent.putExtra("produce",pro);
                        intent.setClass(TypeSelectActivity.this,SearchResultActivity.class);
                        startActivity(intent);
                    }
                }
            });
            return convertView;
        }
    }
    private class MyGridAdapter extends BaseAdapter {
        private List<ProduceType> produceTypes;
        public MyGridAdapter(List<ProduceType> produceTypes){
            this.produceTypes=produceTypes;
        }

        @Override
        public Object getItem(int position) {
            return produceTypes.get(position);
        }

        @Override
        public int getCount() {
            return produceTypes.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if(convertView==null){
                convertView=getLayoutInflater().inflate(R.layout.my_produce_item,null);
            }
            convertView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            ProduceType produce=(ProduceType) getItem(position);
            TextView title=(TextView) convertView.findViewById(R.id.title);
            FilterImageView head=(FilterImageView) convertView.findViewById(R.id.head);
            RelativeLayout.LayoutParams rparams=new RelativeLayout.LayoutParams(grid_width,grid_width);
            rparams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            head.setLayoutParams(rparams);
            title.setText(produce.getName());
            Glide.with(TypeSelectActivity.this).load(UrlUtil.IMG_SERVER_URL+produce.getIcon_url()).placeholder(R.drawable.no_icon).centerCrop()
                    .into(head);
            return convertView;
        }
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long ids) {
        if(select_position==position){
            return;
        }
        select_position=position;
        leftAdapter.notifyDataSetChanged();
        final ProduceType produce=leftAdapter.getItem(position);
        int id=produce.getId();
        if(id==-1){
            history_content.setVisibility(View.VISIBLE);
            list_content.setVisibility(View.GONE);
            ProduceTypesHelper.getHistoryProduce(TypeSelectActivity.this, new ProduceTypesHelper.OnDataRecieveListener() {
                @Override
                public void onRecieveData(final List<ProduceType> produceTypes) {
                    List<String> mtags=new ArrayList<String>();
                    for(ProduceType pro:produceTypes){
                        mtags.add(pro.getName());
                    }
                    tags.setTags(mtags);
                    tags.setOnTagClickListener(new TagView.OnTagClickListener() {
                        @Override
                        public void onTagClick(int position, String text) {
                            if("search".equals(getIntent().getStringExtra("flag"))){
                                ProduceType pro= produceTypes.get(position);
                                HistoryHelper.addSearchHistory(TypeSelectActivity.this,pro.getName());
                                Intent intent=new Intent();
                                intent.putExtra("produce",pro);
                                intent.setClass(TypeSelectActivity.this,SearchResultActivity.class);
                                startActivity(intent);
                            }
                        }
                        @Override
                        public void onTagLongClick(int position, String text) {

                        }

                        @Override
                        public void onTagCrossClick(int position) {

                        }
                    });
                }
            });
        }else{
            history_content.setVisibility(View.GONE);
            list_content.setVisibility(View.VISIBLE);
            ProduceTypesHelper.getSubType(id, TypeSelectActivity.this, new ProduceTypesHelper.OnDataRecieveListener() {
                @Override
                public void onRecieveData(List<ProduceType> produceTypes) {
                    rightAdapter=new MyRightAdapter(TypeSelectActivity.this);
                    rightAdapter.addAll(produceTypes);
                    content.setAdapter(rightAdapter);
                }
            });
        }
    }
    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.back_bn){
            onBackPressed();
        }else if(v.getId()==R.id.right_bn){
            Intent intent=new Intent();
            intent.setClass(this,SearchTypeActivity.class);
            startActivity(intent);
        }
    }
    private class MyLeftAdapter extends ArrayAdapter<ProduceType>{
       public MyLeftAdapter(Context context){
           super(context,0);
       }
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if(convertView==null){
                convertView=getLayoutInflater().inflate(R.layout.check_item,null);
            }
            ProduceType produce=getItem(position);
            View item_main_left_bg=convertView.findViewById(R.id.item_main_left_bg);
            TextView title=(TextView) convertView.findViewById(R.id.title_text);
            if(position==select_position){
                item_main_left_bg.setVisibility(View.VISIBLE);
                convertView.setBackgroundColor(ContextCompat.getColor(TypeSelectActivity.this,R.color.city_search_bg));
                title.setTextColor(ContextCompat.getColor(TypeSelectActivity.this,R.color.colorPrimary));
            }else{
                item_main_left_bg.setVisibility(View.GONE);
                convertView.setBackgroundColor(ContextCompat.getColor(TypeSelectActivity.this,android.R.color.white));
                title.setTextColor(ContextCompat.getColor(TypeSelectActivity.this,R.color.aliwx_common_text_color));
            }
            title.setText(produce.getName());
            return convertView;
        }
    }

    @Override
    public void onSectionClicked(int sectionPosition) {
        int start=65+sectionPosition;
        String letter=String.valueOf((char)start);
        int selection=-1;
        for(int i=0;i<rightAdapter.getCount();i++){
            ProduceType produce=rightAdapter.getItem(i);
            if(produce.getFirst_letter().equals(letter)){
                selection=i;
                break;
            }
        }
        if(selection!=-1){
            content.setSelection(selection);
        }
    }
    @Override
    public void onScrollPositionChanged(float percentage, int sectionPosition) {
        int start=65+sectionPosition;
        String letter=String.valueOf((char)start);
        int selection=-1;
        for(int i=0;i<rightAdapter.getCount();i++){
            ProduceType produce=rightAdapter.getItem(i);
            if(produce.getFirst_letter().equals(letter)){
                selection=i;
                break;
            }
        }
        if(selection!=-1){
            content.setSelection(selection);
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if(rightAdapter==null){
            return;
        }
        ProduceType produce=rightAdapter.getItem(firstVisibleItem);
        char ch=produce.getFirst_letter().charAt(0);
        scroller.showSectionHighlight(ch-65);
    }
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }
}
