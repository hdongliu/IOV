package com.main.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import android.app.Application;
import android.content.Context;

import com.Li.serviceThread.ServerConnectService;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.model.LatLng;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.main.TCPService.TCPService;

public class MyApplication extends Application {

	protected static final String TAG = "MyApplication";
	public static TCPService MyService;
	public static ServerConnectService serverConnectService;
	private static Context context;
	public static long houtai_msg_rcvtime = 0;// 接收后台消息后 的时间
	public static String houtai_msg_push = null;
	public static String MK5Info = null;// 路测消息显示 内容
	public static boolean MK5InfoFlag = false;// 路测消息显示 标志位
	public static long MK5RecieveTime = 0; // 路测消息接收时当前时间
	public static int lightState = 0;
	public static boolean isMk5LatLng = false; //MK5是否发送经纬度过来标志位
	public static final double Scal_to_Covert = 100000;// 转化MK5发来的经纬度用的比例
	public static boolean isServerConnect = false;//与服务器连接的标志位

	public static int Lat_From_MK5 = 0;//MK5发来纬度
	public static int Long_From_MK5 = 0;//MK5发来经度
	public static LatLng LatLong_From_MK5;// 本车的位置

	public static int lightRemainTime = 0;
	public static ScheduledExecutorService scheduledThreadPool;
	public static boolean OBDFlag_display = false;
	public static long OBD_info_recv_Time = 0;
	public static String editName, editIntro, editStart, editEnd, editNote, veh_lpn;
	public static boolean isNetAvailable = true;
	public static String teamID = null;

	public static String member_update = null;
	public static String member_location = null;

	public static double lat = 0;
	public static double loge = 0;// 算是白杰文那边的经度与纬度

	public static String s_obd_VBAT = null;
	public static String s_obd_RPM = null;
	public static String s_obd_SPD = null;
	public static String s_obd_TP = null;
	public static String s_obd_LOD = null;
	public static String s_obd_ECT = null;
	public static String s_obd_FLI = null;
	public static String s_obd_MPH = null;
	
	public static boolean AskRequest ;
	
	public static String carNumber = null;
	public static String Password = null;
	public static String user_name = null;
	
	public static int MK5Scene = 0; //防撞预警场景类型、预警等级数据
	public static long MK5CarwarnningReciveTime = 0;//用于防撞预警比较时间
	
	public static boolean register_login = false ;//注册时登录的标志位
	
	public static boolean team_dismiss = false;//车群组解散标志位

	@Override
	public void onCreate() {
		// 应用程序入口处调用,避免手机内存过小，杀死后台进程,造成SpeechUtility对象为null
		// 设置你申请的应用appid
		// 在使用 SDK 各组间之前初始化 context 信息，传入 ApplicationContext

//		StringBuffer param = new StringBuffer();
//		param.append("appid=" + getString(R.string.app_id));
//		// 设置使用v5+
//		param.append(SpeechConstant.ENGINE_MODE + "=" + SpeechConstant.MODE_MSC);
		SpeechUtility.createUtility(MyApplication.this,  "appid=" + getString(R.string.app_id));
		MyService = new TCPService();
		serverConnectService = new ServerConnectService();
		// 获取全局变量，以便程序调用
		context = getApplicationContext();
		// 开启 Log日志
		// LogcatHelper.getInstance(this).start();
		SDKInitializer.initialize(this);
		super.onCreate();
		/*
		 * 这里创建一个全局的线程池，可以自己定时多长时间跑一次线程，这里设置的是100个线程，
		 * 以后根据项目的需求，可以进行增添，代码中实时监听MK5标志位的线程都会从这里取。这样就不会 因为无限的开线程，导致系统内存的开销过大。
		 */
		scheduledThreadPool = Executors.newScheduledThreadPool(100);

	}

	public static Context getcontext() {
		return context;
	}

}
