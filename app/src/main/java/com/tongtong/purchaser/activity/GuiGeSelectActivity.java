package com.tongtong.purchaser.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.utils.CodeUtil;
import com.tongtong.purchaser.utils.HttpTask;
import com.tongtong.purchaser.utils.UrlUtil;
import com.tongtong.purchaser.widget.NoScrollGridView;
import com.tongtong.purchaser.widget.StyleableToast;

import java.util.HashMap;

/**
 * Created by Administrator on 2018-05-03.
 */

public class GuiGeSelectActivity extends BaseActivity implements View.OnClickListener{
    private HashMap<Integer,String> selecte_type;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guige_select_layout);
        ((TextView)findViewById(R.id.title_text)).setText("选择规格");
        findViewById(R.id.back_bn).setOnClickListener(this);
        loadGuige(getIntent().getIntExtra("produce_id",0));
        selecte_type=(HashMap<Integer,String>) getIntent().getSerializableExtra("guige");
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.back_bn){
            onBackPressed();
        }
    }

    private void loadGuige(int produce_id){
        HttpTask task=new HttpTask(this);
        task.setTaskHandler(new HttpTask.HttpTaskHandler() {
            @Override
            public void taskStart(int code) {

            }
            @Override
            public void taskSuccessful(String str, int code) {
                JsonObject selectResultJson = new JsonParser().parse(str)
                        .getAsJsonObject();
                int selectResultCode = selectResultJson.get("code").getAsInt();
                if(selectResultCode== CodeUtil.SUCCESS_CODE){
                    JsonArray items=selectResultJson.get("items").getAsJsonArray();
                    if(items.size()>0){
                        initGuigeData(items);
                    }
                }
            }
            @Override
            public void taskFailed(int code) {

            }
        });
        JsonObject object=new JsonObject();
        object.addProperty("produce_id",produce_id);
        task.execute(UrlUtil.GET_GUIGE,object.toString());
    }
    private class TagItem{
        private int id;
        private String name;
    }
    private void initGuigeData(JsonArray items){
        final LinearLayout root=(LinearLayout) findViewById(R.id.container);
        Button confirm_bn=(Button) findViewById(R.id.confirm_bn);
        confirm_bn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.putExtra("guige",selecte_type);
                setResult(RESULT_OK,intent);
                finish();
            }
        });
        for(int i=0;i<items.size();i++){
            JsonObject item=items.get(i).getAsJsonObject();
            View produce_item=View.inflate(this,R.layout.produce_item,null);
            TextView title=(TextView) produce_item.findViewById(R.id.title);
            final NoScrollGridView tags=(NoScrollGridView) produce_item.findViewById(R.id.tags);
            title.setText(item.get("name").getAsString());
            final JsonArray list=item.get("list").getAsJsonArray();
            final TagAdapter tagAdapter=new TagAdapter(this,tags);
            for(int j=0;j<list.size();j++){
                JsonObject ta=list.get(j).getAsJsonObject();
                TagItem tg=new TagItem();
                tg.id=ta.get("id").getAsInt();
                tg.name=ta.get("name").getAsString();
                tagAdapter.add(tg);
            }
            tags.setAdapter(tagAdapter);
            tags.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Integer _id=tagAdapter.getItem(position).id;
                    String name=tagAdapter.getItem(position).name;
                    boolean checked= selecte_type.containsKey(_id);
                    if(checked){
                        selecte_type.remove(_id);
                    }else{
                        if(selecte_type.size()>=5){
                            StyleableToast.info(GuiGeSelectActivity.this,"最多选择5个标签");
                            return;
                        }
                        selecte_type.put(_id,name);
                    }
                    tagAdapter.notifyDataSetChanged();
                }
            });
            root.addView(produce_item);
        }
    }

    private class TagAdapter extends ArrayAdapter<TagItem> {
        private NoScrollGridView list;
        public TagAdapter(Context context, NoScrollGridView list){
            super(context,0);
            this.list=list;
        }
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if(convertView==null){
                convertView=getLayoutInflater().inflate(R.layout.guige_item,null);
            }
            TagItem tg=getItem(position);
            boolean item=selecte_type.containsKey(tg.id);
            View tag=convertView.findViewById(R.id.tag);
            TextView title=(TextView) convertView.findViewById(R.id.title);
            if(item){
                tag.setVisibility(View.VISIBLE);
                convertView.setBackgroundResource(R.drawable.edit_bg13);
                title.setTextColor(ContextCompat.getColor(GuiGeSelectActivity.this,R.color.colorPrimary));
            }else{
                tag.setVisibility(View.GONE);
                convertView.setBackgroundResource(R.drawable.edit_bg2);
                title.setTextColor(ContextCompat.getColor(GuiGeSelectActivity.this,R.color.aliwx_common_text_color));
            }
            ((TextView)convertView.findViewById(R.id.title)).setText(tg.name);
            return convertView;
        }
    }
}
