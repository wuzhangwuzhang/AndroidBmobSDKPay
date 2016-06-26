package com.wuzhang.bmobpay;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.bmob.pay.tool.BmobPay;
import com.bmob.pay.tool.PayListener;
import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;

import cn.bmob.sms.BmobSMS;
import cn.bmob.sms.bean.BmobSmsState;
import cn.bmob.sms.exception.BmobException;
import cn.bmob.sms.listener.QuerySMSStateListener;
import cn.bmob.sms.listener.RequestSMSCodeListener;
import cn.bmob.sms.listener.VerifySMSCodeListener;

public class MainActivity extends UnityPlayerActivity {
    private Integer smsID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main); 设置当前的显示布局，该界面不需要
          BmobPay.init(this,"6afa1c017b8248bdee8e19ebedf07a9d");
//            BmobPay.init(this,"729ebe6bbb6481cb2c98ad44bc009fc4");
    }

    //这是安卓提供的方法供Unity调用
    public int test1(int a,int b)
    {
        //参数一:游戏物体
        //参数二:方法名称
        //参数三:传入参数
        UnityPlayer.UnitySendMessage("Main Camera", "test2", "这是Android调用Unity的方法:I am android!");//如果此处参数拼写错误将不会报错，坑爹
        return a+b;
    }

    //支付宝支付
    public void payByZhiFuBao(float price,String goodsName)
    {
        //以为这样可以解决支付宝支付问题，然并卵
        //BmobPay.init(this,"6afa1c017b8248bdee8e19ebedf07a9d");
        new BmobPay(MainActivity.this).pay(price, goodsName, new PayListener() {
            @Override
            public void orderId(String s) {
            }

            @Override
            public void succeed() {
                onPayResultReturn(1, "");
            }

            @Override
            public void fail(int i, String s) {
                onPayResultReturn(0, s);
            }

            @Override
            public void unknow() {
                onPayResultReturn(2, "网络故障");
            }
        });
    }

    //微信支付
    public void payByWeiXin(float price,String goodsName){
        new BmobPay(MainActivity.this).payByWX(price, goodsName, new PayListener() {
            @Override
            public void orderId(String s) {

            }

            @Override
            public void succeed() {
                onPayResultReturn(1, "");
            }

            @Override
            public void fail(int i, String s) {
                onPayResultReturn(0, s);
            }

            @Override
            public void unknow() {
                onPayResultReturn(2, "网络故障");
            }
        });
    }

    //1;支付成功
    //0:支付失败
    //2:未知原因
    //isSuccess:支付是否成功
    //支付失败原因
    public void onPayResultReturn(int isSuccess,String reason)
    {
        String gameObjectName = "BmobPay";
        String methodName = "OnPayResultReturn";
        String arg0 = isSuccess+"|"+reason;
        UnityPlayer.UnitySendMessage(gameObjectName, methodName, arg0);
    }

    /**********************************************************
     * 短信验证码
     * 1，获取验证码
     * 2，验证
     * 3，查询验证状态
     ********************************************************/
    //第一步：获取验证码
    public void requestSendMsg() {
        onCoderReturn("获取 " + "15839930217" + " 的验证码,,,");
        BmobSMS.requestSMSCode(MainActivity.this, "15839930217", "短信验证", new RequestSMSCodeListener() {
            @Override
            public void done(Integer smsId, BmobException ex) {
                // TODO Auto-generated method stub
                if (ex == null) {//验证码发送成功
                    smsID = smsId;
                    Log.i("bmob", "短信id：" + smsId);//用于查询本次短信发送详情
                    onCoderReturn("获取验证码OK：" + smsID);
                }
            }
        });
    }
    //第二步：验证短信码
    public void verifiyCoder(String coder)
    {
        onCoderReturn("开始验证:"+coder);
        BmobSMS.verifySmsCode(MainActivity.this, "15839930217", coder, new VerifySMSCodeListener() {
            @Override
            public void done(BmobException ex) {
                // TODO Auto-generated method stub
                if (ex == null) {//短信验证码已验证成功
                    Log.i("bmob", "验证通过");
                    onCoderReturn("短信验证成功");
                } else {
                    Log.i("bmob", "验证失败：code =" + ex.getErrorCode() + ",msg = " + ex.getLocalizedMessage());
                    onCoderReturn("短信验证失败");
                }
            }
        });
    }

    //第三步：查询验证状态
    public void queryState(String smsid)
    {
        BmobSMS.querySmsState(MainActivity.this, Integer.parseInt(smsid), new QuerySMSStateListener() {
            @Override
            public void done(BmobSmsState bmobSmsState, BmobException e) {
                if (e == null) {
                    Log.i("bmob", "短信状态：" + bmobSmsState.getSmsState() + ",验证状态：" + bmobSmsState.getVerifyState());
                    onCoderReturn(bmobSmsState.getSmsState() + " " + bmobSmsState.getVerifyState());
                }
            }
        });
    }

    //状态返回Unity
    public void onCoderReturn(String state )
    {
        String gameObjectName = "Main Camera";
        String methodName = "OnCoderReturn";
        String arg0 = state;
        UnityPlayer.UnitySendMessage(gameObjectName, methodName, arg0);
    }
}
