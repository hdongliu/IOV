package com.main.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.yanzi.shareserver.Car_Data;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

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
	
	public static boolean isMk5LatLng = false; //MK5是否发送经纬度过来标志位
	public static final double Scal_to_Covert5 = 100000;// 转化MK5发来的经纬度用的比例
	public static final double Scal_to_Covert7 = 10000000;// 转化MK5发来的经纬度用的比例
	public static boolean isServerConnect = false;//与服务器连接的标志位
	public static int readCount = 0;

	public static int Lat_From_MK5 = 0;//MK5发来纬度
	public static int Long_From_MK5 = 0;//MK5发来经度
	public static LatLng LatLong_From_MK5;// 本车的位置
	
	public static int lightState = 0;//红绿灯状态
	public static int lightRemainTime = 0;//红绿灯时间
	public static int adviseSpeed = 0;//通过红绿灯时的建议速度
	public static int redLight = 0;//闯红灯标志
	public static long TrafficLightReciveTime = 0;

	public static int danger = 0;//预警危险
	public static long dangerReciveTime = 0;
	
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

	public static int highPriorityScene = 0; //防撞预警高优先级场景类型
	public static long highPriorityReciveTime = 0;//用于防撞预警高优先级场景比较时间
	public static int highPriorityDistance = 0;//预警距离
	
	public static boolean register_login = false ;//注册时登录的标志位
	
	public static boolean team_dismiss = false;//车群组解散标志位
	
	public static int randomVin ;//车辆随机生成的VIN
	
	public static boolean createstatus = false;
	public static String formationid; //编队ID
	public static int formationtype;
	public static long rcvTime_cfresponse;//创建编队回复时间
	
   
	public static int vehicle_numbers;//车队里的车辆数 
	public static String localvehid;//本车ID
	public static int vehnum;//本车序号
	public static Double curspeed;//本车当前速度
	public static Double advspeed;//本车建议速度
	public static Double curdistance;//当前距离
	public static Double advdistance;//建议距离
	public static String prevehid = null;//入队完成后前车的id

	public static Boolean joinflag = true;//编队时车辆加入同意标准
    
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
		SDKInitializer.initialize(context);
		super.onCreate();
		/*
		 * 这里创建一个全局的线程池，可以自己定时多长时间跑一次线程，这里设置的是100个线程，
		 * 以后根据项目的需求，可以进行增添，代码中实时监听MK5标志位的线程都会从这里取。这样就不会 因为无限的开线程，导致系统内存的开销过大。
		 */
		scheduledThreadPool = Executors.newScheduledThreadPool(100);
		
		if (getSharedPreferences("VIN", MODE_PRIVATE).getInt("veh_vin", 0) == 0){
			randomVin = (int)(System.currentTimeMillis()%1000000000);
			SharedPreferences.Editor editor = getSharedPreferences("VIN", MODE_PRIVATE).edit();
			editor.putInt("veh_vin", randomVin);
			editor.apply();
			
		} else {
			randomVin = getSharedPreferences("VIN", MODE_PRIVATE).getInt("veh_vin", 0);
		}
		
		
//		Log.d(TAG, "system current time is " + randomVin);
		

	}

	public static Context getcontext() {
		return context;
	}

}
