package com.main.TCPService;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.JSONObject;
import org.yanzi.shareserver.Client;
import org.yanzi.shareserver.Manager;
import org.yanzi.shareserver.UnregisteredId;

import com.main.activity.MyApplication;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

/*
 * 与MK5连接服务
 */
public class TCPService extends Service {

	private JSONObject out = new JSONObject();
	private final static String TAG = "TCPService";

	// 心跳时间 修改
	private static final long HEART_BEAT_RATE = 1 * 500; // 0.5秒
	private static final long MAX_HEART_BEAT_RATE = 3 * 1000;// 3秒

	public static long sendTime = 0L;
	public static long rcvTime = 0L;
	ServiceThread serviceThread;
	// private Intent intent = new Intent("org.yanzi.shareserver.receiver");
	String[] inputstr = new String[14];
	String str = "";
	String matches = "";
	ServerSocket serversocket = null;
	public static boolean Flag_Connection_MK5 = false;//与MK5连接
	
	/** 弱引用 在引用对象的同时允许对垃圾对象进行回收  */
	private WeakReference<Socket> mSocket;
	
	//开启socket线程的次数
	private int count = 0;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();

		Log.i(TAG, "TCPServiceBroadcastReceiver------onCreat()中---");
		try {

			Log.i(TAG, "----------创建  soket 端口 8080-----------");
			serversocket = new ServerSocket(8080);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Log.i(TAG, "------------准备进入心跳程序----------");
		// 初始化成功后，就准备发送心跳包
		MyApplication.scheduledThreadPool.scheduleAtFixedRate(
				heartBeatRunnable, 0, HEART_BEAT_RATE, TimeUnit.MILLISECONDS);
		// mHandler.postDelayed(heartBeatRunnable, HEART_BEAT_RATE);
	}

//	private Handler mHandler = new Handler();
	private Runnable heartBeatRunnable = new Runnable() {

		@Override
		public void run() {
			if (System.currentTimeMillis() - sendTime >= HEART_BEAT_RATE) {
				Client ct = Manager.getManager().getClient("HeartBeat");
				if (ct == null) {
					Log.i(TAG, "------ct(hearBeat)= null-----");
					Flag_Connection_MK5 = false;
					//Client.Other_Car_map.clear();//心跳为空，要把里面Other_Car_map的成员清除
				} else {
					// boolean isSuccess = sendMsg("1", ct);// 心跳消息
					sendMsg("1", ct);// 向MK5发送 心跳消息
					Flag_Connection_MK5 = true;
					Log.i(TAG, "发送出去  sendMsg(1, ct)--------------- ");

					if (rcvTime > 0
							&& System.currentTimeMillis() - rcvTime >= MAX_HEART_BEAT_RATE) {
						// mHandler.removeCallbacks(heartBeatRunnable);
						rcvTime = 0;

						Log.i(TAG, "deleteAll()------");
						Manager.getManager().deleteAll();
						

						/*
						 * 2016年1月6日11:36:42 加入代码 还需测试
						 */
						// 删除list中的集合元素
						for (int i = 0; i < UnregisteredId.list.size(); i++) {
							// System.out
							// .println("[TCPService]---删除       list  中的 id等元素");
							Log.i(TAG, "[TCPService]---删除       list  中的 id等元素");
							UnregisteredId.list.remove(i);
						}
						// 删除map集合 中的元素包括 经纬度
//						Set<Integer> set = Client.Other_Car_map.keySet();
//						Iterator<Integer> it = set.iterator();
//						while (it.hasNext()) {
//
//							/*
//							 * map集合中 在迭代过程中 不允许
//							 * 添加和删除元素，否则会异常，如果要删除元素，就要用迭代器来删除。
//							 */
//
//							int key = (int) it.next();
//							it.remove();
//							// System.gc();
//						}
					}
				}
			}
			//延迟HEART_BEAT_RATE的时间 ，继续跑heartBeatRunnable 线程。
//			MyApplication.scheduledThreadPool.schedule(heartBeatRunnable, HEART_BEAT_RATE,
//					TimeUnit.MILLISECONDS);
			// mHandler.postDelayed(this, HEART_BEAT_RATE);

		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		if (serviceThread == null) {
			serviceThread = new ServiceThread();
			serviceThread.start();
		}

		return super.onStartCommand(intent, flags, startId);
	}

	// @Override
	// @Deprecated
	// public void onStart(Intent intent, int startId) {
	// // TODO Auto-generated method stub
	// super.onStart(intent, startId);// 版本过旧，需要更新环境，用最新的环境代替。
	// System.out.println("----------TcpServer   进入啦-----------");
	// serviceThread = new ServiceThread();
	// serviceThread.start();
	// }

	public boolean sendMsg(String msg, Client ct) {
		if (Manager.getManager().getClientNumber() <= 0) {
			return false;
		}

		if (out.has("LIVE"))
			out.remove("LIVE");

		try {
			out.put("LIVE", msg);
			// Log.i(TAG, "out.put(LIVE, msg);---------------");

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.i(TAG, "发送 心跳异常！");
		}

		if (!ct.socket.isClosed() && !ct.socket.isOutputShutdown()) { // socket
																		// 通行端口
																		// 是否关闭
			Manager.getManager().publish(ct, out);
			// System.out.println("----send data : LIVE ----------");
			// out.put("LIVE", msg);
			Log.i(TAG, "----send data : LIVE ----------");

		} else {
			// wkl20151117
			// 需要删除 所有 创建 通信处理对象关闭 ！

			Log.i(TAG,
					"[TCPService]-----ct.socket.isClosed()或者ct.socket.isOutputShutdown()");
			Log.i(TAG, "SocketClose");

			return false;
		}
		sendTime = System.currentTimeMillis();
		return true;
	}

	// 开启 与 MK5连接的服务器
	public void startService(Context c) {
		Intent iService = new Intent(c, TCPService.class);
		iService.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		c.startService(iService);
	}

	public void stopService(Context c) {
		// TODO Auto-generated method stub
		Log.i(TAG, "----service:onstopService()----------");
		Log.i(TAG, "stopService--------------");
		// MyApplication.serviceFlag=false;//标志位，用来关闭 定时service

		// Manager.getManager().deleteAll();
		// UnregisteredId.list.clear();

		Intent iService = new Intent(c, TCPService.class);
		iService.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		c.stopService(iService);
		stopSelf();
		onDestroy();
		// if(serviceThread!=null){
		// serviceThread.stop();
		// }
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, "TCPService onDestroy()--------");

		if (serversocket != null) {
			try {
				serversocket.close();
				Log.i(TAG, "serversocket.close()--------");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		super.onDestroy();
	}

	/*
	 * 接受 MapActivity 和 DisplayActivity 中 退出信号广播接收器，以此来关闭TCPService
	 */
	// public class TCPServiceBroadcastReceiver extends BroadcastReceiver{
	//
	// @Override
	// public void onReceive(Context context, Intent intent) {
	// Log.i(TAG, "TCPServiceBroadcastReceiver------关闭TCPService（）");
	// stopSelf();
	// }
	//
	// }
	/**
	 * 向Activity发送UI信息， 发送广播，通知改变UI
	 */
	// public void sendBroadcastString(String s, String path2) {
	// intent.putExtra("INFO", s + " = " + path2 + "\n");
	// sendBroadcast(intent);
	// intent.removeExtra("INFO");
	// }
	//
	// public void sendBroadcastString1(String s) {
	// intent.putExtra("INFO", s + "\n");
	// sendBroadcast(intent);
	// intent.removeExtra("INFO");
	// }
	//
	// public void sendBroadcastString1(int l) {
	// intent.putExtra("INFO", l + "\n");
	// sendBroadcast(intent);
	// intent.removeExtra("INFO");
	// }

	class ServiceThread extends Thread {

		@Override
		public void run() {

			while (true) {
				try {
					count++;
					// System.out.println("----------等待        客户端-----------");
					Socket socket = serversocket.accept();
					Log.i(TAG, "----------有客户端连接        客户端-----------" +count);
					Client cs = new Client(socket, TCPService.this);
					cs.start();
					Manager.getManager().add(cs);

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	

}
