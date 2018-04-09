package com.main.activity;

import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.yanzi.shareserver.Car_Data;
import org.yanzi.shareserver.Client;
import org.yanzi.shareserver.Manager;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.Li.data.SharePreferenceUtil;
import com.Li.serviceThread.CarGroup;
import com.Li.serviceThread.ClientManager;
import com.Li.serviceThread.ServiceClient;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.InfoWindow.OnInfoWindowClickListener;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.overlayutil.DrivingRouteOverlay;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.iflytek.speech.setting.IatSettings;
import com.iflytek.speech.setting.TtsSettings;
import com.iflytek.speech.util.JsonParser;
import com.liu.Class.formation;
import com.liu.Class.joinadjust;
import com.liu.Class.joinresponse;
import com.liu.Class.leave;
import com.liu.Class.request;
import com.liu.Class.selfadjust;
import com.main.TCPService.TCPService;
import com.main.activity.R;
import com.main.baiduMap.Group;
import com.main.baiduMap.Group.FBackBtnClickListener;
import com.main.baiduMap.Group.FCreakBtnClickListener;
import com.main.baiduMap.MyOrientationListener;
import com.main.chart.ChatMsgEntity;
import com.main.chart.ChatMsgViewAdapter;
import com.main.chart.DisplayActivity;
import com.main.chart.DisplayActivity.MsgReceiver;
import com.main.utilTools.MyDate;

import Utili.Package.LogUtil;
import Utili.Package.ToastUtil;
import Utili.Package.Util;
import Utili.Package.ViewHolder;

/**
 * 演示覆盖物的用法
 */
