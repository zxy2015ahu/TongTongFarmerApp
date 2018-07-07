package com.tongtong.purchaser.frament;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.tu.loadingdialog.LoadingDailog;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.utils.HttpTask;
import com.tongtong.purchaser.utils.UrlUtil;
import com.tongtong.purchaser.widget.BandCardEditText;


/**
 * Created by zxy on 2018/3/8.
 */

public class BandCardFragment extends BaseFrament implements TextWatcher,View.OnClickListener{
    private BandCardEditText ed_bankcard_number;
    private TextView tv_bank_name;
    private ImageButton scan;
    private LoadingDailog loading;
    private static final int REQUEST_CODE_BANKCARD = 111;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.binding_card_layout,container,false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ed_bankcard_number=(BandCardEditText)view.findViewById(R.id.ed_bankcard_number);
        tv_bank_name=(TextView) view.findViewById(R.id.tv_bank_name);
        ed_bankcard_number.addTextChangedListener(this);
        scan=(ImageButton) view.findViewById(R.id.scan);
        scan.setOnClickListener(this);
        initOCRSDK();
    }
    private void initOCRSDK() {
//        OCR.getInstance().initAccessToken(new OnResultListener<AccessToken>() {
//            @Override
//            public void onResult(AccessToken result) {
//                // 调用成功，返回AccessToken对象
//
//            }
//            @Override
//            public void onError(OCRError error) {
//                // 调用失败，返回OCRError子类SDKError对象
//            }
//        }, getActivity().getApplicationContext());
    }
    private void checkCard(){
        HttpTask task=new HttpTask(getActivity());
        task.setTaskHandler(new HttpTask.HttpTaskHandler() {
            @Override
            public void taskStart(int code) {

            }

            @Override
            public void taskSuccessful(String str, int code) {
                final JsonObject selectResultJson = new JsonParser().parse(str)
                        .getAsJsonObject();
                String respCd = selectResultJson.get("respCd").getAsString();
                if("0000".equals(respCd)){
                    tv_bank_name.setVisibility(View.VISIBLE);
                    tv_bank_name.setText(selectResultJson.get("data").getAsJsonObject().get("issNm").getAsString());
                }else{
                    tv_bank_name.setVisibility(View.GONE);
                    tv_bank_name.setText("");
                }
            }
            @Override
            public void taskFailed(int code) {

            }
        });
        JsonObject object=new JsonObject();
        object.addProperty("cardNo",ed_bankcard_number.getBankCardText());
        task.execute(UrlUtil.GET_CARD_INFO,object.toString());
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.scan){
//            AccessToken accessToken = OCR.getInstance().getAccessToken();
//            if (accessToken == null || TextUtils.isEmpty(accessToken.getAccessToken())) {
//                initOCRSDK();
//                StyleableToast.info(getActivity(),"OCR token 正在拉取，请稍后再试 ");
//                return;
//            }
//            Intent intent = new Intent(getActivity(), CameraActivity.class);
//            intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH,
//                    FileUtil.getSaveFile(getActivity().getApplication()).getAbsolutePath());
//            intent.putExtra(CameraActivity.KEY_CONTENT_TYPE,
//                    CameraActivity.CONTENT_TYPE_BANK_CARD);
//            startActivityForResult(intent, REQUEST_CODE_BANKCARD);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_BANKCARD && resultCode == Activity.RESULT_OK) {
            if (data != null) {
//                String contentType = data.getStringExtra(CameraActivity.KEY_CONTENT_TYPE);
//                String filePath = FileUtil.getSaveFile(getActivity().getApplicationContext()).getAbsolutePath();
//                if (!TextUtils.isEmpty(contentType)) {
//                    if (CameraActivity.CONTENT_TYPE_BANK_CARD.equals(contentType)) {
//                        recIDCard(filePath);
//                    }
//                }
            }
        }
    }
    private void recIDCard(String filePath){
//        LoadingDailog.Builder loadBuilder=new LoadingDailog.Builder(getActivity())
//                .setMessage("识别中...")
//                .setCancelable(true)
//                .setCancelOutside(true);
//        loading=loadBuilder.create();
//        loading.show();
//        BankCardParams param = new BankCardParams();
//        param.setImageFile(new File(filePath));
//        OCR.getInstance().recognizeBankCard(param, new OnResultListener<BankCardResult>() {
//            @Override
//            public void onResult(BankCardResult result) {
//                loading.dismiss();
//                if (result != null&&result.getBankCardType()!= BankCardResult.BankCardType.Unknown) {
//                    StyleableToast.success(getActivity(),"识别成功");
//                    ed_bankcard_number.setText(result.getBankCardNumber());
//                    tv_bank_name.setVisibility(View.VISIBLE);
//                    tv_bank_name.setText(result.getBankName()+"\t"+(result.getBankCardType()== BankCardResult.BankCardType.Debit?"借记卡":"信用卡"));
//                }else{
//                    StyleableToast.error(getActivity(),"识别失败");
//                }
//            }
//            @Override
//            public void onError(OCRError error) {
//                loading.dismiss();
//                StyleableToast.error(getActivity(),"识别出错");
//            }
//        });
    }
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if(s.toString().trim().length()>15){
            if(TextUtils.isEmpty(tv_bank_name.getText())){
                checkCard();
            }
        }else{
            tv_bank_name.setVisibility(View.GONE);
            tv_bank_name.setText("");
        }
    }
}
