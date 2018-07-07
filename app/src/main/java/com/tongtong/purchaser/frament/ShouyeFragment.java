package com.tongtong.purchaser.frament;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.githang.statusbar.StatusBarTools;
import com.gongwen.marqueen.MarqueeFactory;
import com.gongwen.marqueen.SimpleMF;
import com.gongwen.marqueen.SimpleMarqueeView;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.guanaj.easyswipemenulibrary.EasySwipeMenuLayout;
import com.guanaj.easyswipemenulibrary.State;
import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.activity.FarmerInfoActivity;
import com.tongtong.purchaser.activity.PlayWebViewActivity;
import com.tongtong.purchaser.activity.TypeSelectActivity;
import com.tongtong.purchaser.adapter.PublishAdapter;
import com.tongtong.purchaser.model.FarmerModel;
import com.tongtong.purchaser.model.MenuModel;
import com.tongtong.purchaser.model.ReleaseModel;
import com.tongtong.purchaser.utils.CodeUtil;
import com.tongtong.purchaser.utils.HttpTask;
import com.tongtong.purchaser.utils.NetUtil;
import com.tongtong.purchaser.utils.UrlUtil;
import com.tongtong.purchaser.utils.UserUtil;
import com.tongtong.purchaser.view.Mydivider;
import com.tongtong.purchaser.widget.NoScrollGridView;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.listener.OnBannerListener;
import com.youth.banner.loader.ImageLoader;

import net.lucode.hackware.magicindicator.buildins.UIUtil;

import org.xml.sax.XMLReader;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by zxy on 2018/4/7.
 */

