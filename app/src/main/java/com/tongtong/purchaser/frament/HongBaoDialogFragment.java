package com.tongtong.purchaser.frament;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.tu.loadingdialog.LoadingDailog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.activity.HongBaoDetailsActivity;
import com.tongtong.purchaser.activity.RobRpActivity;
import com.tongtong.purchaser.utils.CodeUtil;
import com.tongtong.purchaser.utils.HttpTask;
import com.tongtong.purchaser.utils.NetUtil;
import com.tongtong.purchaser.utils.UrlUtil;
import com.tongtong.purchaser.utils.UserUtil;
import com.tongtong.purchaser.widget.StyleableToast;

/**
 * Created by zxy on 2018/3/9.
 */

public class HongBaoDialogFragment extends DialogFragment {

    private AnimationDrawable animationDrawable;
    private LoadingDailog loading;
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog=new Dialog(getActivity(),R.style.alert_dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.open_rp_packet);
        dialog.findViewById(R.id.iv_common_closed).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        final ImageView iv_avatar=(ImageView) dialog.findViewById(R.id.iv_avatar);
        Glide.with(getActivity()).load(NetUtil.getFullUrl(getArguments().getString("headUrl")))
                .asBitmap().centerCrop().into(new BitmapImageViewTarget(iv_avatar) {
            @Override
            protected void setResource(Bitmap resource) {
                RoundedBitmapDrawable circularBitmapDrawable =
                        RoundedBitmapDrawableFactory.create(getResources(), resource);
                circularBitmapDrawable.setCircular(true);
                iv_avatar.setImageDrawable(circularBitmapDrawable);
            }
        });
        ((TextView)dialog.findViewById(R.id.tv_username)).setText(getArguments().getString("name"));
        ((TextView)dialog.findViewById(R.id.tv_greeting)).setText(getArguments().getString("comment"));
        dialog.findViewById(R.id.tv_check_lucky).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                opendetails(getArguments().getInt("id"));
            }
        });
        final ImageButton btn_open_money=(ImageButton)dialog.findViewById(R.id.btn_open_money);
        btn_open_money.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_open_money.setBackgroundResource(R.drawable.open_red_animation_drawable);
                animationDrawable = (AnimationDrawable)btn_open_money.getBackground();
                robrpbyfarmer();
            }
        });
        Window window=dialog.getWindow();
        WindowManager.LayoutParams attributes=window.getAttributes();
        DisplayMetrics dm=new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        attributes.height=dm.heightPixels*2/3;
        window.setAttributes(attributes);
        return dialog;
    }
    private void opendetails(int id){
        HttpTask task=new HttpTask(getActivity());
        task.setTaskHandler(new HttpTask.HttpTaskHandler() {
            @Override
            public void taskStart(int code) {
                LoadingDailog.Builder loadBuilder=new LoadingDailog.Builder(getActivity())
                        .setMessage("加载中...")
                        .setCancelable(true)
                        .setCancelOutside(true);
                loading=loadBuilder.create();
                loading.show();
            }
            @Override
            public void taskSuccessful(String str, int code) {
                loading.dismiss();
                Intent intent=new Intent();
                intent.setClass(RobRpActivity.getInstance(), HongBaoDetailsActivity.class);
                intent.putExtra("result",str);
                startActivity(intent);
            }
            @Override
            public void taskFailed(int code) {
                loading.dismiss();
                StyleableToast.error(RobRpActivity.getInstance(),"打开详情失败");
            }
        });
        JsonObject object=new JsonObject();
        object.addProperty("token", UserUtil.getUserModel(RobRpActivity.getInstance()).getToken());
        object.addProperty("purchaser_id",UserUtil.getUserModel(RobRpActivity.getInstance()).getId());
        object.addProperty("id",id);
        task.execute(UrlUtil.ROB_RP,object.toString());
    }
    private void robrpbyfarmer(){
        HttpTask task=new HttpTask(getActivity());
        task.setTaskHandler(new HttpTask.HttpTaskHandler() {
            @Override
            public void taskStart(int code) {
                animationDrawable.start();
            }
            @Override
            public void taskSuccessful(String str, int code) {
                dismiss();
                animationDrawable.stop();
                RobRpActivity.getInstance().updateMarker();
                JsonObject selectResultJson = new JsonParser().parse(str)
                        .getAsJsonObject();
                int selectResultCode = selectResultJson.get("code").getAsInt();
                if(selectResultCode== CodeUtil.SUCCESS_CODE){
                    Intent intent=new Intent();
                    intent.setClass(RobRpActivity.getInstance(), HongBaoDetailsActivity.class);
                    intent.putExtra("result",str);
                    startActivity(intent);
                }else if(selectResultCode==104){
                    //已抢完
                    StyleableToast.info(getActivity(),"来晚一步，红包已经被抢完");
                    Intent intent=new Intent();
                    intent.setClass(RobRpActivity.getInstance(), HongBaoDetailsActivity.class);
                    intent.putExtra("result",str);
                    startActivity(intent);
                }
            }
            @Override
            public void taskFailed(int code) {
                animationDrawable.stop();
                dismiss();
                StyleableToast.error(RobRpActivity.getInstance(),"打开红包失败！");
            }
        });
        JsonObject object=new JsonObject();
        object.addProperty("token", UserUtil.getUserModel(getActivity()).getToken());
        object.addProperty("purchaser_id",UserUtil.getUserModel(getActivity()).getId());
        object.addProperty("id",getArguments().getInt("id"));
        object.addProperty("lat",getArguments().getDouble("lat"));
        object.addProperty("lng",getArguments().getDouble("lng"));
        task.execute(UrlUtil.ROB_RPS,object.toString());
    }
}
