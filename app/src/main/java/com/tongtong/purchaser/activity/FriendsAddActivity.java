package com.tongtong.purchaser.activity;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.mobileim.channel.event.IWxCallback;
import com.alibaba.mobileim.contact.IYWContact;
import com.alibaba.mobileim.contact.IYWContactService;
import com.alibaba.mobileim.conversation.IYWMessageListener;
import com.alibaba.mobileim.conversation.YWConversation;
import com.alibaba.mobileim.conversation.YWMessage;
import com.alibaba.mobileim.gingko.presenter.contact.IContactProfileUpdateListener;
import com.alibaba.mobileim.kit.common.IMUtility;
import com.alibaba.mobileim.kit.common.YWAsyncBaseAdapter;
import com.alibaba.mobileim.lib.model.message.YWSystemMessage;
import com.bumptech.glide.Glide;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.utils.Constant;
import com.tongtong.purchaser.utils.UserUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018-05-17.
 */

public class FriendsAddActivity extends BaseActivity implements View.OnClickListener{
    private ListView list;
    private YWConversation mConversation;
    private MyAdapter adapter;
    private static final int PAGE_SIZE=1000;
    private List<YWMessage> mMessageList;
    private Handler mHandler=new Handler();
    private IContactProfileUpdateListener iContactProfileUpdateListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friends_add_layout);
        ImageView phone=(ImageView) findViewById(R.id.phone);
        phone.setImageResource(R.drawable.vector_drawable_clear);
        findViewById(R.id.right_bn).setOnClickListener(this);
        ((TextView)findViewById(R.id.title_text)).setText("好友请求");
        findViewById(R.id.back_bn).setOnClickListener(this);
        list=(ListView) findViewById(R.id.list);
        mConversation= UserUtil.getIMKitInstance(this).getConversationService().getCustomConversationByConversationId(Constant.ADD_CONTACT_TAG);
        mMessageList=new ArrayList<>();
        adapter=new MyAdapter();
        mConversation.getMessageLoader().loadMessage(PAGE_SIZE, new IWxCallback() {
            @Override
            public void onSuccess(Object... objects) {
                if(objects!=null&&objects.length>0){
                    List<YWMessage> msgs=(List<YWMessage>)objects[0];
                    mMessageList=msgs;
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(int i, String s) {

            }

            @Override
            public void onProgress(int i) {

            }
        });

        list.setAdapter(adapter);
        //UserUtil.getIMKitInstance(this).getConversationService().markReaded(mConversation);
        mConversation.getMessageLoader().addMessageListener(mMessageListener);
        iContactProfileUpdateListener = new IContactProfileUpdateListener() {
            @Override
            public void onProfileUpdate() {

            }

            @Override
            public void onProfileUpdate(String userid, String appkey) {
                refreshAdapter();
            }
        };
        UserUtil.getIMKitInstance(this).getConversationService().markReaded(mConversation);
        UserUtil.getIMKitInstance(this).getContactService().addProfileUpdateListener(iContactProfileUpdateListener);
    }
    private IYWContactService getContactService(){
        return UserUtil.getIMKitInstance(this).getContactService();
    }
    private void acceptToBecomeFriend(final YWMessage message) {
        final YWSystemMessage msg = (YWSystemMessage) message;
        if (getContactService() != null) {
            getContactService().ackAddContact(message.getAuthorUserId(),message.getAuthorAppkey(),true,"",new IWxCallback() {
                @Override
                public void onSuccess(Object... result) {
                    msg.setSubType(YWSystemMessage.SYSMSG_TYPE_AGREE);
                    refreshAdapter();
                    getContactService().updateContactSystemMessage(msg);
                }

                @Override
                public void onError(int code, String info) {

                }

                @Override
                public void onProgress(int progress) {

                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mConversation.getMessageLoader().removeMessageListener(mMessageListener);
    }

    private void refreshAdapter(){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mConversation.getMessageLoader().loadMessage(PAGE_SIZE, new IWxCallback() {
                    @Override
                    public void onSuccess(Object... objects) {
                        if(objects!=null&&objects.length>0){
                            List<YWMessage> msgs=(List<YWMessage>)objects[0];
                            mMessageList=msgs;
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onError(int i, String s) {

                    }

                    @Override
                    public void onProgress(int i) {

                    }
                });
            }
        });
    }
    IYWMessageListener mMessageListener = new IYWMessageListener() {
        @Override
        public void onItemUpdated() {  //消息列表变更，例如删除一条消息，修改消息状态，加载更多消息等等
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyDataSetChangedWithAsyncLoad();
                }
            });
        }

        @Override
        public void onItemComing() { //收到新消息
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyDataSetChangedWithAsyncLoad();
                        UserUtil.getIMKitInstance(FriendsAddActivity.this).getConversationService().markReaded(mConversation);
                }
            });
        }

        @Override
        public void onInputStatus(byte status) {

        }
    };
    private  class MyAdapter extends YWAsyncBaseAdapter{
        @Override
        public int getCount() {
            return mMessageList.size();
        }

        @Override
        public Object getItem(int position) {
            return mMessageList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView==null){
                convertView=getLayoutInflater().inflate(R.layout.friends_item,null);
            }
            ImageView head_img=(ImageView) convertView.findViewById(R.id.head_img);
           TextView name=(TextView) convertView.findViewById(R.id.name);
            TextView beizhu=(TextView) convertView.findViewById(R.id.beizhu);
            TextView result=(TextView) convertView.findViewById(R.id.receive_state);
            Button tongyi=(Button) convertView.findViewById(R.id.tongyi);
            final YWMessage message=(YWMessage) getItem(position);
            final YWSystemMessage systemMessage=(YWSystemMessage) message;
            String authorUserId = message.getAuthorUserId();
            IYWContact contact = IMUtility.getContactProfileInfo(UserUtil.getIMKitInstance(FriendsAddActivity.this).getUserContext(), message.getAuthorUserId(), message.getAuthorAppkey());
            if(contact!=null){
                name.setText(contact.getShowName());
                Glide.with(FriendsAddActivity.this).load(contact.getAvatarPath())
                        .centerCrop().placeholder(R.drawable.no_icon).into(head_img);
            }else{
                name.setText(authorUserId);
            }
            beizhu.setText(message.getMessageBody().getContent());
            if(systemMessage.isAccepted()){
                tongyi.setVisibility(View.GONE);
                result.setVisibility(View.VISIBLE);
            }else{
                result.setVisibility(View.GONE);
                tongyi.setVisibility(View.VISIBLE);
                tongyi.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        acceptToBecomeFriend(message);
                    }
                });
            }
            return convertView;
        }

        @Override
        public void loadAsyncTask() {
            mConversation.getMessageLoader().loadMessage(PAGE_SIZE, new IWxCallback() {
                @Override
                public void onSuccess(Object... objects) {
                    if(objects!=null&&objects.length>0){
                        List<YWMessage> msgs=(List<YWMessage>)objects[0];
                        mMessageList=msgs;
                        adapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onError(int i, String s) {

                }

                @Override
                public void onProgress(int i) {

                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.right_bn){

        }else if(v.getId()==R.id.back_bn){
            onBackPressed();
        }
    }
}
