package com.Li.serviceThread;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
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

	/** 心跳检测时间  */
	private static final long HEART_BEAT_RATE = 1 * 1000;
	private static final long MAX_HEART_BEAT_RATE = 5 * 1000;

    // 服务器的ip地址和端口
	private static final String remoteIp = "113.250.155.170";
	private static final int port = 8888;
	
	ServerConnectThread serverConnectThread;
	/** 弱引用 在引用对象的同时允许对垃圾对象进行回收  */
	private WeakReference<Socket> mSocket;
	public static ServiceClient serviceClient;

	//接收与发送时间
	private static long sendTime = 0L;
	public static long rcvTime = 0L;

//	public static boolean isConnectService = true;

	private BufferedWriter bw;

	// 发送心跳包
	private Handler mHandler = new Handler();
	private Runnable heartBeatRunnable = new Runnable() {
		@Override
		public void run() {
			if (System.currentTimeMillis() - sendTime >= HEART_BEAT_RATE) {
				boolean isSuccess = sendMsg();// 就发送一个字符LIVE, 如果发送失败，就重新初始化一个socket
				Log.i(TAG, "run: 心跳数据发送是否成功"+isSuccess);
//                isConnectService = isSuccess;
				MyApplication.isServerConnect = isSuccess;
				if (!isSuccess || (rcvTime > 0 && System.currentTimeMillis() - rcvTime >= MAX_HEART_BEAT_RATE)) {
					Log.e(TAG, "run: 心跳超时与服务器未连接！");
					Log.e(TAG, "run: isSuccess="+isSuccess);
					mHandler.removeCallbacks(heartBeatRunnable);

//					releaseLastSocket(ClientManager.getManager().getClient().socketClient);
					ClientManager.getManager().deleteAll();

					rcvTime = 0;
//					isConnectService = false;
					MyApplication.isServerConnect = false;

					new ServerConnectThread().start();
				}
			}
			mHandler.postDelayed(this, HEART_BEAT_RATE);
		}
	};
	
	// 发送信息
	public boolean sendMsg() {
		if (null == ClientManager.getManager().getClient()) {
			return false;
		}

//		Socket soc = mSocket.get();

		Socket soc = ClientManager.getManager().getClient().socketClient;

		try {
			 if (!soc.isClosed() && !soc.isOutputShutdown()) {
				 Util.send_To_Clound(out);
//				 bw = new BufferedWriter(new OutputStreamWriter(soc.getOutputStream()));
//				 bw.write(out.toString());
//				 bw.flush();
//				 Log.i(TAG, "sendMsg: "+out.toString());
				 sendTime = System.currentTimeMillis();// 每次发送成功数据，就改一下最后成功发送的时间，节省心跳间隔时间
//				 Log.i(TAG, "发送成功的时间：" + sendTime);
			 } else {
				 return false;
			 }

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	// 释放socket
	private void releaseLastSocket(Socket mSocket) {
		try {
			if (null != mSocket) {
//				Socket sk = mSocket.get();
				if (!mSocket.isClosed()) {
					mSocket.close();
				}
//				sk = null;
//				mSocket = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();

		try {
			out.put("datatype", "LIVE");
			out.put("fromtype", "veh");
		} catch (JSONException e) {
			e.printStackTrace();
		}
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
//					Socket socketClient = new Socket("113.251.164.234", 8888);// 后台IP端口号//TCP协议改成UDP协议
//					Socket socketClient = new Socket("202.202.43.240", 8888);// 学校服务器的ip地址
					Socket socketClient = new Socket(remoteIp, port);// 后台IP端口号，外网ip
//					Socket socketClient = new Socket("172.22.136.242", 8888);// 后台IP端口号 ,内网IP
					Log.i(TAG, "run: 连接成功");
//					mSocket = new WeakReference<Socket>(socketClient);
					Log.i(TAG, "已经连接到 后台！"); 
					serviceClient = new ServiceClient(socketClient, ServerConnectService.this);
					serviceClient.start();
					/*
					 * 将serviceClient加入 clientManager集合中
					 */
					ClientManager.getManager().add(serviceClient);
					MyApplication.isServerConnect = true;
					mHandler.postDelayed(heartBeatRunnable, HEART_BEAT_RATE);// 初始化成功后，就准备发送心跳包
					
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
