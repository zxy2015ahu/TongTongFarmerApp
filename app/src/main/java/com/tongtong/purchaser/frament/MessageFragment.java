package com.tongtong.purchaser.frament;

/**
 * Created by zxy on 2018/4/7.
 */

import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.mobileim.contact.IYWContact;
import com.alibaba.mobileim.conversation.YWConversation;
import com.alibaba.mobileim.conversation.YWP2PConversationBody;
import com.alibaba.mobileim.lib.presenter.conversation.CustomConversation;
import com.alibaba.mobileim.lib.presenter.conversation.P2PConversation;
import com.alibaba.mobileim.utility.IMUtil;
import com.githang.statusbar.StatusBarTools;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;
import com.jude.easyrecyclerview.decoration.DividerDecoration;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.activity.BaseActivity;
import com.tongtong.purchaser.activity.ChattingActivity;
import com.tongtong.purchaser.activity.ConversationActivity;
import com.tongtong.purchaser.activity.LoginActivity;
import com.tongtong.purchaser.activity.MainActivity;
import com.tongtong.purchaser.activity.MessageActivity;
import com.tongtong.purchaser.adapter.MessageTypeAdapter;
import com.tongtong.purchaser.listener.NetChangeListener;
import com.tongtong.purchaser.model.MessageTypeModel;
import com.tongtong.purchaser.utils.CodeUtil;
import com.tongtong.purchaser.utils.Constant;
import com.tongtong.purchaser.utils.HttpTask;
import com.tongtong.purchaser.utils.NetUtil;
import com.tongtong.purchaser.utils.UrlUtil;
import com.tongtong.purchaser.utils.UserUtil;

import net.lucode.hackware.magicindicator.buildins.UIUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


/**
 * Created by zxy on 2018/4/1.
 */

