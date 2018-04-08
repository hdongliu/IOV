package com.Li.serviceThread;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.JSONObject;
import org.yanzi.shareserver.Car_Data;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.Li.data.GroupData;
import com.Li.register.RegisterActivity;
import com.main.activity.MainActivity;
import com.main.activity.MyApplication;
import com.main.activity.OverlayDemo;

import Utili.Package.LogUtil;

public class ServiceClient extends Thread {
	private Intent intent = new Intent("com.Li.ServiceThread.receiver");
	// COME AND CHANGE FLAG NAME
	public static boolean isConnect = true;
	public static boolean isConnectService = true;
	public Socket socketClient;

	private BufferedWriter bos;
	private BufferedReader br;
	private InputStreamReader isr;
	private OutputStreamWriter ls;
	Context context;
	private final static String TAG = "ServiceClient";
	String matches = "";
	
	//定义Map集合 存储后台发过来的群组内 其他成员信息
	public static Map<String, Car_Data> mapGroup = new HashMap<String, Car_Data>();
	
	//定义Map集合 存储后台发过来的群组成员位置信息
	public static HashMap<Integer, Car_Data> cargroup_member_Location = new HashMap<Integer, Car_Data>();
       
	//定义Map集合 存储后台发过来的已经存在的车群组 信息
    public static HashMap<Integer, CarGroup> carGroup_Exist = new HashMap<Integer, CarGroup>();
		
	String chatContent, from_id;//群组聊天信息, 信息哪个车
	String announcementContent;//后台推送的车队公告信息
	
	public ServiceClient(Socket s, Context c) {
		this.socketClient = s;
		this.context = c;
	}

