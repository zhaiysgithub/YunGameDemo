package kptech.game.kit.ad.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.zad.sdk.Oapi.bean.ZadFeedDataAdBean;
import com.zad.sdk.Oapi.constants.ZADFeedConstant;

import kptech.game.kit.R;
import kptech.game.kit.utils.DensityUtil;

public class AdFeedPopup extends PopupWindow {


    private Activity mActivity;
    public AdFeedPopup(Activity activity, ZadFeedDataAdBean adBean) {//, int position,
        super(activity);
        this.mActivity = activity;

        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = (ViewGroup) inflater.inflate(R.layout.ad_popup_feed, null);
        this.setContentView(rootView);


        //sdk > 21 解决 标题栏没有办法遮罩的问题
        this.setClippingEnabled(false);
        // 设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        // 设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(WindowManager.LayoutParams.MATCH_PARENT);//屏幕的高
        // 设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
        // 设置SelectPicPopupWindow弹出窗体动画效果
//        this.setAnimationStyle(R.style.AnimationWindow);
        // 实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0x78000000);
        // 设置SelectPicPopupWindow弹出窗体的背景
        this.setBackgroundDrawable(dw);

        findFeedView();
        renderFeedAdView(adBean);
    }

    private ViewGroup rootView;
    private TextView title;
    private LinearLayout rlContent1;
    private TextView zmtItemAdTitleTextView1;
    private TextView zmtItemAdDesTextView1;
    private RelativeLayout rlContent;
    private ImageView zmtItemAdImageView1;
    private ImageView zmtItemAdImageView2;
    private FrameLayout zmtItemAdVideo5;
    private RelativeLayout rlContent2;
    private ImageView zmtItemAdImageView3;
    private TextView zmtItemAdTitleTextView3;
    private TextView zmtItemAdDesTextView3;
    private RelativeLayout rlContent3;
    private TextView zmtItemAdTitleTextView4;
    private TextView zmtItemAdDesTextView4;
    private LinearLayout zmtAdImageLayout4;
    private ImageView zmtItemAdImageView41;
    private ImageView zmtItemAdImageView42;
    private ImageView zmtItemAdImageView43;
    private ImageView zmtItemLogoImageView1;
    private TextView zmtItemAdvertisingTextView1;
    private ImageView zmtItemCloseImageView1;


    private void findFeedView() {
        rlContent1 = (LinearLayout) rootView.findViewById(R.id.rl_content1);
        zmtItemAdTitleTextView1 = (TextView) rootView.findViewById(R.id.zmt_item_ad_title_textView1);
        zmtItemAdDesTextView1 = (TextView) rootView.findViewById(R.id.zmt_item_ad_des_textView1);
        rlContent = (RelativeLayout) rootView.findViewById(R.id.rl_content);
        zmtItemAdImageView1 = (ImageView) rootView.findViewById(R.id.zmt_item_ad_imageView1);
        zmtItemAdImageView2 = (ImageView) rootView.findViewById(R.id.zmt_item_ad_imageView2);
        zmtItemAdVideo5 = (FrameLayout) rootView.findViewById(R.id.zmt_item_ad_video5);
        rlContent2 = (RelativeLayout) rootView.findViewById(R.id.rl_content2);
        zmtItemAdImageView3 = (ImageView) rootView.findViewById(R.id.zmt_item_ad_imageView3);
        zmtItemAdTitleTextView3 = (TextView) rootView.findViewById(R.id.zmt_item_ad_title_textView3);
        zmtItemAdDesTextView3 = (TextView) rootView.findViewById(R.id.zmt_item_ad_des_textView3);
        rlContent3 = (RelativeLayout) rootView.findViewById(R.id.rl_content3);
        zmtItemAdTitleTextView4 = (TextView) rootView.findViewById(R.id.zmt_item_ad_title_textView4);
        zmtItemAdDesTextView4 = (TextView) rootView.findViewById(R.id.zmt_item_ad_des_textView4);
        zmtAdImageLayout4 = (LinearLayout) rootView.findViewById(R.id.zmt_ad_image_layout4);
        zmtItemAdImageView41 = (ImageView) rootView.findViewById(R.id.zmt_item_ad_imageView41);
        zmtItemAdImageView42 = (ImageView) rootView.findViewById(R.id.zmt_item_ad_imageView42);
        zmtItemAdImageView43 = (ImageView) rootView.findViewById(R.id.zmt_item_ad_imageView43);
        zmtItemLogoImageView1 = (ImageView) rootView.findViewById(R.id.zmt_item_logo_imageView1);
        zmtItemAdvertisingTextView1 = (TextView) rootView.findViewById(R.id.zmt_item_advertising_textView1);
        zmtItemCloseImageView1 = (ImageView) rootView.findViewById(R.id.zmt_item_close_imageView1);
    }



