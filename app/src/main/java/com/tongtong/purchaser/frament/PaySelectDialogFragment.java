package com.tongtong.purchaser.frament;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.tongtong.purchaser.R;
import com.tongtong.purchaser.activity.BindCardActivity;
import com.tongtong.purchaser.activity.SendHongBaoActivity;
import com.tongtong.purchaser.utils.Constant;


/**
 * Created by zxy on 2018/3/6.
 */

public class PaySelectDialogFragment extends DialogFragment implements View.OnClickListener{
    private RecyclerView pay_type_list;
    private MyAdapter adapter;
    private double my_money,money;
    private int pay_type;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        return inflater.inflate(R.layout.rp_choose_pay_dialog,container,false);
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pay_type_list=(RecyclerView) view.findViewById(R.id.pay_type_list);
        LinearLayoutManager manager=new LinearLayoutManager(getContext());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        pay_type_list.setLayoutManager(manager);
        view.findViewById(R.id.ib_choose_pay_back).setOnClickListener(this);
        my_money=getArguments().getDouble("my_money");
        money=getArguments().getDouble("money");
        pay_type=getArguments().getInt("pay_type");
        adapter=new MyAdapter();
        pay_type_list.setAdapter(adapter);
    }

    private class MyAdapter extends RecyclerView.Adapter<MyViewHolder>{
        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.rp_choose_pay_list_item,parent,false);
            MyViewHolder holder = new MyViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            if(position==0){
                holder.item_divider.setVisibility(View.GONE);
                holder.tv_pay_type.setText(String.format(getString(R.string.my_change),my_money));
                if(my_money<money){
                    holder.iv_pay_icon.setImageResource(R.drawable.rp_change_icon_grey);
                    holder.tv_pay_type.setTextColor(ContextCompat.getColor(getContext(),R.color.rp_text_unselected));
                }else{
                    holder.iv_pay_icon.setImageResource(R.drawable.rp_change_icon);
                    holder.item.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            pay_type= Constant.PAY_TYPE_YUE;
                            topay();
                        }
                    });
                }
            }else if(position==1){
                holder.item_divider.setVisibility(View.VISIBLE);
                holder.iv_pay_icon.setImageResource(R.drawable.rp_alipay_icon);
                holder.tv_pay_type.setText(getString(R.string.ali_pay));
                holder.tv_pay_type_limit.setText(String.format(getString(R.string.choose_pay_jd_limit),10000,10000));
                holder.item.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pay_type= Constant.PAY_TYPE_ALIPAY;
                        topay();
                    }
                });
            }else if(position==2){
                holder.item_divider.setVisibility(View.VISIBLE);
                holder.iv_pay_icon.setImageResource(R.drawable.rp_wxpay_icon);
                holder.tv_pay_type.setText(getString(R.string.wx_pay));
                holder.tv_pay_type_limit.setText(String.format(getString(R.string.choose_pay_jd_limit),10000,10000));
                holder.item.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pay_type= Constant.PAY_TYPE_WEIXIN;
                        topay();
                    }
                });
            }else{
                holder.item_divider.setVisibility(View.VISIBLE);
                holder.iv_pay_icon.setImageResource(R.drawable.rp_add_card_icon);
                holder.tv_pay_type.setText(getString(R.string.add_bankcard));
                holder.tv_pay_type_limit.setVisibility(View.GONE);
                holder.layout_pay_type.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent();
                        intent.setClass(SendHongBaoActivity.getInstance(), BindCardActivity.class);
                        startActivity(intent);
                    }
                });
            }
        }
        @Override
        public int getItemCount() {
            return 4;
        }
    }
    private class MyViewHolder extends RecyclerView.ViewHolder{
        private ImageView iv_pay_icon;
        private TextView tv_pay_type;
        private TextView tv_pay_type_limit;
        private View item_divider;
        private View layout_pay_type;
        private View item;
        public MyViewHolder(View item){
            super(item);
            this.item=item;
            layout_pay_type=item.findViewById(R.id.layout_pay_type);
            iv_pay_icon=(ImageView) item.findViewById(R.id.iv_pay_icon);
            tv_pay_type=(TextView) item.findViewById(R.id.tv_pay_type);
            item_divider=item.findViewById(R.id.item_divider);
            tv_pay_type_limit=(TextView) item.findViewById(R.id.tv_pay_type_limit);
        }
    }
    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.ib_choose_pay_back){
            topay();
        }
    }
    private void topay(){
        dismiss();
        PayDialogFragment pay=new PayDialogFragment();
        Bundle args=getArguments();
        args.putInt("pay_type",pay_type);
        pay.setArguments(args);
        pay.show(SendHongBaoActivity.getInstance().getSupportFragmentManager(),"pay");
    }
}
