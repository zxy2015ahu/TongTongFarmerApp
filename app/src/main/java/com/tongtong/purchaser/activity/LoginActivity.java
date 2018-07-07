package com.tongtong.purchaser.activity;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.application.MyApplication;
import com.tongtong.purchaser.model.MyProduceModel;
import com.tongtong.purchaser.model.PurchaserModel;
import com.tongtong.purchaser.utils.CodeUtil;
import com.tongtong.purchaser.utils.HttpTask;
import com.tongtong.purchaser.utils.HttpTask.HttpTaskHandler;
import com.tongtong.purchaser.utils.ProduceTypesHelper;
import com.tongtong.purchaser.utils.UrlUtil;
import com.tongtong.purchaser.utils.UserUtil;
import com.tongtong.purchaser.view.LoadingDialog;
import com.tongtong.purchaser.widget.AlertDialog;
import com.tongtong.purchaser.widget.StyleableToast;
import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;

import java.util.List;
import java.util.Map;

public class LoginActivity extends BaseActivity implements OnClickListener,
		TextWatcher, HttpTaskHandler {

	private EditText usernameText,phone,code;
	private EditText passwordText;
	private Button loginBn,loginsBn;
    private ImageButton delete,change_pass_type,delete2;
    private boolean show_pass=false;
	private UMShareAPI umShareAPI;
	private SharedPreferences sp;
	private SharedPreferences mylocation;
	private View code_layout,password_layout;
	private  TextView get_code;
	private boolean is_sending=false;
	private int COUNT=60;
	private long timestamp;
	private Handler handler=new Handler();
    private TextView forget_msg;
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		TextView titleText = (TextView) findViewById(R.id.title_text);
		sp=getSharedPreferences("location", Context.MODE_PRIVATE);
		mylocation=getSharedPreferences("mylocation", Context.MODE_PRIVATE);
		titleText.setText(R.string.login_text);
        delete=(ImageButton) findViewById(R.id.delete);
        delete.setOnClickListener(this);
		loginBn = (Button) findViewById(R.id.login_bn);
		loginBn.setEnabled(false);
		loginBn.setOnClickListener(this);
		usernameText = (EditText) findViewById(R.id.username);
		passwordText = (EditText) findViewById(R.id.password);
        change_pass_type=(ImageButton) findViewById(R.id.password_image);
        change_pass_type.setOnClickListener(this);
		delete2=(ImageButton) findViewById(R.id.delete_image_btn);
		delete2.setOnClickListener(this);
		usernameText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().length()>0){
                    delete.setVisibility(View.VISIBLE);
                }else{
                    delete.setVisibility(View.GONE);
                }
				if ((!"".equals(s.toString()))
						&& (!"".equals(passwordText.getEditableText().toString()))) {
					loginBn.setEnabled(true);
				} else {
					loginBn.setEnabled(false);
				}
            }
        });
		passwordText.addTextChangedListener(this);
		findViewById(R.id.weixin).setOnClickListener(this);
		findViewById(R.id.text_input_password_toggle).setOnClickListener(this);
		findViewById(R.id.check_code_and).setOnClickListener(this);
		code_layout=findViewById(R.id.check_code_complete);
		password_layout=findViewById(R.id.password_text);
		phone=(EditText) findViewById(R.id.phone);
		code=(EditText) findViewById(R.id.code);
		loginsBn=(Button) findViewById(R.id.logins);
		loginsBn.setOnClickListener(this);
		get_code=(TextView) findViewById(R.id.get_code);
        forget_msg=(TextView) findViewById(R.id.forward_msg);
		get_code.setOnClickListener(this);
        forget_msg.setOnClickListener(this);
		phone.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				if(s.toString().length()>0){
					delete2.setVisibility(View.VISIBLE);
				}else{
					delete2.setVisibility(View.GONE);
				}
			}
		});
		umShareAPI=UMShareAPI.get(this);
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		umShareAPI.onActivityResult(requestCode, resultCode, data);
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		umShareAPI.release();

	}
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		umShareAPI.onSaveInstanceState(outState);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.text_input_password_toggle:
				password_layout.setVisibility(View.VISIBLE);
				code_layout.setVisibility(View.GONE);
				break;
			case R.id.delete_image_btn:
				phone.getText().clear();
				break;
            case R.id.forward_msg:
                Intent intent=new Intent();
                intent.setClass(this,FindPasswordActivity.class);
                startActivity(intent);
                break;
			case R.id.check_code_and:
				password_layout.setVisibility(View.GONE);
				code_layout.setVisibility(View.VISIBLE);
				break;
			case R.id.get_code:
				if (TextUtils.isEmpty(phone.getText().toString().trim())) {
					showTips("请填写手机号");
					return;
				}
				if (is_sending) {
					return;
				}
				getCode();
				break;
			case R.id.logins:
				if (TextUtils.isEmpty(phone.getText().toString().trim())) {
					showTips("请填写手机号");
					return;
				}
				if (TextUtils.isEmpty(code.getText().toString().trim())) {
					showTips("请填写验证码");
					return;
				}
				checkCode();
				break;
		case R.id.login_bn:
			login();
			break;
			case R.id.weixin:
				umShareAPI.getPlatformInfo(this, SHARE_MEDIA.WEIXIN, new UMAuthListener() {
					@Override
					public void onStart(SHARE_MEDIA share_media) {
						showLoading();
					}
					@Override
					public void onComplete(SHARE_MEDIA share_media, int i, Map<String, String> map) {
						openLogin(map);
					}
					@Override
					public void onError(SHARE_MEDIA share_media, int i, Throwable throwable) {
						dismissLoading();
					}

					@Override
					public void onCancel(SHARE_MEDIA share_media, int i) {
						dismissLoading();
					}
				});
				break;
            case R.id.delete:
                usernameText.getText().clear();
                break;
            case R.id.password_image:
                if(show_pass){
                    show_pass=false;
                    passwordText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
					change_pass_type.setBackgroundResource(R.drawable.passwword_ming);
                }else{
                    show_pass=true;
                    passwordText.setTransformationMethod(PasswordTransformationMethod.getInstance());
					change_pass_type.setBackgroundResource(R.drawable.password_mi);
                }
                break;
