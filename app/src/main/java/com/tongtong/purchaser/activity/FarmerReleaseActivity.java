package com.tongtong.purchaser.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.adapter.CollectionReleaseListAdapter;
import com.tongtong.purchaser.adapter.FarmerReleaseListAdapter;
import com.tongtong.purchaser.model.FarmerReleaseInformationModel;
import com.tongtong.purchaser.model.QueryRangeModel;
import com.tongtong.purchaser.utils.HttpTask;
import com.tongtong.purchaser.utils.HttpTask.HttpTaskHandler;
import com.tongtong.purchaser.utils.UrlUtil;
import com.tongtong.purchaser.utils.UserUtil;
import com.tongtong.purchaser.view.CustomListView;
import com.tongtong.purchaser.view.CustomListView.OnLoadListener;
import com.tongtong.purchaser.view.CustomListView.OnRefreshListener;
import com.tongtong.purchaser.view.ToolBarView;
import com.tongtong.purchaser.view.ToolBarView.OnChangedListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FarmerReleaseActivity extends BaseActivity implements
		OnClickListener, HttpTaskHandler {
	private LinearLayout backBn;
	private CustomListView mListView;
	private QueryRangeModel queryRangeModel;
	private ListView collectionList;
	private ToolBarView toolBar;
	private int startIndex = 0;
	private int num = 20;
	private static final int ONLOAD = 1;
	private static final int ONREFRESH = 2;
	private static final int COLLECTION = 3;
	private List<FarmerReleaseInformationModel> items = new ArrayList<FarmerReleaseInformationModel>();
	private List<FarmerReleaseInformationModel> collectionItems;
	private CollectionReleaseListAdapter collectionAdapter;
	private FarmerReleaseListAdapter mAdapter;

	public static String UPDATE_COLLECTION = "com.tongtong.purchaser.UPDATE_COLLECTION";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_farmer_release);
		queryRangeModel = (QueryRangeModel) getIntent().getSerializableExtra(
				"queryRange");
		TextView titleText = (TextView) findViewById(R.id.title_text);
		titleText.setText(R.string.match_info);
		backBn = (LinearLayout) findViewById(R.id.back_bn);
		backBn.setOnClickListener(this);
		toolBar = (ToolBarView) findViewById(R.id.tool_bar);
		toolBar.addItem(this.getResources()
				.getStringArray(R.array.release_item));
		toolBar.setOnChangedListener(new OnChangedListener() {

			@Override
			public void onChanged(int index) {
				switch (index) {
				case 0:
					mListView.setVisibility(View.VISIBLE);
					collectionList.setVisibility(View.GONE);
					break;
				case 1:
					mListView.setVisibility(View.GONE);
					collectionList.setVisibility(View.VISIBLE);
					break;
				default:
					break;
				}

			}
		});
		
		collectionList = (ListView) findViewById(R.id.collectionList);
		collectionList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int index,
					long arg3) {
				if (index < collectionAdapter.getCount()) {
					Intent intent = new Intent();
					intent.setClass(FarmerReleaseActivity.this,
							FarmerReleaseInfoActivity.class);
					intent.putExtra("releaseInfo",
							(Serializable) collectionAdapter.getItem(index));
					FarmerReleaseActivity.this.startActivity(intent);
				}
			}

		});
		mListView = (CustomListView) findViewById(R.id.mListView);
		mListView.setonRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
				refreshData();

			}
		});

		mListView.setonLoadListener(new OnLoadListener() {

			@Override
			public void onLoad() {

				loadData();
			}
		});
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int index,
					long arg3) {
				if ((index - mListView.getHeaderViewsCount()) < mAdapter
						.getCount()) {
					Intent intent = new Intent();
					intent.setClass(FarmerReleaseActivity.this,
							FarmerReleaseInfoActivity.class);
					intent.putExtra(
							"releaseInfo",
							(Serializable) mAdapter.getItem(index
									- mListView.getHeaderViewsCount()));
					FarmerReleaseActivity.this.startActivity(intent);
				}
			}

		});
		toolBar.setCurrentItem(0);
		refreshData();
		initCollectionDate();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(UPDATE_COLLECTION);
		registerReceiver(updateCollectionReceiver, intentFilter);

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		unregisterReceiver(updateCollectionReceiver);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back_bn:
			finish();
			break;
		default:
			break;
		}

	}

	private void refreshData() {
		startIndex = 0;
		queryRangeModel.setStartIndex(startIndex);
		queryRangeModel.setNum(num);
		startIndex = startIndex + num;
		JsonObject dataJson = new JsonObject();
		Gson gson = new Gson();
		JsonObject queryDataJson = gson.toJsonTree(queryRangeModel)
				.getAsJsonObject();
		dataJson.addProperty("token", UserUtil.getUserModel(this).getToken());
		dataJson.add("queryData", queryDataJson);
		HttpTask httpTask = new HttpTask(ONREFRESH,this);
		httpTask.setTaskHandler(this);
		httpTask.execute(UrlUtil.QUREY_FARMER_RELEASE, dataJson.toString());
	}

	private void loadData() {
		queryRangeModel.setStartIndex(startIndex);
		queryRangeModel.setNum(num);
		startIndex = startIndex + num;
		JsonObject dataJson = new JsonObject();
		Gson gson = new Gson();
		JsonObject queryDataJson = gson.toJsonTree(queryRangeModel)
				.getAsJsonObject();
		dataJson.addProperty("token", UserUtil.getUserModel(this).getToken());
		dataJson.add("queryData", queryDataJson);
		HttpTask httpTask = new HttpTask(ONLOAD,this);
		httpTask.setTaskHandler(this);
		httpTask.execute(UrlUtil.QUREY_FARMER_RELEASE, dataJson.toString());
	}

	@Override
	public void taskStart(int code) {
		// TODO Auto-generated method stub

	}

	@Override
	public void taskSuccessful(String str, int code) {
		switch (code) {
		case ONLOAD:
			JsonObject loadResultJson = new JsonParser().parse(str)
					.getAsJsonObject();
			int loadResultCode = loadResultJson.get("code").getAsInt();
			if (verification(loadResultCode)) {

				Gson gson = new Gson();
				JsonArray releaseInfosJson = loadResultJson.get("data")
						.getAsJsonArray();
				List<FarmerReleaseInformationModel> releaseInfos = gson
						.fromJson(
								releaseInfosJson,
								new TypeToken<List<FarmerReleaseInformationModel>>() {
								}.getType());
				for (int i = 0; i < releaseInfos.size(); i++) {
					items.add(releaseInfos.get(i));

				}
				mAdapter = new FarmerReleaseListAdapter(
						FarmerReleaseActivity.this, items);

				mListView.setAdapter(mAdapter);
				mListView.onLoadComplete();
				if (releaseInfos.size() < num) {
					mListView.noHaveMore();
				} else {
					mListView.haveMore();
				}

			}
			break;
		case ONREFRESH:
			JsonObject refreshResultJson = new JsonParser().parse(str)
					.getAsJsonObject();
			int refreshResultCode = refreshResultJson.get("code").getAsInt();
			if (verification(refreshResultCode)) {

				Gson gson = new Gson();
				JsonArray releaseInfosJson = refreshResultJson.get("data")
						.getAsJsonArray();
				List<FarmerReleaseInformationModel> releaseInfos = gson
						.fromJson(
								releaseInfosJson,
								new TypeToken<List<FarmerReleaseInformationModel>>() {
								}.getType());

				items = releaseInfos;
				mAdapter = new FarmerReleaseListAdapter(
						FarmerReleaseActivity.this, items);
				mListView.setAdapter(mAdapter);

				mListView.onRefreshComplete();
				if (releaseInfos.size() < num) {
					mListView.noHaveMore();
				} else {
					mListView.haveMore();
				}
			}
			break;

		case COLLECTION:
			JsonObject resultJson = new JsonParser().parse(str)
					.getAsJsonObject();
			int resultCode = resultJson.get("code").getAsInt();
			if (verification(resultCode)) {

				Gson gson = new Gson();
				JsonArray releaseInfosJson = resultJson.get("data")
						.getAsJsonArray();
				collectionItems = gson.fromJson(releaseInfosJson,
						new TypeToken<List<FarmerReleaseInformationModel>>() {
						}.getType());
				collectionAdapter = new CollectionReleaseListAdapter(this,
						collectionItems);
				collectionList.setAdapter(collectionAdapter);

			}
			break;

		default:
			break;
		}

	}

	@Override
	public void taskFailed(int code) {
		// TODO Auto-generated method stub

	}

	private void initCollectionDate() {
		JsonObject dataJson = new JsonObject();
		dataJson.addProperty("token", UserUtil.getUserModel(this).getToken());
		HttpTask httpTask = new HttpTask(COLLECTION,this);
		httpTask.setTaskHandler(this);
		httpTask.execute(UrlUtil.SELECT_COLLECTION_RELEASE, dataJson.toString());
	}

	private BroadcastReceiver updateCollectionReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			initCollectionDate();

		}

	};
}