public class OverlayDemo extends Activity
		implements FBackBtnClickListener, FCreakBtnClickListener, OnClickListener, OnGetRoutePlanResultListener, OnItemClickListener {
	private final static String TAG = "liuhongdong";
	
	/**
	 * MapView 是地图主控件
	 */
	MapView mMapView;
	BaiduMap mBaiduMap;
	// 定位相关
	LocationClient mLocClient;
	public MyLocationListenner myListener = new MyLocationListenner();
	private LocationMode mCurrentMode;
	BitmapDescriptor mCurrentMarker;
	private Double lastX = 0.0;
	private int mCurrentDirection = 0;
	private double mCurrentLat = 0.0;
	private double mCurrentLon = 0.0;
	private float mCurrentAccracy;

	// UI相关
	Button requestLocButton;
	boolean isFirstLoc = true; // 是否首次定位
	private MyLocationData locData;

	private RelativeLayout create_cargroup_fragmentRelative, map;
	public static String car_group_id_random;
	
	public JSONObject mJson_MemberAgree_cloud = new JSONObject();
	public JSONObject mJson_team_cloud = new JSONObject();
	public JSONObject mJson_team = new JSONObject();
	public JSONObject mJson_team_update = new JSONObject();
	public JSONObject mJson_to_clound = new JSONObject();
	public JSONObject mJson_team_dismiss = new JSONObject();
	public JSONObject mJson_member_update = new JSONObject();
	public JSONObject mJson_member_show = new JSONObject();
	public JSONObject mJson_team_invite = new JSONObject();
	public JSONObject mJson_team_creation = new JSONObject();
	public JSONObject mJson_team_invite_agree = new JSONObject();
	public JSONObject mJson_team_invite_disagree = new JSONObject();
	
	public JSONObject json_CreateFormation = new JSONObject();
	public JSONObject json_QueryFormation = new JSONObject();
	public JSONObject json_JoinFormation = new JSONObject();
	public JSONObject json_JoinRequst_Result = new JSONObject();
	public JSONObject json_vehdismiss_formation = new JSONObject();
	
	
	public SharePreferenceUtil util;

	// 车群组中按钮
	private Button btn_delGroup, groupSoundBtn, groupSend, btn_groupInvate, btn_group_inviteBack, btn_group_inviteOK,
			btn_groupMemberBack, btn_groupMember_name, groupInvite_agree_btn, groupInvite_cancel_btn, btn_create_group,
			myLocation, btn_exist_gruop, btn_carGroup, btn_cargroup_back, Mk5_button, btn_mk5function_back, group_chat_btn;
	private EditText groupInput, EditText_invite_group_name, inviteUser;
	private LinearLayout groupInvite_linearlayout, groupInviteApply;
	private TextView group__invite_apply_name, group_apply_start, group_apply_end;
	private RelativeLayout groupMember_relativelayout;
	private SimpleAdapter mAdapterGroup, mAdapterCarGroup;
	private ListView mListView_group_member;
	private DrivingRouteLine route;
	private GroupCreatReceiver groupCreatReceiver;
	private GroupDeleteReceiver groupDeleteReceiver;
	private AddCarRegister addCarRegister;
	private MemberUpdateReceiver memberUpdateReceiver;
	private InviteAskRegister inviteAskRegister;
	private AskAnswerRegister askAnswerRegister;
	private AnnouncementRegister announcementRegister;
	private GroupExistRegister groupExistRegister;
	private DeleteIdReceiver deleteIdReceiver;
	private MotorcadeListReceiver motorcadeListReceiver;
	private JoinRequestReceiver joinRequestReceiver;
	private JoinResponseReceiver joinResponseReceiver;
	private ChangeHeadingReceiver changeHeadingReceiver;
	private OtherAdjustReceiver otherAdjustReceiver;
	private JoinFinishReceiver joinFinishReceiver;
	private DismissFormationReceiver dismissFormationReceiver;
	private DismissedFormationReceiver dismissedFormationReceiver;
	private LeaveFormationReceiver leaveFormationReceiver;
	private OtherLeaveFormationReceiver otherLeaveFormationReceiver;
	private OtherLeaveSuccessReceiver otherLeaveSuccessReceiver;
	private LeaveMsgReceiver leaveMsgReceiver;
	
	// 车辆编队的按钮
    private Button MK5_formation_trigger, btn_formation, btn_queryformation, btn_MK5formation_back;

    // 创建编队页面相关的布局，编辑框和按钮
    private RelativeLayout create_formation_layout;
    public static EditText formationName, formationStart, formationEnd;
    private Button btn_formation_back, btn_formation_creat;
    
    // 创建编队回复的界面
    private RelativeLayout create_formation_response_layout;
    private TextView formationid_text;
    
    // 车队状态界面
    private RelativeLayout motorcade_status_layout;
    private TextView motorcade_id, motorcade_numbers, motorcade_ordinal,pre_vehid, 
    vehicle_speed, vehicle_advisoryspeed, vehicle_curdistance, vehicle_advdistance;
    private Button veh_dismiss_btn;
    
    // 车队列表界面
    private RelativeLayout motorcade_list_layout;
    private ListView motorcadelist_listview;
    public static HashMap<Integer, formation> motorcade_arraylist = new HashMap<Integer, formation>();
    //编队车辆位置标志位
    private Boolean formationMarkerFlag = true;
    
    // 车队详细界面
    private RelativeLayout motorcadeinfo_layout;
    private TextView motorcadeinfo_id, motorcadeinfo_numbers, motorcadeinfo_name, 
    motorcadeinfo_type, motorcadeinfo_start, motorcadeinfo_end;
    
    // 入队请求界面
    private RelativeLayout joinrequestinfo_layout;
    private Button joinrequest_agree_btn, joinrequest_reject_btn;
    private TextView joinrequest_vehid, joinrequest_quadrant, joinrequest_distance;
    
    // 入队请求的回复界面
    private RelativeLayout joinresponseinfo_layout;
    private TextView joinresponse_info, joinresponse_id, joinresponse_result,
            joinresponse_preid, joinresponse_behid, joinresponse_chghd;
    
    // 入队请求同意后，车队中其他车辆的调整
    private RelativeLayout entranceprompt_otheradjust;
    private TextView otheradj_advspeed, otheradj_advdistance, 
            otheradj_curdistance, otheradj_joincarnum;
    
    //入队请求同意后，入队车辆的入队提示
    private RelativeLayout entranceprompt_layout;
    private TextView entranceprompt_text;
    
    //入队请求同意后，入队车辆的入队完成提示
    private RelativeLayout entrancefinish_layout;
    private TextView finish_vehid;
    
    //头车解散编队
    private RelativeLayout dismissformation_layout;
    private TextView dismissInfo;
    
    //离队提醒
    private RelativeLayout leaveformation_layout;
    
    //车队 中有车辆离队，通知其他车辆有车离队
    private RelativeLayout otherleaveformation_layout;
    private TextView leaveVehid;
    //车队 中有车辆离队,设置它的离队标准位
    private Boolean leaveFlag = false;
    //有车入队,设置它的入队标准位
    private Boolean entryFlag = false;
    
	private ChatMsgViewAdapter mAdapter;// 消息视图的Adapter

	private BitmapDescriptor mIconMaker;
	public static volatile int isMK5FirstLocation = 0;
	// 定位回调 全局 经纬度
	public LatLng desLatLng1;
	private Button chat_display_btn, chat_close_btn;
	private RelativeLayout cargroup_chat_Relative;// 车车聊天相对布局
	private LinearLayout cargroup_button;// 车群组相关按钮
	private LinearLayout formation_LinearLayout; //车辆编队按钮布局
	private LinearLayout Mk5fuction_LinearLayout;// mk5相关业务按钮
	private LinearLayout obd_linearlayout;// OBD线性布局
	MsgReceiver msgReceiver = null;
	// 语音合成引擎
	private SpeechSynthesizer mTts;
	// 默认云端发音人
	public static String voicerCloud = "xiaorong";
	// 默认本地发音人
	public static String voicerLocal = "xiaorong";
	// 缓冲进度
	private int mPercentForBuffering = 0;
	// 播放进度
	private int mPercentForPlaying = 0;
	// 云端/本地选择按钮
	// 引擎类型
	private String mEngineType = SpeechConstant.TYPE_CLOUD;

	private Toast nToast, mToast;
	private SharedPreferences nSharedPreferences;
	// 语音听写对象
	private SpeechRecognizer mIat;
	// 语音听写UI
	private RecognizerDialog iatDialog;

	// 定义一个现实群组聊天信息的文字框
	private String recordTime = MyDate.getDateEN(), nowTime;
	private boolean result = false;
	private EditText mEditText_chat_Content;

	// 后台推送车队公告信息的文本框
	private RelativeLayout mcar_group_announcement;
	private TextView mannouncement_content_TextView;
	private Button mBtn_announcement_Back;

	// OBD信息
	private TextView tv_vbat, tv_rpm, tv_spd, tv_tp, tv_lod, tv_ect, tv_fli, tv_mph;
	// 交通灯
	private RelativeLayout light;
	private ImageView light_left, light_straight, light_right;
	private TextView straight_remain;
	// 申明全局变量
	private Button mBtn_chat_Send, mBtn_chat_Sound;// 聊天的发送与点击说话按钮
	private MyApplication application;
	private ListView mchat_content_ListView;
	private List<ChatMsgEntity> mDataArrays = new ArrayList<ChatMsgEntity>();// 消息对象数组
	private SharedPreferences mSharedPreferences;
	private JSONObject mJsonSend = new JSONObject();

	int ret = 0;// 函数调用返回值
	private Button btn_groupMember_onmap;
	private Button mBtn_obd_display;// OBD显示弹框
	private Button mBtn_obd_dsp_return;// OBD显示弹框不显示
	private Button mBtn_trafficlight_display;// 交通灯显示
	private LinearLayout trafficlight_linearlayout;
	private LinearLayout advisespeed_linearlayout;//交通灯处建议速度布局
	private TextView advise_speed;//交通灯处建议速度
	// 消息推送
	private RelativeLayout houtai_ifo_rl_layout;
	private TextView MK5_tv_push_ifo;
	private TextView houtai_tv_ifo;
	private LinearLayout msgpush_linearlayout;
	private Button mBtn_msgpush_display;
	private Button mBtn_msgpush_dsp_return;
	protected Handler myHandler;
	// 点车辆图标，弹的对话框
	private RelativeLayout mMarkerInfoLy;
	private LinearLayout mMarker_linearlayout;
	protected Follow_Thread mFollow_Thread;// 跟驰线程
	private RoutePlanSearch mSearch;
	private DrivingRouteOverlay routeOverlay;// 驾车覆盖物
	//防撞预警
	private LinearLayout anti_collision_linearlayout;
	private ImageView anti_collision_imgview;
	private TextView anti_collision_txtview;
	private long MAX_LIMIT_TIME = 3000; // 防撞预警显示时间
	private boolean Sound_Switch = true;// 防撞预警语音播报开关
    
	// 查询已存在车群组相关的UI
	private RelativeLayout groupExist_relativelayout;
	private Button btn_cargroupExistBack;
	private ListView mListView_cargroup;
	
	protected Car_Data car_info;
	
	// 存储mk5发送来的其他车辆经纬度的标注图标
	public static HashMap<Integer, Marker> addMarkerFromMk5 = new HashMap<Integer, Marker>();
	
	// 存储服务器发送来的车队中车辆经纬度的标注图标
    private static HashMap<Integer, Marker> carGroupMarkerFromCloud = new HashMap<Integer, Marker>();
    
    // 存储编队中车辆经纬度的标注图标
    private static HashMap<String, Marker> formationMarker = new HashMap<String, Marker>();
    
    // 从mk5接收到的当前编队的成员信息
    private static HashMap<String, String> formationVehid = new HashMap<String, String>();
    
    private static String vin;
    private static int intVin;
  //定义Map集合 存储后台发过来判断后的群组成员位置信息
  	public static HashMap<Integer, Car_Data> cargroup_member_Location = new HashMap<Integer, Car_Data>();
  	public static HashMap<String, Car_Data> cargroup_member_LocationWithUsername = new HashMap<String, Car_Data>();
 // 存储需要移除的车队成员的VIN
 	public static List<Integer> cargroup_member_LocationRemove = new ArrayList<Integer>();
 	
 	private LatLng FormationSelfLatLng = null;//编队本车的经纬度
 	private DecimalFormat df = new DecimalFormat("0.00");
 	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// 设置屏幕旋转
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		
		setContentView(R.layout.activity_overlay);
		util = new SharePreferenceUtil(OverlayDemo.this, "saveUserID");

		// 初始化识别对象
		mIat = SpeechRecognizer.createRecognizer(this, mInitListener);
		// 初始化听写Dialog,如果只使用有UI听写功能,无需创建SpeechRecognizer
		iatDialog = new RecognizerDialog(this, mInitListener);

		initBaiduMap();
		initView();
		initReceiver();

		mSharedPreferences = getSharedPreferences(IatSettings.PREFER_NAME, Activity.MODE_PRIVATE);
		mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
		nToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
		// 初始化合成对象
		mTts = SpeechSynthesizer.createSynthesizer(this, mTtsInitListener);

		nSharedPreferences = getSharedPreferences(TtsSettings.PREFER_NAME, Activity.MODE_PRIVATE);
//		Test_Car_MK5();//离线调试加的车，与MK5调时，要屏这句
//		Test_Car_cloud();//离线调试加的车，与MK5调时，要屏这句
		initMarkerClickEvent();// 点击Marker，弹出对话框
		initMapClickEvent();// 点击地图图标事件注册，主要作用是吧
		UI_handler();
		new Thread(new Runnable() {
			int count = 0;
			int tmpcount = 0;
            int countlatlng = 0;
            int count_dismiss = 0;
            int formationcount = 0;
			public void run() {
				while (true) {
					count++;
					tmpcount++;
					countlatlng++;
					count_dismiss++;
					formationcount++;
					if (MyApplication.lightState != 0) {// 交通灯和建议车速显示
						myHandler.sendEmptyMessage(0x001);
					} 
					else if (MyApplication.lightState == 0){
						myHandler.sendEmptyMessage(0x0011);//清除交通灯和建议车速
					}
					
					if (MyApplication.OBDFlag_display) {// OBD显示
						myHandler.sendEmptyMessage(0x002);
					}
					if (MyApplication.MK5InfoFlag || (MyApplication.houtai_msg_push != null)) {// 消息推送
						myHandler.sendEmptyMessage(0x003);
					}
					if ((!ServiceClient.cargroup_member.isEmpty()) && (count >= 5)) {// 其他车辆
						myHandler.sendEmptyMessage(0x004);// 添加车队成员信息位置
						count = 0;// 保证更新车的图标2秒一次
					}
					    
					if (!Client.Other_Car_map.isEmpty()) {//判断mk5是否有发送其他车辆的信息
//						 Log.i(TAG, "myHandler01-------------------------------------");
						myHandler.sendEmptyMessage(0x005);
					}
//					if (MyApplication.Lat_From_MK5 > 0) {
//						myHandler.sendEmptyMessage(0x006);// 显示MK5发来的本车图标
//					}
					
					if ( (MyApplication.team_dismiss) && (count_dismiss >= 20)) {
						
						myHandler.sendEmptyMessage(0x007);// 车队解散 清除地图上关于群组的信息
						
						MyApplication.team_dismiss = false;
						count_dismiss = 0;
					}            
					
					// 防撞预警--张卓鹏
					if (1 == MyApplication.MK5Scene / 1000) { // ***1.交叉路口碰撞预警
						myHandler.sendEmptyMessage(0x008);
					}
																																		
										 
	  
					if (2 == MyApplication.MK5Scene / 1000) { // 2.左转辅助
						myHandler.sendEmptyMessage(0x009);
					}
					if (3 == MyApplication.MK5Scene / 1000) { // ***3.紧急制动预警
						myHandler.sendEmptyMessage(0x010);
					}
					if (4 == MyApplication.MK5Scene / 1000) { // ***4.逆向超车碰撞预警
						myHandler.sendEmptyMessage(0x011);
					}
					if (5 == MyApplication.MK5Scene / 1000) { // ***5.逆向行驶告警（会车）
						myHandler.sendEmptyMessage(0x012);
					}
					if (6 == MyApplication.MK5Scene / 1000) { // 6.盲区预警/变道辅助
						myHandler.sendEmptyMessage(0x013);
					}
					if (7 == MyApplication.MK5Scene / 1000) { // ***7.前方静止/慢速车辆告警
						myHandler.sendEmptyMessage(0x014);
					}
					if (8 == MyApplication.MK5Scene / 1000) { // 8.异常车辆预警
						myHandler.sendEmptyMessage(0x015);
					}
					if (9 == MyApplication.MK5Scene / 1000) { // 9.车辆失控预警
						myHandler.sendEmptyMessage(0x016);
					}
					if (10 == MyApplication.MK5Scene / 1000) { // 10.弱势交通参与者预警
						myHandler.sendEmptyMessage(0x017);
					}
					if (11 == MyApplication.MK5Scene / 1000) { // 11.摩托车预警
						myHandler.sendEmptyMessage(0x018);
					}
					if (12 == MyApplication.MK5Scene / 1000) { // 12.道路危险状况提示
						myHandler.sendEmptyMessage(0x019);
					}
					if (13 == MyApplication.MK5Scene / 1000) { // 13.限速预警
						myHandler.sendEmptyMessage(0x020);
					}
					if (14 == MyApplication.MK5Scene / 1000) { // 14.闯红灯预警
						myHandler.sendEmptyMessage(0x021);
					}
					if (15 == MyApplication.MK5Scene / 1000) { // 15.路口设施辅助紧急车辆预警
						myHandler.sendEmptyMessage(0x022);
					}
					if (16 == MyApplication.MK5Scene / 1000) { // 16.基于环境物体感知的安全驾驶辅助提示
						myHandler.sendEmptyMessage(0x023);
					}
					if (17 == MyApplication.MK5Scene / 1000) { // ***17.前向碰撞预警
						myHandler.sendEmptyMessage(0x024);
					}
					if (18 == MyApplication.MK5Scene / 1000) { // 18.侧向碰撞预警
						myHandler.sendEmptyMessage(0x025);
					}
					if (19 == MyApplication.MK5Scene / 1000) { // ***19.后方碰撞预警
						myHandler.sendEmptyMessage(0x026);
					}
					if (41 == MyApplication.MK5Scene / 1000) { // ***41.会车预警
						myHandler.sendEmptyMessage(0x048);
					}
//					if (0 == MyApplication.MK5Scene) { // ***0.清除警示图片
//						myHandler.sendEmptyMessage(0x049);
//					}
					
					//创建编队回复
					if (MyApplication.createstatus) {
						myHandler.sendEmptyMessage(0x027);
					}
					
					//2s后隐藏回复界面
					if ((System.currentTimeMillis() - MyApplication.rcvTime_cfresponse >= 2000) && (MyApplication.rcvTime_cfresponse != 0)) {
						MyApplication.createstatus = false;
						MyApplication.rcvTime_cfresponse = 0;
						myHandler.sendEmptyMessage(0x028);
					}
					
					//更新编队的状态
					if ((!Client.formation_status.isEmpty()) && (formationcount >= 5)) {// 其他车辆
						myHandler.sendEmptyMessage(0x029);
						formationcount = 0;// 保证更新编队的状态2秒一次
						Log.i(TAG, "更新编队车辆图标！");
					}
					
					//更新车辆状态
					if (!TextUtils.isEmpty(MyApplication.localvehid)) {
						myHandler.sendEmptyMessage(0x030);
						Log.i("liuhongdong", "更新车辆状态-----------"+MyApplication.localvehid);
					}
					
					// 读取显示车队列表
//					if (!Client.motorcade_list.isEmpty()) {
//						Log.i(TAG, "motorcade_list is  not empty----------");
//						myHandler.sendEmptyMessage(0x030);
//					}
				
					
					
                    //给服务器发送车辆的经纬度信息
					if (Util.isNetworkAvailable(OverlayDemo.this) && (countlatlng >= 10)){
						countlatlng = 0;
						if (MyApplication.isMk5LatLng) {
							if (MyApplication.isServerConnect){
								try {
									mJson_to_clound.put("datatype", "LOG_VEH_STATU");
									mJson_to_clound.put("veh_heading", (int) MyOrientationListener.lastX);
									mJson_to_clound.put("veh_id", MyApplication.user_name);
									mJson_to_clound.put("fromtype", "veh");
									mJson_to_clound.put("VIN", String.valueOf(Client.idLocal));//MK5发送来的车辆Id
									mJson_to_clound.put("veh_lat", String.valueOf(desLatLng1.latitude));
									mJson_to_clound.put("veh_lon", String.valueOf(desLatLng1.longitude));
									mJson_to_clound.put("veh_speed", "0");
									mJson_to_clound.put("fromid", MyApplication.user_name);// 值和veh_id一样
									
									Util.send_To_Clound(mJson_to_clound);
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}

							// }if (MyApplication.isServerConnect)
						} else {
							if (MyApplication.isServerConnect){
								try {

									mJson_to_clound.put("datatype", "LOG_VEH_STATU");
									mJson_to_clound.put("veh_heading", (int) MyOrientationListener.lastX);
									mJson_to_clound.put("veh_id", MyApplication.user_name);
									mJson_to_clound.put("fromtype", "veh");
									mJson_to_clound.put("VIN", String.valueOf(MyApplication.randomVin));
									mJson_to_clound.put("veh_lat", String.valueOf(mCurrentLat));
									mJson_to_clound.put("veh_lon", String.valueOf(mCurrentLon));
									mJson_to_clound.put("veh_speed", "0");
									mJson_to_clound.put("fromid", MyApplication.user_name);// 值和veh_id一样

									Util.send_To_Clound(mJson_to_clound);

									Log.i(TAG, "---------------------------------------------------------------------");
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} 
							}
							
						}
					}	
						
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	private void Test_Car_cloud() {
		// 上新街
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				while (true) {
					Car_Data other_car_data = new Car_Data();
					other_car_data.longi_cloud = 106.60339;
					other_car_data.lat_cloud = 29.56212;
					other_car_data.id = 100;
					ServiceClient.cargroup_member_Location.put(100, other_car_data);//
					// 重庆站
					Car_Data other_car_data1 = new Car_Data();
					other_car_data1.longi_cloud = 106.55251;
					other_car_data1.lat_cloud = 29.55527;
					other_car_data1.id = 101;
					ServiceClient.cargroup_member_Location.put(101, other_car_data1);//
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}).start();

	}
	private void Test_Car_MK5() {
		// 上新街
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				while (true) {
					Car_Data other_car_data = new Car_Data();
					other_car_data.longi = 10660339;
					other_car_data.lat = 2956212;
					other_car_data.id = 100;
					Client.Other_Car_map.put(100, other_car_data);//
					// 重庆站
					Car_Data other_car_data1 = new Car_Data();
					other_car_data1.longi = 10657598;//10655251;29.5314767802,106.5759864920
					other_car_data1.lat = 2953147;//2955527;
					other_car_data1.id = 101;
					Client.Other_Car_map.put(101, other_car_data1);//
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}).start();

	}

	private void initMarkerClickEvent() {

		// 对 marker 添加点击相应事件
		mBaiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick(Marker marker) {
				if ((Car_Data) marker.getExtraInfo().get("Car_info") instanceof Car_Data) {
					Car_Data car_info = (Car_Data) marker.getExtraInfo().get("Car_info");
					mMarkerInfoLy.setVisibility(View.VISIBLE);
					mMarker_linearlayout.setVisibility(View.VISIBLE);
					popupInfo_Car_Info(mMarkerInfoLy, car_info);// 把存在marker点中的信息存入布局框架中
					return false;
				} else {
					ToastUtil.makeText(getApplicationContext(), "你点击的本车");
					Log.i(TAG, "你点击的本车");
					return true;
				}
			}
		});
	}

	protected void popupInfo_Car_Info(RelativeLayout mMarkerLy, Car_Data mCar_Data) {
		ViewHolder viewHolder = null;

		if (mMarkerLy.getTag() == null) {
			viewHolder = new ViewHolder();
			viewHolder.infoName = (TextView) mMarkerLy.findViewById(R.id.tvinfo_name);
			viewHolder.infoAddress = (TextView) mMarkerLy.findViewById(R.id.tvinfo_distance);
			viewHolder.car_id = (TextView) mMarkerLy.findViewById(R.id.tvcar_id);
			viewHolder.car_info = (TextView) mMarkerLy.findViewById(R.id.tvcar_info);

			mMarkerLy.setTag(viewHolder);
		}
		viewHolder = (ViewHolder) mMarkerLy.getTag();// info.getPicture()
		viewHolder.infoAddress.setText(String.valueOf(mCar_Data.lat));
		viewHolder.infoName.setText(String.valueOf(mCar_Data.longi));
		viewHolder.car_id.setText(String.valueOf(mCar_Data.id));
		viewHolder.car_info
				.setText("预警模式为-》" + String.valueOf(mCar_Data.Mode) + "预警级别为-》" + String.valueOf(mCar_Data.Level));

		NaviSkipClickEvent_Car_Info(mCar_Data);

	}

	private void NaviSkipClickEvent_Car_Info(final Car_Data mCar_Data) {
		Button navi = (Button) findViewById(R.id.navigate);
		navi.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 点击跟上这辆车
				if (mFollow_Thread == null) {
					mFollow_Thread = new Follow_Thread(mCar_Data);
					mFollow_Thread.start();
				}

			}
		});

		Button btn_stop_carfollow = (Button) findViewById(R.id.cargroup_follow_end);
		btn_stop_carfollow.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 停止跟车
				if (mFollow_Thread != null) {
					mBaiduMap.clear();
					mFollow_Thread.exit = true;
					mFollow_Thread = null;
				}
				mBaiduMap.clear();
			}
		});

	}

	private void initMapClickEvent() {
		mBaiduMap.setOnMapClickListener(new OnMapClickListener() {

			@Override
			public void onMapClick(LatLng arg0) {
				mMarkerInfoLy.setVisibility(View.GONE);
				mMarker_linearlayout.setVisibility(View.GONE);
				msgpush_linearlayout.setVisibility(View.GONE);
				houtai_ifo_rl_layout.setVisibility(View.GONE);
				light.setVisibility(View.GONE);
				trafficlight_linearlayout.setVisibility(View.GONE);
				obd_linearlayout.setVisibility(View.GONE);
				anti_collision_linearlayout.setVisibility(View.GONE);
				mBaiduMap.hideInfoWindow();

			}

			@Override
			public boolean onMapPoiClick(MapPoi arg0) {
				// TODO Auto-generated method stub
				return false;
			}

		});

	}

	private void UI_handler() {
		myHandler = new Handler() {
			public void handleMessage(Message msg) {
				//显示交通灯和建议速度
				if (msg.what == 0x001) {
					//显示交通灯
					light.setVisibility(View.VISIBLE);
					trafficlight_linearlayout.setVisibility(View.VISIBLE);
					//左转
					if (3 == (MyApplication.lightState / 100)) {
						light_left.setImageResource(R.drawable.red_left);
						light_left.setVisibility(View.VISIBLE);
					} else if (2 == (MyApplication.lightState / 100)) {
						light_left.setImageResource(R.drawable.yellow_left);
						light_left.setVisibility(View.VISIBLE);
					} else if (1 == (MyApplication.lightState / 100)) {
						light_left.setImageResource(R.drawable.green_left);
						light_left.setVisibility(View.VISIBLE);
					}
					
					//直行
                    if (3 == ((MyApplication.lightState % 100) /10)) {
						light_straight.setImageResource(R.drawable.red_straight);
						light_straight.setVisibility(View.VISIBLE);
						straight_remain.setText(String.valueOf(MyApplication.lightRemainTime));
						straight_remain.setTextColor(Color.RED);
					} else if (2 == ((MyApplication.lightState % 100) /10)) {
						light_straight.setImageResource(R.drawable.yellow_straight);
						light_straight.setVisibility(View.VISIBLE);
						straight_remain.setText(String.valueOf(MyApplication.lightRemainTime));
						straight_remain.setTextColor(Color.YELLOW);
					} else if (1 == ((MyApplication.lightState % 100) /10)) {
						light_straight.setImageResource(R.drawable.green_straight);
						light_straight.setVisibility(View.VISIBLE);
						straight_remain.setText(String.valueOf(MyApplication.lightRemainTime));
						straight_remain.setTextColor(Color.GREEN);
					}
					
                    //右转
                    if (3 == (MyApplication.lightState % 10)) {
						light_right.setImageResource(R.drawable.red_right);
						light_right.setVisibility(View.VISIBLE);
					} else if (2 == (MyApplication.lightState % 10)) {
						light_right.setImageResource(R.drawable.yellow_right);
						light_right.setVisibility(View.VISIBLE);
					} else if (1 == (MyApplication.lightState % 10)) {
						light_right.setImageResource(R.drawable.green_right);
						light_right.setVisibility(View.VISIBLE);
					}
                    
                    //显示建议速度
                    if (MyApplication.adviseSpeed != 0) {
                        advisespeed_linearlayout.setVisibility(View.VISIBLE);
                        advise_speed.setText(String.valueOf(MyApplication.adviseSpeed));
                    } else {
                    	advisespeed_linearlayout.setVisibility(View.GONE);
                    }
                    
				}
				//清理交通灯和建议速度
				if (msg.what == 0x0011) {
					light.setVisibility(View.GONE);
					trafficlight_linearlayout.setVisibility(View.GONE);
					
					advisespeed_linearlayout.setVisibility(View.GONE);
					MyApplication.adviseSpeed = 0;
				}
				
				// 当没有收到交通灯信息，3s之后消失  
				if (System.currentTimeMillis() - MyApplication.TrafficLightReciveTime >= MAX_LIMIT_TIME) {
					trafficlight_linearlayout.setVisibility(View.GONE);
					advisespeed_linearlayout.setVisibility(View.GONE);
					MyApplication.lightState = 0;
					MyApplication.lightRemainTime = 0;
					MyApplication.adviseSpeed = 0;
				}
				
				if (msg.what == 0x002) {
					Display_OBD();

				}
				if (msg.what == 0x003) {
					Display_Msg_Push();

				}
				if (msg.what == 0x004) {// 车队成员位置显示
					addInfosOverlayHashMap(ServiceClient.cargroup_member);
					
				}
				
				if (msg.what == 0x005) {
//					mBaiduMap.clear();// 为了防止MK5发来的GPS有细小改变，每次更新UI时，先把地图清一下
					setStaticDataToInfoHashMap();// 
				}
				if (msg.what == 0x006) {
//					Util.Display_MyLoc_Marker(mBaiduMap);//显示本车的位置
				}
				if (msg.what == 0x007) {
					mBaiduMap.clear();
				}
				
				// 防撞预警警示
				if (0x008 == msg.what) { // ***1.交叉路口碰撞预警
					if (3 == MyApplication.MK5Scene % 10) { // 声音警示
											
						if (true == Sound_Switch) {
							mTts.startSpeaking("前方路口有碰撞危险", mTtsListener);
//							MyApplication.MK5Scene = 0 ; //直接清空原来发送过来的Scene值之后，图片无法消失（UIhandler无法进入？）
						}
					}
					if (1 == (MyApplication.MK5Scene % 100)/10) { // 左侧路口
						anti_collision_linearlayout.setVisibility(View.VISIBLE);
						anti_collision_linearlayout.setBackgroundResource(R.drawable.warning1011);
					}
					if (2 == (MyApplication.MK5Scene % 100)/10) { // 右侧路口
						anti_collision_linearlayout.setVisibility(View.VISIBLE);
						anti_collision_linearlayout.setBackgroundResource(R.drawable.warning1021);
					}
				}
				 if (0x010 == msg.what) { // ***3.紧急制动预警
					 if (3 == MyApplication.MK5Scene % 10) { // 声音警示
							if (true == Sound_Switch) {
								mTts.startSpeaking("前方车辆紧急刹车", mTtsListener);
							}
					 }
					 anti_collision_linearlayout.setVisibility(View.VISIBLE);
					 anti_collision_linearlayout.setBackgroundResource(R.drawable.warning3001);
											
	  
				 }
		
//				if (0x011 == msg.what) { // ***4.逆向超车碰撞预警
//					if (3 == MyApplication.MK5Scene % 10) {			
//						if (true == Sound_Switch) {
//							mTts.startSpeaking("当前路段超车可能发生碰撞危险，请谨慎驾驶", mTtsListener);
//						}
//					}
//					anti_collision_linearlayout.setVisibility(View.VISIBLE);
//					anti_collision_linearlayout.setBackgroundResource(R.drawable.image_view_car_warning_overtaking);
//				}
				if (0x012 == msg.what) { // ***5.逆向行驶告警（会车）
					if (3 == MyApplication.MK5Scene % 10) {				
						if (true == Sound_Switch) {
							mTts.startSpeaking("当前路段会车可能发生碰撞", mTtsListener);
						}
					}
					anti_collision_linearlayout.setVisibility(View.VISIBLE);
					anti_collision_linearlayout.setBackgroundResource(R.drawable.image_view_car_warning_missing);
				}
				 if (0x014 == msg.what) { // ***7.前方静止/慢速车辆告警
					 if (3 == MyApplication.MK5Scene % 10) {				
							if (true == Sound_Switch) {
								mTts.startSpeaking("前方车辆行驶缓慢", mTtsListener);
							}
					 }
					 anti_collision_linearlayout.setVisibility(View.VISIBLE);
					 anti_collision_linearlayout.setBackgroundResource(R.drawable.warning7001);
				 }
	  
		
		
				if (0x024 == msg.what) { // ***17.前向碰撞预警
															 
												 														
					if (3 == MyApplication.MK5Scene % 10) {
						if (true == Sound_Switch) {
							mTts.startSpeaking("前方车辆碰撞危险", mTtsListener);
						}
					}
					anti_collision_linearlayout.setVisibility(View.VISIBLE);
					anti_collision_linearlayout.setBackgroundResource(R.drawable.warning17001);
				}
	
				if (0x026 == msg.what) { // ***19.后方碰撞预警
															 
																										 
					if (3 == MyApplication.MK5Scene % 10) {
						if (true == Sound_Switch) {
							mTts.startSpeaking("后方车辆碰撞危险", mTtsListener);
						}
					}
					anti_collision_linearlayout.setVisibility(View.VISIBLE);
					anti_collision_linearlayout.setBackgroundResource(R.drawable.warning19001);
				}
				if (0x048 == msg.what) { // ***41.会车预警
					if (3 == MyApplication.MK5Scene % 10) {
						if (true == Sound_Switch) {
							mTts.startSpeaking("前方会车预警", mTtsListener);
						}
					}
					anti_collision_linearlayout.setVisibility(View.VISIBLE);
					anti_collision_linearlayout.setBackgroundResource(R.drawable.warning41001);
				}
				
//				// 当MK5发送0消息时，清除图片
//				if (0x049 == msg.what) { // ***0.清除警示图片
//					anti_collision_linearlayout.setVisibility(View.GONE);
//				}
				// 当预警图片显示3s之后消失  
				if (System.currentTimeMillis() - MyApplication.MK5CarwarnningReciveTime >= MAX_LIMIT_TIME) {
					anti_collision_linearlayout.setVisibility(View.GONE);
					MyApplication.MK5Scene = 0;
				}
				
				// 显示创建编队的回复页面
				if (0x027 == msg.what) {
					create_formation_response_layout.setVisibility(View.VISIBLE);
					formationid_text.setText("车队ID：" + MyApplication.formationid);
				}
				
				// 隐藏创建编队的回复页面 同时开启车队状态的页面
				if (0x028 == msg.what) {
					create_formation_response_layout.setVisibility(View.GONE);
					veh_dismiss_btn.setText("解散");
					motorcade_status_layout.setVisibility(View.VISIBLE);

				}
				
				// 更新编队图标的状态
				if (0x029 == msg.what) {
				    updateFormationStatus(Client.formation_status);
				}
				
				
				// 更新车辆状态
				if (0x030 == msg.what) {
					motorcade_id.setText(MyApplication.formationid);
					motorcade_numbers.setText(String.valueOf(MyApplication.vehicle_numbers));
					motorcade_ordinal.setText(String.valueOf(MyApplication.vehnum));
					pre_vehid.setText(String.valueOf(MyApplication.prevehid));
					vehicle_speed.setText(String.valueOf(df.format(MyApplication.curspeed)));
					vehicle_advisoryspeed.setText(String.valueOf(df.format(MyApplication.advdistance)));
					vehicle_curdistance.setText(String.valueOf(df.format(MyApplication.curdistance)));
					vehicle_advdistance.setText(String.valueOf(df.format(MyApplication.advdistance)));//Math.floor(MyApplication.advdistance)
				}
				
			}
		};

	}
	
	
	
	// 更新编队的状态
	private void updateFormationStatus(final HashMap<String, String> formationStatus) {
		
	    if (formationMarkerFlag) {
	    	formationMarkerFlag = false;
//		
		
		    //获取当前编队的成员vehid
		    for (String formationid : formationStatus.keySet()) {
		    	try {
		    		JSONArray jsonArray = new JSONArray(formationStatus.get(formationid));
				    for (int i = 0; i < jsonArray.length(); i++) {
				        JSONObject vehicleStatus = jsonArray.getJSONObject(i);
				       
				        String vehid = vehicleStatus.getString("vehid");
				        formationVehid.put(vehid, "vehid");
				        Log.w("liuhongdong", "此次的车辆id为："+vehid);
				    }
				    
				} catch (Exception e) {
					// TODO: handle exception
				}
		    	
		    }
		    
//	    	//移除已经离队车辆的图标
		    if (!formationMarker.isEmpty()) {
				for (int i = 0; i < formationMarker.keySet().size(); i++) {
					Log.w("liuhongdong", "formationMarker中车辆id为："+formationMarker.keySet().toArray()[i]);
					if (!formationVehid.containsKey(formationMarker.keySet().toArray()[i])) {
						Log.i("liuhongdong", "此次的移除车辆id为："+formationMarker.keySet().toArray()[i]);
						formationMarker.get(formationMarker.keySet().toArray()[i]).remove();
						formationMarker.remove(formationMarker.keySet().toArray()[i]);
						
					}
				}
				formationVehid.clear();
		    }

	    	
			new Thread() {
				private BitmapDescriptor mIconMaker;
				private InfoWindow mInfoWindow;
				View view = LayoutInflater.from(OverlayDemo.this).inflate(R.layout.marker_layout, null);
				TextView tv_num_price=(TextView) view.findViewById(R.id.tv_num_price);
				
				public void run() {
					OverlayOptions overlayOptions = null;
					Marker marker = null;
					for (String formationid : formationStatus.keySet()) {
						
						try {
							JSONArray jsonArray = new JSONArray(formationStatus.get(formationid));
						    for (int i = 0; i < jsonArray.length(); i++) {
						        JSONObject vehicleStatus = jsonArray.getJSONObject(i);
						        if (!vehicleStatus.getString("vehid").equals(MyApplication.localvehid)) {
						            double longi = vehicleStatus.getDouble("vehlon");
						            double lat = vehicleStatus.getDouble("vehlat");
						            String vehid = vehicleStatus.getString("vehid");
						            
						            LatLng sourceLatLng = new LatLng((double) lat, (double) longi);// 纬度，经度 
									// 转换成百度地图需要的经纬度
									CoordinateConverter converter = new CoordinateConverter();
									converter.from(CoordinateConverter.CoordType.GPS);
									converter.coord(sourceLatLng);
									LatLng BDLatLng = converter.convert();
									
									Log.i("liuhongdong", "GPS原始经纬度数据：纬度"+sourceLatLng.latitude+"经度"+sourceLatLng.longitude);
									Log.i("liuhongdong", "转换后的经纬度数据：纬度"+BDLatLng.latitude+"经度"+BDLatLng.longitude);
									
						            if (!formationMarker.containsKey(vehid)) {
						            	tv_num_price.setText(vehid);
										BitmapDescriptor free_view = BitmapDescriptorFactory.fromView(view);
										
										overlayOptions = new MarkerOptions().position(BDLatLng).icon(free_view).zIndex(10);
										marker = (Marker) (mBaiduMap.addOverlay(overlayOptions));
						            	
										formationMarker.put(vehid, marker);
										
						            } else {
						            	formationMarker.get(vehid).setPosition(BDLatLng);
						            }
						        } else {
						        	double longi = vehicleStatus.getDouble("vehlon");
						            double lat = vehicleStatus.getDouble("vehlat");
						            LatLng sourceLatLng = new LatLng((double) lat, (double) longi);// 纬度，经度 
									// 转换成百度地图需要的经纬度
									CoordinateConverter converter = new CoordinateConverter();
									converter.from(CoordinateConverter.CoordType.GPS);
									converter.coord(sourceLatLng);
									FormationSelfLatLng = converter.convert();
						            
						        }
						    	
						    }
							
							
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
				}
			
			}.start();
		
			formationMarkerFlag = true;
	    }
	}
	
	
	private void setStaticDataToInfoHashMap() {
		if (Client.Other_Car_map.isEmpty()) {
			return;// 如果是空，直接返回
		}

		addInfosOverlayHashMap_mk5(Client.Other_Car_map, cargroup_member_Location);// 把存储在Info中的信息填充到Marker点中，显示在地图上。
	}
	private void addInfosOverlayHashMap_mk5(final HashMap<Integer, Car_Data> other_Car_map, final HashMap<Integer, Car_Data> cargroup_Location) {

		new Thread() {// 一进来就开启新进程，因为网络读取图片不能再主线程中操作
			private BitmapDescriptor mIconMaker = BitmapDescriptorFactory.fromResource(R.drawable.othercarmark_mk5);;

			public void run() {

//				 mBaiduMap.clear();
				OverlayOptions overlayOptions = null;
				Marker marker = null;
				int longi, lat;
				LatLng sourceLatLng, otherLLWithbd09ll;
				
				for (Integer key : other_Car_map.keySet()) {
					car_info = other_Car_map.get(key);
					longi = other_Car_map.get(key).longi;
					lat = other_Car_map.get(key).lat;
					//判断经纬度的位数
					if ((lat / 100000000) > 0 && (longi / 1000000000) > 0) {
						sourceLatLng = new LatLng(((double) lat / MyApplication.Scal_to_Covert7),
								((double) longi / MyApplication.Scal_to_Covert7));// 纬度，经度
						
					} else {
						sourceLatLng = new LatLng(((double) lat / MyApplication.Scal_to_Covert5),
								((double) longi / MyApplication.Scal_to_Covert5));// 纬度，经度
					}
			    	
			    	otherLLWithbd09ll = Util.GPS_Covert(sourceLatLng);
				    if (!cargroup_Location.containsKey(key)) {
				    	
				    	if (!addMarkerFromMk5.containsKey(key)) {
							
							overlayOptions = new MarkerOptions().position(otherLLWithbd09ll).icon(mIconMaker)
									.zIndex(5);// zIndex没什么用
							marker = (Marker) (mBaiduMap.addOverlay(overlayOptions));
							addMarkerFromMk5.put(key, marker);
				    	} else {
				    		addMarkerFromMk5.get(key).setPosition(otherLLWithbd09ll);
				    	}
				    } 
				    else {
				    	if (addMarkerFromMk5.containsKey(key)) {
				    		addMarkerFromMk5.get(key).remove();
					    	addMarkerFromMk5.remove(key);
					    	Log.d(TAG, "marker from mk5 always clear");
				    	}
				    	
				    }
				}
			}

		}.start();

	}
	protected void Display_Msg_Push() {
		msgpush_linearlayout.setVisibility(View.VISIBLE);
		houtai_ifo_rl_layout.setVisibility(View.VISIBLE);
		// 显示路测消息内容
		houtai_tv_ifo.setVisibility(View.VISIBLE);
		houtai_tv_ifo.setText(MyApplication.houtai_msg_push);
		MK5_tv_push_ifo.setVisibility(View.VISIBLE);
		MK5_tv_push_ifo.setText(MyApplication.MK5Info);

	}

	protected void Display_OBD() {
		obd_linearlayout.setVisibility(View.VISIBLE);
		tv_vbat.setText((MyApplication.s_obd_VBAT) + "v");
		tv_rpm.setText((MyApplication.s_obd_RPM) + "rpm");
		tv_spd.setText((MyApplication.s_obd_SPD) + "km/h");
		tv_tp.setText((MyApplication.s_obd_TP) + "%");
		tv_lod.setText((MyApplication.s_obd_LOD) + "%");
		tv_ect.setText((MyApplication.s_obd_ECT) + "℃");
		tv_fli.setText((MyApplication.s_obd_FLI) + "%");
		tv_mph.setText((MyApplication.s_obd_MPH) + "L/100km");
		// 当超过最大的显示时间后，就清标志位，然后把所有显示清了，等MK5发来下次的信息
		if (System.currentTimeMillis() - MyApplication.OBD_info_recv_Time > MAX_LIMIT_TIME) {
			MyApplication.OBDFlag_display = false;// MK5发来置1，显示了清0，
			tv_vbat.setText("over time" + "v");
			tv_rpm.setText("over time" + "rpm");
			tv_spd.setText("over time" + "km/h");
			tv_tp.setText("over time" + "%");
			tv_lod.setText("over time" + "%");
			tv_ect.setText("over time" + "℃");
			tv_fli.setText("over time" + "%");
			tv_mph.setText("over time" + "L/100km");
		}

	}


	/*
	 * 把地图布局控制可视化
	 */
	private void setTrafficLight_layout_visual() {
		light.setVisibility(View.VISIBLE);
		trafficlight_linearlayout.setVisibility(View.VISIBLE);

	}

	/*
	 * 地图与定位初始化
	 */
	private void initBaiduMap() {
		// 地图初始化
		mMapView = (MapView) findViewById(R.id.vtmapView);
		mBaiduMap = mMapView.getMap();
		mCurrentMode = LocationMode.NORMAL;
		mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(mCurrentMode, true, mCurrentMarker));
		// 开启定位图层
		mBaiduMap.setMyLocationEnabled(true);
		// 定位初始化
		mLocClient = new LocationClient(getApplicationContext());
		mLocClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);// 设置定位模式为高精度模式
		option.setCoorType("bd09ll"); // 设置坐标类型
		option.setScanSpan(1000);// 1秒一次定位
		
		mLocClient.setLocOption(option);
		mLocClient.start();
		// 初始化搜索模块，注册事件监听
		mSearch = RoutePlanSearch.newInstance();
		mSearch.setOnGetRoutePlanResultListener(this);
	}

	/**
	 * 定位监听函数
	 */
	public class MyLocationListenner implements BDLocationListener {

		public void onReceiveLocation(BDLocation location) {
			// map view 销毁后不在处理新接收的位置
			if (location == null || mMapView == null) {
				return;
			}
			mCurrentLat = location.getLatitude();
			mCurrentLon = location.getLongitude();
			mCurrentAccracy = location.getRadius();

			// 判断是否有MK5的经纬度
			if (MyApplication.isMk5LatLng) {
				// Log.i(tag, "isMK5FirstLocation == 2 中------>>>>");

				// LJL 转换成百度地图需要的经纬度
				CoordinateConverter converter = new CoordinateConverter();
				converter.from(CoordinateConverter.CoordType.GPS);
				LatLng sourceLatLng;
				//判断经纬度的位数
				if ((Client.LatLocalNumber / 100000000) > 0 && (Client.LongLocalNumber / 1000000000) > 0) {
					sourceLatLng = new LatLng(((double) (Client.LatLocalNumber)) / MyApplication.Scal_to_Covert7,
							((double) (Client.LongLocalNumber)) / MyApplication.Scal_to_Covert7);
				} else {
					sourceLatLng = new LatLng(((double) (Client.LatLocalNumber)) / MyApplication.Scal_to_Covert5,
							((double) (Client.LongLocalNumber)) / MyApplication.Scal_to_Covert5);
				}
				converter.coord(sourceLatLng);
				desLatLng1 = converter.convert();

				Log.e("MK5Location", "MapActivity 中 自身MK5的    原始 纬度  是：" + String.valueOf(Client.LatLocalNumber));
				Log.e("MK5Location", "MapActivity 中 自身MK5的    原始 经度 是：" + String.valueOf(Client.LongLocalNumber));

				Log.e("MK5Location", "MapActivity 中 自身MK5的    转换后的 纬度  是：" + String.valueOf(desLatLng1.latitude));
				Log.e("MK5Location", "MapActivity 中 自身MK5的      转换后的 经度  是：" + String.valueOf(desLatLng1.longitude));

				locData = new MyLocationData.Builder().accuracy(location.getRadius())
						// 此处设置开发者获取到的方向信息，顺时针0-360
						.direction(mCurrentDirection).latitude(desLatLng1.latitude)
						.longitude(desLatLng1.longitude).build();
				mBaiduMap.setMyLocationData(locData);

			} else if (FormationSelfLatLng != null) {//编队是本车的定位数据
				locData = new MyLocationData.Builder().accuracy(location.getRadius())
						// 此处设置开发者获取到的方向信息，顺时针0-360
						.direction(mCurrentDirection).latitude(FormationSelfLatLng.latitude)
						.longitude(FormationSelfLatLng.longitude).build();
				mBaiduMap.setMyLocationData(locData);
				FormationSelfLatLng = null;
				
			} else {
				// 如果Mk5没有发定位数据，就使用网络定位数据
				locData = new MyLocationData.Builder().accuracy(location.getRadius())
						// 此处设置开发者获取到的方向信息，顺时针0-360
						.direction(mCurrentDirection).latitude(location.getLatitude())
						.longitude(location.getLongitude()).build();
				mBaiduMap.setMyLocationData(locData);
			}

			if (isFirstLoc) {
				isFirstLoc = false;
				LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
				MapStatus.Builder builder = new MapStatus.Builder();
				builder.target(ll).zoom(18.0f);
				mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
			}
		}

		public void onReceivePoi(BDLocation poiLocation) {
		}
	}

	private void initView() {
		
		// 车群组相关按钮布局
		cargroup_button = (LinearLayout) findViewById(R.id.carGroup_button);
		cargroup_button.setVisibility(View.GONE);

		// 车群组相关按钮布局 的触发按钮
		btn_carGroup = (Button)findViewById(R.id.carGroup_trigger);
		btn_carGroup.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				btn_carGroup.setVisibility(View.GONE);
				cargroup_button.setVisibility(View.VISIBLE);
				return true;
			}
		});
		
		// 查询已经存在的车群组
		btn_exist_gruop = (Button)findViewById(R.id.exist_group);
		btn_exist_gruop.setOnClickListener(this);
		
		groupExist_relativelayout = (RelativeLayout) findViewById(R.id.GroupExist_Relative);
		btn_cargroupExistBack = (Button) findViewById(R.id.group_exist_back_btn);
		btn_cargroupExistBack.setOnClickListener(this);
		mListView_cargroup = (ListView) findViewById(R.id.car_group_listview);
		mListView_cargroup.setOnItemClickListener(this);
		
		// 车群组界面 fragment（碎片组件）
		create_cargroup_fragmentRelative = (RelativeLayout) findViewById(R.id.id_fragment_relative);
		create_cargroup_fragmentRelative.setVisibility(View.GONE);
		
		// 创建车群组组件
		btn_create_group = (Button) findViewById(R.id.group);
		btn_create_group.setOnClickListener(this);
		
		// 邀约,解散，群组成员，成员位置
		groupInvite_linearlayout = (LinearLayout) findViewById(R.id.group_invite);
		EditText_invite_group_name = (EditText) findViewById(R.id.group_invite_name);
		inviteUser = (EditText) findViewById(R.id.group_invite_user_name);
		btn_group_inviteBack = (Button) findViewById(R.id.group_invite_back_btn);
		btn_group_inviteBack.setOnClickListener(this);
		btn_group_inviteOK = (Button) findViewById(R.id.group_invite_ok);
		btn_group_inviteOK.setOnClickListener(this);

		btn_delGroup = (Button) findViewById(R.id.delete_group);
		btn_delGroup.setOnClickListener(this);
		
		btn_groupInvate = (Button) findViewById(R.id.group_invate);
		btn_groupInvate.setOnClickListener(this);
		
		btn_groupMember_name = (Button) findViewById(R.id.group_member_name_list);
		btn_groupMember_name.setOnClickListener(this);
		
		groupMember_relativelayout = (RelativeLayout) findViewById(R.id.car_group_member);
		btn_groupMemberBack = (Button) findViewById(R.id.group_member_back_btn);
		btn_groupMemberBack.setOnClickListener(this);
		mListView_group_member = (ListView) findViewById(R.id.group_listview_member);

		groupInviteApply = (LinearLayout) findViewById(R.id.group_invite_apply);
		group__invite_apply_name = (TextView) findViewById(R.id.group__invite_apply_name);
		group_apply_start = (TextView) findViewById(R.id.group_apply_start);
		group_apply_end = (TextView) findViewById(R.id.group_apply_end);

		groupInvite_agree_btn = (Button) findViewById(R.id.group_invite_agree);
		groupInvite_agree_btn.setOnClickListener(this);
		groupInvite_cancel_btn = (Button) findViewById(R.id.group_invite_cancel);
		groupInvite_cancel_btn.setOnClickListener(this);
		
		//聊天
		group_chat_btn = (Button) findViewById(R.id.group_chat);
		group_chat_btn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				cargroup_chat_Relative.setVisibility(View.VISIBLE);
			}
		});
		
		btn_cargroup_back = (Button) findViewById(R.id.cargroup_back);
		btn_cargroup_back.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				btn_carGroup.setVisibility(View.VISIBLE);
				cargroup_button.setVisibility(View.GONE);
				return true;
			}
		});

		// mk5业务相关 布局
		Mk5fuction_LinearLayout = (LinearLayout) findViewById(R.id.MK5info_LinearLayout);
		Mk5fuction_LinearLayout.setVisibility(View.GONE);

		//  mk5业务 的触发按钮
		Mk5_button = (Button)findViewById(R.id.MK5_trigger);
		Mk5_button.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				Mk5_button.setVisibility(View.GONE);
				Mk5fuction_LinearLayout.setVisibility(View.VISIBLE);
				return true;
			}
		});
		
		btn_mk5function_back = (Button) findViewById(R.id.MK5function_back);
		btn_mk5function_back.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				Mk5_button.setVisibility(View.VISIBLE);
				Mk5fuction_LinearLayout.setVisibility(View.GONE);
				return true;
			}
		});
		
		
		// 定自己的位置
		myLocation = (Button) findViewById(R.id.centre_location);
		myLocation.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				centerLoc();

			}
		});

		// 群组聊天布局
		cargroup_chat_Relative = (RelativeLayout) findViewById(R.id.car_group);
		cargroup_chat_Relative.setVisibility(View.GONE);

		// 关闭隐藏聊天按钮
		chat_close_btn = (Button) findViewById(R.id.group_chat_back_btn);
		chat_close_btn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				cargroup_chat_Relative.setVisibility(View.GONE);
			}
		});

		// 群组聊天显示，发送，输入
		mchat_content_ListView = (ListView) findViewById(R.id.group_listview);
		mBtn_chat_Send = (Button) findViewById(R.id.group_send);
		mBtn_chat_Send.setOnClickListener(this);
		mEditText_chat_Content = (EditText) findViewById(R.id.group_input);
		mBtn_chat_Sound = (Button) findViewById(R.id.group_soundBtn);
		mBtn_chat_Sound.setOnClickListener(this);

		// 后台推送车队公告信息
		mcar_group_announcement = (RelativeLayout) findViewById(R.id.car_group_announcement);
		mannouncement_content_TextView = (TextView) findViewById(R.id.group_textview_announcement);
		mBtn_announcement_Back = (Button) findViewById(R.id.group_announcement_back_btn);
		mBtn_announcement_Back.setOnClickListener(this);

		// OBD
		mBtn_obd_display = (Button) findViewById(R.id.btn_OBD);
		mBtn_obd_display.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				obd_linearlayout.setVisibility(View.VISIBLE);
			}
		});
		mBtn_obd_dsp_return = (Button) findViewById(R.id.obdreturn_btn);
		mBtn_obd_dsp_return.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				obd_linearlayout.setVisibility(View.GONE);

			}
		});
		obd_linearlayout = (LinearLayout) findViewById(R.id.obd_linearlayout);
		obd_linearlayout.setVisibility(View.GONE);
		// OBD 消息显示界面
		tv_vbat = (TextView) findViewById(R.id.obd_tv_vbat);// OBD发来的电池电压
		tv_rpm = (TextView) findViewById(R.id.obd_tv_rpm);// OBD发来的发动机转速
		tv_spd = (TextView) findViewById(R.id.obd_tv_spd);// OBD发来的车速
		tv_tp = (TextView) findViewById(R.id.obd_tv_tp);// OBD发来的节气门开度
		tv_lod = (TextView) findViewById(R.id.obd_tv_lod);// OBD发来的发动机负荷
		tv_ect = (TextView) findViewById(R.id.obd_tv_ect);// OBD发来的冷却液水温
		tv_fli = (TextView) findViewById(R.id.obd_tv_fli);// OBD发来的剩余油量
		tv_mph = (TextView) findViewById(R.id.obd_tv_mph);// OBD发来的瞬时油耗

		//交通灯处建议速度
		advisespeed_linearlayout = (LinearLayout) findViewById(R.id.advisespeed_linearlayout);
		advisespeed_linearlayout.setVisibility(View.VISIBLE);
		advise_speed = (TextView) findViewById(R.id.advise_speed);
		
		// 交通灯界面显示
		trafficlight_linearlayout = (LinearLayout) findViewById(R.id.trafficlight_linearlayout);
		trafficlight_linearlayout.setVisibility(View.GONE);		
		light = (RelativeLayout) findViewById(R.id.light_layout);
		light.setVisibility(View.GONE);
		light_left = (ImageView) findViewById(R.id.light_left);
		light_straight = (ImageView) findViewById(R.id.light_straight);
		light_right = (ImageView) findViewById(R.id.light_right);
		straight_remain = (TextView) findViewById(R.id.straight_remain);
		
		mBtn_trafficlight_display = (Button) findViewById(R.id.btn_TrafficLight);
		mBtn_trafficlight_display.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				light.setVisibility(View.VISIBLE);
				trafficlight_linearlayout.setVisibility(View.VISIBLE);
			}
		});

		// 消息推送画面
		// 路测后台 消息显示界面初始化
		msgpush_linearlayout = (LinearLayout) findViewById(R.id.msgpush_linearlayout);
		msgpush_linearlayout.setVisibility(View.GONE);
		houtai_ifo_rl_layout = (RelativeLayout) findViewById(R.id.houtai_layout);
		houtai_ifo_rl_layout.setVisibility(View.GONE);
		// 显示路测消息 的消息头：路测消息
		MK5_tv_push_ifo = (TextView) findViewById(R.id.mk5_tv);
		MK5_tv_push_ifo.setVisibility(View.VISIBLE);
		// 显示路测消息内容
		houtai_tv_ifo = (TextView) findViewById(R.id.houtai_tv);
		houtai_tv_ifo.setVisibility(View.GONE);

		mBtn_msgpush_display = (Button) findViewById(R.id.btn_MsgPush);
		mBtn_msgpush_display.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				msgpush_linearlayout.setVisibility(View.VISIBLE);
				houtai_ifo_rl_layout.setVisibility(View.VISIBLE);
			}
		});
		mBtn_msgpush_dsp_return = (Button) findViewById(R.id.houtaireturn_btn);
		mBtn_msgpush_dsp_return.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				msgpush_linearlayout.setVisibility(View.GONE);
				houtai_ifo_rl_layout.setVisibility(View.GONE);
			}
		});
		// 点车辆图标，弹出的对话框
		mMarkerInfoLy = (RelativeLayout) findViewById(R.id.id_marker_info);
		mMarkerInfoLy.setVisibility(View.GONE);
		mMarker_linearlayout = (LinearLayout) findViewById(R.id.cargroup_member_linearlayout);
		mMarker_linearlayout.setVisibility(View.GONE);

		// 防撞预警
		anti_collision_linearlayout = (LinearLayout) findViewById(R.id.anti_collision_linearlayout);
		anti_collision_linearlayout.setVisibility(View.VISIBLE);
