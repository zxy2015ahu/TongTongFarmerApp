package com.tongtong.purchaser.activity;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.adapter.DialogListAdapter;
import com.tongtong.purchaser.frament.WodeFragment;
import com.tongtong.purchaser.utils.NetUtil;
import com.tongtong.purchaser.utils.UserUtil;

import java.io.File;

public class MyInfoActivity extends BaseActivity implements OnClickListener {

	private View backBn;
	private View headLayout;
	private ImageView headImageView;
	private View updatePasswordLayout;
	private View nameLayout;
	private View companyLayout;
	private TextView phoneTextView;
	private TextView nameTextView;
	private TextView addressTextView;
	private TextView companyTextView;
	private View addressLayout;
	private String headUrl;
	private String name;
	private String addressStr;
	private String companyName;
	private final int START_ALBUM_REQUESTCODE = 1;
	private final int CAMERA_WITH_DATA = 2;
	private final int CROP_RESULT_CODE = 3;
	public static final String TMP_PATH = "clip_temp.jpg";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_myinfo);
		TextView titleText = (TextView) findViewById(R.id.title_text);
		titleText.setText(R.string.myinfo_title);
		backBn = findViewById(R.id.back_bn);
		backBn.setOnClickListener(this);
		headLayout = findViewById(R.id.head_layout);
		headLayout.setOnClickListener(this);
		phoneTextView = (TextView) findViewById(R.id.phone);
		phoneTextView.setText(UserUtil.getUserModel(this).getPhone());
		nameTextView = (TextView) findViewById(R.id.name);
		addressTextView = (TextView) findViewById(R.id.address);
		companyTextView = (TextView) findViewById(R.id.company);
		addressLayout = findViewById(R.id.address_layout);
		addressLayout.setOnClickListener(this);
		nameLayout = findViewById(R.id.name_layout);
		nameLayout.setOnClickListener(this);
		companyLayout = findViewById(R.id.company_layout);
		companyLayout.setOnClickListener(this);
		companyLayout.setVisibility(View.GONE);
	    name = UserUtil.getUserModel(this).getName();
		if(name==null||name.equals("")){
			nameTextView.setText(R.string.no_write);
		}
		else{
			nameTextView.setText(name);
		}
		addressStr = UserUtil.getUserModel(this).getAddressStr();
		if(addressStr==null||addressStr.equals("")){
			addressTextView.setText(R.string.no_write);
		}
		else{
			addressTextView.setText(addressStr);
		}
		companyName = UserUtil.getUserModel(this).getCompany().getCompanyName();
		if(companyName==null||companyName.equals("")){
			companyTextView.setText(R.string.no_write);
		}
		else{
			companyTextView.setText(companyName);
		}
		updatePasswordLayout = findViewById(R.id.password_layout);
		updatePasswordLayout.setOnClickListener(this);
		headImageView = (ImageView) findViewById(R.id.head_img);
		headUrl = UserUtil.getUserModel(this).getHeadUrl();
		Glide.with(this).load(NetUtil.getFullUrl(headUrl))
				.into(headImageView);
	}
	
	

	@Override
	protected void onResume() {
		
		super.onResume();
		if(!addressStr.equals(UserUtil.getUserModel(this).getAddressStr())){
			addressStr = UserUtil.getUserModel(this).getAddressStr();
			addressTextView.setText(addressStr);
		}
		
		if(!name.equals(UserUtil.getUserModel(this).getName())){
			name = UserUtil.getUserModel(this).getName();
			nameTextView.setText(name);
            if(WodeFragment.getInstance()!=null){
                WodeFragment.getInstance().setInfo();
            }
		}
		
		if(!companyName.equals(UserUtil.getUserModel(this).getCompany().getCompanyName())){
			companyName = UserUtil.getUserModel(this).getCompany().getCompanyName();
			companyTextView.setText(companyName);
		}
	}



	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back_bn:

			finish();

			break;
		case R.id.address_layout:
			Intent addressIntent = new Intent();
			addressIntent.setClass(this, ChoiceProvinceActivity.class);
			this.startActivity(addressIntent);
			break;
		case R.id.name_layout:

			Intent updateNameIntent = new Intent();
			updateNameIntent.setClass(this, UpdateNameActivity.class);
			this.startActivity(updateNameIntent);

			break;
		case R.id.head_layout:

			openHead();

			break;
		case R.id.company_layout:
			Intent choiceCompanyIntent = new Intent();
			choiceCompanyIntent.setClass(this, ChoiceCompanyActivity.class);
			this.startActivity(choiceCompanyIntent);
			

			break;

		case R.id.password_layout:



			break;

		default:
			break;
		}

	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// String result = null;
		if (resultCode != RESULT_OK) {
			return;
		}

		switch (requestCode) {

		case START_ALBUM_REQUESTCODE:

			Intent intent = new Intent(this, ClipImageActivity.class);
			intent.setData(data.getData());
			this.startActivityForResult(intent, CROP_RESULT_CODE);

			break;
		case CAMERA_WITH_DATA:
			startCropImageActivity(Environment.getExternalStorageDirectory()
					+ "/" + TMP_PATH);
			break;
			case CROP_RESULT_CODE:
				headUrl = UserUtil.getUserModel(this).getHeadUrl();
				Glide.with(this).load(NetUtil.getFullUrl(headUrl))
						.into(headImageView);
				if(WodeFragment.getInstance()!=null){
					WodeFragment.getInstance().setInfo();
				}
				break;
			
		}
	}
	
	private void openHead() {
		final String[] items = this.getResources().getStringArray(
				R.array.setting_head);
		DialogListAdapter dialogAdapter = new DialogListAdapter(this, items);
		AlertDialog.Builder singleChoiceDialog = new AlertDialog.Builder(this);

		singleChoiceDialog.setSingleChoiceItems(dialogAdapter, -1,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (which == 0) {
							startAlbum();
						} else if (which == 1) {
							startCapture();
						}
						dialog.dismiss();
					}
				});
		singleChoiceDialog.show();
	}
	
	private void startAlbum() {
		try {
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
			intent.setType("image/*");
			startActivityForResult(intent, START_ALBUM_REQUESTCODE);
		} catch (ActivityNotFoundException e) {
			e.printStackTrace();
			try {
				Intent intent = new Intent(Intent.ACTION_PICK, null);
				intent.setDataAndType(
						MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
				startActivityForResult(intent, START_ALBUM_REQUESTCODE);
			} catch (Exception e2) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
	}

	private void startCapture() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
			intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(this,"com.tongtong.purchaser.installapk",new File(
					Environment.getExternalStorageDirectory(), TMP_PATH)));
			intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		}else{
			intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(
					Environment.getExternalStorageDirectory(), TMP_PATH)));
		}
		startActivityForResult(intent, CAMERA_WITH_DATA);
	}
	
	private void startCropImageActivity(String path) {
		ClipImageActivity.startActivity(this, path, CROP_RESULT_CODE);
	}
}
