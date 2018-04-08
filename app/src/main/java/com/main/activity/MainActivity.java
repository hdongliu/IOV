package com.main.activity;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.IntentCompat;
import android.util.Log;
import android.view.Window;

import com.Li.carMakerService.CarMakerService;
import com.Li.data.SharePreferenceUtil;
import com.Li.register.LoginActivity;
import com.Li.register.RegisterActivity;
import com.Li.serviceThread.ClientManager;
import com.Li.serviceThread.ServiceClient;
import com.main.chart.DisplayActivity;
import Utili.Package.Util;

public class MainActivity extends Activity {
	public static JSONObject mainActicityHeadParam = new JSONObject();
//	public static boolean isNetAvailable = true;
	public Socket socketClient;
	public SharePreferenceUtil util;
	public static boolean isSuccesslogin = false;
	// public ServiceThread serviceThread;
	private MsgReceiverService msgReceiverService;
	private MsgReceiverServiceFalse msgReceiverServiceFalse;

	private String TAG = "MainActivity";
	/*
	 * 与后台连接FLAG true when connected
	 */
	// public static boolean connetisTrue = false;

	public Handler handler;

	public String tag = "MainActivity";
	private boolean isTrue = true;
	public static boolean initServiceThread = false;
	
	public static ServiceClient serviceClient;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		
		/*
		 * carMaker通信
		 */
		Intent intentCar=new Intent(MainActivity.this,CarMakerService.class);
		startService(intentCar);
		
		
//		Intent intent1 = new Intent(MainActivity.this,
//				RegisterActivity.class);
//		startActivity(intent1);
		
		/*
		 * 开启 与 服务器连接的服务 
		 */
		MyApplication.serverConnectService.startService(MainActivity.this);
		
		/*
		 * 开启 与 MK5连接的服务器
		 */
		MyApplication.MyService.startService(MainActivity.this);
		
		Intent intentLogin= new Intent(this,LoginActivity.class);
		startActivity(intentLogin);
		 
		// 与冯旭MK5连接，显示交通报警图片，开启UDP service
//		Intent udpService = new Intent(this, UDPservice.class);
//		startService(udpService);

//		 Intent intent= new Intent(this,AutoActivity.class);
//		 startActivity(intent);
//		 MainActivity.this.finish();
	}


	/**
	 * 
	 * 接收serviceClient ()发过来的广播，在此类中显示
	 */
	/*
	 * 接收 登录的广播进入TAB ACTIVITY WYL 在ServiceClient里第一次收到"isSuccesslogin":true 在
	 * ServiceClient sendLogin();里发送广播，这里收到，跳到Tab activity 验证成功，进入
	 * TabMainActivity（）界面
	 */
	public class MsgReceiverService extends BroadcastReceiver {

		public void onReceive(Context context, Intent intent) {
			Log.i(TAG, "{MsgReceiverService} 接受到了---准备进入 TabMainActivity");
			Intent intent1 = new Intent(MainActivity.this,
					TabMainActivity.class);
//			 Intent intent1 = new Intent(MainActivity.this,
//			 MapActivity.class);
			// Intent intent1 = new Intent(MainActivity.this,
			// RegisterActivity.class);
			startActivity(intent1);
			MainActivity.this.finish();
		}

	}

	/*
	 * 当第一次收到isSuccesslogin":false时，在ServiceClient 的
	 * sendRegister()发出广播，这里收到广播跳到register activity进行注册
	 */
	/*
	 * 验证失败 进入注册界面
	 */
	public class MsgReceiverServiceFalse extends BroadcastReceiver {

		public void onReceive(Context context, Intent intent) {
			Log.i(TAG, "【MsgReceiverServiceFalse】------注册进入了");
			Intent intent1 = new Intent(MainActivity.this,
					RegisterActivity.class);
			startActivity(intent1);

			MainActivity.this.finish();

		}

	}

	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		Log.i(TAG, "onStart");

	}

	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.i(TAG, "onResume");
	}



	/*
	 * 弹出 ------亲！您的网络连接未打开哦------------ 方法
	 */
	private void toast(Context context) {
		new AlertDialog.Builder(context)
				.setTitle("温馨提示")
				.setMessage("亲！您的网络连接未打开哦")
				.setPositiveButton("进入界面",
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {
								// 王永龙为长安临时改的，原来的是跳到TabMainActivity
//								 Intent intent = new Intent(MainActivity.this,
//								 MapActivity.class);
								Intent intent = new Intent(MainActivity.this,
										TabMainActivity.class);

								startActivity(intent);
								MainActivity.this.finish();

							}
						})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						MainActivity.this.finish();
						System.exit(0);// 退出程序
					}
				}).create().show();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		System.out.println("[MainActivity]-------onDestroy MainActivity");

	}

}
