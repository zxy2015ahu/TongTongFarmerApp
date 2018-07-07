package com.tongtong.purchaser.adapter;


import java.util.List;


import com.tongtong.purchaser.R;
import com.tongtong.purchaser.model.PurchaserReleaseInformationModel;
import com.tongtong.purchaser.utils.HttpImageLoadTask;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class BuyListAdapter extends BaseAdapter{

	private List<PurchaserReleaseInformationModel> items;
	private Context mContext;
	private RemoverListener ls;
	public BuyListAdapter(Context c,List<PurchaserReleaseInformationModel> items,RemoverListener ls){
		mContext = c;
		this.items = items;
		this.ls = ls;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return items.size();
	}

	@Override
	public PurchaserReleaseInformationModel getItem(int arg0) {
		// TODO Auto-generated method stub
		return items.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(final int position, View view, ViewGroup arg2) {
		view = LayoutInflater.from(mContext).inflate(R.layout.buy_list_item, null);
		ImageView icon = (ImageView) view.findViewById(R.id.icon);
		TextView title = (TextView) view.findViewById(R.id.title);
		TextView remarks = (TextView) view.findViewById(R.id.remarks);
		TextView remove = (TextView) view.findViewById(R.id.remove);
		title.setText(items.get(position).getProduce().getName());
		if(items.get(position).getRemarks()!=null&&!items.get(position).getRemarks().equals("")){
			remarks.setVisibility(View.VISIBLE);
			remarks.setText(items.get(position).getRemarks());
		}
		else{
			remarks.setVisibility(View.GONE);
		}
		
		HttpImageLoadTask httpImageLoadTask = new HttpImageLoadTask(icon, R.drawable.no_icon);
		httpImageLoadTask.execute(items.get(position).getProduce().getIconUrl());
		
		remove.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				if(ls!=null){
					ls.onRemove(position);
				}
			}
		});
		return view;
	}
	
	public interface RemoverListener{
		void onRemove(int index);
	}

}
