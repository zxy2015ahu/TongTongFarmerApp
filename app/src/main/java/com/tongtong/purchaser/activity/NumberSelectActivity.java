package com.tongtong.purchaser.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.EditText;
import android.widget.TextView;

import com.flyco.dialog.listener.OnOperItemClickL;
import com.flyco.dialog.widget.ActionSheetDialog;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.widget.NoScrollGridView;
import com.tongtong.purchaser.widget.StyleableToast;

/**
 * Created by Administrator on 2018-05-04.
 */

public class NumberSelectActivity extends BaseActivity implements View.OnClickListener,
        AdapterView.OnItemClickListener,TextWatcher{
    private NoScrollGridView list;
    private MyAdapter adapter;
    private int select_position;
    private SharedPreferences sp;
    private TextView unit;
    private EditText album_num;
    private TextView desciption;
    private InputMethodManager imm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.number_select_layout);
        sp=getSharedPreferences("location", Context.MODE_PRIVATE);
        ((TextView)findViewById(R.id.title_text)).setText("需求量");
        findViewById(R.id.back_bn).setOnClickListener(this);
        list=(NoScrollGridView) findViewById(R.id.list);
        adapter=new MyAdapter(this);
        adapter.add("单次");
        adapter.add("每天");
        adapter.add("每周");
        adapter.add("每月");
        list.setOnItemClickListener(this);
        findViewById(R.id.number_unit).setOnClickListener(this);
        unit=(TextView) findViewById(R.id.price_unit);
        final String units=sp.getString("unit","");
        final String[] items=units.split(",");
        album_num=(EditText) findViewById(R.id.album_num);
        album_num.addTextChangedListener(this);
        desciption=(TextView) findViewById(R.id.description);
        findViewById(R.id.confirm_bn).setOnClickListener(this);
        select_position=getIntent().getIntExtra("select_position",0);
        album_num.setText(getIntent().getStringExtra("num"));
        unit.setText(TextUtils.isEmpty(getIntent().getStringExtra("unit"))?items[0]:getIntent().getStringExtra("unit"));
        desciption.setText(getIntent().getStringExtra("desc"));
        list.setAdapter(adapter);
        imm=(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    }
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        imm.hideSoftInputFromWindow(album_num.getWindowToken(),0);
    }

    @Override
    public void afterTextChanged(Editable s) {
        if(s.toString().length()>0){
            desciption.setText(adapter.getItem(select_position)+s.toString()+unit.getText().toString());
        }else{
            desciption.setText(null);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        select_position=position;
        if(!TextUtils.isEmpty(album_num.getText().toString())){
            desciption.setText(adapter.getItem(select_position)+album_num.getText().toString()+unit.getText().toString());
        }
        adapter.notifyDataSetChanged();
    }

    private class MyAdapter extends ArrayAdapter<String>{
        public MyAdapter(Context context){
            super(context,0);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if(convertView==null){
                convertView=getLayoutInflater().inflate(R.layout.guige_item,null);
            }
            View tag=convertView.findViewById(R.id.tag);
            TextView title=(TextView) convertView.findViewById(R.id.title);
            if(select_position==position){
                tag.setVisibility(View.VISIBLE);
                convertView.setBackgroundResource(R.drawable.edit_bg13);
                title.setTextColor(ContextCompat.getColor(NumberSelectActivity.this,R.color.colorPrimary));
            }else{
                tag.setVisibility(View.GONE);
                convertView.setBackgroundResource(R.drawable.edit_bg2);
                title.setTextColor(ContextCompat.getColor(NumberSelectActivity.this,R.color.aliwx_common_text_color));
            }
            ((TextView)convertView.findViewById(R.id.title)).setText(getItem(position));
            return convertView;
        }
    }
    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.back_bn){
            onBackPressed();
        }else if(v.getId()==R.id.number_unit){
            final String units=sp.getString("unit","");
            final String[] items=units.split(",");
            final ActionSheetDialog dialog=new ActionSheetDialog(this,items,null);
            dialog.itemTextSize(15).itemHeight(38).setOnOperItemClickL(new OnOperItemClickL() {
                @Override
                public void onOperItemClick(AdapterView<?> parent, View view, int position, long id) {
                    unit.setText(items[position]);
                    if(!TextUtils.isEmpty(album_num.getText().toString())){
                        desciption.setText(adapter.getItem(select_position)+album_num.getText().toString()+unit.getText().toString());
                    }

                    dialog.dismiss();
                }
            });
            dialog.isTitleShow(false).show();
        }else if(v.getId()==R.id.confirm_bn){
            if(TextUtils.isEmpty(album_num.getText().toString())){
                StyleableToast.info(this,"请输入需求量");
                return;
            }
            Intent intent=new Intent();
            intent.putExtra("num",album_num.getText().toString());
            intent.putExtra("unit",unit.getText().toString());
            intent.putExtra("select_position",select_position);
            intent.putExtra("desc",desciption.getText().toString());
            setResult(RESULT_OK,intent);
            finish();
        }
    }
}