    private void renderFeedAdView(ZadFeedDataAdBean ad) {
        switch (ad.getImageMode()) {
            case ZADFeedConstant.IMAGE_MODE_LARGE_IMG:
                rlContent1.setVisibility(View.VISIBLE);
                rlContent2.setVisibility(View.GONE);
                rlContent3.setVisibility(View.GONE);
                zmtItemAdImageView1.setVisibility(View.VISIBLE);
                zmtItemAdImageView2.setVisibility(View.GONE);
                zmtItemAdVideo5.setVisibility(View.GONE);
                if (ad.getImageList().size() > 0) {
                    rlContent.setVisibility(View.VISIBLE);
                    zmtItemAdImageView1.setVisibility(View.VISIBLE);
                    try {
                        Picasso.with(mActivity).load(ad.getImageList().get(0)).into(zmtItemAdImageView1);
                    }catch (Exception e){}
                } else {
                    rlContent.setVisibility(View.GONE);
                    zmtItemAdImageView1.setVisibility(View.GONE);
                }
                setContent(ad, zmtItemAdTitleTextView1, zmtItemAdDesTextView1);
                break;
            case ZADFeedConstant.IMAGE_MODE_VERTICAL_IMG:
                rlContent1.setVisibility(View.VISIBLE);
                rlContent2.setVisibility(View.GONE);
                rlContent3.setVisibility(View.GONE);
                zmtItemAdImageView1.setVisibility(View.GONE);
                zmtItemAdImageView2.setVisibility(View.VISIBLE);
                zmtItemAdVideo5.setVisibility(View.GONE);
                if (ad.getImageList().size() > 0) {
                    try {
                        Picasso.with(mActivity).load(ad.getImageList().get(0)).into(zmtItemAdImageView2);
                    }catch (Exception e){}
                }
                setContent(ad, zmtItemAdTitleTextView1, zmtItemAdDesTextView1);
                break;
            case ZADFeedConstant.IMAGE_MODE_SMALL_IMG:
                rlContent1.setVisibility(View.GONE);
                rlContent2.setVisibility(View.VISIBLE);
                rlContent3.setVisibility(View.GONE);
                if (ad.getImageList().size() > 0) {
                    try {
                        Picasso.with(mActivity).load(ad.getImageList().get(0)).into(zmtItemAdImageView3);
                    }catch (Exception e){}
                }
                setContent(ad, zmtItemAdTitleTextView3, zmtItemAdDesTextView3);
                break;
            case ZADFeedConstant.IMAGE_MODE_GROUP_IMG:
                rlContent1.setVisibility(View.GONE);
                rlContent2.setVisibility(View.GONE);
                rlContent3.setVisibility(View.VISIBLE);
                if (ad.getImageList().size() > 2) {
                    try {
                        Picasso.with(mActivity).load(ad.getImageList().get(0)).into(zmtItemAdImageView41);
                        Picasso.with(mActivity).load(ad.getImageList().get(1)).into(zmtItemAdImageView42);
                        Picasso.with(mActivity).load(ad.getImageList().get(2)).into(zmtItemAdImageView43);
                    }catch (Exception e){}
                }
                setContent(ad, zmtItemAdTitleTextView4, zmtItemAdDesTextView4);
                break;
            case ZADFeedConstant.IMAGE_MODE_VIDEO:
                rlContent1.setVisibility(View.VISIBLE);
                rlContent2.setVisibility(View.GONE);
                rlContent3.setVisibility(View.GONE);
                zmtItemAdImageView1.setVisibility(View.GONE);
                zmtItemAdImageView2.setVisibility(View.GONE);
                zmtItemAdVideo5.setVisibility(View.VISIBLE);
                zmtItemAdVideo5.addView(ad.getAdView());
                setContent(ad, zmtItemAdTitleTextView1, zmtItemAdDesTextView1);
                break;
        }
        if (!TextUtils.isEmpty(ad.getAdLogoUrl())) {
            zmtItemLogoImageView1.setVisibility(View.VISIBLE);
            try {
                Picasso.with(mActivity).load(ad.getAdLogoUrl()).into(zmtItemLogoImageView1);
            }catch (Exception e){}
        } else {
            zmtItemLogoImageView1.setVisibility(View.GONE);
        }
        zmtItemCloseImageView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ViewGroup) rootView).removeAllViews();
                dismiss();
            }
        });
        ad.renderAdView(rootView);
        ad.reportViewShow(rootView); // 用来统计广告展示，内部排重，不调用会影响计费

//        View adview = ad.getAdView();
//        adview.setBackgroundColor(Color.GREEN);
    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        super.showAtLocation(parent, gravity, x, y);
        int height = getContentView().getMeasuredHeight();
        Log.i("",""+height);

    }

    private void setContent(ZadFeedDataAdBean zadFeedAd, TextView title, TextView des) {
        if (!TextUtils.isEmpty(zadFeedAd.getTitle())) {
            title.setVisibility(View.VISIBLE);
            title.setText(zadFeedAd.getTitle());
        } else {
            title.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(zadFeedAd.getDescription())) {
            des.setVisibility(View.VISIBLE);
            des.setText(zadFeedAd.getDescription());
        } else {
            des.setVisibility(View.GONE);
        }
    }
}