//		anti_collision_linearlayout.setBackgroundResource(R.drawable.warning1011);
		anti_collision_imgview = (ImageView) findViewById(R.id.anti_collision_imgview);
		anti_collision_txtview = (TextView) findViewById(R.id.anti_collision_txtview);
		
		// 车辆编队相关按钮 整体布局
		formation_LinearLayout = (LinearLayout) findViewById(R.id.MK5formation_LinearLayout);
		formation_LinearLayout.setVisibility(View.GONE);

		// 车辆编队相关按钮布局 的触发按钮
		MK5_formation_trigger = (Button)findViewById(R.id.MK5_formation_trigger);
		MK5_formation_trigger.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				MK5_formation_trigger.setVisibility(View.GONE);
				formation_LinearLayout.setVisibility(View.VISIBLE);
				return true;
			}
		});
		
		//车辆编队相关功能按钮
		
		//创建编队
		btn_formation = (Button) findViewById(R.id.btn_Formation);
		btn_formation.setOnClickListener(this);
		
		//创建编队页面的布局，编辑框和按钮
		create_formation_layout= (RelativeLayout) findViewById(R.id.create_formation_layout);
	    formationName = (EditText) findViewById(R.id.formation_name);
	    formationStart = (EditText) findViewById(R.id.formation_start);
	    formationEnd = (EditText) findViewById(R.id.formation_end);
	    btn_formation_creat = (Button) findViewById(R.id.formation_creat_btn);
	    btn_formation_back = (Button) findViewById(R.id.formation_back_btn);
	    create_formation_layout.setVisibility(View.GONE);
	    btn_formation_creat.setOnClickListener(this);
	    btn_formation_back.setOnClickListener(this);
		
		//查询编队
		btn_queryformation = (Button) findViewById(R.id.btn_QueryFormation);
		btn_queryformation.setOnClickListener(this);
		
		//收起编队相关功能按钮
		btn_MK5formation_back = (Button) findViewById(R.id.MK5formation_back);
		btn_MK5formation_back.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				MK5_formation_trigger.setVisibility(View.VISIBLE);
				formation_LinearLayout.setVisibility(View.GONE);
				return true;
			}
		});
		
		// 创建编队回复的相关的布局和信息设置文本
		create_formation_response_layout = (RelativeLayout) findViewById(R.id.create_formation_response_layout);
		formationid_text = (TextView) findViewById(R.id.formationid_textview);
	    create_formation_response_layout.setVisibility(View.GONE);
