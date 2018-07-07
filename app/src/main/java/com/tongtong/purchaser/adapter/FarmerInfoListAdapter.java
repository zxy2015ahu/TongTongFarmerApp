package com.tongtong.purchaser.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tongtong.purchaser.R;
import com.tongtong.purchaser.model.FarmerReleaseInformationModel;
import com.tongtong.purchaser.utils.HttpImageLoadTask;

import java.util.List;

public class FarmerInfoListAdapter extends BaseAdapter {

	private List<FarmerReleaseInformationModel> items;
	private Context mContext;

	public FarmerInfoListAdapter(Context c,
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
				R.layout.farmer_info_list_item, null);
		ImageView icon = (ImageView) view.findViewById(R.id.icon);
		TextView title = (TextView) view.findViewById(R.id.title);
		TextView location = (TextView) view.findViewById(R.id.location);
		TextView estimatedQuantity = (TextView) view
				.findViewById(R.id.estimated_quantity);
		title.setText(items.get(position).getProduce().getName());
		location.setText(items.get(position).getReleaseLocation());
		estimatedQuantity.setText(items.get(position).getEstimatedQuantity()
				+ items.get(position).getProduce().getUnit());
		
		HttpImageLoadTask httpImageLoadTask= new HttpImageLoadTask(icon, R.drawable.no_icon);
		httpImageLoadTask.execute(items.get(position).getReleaseVedioThumb());
		return view;
	}

	
}
