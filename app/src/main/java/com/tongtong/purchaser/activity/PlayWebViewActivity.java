package com.tongtong.purchaser.activity;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.githang.statusbar.StatusBarCompat;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.widget.StyleableToast;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;
import com.umeng.socialize.shareboard.SnsPlatform;
import com.umeng.socialize.utils.ShareBoardlistener;

import net.lucode.hackware.magicindicator.buildins.UIUtil;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;


/**
 * Created by Administrator on 2017/11/20.
 */

public class PlayWebViewActivity extends BaseActivity implements View.OnClickListener{
    private WebView webview;
    private ProgressBar pb;
    private TextView titles;
    private LinearLayout left_from;
    private boolean isError = false;
    private View error_layout;
    private Handler handler;
    private UMShareListener mShareListener;
    private ShareAction mShareAction;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_view_layout);
        error_layout=findViewById(R.id.error_layout);
        error_layout.setOnClickListener(this);
        mShareListener=new MyshareListener();
        StatusBarCompat.setStatusBarColor(this, Color.WHITE,true);
        titles=((TextView)findViewById(R.id.title_text));
        left_from=(LinearLayout) findViewById(R.id.left_from);
        handler=new Handler();
        DisplayMetrics dm=new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        left_from.measure(0,0);
        int width=dm.widthPixels-2*left_from.getMeasuredWidth()- UIUtil.dip2px(this,12f);
        titles.getLayoutParams().width=width;
        findViewById(R.id.back_bn).setOnClickListener(this);
        findViewById(R.id.close_btn).setOnClickListener(this);
        webview=(WebView) findViewById(R.id.web);
        pb=(ProgressBar) findViewById(R.id.myProgressBar);
        webview.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setSupportZoom(true);
        webview.getSettings().setTextZoom(100);
        webview.getSettings().setBuiltInZoomControls(true);
        webview.getSettings().setDisplayZoomControls(false);
        webview.getSettings().setUseWideViewPort(true);
        webview.getSettings().setLoadWithOverviewMode(true);
        webview.getSettings().setPluginState(WebSettings.PluginState.ON);
        webview.getSettings().setDomStorageEnabled(true);
        webview.getSettings().setSupportMultipleWindows(true);
        webview.getSettings().setSaveFormData(true);
        webview.getSettings().setSavePassword(true);
        webview.addJavascriptInterface(new MyJsInterface(),"bbsHandler");
        webview.getSettings().setLoadWithOverviewMode(true);
        webview.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if(pb.getVisibility()==View.GONE){
                    pb.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if(pb.getVisibility()==View.VISIBLE){
                    pb.setVisibility(View.GONE);
                }
                if (!isError){
                    error_layout.setVisibility(GONE);
                    webview.setVisibility(VISIBLE);
                }else{
                    error_layout.setVisibility(VISIBLE);
                    webview.setVisibility(GONE);
                }

            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if(url.startsWith("intent")||url.startsWith("youku")||url.startsWith("vipshop")){
                    return true;
                }else{
                    return super.shouldOverrideUrlLoading(view, url);
                }
            }

            @Override
            public void onReceivedError(final WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                isError = true;
            }
        });
        webview.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                titles.setText(title);
            }
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                pb.setProgress(newProgress);
            }
        });
        webview.loadUrl(getIntent().getStringExtra("url"));
    }
    private class MyshareListener implements UMShareListener{
        @Override
        public void onStart(SHARE_MEDIA share_media) {

        }

        @Override
        public void onResult(SHARE_MEDIA share_media) {
            showTips("分享成功");
        }

        @Override
        public void onCancel(SHARE_MEDIA share_media) {
            StyleableToast.info(PlayWebViewActivity.this,"分享已取消");
        }

        @Override
        public void onError(SHARE_MEDIA share_media, Throwable throwable) {
            StyleableToast.info(PlayWebViewActivity.this,"分享发生错误，请重试");
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /** attention to this below ,must add this**/
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mShareAction.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UMShareAPI.get(this).release();
    }
    private class MyJsInterface{
        @JavascriptInterface
        public void share(final String json){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    JsonObject object=new JsonParser().parse(json).getAsJsonObject();
                    JsonObject content=object.get("content").getAsJsonObject();
                    final String title=content.get("title").getAsString();
                    final String shareText=content.get("shareText").getAsString();
                    final String imgUrl=content.get("imgUrl").getAsString();
                    final String threadUrl=content.get("threadUrl").getAsString();
                    if(mShareAction==null){
                        mShareAction=new ShareAction(PlayWebViewActivity.this)
                                .setDisplayList(SHARE_MEDIA.WEIXIN,SHARE_MEDIA.WEIXIN_CIRCLE,SHARE_MEDIA.WEIXIN_FAVORITE)
                                .addButton("复制链接", "复制链接", "umeng_socialize_copyurl", "umeng_socialize_copyurl")
                                .setShareboardclickCallback(new ShareBoardlistener() {
                                    @Override
                                    public void onclick(SnsPlatform snsPlatform, SHARE_MEDIA share_media) {
                                        if (snsPlatform.mShowWord.equals("复制链接")) {
                                            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                            // 将文本内容放到系统剪贴板里。
                                            cm.setText(threadUrl);
                                            StyleableToast.info(PlayWebViewActivity.this,"链接已复制");
                                        } else {
                                            UMWeb web = new UMWeb(threadUrl);
                                            web.setTitle(title);
                                            web.setDescription(shareText);
                                            web.setThumb(new UMImage(PlayWebViewActivity.this, imgUrl));
                                            new ShareAction(PlayWebViewActivity.this).withMedia(web)
                                                    .setPlatform(share_media)
                                                    .setCallback(mShareListener)
                                                    .share();
                                        }
                                    }
                                });
                    }
                    mShareAction.open();
                }
            });
        }
    }
    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.back_bn){
            if(webview.canGoBack()){
                webview.goBack();
            }else{
                super.onBackPressed();
            }
        }else if(v.getId()==R.id.close_btn){
            super.onBackPressed();
        }else if(v.getId()==R.id.error_layout){
            isError=false;
            webview.reload();
        }
    }

    @Override
    public void onBackPressed() {
        if(webview.canGoBack()){
            webview.goBack();
        }else{
            super.onBackPressed();
        }
    }
}