//	    formationid_text.setText("车队ID：渝A.JL485");
	    
	    // 车辆状态相关布局与信息
	    motorcade_status_layout = (RelativeLayout) findViewById(R.id.motorcade_status_layout);
	    motorcade_id = (TextView) findViewById(R.id.motorcade_id);
	    motorcade_numbers = (TextView) findViewById(R.id.motorcade_numbers);
	    motorcade_ordinal = (TextView) findViewById(R.id.vehicle_ordinal);
	    pre_vehid = (TextView) findViewById(R.id.vehicle_preid);
	    vehicle_speed = (TextView) findViewById(R.id.vehicle_speed);
	    vehicle_advisoryspeed = (TextView) findViewById(R.id.vehicle_advisoryspeed);
	    vehicle_curdistance = (TextView) findViewById(R.id.vehicle_curdistance);
	    vehicle_advdistance = (TextView) findViewById(R.id.vehicle_advdistance);
	    veh_dismiss_btn = (Button) findViewById(R.id.veh_dismiss_btn);
	    veh_dismiss_btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (TCPService.Flag_Connection_MK5) {
					if ("解散".equals(veh_dismiss_btn.getText().toString())) {
						try {
							Log.i("liuhongdong", "头车发送解散");
							json_vehdismiss_formation.put("msgtype", "disbandformation");

						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		        		Util.send_To_MK5("FORMATION", json_vehdismiss_formation);
					} else if("离队".equals(veh_dismiss_btn.getText().toString())){
						try {
							Log.i("liuhongdong", "从车发送离队");
							json_vehdismiss_formation.put("msgtype", "leaveformation");

						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		        		Util.send_To_MK5("FORMATION", json_vehdismiss_formation);
					}
	        	} else {
					Toast.makeText(OverlayDemo.this, "与MK5未连接，发送失败", Toast.LENGTH_SHORT).show();
	        	}
                
			}
		});
	    
	    
 	    motorcade_status_layout.setVisibility(View.GONE);
 	    
 	    // 车队列表
 	    motorcade_list_layout = (RelativeLayout) findViewById(R.id.motorcade_list_layout);
 	    motorcadelist_listview  = (ListView) findViewById(R.id.motorcadelistview);
 	    motorcadelist_listview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				Client.motorcade_list_flag = false;
				motorcade_list_layout.setVisibility(View.GONE);
				motorcadeinfo_layout.setVisibility(View.VISIBLE);
				formation formationitem = motorcade_arraylist.get(position);
		 	    motorcadeinfo_id.setText(formationitem.getFormationid());
		 	    motorcadeinfo_name.setText(formationitem.getFormationname()); 
		 	    motorcadeinfo_numbers.setText(String.valueOf(formationitem.getFormationnum()));
		 	    if (formationitem.getFormaitontype() == 0) {
		 	    	motorcadeinfo_type.setText("unknown");
		 	    } else if (formationitem.getFormaitontype() == 1) {
		 	    	motorcadeinfo_type.setText("car");
		 	    } else if (formationitem.getFormaitontype() == 2) {
		 	    	motorcadeinfo_type.setText("bus");
		 	    } else if (formationitem.getFormaitontype() == 3) {
		 	    	motorcadeinfo_type.setText("special");
		 	    } else if (formationitem.getFormaitontype() == 4) {
		 	    	motorcadeinfo_type.setText("axle");
		 	    }
		 	    motorcadeinfo_start.setText(formationitem.getFormationstartlocation());
			    motorcadeinfo_end.setText(formationitem.getFormationdestination());
			    				
			}
			
		});
 	    Button motorcadelist_back_btn = (Button) findViewById(R.id.motorcadelist_back_btn);
 	    motorcadelist_back_btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				motorcade_list_layout.setVisibility(View.GONE);
			}
		});
 	    motorcade_list_layout.setVisibility(View.GONE);
 	    
 	    // 车队详细信息
 	    motorcadeinfo_layout = (RelativeLayout) findViewById(R.id.motorcadeinfo_layout);
 	    motorcadeinfo_id = (TextView) findViewById(R.id.motorcadeinfo_id);
 	    motorcadeinfo_name = (TextView) findViewById(R.id.motorcade_name);
 	    motorcadeinfo_numbers = (TextView) findViewById(R.id.motorcade_number);
 	    motorcadeinfo_type = (TextView) findViewById(R.id.motorcade_type);
 	    motorcadeinfo_start = (TextView) findViewById(R.id.motorcade_start);
	    motorcadeinfo_end = (TextView) findViewById(R.id.motorcade_end);
	    Button motorcadeinfo_back_btn = (Button) findViewById(R.id.motorcadeinfo_back_btn);
	    motorcadeinfo_back_btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				motorcadeinfo_layout.setVisibility(View.GONE);
			}
		});
	    Button join_formation_btn = (Button) findViewById(R.id.join_formation);
	    join_formation_btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (TCPService.Flag_Connection_MK5) {
	        		try {
						json_JoinFormation.put("msgtype", "joinformation");
						json_JoinFormation.put("formationid", motorcadeinfo_id.getText().toString());
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	        		Util.send_To_MK5("FORMATION", json_JoinFormation);
	        		motorcadeinfo_layout.setVisibility(View.GONE);
	        		Client.motorcade_list.clear();
	        		Client.motorcade_list_flag = true;
	        	} else {
					Toast.makeText(OverlayDemo.this, "与MK5未连接，发送失败", Toast.LENGTH_SHORT).show();
					Client.motorcade_list_flag = true;
	        	}
			}
		});
	    motorcadeinfo_layout.setVisibility(View.GONE);
	    
	    // 车辆入队
	    joinrequestinfo_layout = (RelativeLayout) findViewById(R.id.joinrequestinfo_layout);
	    joinrequest_vehid = (TextView) findViewById(R.id.joinrequest_vehid);
	    joinrequest_quadrant = (TextView) findViewById(R.id.joinrequest_quadrant);
	    joinrequest_distance = (TextView) findViewById(R.id.joinrequest_distance);
	    joinrequest_agree_btn = (Button) findViewById(R.id.joinrequest_agree);
	    joinrequest_agree_btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (TCPService.Flag_Connection_MK5) {
	        		try {
						json_JoinRequst_Result.put("msgtype", "joinformationrespons");
						json_JoinRequst_Result.put("result", true);
						json_JoinRequst_Result.put("vehid", joinrequest_vehid.getText().toString());
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	        		Util.send_To_MK5("FORMATION", json_JoinRequst_Result);
	        		joinrequestinfo_layout.setVisibility(View.GONE);
	        		Client.requestflag = false;
	        		joinrequestinfo_layout.postDelayed(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							Client.requestflag = true;
						}
					}, 3000);
	        	} else {
					Toast.makeText(OverlayDemo.this, "与MK5未连接，发送失败", Toast.LENGTH_SHORT).show();
					joinrequestinfo_layout.setVisibility(View.GONE);
					Client.requestflag = false;
	        	}
			}
		});
	    joinrequest_reject_btn = (Button) findViewById(R.id.joinrequest_reject);
	    joinrequest_reject_btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (TCPService.Flag_Connection_MK5) {
	        		try {
						json_JoinRequst_Result.put("msgtype", "joinresponse");
						json_JoinRequst_Result.put("result", false);
						json_JoinRequst_Result.put("vehid", joinrequest_vehid.getText().toString());
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	        		Util.send_To_MK5("FORMATION", json_JoinRequst_Result);
	        		joinrequestinfo_layout.setVisibility(View.GONE);
	        		Client.requestflag = false;
	        	} else {
					Toast.makeText(OverlayDemo.this, "与MK5未连接，发送失败", Toast.LENGTH_SHORT).show();
					joinrequestinfo_layout.setVisibility(View.GONE);
					Client.requestflag = false;
	        	}
			}
		});
	    joinrequestinfo_layout.setVisibility(View.GONE);
	    
	    //请求入队车辆收到头车是否同意的回复
	    joinresponseinfo_layout = (RelativeLayout) findViewById(R.id.joinresponseinfo_layout);
	    joinresponse_id = (TextView) findViewById(R.id.joinresponse_id);
	    joinresponse_result = (TextView) findViewById(R.id.joinresponse_result);
	    joinresponse_preid = (TextView) findViewById(R.id.joinresponse_preid);
	    joinresponse_behid = (TextView) findViewById(R.id.joinresponse_behid);
	    joinresponse_chghd = (TextView) findViewById(R.id.joinresponse_chghd);
	    joinresponseinfo_layout.setVisibility(View.GONE);
	    
	    // 入队请求同意后，车队中其他车辆的调整
	    entranceprompt_otheradjust = (RelativeLayout) findViewById(R.id.entranceprompt_otheradjust);
	    otheradj_advspeed = (TextView) findViewById(R.id.advspeed);
	    otheradj_advdistance = (TextView) findViewById(R.id.advdistance);
	    otheradj_curdistance = (TextView) findViewById(R.id.curdistance);
	    otheradj_joincarnum = (TextView) findViewById(R.id.joincarnum);
	    entranceprompt_otheradjust.setVisibility(View.GONE);
	     	    
	    //入队请求同意后，入队车辆的入队提示
	    entranceprompt_layout = (RelativeLayout) findViewById(R.id.entranceprompt_layout);
	    entranceprompt_text = (TextView) findViewById(R.id.entranceprompt_text);
	    entranceprompt_layout.setVisibility(View.GONE);
	    
	    //入队请求同意后，入队车辆的入队完成提示
	    entrancefinish_layout = (RelativeLayout) findViewById(R.id.entrancefinish_layout);
	    finish_vehid = (TextView) findViewById(R.id.finish_vehid);
	    entrancefinish_layout.setVisibility(View.GONE);
	    
	    //头车解散车队
	    dismissformation_layout = (RelativeLayout) findViewById(R.id.dismissformation_layout);
	    dismissInfo = (TextView) findViewById(R.id.dismiss_info);
	    dismissformation_layout.setVisibility(View.GONE);
	    
	    //离队提醒
	    leaveformation_layout = (RelativeLayout) findViewById(R.id.leaveformation_layout);
	    leaveformation_layout.setVisibility(View.GONE);
	    
	    //车队中有车辆离队成功后，通知其他车辆有车离队
	    otherleaveformation_layout = (RelativeLayout) findViewById(R.id.otherleaveformation_layout);
	    leaveVehid = (TextView) findViewById(R.id.leave_vehid);
	    otherleaveformation_layout.setVisibility(View.GONE);
	}

	/**
	 * 初始化监听器。
	 */
	private InitListener mInitListener = new InitListener() {

		@Override
		public void onInit(int code) {
			if (code != ErrorCode.SUCCESS) {
				showTip("初始化失败,错误码：" + code);
			}
		}
	};

	/**
	 * 初始化监听。
	 */
	private InitListener mTtsInitListener = new InitListener() {
		@Override
		public void onInit(int code) {
			if (code != ErrorCode.SUCCESS) {
				showTip("初始化失败,错误码：" + code);
			}
		}
	};

	/**
	 * 合成回调监听。
	 */
	private SynthesizerListener mTtsListener = new SynthesizerListener() {
		@Override
		public void onSpeakBegin() {
			Sound_Switch = false; //播放开始时，关闭开关。
			showTip("开始播放");
		}

		@Override
		public void onSpeakPaused() {
			showTip("暂停播放");
		}

		@Override
		public void onSpeakResumed() {
			showTip("继续播放");
		}

		@Override
		public void onBufferProgress(int percent, int beginPos, int endPos, String info) {
			mPercentForBuffering = percent;
			nToast.setText(
					String.format(getString(R.string.tts_toast_format), mPercentForBuffering, mPercentForPlaying));

			nToast.show();
		}

		@Override
		public void onSpeakProgress(int percent, int beginPos, int endPos) {
			mPercentForPlaying = percent;
			showTip(String.format(getString(R.string.tts_toast_format), mPercentForBuffering, mPercentForPlaying));
		}

		@Override
		public void onCompleted(SpeechError error) {
			if (error == null) {
				Sound_Switch = true; //播放完成后，打开开关。
				showTip("播放完成");
			} else if (error != null) {
				showTip(error.getPlainDescription(true));
			}
		}

		@Override
		public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {
			// TODO Auto-generated method stub

		}
	};

	/**
	 * 听写监听器。
	 */
	private RecognizerListener recognizerListener = new RecognizerListener() {

		@Override
		public void onBeginOfSpeech() {
			showTip("开始说话");
		}

		@Override
		public void onError(SpeechError error) {
			showTip(error.getPlainDescription(true));
		}

		@Override
		public void onEndOfSpeech() {
			showTip("结束说话");
		}

		@Override
		public void onResult(RecognizerResult results, boolean isLast) {
			String text = JsonParser.parseIatResult(results.getResultString());
			mEditText_chat_Content.append(text);
			mEditText_chat_Content.setSelection(mEditText_chat_Content.length());
			if (isLast) {
				// TODO 最后的结果
				send();
			}
		}

		@Override
		public void onVolumeChanged(int volume) {
			showTip("当前正在说话，音量大小：" + volume);
		}

		@Override
		public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {

		}
	};

	/**
	 * 听写UI监听器
	 */
	private RecognizerDialogListener recognizerDialogListener = new RecognizerDialogListener() {
		public void onResult(RecognizerResult result, boolean isLast) {

			// Log.d(TAG, "recognizer result：" + result.getResultString());
			System.out.println("=======进入RecognizerDialogListener（）中========= ");
			String text = JsonParser.parseIatResult(result.getResultString());
			// 语音识别文本
			// mEditTextContent.append(text);
			// try {
			// Thread.sleep(3000);
			// } catch (InterruptedException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			// send2();
			if (text.length() > 1) {
				nowTime = MyDate.getDateEN();
				// 判断 两个时间是否相差 一段时间
				if (isTimePassed()) {
					ChatMsgEntity entity = new ChatMsgEntity("我", text, false);
					mDataArrays.add(entity);
					mAdapter = new ChatMsgViewAdapter(OverlayDemo.this, mDataArrays);
					mchat_content_ListView.setAdapter(mAdapter);
					mchat_content_ListView.setSelection(mAdapter.getCount() - 1);
					mEditText_chat_Content.setText("");// 清空编辑框数据
					mchat_content_ListView.setSelection(mchat_content_ListView.getCount() - 1);
					recordTime = MyDate.getDateEN(); // 记录当前时间
				}
			}
			ImageView imageView = new ImageView(OverlayDemo.this);
			if (text.equals("。")) {
				mEditText_chat_Content.append("\r\n");
			}
			mEditText_chat_Content.setSelection(mEditText_chat_Content.length());

		}

		/**
		 * 识别回调错误.
		 */
		public void onError(SpeechError error) {
			showTip(error.getPlainDescription(true));
		}

	};


	public void setParam() {
		// 清空参数
		mIat.setParameter(SpeechConstant.PARAMS, null);
		String lag = mSharedPreferences.getString("iat_language_preference", "mandarin");
		// 设置引擎
		mIat.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
		if (lag.equals("en_us")) {
			// 设置语言
			mIat.setParameter(SpeechConstant.LANGUAGE, "en_us");
		} else {
			// 设置语言
			mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
			// 设置语言区域
			mIat.setParameter(SpeechConstant.ACCENT, lag);
		}
		// 设置语音前端点
		mIat.setParameter(SpeechConstant.VAD_BOS, mSharedPreferences.getString("iat_vadbos_preference", "4000"));
		// 设置语音后端点
		mIat.setParameter(SpeechConstant.VAD_EOS, mSharedPreferences.getString("iat_vadeos_preference", "1000"));
		// 设置标点符号
		mIat.setParameter(SpeechConstant.ASR_PTT, mSharedPreferences.getString("iat_punc_preference", "1"));
		// 设置音频保存路径
		mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH,
				Environment.getExternalStorageDirectory() + "/iflytek/wavaudio.pcm");

	}

	/**
	 * 参数设置
	 * 
	 * @param param
	 * @return
	 */
	private void setParam1() {
		// 清空参数
		mTts.setParameter(SpeechConstant.PARAMS, null);
		// 设置合成
		if (mEngineType.equals(SpeechConstant.TYPE_CLOUD)) {
			// 设置使用云端引擎
			mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
			// 设置发音人，voicerCloud
			mTts.setParameter(SpeechConstant.VOICE_NAME, voicerCloud);
		}

		// 设置语速
		mTts.setParameter(SpeechConstant.SPEED, nSharedPreferences.getString("speed_preference", "50"));

		// 设置音调
		mTts.setParameter(SpeechConstant.PITCH, nSharedPreferences.getString("pitch_preference", "50"));

		// 设置音量
		mTts.setParameter(SpeechConstant.VOLUME, nSharedPreferences.getString("volume_preference", "50"));

		// 设置播放器音频流类型
		mTts.setParameter(SpeechConstant.STREAM_TYPE, nSharedPreferences.getString("stream_preference", "3"));
	}

	private void showTip(final String str) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mToast.setText(str);
				mToast.show();
			}
		});
	}

	private void showTip1(final String str) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				nToast.setText(str);
				nToast.show();
			}
		});
	}

	// 当前位置定位
	private void centerLoc() {
		if (mCurrentLat == 0.0f || mCurrentLon == 0.0) {// 没有获取到定位返回
			return;
		}
		LatLng ll = new LatLng(mCurrentLat, mCurrentLon);
		MapStatus.Builder builder = new MapStatus.Builder();
		builder.target(ll).zoom(18.0f);
		mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
	}

	private void initReceiver() {
		// 注册接收 后台车群组聊天消息广播
		msgReceiver = new MsgReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("com.Li.ServiceClient.sendChat");
		registerReceiver(msgReceiver, intentFilter);

		// 注册 车群组创建消息广播
		groupCreatReceiver = new GroupCreatReceiver();
		IntentFilter intentFilter1 = new IntentFilter();
		intentFilter1.addAction("com.Li.ServiceClient.sendGroupSuccess");
		registerReceiver(groupCreatReceiver, intentFilter1);

		// 删除 车群组 消息广播
		groupDeleteReceiver = new GroupDeleteReceiver();
		IntentFilter intentFilter2 = new IntentFilter();
		intentFilter2.addAction("com.Li.ServiceClient.sendDeleteSuccess");
		registerReceiver(groupDeleteReceiver, intentFilter2);

		// 接收车群组邀约 同意 或 取消 消息广播
		addCarRegister = new AddCarRegister();
		IntentFilter intentFilter3 = new IntentFilter();
		intentFilter3.addAction("com.Li.ServiceClient.sendAddCarRegister");
		registerReceiver(addCarRegister, intentFilter3);

		// 接收 车队成员 消息广播
		memberUpdateReceiver = new MemberUpdateReceiver();
		IntentFilter intentFilter4 = new IntentFilter();
		intentFilter4.addAction("com.Li.ServiceClient.sendMemberUpdate");
		registerReceiver(memberUpdateReceiver, intentFilter4);

		// 接收 车群组邀约请求信息是否发送成功 消息广播
		inviteAskRegister = new InviteAskRegister();
		IntentFilter intentFilter5 = new IntentFilter();
		intentFilter5.addAction("com.Li.ServiceClient.sendInviteAsk");
		registerReceiver(inviteAskRegister, intentFilter5);

		// 接收 被邀请车辆是否同意被邀请 消息广播
		askAnswerRegister = new AskAnswerRegister();
		IntentFilter intentFilter6 = new IntentFilter();
		intentFilter6.addAction("com.Li.ServiceClient.sendAskAnswer");
		registerReceiver(askAnswerRegister, intentFilter6);

		// 接收 后台推送车队公告 消息广播
		announcementRegister = new AnnouncementRegister();
		IntentFilter intentFilter7 = new IntentFilter();
		intentFilter7.addAction("com.Li.ServiceClient.sendAnnouncement");
		registerReceiver(announcementRegister, intentFilter7);
		
		// 接收 后台发送的已经存在的车群组 消息广播
		groupExistRegister = new GroupExistRegister();
		IntentFilter intentFilter8 = new IntentFilter();
		intentFilter8.addAction("com.Li.ServiceClient.GroupExistRegister");
		registerReceiver(groupExistRegister, intentFilter8);
		
		//mk5删除车辆的广播
		deleteIdReceiver = new DeleteIdReceiver();
		IntentFilter intentFilter9 = new IntentFilter();
		intentFilter9.addAction("com.liu.client.deleteid");
		registerReceiver(deleteIdReceiver, intentFilter9);
		
		
		//mk5编队车队列表
	    motorcadeListReceiver = new MotorcadeListReceiver();
		IntentFilter intentFilter10 = new IntentFilter();
		intentFilter10.addAction("com.liu.Client.sendMotorcadeList");
		registerReceiver(motorcadeListReceiver, intentFilter10);
		
		//编队  其他车的加队请求
		joinRequestReceiver = new JoinRequestReceiver();
		IntentFilter intentFilter11 = new IntentFilter();
		intentFilter11.addAction("com.liu.Client.sendJoinRequest");
		registerReceiver(joinRequestReceiver, intentFilter11);
		
		//入队请求后 头车对入队请求车辆的回复
		joinResponseReceiver = new JoinResponseReceiver();
		IntentFilter intentFilter12 = new IntentFilter();
		intentFilter12.addAction("com.liu.Client.sendJoinResponse");
		registerReceiver(joinResponseReceiver, intentFilter12);
		
		//入队请求后  入队请求车辆进行入队的消息广播
		changeHeadingReceiver = new ChangeHeadingReceiver();
		IntentFilter intentFilter13 = new IntentFilter();
		intentFilter13.addAction("com.liu.Client.sendChangeHeading");
		registerReceiver(changeHeadingReceiver, intentFilter13);
		
		//入队请求同意后，车队中其他车辆的调整
		otherAdjustReceiver = new OtherAdjustReceiver();
		IntentFilter intentFilter14 = new IntentFilter();
		intentFilter14.addAction("com.liu.Client.sendOtherAdjust");
		registerReceiver(otherAdjustReceiver, intentFilter14);
		
		//入队请求同意后 入队请求车辆完成入队的消息广播
		joinFinishReceiver = new JoinFinishReceiver();
		IntentFilter intentFilter15 = new IntentFilter();
		intentFilter15.addAction("com.liu.Client.sendJoinFinish");
		registerReceiver(joinFinishReceiver, intentFilter15);
		
		//头车解散车队消息广播
		dismissFormationReceiver = new DismissFormationReceiver();
		IntentFilter intentFilter16 = new IntentFilter();
		intentFilter16.addAction("com.liu.Client.sendDismissFormation");
		registerReceiver(dismissFormationReceiver, intentFilter16);
		
		//头车解散车队成功后，通知车队其他车辆车队已经解散了的消息广播
		dismissedFormationReceiver = new DismissedFormationReceiver();
		IntentFilter intentFilter17 = new IntentFilter();
		intentFilter17.addAction("com.liu.Client.sendDismissedFormation");
		registerReceiver(dismissedFormationReceiver, intentFilter17);
		
		//头车允许请求离队的车离队
		leaveFormationReceiver = new LeaveFormationReceiver();
		IntentFilter intentFilter18 = new IntentFilter();
		intentFilter18.addAction("com.liu.Client.sendLeaveFormation");
		registerReceiver(leaveFormationReceiver, intentFilter18);
		
		//把车队内有车离队的消息发送给车队内其他车辆
		otherLeaveFormationReceiver = new OtherLeaveFormationReceiver();
		IntentFilter intentFilter19 = new IntentFilter();
		intentFilter19.addAction("com.liu.Client.sendOtherLeaveFormation");
		registerReceiver(otherLeaveFormationReceiver, intentFilter19);
		
		//发送给车队内其他车 车队内要离队的车 离队成功
		otherLeaveSuccessReceiver = new OtherLeaveSuccessReceiver();
		IntentFilter intentFilter20 = new IntentFilter();
		intentFilter20.addAction("com.liu.Client.sendOtherLeaveSuccess");
		registerReceiver(otherLeaveSuccessReceiver, intentFilter20);
		
		//发送给离队车辆本身 离队车辆离队是否成功
		leaveMsgReceiver = new LeaveMsgReceiver();
		IntentFilter intentFilter21 = new IntentFilter();
		intentFilter21.addAction("com.liu.Client.sendLeaveMsg");
		registerReceiver(leaveMsgReceiver, intentFilter21);
	}

	// 车群组创建按钮监听点击方法
	public void onCreakBtnClick() {
		create_cargroup_fragmentRelative.setVisibility(View.GONE);
		car_group_id_random = String.valueOf(new Random().nextInt(99999));
		if (Util.isNetworkAvailable(OverlayDemo.this) && MyApplication.isServerConnect) {

			// 这里将群组创建中的 信息发送到 后台
			try {
				mJson_team_creation.put("datatype", "TEAM_REGISTER");
				mJson_team_creation.put("fromid", MyApplication.user_name);
				mJson_team_creation.put("fromtype", "veh");
				mJson_team_creation.put("team_id", car_group_id_random);
				mJson_team_creation.put("team_name", Group.editName1.getText().toString());
				mJson_team_creation.put("team_description", Group.editIntro1.getText().toString());
				mJson_team_creation.put("team_start", Group.editStart1.getText().toString());
				mJson_team_creation.put("team_end", Group.editEnd1.getText().toString());
				mJson_team_creation.put("team_veh_maxnumber", "10");
				mJson_team_creation.put("team_veh_number", "1");
				 	
				Util.send_To_Clound(mJson_team_creation);
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// 测试 ： 没网 的时候用
		if (!MyApplication.isNetAvailable) {
			map.setPadding(0, 0, 600, 0);
			cargroup_chat_Relative.setVisibility(View.VISIBLE);
		}
	}

	public void onBackBtnClick() {
		create_cargroup_fragmentRelative.setVisibility(View.GONE);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// TODO Auto-generated method stub
		car_group_id_random = ServiceClient.carGroup_Exist.get(position+1).team_id;
		MyApplication.editName = ServiceClient.carGroup_Exist.get(position+1).team_name;
		groupExist_relativelayout.setVisibility(View.GONE);
		ToastUtil.makeText(this, "已进入"+ ServiceClient.carGroup_Exist.get(position+1).team_name +"车队");
		
		if (Util.isNetworkAvailable(this) && MyApplication.isServerConnect){
			try{
				mJson_team.put("datatype", "MEMBER_LOCATION_DATA");
				mJson_team.put("fromtype", "veh");
				mJson_team.put("fromid", MyApplication.user_name);
				mJson_team.put("team_id", car_group_id_random);
				
				Util.send_To_Clound(mJson_team);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		//自定义 进入车队的图标
		mCurrentMarker = BitmapDescriptorFactory
				.fromResource(R.drawable.icon_marker_1);
		mBaiduMap
				.setMyLocationConfigeration(new MyLocationConfiguration(
						mCurrentMode, true, mCurrentMarker));
		
		// 给车队成员车辆添加信息显示
//		Button button = new Button(getApplicationContext());
//        button.setBackgroundResource(R.drawable.popup);
//		button.setText(MyApplication.user_name);
//        button.setTextColor(Color.BLACK);
//       
//        LatLng ll = marker.getPosition();
//        mInfoWindow = new InfoWindow(button, ll, -47);
//        mBaiduMap.showInfoWindow(mInfoWindow);
	}        

	public void onClick(View v) {
		switch (v.getId()) {
		// 查询已经存在的车群组
		case R.id.exist_group:
			if (Util.isNetworkAvailable(this) && MyApplication.isServerConnect){
				try{
					mJson_team_update.put("datatype", "TEAM_UPDATE");
					mJson_team_update.put("fromtype", "veh");
					mJson_team_update.put("fromid", MyApplication.user_name);
					
					Util.send_To_Clound(mJson_team_update);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				groupExist_relativelayout.setVisibility(View.VISIBLE);
			}
			break;
		// 隐藏车群组显示列表	
		case R.id.group_exist_back_btn:
			groupExist_relativelayout.setVisibility(View.GONE);
			break;
			
		// 点击创建群组
		case R.id.group:
			create_cargroup_fragmentRelative.setVisibility(View.VISIBLE);
			break;
		// 点击解散按钮
		case R.id.delete_group:
			if (Util.isNetworkAvailable(this) && MyApplication.isServerConnect) {
				try {
					mJson_team_dismiss.put("datatype", "TEAM_DISMISS");
					mJson_team_dismiss.put("team_id", car_group_id_random);
					mJson_team_dismiss.put("fromtype", "veh");
					mJson_team_dismiss.put("fromid", MyApplication.user_name);
					
					Util.send_To_Clound(mJson_team_dismiss);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				// 测试 显示车群组界面
				map.setPadding(0, 0, 0, 0);
				cargroup_chat_Relative.setVisibility(View.GONE);
				groupMember_relativelayout.setVisibility(View.GONE);
				groupInvite_linearlayout.setVisibility(View.GONE);
			}
			break;

		// 邀请成员按钮
		case R.id.group_invate:
			// 添加动作
			// 测试 显示车群组界面
			groupInvite_linearlayout.setVisibility(View.VISIBLE);
			break;
		// 邀约界面 返回按钮
		case R.id.group_invite_back_btn:
			groupInvite_linearlayout.setVisibility(View.GONE);
			break;
		// 邀约界面确认按钮
		case R.id.group_invite_ok:
			if (Util.isNetworkAvailable(this) && MyApplication.isServerConnect) {

				try {// TEAM_INVITE
					mJson_team_invite.put("datatype", "TEAM_INVITE");
					// 车牌号(被邀请车牌)
					mJson_team_invite.put("fromtype", "veh");
					mJson_team_invite.put("veh_lpn", EditText_invite_group_name.getText().toString());
					// 车主用户名(被邀请车主用户名)
					mJson_team_invite.put("owner_uname", inviteUser.getText().toString());
					// update 2016年10月25日16:06:57 添加fromid 值和veh_id一样
					mJson_team_invite.put("fromid", MyApplication.user_name);
					mJson_team_invite.put("team_id", car_group_id_random);
					
					// LJL updata 2016年12月14日11:18:57
					mJson_team_invite.put("team_name", MyApplication.editName);

					Util.send_To_Clound(mJson_team_invite);
					
					// 邀请框信息清零
					EditText_invite_group_name.setText("");
					inviteUser.setText("");

				} catch (JSONException e) {
					e.printStackTrace();
				}
				groupInvite_linearlayout.setVisibility(View.GONE);
			}
			break;
		// 群组成员按钮，点击后 能显示群组内所有成员
		case R.id.group_member_name_list:

			if (Util.isNetworkAvailable(this) && MyApplication.isServerConnect) {

				try {// MEMBER_UPDATE
					mJson_member_update.put("datatype", "MEMBER_UPDATE");
					mJson_member_update.put("fromid", MyApplication.user_name);
					mJson_member_update.put("fromtype", "veh");
					mJson_member_update.put("team_id", car_group_id_random);
					mJson_member_update.put("team_name", MyApplication.editName);

					Util.send_To_Clound(mJson_member_update);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				groupMember_relativelayout.setVisibility(View.VISIBLE);
			}
			break;

		case R.id.group_member_back_btn:
			groupMember_relativelayout.setVisibility(View.GONE);
			break;
        // 被邀请车辆 点击同意邀请
		case R.id.group_invite_agree:

			if (Util.isNetworkAvailable(this) && MyApplication.isServerConnect) {

				try {// TEAM_RESPONSE
					mJson_team_invite_agree.put("datatype", "TEAM_RESPONSE");
					// 车牌号(被邀请车牌)
					mJson_team_invite_agree.put("fromtype", "veh");
					mJson_team_invite_agree.put("fromid", MyApplication.user_name);
					mJson_team_invite_agree.put("toid", MyApplication.veh_lpn);
					mJson_team_invite_agree.put("response", true);
					mJson_team_invite_agree.put("team_id", MyApplication.teamID);
					mJson_team_invite_agree.put("team_name", MyApplication.editName);

					Util.send_To_Clound(mJson_team_invite_agree);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				groupInviteApply.setVisibility(View.GONE);
				
				btn_delGroup.setClickable(true);
				btn_groupInvate.setClickable(true);
				btn_groupMember_name.setClickable(true);
				
				//自定义 进入车队的图标
//				mCurrentMarker = BitmapDescriptorFactory
//						.fromResource(R.drawable.icon_marker_1);//R.drawable.icon_geo
//				mBaiduMap
//						.setMyLocationConfigeration(new MyLocationConfiguration(
//								mCurrentMode, true, mCurrentMarker));
			}

			break;
	    // 被邀请车辆 点击不同意邀请
		case R.id.group_invite_cancel:
			Log.i("lijialong5", "group_invite_ok2");

			if (Util.isNetworkAvailable(this) && MyApplication.isServerConnect) {

				try {// TEAM_RESPONSE
					mJson_team_invite_disagree.put("datatype", "TEAM_RESPONSE");
					// 车牌号(被邀请车牌)
					mJson_team_invite_disagree.put("fromtype", "veh");
					mJson_team_invite_disagree.put("fromid", MyApplication.user_name);
					mJson_team_invite_disagree.put("toid", MyApplication.veh_lpn);
					mJson_team_invite_disagree.put("response", false);

					mJson_team_invite_disagree.put("veh_lpn", EditText_invite_group_name.getText().toString());
					// 车主用户名(被邀请车主用户名)
					mJson_team_invite_disagree.put("owner_uname", inviteUser.getText().toString());
					// update 2016年10月25日16:06:57 添加fromid 值和veh_id一样
					mJson_team_invite_disagree.put("team_id", car_group_id_random);
					// LJL updata 2016年12月14日11:18:57
					mJson_team_invite_disagree.put("team_name", MyApplication.editName);

					Util.send_To_Clound(mJson_team_invite_disagree);

				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				MyApplication.editName = null;
				MyApplication.veh_lpn = null;
				OverlayDemo.car_group_id_random = null;
				MyApplication.teamID = null;
				
				groupInviteApply.setVisibility(View.GONE);
			}
			break;

		// 车群组聊天按钮
		case R.id.group_send:// 发送按钮点击事件
			send();
			break;
		case R.id.group_soundBtn:
			setParam();
			boolean isShowDialog = mSharedPreferences.getBoolean(getString(R.string.pref_key_iat_show), false);
			if (isShowDialog) {
				// 显示听写对话框
				iatDialog.setListener(recognizerDialogListener);
				iatDialog.show();
				showTip(getString(R.string.text_begin));
			} else {
				// 不显示听写对话框
				ret = mIat.startListening(recognizerListener);
				Log.i("wangyonglong", "" + ret);
				if (ret != ErrorCode.SUCCESS) {
					showTip("听写失败,错误码：" + ret);
				} else {
					showTip(getString(R.string.text_begin));
				}
			}
			break;

		case R.id.group_announcement_back_btn:
			mcar_group_announcement.setVisibility(View.GONE);
			break;
		
		//弹出车辆创建编队页面按钮		
		case R.id.btn_Formation:
			create_formation_layout.setVisibility(View.VISIBLE);
			break;
		//车辆创建编队按钮
		case R.id.formation_creat_btn:
			if (TCPService.Flag_Connection_MK5) {
				try {
					json_CreateFormation.put("msgtype", "creatformation");
					json_CreateFormation.put("formationname", formationName.getText().toString());
					json_CreateFormation.put("formationstart", formationStart.getText().toString());
					json_CreateFormation.put("formationend", formationEnd.getText().toString());
					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Util.send_To_MK5("FORMATION", json_CreateFormation);
				
				create_formation_layout.setVisibility(View.GONE);
			} else {
				Toast.makeText(this, "与MK5未连接，发送失败", Toast.LENGTH_SHORT).show();
			}
			break;
		//车辆创建编队页面撤销按钮	
		case R.id.formation_back_btn:
			create_formation_layout.setVisibility(View.GONE);
			break;
			
			
		//查询编队	
        case R.id.btn_QueryFormation:
			if (TCPService.Flag_Connection_MK5) {
				try {
					json_QueryFormation.put("msgtype", "queryformation");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Util.send_To_MK5("FORMATION", json_QueryFormation);
				Log.i("liuhongdong", "查询编队信息发送成功！");
			} else {
				Toast.makeText(this, "与MK5未连接，发送失败", Toast.LENGTH_SHORT).show();
			}
			break;
			
		// 加入车队
//        case R.id.join_formation:
//        	if (TCPService.Flag_Connection_MK5) {
//        		try {
//					json_JoinFormation.put("msgtype", "joinformation");
//					json_JoinFormation.put("formationid", Client.motorcade_list.get(0).getFormationid());
//				} catch (JSONException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//        		Util.send_To_MK5("FORMATION", json_JoinFormation);
//        		motorcade_list_layout.setVisibility(View.GONE);
//        		Client.motorcade_list.clear();
//        	} else {
//				Toast.makeText(this, "与MK5未连接，发送失败", Toast.LENGTH_SHORT).show();
//			}
//        	
//			break;
		}
		

	}

	/**
	 * 发送消息
	 */
	private void send() {
		String contString = mEditText_chat_Content.getText().toString();
		if (contString.length() <= 0) {
			return;
		}

		mEditText_chat_Content.setText("");// 清空编辑框数据
		try {
			mJsonSend.put("datatype", "MEMBER_CHAT");
			mJsonSend.put("fromtype", "veh");
			mJsonSend.put("fromid", MyApplication.user_name);
			mJsonSend.put("team_id", car_group_id_random);
			mJsonSend.put("chatContent", contString);
			ServiceClient ct = ClientManager.getManager().getClient();
			if (ct == null) {
				Toast toast = Toast.makeText(getApplicationContext(), "[ERROR] ：与后台没有连接，发送失败，请确保服务器连接成功后重新发送！",
						Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
				return;
			}
			// 向后台发送 Jason包
			ClientManager.getManager().servicePublish(ct, mJsonSend);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 点击空白处 隐藏软键盘
	public boolean onTouchEvent(MotionEvent event) {
		if (null != this.getCurrentFocus()) {
			/**
			 * 点击空白位置 隐藏软键盘
			 */
			InputMethodManager mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
			return mInputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
		}
		return super.onTouchEvent(event);
	}

	/**
	 * 时间差值：小于30秒 返回true 大于则为false
	 * 
	 * @return
	 */
	public boolean isTimePassed() {
		nowTime = MyDate.getDateEN();
		try {
			MyDate.format1.parse(recordTime);
			long between = (MyDate.format1.parse(nowTime).getTime() - MyDate.format1.parse(recordTime).getTime());
			result = between < 1000 * 30;
			System.out.println(result);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//

		return result;
	}

	// 接收后台车群组聊天消息的广播
	public class MsgReceiver extends BroadcastReceiver {

		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String s = intent.getStringExtra("CHAT");
			String from = intent.getStringExtra("FROM");
			String veh_id = new String(MyApplication.user_name);

			mAdapter = new ChatMsgViewAdapter(OverlayDemo.this, mDataArrays);

			if (veh_id.equals(from) == true) {
				ChatMsgEntity entity = new ChatMsgEntity("我", MyDate.getDateEN(), s, false);
				mDataArrays.add(entity);
				mchat_content_ListView.setAdapter(mAdapter);
				mchat_content_ListView.setSelection(mAdapter.getCount() - 1);
			} else {
				ChatMsgEntity entity = new ChatMsgEntity(from, MyDate.getDateEN(), s, true);
				mDataArrays.add(entity);
				mchat_content_ListView.setAdapter(mAdapter);
				mchat_content_ListView.setSelection(mAdapter.getCount() - 1);
			}

			setParam1();
			int code = mTts.startSpeaking(s, mTtsListener); // 文字转语音 wkl
			if (code != ErrorCode.SUCCESS) {
				showTip1("语音合成失败,错误码: " + code);
			}
		}
	}

	// 接收后台车群组注册消息的广播
	public class GroupCreatReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// 测试 显示车群组界面
			ToastUtil.makeText(OverlayDemo.this, "车群组注册成功");
			//给车队相关的按钮使能
			btn_delGroup.setClickable(true);
			btn_groupInvate.setClickable(true);
			btn_groupMember_name.setClickable(true);

			//给车队信息变量赋值
			MyApplication.editName = Group.editName1.getText().toString();
			MyApplication.editIntro = Group.editIntro1.getText().toString();
			MyApplication.editStart = Group.editStart1.getText().toString();
			MyApplication.editEnd = Group.editEnd1.getText().toString();
			MyApplication.editNote = Group.editNote1.getText().toString();
			// 把创建车队表的输入信息清零
			Group.editName1.setText(""); 
			Group.editIntro1.setText("");
			Group.editStart1.setText("");
			Group.editEnd1.setText("");
			Group.editNote1.setText("");
			
			//自定义 进入车队的图标
			mCurrentMarker = BitmapDescriptorFactory
					.fromResource(R.drawable.icon_marker_1);//R.drawable.icon_geo
			
			mBaiduMap
					.setMyLocationConfigeration(new MyLocationConfiguration(
							mCurrentMode, true, mCurrentMarker));

		}
	}

	// 接收解散车群组消息的广播
	public class GroupDeleteReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			
			MyApplication.team_dismiss = true;
			// 停止跟车
			if (mFollow_Thread != null) {
				mBaiduMap.clear();
				mFollow_Thread.exit = true;
				mFollow_Thread = null;
			}
			mBaiduMap.clear();
			
			btn_delGroup.setClickable(false);
			btn_groupInvate.setClickable(false);
			btn_groupMember_name.setClickable(false);
			
			ToastUtil.makeText(OverlayDemo.this, "车队"+MyApplication.editName+"解散成功");
			
			MyApplication.editName = null;//team_name
			OverlayDemo.car_group_id_random = null; //team_id
			
			//解散车队成功后 设置车辆默认图标
			mCurrentMarker = null;
			mBaiduMap
					.setMyLocationConfigeration(new MyLocationConfiguration(
							mCurrentMode, true, null));
			
			mBaiduMap.clear();
			
		}
	}

	// 接收后台邀约申请消息的广播
	public class AddCarRegister extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String start = intent.getStringExtra("team_start");
			String end = intent.getStringExtra("team_end");
			
			group__invite_apply_name.setText(MyApplication.editName);
			group_apply_start.setText(start);
			group_apply_end.setText(end);
			groupInviteApply.setVisibility(View.VISIBLE);
		}
	}

	// 接收后台 车队成员 消息的广播
	public class MemberUpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				JSONObject connetJSon = new JSONObject(MyApplication.member_update);
				List<Map<String, String>> getData_update = new ArrayList<Map<String, String>>();
				for (int i = 1; i < connetJSon.length() - 1; i++) {
					Map<String, String> mapGroud_update = new HashMap<String, String>();
					mapGroud_update.put("veh_lpn", connetJSon.getString("veh" + i + "_lpn"));
					getData_update.add(mapGroud_update);
				}

				mAdapterGroup = new SimpleAdapter(OverlayDemo.this, getData_update, R.layout.name01,
						new String[] { "veh_lpn" }, new int[] { R.id.text_mame });

				mListView_group_member.setAdapter(mAdapterGroup);

				MyApplication.member_update = null;

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	// 接收 车群组邀约请求信息是否发送成功 消息广播
	public class InviteAskRegister extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (MyApplication.AskRequest) {
				ToastUtil.makeText(OverlayDemo.this, "邀请请求发送成功");
			} else {
				ToastUtil.makeText(OverlayDemo.this, "邀请请求发送失败");
			}
		}
	}

	// 接收 被邀请车辆是否同意被邀请 消息广播
	public class AskAnswerRegister extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Boolean isAskSuccess = intent.getBooleanExtra("isAskSuccess", false);
			String AskReturnContent = intent.getStringExtra("AskReturnContent");
			if (isAskSuccess) {
				ToastUtil.makeText(OverlayDemo.this, AskReturnContent);
				// 被邀请车辆同意后给后台发送定位请求
				
				if(Util.isNetworkAvailable(OverlayDemo.this) && MyApplication.isServerConnect){
					try{
						mJson_MemberAgree_cloud.put("datatype", "MEMBER_LOCATION_DATA");
						mJson_MemberAgree_cloud.put("fromtype", "veh");
						mJson_MemberAgree_cloud.put("fromid", MyApplication.user_name);
						mJson_MemberAgree_cloud.put("team_id", car_group_id_random);
						
						Util.send_To_Clound(mJson_MemberAgree_cloud);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				
				
				//自定义 进入车队的图标
				mCurrentMarker = BitmapDescriptorFactory
						.fromResource(R.drawable.icon_marker_1);//R.drawable.icon_geo
				mBaiduMap
						.setMyLocationConfigeration(new MyLocationConfiguration(
								mCurrentMode, true, mCurrentMarker));
			} else {
				ToastUtil.makeText(OverlayDemo.this, AskReturnContent);
			}
			
		}
	}

	// 接收 后台推送的车队公告 消息广播
	public class AnnouncementRegister extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String announcement = intent.getStringExtra("Announcement");
			mcar_group_announcement.setVisibility(View.VISIBLE);
			mannouncement_content_TextView.setText(announcement);
		}
	}
	
	// 接收 后台发送的已经存在的车群组的 消息广播
	public class GroupExistRegister extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			List<Map<String, String>> getCarGroup = new ArrayList<Map<String, String>>();
			for (int i = 1; i <= ServiceClient.carGroup_Exist.size(); i++) {
				Map<String, String> mapCarGroup = new HashMap<String, String>();
				mapCarGroup.put("team_name", ServiceClient.carGroup_Exist.get(i).team_name.toString());
				getCarGroup.add(mapCarGroup);
			}

			mAdapterCarGroup = new SimpleAdapter(OverlayDemo.this, getCarGroup, R.layout.cargroup_name,
					new String[] { "team_name" }, new int[] { R.id.text_mame });

			mListView_cargroup.setAdapter(mAdapterCarGroup);

			btn_delGroup.setClickable(true);
			btn_groupInvate.setClickable(true);
			btn_groupMember_name.setClickable(true);
//			btn_groupMember_onmap.setClickable(true);
			
		}
	}
	
	//接收mk5发送要删除的车辆广播
	public class DeleteIdReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			int deleteId = intent.getIntExtra("deleteId", 0);
			Log.i(TAG, "deleteID will--------- delete success----------------");
			if (deleteId != 0) {
				addMarkerFromMk5.get(deleteId).remove();
				addMarkerFromMk5.remove(deleteId);
				Log.i(TAG, "deleteID always delete success----------------");
			}
		}
	}
	
	
	
	//mk5编队车队列表信息
	public class MotorcadeListReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			HashMap<Integer, String> motorcade_list = (HashMap<Integer, String>) intent.getSerializableExtra("list");
			
			int motorcadenum = 0;
			for (Integer i : motorcade_list.keySet()) {
				motorcadenum = i;
			}
			
			try {
				JSONArray jsonArray = new JSONArray(motorcade_list.get(motorcadenum));
				for (int i = 0; i < motorcadenum; i ++) {
					JSONObject formationJson = jsonArray.getJSONObject(i);
					formation formationmsg = new formation();
					formationmsg.setFormationid(formationJson.getString("formationid"));
					formationmsg.setFormationname(formationJson.getString("formationname"));
					formationmsg.setFormationnum(formationJson.getInt("formationnum"));
					formationmsg.setFormaitontype(formationJson.getInt("formationtype"));
					formationmsg.setFormationdestination(formationJson.getString("formationdestination"));
					formationmsg.setFormationstartlocation(formationJson.getString("formationstartlocation"));
					formationmsg.setVehleaderlat(formationJson.getDouble("vehleaderlat"));
					formationmsg.setVehleaderlon(formationJson.getDouble("vehleaderlon"));
					formationmsg.setVehleaderspeed(formationJson.getDouble("vehleaderspeed"));
					motorcade_arraylist.put(i, formationmsg);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			List<Map<String, String>> list_motorcade_data = new ArrayList<Map<String,String>>();
			for (int i = 0; i < motorcade_arraylist.size(); i++) {
				Map<String, String> motorcadeitem = new HashMap<String, String>();
				motorcadeitem.put("motorcadename", motorcade_arraylist.get(i).getFormationname());
				list_motorcade_data.add(motorcadeitem);
			}
			
			SimpleAdapter motorcadelistAdapter = new SimpleAdapter(OverlayDemo.this, list_motorcade_data,
					R.layout.motorcadelist_item, new String[] {"motorcadename"}, new int[] {R.id.motorcade_mame});
			motorcadelist_listview.setAdapter(motorcadelistAdapter);
			motorcade_list_layout.setVisibility(View.VISIBLE);
		}
	}
	
	// 编队  其他车的加队请求接收广播
	public class JoinRequestReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			request requestinfo = (request) intent.getSerializableExtra("joinrequest");
			String vehid = requestinfo.getVehid();
			int quadrant = requestinfo.getQuadrant();
			Double distance = requestinfo.getDistance();
			String quadrantStr = null;
			if (quadrant == 1) {
				quadrantStr = "右前方";
			} else if (quadrant == 2) {
				quadrantStr = "左前方";
			} else if (quadrant == 3) {
				quadrantStr = "左后方";
			} else if (quadrant == 4) {
				quadrantStr = "右后方";
			}
			joinrequest_vehid.setText(vehid);
			joinrequest_quadrant.setText(quadrantStr);
			joinrequest_distance.setText(String.valueOf(df.format(distance))+"米");
		    joinrequestinfo_layout.setVisibility(View.VISIBLE);
		}
	}
	

	
	// 入队请求后 头车对入队请求车辆的回复
	public class JoinResponseReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			Log.i(TAG, "队请求后 头车对入队请求车辆的回复");
			joinresponse joinrs = (joinresponse) intent.getSerializableExtra("joinresponse");
			Boolean status = joinrs.getStatus();
			String join_formationid = joinrs.getFormationid();
			String join_preid = joinrs.getPrevehid();
			String join_behid = joinrs.getBehvehid();
			Boolean join_chghd = joinrs.getChangeheading();
			String statusStr = null, chghdStr = null;
			if (status) {
				statusStr = "同意";
			} else {
				statusStr = "拒绝";
			}
			if (join_chghd) {
				chghdStr = "是";
			} else {
				chghdStr = "否";
			}
			joinresponse_id.setText(join_formationid);
			joinresponse_result.setText(statusStr);
			joinresponse_preid.setText(join_preid);
			joinresponse_behid.setText(join_behid);
			joinresponse_chghd.setText(chghdStr);
			joinresponseinfo_layout.setVisibility(View.VISIBLE);
			