public class MessageFragment extends BaseFrament implements View.OnClickListener,
        NetChangeListener,SwipeRefreshLayout.OnRefreshListener,HttpTask.HttpTaskHandler,RecyclerArrayAdapter.OnItemClickListener{
    private EasyRecyclerView recyclerView;
    private View nonetwork;
    private boolean is_error;
    private MessageTypeAdapter adapter;
    private SimpleDateFormat format;
    private static MessageFragment instance;
    public static MessageFragment getInstance(){
        return instance;
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.xiaoxi_fragment,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            RelativeLayout title_bar=(RelativeLayout) view.findViewById(R.id.title_bar);
            ViewGroup.LayoutParams params=title_bar.getLayoutParams();
            if(params!=null){
                if(params instanceof ViewGroup.MarginLayoutParams){
                    ViewGroup.MarginLayoutParams marginLayoutParams=(ViewGroup.MarginLayoutParams) title_bar.getLayoutParams();
                    marginLayoutParams.topMargin= StatusBarTools.getStatusBarHeight(getActivity());
                }
            }
        }
        instance=this;
        view.findViewById(R.id.back_bn).setVisibility(View.GONE);
        ((TextView)view.findViewById(R.id.title_text)).setText(R.string.xiaoxi);
        view.findViewById(R.id.right_bn).setOnClickListener(this);
        recyclerView=(EasyRecyclerView) view.findViewById(R.id.list);
        format=new SimpleDateFormat("M月d日 HH:mm:ss");
        nonetwork=view.findViewById(R.id.network);
        view.findViewById(R.id.shezhi).setOnClickListener(this);
        view.findViewById(R.id.retry_btn).setOnClickListener(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        DividerDecoration divider=new DividerDecoration(ContextCompat.getColor(getActivity(),R.color.aliwx_common_line_color), UIUtil.dip2px(getActivity(),0.5f),UIUtil.dip2px(getActivity(),10f),0);
        divider.setDrawLastItem(true);
        recyclerView.addItemDecoration(divider);
        adapter=new MessageTypeAdapter(getActivity());
        recyclerView.setAdapterWithProgress(adapter);
        recyclerView.setRefreshListener(this);
        adapter.setOnItemClickListener(this);
        recyclerView.setRefreshingColorResources(R.color.colorPrimary);
        BaseActivity.netChangeListeners.add(this);
        if(NetUtil.getNetWorkState(getActivity())>=0){
            loadData();
        }else{
            is_error=true;
            recyclerView.setVisibility(View.GONE);
            nonetwork.setVisibility(View.VISIBLE);
        }
    }

    public void notifyMsg(){
        for(int i=0;i<adapter.getCount();i++){
            if(adapter.getItem(i).getMsg_type()==4){
                List<YWConversation> conversations= UserUtil.getIMKitInstance(getActivity()).getConversationService().getConversationList();
                YWConversation temp=null;
                if(conversations!=null){
                    for(YWConversation conversation:conversations){
                        if(conversation.getUnreadCount()>0){
                            temp=conversation;
                            break;
                        }
                    }
                    if(temp!=null) {
                        adapter.getItem(i).setUnread_count(UserUtil.getIMKitInstance(getActivity()).getConversationService().getAllUnreadCount());
                        if (temp instanceof P2PConversation){
                        YWP2PConversationBody conversationBody = (YWP2PConversationBody) temp.getConversationBody();
                        String show_name = conversationBody.getContact().getShowName();
                        if (TextUtils.isEmpty(show_name)) {
                            IYWContact contact = UserUtil.getIMKitInstance(getActivity()).getContactService().getContactProfileInfo(conversationBody.getContact().getUserId(), conversationBody.getContact().getAppKey());
                            show_name = contact.getShowName();
                        }
                        if (TextUtils.isEmpty(show_name)) {
                            show_name = conversationBody.getContact().getUserId();
                        }
                        adapter.getItem(i).setContent(show_name + ":" + temp.getLatestContent());
                        adapter.getItem(i).setAdd_time(IMUtil.getFormatTime(temp.getLatestTimeInMillisecond(), UserUtil.getIMKitInstance(getActivity()).getIMCore().getServerTime()));
                        }else if(temp instanceof CustomConversation){
                            adapter.getItem(i).setAdd_time(IMUtil.getFormatTime(temp.getLatestTimeInMillisecond(), UserUtil.getIMKitInstance(getActivity()).getIMCore().getServerTime()));
                            adapter.getItem(i).setContent( temp.getLatestContent());
                        }
                        adapter.notifyDataSetChanged();
                    }else{
                        adapter.getItem(i).setUnread_count(UserUtil.getIMKitInstance(getActivity()).getConversationService().getAllUnreadCount());
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        }
    }

    @Override
    public void taskStart(int code) {

    }
    @Override
    public void taskFailed(int code) {
        recyclerView.setVisibility(View.GONE);
        nonetwork.setVisibility(View.VISIBLE);
        is_error=true;
    }

    @Override
    public void onItemClick(int position) {
        if(adapter.getItem(position).getMsg_type()!=4){
            Intent intent=new Intent();
            intent.setClass(getActivity(),MessageActivity.class);
            intent.putExtra("title",adapter.getItem(position).getTitle());
            intent.putExtra("msg_type",adapter.getItem(position).getMsg_type());
            startActivity(intent);
        }else{
            Intent chat = new Intent();
            chat.setClass(getActivity(),ConversationActivity.class);
            startActivity(chat);
        }
    }

    @Override
    public void onRefresh() {
        if(NetUtil.getNetWorkState(getActivity())>=0){
            loadData();
        }else{
            recyclerView.setRefreshing(false);
            is_error=true;
            recyclerView.setVisibility(View.GONE);
            nonetwork.setVisibility(View.VISIBLE);
        }
    }

    private void loadData(){
        if(NetUtil.getNetWorkState(getActivity())==-1){
            taskFailed(0);
            return;
        }
        //dividerItemDecoration.setDrawLastItem(false);
        HttpTask task=new HttpTask(getActivity());
        task.setTaskHandler(this);
        JsonObject dataJson = new JsonObject();
        dataJson.addProperty("token", UserUtil.getUserModel(getActivity()).getToken());
        dataJson.addProperty("purchaser_id", UserUtil.getUserModel(getActivity()).getId());
        task.execute(UrlUtil.GET_MSG_TYPE_LIST,dataJson.toString());
    }


    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.shezhi){
            Intent intent = null;
            /**
             * 判断手机系统的版本！如果API大于10 就是3.0+
             * 因为3.0以上的版本的设置和3.0以下的设置不一样，调用的方法不同
             */
            if (android.os.Build.VERSION.SDK_INT > 10) {
                intent = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
            } else {
                intent = new Intent();
                ComponentName component = new ComponentName(
                        "com.android.settings",
                        "com.android.settings.WirelessSettings");
                intent.setComponent(component);
                intent.setAction("android.intent.action.VIEW");
            }
            startActivity(intent);
        }else if(v.getId()==R.id.retry_btn){
            is_error = false;
            recyclerView.setVisibility(View.VISIBLE);
            nonetwork.setVisibility(View.GONE);
            loadData();
        }else if(v.getId()==R.id.right_bn){
            if(UserUtil.getUserModel(getActivity())==null){
                Intent intent=new Intent();
                intent.setClass(getActivity(), LoginActivity.class);
                startActivity(intent);
                return;
            }
            Intent intent=new Intent();
            intent.putExtra(ChattingActivity.TARGET_ID, Constant.SERVER_ACCOUNT);
            intent.putExtra(ChattingActivity.TARGET_ESERVICE,ChattingActivity.TARGET_ESERVICE);
            intent.setClass(getActivity(),ChattingActivity.class);
            startActivity(intent);
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        BaseActivity.netChangeListeners.remove(this);
    }
    @Override
    public void onnetChange(boolean isAvalable) {
        if(isAvalable){
            if(is_error){
                is_error=false;
                nonetwork.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                loadData();
            }

        }
    }
    @Override
    public void taskSuccessful(String str, int code) {
        is_error=false;
        JsonObject selectResultJson = new JsonParser().parse(str)
                .getAsJsonObject();
        int selectResultCode = selectResultJson.get("code").getAsInt();
        if(selectResultCode== CodeUtil.SUCCESS_CODE){
            recyclerView.setRefreshing(false);
            adapter.clear();
            JsonArray orders=selectResultJson.get("items").getAsJsonArray();
            int count=0;
            for(int i=0;i<orders.size();i++){
                JsonObject order=orders.get(i).getAsJsonObject();
                MessageTypeModel am=new MessageTypeModel();
                am.setTitle(order.get("title").getAsString());
                am.setIcon(order.get("icon").getAsString());
                am.setMsg_type(order.get("msg_type").getAsInt());
                if(am.getMsg_type()!=4){
                    try{
                        am.setAdd_time(IMUtil.getFormatTime(format.parse(order.get("add_time").getAsString()).getTime()*1000, UserUtil.getIMKitInstance(getActivity()).getIMCore().getServerTime()));
                    }catch (Exception e){

                    }
                    am.setContent(order.get("content").getAsString());
                    am.setUnread_count(order.get("unread_count").getAsInt());
                    count+=am.getUnread_count();
                }else{
                    List<YWConversation> conversations=UserUtil.getIMKitInstance(getActivity()).getConversationService().getConversationList();
                    YWConversation temp=null;
                    if(conversations!=null){
                        for(YWConversation conversation:conversations){
                            if(conversation.getUnreadCount()>0){
                                temp=conversation;
                                break;
                            }
                        }
                        if(temp!=null){
                            am.setUnread_count(UserUtil.getIMKitInstance(getActivity()).getConversationService().getAllUnreadCount());
                            if(temp instanceof P2PConversation) {
                                YWP2PConversationBody conversationBody = (YWP2PConversationBody) temp.getConversationBody();
                                String show_name = conversationBody.getContact().getShowName();
                                if (TextUtils.isEmpty(show_name)) {
                                    IYWContact contact = UserUtil.getIMKitInstance(getActivity()).getContactService().getContactProfileInfo(conversationBody.getContact().getUserId(), conversationBody.getContact().getAppKey());
                                    show_name = contact.getShowName();
                                }
                                if (TextUtils.isEmpty(show_name)) {
                                    show_name = conversationBody.getContact().getUserId();
                                }
                                am.setContent(show_name + ":" + temp.getLatestContent());
                                am.setAdd_time(IMUtil.getFormatTime(temp.getLatestTimeInMillisecond(), UserUtil.getIMKitInstance(getActivity()).getIMCore().getServerTime()));
                            }else if(temp instanceof CustomConversation){
                                CustomConversation sys=(CustomConversation)temp;
                                am.setAdd_time(IMUtil.getFormatTime(temp.getLatestTimeInMillisecond(), UserUtil.getIMKitInstance(getActivity()).getIMCore().getServerTime()));
                                am.setContent( temp.getLatestContent());
                            }
                        }
                    }
                }
                adapter.add(am);
            }
            if(adapter.getCount()>0){
                recyclerView.showRecycler();
            }else if(adapter.getCount()==0){
                recyclerView.showEmpty();
            }
            ((MainActivity)getActivity()).setCount(count);
        }
    }
}

