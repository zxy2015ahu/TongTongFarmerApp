package com.tongtong.purchaser.adapter;



import com.tongtong.purchaser.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class DialogListAdapter extends BaseAdapter{

	private String[] items;
	private Context mContext;
	public DialogListAdapter(Context c,String[] items){
		mContext = c;
		this.items = items;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return items.length;
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return items[arg0];
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View view, ViewGroup arg2) {
		view = LayoutInflater.from(mContext).inflate(R.layout.dialog_list_item, null);
		TextView title = (TextView) view.findViewById(R.id.title);
		title.setText(items[position]);
		return view;
	}

}