	public void run() {
		try {
			Log.i(TAG, "ServiceClient is running");
			ls = new OutputStreamWriter(socketClient.getOutputStream());
			bos = new BufferedWriter(ls);
			isr = new InputStreamReader(socketClient.getInputStream());
			br = new BufferedReader(isr);
			// bw初始化过后，回传一个信号给MainActivity（），告诉它 已经初始化ok。
			MainActivity.initServiceThread = true;
			// 在线程池中取出线程 循环解析后台传来的消息
			ExecutorService fixedThreadPool = Executors.newFixedThreadPool(5);

			// 线程池中定时开启线程 检测与后台的连接
			MyApplication.scheduledThreadPool.scheduleAtFixedRate(
					connectRunnable, 0, 3000, TimeUnit.MILLISECONDS);
			while (isConnect) {
				if (isConnectService) {// 首先判断其他界面有没有点击 退出按钮，若点击了
										// 则这里的标志位会职位false
					// 读取传过来的数据
					while ((matches = br.readLine()) != null) {
						Log.i(TAG, "-----------" + matches);
						fixedThreadPool.execute(command);
					}
				} else {
					Log.i(TAG, "serviceClient() 关闭");
					MyApplication.scheduledThreadPool.wait();
					bos.close();
					br.close();
					ls.close();
					isr.close();
					isConnect = false;
				}
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*************************************************/
	Runnable command = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				parseJson(matches);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	};
	/*************************************************/
	/*
	 * 检测 与后台链接
	 */
	Runnable connectRunnable = new Runnable() {
		@Override
		public void run() {
			if (socketClient.isInputShutdown()
					&& socketClient.isOutputShutdown()
					&& socketClient.isClosed()) {

				if (bos != null && br != null && ls != null && isr != null) {

					try {
						bos.close();
						br.close();
						ls.close();
						isr.close();
						socketClient.close();
						isConnect = false;

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			socketClient = null;
		}
	};
	

	public void send(JSONObject headParam) {
		try {
			bos.write(headParam.toString());
			bos.flush();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 绑定client 来发送Jason包
	public void ServiceSend(ServiceClient sc, JSONObject out) {
		sc.send(out);
	}

	// 解析json包
	public void parseJson(String strResult) throws JSONException,UnsupportedEncodingException {

		if (strResult != null) {

			JSONObject connetJSon = new JSONObject(strResult);

			// 注册 解析
			if (connetJSon.has("isSuccess")) {
				sendRegisterLogin(connetJSon.getBoolean("isSuccess"), connetJSon.getString("content"));
			}
			/*
			 * 登录 解析
			 */
			if (connetJSon.has("isSuccesslogin")) {
				String loginContent = connetJSon.getString("content");
				if ((Boolean) connetJSon.get("isSuccesslogin")) {
					if (MyApplication.register_login){
						sendRegisterLoginSuccess();
						MyApplication.register_login = false;
					} else {
						sendLogin(loginContent);
					}
					
				} else {
					sendLogin(loginContent);
				}
			}

			//
			if (connetJSon.has("EmergencyPushBackGround")) {
				MyApplication.houtai_msg_rcvtime = System.currentTimeMillis();
				MyApplication.houtai_msg_push = connetJSon
						.getString("EmergencyPushBackGround");
			}

		    //车群组创建返回信息
			if (connetJSon.has("isGroupSuccess")) {
				if ((Boolean) connetJSon.get("isGroupSuccess")) {
					// 如果为后台传来的值为true 则发送广播
					sendGroupSuccess();
				} else {
					Looper.prepare();
					Toast.makeText(MyApplication.getcontext(),
							"对不起，您注册的信息有误，请从新注册！", Toast.LENGTH_SHORT).show();
					Looper.loop();
				}
			}
			
			// 解散车队
			if (connetJSon.has("isDismissSuccess")) {
				if (connetJSon.getBoolean("isDismissSuccess")) {
					// 如果为后台传来的值为true 则发送广播
				    cargroup_member_Location.clear();
				    sendDeleteSuccess();
				} else {
					Looper.prepare();
					Toast.makeText(MyApplication.getcontext(),"对不起，您删除的信息有误，请从新删除！", Toast.LENGTH_SHORT).show();
					Looper.loop();
				}
			}
			
			// 解析 后台发送来的 邀请请求 是否发送成功
			if(connetJSon.has("isAskSuccess")){
				MyApplication.AskRequest = connetJSon.getBoolean("isAskSuccess");
				sendInviteAsk();
			}
			
			// 解析 被邀请车辆是否同意被邀请
			if(connetJSon.has("isInviteSuccess")){
				Log.i(TAG, "邀请同意了--------------------------------");
				Boolean isAskSuccess = connetJSon.getBoolean("isInviteSuccess");
				String AskReturnContent = connetJSon.getString("content");
				sendAskAnswer(isAskSuccess, AskReturnContent);
				Log.i(TAG, connetJSon.getString("content")+"----------------------------------");
				Log.i(TAG, AskReturnContent+"------------------------------------------------");
			}
			
			String datatype = connetJSon.getString("datatype");
			Log.i(TAG, datatype+"----------------------------------");
			// 后台发送的邀请请求信息
			if ("TEAM_INVITE".equals(datatype)) {
				Log.i(TAG, "team_invite----------------------------------");
				MyApplication.editName = connetJSon.getString("team_name");
				
				MyApplication.veh_lpn = connetJSon.getString("fromid");
				OverlayDemo.car_group_id_random = connetJSon.getString("team_id");
				MyApplication.teamID = connetJSon.getString("team_id");
				
				String teamStart = connetJSon.getString("team_start");
				String teamEnd = connetJSon.getString("team_end");
				
				sendAddCarRegister(teamStart, teamEnd);
				Log.i(TAG, "team_invite111111111----------------------------------");
			}
			
			// 解析 后台发送来的车队成员
			if("MEMBER_UPDATE".equals(datatype)){
				MyApplication.member_update = strResult;
				sendGroupMember();
			}
			
			// 解析 后台发送来的车队成员 经纬度信息
			if("MEMBER_LOCATION_DATA".equals(datatype)){
				try{
					for(int i = 1; i <= (connetJSon.length()-2)/3; i++){
						// 把非本车的经纬度添加到cargroup_member_Location
						if (!(connetJSon.getString("veh"+i+"_id").equals(MyApplication.user_name)) ){
							Car_Data mCar_Data = new Car_Data();
							mCar_Data.lat_cloud = Double.valueOf(connetJSon.getString("veh"+i+"_lat"));
							mCar_Data.longi_cloud = Double.valueOf(connetJSon.getString("veh"+i+"_lon"));
							mCar_Data.user_name = connetJSon.getString("veh"+i+"_id");
							cargroup_member_Location.put(i , mCar_Data);
						}
					}	
					Log.i(TAG, "车队成员位置信息"+strResult);
				}catch(JSONException e){
					e.printStackTrace();
				}
			}
			
			// 解析 后台发送来的车群组聊天信息
			if("MEMBER_CHAT".equals(datatype)){
				chatContent = connetJSon.getString("chatContent");	
				from_id = connetJSon.getString("from_id");	
				sendChat(chatContent, from_id);
			}
			
			// 解析 后台推送的车队公告信息
			if("team_announce".equals(datatype)){
				announcementContent = connetJSon.getString("announce_content");	
				sendAnnouncement(announcementContent);
			}
			
			// 解析 后台发送的已经存在的车群组 信息
			if("TEAM_UPDATE".equals(datatype)){
				carGroup_Exist.clear();
				try{
					for(int i = 1; i <= (connetJSon.length()-1)/2; i++)
					{
					    CarGroup mcarGroup= new CarGroup();
					    mcarGroup.team_id = String.valueOf(connetJSon.getString("team"+i+"_id"));
					    mcarGroup.team_name = String.valueOf(connetJSon.getString("team"+i+"_name"));
					    carGroup_Exist.put(i , mcarGroup);
					}	
				}catch(JSONException e){
					e.printStackTrace();
				}
				sendGroupExist();
			}
						
			/*
			 * 根据String类型的车辆id  来存储群组成员信息
			 */
//			String groupMember=connetJSon.getString("fromid");
//			
//			if(mapGroup.containsKey(groupMember)){
//				
//			}else{
//				sendAddCarRegister();
//				MyApplication.editName = connetJSon.getString("team_name");
//				MyApplication.veh_lpn = connetJSon.getString("fromid");
//				OverlayDemo.car_group_id_random = connetJSon.getString("team_id");
//				
//			}
//			
//			if (connetJSon.has("team_id")) {
//				MyApplication.teamID = connetJSon.getString("team_id");
//			}

		}
	}

	/*
	 * 当收到后台isSuccesslogin=true 并且 isRegister = false
	 */
	// 发广播进入,在MAIN ACT 里接收广播跳到TAB ACTIVITY WYL
	public void sendLogin(String s) {
		Intent intent = new Intent("com.Li.ServiceClient.sentLogin");
		intent.putExtra("loginContent", s);
		context.sendBroadcast(intent);
	}

	// 第一次注册成功后，立即登录后后台回复的登录成功广播
	public void sendRegisterLoginSuccess() {
		Intent intent1 = new Intent("com.Li.ServiceClient.sentRegisterLoginSuccess");
		context.sendBroadcast(intent1);
	}
	

	// 向后台发送登陆成功注册的JSON车辆信息在 registeractivity MsgReceiverServiceRegisterLogin
	/*
	 * broadcaster receiver get the bradcaster and send register info to houtai
	 * in regiteractivity MsgReceiverServiceRegisterLogin set isRegister = true
	 */
	// 在注册活动中收到广播，广播接收器MsgReceiverServiceRegisterLogin，向后台发送注册信息，并置isRegister =
	// true
	public void sendRegisterLogin(boolean b, String s) {
		Intent intent1 = new Intent("com.Li.ServiceClient.sentRegisterLogin");
		intent1.putExtra("isRegister", b);
		intent1.putExtra("content", s);
		context.sendBroadcast(intent1);
	}

	/*
	 * 当收到后台isSuccesslogin=true 并且 isRegister = true 发广播，广播接收器在REGISTER
	 * ACTIVITY使程序跑到TAB -->MAP活动 WYL
	 */
	public void sendRegisterLoginTrue() {
		Intent intent1 = new Intent(
				"com.Li.ServiceClient.sendRegisterLoginTrue");
		context.sendBroadcast(intent1);
	}

	public void sendGroupSuccess() {
		Intent intent2 = new Intent("com.Li.ServiceClient.sendGroupSuccess");
		context.sendBroadcast(intent2);
	}

	public void sendDeleteSuccess() {
		Intent intent2 = new Intent("com.Li.ServiceClient.sendDeleteSuccess");
		context.sendBroadcast(intent2);
	}
	
	public void sendAddCarRegister(String start, String end){
		Intent intent = new Intent("com.Li.ServiceClient.sendAddCarRegister");
		intent.putExtra("team_start", start);
		intent.putExtra("team_end", end);
		context.sendBroadcast(intent);
	}
	
	public void sendIsInviteSuccess(){
		Intent intent2 = new Intent("com.Li.ServiceClient.sendIsInviteSuccess");
		context.sendBroadcast(intent2);
	}
	
	public void sendGroupMember(){
		Intent intent2 = new Intent("com.Li.ServiceClient.sendMemberUpdate");
		context.sendBroadcast(intent2);
	}
	
	public void sendInviteAsk(){
		Intent intent2 = new Intent("com.Li.ServiceClient.sendInviteAsk");
		context.sendBroadcast(intent2);
	}
	
	public void sendAskAnswer(Boolean b, String s){
		Intent intent = new Intent("com.Li.ServiceClient.sendAskAnswer");
		intent.putExtra("isAskSuccess", b);
		intent.putExtra("AskReturnContent", s);
		context.sendBroadcast(intent);
	}
	
	public void sendChat(String s, String t) {
		Intent intent = new Intent("com.Li.ServiceClient.sendChat");
		intent.putExtra("CHAT", s);
		intent.putExtra("FROM", t);
		context.sendBroadcast(intent);
		intent.removeExtra("CHAT");
		intent.removeExtra("FROM");
	}
	
	public void sendAnnouncement(String s) {
		Intent intent = new Intent("com.Li.ServiceClient.sendAnnouncement");
		intent.putExtra("Announcement", s);
		context.sendBroadcast(intent);
		intent.removeExtra("Announcement");
	}

	public void sendGroupExist() {
		Intent intent = new Intent("com.Li.ServiceClient.GroupExistRegister");
		context.sendBroadcast(intent);
	}
	
}
