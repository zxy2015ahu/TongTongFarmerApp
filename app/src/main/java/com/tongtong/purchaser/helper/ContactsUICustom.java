package com.tongtong.purchaser.helper;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.mobileim.aop.Pointcut;
import com.alibaba.mobileim.aop.custom.IMContactsUI;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.activity.FindContactActivity;


/**
 * 联系人界面UI的定制点(根据需要实现相应的接口来达到联系人界面)，不设置则使用云旺默认的实现
 * 调用方设置的回调，必须继承BaseAdvice 根据不同的需求实现 不同的 开放的 Advice
 * com.alibaba.mobileim.aop.pointcuts包下开放了不同的Advice.通过实现多个接口，组合成对不同的ui界面的定制
需要在application中将这个Advice绑定。设置以下代码
 * AdviceBinder.bindAdvice(PointCutEnum.CONTACTS_UI_POINTCUT, ContactsUICustomSample.class);
 *
 * @author shuheng
 */
public class ContactsUICustom extends IMContactsUI {

    public ContactsUICustom(Pointcut pointcut) {
        super(pointcut);
    }

    /**
     * 返回联系人自定义标题
     *
     * @param fragment
     * @param context
     * @param inflater
     * @return
     */
    @Override
    public View getCustomTitle(final Fragment fragment, final Context context, final LayoutInflater inflater) {
        //TODO 重要：必须以该形式初始化customView---［inflate(R.layout.**, new RelativeLayout(context),false)］------，以让inflater知道父布局的类型，否则布局xml**中定义的高度和宽度无效，均被默认的wrap_content替代
        View customView =  inflater
                .inflate(R.layout.contact_head_item, new RelativeLayout(context), false);
        TextView title = (TextView) customView.findViewById(R.id.title_text);
        title.setText("我的联系人");
        View backButton = customView.findViewById(R.id.back_bn);
        backButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                fragment.getActivity().finish();
            }
        });
        View rightButton =customView.findViewById(R.id.right_bn);
        rightButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setClass(fragment.getActivity(), FindContactActivity.class);
                fragment.startActivity(intent);
            }
        });
        return customView;
    }
}
