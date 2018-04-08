package com.times1688.ddysc.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.meiqia.meiqiasdk.util.MQIntentBuilder;
import com.mylhyl.circledialog.CircleDialog;
import com.mylhyl.circledialog.callback.ConfigButton;
import com.mylhyl.circledialog.callback.ConfigDialog;
import com.mylhyl.circledialog.params.ButtonParams;
import com.mylhyl.circledialog.params.DialogParams;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.times1688.ddysc.application.Constants;
import com.times1688.ddysc.R;
import com.times1688.ddysc.utils.Util;
import com.times1688.ddysc.bean.TypeBean;
import com.tsy.sdk.pay.alipay.Alipay;
import com.tsy.sdk.pay.weixin.WXPay;

import org.json.JSONException;
import org.json.JSONObject;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import cn.sharesdk.onekeyshare.OnekeyShare;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {

    public static WebView wb;
    private String url;
    public static String value1;
    public final String HOST = "http://www.times1688.com/";
    public final String HOME1 = HOST + "index.php";
    public final String HOME2 = HOST + "class_page.php";
    public final String HOME3 = HOST + "BBS/index.php";
    public final String HOME4 = HOST + "order_list_new.php";
    private long exitTime = 0;
    public ValueCallback<Uri[]> mUploadMessageForAndroid5;
    public ValueCallback<Uri> mUploadMessage;

    public final static int FILECHOOSER_RESULTCODE = 1;
    public final static int FILECHOOSER_RESULTCODE_FOR_ANDROID_5 = 2;
    private ClipboardManager manager;
    private String text;
    private ClipData clipData;
    private ClipData.Item item;
    private final long SPLASH_LENGTH = 3000;
    Handler handler = new Handler();
    private ArrayList<TypeBean> mList = new ArrayList<TypeBean>();
    String[] password = {"12345678"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //创建微信api并注册到微信
        Constants.wx_api = WXAPIFactory.createWXAPI(MainActivity.this, Constants.APP_ID, true);
        Constants.wx_api.registerApp(Constants.APP_ID);
        /*byId = findViewById(R.id.bt);
        byId.setOnClickListener(this);*/
        loaDingWeb();
        //获取黏贴板内容用于口令
        manager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        for (int i = 0; i < 12; i++) {
            mList.add(new TypeBean(i, i + "月"));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 剪贴板中的数据被改变，此方法将被回调
        if (manager.hasPrimaryClip()) {
            clipData = manager.getPrimaryClip();
            item = clipData.getItemAt(0);
            if (item.getText() != null) {
                text = item.getText().toString();
                String key = "$";
                final int first = text.indexOf(key);
                if (first >= 0) {
                    String new1 = text.substring(first + 1);
                    int tow = new1.indexOf(key);
                    if (tow >= 0) {
                        final String new2 = new1.substring(0, tow);
                        if (new2.length() == 8) {
                            //new2即为口令字符串
                            for (int i = 0; i < password.length; i++) {
                                String s = password[i];
                                if (!s.equals(new2)) {
                                    //wb.loadUrl("javascript:$(function(){get_product(\" " + new2 + "\");})");
                                    handler.postDelayed(new Runnable() {  //使用handler的postDelayed实现延时跳转
                                        public void run() {
                                            wb.loadUrl("javascript:get_product(\" " + new2 + "\")");
                                        }
                                    }, SPLASH_LENGTH);
                                    password[i] = new2;
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    @SuppressLint("SetJavaScriptEnabled")
    private void loaDingWeb() {
        //启用支持javascript
        wb = findViewById(R.id.id_wb);
        wb.setWebChromeClient(new WebChromeClient());
        WebSettings settings = wb.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setAllowFileAccess(true);// 设置允许访问文件数据
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setUseWideViewPort(true);//将图片调整到适合webview的大小
        settings.setLoadWithOverviewMode(true);// 缩放至屏幕的大小
        wb.requestFocus();
        wb.loadUrl("http://www.times1688.com/");
        //覆盖WebView默认使用第三方或系统默认浏览器打开网页的行为，使网页用WebView打开
        wb.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // TODO Auto-generated method stub
                //Android8.0以下的需要返回true 并且需要loadUrl；8.0之后效果相反
                if (Build.VERSION.SDK_INT < 26) {
                    view.loadUrl(url);
                    return true;
                }
                //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
                return false;
            }
        });

        wb.addJavascriptInterface(new JavaScriptObject(), "android");
        //选取相册图片
        wb.setWebChromeClient(
                new WebChromeClient() {
                    public void onProgressChanged(WebView view, int progress) {// 载入进度改变而触发
                        if (progress == 100) {
                            //handler.sendEmptyMessage(1);// 如果全部载入,隐藏进度对话框
                        }
                        super.onProgressChanged(view, progress);
                    }

                    //扩展支持alert事件
                    @Override
                    public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                        builder.setTitle("提示").setMessage(message).setPositiveButton("确定", null);
                        builder.setCancelable(false);
                        //builder.setIcon(R.drawable.ic_launcher);
                        AlertDialog dialog = builder.create();
                        dialog.show();
                        result.confirm();
                        return true;
                    }

                    // For Android > 5.0
                    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> uploadMsg, WebChromeClient.FileChooserParams fileChooserParams) {
                        openFileChooserImplForAndroid5(uploadMsg);
                        return true;
                    }
                }
        );

    }

    //选取相册图片
    private void openFileChooserImplForAndroid5(ValueCallback<Uri[]> uploadMsg) {
        mUploadMessageForAndroid5 = uploadMsg;
        Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
        contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
        contentSelectionIntent.setType("image/*");
        Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
        chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
        chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
        startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE_FOR_ANDROID_5);
    }


    //改写物理按键——返回的逻辑
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        url = wb.getUrl();
        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (TextUtils.equals(HOST, url) || TextUtils.equals(HOME1, url) || TextUtils.equals(HOME2, url) || TextUtils.equals(HOME3, url)
                    || TextUtils.equals(HOME4, url)) {
                if ((System.currentTimeMillis() - exitTime) > 2000) {
                    Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                    exitTime = System.currentTimeMillis();
                } else {
                    finish();//如果是重定向网址就finish
                }
                return true;
            } else {
                wb.goBack();//返回上一个页面
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("all")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        //返回选取后的相册数据
        if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage)
                return;
            Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;

        } else if (requestCode == FILECHOOSER_RESULTCODE_FOR_ANDROID_5) {
            if (null == mUploadMessageForAndroid5)
                return;
            Uri result = (intent == null || resultCode != RESULT_OK) ? null : intent.getData();
            if (result != null) {
                mUploadMessageForAndroid5.onReceiveValue(new Uri[]{result});
            } else {
                mUploadMessageForAndroid5.onReceiveValue(new Uri[]{});
            }
            mUploadMessageForAndroid5 = null;
        }
    }

    //js交互的方法
    public class JavaScriptObject {
        //单项选择器弹窗
        @JavascriptInterface
        public void selectorPopover() {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Util.alertBottomWheelOption(MainActivity.this, mList, new Util.OnWheelViewClick() {
                        @Override
                        public void onClick(View view, int postion) {
                            Toast.makeText(MainActivity.this, mList.get(postion).getName(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }

        //美洽客服
        @JavascriptInterface
        public void onlineCustomerService() {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new MQIntentBuilder(MainActivity.this).build();
                    startActivity(intent);
                }
            });
        }

        //微信登录
        @JavascriptInterface
        public void weChatLogin(String value) {
            Log.v("值", String.valueOf(value));
            //information = WXEntryActivity.information;
            //wb.loadUrl("javascript:alertMessage(\" " + WXEntryActivity.information + "\")");
            SendAuth.Req req = new SendAuth.Req();
            req.scope = "snsapi_userinfo";
            req.state = "wechat_sdk_demo_test";
            Constants.wx_api.sendReq(req);
            value1 = value;
        }


        //分享
        @JavascriptInterface
        public void dingDingyangShare(String shareContent, String imagePath, String linkPath, String title, String sharePassword, String viewSelection) throws JSONException {
            Log.v("dingDingyangShare", "内容" + shareContent + "图片路径" + imagePath + "链接路径" + linkPath + "标题" + title + "口令" + sharePassword + "查看我的精选" + viewSelection);
            showShare(shareContent, imagePath, linkPath, title, sharePassword, viewSelection);
        }

        //支付宝
        @JavascriptInterface
        public void dingDingyangAliPay(final String info, final String address) {
            //wb.loadUrl("javascript:alertMessage(\" " + info + address + "\")");
            Log.v("支付宝订单信息", String.valueOf(info + "地址" + address));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //跳转支付界面
                    openPayment(info, address);
                }
            });
        }

        //微信支付
        @JavascriptInterface
        public void dingDingyangWeChatPay(String appid, String noncestr, String packageValue, String partnerid, String prepayid, String sign, String timestamp, final String wxUrl) throws JSONException {
            // wb.loadUrl("javascript:alertMessage(\" " + information + wxUrl + "\")");
            Log.v("微信订单信息", String.valueOf("appid" + ":" + appid + "    " + "noncestr" + ":" + noncestr + "   " + "packageValue" + ":" + packageValue + "    " + "partnerid" + ":" + partnerid + "    " + "prepayId" + ":" + prepayid + "   " + "sign" + ":" + sign + "   " + "timeStamp" + ":" + timestamp + "地址" + wxUrl));
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put("appid", appid);
            jsonObject.put("partnerid", partnerid);
            jsonObject.put("prepayid", prepayid);
            jsonObject.put("package", packageValue);
            jsonObject.put("noncestr", noncestr);
            jsonObject.put("timestamp", timestamp);
            jsonObject.put("sign", sign);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //跳转支付界面
                    weChatPayJump(jsonObject, wxUrl);
                }
            });

        }
    }

    //截取口令-店主精选使用
    private String InterceptPassword(String sharePassword) {
        String new2 = null;
        String key = "$";
        final int first = sharePassword.indexOf(key);
        if (first >= 0) {
            String new1 = sharePassword.substring(first + 1);
            int tow = new1.indexOf(key);
            if (tow >= 0) {
                new2 = new1.substring(0, tow);
            }
        }
        return new2;
    }

    //微信分享-朋友圈分享-口令分享-图文分享-复制链接-店主精选
    private void showShare(String content, String imagePath, final String linkPath, String title, final String sharePassword, final String viewSelection) throws JSONException {
        // 参考代码配置章节，设置分享参数
        OnekeyShare oks = new OnekeyShare();
        // 1:构造一个图标-->口令分享
        Bitmap enableLogo = BitmapFactory.decodeResource(this.getResources(), R.mipmap.password);
        String label = "口令分享";
        View.OnClickListener listener = new View.OnClickListener() {
            public void onClick(View v) {
                ClipData mClipData;
                mClipData = ClipData.newPlainText("test", sharePassword);
                manager.setPrimaryClip(mClipData);
                final String[] items = {"微信"};
                new CircleDialog.Builder(MainActivity.this)
                        .configDialog(new ConfigDialog() {
                            @Override
                            public void onConfig(DialogParams params) {
                                //增加弹出动画
                                params.animStyle = R.style.dialogWindowAnim;
                            }
                        })
                        .setItems(items, new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int
                                    position, long id) {
                                new CircleDialog.Builder(MainActivity.this)
                                        .setCanceledOnTouchOutside(false)
                                        .setCancelable(false)
                                        .setTitle("已为您生成淘口令")
                                        .setText(sharePassword)
                                        .setNegative("不分享了", null)
                                        .setPositive("去粘贴", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                //打开微信客户端
                                                Intent intent = new Intent();
                                                ComponentName cmp = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI");
                                                intent.setAction(Intent.ACTION_MAIN);
                                                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                intent.setComponent(cmp);
                                                startActivityForResult(intent, 0);
                                            }
                                        })
                                        .show();
                            }
                        })
                        .setNegative("取消", null)
                        .configNegative(new ConfigButton() {
                            @Override
                            public void onConfig(ButtonParams params) {
                                //取消按钮字体颜色
                                params.textColor = Color.RED;
                            }
                        })
                        .show();
            }
        };

        //2:构造一个图标-->图文分享
        final Bitmap graphicSharing = BitmapFactory.decodeResource(this.getResources(), R.mipmap.tuvensharing);
        String graphicSharingLabel = "图文分享";
        //将图片保存到相册中
        File saveFile = savePicToSDCard(graphicSharing);
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(saveFile)));
        View.OnClickListener graphicSharingListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String contents = "商品二维码已保存到手机相册";
                String confirmation = "去分享朋友圈";
                selectABombFromTheShopkeeper(contents, confirmation, 1, viewSelection);
            }
        };

        //3:构造一个图标-->复制链接
        Bitmap copyLink = BitmapFactory.decodeResource(this.getResources(), R.mipmap.copylink);
        String copyLinkLabel = "复制链接";
        View.OnClickListener copyLinkListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipData mClipData;
                mClipData = ClipData.newPlainText("test", linkPath);
                manager.setPrimaryClip(mClipData);
                Toast toast = Toast.makeText(MainActivity.this, "已复制到剪切板", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        };
        // 4:构造一个图标-->店主精选
        Bitmap shopkeepers = BitmapFactory.decodeResource(this.getResources(), R.mipmap.shopkeepers);
        String shopkeepersLabel = "店主精选";
        View.OnClickListener shopkeepersListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //返回截取口令
                String passWord = InterceptPassword(sharePassword);
                try {
                    //step 1: 同样的需要创建一个OkHttpClick对象
                    OkHttpClient client = new OkHttpClient();
                    //step 2: 创建  FormBody.Builder
                    FormBody formBody = new FormBody.Builder()
                            .add("handle", "selected")
                            .add("code", passWord)
                            .build();
                    //step 3: 创建请求
                    Request request = new Request.Builder()
                            .url("http://wxshop.gaoliuxu.com/Handle/share.php")
                            .post(formBody)
                            .build();
                    //step 4： 建立联系 创建Call对象
                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            // TODO: 17-1-4  请求失败
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            // TODO: 17-1-4 请求成功
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String contents = "已添加到店主精选";
                String confirmation = "查看我的精选";
                selectABombFromTheShopkeeper(contents, confirmation, 2, viewSelection);
            }
        };
        //口令分享
        oks.setCustomerLogo(enableLogo, label, listener);
        //图文分享
        oks.setCustomerLogo(graphicSharing, graphicSharingLabel, graphicSharingListener);
        //复制链接
        oks.setCustomerLogo(copyLink, copyLinkLabel, copyLinkListener);
        //店主精选
        if (!viewSelection.equals("")) {
            oks.setCustomerLogo(shopkeepers, shopkeepersLabel, shopkeepersListener);
        }
        //关闭sso授权
        oks.disableSSOWhenAuthorize();
        // title标题，微信、QQ和QQ空间等平台使用
        oks.setTitle(title);
        // titleUrl QQ和QQ空间跳转链接
        //oks.setTitleUrl(linkPath);
        // text是分享文本，所有平台都需要这个字段
        oks.setText(content);
        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
        oks.setImageUrl(imagePath);//确保SDcard下面存在此张图片
        // url在微信、微博，Facebook等平台中使用
        oks.setUrl(linkPath);
        // 启动分享GUI
        oks.show(this);
    }

    //支付宝回调
    private void openPayment(String pay_param, final String address) {
        new Alipay(this, pay_param, new Alipay.AlipayResultCallBack() {
            @Override
            public void onSuccess() {
                Toast.makeText(getApplication(), "支付成功", Toast.LENGTH_SHORT).show();
                wb.loadUrl(address);
            }

            @Override
            public void onDealing() {
                Toast.makeText(getApplication(), "支付处理中...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(int error_code) {
                switch (error_code) {
                    case Alipay.ERROR_RESULT:
                        Toast.makeText(getApplication(), "支付失败:支付结果解析错误", Toast.LENGTH_SHORT).show();
                        break;

                    case Alipay.ERROR_NETWORK:
                        Toast.makeText(getApplication(), "支付失败:网络连接错误", Toast.LENGTH_SHORT).show();
                        break;

                    case Alipay.ERROR_PAY:
                        Toast.makeText(getApplication(), "支付错误:支付码支付失败", Toast.LENGTH_SHORT).show();
                        break;

                    default:
                        Toast.makeText(getApplication(), "支付错误", Toast.LENGTH_SHORT).show();
                        wb.loadUrl(address);
                        break;
                }

            }

            @Override
            public void onCancel() {
                Toast.makeText(getApplication(), "支付取消", Toast.LENGTH_SHORT).show();
                wb.loadUrl(address);
            }
        }).doPay();
    }

    //微信支付回调
    private void weChatPayJump(JSONObject pay_param, final String wxUrl) {
        String wx_appid = "wxbd1eb3554d027fb3";     //替换为自己的appid
        WXPay.init(getApplicationContext(), wx_appid);      //要在支付前调用
        WXPay.getInstance().doPay(String.valueOf(pay_param), new WXPay.WXPayResultCallBack() {
            @Override
            public void onSuccess() {
                Toast.makeText(getApplication(), "支付成功", Toast.LENGTH_SHORT).show();
                wb.loadUrl(wxUrl);
            }

            @Override
            public void onError(int error_code) {
                switch (error_code) {
                    case WXPay.NO_OR_LOW_WX:
                        Toast.makeText(getApplication(), "未安装微信或微信版本过低", Toast.LENGTH_SHORT).show();
                        wb.loadUrl(wxUrl);
                        break;

                    case WXPay.ERROR_PAY_PARAM:
                        Toast.makeText(getApplication(), "参数错误", Toast.LENGTH_SHORT).show();
                        wb.loadUrl(wxUrl);
                        break;

                    case WXPay.ERROR_PAY:
                        Toast.makeText(getApplication(), "支付失败", Toast.LENGTH_SHORT).show();
                        wb.loadUrl(wxUrl);
                        break;
                }
            }

            @Override
            public void onCancel() {
                Toast.makeText(getApplication(), "支付取消", Toast.LENGTH_SHORT).show();
                wb.loadUrl(wxUrl);
            }
        });
    }

    //图文分享--店主精选弹窗
    private void selectABombFromTheShopkeeper(final String contents, final String confirmation, final int select, final String viewSelection) {
        //获取AlertDialog对象
        final AlertDialog build = new AlertDialog.Builder(this).create();
        build.setCancelable(false);
        //自定义布局
        final View view = getLayoutInflater().inflate(R.layout.module_dialog_shopkeepers, null);
        //把自定义的布局设置到dialog中,注意,布局设置一定要在show之前.从第二个参数分别填充内容与边框
        build.setView(view, 0, 0, 0, 0);
        //设置是否点击物理返回键取消弹窗
        build.setCancelable(true);
        //一定要先show出来再设置dialog的参数,不然就不会改变dialog的大小了
        build.show();
        //得到当前显示的设备的宽度,单位是像素
        int width = getWindowManager().getDefaultDisplay().getWidth();
        int height = getWindowManager().getDefaultDisplay().getHeight();
        //得到这个Dialog界面的参数对象
        final WindowManager.LayoutParams params = build.getWindow().getAttributes();
        //设置Dialog的界面宽度
        params.width = width - (width / 6);
        params.height = (int) (height * 0.4);
        /*//设置Dialog高度为包裹内容
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;*/
        //设置dialog的重心
        params.gravity = Gravity.CENTER;
        //最后把这个参数对象设置进去,即与dialog绑定
        build.getWindow().setAttributes(params);
        final TextView dialog_content_tv = view.findViewById(R.id.dialog_content_tv);
        final Button dialog_determine_bt = view.findViewById(R.id.dialog_determine_btn_view);
        final Button dialog_cancel_bt = view.findViewById(R.id.dialog_cancel_btn_close);
        dialog_content_tv.setText(contents);
        dialog_determine_bt.setText(confirmation);
        //确定按钮
        dialog_determine_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (select == 1) {
                    //打开微信客户端
                    Intent intent = new Intent();
                    ComponentName cmp = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI");
                    intent.setAction(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setComponent(cmp);
                    startActivityForResult(intent, 0);
                } else if (select == 2) {
                    //点击查看我的精选
                    wb.loadUrl(viewSelection);
                    build.dismiss();
                }
            }
        });
        //取消按钮
        dialog_cancel_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                build.dismiss();
            }
        });
    }

    //保存图片到相册
    @SuppressLint("SimpleDateFormat")
    public File savePicToSDCard(Bitmap bitmap) {
        File picFile = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String fileName = sdf.format(new Date()) + ".jpg";
        boolean isSDExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);                        //判断SD卡是否存在
        if (isSDExist) {
            String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            File saveDir = new File(sdPath, "叮叮羊微集");
            if (!saveDir.exists()) {
                saveDir.mkdir();
            }
            picFile = new File(saveDir, fileName);
            try {
                FileOutputStream out = new FileOutputStream(picFile); //文件输出流
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return picFile;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ClipData mClipData = ClipData.newPlainText("test", null);
        manager.setPrimaryClip(mClipData);
    }
}