//		case R.id.find_password_text:
//			Intent findPasswordIntent = new Intent();
//			findPasswordIntent.setClass(LoginActivity.this,
//					FindPasswordActivity.class);
//			startActivity(findPasswordIntent);
//			break;
		default:
			break;
		}
	}
	private void checkCode(){
		HttpTask task=new HttpTask(this);
		task.setTaskHandler(new HttpTask.HttpTaskHandler() {
			@Override
			public void taskStart(int code) {
				showLoading();
			}
			@Override
			public void taskSuccessful(String str, int code) {
				JsonObject object=new JsonParser().parse(str).getAsJsonObject();
				if(object.get("code").getAsInt()== CodeUtil.SUCCESS_CODE){
					Gson gson=new Gson();
					JsonObject userJson = object.get("data").getAsJsonObject();
					PurchaserModel purchaser = gson.fromJson(userJson, PurchaserModel.class);
					UserUtil.setUserModel(LoginActivity.this, purchaser);
					getConfig();
				}else if(object.get("code").getAsInt()==108){
					dismissLoading();
					Intent intent=new Intent();
					intent.putExtra("mobile",phone.getText().toString());
					intent.setClass(LoginActivity.this,ProfileSettingActivity.class);
					startActivity(intent);
					finish();
				}else{
					dismissLoading();
					showTips(object.get("info").getAsString());
				}
			}
			@Override
			public void taskFailed(int code) {
				dismissLoading();
			}
		});
		JsonObject object=new JsonObject();
		object.addProperty("timestamp",timestamp);
		object.addProperty("mobile",phone.getText().toString().trim());
		object.addProperty("code",code.getText().toString().trim());
		task.execute(UrlUtil.CHECK_CODE_LOGIN,object.toString());
	}
	private void countDown(){
		is_sending=true;
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if(COUNT>0) {
					COUNT--;
					get_code.setText(COUNT + "秒后重发");
					handler.postDelayed(this, 1000);
				}else{
					handler.removeCallbacks(this);
					COUNT=60;
					get_code.setText("获取验证码");
					is_sending=false;
				}
			}
		},1000);
	}
	private void getCode(){
		HttpTask task=new HttpTask(this);
		timestamp= System.currentTimeMillis()/1000;
		task.setTaskHandler(new HttpTask.HttpTaskHandler() {
			@Override
			public void taskStart(int code) {
				showLoading();
			}
			@Override
			public void taskSuccessful(String str, int code) {
				dismissLoading();
				JsonObject object=new JsonParser().parse(str).getAsJsonObject();
				if(object.get("code").getAsInt()== CodeUtil.SUCCESS_CODE){
					StyleableToast.info(LoginActivity.this,"验证码已发送");
					countDown();
				}else{
					StyleableToast.error(LoginActivity.this,object.get("info").getAsString());
				}
			}
			@Override
			public void taskFailed(int code) {
				dismissLoading();
			}
		});
		JsonObject object=new JsonObject();
		object.addProperty("timestamp",timestamp);
		object.addProperty("mobile",phone.getText().toString().trim());
		task.execute(UrlUtil.SEND_CODE,object.toString());
	}
	private void openLogin(final Map<String,String> params){
		HttpTask task=new HttpTask(this);
		final String name,iconurl,openid;
			name=params.get("name");
			iconurl=params.get("iconurl");
			openid=params.get("openid");
		task.setTaskHandler(new HttpTaskHandler() {
			@Override
			public void taskStart(int code) {

			}

			@Override
			public void taskSuccessful(String str, int code) {
				JsonObject object=new JsonParser().parse(str).getAsJsonObject();
				if(object.get("code").getAsInt()==CodeUtil.SUCCESS_CODE){
					if(object.has("data")){
						Gson gson=new Gson();
						JsonObject userJson = object.get("data").getAsJsonObject();
						PurchaserModel purchaser = gson.fromJson(userJson, PurchaserModel.class);
						UserUtil.setUserModel(LoginActivity.this, purchaser);
						getConfig();
					}else{
						dismissLoading();
						Intent intent=new Intent();
						intent.setClass(LoginActivity.this,BindMobileActivity.class);
						intent.putExtra("name",name);
						intent.putExtra("iconurl",iconurl);
						intent.putExtra("openid",openid);
						startActivity(intent);
					}
				}
			}
			@Override
			public void taskFailed(int code) {
				dismissLoading();
			}
		});
		JsonObject object=new JsonObject();
		object.addProperty("open_api",openid);
		task.execute(UrlUtil.OPEN_LOGIN,object.toString());
	}
	private void getConfig(){
		HttpTask task=new HttpTask(this);
		task.setTaskHandler(new HttpTask.HttpTaskHandler() {
			@Override
			public void taskStart(int code) {

			}
			@Override
			public void taskSuccessful(String str, int code) {
				dismissLoading();
				JsonObject selectResultJson = new JsonParser().parse(str)
						.getAsJsonObject();
				int selectResultCode = selectResultJson.get("code").getAsInt();
				if(selectResultCode== CodeUtil.SUCCESS_CODE){
					handleProduceSpecification(selectResultJson);
					sp.edit().putString("hot_search",selectResultJson.get("hot_search").getAsString()).commit();
					sp.edit().putString("unit",selectResultJson.get("unit").getAsString()).commit();
					Intent intent = new Intent();
					intent.putExtra("result",str);
					intent.setClass(LoginActivity.this, MainActivity.class);
					startActivity(intent);
					finish();
				}else{
					AlertDialog dialog=new AlertDialog(LoginActivity.this).builder();
					dialog.setTitle("提示");
					dialog.setMsg("初始化发生错误，请退出后重试");
					dialog.setCancelable(false);
					dialog.setPositiveButton("确定", new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							MyApplication.instance.exit();
						}
					});
					dialog.show();
				}
			}
			@Override
			public void taskFailed(int code) {
				dismissLoading();
				AlertDialog dialog=new AlertDialog(LoginActivity.this).builder();
				dialog.setTitle("提示");
				dialog.setMsg("初始化发生错误，请退出后重试");
				dialog.setCancelable(false);
				dialog.setPositiveButton("确定", new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						MyApplication.instance.exit();
					}
				});
				dialog.show();
			}
		});
		JsonObject object=new JsonObject();
		object.addProperty("purchaser_id",UserUtil.getUserModel(this)!=null?UserUtil.getUserModel(this).getId():0);
		object.addProperty("adcode",mylocation.getInt("code",0)!=0?mylocation.getInt("code",0):sp.getInt("code",0));
		object.addProperty("lat",Double.valueOf(sp.getString("lat","0")));
		object.addProperty("lng",Double.valueOf(sp.getString("lng","0")));
		task.execute(UrlUtil.GET_CONFIG,object.toString());
	}
	private void handleProduceSpecification(final JsonObject selectResultJson){
		int api_version=selectResultJson.get("api_version").getAsInt();
		int local_version=sp.getInt("local_version",-1);
		if(local_version>=api_version){
			return;
		}
		sp.edit().putInt("local_version",api_version).commit();
		new Thread(){
			@Override
			public void run() {
				ProduceTypesHelper.deleteProduceTypes(LoginActivity.this);
				JsonArray produce_types=selectResultJson.get("produces").getAsJsonArray();
				Gson gson=new Gson();
				List<MyProduceModel> produceTypes=gson.fromJson(produce_types,
						new TypeToken<List<MyProduceModel>>() {
						}.getType());
				ProduceTypesHelper.addProduceType(LoginActivity.this,produceTypes);
			}
		}.start();
	}
	private void login() {
		String username = usernameText.getText().toString();
		String password = passwordText.getText().toString();
		if (username == null || username.equals("")) {
			showTips("手机号不能为空");
			return;
		}
		if (password == null || password.equals("")) {
			showTips("密码不能为空");
			return;
		}
		JsonObject loginData = new JsonObject();
		loginData.addProperty("username", username);
		loginData.addProperty("password", password);

		HttpTask loginHttp = new HttpTask(this);
		loginHttp.setTaskHandler(this);
		
		loginHttp.execute(UrlUtil.LOGIN_URL, loginData.toString());
	}

	@Override
	public void afterTextChanged(Editable arg0) {
        if(arg0.toString().length()>0){
            change_pass_type.setVisibility(View.VISIBLE);
            forget_msg.setVisibility(View.GONE);
        }else{
            change_pass_type.setVisibility(View.GONE);
            forget_msg.setVisibility(View.VISIBLE);
        }
	}

	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
			int arg3) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		if ((!"".equals(usernameText.getEditableText().toString()))
				&& (!"".equals(passwordText.getEditableText().toString()))) {
			loginBn.setEnabled(true);
		} else {
			loginBn.setEnabled(false);
		}
	}

	@Override
	public void taskStart(int code) {
		showLoading();

	}

	@Override
	public void taskSuccessful(String str, int code) {

		JsonObject resultJson = new JsonParser().parse(str).getAsJsonObject();;
		int resultCode = resultJson.get("code").getAsInt();
		if (resultCode == CodeUtil.USERNAME_PASSWORD_ERR) {
            dismissLoading();
			showTips("用户名或密码错误");
		} else if (resultCode == CodeUtil.SUCCESS_CODE) {
			Gson gson = new Gson();
			JsonObject userJson = resultJson.get("data").getAsJsonObject();
			PurchaserModel purchaser = gson.fromJson(userJson, PurchaserModel.class); 
			UserUtil.setUserModel(this, purchaser);
			getConfig();
		}

	}

	@Override
	public void taskFailed(int code) {
		showToast(R.string.net_err);
	}

}
