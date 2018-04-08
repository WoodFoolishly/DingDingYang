package com.times1688.ddysc.application;

import android.app.Application;

import com.meiqia.core.MQManager;
import com.meiqia.core.callback.OnInitCallback;
import com.meiqia.meiqiasdk.util.MQConfig;
import com.mob.MobSDK;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import cn.sharesdk.framework.ShareSDK;

public class Constants extends Application {
    public static IWXAPI wx_api; //全局的微信api对象
    //public static IWXAPI mWxApi;
    public static final String APP_ID = "wxbd1eb3554d027fb3"; //替换为申请到的app id
    public static final String APP_SECRET = "04e6069d06c2dc3775608efd57c91f30"; //替换为申请到的app id

    @Override
    public void onCreate() {
        super.onCreate();
        //第二个参数是指你应用在微信开放平台上的AppID
        wx_api = WXAPIFactory.createWXAPI(this, APP_ID, true);
        //分享
        MobSDK.init(this);
        //美洽客服
        MQConfig.init(this, "0542e59028d3dc232b5aad296cba2c37", new OnInitCallback() {
            @Override
            public void onSuccess(String s) {
            }

            @Override
            public void onFailure(int i, String s) {
            }
        });
    }
}
