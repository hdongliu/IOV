package com.Li.serviceThread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.JSONObject;
import org.yanzi.shareserver.Client;
import org.yanzi.shareserver.Manager;
import org.yanzi.shareserver.UnregisteredId;

import com.Li.data.SharePreferenceUtil;
import com.main.activity.MainActivity;
import com.main.activity.MyApplication;

import Utili.Package.LogUtil;
import Utili.Package.Util;

import com.main.activity.MainActivity.MsgReceiverService;
import com.main.activity.MainActivity.MsgReceiverServiceFalse;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

/*
 * 与服务器连接服务
 */
public class ServerConnectService extends Service {

	private JSONObject out = new JSONObject();
	private final static String TAG = "ServerConnectService";

	ServerConnectThread serverConnectThread;
	public Socket socketClient;
	public static ServiceClient serviceClient;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		serverConnectThread = new ServerConnectThread();
		serverConnectThread.start();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	// 开启 与 后台连接的服务
	public void startService(Context c) {
		Intent iService = new Intent(c, ServerConnectService.class);
		iService.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		c.startService(iService);
	}

	public void stopService(Context c) {
		// TODO Auto-generated method stub
		Log.i(TAG, "----service:onstopService()----------");
		Log.i(TAG, "stopService--------------");

		Intent iService = new Intent(c, ServerConnectService.class);
		iService.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		c.stopService(iService);
		stopSelf();
		onDestroy();
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, "TCPService onDestroy()--------");
		super.onDestroy();
	}

	private class ServerConnectThread extends Thread {
		public void run() {
			if (Util.isNetworkAvailable(ServerConnectService.this) ) {
				try {
					Log.i(TAG, "准备连接到 后台！");
	//				socketClient = new Socket("113.251.164.234", 8888);// 后台IP端口号//TCP协议改成UDP协议
	//				socketClient = new Socket("113.250.156.102", 8888);// 后台IP端口号//TCP协议改成UDP协议
					socketClient = new Socket("113.251.223.79", 8888);// 后台IP端口号//TCP协议改成UDP协议
					Log.i(TAG, "已经连接到 后台！"); 
					serviceClient = new ServiceClient(socketClient, ServerConnectService.this);
					serviceClient.start();
					ClientManager.getManager().add(serviceClient);
					MyApplication.isServerConnect = true;
					/*
					 * 将serviceClient加入 clientManager集合中
					 */
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
		    }
	   }
    }
}
