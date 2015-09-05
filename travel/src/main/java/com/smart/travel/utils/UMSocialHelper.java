package com.smart.travel.utils;

import android.app.Activity;
import android.content.Intent;

import com.smart.travel.R;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.media.QQShareContent;
import com.umeng.socialize.media.QZoneShareContent;
import com.umeng.socialize.media.SinaShareContent;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.sso.QZoneSsoHandler;
import com.umeng.socialize.sso.SinaSsoHandler;
import com.umeng.socialize.sso.UMQQSsoHandler;
import com.umeng.socialize.sso.UMSsoHandler;
import com.umeng.socialize.weixin.controller.UMWXHandler;
import com.umeng.socialize.weixin.media.CircleShareContent;
import com.umeng.socialize.weixin.media.WeiXinShareContent;

public class UMSocialHelper {
    public static final String QQ_APP_ID = "1104843670";
    public static final String QQ_APP_SECRET = "bd1jvfyI8gpCpHrU";

    public static final String WX_APP_ID = "wx6ebd26195e34acd1";
    public static final String WX_APP_SECRET = "533378ebcf2bb5fbffe4308c89b76f14";

    private static UMSocialHelper instance;

    private UMSocialService mController;
    private Activity mActivity;

    private UMSocialHelper() {}

    public static UMSocialHelper getInstance() {
        if (instance == null) {
            instance = new UMSocialHelper();
        }
        return instance;
    }

    public void setUp(Activity activity) {
        mController = UMServiceFactory.getUMSocialService("com.umeng.share");
        // add WX chart
        UMWXHandler wxHandler = new UMWXHandler(activity, WX_APP_ID, WX_APP_SECRET);
        wxHandler.addToSocialSDK();
        // add WX friends
        UMWXHandler wxCircleHandler = new UMWXHandler(activity, WX_APP_ID, WX_APP_SECRET);
        wxCircleHandler.setToCircle(true);
        wxCircleHandler.addToSocialSDK();
        // add qq
        UMQQSsoHandler qqSsoHandler = new UMQQSsoHandler(activity, QQ_APP_ID, QQ_APP_SECRET);
        qqSsoHandler.addToSocialSDK();
        // add qqZone
        QZoneSsoHandler qZoneSsoHandler = new QZoneSsoHandler(activity, QQ_APP_ID, QQ_APP_SECRET);
        qZoneSsoHandler.addToSocialSDK();
        // add Sina, a litter different. Related with onActivityResult
        mController.getConfig().setSsoHandler(new SinaSsoHandler());

        mController.getConfig().removePlatform(SHARE_MEDIA.TENCENT);
        mActivity = activity;
    }

    public UMSocialService getUMSocialService() {
        return mController;
    }

    public void openShare(boolean bool) {
        mController.openShare(mActivity, bool);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        UMSsoHandler ssoHandler = mController.getConfig().getSsoHandler(requestCode) ;
        if(ssoHandler != null){
            ssoHandler.authorizeCallBack(requestCode, resultCode, data);
        }
    }

    public void setShareContent(String title, String content, String url, UMImage image) {
        // share to WX friends
        WeiXinShareContent weiXinContent = new WeiXinShareContent();
        weiXinContent.setTitle(title);//
        weiXinContent.setShareContent(content);
        weiXinContent.setTargetUrl(url);
        // must set, otherwise url is invalid
        weiXinContent.setShareImage(image);
        mController.setShareMedia(weiXinContent);

        // share to WX circle
        CircleShareContent circleContent = new CircleShareContent();
        circleContent.setTitle(title);
        circleContent.setShareContent(content); //
        circleContent.setTargetUrl(url);
        circleContent.setShareImage(image);
        mController.setShareMedia(circleContent);

        // share to QQ friends
        QQShareContent qqShareContent = new QQShareContent();
        qqShareContent.setTitle(title);//
        qqShareContent.setShareContent(content);
        qqShareContent.setTargetUrl(url);
        qqShareContent.setShareImage(image);
        mController.setShareMedia(qqShareContent);

        // share to QQZone
        QZoneShareContent qZoneShareContent = new QZoneShareContent();
        qZoneShareContent.setTitle(title);//
        qZoneShareContent.setShareContent(content);
        qZoneShareContent.setTargetUrl(url);
        qZoneShareContent.setShareImage(image);
        mController.setShareMedia(qZoneShareContent);

        // share to Sina
        SinaShareContent sinaShareContent = new SinaShareContent();
        sinaShareContent.setTitle(title);//
        sinaShareContent.setShareContent(content);
        sinaShareContent.setTargetUrl(url);
        sinaShareContent.setShareImage(image);
        mController.setShareMedia(sinaShareContent);
    }
}
