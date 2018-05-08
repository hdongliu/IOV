package org.yanzi.shareserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.text.Format;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.liu.Class.formation;
import com.liu.Class.joinadjust;
import com.liu.Class.joinresponse;
import com.liu.Class.leave;
import com.liu.Class.request;
import com.liu.Class.selfadjust;
import com.main.TCPService.TCPService;
import com.main.activity.MyApplication;
import com.main.activity.OverlayDemo;
import com.main.activity.RadarDemo.MyPagerAdapter;
import com.main.baiduMap.Group;
import com.main.baiduMap.MapActivity_val;

import Utili.Package.Util;

/*
 * 与MK5通信线程
 */
public class Client extends Thread {
	private final static String TAG = "Client";
	private String FLAG = "client";
	public Socket socket;
	Context context;
	InputStream is = null;
	OutputStream os = null;
	BufferedWriter bos;
	BufferedReader br;
	InputStreamReader isr;
	OutputStreamWriter ls;
	UnregisteredId ID;
	Integer sendId = null;
	Data1 data1 = new Data1();
	Data2 data2 = new Data2();
	private Intent intent = new Intent("org.yanzi.shareserver.receiver");
	int size;
	boolean isfirst = true;
	boolean isadd = true;
	boolean isdisconnect = true;
	private boolean issend = false;
	public String clientID;
	// static TreeSet<Data> tree = new TreeSet<Data>();
	public static HashMap<Integer, Car_Data> Other_Car_map = new HashMap<Integer, Car_Data>();
	private JSONObject headParam = new JSONObject();
	// public static boolean carWarning = false;
	// COME AND CHANGE FLAG NAME
	public static boolean carWarningCross = false;
	// COME AND CHANGE FLAG NAME
	public static boolean carWarningOvertaking = false;
	public static boolean Car_Vel_Guide_lock = false;// 由MK5发来的停止标志位
	public static int Car_Vel_Guide_flag = 0;
	// 添加红绿灯显示
	public static int trafficLight = 1;
	public static long MK5RecieveTimeTrafRed = 0;
	public static long MK5RecieveTimeTrafGreen = 0;
	public static long MK5RecieveTimeTrafYellow = 0;
	public static boolean MK5Flag = false;
	public static int LatLocalNumber;
	public static int LongLocalNumber;
	public static int idLocal;//来自于MK5的本车ID号
	public static JSONObject out = new JSONObject();
	public static int vin;
	static public String flagAlert = null;

	public static boolean MK5InfoFlagEm = false;// 紧急信息显示 标志位
	public static final int CONTROL_VIN = 1;
	private boolean PareseTaskIsFirst = true;
	private boolean isconnect = true;
	private boolean isnewmessage = false;
	int a = 0;
	int b = 0;
	public String matches = "";
	
	//定义Map集合 存储MK5发过来的车队状态信息
    public static HashMap<String, String> formation_status = new HashMap<String, String>();
    //定义Map集合 存储MK5发过来的附件车队信息
    public static HashMap<Integer, String> motorcade_list = new HashMap<Integer, String>();

    public static boolean motorcade_list_flag = true;
    public static boolean requestflag = true;
    
    private JSONObject json_motorcadelist = null;

	public Client(Socket s, Context c) {
		this.socket = s;
		this.context = c;
	}

