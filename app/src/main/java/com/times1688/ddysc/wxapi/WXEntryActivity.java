package com.times1688.ddysc.wxapi;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;

import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.times1688.ddysc.application.Constants;
import com.times1688.ddysc.activity.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

    public static String information;
    private String url;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Constants.wx_api.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        Constants.wx_api.handleIntent(intent, this);//必须调用此句话
    }

    //微信请求相应
    @Override
    public void onReq(BaseReq req) {
    }

    //发送到微信请求的响应结果
    @Override
    public void onResp(BaseResp resp) {
        switch (resp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                Log.i("WXTest", "onResp OK");
                if (resp instanceof SendAuth.Resp) {
                    SendAuth.Resp newResp = (SendAuth.Resp) resp;
                    if (newResp != null) {
                        //获取微信传回的code
                        String code = newResp.code;
                        Log.i("WXTest", "onResp code = " + code);
                        try {
                            getAccess_token(code);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                Log.i("WXTest", "onResp ERR_USER_CANCEL ");
                //发送取消
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                Log.i("WXTest", "onResp ERR_AUTH_DENIED");
                //发送被拒绝
                break;
            default:
                Log.i("WXTest", "onResp default errCode " + resp.errCode);
                //发送返回
                break;
        }
        finish();
    }

    private void getAccess_token(String code) throws IOException {
        String path = "https://api.weixin.qq.com/sns/oauth2/access_token?appid="
                + Constants.APP_ID
                + "&secret="
                + Constants.APP_SECRET
                + "&code="
                + code
                + "&grant_type=authorization_code";
        Log.v("的什么都是", path);
        //新建客户端
        OkHttpClient client = new OkHttpClient();
        //新建请求
        Request request = new Request.Builder()
                .get() //get请求
                .url(path).build();
        Log.v("的什么都是", String.valueOf(request));
        //返回对象
        Response response = client.newCall(request).execute();
        //阻塞线程。
        if (response.isSuccessful()) {
            Log.e("code", ":" + response.code());
            //Log.e("body", response.body().string());
            try {
                JSONObject jsonObject = new JSONObject(response.body().string());
                String openid = jsonObject.getString("openid").trim();
                String access_token = jsonObject.getString("access_token").trim();
                Log.v("三大角色的", openid + "啥都好说" + access_token);
                getUserMesg(access_token, openid);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Log.e("---", "不成功");
        }
    }

    private void getUserMesg(String access_token, String openid) throws IOException, JSONException {
        String path = "https://api.weixin.qq.com/sns/userinfo?access_token="
                + access_token
                + "&openid="
                + openid;
        //新建客户端
        OkHttpClient client = new OkHttpClient();
        //新建请求
        Request request = new Request.Builder()
                .get() //get请求
                .url(path).build();
        //返回对象
        Response response = client.newCall(request).execute();
        //阻塞线程。
        if (response.isSuccessful()) {
            Log.e("code", ":" + response.code());
            //Log.e("body", response.body().string());
            information = response.body().string();
            JSONObject jsonObject = new JSONObject(information);
            String openid_wx = jsonObject.getString("openid");
            String nickname = jsonObject.getString("nickname");
            String headimgurl = jsonObject.getString("headimgurl");
            if (MainActivity.value1.equals("1")) {
                url = "http://www.times1688.com/wx_login.php?" + "openid=" + openid_wx + "&nickname=" + nickname + "&headimgurl=" + headimgurl;
            } else {
                url = "http://www.times1688.com/wx_login.php?" + "openid=" + openid_wx + "&nickname=" + nickname + "&headimgurl=" + headimgurl + "&url=" + MainActivity.value1;
            }
            //新建请求
            Request request1 = new Request.Builder()
                    .get() //get请求
                    .url(url).build();
            Log.v("算得上的的1", String.valueOf(request1));
            //返回对象
            Response response1 = client.newCall(request1).execute();
            if (response1.isSuccessful()) {
                Log.e("code", ":" + response1.code());
                //Log.e("body", response1.body().string());
                JSONObject jsonObject2 = new JSONObject(response1.body().string());
                String url = jsonObject2.getString("url");
                Log.v("晒单晒单所", url);
                //页面跳转
                MainActivity.wb.loadUrl(url);
            }
        } else {
            Log.e("---", "不成功");
        }
    }

}