package org.yanzi.shareserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.main.TCPService.TCPService;
import com.main.activity.MyApplication;
import com.main.baiduMap.MapActivity_val;

/*
 * 与MK5通信线程
 */
public class Client extends Thread {
	private final static String TAG = "liuhongdong";
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

				Log.i("zhanghao", "从Mk5接受到的信息为：" + matches);
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

			Log.i(TAG, "bos.flush()------");

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
		/*
		 * currentState 为红绿灯的状态，为数字1、2、3。“1”是红灯状态；“2”是黄灯状态；“3”是红灯状态
		 */
		if (TCPService.Flag_Connection_MK5 == false) {// 如果与MK5连接中断，则要清车辆
			Other_Car_map.clear();
		}
		if (jsonObject.has("CurrentState")) {
			// Log.i(TAG, "currentState------");
			MyApplication.lightState = jsonObject.getInt("CurrentState");
		}
		if (jsonObject.has("Lat_Local")) {// MK5发的经纬度
			MyApplication.Lat_From_MK5 = jsonObject.getInt("Lat_Local");
			Log.i(TAG, "=======================================================" + MyApplication.Lat_From_MK5);
		}

		if (jsonObject.has("Long_Local")) {
			MyApplication.Long_From_MK5 = jsonObject.getInt("Long_Local");
		}
		/*
		 * TimeRemain为红绿灯的剩余时间
		 */
		if (jsonObject.has("TimeRemain")) {
			// Log.i(TAG, "TimeRemain------");
			MyApplication.lightRemainTime = jsonObject.getInt("TimeRemain");
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
			Log.i("Manager1", "[clientID]: " + this.clientID + "!!");
		}
		/*
		 * 删除指定的ID
		 */
		if (jsonObject.has("deleteID")) {// 删除接受到的
											// mk5的id//删除附近的车辆，让它不在UI上显示车辆的位置
			int deleteid = jsonObject.getInt("deleteID");
			if (Other_Car_map.containsKey(deleteid)) {
				Other_Car_map.remove(deleteid);
			}

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

		/*
		 * 防撞预警 //以前的防撞预警——张卓鹏
		 */
		// if (jsonObject.has("anticollision_mode")) {
		// /*
		// * stateMode == 1 || stateMode == 2 为跟驰模式 stateMode == 3 ||
		// * stateMode == 6 为弯道模式 stateMode == 4 || stateMode == 5 为超车模式
		// */
		//
		// MyApplication.MK5CarwarnningReciveTime = System.currentTimeMillis();
		// int stateMode = jsonObject.getInt("anticollision_mode");
		// Log.i("预警", "接受 防撞预警信息 ----anticollision_mode 中 statMode 是： "
		// + stateMode);
		// /*
		// * 在接收端已经判断，carwarningFlag=stateMode，1&&2为“1”，3&& 6为“3”，4&&5为“4”
		// */
		// if (stateMode == 1 || stateMode == 2) {
		// stateMode = 1;
		// } else if (stateMode == 3 || stateMode == 6) {
		// stateMode = 3;
		// } else {
		// stateMode = 4;
		// }
		// }

//		// 防撞预警——张卓鹏测试使用
//		if (jsonObject.has("Scene")) {// 代表防撞预警模式和等级
//			MyApplication.MK5CarwarnningReciveTime = System.currentTimeMillis();
//			MyApplication.MK5Scene = jsonObject.getInt("Scene");
//			Log.i(TAG, "Scene-->" + MyApplication.MK5Scene);
//		}

		/*
		 * MK5消息通过 msgID 来管理，包括 ACM、BSM
		 */

		if (jsonObject.has("msgID")) {
			switch ((short) jsonObject.getInt("msgID")) {
			case 0:
				// /*
				// * 车辆自身经纬度信息
				// */
				// if (jsonObject.has("Long_Local")) {// 自身经度信息
				//
				// int LongLocal = jsonObject.getInt("Long_Local");
				//
				// Log.i(TAG, "MK5 端 经度：----------------------------------" +
				// "LongLocal");
				//
				// LongLocalNumber = LongLocal;
				//
				// if (LongLocal != 0 && (int) (LongLocal /
				// MyApplication.Scal_to_Covert) != 180) {
				// MyApplication.isMk5LatLng = true;
				// } else {
				// MyApplication.isMk5LatLng = false;
				// }
				//
				// }
				// if (jsonObject.has("Lat_Local")) {// 自身纬度信息
				//
				// Log.i(TAG, "MK5 端 经度：------------------------------------" +
				// "Lat_Local");
				//
				// int LatLocal = jsonObject.getInt("Lat_Local");
				//
				// // Log.i(TAG, "MK5 自身 传过来的纬度：" +
				// // String.valueOf(LatLocal));
				//
				// LatLocalNumber = LatLocal;
				//
				// MK5Flag = true;
				//
				// }
				// break;

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
				// BSM（BasicSafetyMessage）基础安全消息：
				// （1）本地GPS
				// （2）防撞预警

				// 防撞预警——张卓鹏
				if (jsonObject.has("Scene")) {// 代表防撞预警模式和等级
					MyApplication.MK5CarwarnningReciveTime = System.currentTimeMillis();
					MyApplication.MK5Scene = jsonObject.getInt("Scene");
					Log.i(TAG, "Scene-->" + MyApplication.MK5Scene);
				}

				/*
				 * 车身信息，包括本身信息和其他车辆信息
				 */
				int key = jsonObject.getInt("id");
				if (Other_Car_map.containsKey(key)) {
					Log.i(TAG, "---------有相同ID,覆盖--------------key" + key);
					Other_Car_map.get(key).msgID = (short) jsonObject.getInt("msgID");
					if (jsonObject.has("msgCnt")) {
						Other_Car_map.get(key).msgCnt = (short) jsonObject.getInt("msgCnt");// 向map集合中加数据
					}
					if (jsonObject.has("lat")) {// 其他车辆纬度
						Other_Car_map.get(key).lat = jsonObject.getInt("lat");
						Log.i(TAG, "MK5 recieved other car latitude is 2:" + String.valueOf(Other_Car_map.get(key).lat));

					}
					if (jsonObject.has("long")) {// 其他车辆经度
						Other_Car_map.get(key).longi = jsonObject.getInt("long");

						Log.i(TAG, "MK5 recieved other car latitude is 2:" + String.valueOf(Other_Car_map.get(key).longi));

					}
					//
					if (jsonObject.has("Lat_Rcv")) {// 其他车辆纬度
						Other_Car_map.get(key).lat = jsonObject.getInt("Lat_Rcv");
						Log.i(TAG, "MK5 recieved other car latitude is 2:" + String.valueOf(Other_Car_map.get(key).lat));

					}
					if (jsonObject.has("Long_Rcv")) {//
						Other_Car_map.get(key).longi = jsonObject.getInt("Long_Rcv");

						Log.i(TAG, "MK5 recieved other car latitude is 2:" + String.valueOf(Other_Car_map.get(key).longi));

					}
					//
					if (jsonObject.has("Mode")) {// 代表防撞预警模式，
						Other_Car_map.get(key).Mode = jsonObject.getInt("Mode");
						Log.i(TAG, "Mode-->" + jsonObject.getInt("Mode"));

					}
					if (jsonObject.has("Level")) {// 代表防撞预警等级，
						Other_Car_map.get(key).Level = jsonObject.getInt("Level ");
						Log.i(TAG, "Level-->" + jsonObject.getInt("Level"));
					}
					if (jsonObject.has("id")) {
						Other_Car_map.get(key).id = jsonObject.getInt("id");
					}
					if (jsonObject.has("secMark")) {
						Other_Car_map.get(key).secMark = (short) jsonObject.getInt("secMark");
					}

					if (jsonObject.has("elev")) {
						Other_Car_map.get(key).elev = (short) jsonObject.getInt("elev");
					}

					if (jsonObject.has("accuracy")) {
						Other_Car_map.get(key).accuracy = jsonObject.getInt("accuracy");
					}

					if (jsonObject.has("speed")) {
						Other_Car_map.get(key).speed = (short) jsonObject.getInt("speed");
					}

					if (jsonObject.has("heading")) {
						Other_Car_map.get(key).heading = (short) jsonObject.getInt("heading");
					}

					if (jsonObject.has("angle")) {
						Other_Car_map.get(key).angle = (short) jsonObject.getInt("angle");
					}
					if (jsonObject.has("longitude")) {// 自身的经度

						// map.get(key).way.longitude = jsonObject
						// .getInt("longitude");

					}
					if (jsonObject.has("latitude")) {// 自身的纬度
						// map.get(key).way.latitude = jsonObject
						// .getInt("latitude");

					}
					if (jsonObject.has("vertical")) {
						Other_Car_map.get(key).way.vertical = (short) jsonObject.getInt("vertical");
					}
					if (jsonObject.has("yaw")) {
						Other_Car_map.get(key).way.yaw = (char) jsonObject.getInt("yaw");
					}
					if (jsonObject.has("breaks")) {
						Other_Car_map.get(key).breaks = (short) jsonObject.getInt("breaks");
					}
				} else {
					Log.i(TAG, "---------不相同ID,添加MAP--------------key" + key);
					Car_Data data = new Car_Data();
					data.msgID = (short) jsonObject.getInt("msgID");
					if (jsonObject.has("msgCnt")) {
						data.msgCnt = (short) jsonObject.getInt("msgCnt");
					}

					if (jsonObject.has("lat")) {
						data.lat = jsonObject.getInt("lat");

						Log.i(TAG, "MK5 recieved other car latitude is 1:" + String.valueOf(data.lat));

					}
					if (jsonObject.has("long")) {
						data.longi = jsonObject.getInt("long");

						Log.i(TAG, "MK5 recieved other car longtitude is 1:" + String.valueOf(data.longi));

					}
					//
					//
					if (jsonObject.has("Lat_Rcv")) {// 其他车辆纬度
						data.lat = jsonObject.getInt("Lat_Rcv");
						Log.i(TAG, "MK5 recieved other car latitude is 2:" + String.valueOf(data.lat));

					}
					if (jsonObject.has("Long_Rcv")) {//
						data.longi = jsonObject.getInt("Long_Rcv");

						Log.i(TAG, "MK5 recieved other car latitude is 2:" + String.valueOf(data.longi));

					}
					//
					if (jsonObject.has("Mode")) {// 代表防撞预警模式，
						data.Mode = jsonObject.getInt("Mode");
						Log.i(TAG, "Mode-->" + data.Mode);

					}
					if (jsonObject.has("Level")) {// 代表防撞预警等级，
						data.Level = jsonObject.getInt("Level");
						Log.i(TAG, "Level-->" + data.Level);
					}
					if (jsonObject.has("id")) {
						data.id = jsonObject.getInt("id");
					}
					if (jsonObject.has("secMark")) {
						data.secMark = (short) jsonObject.getInt("secMark");
					}
					if (jsonObject.has("elev")) {
						data.elev = (short) jsonObject.getInt("elev");
					}
					if (jsonObject.has("accuracy")) {
						data.accuracy = jsonObject.getInt("accuracy");
					}
					if (jsonObject.has("speed")) {
						data.speed = (short) jsonObject.getInt("speed");
					}
					if (jsonObject.has("heading")) {
						data.heading = (short) jsonObject.getInt("heading");
					}

					if (jsonObject.has("angle")) {
						data.angle = (short) jsonObject.getInt("angle");
					}
					if (jsonObject.has("longitude")) {// 自身的经度
						// data.way.longitude = (short) jsonObject
						// .getInt("longitude");

					}
					if (jsonObject.has("latitude")) {// 自身的纬度
						// data.way.latitude = (short) jsonObject
						// .getInt("latitude");

					}
					if (jsonObject.has("vertical")) {
						data.way.vertical = (short) jsonObject.getInt("vertical");
					}
					if (jsonObject.has("yaw")) {
						data.way.yaw = (char) jsonObject.getInt("yaw");
					}
					if (jsonObject.has("breaks")) {
						data.breaks = (short) jsonObject.getInt("breaks");
					}
					if ((data.longi > 0) && (data.lat > 0)) {
						Other_Car_map.put(key, data);// 容错，仅吧，正的部分加入，后期还要修改
					}

				}

				/*
				 * 车辆自身经纬度信息
				 */
				if (jsonObject.has("Long_Local")) {// 自身经度信息

					int LongLocal = jsonObject.getInt("Long_Local");

					Log.i("lijialong", "MK5 端 经度：" + "LongLocal");

					LongLocalNumber = LongLocal;

					if (LongLocal != 0 && (int) (LongLocal / MyApplication.Scal_to_Covert) != 180) {
						MyApplication.isMk5LatLng = true;
					} else {
						MyApplication.isMk5LatLng = false;
					}

				}
				if (jsonObject.has("Lat_Local")) {// 自身纬度信息

					Log.i(TAG, "MK5 端 经度：" + "Lat_Local");

					int LatLocal = jsonObject.getInt("Lat_Local");

					// Log.i(TAG, "MK5 自身 传过来的纬度：" +
					// String.valueOf(LatLocal));

					LatLocalNumber = LatLocal;

					MK5Flag = true;

				}

				if (jsonObject.has("VIN")) {// 自身车辆的ID号码？ 车架号 车辆唯一标识符
					Log.i(TAG, "收到MK5 传过来的 VIN！");
				}

				if (jsonObject.has("anticollision_state")) {

					Log.i("预警", "接受 防撞预警信息 ----anticollision_state  中  ");

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
