package com.tongtong.purchaser.adapter;

import java.util.List;

import com.tongtong.purchaser.R;
import com.tongtong.purchaser.model.FarmerReleaseInformationModel;
import com.tongtong.purchaser.utils.HttpImageLoadTask;
import com.tongtong.purchaser.view.RoundAngleImageView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CollectionReleaseListAdapter extends BaseAdapter {

	private List<FarmerReleaseInformationModel> items;
	private Context mContext;

	public CollectionReleaseListAdapter(Context c,
			List<FarmerReleaseInformationModel> items) {
		mContext = c;
		this.items = items;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return items.size();
	}

	@Override
	public FarmerReleaseInformationModel getItem(int arg0) {
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
		view = LayoutInflater.from(mContext).inflate(
				R.layout.collection_release_list_item, null);
		RoundAngleImageView icon = (RoundAngleImageView) view.findViewById(R.id.icon);
		TextView title = (TextView) view.findViewById(R.id.title);
		TextView location = (TextView) view.findViewById(R.id.location);
		TextView state = (TextView) view.findViewById(R.id.state);
		TextView estimatedQuantity = (TextView) view
				.findViewById(R.id.estimated_quantity);
		title.setText(items.get(position).getFarmer().getName() + "("
				+ items.get(position).getProduce().getName() + ")");
		location.setText(items.get(position).getReleaseLocation());
		if(items.get(position).getState()==1){
			state.setText(R.string.sale_normal);
		}
		else{
			state.setText(R.string.sale_close);
		}
		estimatedQuantity.setText(items.get(position).getEstimatedQuantity()
				+ items.get(position).getProduce().getUnit());
		HttpImageLoadTask httpImageLoadTask = new HttpImageLoadTask(icon,
				R.drawable.default_head);
		httpImageLoadTask
				.execute(items.get(position).getFarmer().getHeadUrl());
		return view;
	}

}