//			if (status) {
//				joinresponseinfo_layout.postDelayed(new Runnable() {
//					
//					@Override
//					public void run() {
//						// TODO Auto-generated method stub
//						joinresponseinfo_layout.setVisibility(View.INVISIBLE);
//					
//					}
//				}, 3000);
//			} else {
//				joinresponseinfo_layout.postDelayed(new Runnable() {
//					
//					@Override
//					public void run() {
//						// TODO Auto-generated method stub
//						joinresponseinfo_layout.setVisibility(View.INVISIBLE);
//					}
//				}, 3000);				
//			}
		    
		}
		
	}
	
	// 入队请求后  入队请求车辆进行入队的消息广播
	public class ChangeHeadingReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
//			if ("INVISIBLE".equals(joinresponseinfo_layout.getVisibility())) {
//				Boolean changeHead = intent.getBooleanExtra("ChangeHead", false);
//				String changeHeadStr = null;
//				if (changeHead) {
//					changeHeadStr = "调头入队";
//				} else {
//					changeHeadStr = "正在入队";
//				}
//				entranceprompt_text.setText(changeHeadStr);
//				entranceprompt_layout.setVisibility(View.VISIBLE);
//				
//			}
			
			
			selfadjust sa = (selfadjust) intent.getSerializableExtra("selfadjust");
			Boolean chghd = sa.getChangehead();
			final String prevehid = sa.getPrevehid();
			String chghdStr = null;
			if (chghd) {
				chghdStr = "是";
			} else {
				chghdStr = "否";
			}
			joinresponse_chghd.setText(chghdStr);
			entryFlag = true;
			new Thread() {
				public void run() {
					while (entryFlag) {
						if (formationMarker.containsKey(prevehid)) {
							formationMarker.get(prevehid).setVisible(false);
						}
						
						try {
							sleep(500);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						if (formationMarker.containsKey(prevehid)) {
							formationMarker.get(prevehid).setVisible(true);
						}
						
						try {
							sleep(500);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				};
			}.start();

		}
		
	}
	
	//入队请求同意后，车队中其他车辆的调整
	public class OtherAdjustReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			joinadjust otheradjust = (joinadjust) intent.getSerializableExtra("joinadjust");
			int positiontype = otheradjust.getPositiontype();
			if (positiontype == 1) {
				MyLocationData mMyLocationData = mBaiduMap.getLocationData();
				double latitude = mMyLocationData.latitude;
				double longitude = mMyLocationData.longitude;
				LatLng ll = new LatLng(latitude, longitude);
				
				Button button = new Button(getApplicationContext());
				button.setBackgroundResource(R.drawable.entry);
				
                InfoWindow mInfoWindow = new InfoWindow(BitmapDescriptorFactory.fromView(button), ll, -70, new OnInfoWindowClickListener() {
					
					@Override
					public void onInfoWindowClick() {
						// TODO Auto-generated method stub
						
					}
				});
                mBaiduMap.showInfoWindow(mInfoWindow);
                 	
			} else {
				Double advspeed = otheradjust.getAdvspeed();
				Double advdistance = otheradjust.getAdvdistance();
				Double curdistance = otheradjust.getCurdistance();
				int joincarnum = otheradjust.getJoincarnum();
				otheradj_advspeed.setText(String.valueOf(df.format(advspeed)));
				otheradj_advdistance.setText(String.valueOf(df.format(advdistance)));
				otheradj_curdistance.setText(String.valueOf(df.format(curdistance)));
				otheradj_joincarnum.setText(String.valueOf(joincarnum));
				entranceprompt_otheradjust.setVisibility(View.VISIBLE);
			}
			
		}
		
	}
	
	//入队请求同意后 入队请求车辆完成入队的消息广播
	public class JoinFinishReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String vehid = intent.getStringExtra("finishVehid");
			
			joinresponseinfo_layout.setVisibility(View.GONE);
			
			entranceprompt_layout.setVisibility(View.GONE);
			entranceprompt_otheradjust.setVisibility(View.GONE);
			mBaiduMap.hideInfoWindow();
			
			if (vehid.equals(MyApplication.localvehid)) {
				finish_vehid.setText("入队完成");
			} else {
				finish_vehid.setText(vehid+"车,"+"入队完成");
			}
			
			entrancefinish_layout.setVisibility(View.VISIBLE);
			
			entrancefinish_layout.postDelayed(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					entrancefinish_layout.setVisibility(View.GONE);
					if (TextUtils.isEmpty(veh_dismiss_btn.getText().toString())) {
						veh_dismiss_btn.setText("离队");
					}
					motorcade_status_layout.setVisibility(View.VISIBLE);
				}
			}, 2000);
			
			entryFlag = false;
			
		}
		
	}
	
	//头车解散车队
	public class DismissFormationReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			motorcade_status_layout.setVisibility(View.GONE);
			dismissInfo.setText("车队解散成功");
			dismissformation_layout.setVisibility(View.VISIBLE);
			dismissformation_layout.postDelayed(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					dismissformation_layout.setVisibility(View.GONE);
					dismissInfo = null;
					mBaiduMap.clear();
					FormationSelfLatLng = null;
					formationMarker.clear();
				}
			}, 3000);
		}
		
	}
	
	// 头车解散车队成功后，通知车队其他车辆车队已经解散了
	private class DismissedFormationReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String dismissId = intent.getStringExtra("dismissedId");
			dismissInfo.setText(dismissId+"车队被解散");
			dismissformation_layout.setVisibility(View.VISIBLE);
			dismissformation_layout.postDelayed(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					dismissformation_layout.setVisibility(View.GONE);
					motorcade_status_layout.setVisibility(View.GONE);
					dismissInfo = null;
					mBaiduMap.clear();
					FormationSelfLatLng = null;
					formationMarker.clear();
				}
			}, 3000);
		}
		
	}
	
	//头车允许请求离队的车离队
	private class LeaveFormationReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			leaveformation_layout.setVisibility(View.VISIBLE);
			leaveformation_layout.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					leaveformation_layout.setVisibility(View.GONE);
					motorcade_status_layout.setVisibility(View.GONE);
					mBaiduMap.clear();
					
					FormationSelfLatLng = null;
				}
			}, 3000);
		}
		
	}
	
	// 把车队内有车离队的消息发送给车队内其他车辆
	private class OtherLeaveFormationReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			
			leave leavemsg = (leave) intent.getSerializableExtra("leave");
			final String leaveId = leavemsg.getLeaveId();
			int leavePosition = leavemsg.getPosition();
			
			if (leavePosition == 1) {
//				final String otherid = leaveId;
				leaveFlag = true;
				
				MyLocationData mMyLocationData = mBaiduMap.getLocationData();
				double latitude = mMyLocationData.latitude;
				double longitude = mMyLocationData.longitude;
				LatLng ll = new LatLng(latitude, longitude);
				
				Button button = new Button(getApplicationContext());
				button.setBackgroundResource(R.drawable.leave);
				
                InfoWindow mInfoWindow = new InfoWindow(BitmapDescriptorFactory.fromView(button), ll, -70, new OnInfoWindowClickListener() {
					
					@Override
					public void onInfoWindowClick() {
						// TODO Auto-generated method stub
						
					}
				});
                mBaiduMap.showInfoWindow(mInfoWindow);
                
				new Thread() {
					public void run() {
						while (leaveFlag) {
							if (formationMarker.containsKey(leaveId)) {
								formationMarker.get(leaveId).setVisible(false);
							}
							
							try {
								sleep(500);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
							if (formationMarker.containsKey(leaveId)) {
								formationMarker.get(leaveId).setVisible(true);
							}
							
							try {
								sleep(500);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					};
				}.start();
			} else {
				leaveVehid.setText(leaveId+"车辆即将离队");
				otherleaveformation_layout.setVisibility(View.VISIBLE);
				otherleaveformation_layout.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						otherleaveformation_layout.setVisibility(View.GONE);
					}
				}, 3000);
			}
		}
		
	}
	
	//车队内要离队的车 离队成功
	private class OtherLeaveSuccessReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			leave leavemsg = (leave) intent.getSerializableExtra("leave");
			String leaveId = leavemsg.getLeaveId();
			int leavePosition = leavemsg.getPosition();
			
			if (leavePosition == 1) {
				leaveFlag = false;
				mBaiduMap.hideInfoWindow();
			}
		}
		
	}
	
	//要离队的本车是否离队成功
	private class LeaveMsgReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			Boolean leave = intent.getBooleanExtra("leave", false);
			if (leave) {
				leaveVehid.setText("车辆离队成功！");
				otherleaveformation_layout.setVisibility(View.VISIBLE);
				otherleaveformation_layout.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						otherleaveformation_layout.setVisibility(View.GONE);
						
						motorcade_status_layout.setVisibility(View.GONE);
						
						mBaiduMap.clear();
						
//						formationMarker.clear();
						
					}
				}, 3000);
			} else {
				leaveVehid.setText("请尽快驶离车队！");
				otherleaveformation_layout.setVisibility(View.VISIBLE);
				otherleaveformation_layout.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						otherleaveformation_layout.setVisibility(View.GONE);
					}
				}, 3000);
			}
		}
		
	}
	
	
	/**
	 * 添加 车队成员车辆的 覆盖物
	 * @throws JSONException 
	 */
	public void addInfosOverlayHashMap(final HashMap<String, String> other_Car_map) {
		  for (String teamId : other_Car_map.keySet()) {
			  
			  try {
				  JSONArray jsonArray = new JSONArray(other_Car_map.get(teamId));
				  for (int i =0; i < jsonArray.length(); i++) {
					  JSONObject cargroupJson = jsonArray.getJSONObject(i);
					  if (!cargroupJson.getString("veh_id").equals(MyApplication.user_name)) {
							
							Log.d(TAG, "car group member location is sava " + cargroupJson.getString("veh_id"));
							vin = cargroupJson.getString("VIN");
							intVin = Integer.valueOf(vin);
							if (!cargroup_member_Location.containsKey(intVin) && (cargroup_member_LocationWithUsername.containsKey(cargroupJson.getString("veh_id")))){
								int hashMapWithVin = cargroup_member_LocationWithUsername.get(cargroupJson.getString("veh_id")).vin;
								cargroup_member_Location.remove(hashMapWithVin);
								cargroup_member_LocationWithUsername.remove(cargroupJson.getString("veh_id"));
								cargroup_member_LocationRemove.add(hashMapWithVin);
								
								Car_Data mCar_Data = new Car_Data();
								mCar_Data.lat_cloud = Double.valueOf(cargroupJson.getString("veh_lat"));
								mCar_Data.longi_cloud = Double.valueOf(cargroupJson.getString("veh_lon"));
								mCar_Data.user_name = cargroupJson.getString("veh_id");
								mCar_Data.vin = intVin;
								cargroup_member_Location.put(intVin, mCar_Data);
								cargroup_member_LocationWithUsername.put(mCar_Data.user_name, mCar_Data);
								
							} else {
								Car_Data mCar_Data = new Car_Data();
								mCar_Data.lat_cloud = Double.valueOf(cargroupJson.getString("veh_lat"));
								mCar_Data.longi_cloud = Double.valueOf(cargroupJson.getString("veh_lon"));
								mCar_Data.user_name = cargroupJson.getString("veh_id");
								mCar_Data.vin = intVin;
								cargroup_member_Location.put(intVin, mCar_Data);
								cargroup_member_LocationWithUsername.put(mCar_Data.user_name, mCar_Data);
							}   
						
						
						for (int vin : cargroup_member_Location.keySet()) {
							Log.d(TAG, "cargroup member location is  " +  cargroup_member_Location.get(vin).lat_cloud   +   cargroup_member_Location.get(vin).longi_cloud);
						}
					 }
				  }

			  } catch (JSONException e1) {
				  // TODO Auto-generated catch block
			      e1.printStackTrace();
		      }
			  
		  }
		  
			
			if (!cargroup_member_LocationRemove.isEmpty()) {
				for (Integer key : cargroup_member_LocationRemove) {
					if (carGroupMarkerFromCloud.containsKey(key)) {
						carGroupMarkerFromCloud.get(key).remove();
						carGroupMarkerFromCloud.remove(key);
					}
				}
			}
			
			new Thread() {// 一进来就开启新进程，因为网络读取图片不能再主线程中操作
			private BitmapDescriptor mIconMaker;
			private InfoWindow mInfoWindow;
			View view = LayoutInflater.from(OverlayDemo.this).inflate(R.layout.marker_layout, null);
			TextView tv_num_price=(TextView) view.findViewById(R.id.tv_num_price);
		
			public void run() {
				OverlayOptions overlayOptions = null;
				Marker marker = null;
		
				for (Integer key : cargroup_member_Location.keySet()) {
					Car_Data mCar_Data = cargroup_member_Location.get(key);
					double longi = mCar_Data.longi_cloud;
					double lat = mCar_Data.lat_cloud;
					String user_name = mCar_Data.user_name;
					// 转换成百度地图需要的经纬度
					LatLng sourceLatLng = new LatLng((double) lat, (double) longi);// 纬度，经度
					
					if (!carGroupMarkerFromCloud.containsKey(key)) {
						
						tv_num_price.setText(user_name);
						BitmapDescriptor free_view = BitmapDescriptorFactory.fromView(view);
						
						overlayOptions = new MarkerOptions().position(sourceLatLng).icon(free_view).zIndex(10);
						marker = (Marker) (mBaiduMap.addOverlay(overlayOptions));
						// 额外的信息需要用Bundle存储该marker点的Info数据源
						// 存入的信息放入键值对中，每次都NEW一个Bundle对象
						// 取出时只需要用marker.getExtraInfo().get("info");
						Bundle bundle = new Bundle();// bundel类似于session
						bundle.putSerializable("Car_info", mCar_Data);
						marker.setExtraInfo(bundle);// 把INFO信息存入marker点中
						
						carGroupMarkerFromCloud.put(key, marker);
					} else {
						carGroupMarkerFromCloud.get(key).setPosition(sourceLatLng);
					}
					
				}
				
			}
		
		}.start();
	}
	
	
	
	

	/**
	 * 清除所有Overlay
	 * 
	 * @param view
	 */
	public void clearOverlay(View view) {
		mBaiduMap.clear();
	}

	@Override
	protected void onPause() {
		mMapView.onPause();

		super.onPause();

	}

	@Override
	protected void onResume() {

		mMapView.onResume();

		super.onResume();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mFollow_Thread.exit = true;
	}

	public class Follow_Thread extends Thread {
		public volatile boolean exit = false;
		private Car_Data mCar_Data, Car_data_cloud;
		private double longi, lat;
		private String user_name;

		public Follow_Thread(Car_Data mCar_Data) {
			this.mCar_Data = mCar_Data;
			this.user_name = mCar_Data.user_name;
		}

		public void run() {

			// double longi = mCar_Data.longi_cloud;
			// double lat = mCar_Data.lat_cloud;
			// // LJL 转换成百度地图需要的经纬度
			// LatLng targetLatLng = new LatLng(lat ,longi );// 纬度，经度
			// // 设置起终点信息，对于tranist search 来说，城市名无意义
			// PlanNode stNode = PlanNode.withLocation(new LatLng(29.53859,
			// 106.614679));//重邮29.53859, 106.614679 ((double)
			// (Client.LatLocalNumber))/ Scal_to_Covert,((double)
			// (Client.LongLocalNumber))/ Scal_to_Covert)
			// PlanNode enNode = PlanNode.withLocation(targetLatLng);
			while (!exit) {
				
				for (Integer key : ServiceClient.cargroup_member_Location.keySet()) {
					Car_data_cloud = ServiceClient.cargroup_member_Location.get(key);
					if (Car_data_cloud.user_name.equals(user_name)){
						longi = Car_data_cloud.longi_cloud;
						lat = Car_data_cloud.lat_cloud;
					}
				}
			    
//				double longi = mCar_Data.longi_cloud;
//				double lat = mCar_Data.lat_cloud;
				// LJL 转换成百度地图需要的经纬度
				LatLng targetLatLng = new LatLng(lat, longi);// 纬度，经度
				LatLng startLatLng = new LatLng(mCurrentLat, mCurrentLon);// 纬度，经度
				// 设置起终点信息，对于tranist search 来说，城市名无意义
				PlanNode stNode = PlanNode.withLocation(startLatLng);// 重邮29.53859,
																		// 106.614679
																		// ((double)
																		// (Client.LatLocalNumber))/
																		// Scal_to_Covert,((double)
																		// (Client.LongLocalNumber))/
																		// Scal_to_Covert)
				PlanNode enNode = PlanNode.withLocation(targetLatLng);

				mSearch.drivingSearch((new DrivingRoutePlanOption()).from(stNode).to(enNode));
				targetLatLng = null;
				startLatLng = null;
				stNode = null;
				enNode = null;
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void onGetDrivingRouteResult(DrivingRouteResult result) {
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			ToastUtil.makeText(OverlayDemo.this, "抱歉，未找到结果");
		}
		if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
			// 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
			ToastUtil.makeText(OverlayDemo.this, "抱歉，地址有歧义");
			return;
		}
		if (result.error == SearchResult.ERRORNO.NO_ERROR) {
			route = result.getRouteLines().get(0);
			DrivingRouteOverlay overlay = new DrivingRouteOverlay(mBaiduMap);
			routeOverlay = overlay;
			mBaiduMap.setOnMarkerClickListener(overlay);
			overlay.setData(result.getRouteLines().get(0));
			overlay.addToMap();
			overlay.setFocus(true);
		}

	}

	@Override
	public void onGetTransitRouteResult(TransitRouteResult arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onGetWalkingRouteResult(WalkingRouteResult arg0) {
		// TODO Auto-generated method stub

	}

	
}