public class ShouyeFragment extends BaseFrament implements RecyclerArrayAdapter.OnLoadMoreListener,
        HttpTask.HttpTaskHandler,SwipeRefreshLayout.OnRefreshListener{
    private EasyRecyclerView recyclerView;
    private PublishAdapter adapter;
    private DisplayMetrics dm;
    private List<String> imgs,titles;
    private List<String> links;
    private Banner banner;
    private TextView tv_city_name;
    private int adcode;
    private int start_page;
    private boolean is_error;
    private RecyclerArrayAdapter.ItemView list_empty;
    private SimpleMarqueeView marqueeView;
    private int width;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.shouye_fragment,container,false);
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
        recyclerView=(EasyRecyclerView) view.findViewById(R.id.list);
        ((TextView)view.findViewById(R.id.title_text)).setText("发现");
        Mydivider mydivider=new Mydivider(ContextCompat.getColor(getActivity(),R.color.aliwx_divider_color),1);
        mydivider.setDrawHeaderFooter(false);
        mydivider.setDrawLastItem(true);
        recyclerView.addItemDecoration(mydivider);
        recyclerView.setRefreshingColorResources(R.color.colorPrimary);
        tv_city_name=(TextView) view.findViewById(R.id.tv_city_name);
        LinearLayoutManager manager=new LinearLayoutManager(getActivity());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
        recyclerView.setRefreshListener(this);
        adapter=new PublishAdapter(getActivity());
        adapter.setNoMore(R.layout.no_more_layout);
        adapter.setMore(R.layout.autolistview_footer,this);
        adapter.setError(R.layout.error_layout, new RecyclerArrayAdapter.OnErrorListener() {
            @Override
            public void onErrorShow() {
                adapter.resumeMore();
            }
            @Override
            public void onErrorClick() {
                adapter.resumeMore();
            }
        });
        recyclerView.setAdapter(adapter);
        dm=new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        width=(dm.widthPixels-UIUtil.dip2px(getActivity(),8f)-24)/3;
        imgs=new ArrayList<>();
        titles=new ArrayList<>();
        links=new ArrayList<>();
        JsonObject selectResultJson = new JsonParser().parse(getActivity().getIntent().getStringExtra("result"))
                .getAsJsonObject();
        JsonArray banners=selectResultJson.get("banners").getAsJsonArray();
        JsonArray bannerString=selectResultJson.get("bannerString").getAsJsonArray();
        for(int i=0;i<banners.size();i++){
            JsonObject banner=banners.get(i).getAsJsonObject();
            imgs.add(NetUtil.getFullUrl(banner.get("src").getAsString()));
            titles.add(banner.get("title").getAsString());
            links.add(banner.get("link").getAsString());
        }
        initBanner();
        initMarqueenView(bannerString);
        List<VHItem> its=new ArrayList<>();
        JsonArray bannerItems=selectResultJson.get("bannerItems").getAsJsonArray();
        for(int i=0;i<bannerItems.size();i++){
            JsonObject bi=bannerItems.get(i).getAsJsonObject();
            VHItem vhItem=new VHItem();
            vhItem.image_url=bi.get("image_url").getAsString();
            vhItem.url=bi.get("url").getAsString();
            its.add(vhItem);
        }
        addBannerItem(its);
        addItemTitle(R.drawable.vector_drawable_recmend, "我的推荐", new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        JsonArray farmer_release=selectResultJson.get("farmer_release").getAsJsonArray();
        int next_page=selectResultJson.get("next_page").getAsInt();
        if(farmer_release.size()>0){
            Gson gson=new Gson();
            List<ReleaseModel> items = gson.fromJson(farmer_release,
                    new TypeToken<List<ReleaseModel>>() {
                    }.getType());
            adapter.addAll(items);
            if(next_page==0){
                adapter.stopMore();
            }
        }else{
            addlistempty();
        }

    }

    private void getMarqueenString(){
        HttpTask task=new HttpTask(getActivity());
        task.setTaskHandler(new HttpTask.HttpTaskHandler() {
            @Override
            public void taskStart(int code) {

            }
            @Override
            public void taskSuccessful(String str, int code) {
                recyclerView.setRefreshing(false);
                JsonObject selectResultJson = new JsonParser().parse(str)
                        .getAsJsonObject();
                JsonArray bannerString=selectResultJson.get("bannerString").getAsJsonArray();
                initMarqueenData(bannerString);
            }
            @Override
            public void taskFailed(int code) {
                recyclerView.setRefreshing(false);
            }
        });
        task.execute(UrlUtil.GET_MARQUEEN);
    }
    @Override
    public void onRefresh() {
        recyclerView.setRefreshing(true);
        getMarqueenString();
    }
    @Override
    public void taskFailed(int code) {
        if(start_page>0){
            adapter.pauseMore();
        }
        is_error=true;
    }

    @Override
    public void taskStart(int code) {

    }
    @Override
    public void taskSuccessful(String str, int code) {
        JsonObject refreshResultJson = new JsonParser().parse(str)
                .getAsJsonObject();
        int refreshResultCode = refreshResultJson.get("code").getAsInt();
        if(start_page==0){
            adapter.clear();
        }
        if(refreshResultCode== CodeUtil.SUCCESS_CODE){
            Gson gson = new Gson();
            JsonArray releaseInfosJson = refreshResultJson.get("farmer_release")
                    .getAsJsonArray();
            List<ReleaseModel> items = gson.fromJson(releaseInfosJson,
                    new TypeToken<List<ReleaseModel>>() {
                    }.getType());
            if(start_page==0&&items.size()==0){
                adapter.removeHeader(list_empty);
                addlistempty();
                return;
            }else if(start_page==0){
                adapter.removeHeader(list_empty);
            }
            adapter.addAll(items);
            int next_page=refreshResultJson.get("next_page").getAsInt();
            if(next_page==0){
                adapter.stopMore();
            }
        }
    }
    private void loadData(){
        if(NetUtil.getNetWorkState(getActivity())==-1){
            taskFailed(0);
            return;
        }
        JsonObject dataJson = new JsonObject();
        dataJson.addProperty("purchaser_id", UserUtil.getUserModel(getActivity()).getId());
        dataJson.addProperty("adcode",adcode);
        dataJson.addProperty("start_page",start_page);
        HttpTask httpTask = new HttpTask(getActivity());
        httpTask.setTaskHandler(this);
        httpTask.execute(UrlUtil.GET_RECMEND_DATA, dataJson.toString());
    }
    @Override
    public void onLoadMore() {
        if(!is_error){
            start_page++;
        }
        loadData();
    }

    private void addlistempty(){
        if(list_empty==null){
            list_empty=new RecyclerArrayAdapter.ItemView() {
                @Override
                public View onCreateView(ViewGroup parent) {
                    View view=View.inflate(getActivity(),R.layout.shouye_list_empty,null);
                    return view;
                }

                @Override
                public void onBindView(android.view.View headerView) {

                }
            };
        }
        adapter.addHeader(list_empty);
    }

    private class VHItem{
        private String url,image_url;
    }
    private class MyViewHolder extends RecyclerView.ViewHolder{
        public ImageView image;
        public MyViewHolder(View itemView){
            super(itemView);
            image=(ImageView) itemView.findViewById(R.id.image_item);
        }
    }


    private void addBannerItem(final List<VHItem> items){
        adapter.addHeader(new RecyclerArrayAdapter.ItemView() {
            @Override
            public View onCreateView(ViewGroup parent) {
                View header=getActivity().getLayoutInflater().inflate(R.layout.banner_item,null);
                header.setLayoutParams(new EasyRecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UIUtil.dip2px(getActivity(),70f)));
                LinearLayout rv=(LinearLayout) header.findViewById(R.id.root);
                for(int i=0;i<items.size();i++){
                    VHItem it=items.get(i);
                    ImageView iv=new ImageView(getActivity());
                    LinearLayout.LayoutParams rp=null;
                    if(i>0){
                        rp=new LinearLayout.LayoutParams(width,width*9/16);
                        rp.leftMargin=12;
                    }else{
                        rp=new LinearLayout.LayoutParams(width,width*9/16);
                    }
                    iv.setLayoutParams(rp);
                    iv.setScaleType(ImageView.ScaleType.FIT_XY);
                    Glide.with(getActivity()).load(NetUtil.getFullUrl(it.image_url)).centerCrop().into(iv);
                    rv.addView(iv);
                }
                return header;
            }

            @Override
            public void onBindView(View headerView) {

            }
        });
    }


    private void addHeaderLine(){
        adapter.addHeader(new RecyclerArrayAdapter.ItemView() {
            @Override
            public View onCreateView(ViewGroup parent) {
                View header=View.inflate(getActivity(),R.layout.header,null);
                header.setLayoutParams(new EasyRecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UIUtil.dip2px(getActivity(),10f)));
                return header;
            }

            @Override
            public void onBindView(View headerView) {

            }
        });
    }
    private void addItemTitle(final int resId, final String title, final View.OnClickListener clickListener){
        adapter.addHeader(new RecyclerArrayAdapter.ItemView() {
            @Override
            public View onCreateView(ViewGroup parent) {
                View view=View.inflate(getActivity(),R.layout.shhouye_item_title,null);
                ((ImageView)view.findViewById(R.id.img)).setImageResource(resId);
                ((TextView)view.findViewById(R.id.title)).setText(title);
                TextView more=(TextView) view.findViewById(R.id.more);
                if(clickListener!=null){
                    more.setVisibility(View.VISIBLE);
                    more.setOnClickListener(clickListener);
                }
                view.measure(0,0);
                view.setLayoutParams(new EasyRecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, view.getMeasuredHeight()));
                return view;
            }

            @Override
            public void onBindView(View headerView) {

            }
        });
    }

    private void setClick(MarqueeFactory factory){
        try{
            Method method=factory.getClass().getSuperclass().getDeclaredMethod("getMarqueeViews");
            method.setAccessible(true);
            List<TextView> textViews=(List<TextView>)method.invoke(factory);
            for(TextView textView:textViews){
                textView.setMovementMethod(LinkMovementMethod.getInstance());
            }
        }catch (Exception e){
            e.printStackTrace();

        }
    }
    private void initMarqueenData(JsonArray bannerString){
        Spanned[] bs=new Spanned[1];
        bs[0]= Html.fromHtml("暂时没有平台动态");
        if(bannerString.size()>0){
            bs=new Spanned[bannerString.size()];
            for(int i=0;i<bannerString.size();i++){
                JsonObject ban=bannerString.get(i).getAsJsonObject();
                final int farmer_id=ban.get("farmer_id").getAsInt();
                final int purchaser_id=ban.get("purchaser_id").getAsInt();
                final String farmer_name=ban.get("farmer_name").getAsString();
                bs[i]= Html.fromHtml(ban.get("content").getAsString(), null, new Html.TagHandler() {
                    int startTag,endTag;
                    @Override
                    public void handleTag(boolean opening, String tag,final Editable output, XMLReader xmlReader) {
                        if(tag.equalsIgnoreCase("click_farmer")){
                            if(opening){
                                startTag=output.length();
                            }else{
                                endTag=output.length();

                                output.setSpan(new ClickableSpan() {
                                    @Override
                                    public void onClick(View widget) {
                                        getFarmerInfo(farmer_id);
                                    }
                                    @Override
                                    public void updateDrawState(TextPaint ds) {
                                        ds.setColor(ContextCompat.getColor(getActivity(),R.color.colorPrimary));
                                        ds.setUnderlineText(false);
                                        ds.clearShadowLayer();
                                    }
                                },startTag,endTag,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            }
                        }else if(tag.equalsIgnoreCase("click_purchaser")){
                            if(opening){
                                startTag=output.length();
                            }else{
                                endTag=output.length();
                                output.setSpan(new ClickableSpan() {
                                    @Override
                                    public void onClick(View widget) {

                                    }
                                    @Override
                                    public void updateDrawState(TextPaint ds) {
                                        ds.setColor(ContextCompat.getColor(getActivity(),R.color.colorPrimary));
                                        ds.setUnderlineText(false);
                                        ds.clearShadowLayer();
                                    }
                                },startTag,endTag,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            }
                        }
                    }
                });
            }
        }
        final List<Spanned> datas = Arrays.asList(bs);
        SimpleMF<Spanned> marqueeFactory = new SimpleMF(getActivity());
        marqueeFactory.setData(datas);
        marqueeView.setMarqueeFactory(marqueeFactory);
        setClick(marqueeFactory);
        marqueeView.startFlipping();
    }

    private void getFarmerInfo(final int farmer_id){
        HttpTask task=new HttpTask(getActivity());
        task.setTaskHandler(new HttpTask.HttpTaskHandler() {
            @Override
            public void taskStart(int code) {

            }

            @Override
            public void taskSuccessful(String str, int code) {
                JsonObject refreshResultJson = new JsonParser().parse(str)
                        .getAsJsonObject();
                int refreshResultCode = refreshResultJson.get("code").getAsInt();
                JsonObject farmer=refreshResultJson.get("farmer").getAsJsonObject();
                if(refreshResultCode==CodeUtil.SUCCESS_CODE){
                    FarmerModel fm=new FarmerModel();
                    fm.setName(farmer.get("name").getAsString());
                    fm.setPhone(farmer.get("phone").getAsString());
                    fm.setCardid(farmer.get("cardid").getAsString());
                    fm.setHeadUrl(farmer.get("headUrl").getAsString());
                    fm.setId(farmer_id);
                    fm.setAddressStr(farmer.get("addressStr").getAsString());
                    Intent intent=new Intent();
                    intent.putExtra("farmer",fm);
                    intent.setClass(getActivity(),FarmerInfoActivity.class);
                    startActivity(intent);
                }
            }

            @Override
            public void taskFailed(int code) {

            }
        });
        JsonObject object=new JsonObject();
        object.addProperty("farmer_id",farmer_id);
        task.execute(UrlUtil.GET_FARMER_INFO,object.toString());
    }
    private void initMarqueenView(final JsonArray bannerString){
        adapter.addHeader(new RecyclerArrayAdapter.ItemView() {
            @Override
            public View onCreateView(ViewGroup parent) {
                View view=View.inflate(getActivity(),R.layout.marqueen_layout,null);
                view.setLayoutParams(new EasyRecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UIUtil.dip2px(getActivity(),44f)));
                marqueeView=(SimpleMarqueeView) view.findViewById(R.id.marqueeView);
                initMarqueenData(bannerString);
                return view;
            }

            @Override
            public void onBindView(View headerView) {

            }
        });
    }
    private void initBanner(){
        adapter.addHeader(new RecyclerArrayAdapter.ItemView() {
            @Override
            public View onCreateView(ViewGroup parent) {
                View view=getActivity().getLayoutInflater().inflate(R.layout.banner_layout,null);
                banner=(Banner) view.findViewById(R.id.banner);
                banner.setLayoutParams(new EasyRecyclerView.LayoutParams(dm.widthPixels,dm.widthPixels*2/5));
                banner.setImageLoader(new ImageLoader() {
                    @Override
                    public void displayImage(Context context, Object path, ImageView imageView) {
                        Glide.with(getActivity()).load(path.toString()).placeholder(R.drawable.no_banner).override(dm.widthPixels,dm.widthPixels*17/37)
                                .into(imageView);
                    }
                });
                banner.setOnBannerListener(new OnBannerListener() {
                    @Override
                    public void OnBannerClick(int position) {
                        if(links.get(position).startsWith("http")){
                            Intent intent=new Intent();
                            intent.setClass(getActivity(), PlayWebViewActivity.class);
                            intent.putExtra("url",links.get(position));
                            startActivity(intent);
                        }
                    }
                });
                banner.setImages(imgs);
                banner.setBannerTitles(titles);
                banner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR_TITLE_INSIDE);
                banner.setIndicatorGravity(BannerConfig.CENTER);
                banner.isAutoPlay(true);
                banner.setDelayTime(5000);
                banner.start();
                return view;
            }
            @Override
            public void onBindView(View headerView) {

            }
        });
    }
    private void initMenu(){
        adapter.addHeader(new RecyclerArrayAdapter.ItemView() {
            @Override
            public View onCreateView(ViewGroup parent) {
                View menu_layout=View.inflate(getActivity(),R.layout.shouye_menu,null);
                NoScrollGridView grid=(NoScrollGridView) menu_layout.findViewById(R.id.list);
                grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if(position==0){
                            Intent intent=new Intent();
                            intent.putExtra("flag","search");
                            intent.setClass(getActivity(), TypeSelectActivity.class);
                            startActivity(intent);
                        }
                    }
                });
                MyMemuAdapter memuAdapter=new MyMemuAdapter(getActivity());
                memuAdapter.add(new MenuModel("分类查找",R.drawable.vector_drawable_fenleichazhao,R.drawable.fenlei_bg_normal));
                memuAdapter.add(new MenuModel("附近红包",R.drawable.vector_drawable_qianghongbao,R.drawable.qianghongbao_bg_normal));
                memuAdapter.add(new MenuModel("发红包",R.drawable.vector_drawable_fahongbao,R.drawable.fahongbao_bg_normal));
                memuAdapter.add(new MenuModel("行业资讯",R.drawable.vector_drawable_zixun,R.drawable.zixun_bg_normal));
                grid.setAdapter(memuAdapter);
                return menu_layout;
            }

            @Override
            public void onBindView(View headerView) {

            }
        });
    }
    private class MyMemuAdapter extends ArrayAdapter<MenuModel> {

        public MyMemuAdapter(Context context) {
            super(context, 0);
            // TODO Auto-generated constructor stub
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            if(convertView==null){
                convertView=getActivity().getLayoutInflater().inflate(R.layout.shouye_menu_item, null);
            }
            MenuModel menuModel=getItem(position);
            ImageView img=(ImageView) convertView.findViewById(R.id.img);
            TextView name=(TextView) convertView.findViewById(R.id.name);
            RelativeLayout head_img=(RelativeLayout) convertView.findViewById(R.id.head_img);
            img.setImageResource(menuModel.getResId());
            name.setText(menuModel.getName());
            head_img.setBackgroundResource(menuModel.getBg());
            return convertView;
        }
    }



    private void handleMenu(EasySwipeMenuLayout menuLayout){
        try {

                Method method = menuLayout.getClass().getDeclaredMethod("handlerSwipeMenu", State.class);
                method.setAccessible(true);
            if(EasySwipeMenuLayout.getViewCache()!=null&&EasySwipeMenuLayout.getViewCache()!=menuLayout){
                if(EasySwipeMenuLayout.getStateCache()!=State.CLOSE)
                method.invoke(EasySwipeMenuLayout.getViewCache(), State.CLOSE);
            }
            if(EasySwipeMenuLayout.getStateCache()!=State.RIGHTOPEN) {
                method.invoke(menuLayout, State.RIGHTOPEN);
            }else{
                method.invoke(menuLayout, State.CLOSE);
            }
        }catch (Exception e){

        }
    }
}
