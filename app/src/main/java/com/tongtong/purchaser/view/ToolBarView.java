package com.tongtong.purchaser.view;


import com.tongtong.purchaser.R;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class ToolBarView extends LinearLayout {
    private Context mContext;
    private OnChangedListener changedLs;
	public ToolBarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}
	
	public void addItem(String[] strs){
		LayoutInflater inflater = LayoutInflater.from(mContext);
		for(int i=0;i<strs.length;i++){
			final int index = i;
			View item = inflater.inflate(R.layout.toolbar_item, null);
			LayoutParams params = new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
			params.weight = 1.0f;
			item.setLayoutParams(params);
			TextView label = (TextView) item.findViewById(R.id.item_label);
			label.setText(strs[i]);
			this.addView(item);
			item.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					setCurrentItem(index);
				}
			});
		}
	}
	
	public void setCurrentItem(int index){
		for(int i=0;i<this.getChildCount();i++){
			if(index == i){
				View child = this.getChildAt(i);
				TextView label = (TextView) child.findViewById(R.id.item_label);
				label.setTextColor(getResources().getColor(R.color.toolbar_text_selected));
				child.setBackgroundResource(R.drawable.toolbar_item_bg_selected);
			}
			else{
				View child = this.getChildAt(i);
				TextView label = (TextView) child.findViewById(R.id.item_label);
				label.setTextColor(getResources().getColor(R.color.toolbar_text_normal));
				
				child.setBackgroundResource(R.drawable.toolbar_item_bg_normal);
			}
		}
		if(changedLs!=null){
		changedLs.onChanged(index);
		}
	}
	
	public void setOnChangedListener(OnChangedListener ls){
		changedLs = ls;
	}
	
	public interface OnChangedListener {
		public void onChanged(int index);
	}

}
