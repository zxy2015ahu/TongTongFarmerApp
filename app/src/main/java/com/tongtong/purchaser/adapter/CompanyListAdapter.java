package com.tongtong.purchaser.adapter;


import java.util.List;

import com.tongtong.purchaser.R;
import com.tongtong.purchaser.model.CompanyModel;
import com.tongtong.purchaser.model.RegionModel;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class CompanyListAdapter extends BaseAdapter{

	private List<CompanyModel> items;
	private Context mContext;
	public CompanyListAdapter(Context c,List<CompanyModel> items){
		mContext = c;
		this.items = items;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return items.size();
	}

	@Override
	public CompanyModel getItem(int arg0) {
		// TODO Auto-generated method stub
		return items.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View view, ViewGroup arg2) {
		view = LayoutInflater.from(mContext).inflate(R.layout.company_list_item, null);
		TextView title = (TextView) view.findViewById(R.id.title);
		title.setText(items.get(position).getCompanyName());
		return view;
	}

}
