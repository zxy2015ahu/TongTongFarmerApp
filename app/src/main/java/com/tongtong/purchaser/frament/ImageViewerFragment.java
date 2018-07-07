package com.tongtong.purchaser.frament;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.utils.BitmapUtil;
import com.tongtong.purchaser.utils.NetUtil;
import com.tongtong.purchaser.utils.UrlUtil;
import com.tongtong.purchaser.widget.StyleableToast;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;


/**
 * Created by Administrator on 2018-05-12.
 */

public class ImageViewerFragment extends Fragment {
    private int position;
    private int index;
    private String url;
    private PhotoView image;
    private ProgressBar progressBar;
    private boolean is_loaded=false;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.image_viewer_fragment,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        position=getArguments().getInt("defaultIndex",0);
        index=getArguments().getInt("index");
        url=getArguments().getString("url");
        image=(PhotoView) view.findViewById(R.id.image);
        image.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
            @Override
            public void onViewTap(View view, float x, float y) {
                getActivity().onBackPressed();
            }
        });
        progressBar=(ProgressBar) view.findViewById(R.id.progressBar);
        image.setZoomable(true);
        if(position==index){
            Glide.with(getActivity()).load(NetUtil.getFullUrl(url)).asBitmap()
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(final Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            progressBar.setVisibility(View.GONE);
                            image.setImageBitmap(resource);
                            image.setOnLongClickListener(new View.OnLongClickListener() {
                                @Override
                                public boolean onLongClick(View v) {
                                    final BottomSheetDialog dialog=new BottomSheetDialog(getActivity());
                                    dialog.setContentView(R.layout.bottom_sheet_layout);
                                    dialog.findViewById(R.id.img).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            BitmapUtil.saveBmp2Gallery(resource,getActivity());
                                            dialog.dismiss();
                                        }
                                    });
                                    dialog.findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            dialog.dismiss();
                                        }
                                    });
                                    dialog.show();
                                    return false;
                                }
                            });
                        }
                        @Override
                        public void onLoadStarted(Drawable placeholder) {
                            progressBar.setVisibility(View.VISIBLE);
                        }
                        @Override
                        public void onLoadFailed(Exception e, Drawable errorDrawable) {
                            progressBar.setVisibility(View.GONE);
                            StyleableToast.error(getActivity(),"图片加载出错");
                        }
                    });
            is_loaded=true;
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser&&!is_loaded&&position!=index){
            Glide.with(getActivity()).load(NetUtil.getFullUrl(url)).asBitmap()
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(final Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            progressBar.setVisibility(View.GONE);
                            image.setImageBitmap(resource);
                            image.setOnLongClickListener(new View.OnLongClickListener() {
                                @Override
                                public boolean onLongClick(View v) {
                                    final BottomSheetDialog dialog=new BottomSheetDialog(getActivity());
                                    dialog.setContentView(R.layout.bottom_sheet_layout);
                                    dialog.findViewById(R.id.img).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            BitmapUtil.saveBmp2Gallery(resource,getActivity());
                                            dialog.dismiss();
                                        }
                                    });
                                    dialog.findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            dialog.dismiss();
                                        }
                                    });
                                    dialog.show();
                                    return false;
                                }
                            });
                        }
                        @Override
                        public void onLoadStarted(Drawable placeholder) {
                            progressBar.setVisibility(View.VISIBLE);
                        }
                        @Override
                        public void onLoadFailed(Exception e, Drawable errorDrawable) {
                            progressBar.setVisibility(View.GONE);
                            StyleableToast.error(getActivity(),"图片加载出错");
                        }
                    });
            is_loaded=true;
        }
    }
}