	@Override
	public void run() {
		try {
			is = socket.getInputStream();
			os = socket.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}

		isr = new InputStreamReader(is);
		br = new BufferedReader(isr);
		ls = new OutputStreamWriter(os);
		bos = new BufferedWriter(ls);

		ExecutorService fixedThreadPool = Executors.newFixedThreadPool(5);
		try {
			while ((matches = br.readLine()) != null) {

				Log.v(TAG, "从Mk5接受到的信息为：" + matches);
				fixedThreadPool.execute(command);

			}
		} catch (Exception e) {
			try {
				Log.i("Manager1", "catch (IOException e)  中----------- ");
				isconnect = false;
				if (is != null) {
					Log.i("Manager1", "从   is dddddd11111：");
					is.close();
				}
				if (os != null) {
					Log.i("Manager1", "从    os    dddddd22222：");
					os.close();
				}

				if (socket != null) {
					Log.i("Manager1", "从       socket     dddddd33333：");
					socket.close();
				}
				e.printStackTrace();

			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	public void send(JSONObject headParam) {
		try {

			bos.write(headParam.toString());
			bos.flush();

			Log.i(TAG, "bos.flush()------headParam.toString()"+headParam.toString());

			// /clear send message wkl 20150726
			if (headParam.has("addID"))
				headParam.remove("addID");
			if (headParam.has("deleteID"))
				headParam.remove("deleteID");
			if (headParam.has("chatContent"))
				headParam.remove("chatContent");
			if (headParam.has("control"))
				headParam.remove("control");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	

	// 解析Mk5发过来的 消息
	public void parseJson(String strResult) throws JSONException {

		JSONObject jsonObject = new JSONObject(strResult);
		
		//编队相关的业务解析
		if (jsonObject.has("msgtype")) {
			// 创建编队回复
			if ("creatformation".equals(jsonObject.getString("msgtype"))) {
				Log.i(TAG, "创建编队回复creatformation information always save -----------");
				MyApplication.createstatus = jsonObject.getBoolean("status");
				MyApplication.formationid = jsonObject.getString("formationid");
				MyApplication.formationtype = jsonObject.getInt("formationtype");
				MyApplication.rcvTime_cfresponse = System.currentTimeMillis();
				
				if (MyApplication.createstatus) {
					MyApplication.joinflag = true;
					if (MyApplication.isServerConnect) {

						// 这里将创建编队中的 信息发送到 后台
						try {
							JSONObject mJson_team_creation = new JSONObject();
							mJson_team_creation.put("datatype", "FORMATION_REGISTER");
							mJson_team_creation.put("fromid", MyApplication.formationid);
							mJson_team_creation.put("fromtype", "veh");
							mJson_team_creation.put("team_id", MyApplication.formationid);
							mJson_team_creation.put("team_name", OverlayDemo.formationName.getText().toString());//OverlayDemo.formationName.getText().toString()
							mJson_team_creation.put("team_description", "hello,一路同行！");
							mJson_team_creation.put("team_start", OverlayDemo.formationStart.getText().toString());
							mJson_team_creation.put("team_end", OverlayDemo.formationEnd.getText().toString());
							mJson_team_creation.put("team_veh_maxnumber", "10");
							mJson_team_creation.put("team_veh_number", "1");
							 	
							Util.send_To_Clound(mJson_team_creation);
							Log.w(TAG, "parseJson: 已经成功把创建编队的信息发送给服务器！");
							
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
			
			// 接收车队状态信息
			if ("vehlist".equals(jsonObject.getString("msgtype"))) {
				if (MyApplication.joinflag) {
					MyApplication.formationid = jsonObject.getString("formationid");
					MyApplication.vehicle_numbers = jsonObject.getInt("membervehnum");
					MyApplication.localvehid = jsonObject.getString("localvehid");
					MyApplication.vehnum = jsonObject.getInt("vehnum");
					MyApplication.curspeed = jsonObject.getDouble("curspeed");
					MyApplication.advspeed = jsonObject.getDouble("advspeed");
					MyApplication.curdistance = jsonObject.getDouble("curdistance");
					MyApplication.advdistance = jsonObject.getDouble("advdistance");
					MyApplication.prevehid = jsonObject.getString("prevehid");
					Log.i(TAG, "接收车队状态信息------------"+MyApplication.formationid);
					String vehiclelist = jsonObject.getString("vehiclelist");
					formation_status.put(jsonObject.getString("formationid"), vehiclelist);
				}
			}
			
			// 点击查询车队后，接收mk5回复的附近的车队
			if ("motorcadelist".equals(jsonObject.getString("msgtype"))) {
				int motorcadenum = jsonObject.getInt("motorcadenum");
				if (motorcadenum > 0 && motorcade_list_flag) {
					Log.i(TAG, " 点击查询车队后，接收mk5回复的附近的车队----------"+motorcadenum);
					motorcade_list.put(motorcadenum, jsonObject.getString("motorcadelist"));
					sendMotorcadeList(motorcade_list);
				}
//				if (motorcadenum > 0) {
//					Log.i(TAG, " 点击查询车队后，接收mk5回复的附近的车队1111----------"+jsonObject.getString("motorcadelist"));
//					motorcade_list.put(motorcadenum, jsonObject.getString("motorcadelist"));
//					Log.i(TAG, " 点击查询车队后，接收mk5回复的附近的车队1111111111----------"+jsonObject.getString("motorcadelist"));
//					sendMotorcadeList(motorcade_list);
//					Log.i(TAG, " 点击查询车队后，接收mk5回复的附近的车队222222----------");
//				}
				
				
//				if (motorcadenum > 0) {
//					JSONArray jsonArray = new JSONArray(jsonObject.getString("motorcadelist"));
//					for (int i = 0; i < motorcadenum; i ++) {
//						JSONObject formationJson = jsonArray.getJSONObject(i);
//						formation formationmsg = new formation();
//						formationmsg.setFormationid(formationJson.getString("formationid"));
//						formationmsg.setFormationname(formationJson.getString("formationname"));
//						formationmsg.setFormationnum(formationJson.getInt("formationnum"));
//						formationmsg.setFormaitontype(formationJson.getInt("formationtype"));
//						formationmsg.setFormationdestination(formationJson.getString("formationdestination"));
//						formationmsg.setFormationstartlocation(formationJson.getString("formationstartlocation"));
//						formationmsg.setVehleaderlat(formationJson.getDouble("vehleaderlat"));
//						formationmsg.setVehleaderlon(formationJson.getDouble("vehleaderlon"));
//						formationmsg.setVehleaderspeed(formationJson.getDouble("vehleaderspeed"));
//						motorcade_list.put(i, formationmsg);
//					}
//					for (int num : motorcade_list.keySet()) {
//						Log.i(TAG, "motorcade_list is  " +  num);
//					}
//					sendMotorcadeList();
//				}
			
			}
			
			// 编队  其他车的加队请求
			if ("joinformationrequst".equals(jsonObject.getString("msgtype"))) {
				if (requestflag) {
					request requestmsg = new request();
					requestmsg.setVehid(jsonObject.getString("vehid"));
					requestmsg.setQuadrant(jsonObject.getInt("quadrant"));
					requestmsg.setDistance(jsonObject.getDouble("distance"));
				    sendJoinRequest(requestmsg);
				}
			    Log.i(TAG, "编队  其他车的加队请求");
			}
			
			// 入队请求后 头车对入队请求车辆的回复（同意或者拒绝）
			if ("joinformationresult".equals(jsonObject.getString("msgtype"))) {
				joinresponse joinrs = new joinresponse();
				joinrs.setStatus(jsonObject.getBoolean("status"));
				joinrs.setChangeheading(jsonObject.getBoolean("changeheading"));
				joinrs.setFormationid(jsonObject.getString("formationid"));
				joinrs.setPrevehid(jsonObject.getString("prevehid"));
				joinrs.setBehvehid(jsonObject.getString("behvehid"));
				joinrs.setMembernum(jsonObject.getInt("membernum"));
				sendJoinResponse(joinrs);

				if (!MyApplication.joinflag) {
					if (joinrs.getStatus()) {
						MyApplication.joinflag = true;
						MyApplication.formationid = joinrs.getFormationid();
						if (MyApplication.isServerConnect) {

							// 这里将创建编队中的 信息发送到 后台
							try {
								JSONObject mJson_team_invite_agree = new JSONObject();
								mJson_team_invite_agree.put("datatype", "FORMATION_RESPONSE");
								// 车牌号(被邀请车牌)
								mJson_team_invite_agree.put("fromtype", "veh");
								mJson_team_invite_agree.put("fromid", MyApplication.user_name);
								mJson_team_invite_agree.put("team_id", joinrs.getFormationid());

								Util.send_To_Clound(mJson_team_invite_agree);

							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}

				}

			}
			
			// 入队请求后  入队请求车辆进行车队的过程入队提示
			if ("joinformationchangeheading".equals(jsonObject.getString("msgtype"))) {
//				Boolean changehead = jsonObject.getBoolean("changeheading");
//				String preVehId = jsonObject.getString("prevehicleid");
				selfadjust adjust = new selfadjust();
				adjust.setChangehead(jsonObject.getBoolean("changeheading"));
				adjust.setPrevehid(jsonObject.getString("prevehicleid"));
				sendChangeHeading(adjust);
			}
			
			//入队请求同意后，车队中其他车辆的调整
			if ("joinformationadjust".equals(jsonObject.getString("msgtype"))) {
				joinadjust otheradjust = new joinadjust();
				otheradjust.setAdvspeed(jsonObject.getDouble("advspeed"));
				otheradjust.setAdvdistance(jsonObject.getDouble("advdistance"));
				otheradjust.setCurdistance(jsonObject.getDouble("curdistance"));
				otheradjust.setJoincarnum(jsonObject.getInt("joincarnum"));
				otheradjust.setPositiontype(jsonObject.getInt("positiontype"));
				sendOtherAdjust(otheradjust);
			}
			
			// 头车对入队请求车辆的回复为同意后，请求车辆入队，知道车辆入队成功，再通知车辆入队完成
			if ("joinformationfinish".equals(jsonObject.getString("msgtype"))) {
				String finishVehId = jsonObject.getString("vehid");
				sendJoinFinish(finishVehId);
			}
			
			// 头车解散车队
			if ("disbandformationsuccess".equals(jsonObject.getString("msgtype"))) {
				if (!MyApplication.formationDismissFlag) {
					MyApplication.formationDismissFlag = true;
					MyApplication.joinflag = false;
					formation_status.clear();//把编队信息状态清除
					sendDismissFormation();
					Log.w(TAG, "parseJson: 收到编队解散disbandformationsuccess");
					if (MyApplication.isServerConnect) {
						try {
							JSONObject mJson_team_dismiss = new JSONObject();
							mJson_team_dismiss.put("datatype", "FORMATION_DISMISS");
							mJson_team_dismiss.put("team_id", MyApplication.formationid);
							mJson_team_dismiss.put("fromtype", "veh");
							mJson_team_dismiss.put("fromid", MyApplication.user_name);

							Util.send_To_Clound(mJson_team_dismiss);

							Log.w(TAG, "parseJson: 车队被解散同时把解散消息发送给服务器！");
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}
			}
			
			// 头车解散车队成功后，通知车队其他车辆车队已经解散了
			if ("disbandedformation".equals(jsonObject.getString("msgtype"))) {
				if (!MyApplication.formationDismissedFlag) {
					MyApplication.formationDismissedFlag = true;
					MyApplication.joinflag = false;
					String formationid = jsonObject.getString("formationid");
					formation_status.clear();
					sendDismissedFormation(formationid);
				}

			}
			
			//头车允许请求离队的车离队
//			if ("leaveformationsuccess".equals(jsonObject.getString("msgtype"))) {
//				sendLeaveFormation();
//			}
			
			//把车队内有车离队的消息发送给车队内其他车辆
			if ("leaveformationmessage".equals(jsonObject.getString("msgtype"))) {
				leave leavemsg = new leave();
				leavemsg.setLeaveId(jsonObject.getString("vehid"));
				leavemsg.setPosition(jsonObject.getInt("positiontype"));
				sendOtherLeaveFormation(leavemsg);
				Log.i(TAG, "把车队内有车离队的消息发送给车队内其他车辆");
			}
			
			//车队内要离队的车离队成功
			if ("leaveformationsuccess".equals(jsonObject.getString("msgtype"))) {
				if (!MyApplication.leaveOtherFormation) {
					MyApplication.leaveOtherFormation = true;
					leave leavemsg = new leave();
					leavemsg.setLeaveId(jsonObject.getString("vehid"));
					leavemsg.setLeaveVin(jsonObject.getInt("vehvin"));
					leavemsg.setPosition(jsonObject.getInt("positiontype"));
					sendOtherLeaveSuccess(leavemsg);
					Log.i(TAG, "车队内要离队的车离队成功" + jsonObject.getInt("vehvin"));
			   }
			}

			//离队车辆离队后，通知车队内其他车辆有离队
//			if ("leaveformationmessage".equals(jsonObject.getString("msgtype"))) {
//				String leaveId = jsonObject.getString("vehid");
//				sendOtherLeaveFormation(leaveId);
//			}
			
			// 离队车辆未离队成功
			if ("nativeleaveformationmessage".equals(jsonObject.getString("msgtype"))) {
				Boolean leave = false;
				sendLeaveMsg(leave);
				Log.i(TAG, "离队车辆未离队成功");
			}
			
			// 离队车辆离队成功
			if ("nativeleaveformationsuccessmessage".equals(jsonObject.getString("msgtype"))) {
				if (!MyApplication.ownerLeaveFlag) {
					MyApplication.ownerLeaveFlag = true;
					Boolean leave = true;

					Log.i(TAG, "离队车辆离队成功");
					MyApplication.joinflag = false;//把入队标志置为false
					formation_status.clear();//把编队信息状态清除
					sendLeaveMsg(leave);
					Log.i(TAG, "离队车辆离队成功的消息广播成功");
					if (MyApplication.isServerConnect) {
						try {
							JSONObject mJson_team_leave = new JSONObject();
							mJson_team_leave.put("datatype", "TEAM_EXIT");
							mJson_team_leave.put("team_id", MyApplication.formationid);
							mJson_team_leave.put("fromtype", "veh");
							mJson_team_leave.put("fromid", MyApplication.user_name);

							Util.send_To_Clound(mJson_team_leave);
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}

				}
			}
			
			// 接收加入车队后的车队信息
			if ("".equals(jsonObject.getString("msgtype"))) {
				
			}
			
		}
		
		if (TCPService.Flag_Connection_MK5 == false) {// 如果与MK5连接中断，则要清车辆
			Other_Car_map.clear();
		}
		
		if (jsonObject.has("Lat_Local")) {
			idLocal = jsonObject.getInt("ID");
			LongLocalNumber = jsonObject.getInt("Long_Local");
			LatLocalNumber = jsonObject.getInt("Lat_Local");
			
			MyApplication.isMk5LatLng = true;

			Log.w(TAG, "parseJson: mk5已经连接，接收到其经纬度为+"+LongLocalNumber+"+"+LatLocalNumber );
		}
		
		// obd data 解析 wangyonglong
		if (jsonObject.has("VBAT")) {
			MyApplication.OBDFlag_display = true;
			MyApplication.OBD_info_recv_Time = System.currentTimeMillis();
			MyApplication.s_obd_VBAT = jsonObject.getString("VBAT");
		}

		if (jsonObject.has("RPM")) {
			MyApplication.s_obd_RPM = jsonObject.getString("RPM");
		}

		if (jsonObject.has("SPD")) {
			MyApplication.s_obd_SPD = jsonObject.getString("SPD");
		}

		if (jsonObject.has("TP")) {
			MyApplication.s_obd_TP = jsonObject.getString("TP");
		}

		if (jsonObject.has("LOD")) {
			MyApplication.s_obd_LOD = jsonObject.getString("LOD");
		}

		if (jsonObject.has("ECT")) {
			MyApplication.s_obd_ECT = jsonObject.getString("ECT");
		}

		if (jsonObject.has("FLI")) {
			MyApplication.s_obd_FLI = jsonObject.getString("FLI");
		} 

		if (jsonObject.has("MPH")) {
			MyApplication.s_obd_MPH = jsonObject.getString("MPH");
		}
		if (jsonObject.has("fromtype")) {
			MapActivity_val.fromtype = jsonObject.getString("fromtype");
		}

		/*
		 * 后台发送消息 2016年4月18日14:28:24
		 */
		if (jsonObject.has("veh_type")) {// 路测发来的消息
			MyApplication.MK5RecieveTime = System.currentTimeMillis();
			MyApplication.MK5InfoFlag = true;
			MyApplication.MK5Info = jsonObject.getString("veh_type");
			Log.i(TAG, "MK5   路测传过来的   veh_type   信息是：" + jsonObject.getString("veh_type"));
		}
		/*
		 * 接收后台传的消息
		 */
		if (jsonObject.has("announce")) {// 后台传过来的消息
			MyApplication.MK5RecieveTime = System.currentTimeMillis();
			MyApplication.MK5InfoFlag = true;
			MyApplication.MK5Info = jsonObject.getString("announce");
			Log.i(TAG, "MK5   路测传过来的   announce 信息是：" + jsonObject.getString("announce"));

		}

		if (jsonObject.has("VIN")) {// 自身车辆的ID号码？
			Log.i(TAG, "收到MK5 传过来的 VIN！");
		}

		/*
		 * 心跳信息接收
		 */
		if (jsonObject.has("LIVE")) {

			TCPService.rcvTime = System.currentTimeMillis();
		}

		if (jsonObject.has("clientID")) {// 与mk5 连接的id ，确定为我和mk5通信那个模块的id
			String clientID = jsonObject.getString("clientID");
			this.clientID = clientID;
			Log.i(TAG, "[clientID]: " + this.clientID + "!!");
			
		}

		/*
		 * 删除指定的ID
		 */
		if (jsonObject.has("deleteID")) {// 删除接受到的
											// mk5的id//删除附近的车辆，让它不在UI上显示车辆的位置
			int deleteid = jsonObject.getInt("deleteID");
		    if (OverlayDemo.addMarkerFromMk5.containsKey(deleteid)) {
		    	Intent intent = new Intent("com.liu.client.deleteid");
		    	intent.putExtra("deletedId", deleteid);
		    	MyApplication.getcontext().sendBroadcast(intent);
		    	Log.i(TAG, "deleteId always in addMarkerFrom Mk5 ---------------");
		    }
		    Log.i(TAG, "deleteId is ---------" + deleteid);
//			if (Other_Car_map.containsKey(deleteid)) {
//				Other_Car_map.remove(deleteid);
//			}
		}
		
		if (jsonObject.has("addID")) {// 添加id，

			int addid = jsonObject.getInt("addID"); // 把addID的值赋值给addid
			Log.i(TAG, "[client_test]: addID(" + addid + ")!!");
			sendId = null;
			sendId = addid;
			for (int i = 0; i < UnregisteredId.list.size(); i++) {
				if (UnregisteredId.list.get(i) == addid) {// 如果
					isadd = false;
					break;
				}
			}
			if (isadd == true) {
				UnregisteredId.list.add(addid);// 加一个判断
				headParam.put("addID", sendId); // 发送 需要添加到聊天组群的 用户ID //测试 wkl
				// COME AND CHANGE FLAG NAME // 20150728
				issend = true;// 发给mk5

				/*
				 * ljl 添加代码
				 */
				send(headParam);
				Log.i(TAG, "[client_test]: addID 2 !!");
			}
		}

		// 防撞预警——张卓鹏测试使用
		if (jsonObject.has("Scene")) {// 代表防撞预警模式和等级
			MyApplication.MK5CarwarnningReciveTime = System.currentTimeMillis();
			MyApplication.MK5Scene = jsonObject.getInt("Scene");
			Log.i(TAG, "Scene-->" + MyApplication.MK5Scene);
		}

		/*
		 * MK5消息通过 msgID 来管理，包括 ACM、BSM
		 */

		if (jsonObject.has("msgID")) {
			switch ((short) jsonObject.getInt("msgID")) {
			case 0:
				if (jsonObject.has("Scene")) {
					MyApplication.highPriorityReciveTime = System.currentTimeMillis();
					MyApplication.highPriorityScene = jsonObject.getInt("Scene");
                    MyApplication.highPriorityDistance = jsonObject.getInt("Distance");
				}
				break;

			case 1:
				// ACM(alaCarteMessage)自定义消息：
				// （1） 交通灯消息
				// （2） 聊天消息
				// （3）紧急消息发布

				
				if (jsonObject.has("emergencyMsg")) {// 紧急消息发布
					Log.i("lijialong5", "11111");
					String em = jsonObject.getString("emergencyMsg");
					MyApplication.MK5InfoFlag = true;
					MyApplication.MK5RecieveTime = System.currentTimeMillis();
					// MK5InfoFlagEm = true;
					MyApplication.MK5Info = jsonObject.getString("emergencyMsg");
				}

				if (jsonObject.has("chatContent")) { // 聊天消息
					Log.i("wangyonglong", "receive chatContent message!");
					int id = jsonObject.getInt("id");
					// String id_String = String.valueOf(id);
					String content = jsonObject.getString("chatContent");
					// 在解析以此
					if (isJson(content)) {// 判断发过来的消息是否是joson格式
						parseJson(content);
					} else {
						sendBroadcastString1(content);
					}
					break;
				}

			case 2:
				// 其他车辆的经纬度信息

				// 防撞预警——张卓鹏
				if (jsonObject.has("Scene")) {// 代表防撞预警模式和等级
					MyApplication.MK5CarwarnningReciveTime = System.currentTimeMillis();
					MyApplication.MK5Scene = jsonObject.getInt("Scene");
					Log.i(TAG, "Scene-->" + MyApplication.MK5Scene);
				}

				/*
				 * 车身信息，包括本身信息和其他车辆信息
				 */
				int key = jsonObject.getInt("ID");
				
				Log.i(TAG, "---------向Other_Car_map存储其他车辆的经纬度信息--------------" + key);
				Car_Data data = new Car_Data();
				data.msgID = (short) jsonObject.getInt("msgID");
				data.lat = jsonObject.getInt("Lat_Rcv");// 其他车辆纬度
				data.longi = jsonObject.getInt("Long_Rcv");// 其他车辆经度
				data.id = jsonObject.getInt("ID");
				if ((data.longi > 0) && (data.lat > 0)) {
					Other_Car_map.put(key, data);// 容错，仅吧，正的部分加入，后期还要修改
				}

				break;
				
			case 3:

				//交通灯信息和闯红灯
				if (jsonObject.has("CurrentState")) {
					//交通灯和建议车速信息  currentState 为红绿灯的状态，为数字1、2、3。“3”是红灯状态；“2”是黄灯状态；“1”是绿灯状态
					MyApplication.lightState = jsonObject.getInt("CurrentState");
					MyApplication.lightRemainTime = jsonObject.getInt("TimeRemain");
					MyApplication.adviseSpeed = jsonObject.getInt("Speed_Adv");
					if (jsonObject.has("RedLight")) {
                        MyApplication.redLight = jsonObject.getInt("RedLight");
					}
					MyApplication.TrafficLightReciveTime = System.currentTimeMillis();
				}

				//道路危险
				if (jsonObject.has("VDanger")) {
					MyApplication.danger = jsonObject.getInt("VDagner");
					MyApplication.dangerReciveTime = System.currentTimeMillis();
				}

				//限速
				if (jsonObject.has("VLimit")) {

				}

				break;

			case 5:
				if (jsonObject.has("request")) {
//					Context otherAppContext = createPackageContext("com.Li.register.LoginActivity", Context.CONTEXT_IGNORE_SECURITY);
					SharedPreferences account = MyApplication.getcontext().getSharedPreferences("login", Context.MODE_PRIVATE);
				    String license = account.getString("user", "");
				    if (!license.isEmpty()) {
				    	JSONObject jsonLicense = new JSONObject();
				    	jsonLicense.put("License", license);
				    	Util.send_To_MK5(clientID, jsonLicense);
					} else {

					}
				}

				if (jsonObject.has("get")) {
//					Toast.makeText(this, "", Toast.LENGTH_SHORT).show();

				}

				break;

			case 11:
				data2.msgID = (short) jsonObject.getInt("msgID");
				if (jsonObject.has("msgCnt")) {// 消息计数
					data2.msgCnt = (short) jsonObject.getInt("msgCnt");
				}
				if (jsonObject.has("lat")) {
					data2.lat = jsonObject.getInt("lat");
				}
				if (jsonObject.has("typeEvent")) {
					data2.typeEvent = jsonObject.getLong("typeEvent");
				}
				if (jsonObject.has("Long")) {
					data2.Long = jsonObject.getInt("Long");
				}
				break;

			}
		}

		if (jsonObject.has("vin")) {
			Client.vin = jsonObject.getInt("vin");
		}

	}

	private boolean isJson(String content) {
		try {

			new JSONObject(content);

		} catch (JSONException e) {
			return false;
		}
		return true;
	}

	/**
	 * 通知Activity更新UI
	 * 
	 * @param s
	 * @param path2
	 */
	public void sendBroadcastString(String s, String path2) {
		intent.putExtra("INFO", s + "路径  = " + path2 + "\n");
		context.sendBroadcast(intent);
		intent.removeExtra("INFO");
	}

	public void sendBroadcastString1(String s) {
		intent.putExtra("INFO", s + "\n");
		context.sendBroadcast(intent);
		intent.removeExtra("INFO");
	}

	public void sendBroadcastString1(int l) {
		intent.putExtra("INFO", l + "\n");
		context.sendBroadcast(intent);
		intent.removeExtra("INFO");
	}
	
	private void sendMotorcadeList(HashMap<Integer, String> h) {
		Intent intent = new Intent("com.liu.Client.sendMotorcadeList");
		Bundle bundle = new Bundle();
		bundle.putSerializable("list", h);
		intent.putExtras(bundle);
		context.sendBroadcast(intent);
		
	}
	
	private void sendJoinRequest(request r) {
		Intent intent = new Intent("com.liu.Client.sendJoinRequest");
		intent.putExtra("joinrequest", r);
		context.sendBroadcast(intent);
	}
	
	private void sendJoinResponse(joinresponse j) {
		Intent intent = new Intent("com.liu.Client.sendJoinResponse");
		intent.putExtra("joinresponse", j);
		context.sendBroadcast(intent);
	}
	
	private void sendChangeHeading(selfadjust sa) {
		Intent intent = new Intent("com.liu.Client.sendChangeHeading");
		intent.putExtra("selfadjust", sa);
		context.sendBroadcast(intent);
	}
	
	private void sendOtherAdjust(joinadjust jd) {
		Intent intent = new Intent("com.liu.Client.sendOtherAdjust");
		intent.putExtra("joinadjust", jd);
		context.sendBroadcast(intent);
	}
	
	private void sendJoinFinish(String vehid) {
		Intent intent = new Intent("com.liu.Client.sendJoinFinish");
		intent.putExtra("finishVehid", vehid);
		context.sendBroadcast(intent);
	}
	
	private void sendDismissFormation() {
		Intent intent = new Intent("com.liu.Client.sendDismissFormation");
		context.sendBroadcast(intent);
	}
	
	private void sendDismissedFormation(String s) {
		Intent intent = new Intent("com.liu.Client.sendDismissedFormation");
		intent.putExtra("dismissedId", s);
		context.sendBroadcast(intent);
	}
	
	private void sendLeaveFormation() {
		Intent intent = new Intent("com.liu.Client.sendLeaveFormation");
		context.sendBroadcast(intent);
	}
	
	private void sendOtherLeaveFormation(leave l) {
		Intent intent = new Intent("com.liu.Client.sendOtherLeaveFormation");
		intent.putExtra("leave", l);
		context.sendBroadcast(intent);
	}
	
//	private void sendOtherLeaveFormation(String s) {
//		Intent intent = new Intent("com.liu.Client.sendOtherLeaveFormation");
//		intent.putExtra("leaveId", s);
//		context.sendBroadcast(intent);
//	}

	private void sendOtherLeaveSuccess(leave l) {
		Intent intent = new Intent("com.liu.Client.sendOtherLeaveSuccess");
		intent.putExtra("leave", l);
		context.sendBroadcast(intent);
	}
	
	private void sendLeaveMsg(Boolean b) {
		Intent intent = new Intent("com.liu.Client.sendLeaveMsg");
		intent.putExtra("leave", b);
		context.sendBroadcast(intent);
	}

	/*
	 * 异步处理Mk5传过来的 消息，解决MK5消息 堆积的问题
	 */
	class PareseTask extends AsyncTask<String, Integer, Boolean> {

		@Override
		protected Boolean doInBackground(String... params) {
			// TODO Auto-generated method stub
			while (isconnect) {
				if (isnewmessage == true) {
					try {
						Log.i(TAG, "joson解析");
						parseJson(matches);
						isnewmessage = false;
						// matches = null;
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				Log.i(TAG, "joson解析线程退出");
			} else {
				Log.i(TAG, "joson解析错误");
			}
		}

	}

	/*************************************************/
	Runnable command = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				parseJson(matches);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};
}
