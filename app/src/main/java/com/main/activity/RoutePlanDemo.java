package com.main.activity;

import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.yanzi.shareserver.Client;
import org.yanzi.shareserver.Car_Data;

import Utili.Package.ToastUtil;
import Utili.Package.Util;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;

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
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.navi.BaiduMapAppNotSupportNaviException;
import com.baidu.mapapi.navi.BaiduMapNavigation;
import com.baidu.mapapi.navi.NaviParaOption;
import com.baidu.mapapi.overlayutil.DrivingRouteOverlay;
import com.baidu.mapapi.overlayutil.OverlayManager;
import com.baidu.mapapi.overlayutil.TransitRouteOverlay;
import com.baidu.mapapi.overlayutil.WalkingRouteOverlay;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteLine;
import com.baidu.mapapi.search.route.TransitRoutePlanOption;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteLine;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.baidu.mapapi.utils.OpenClientUtil;
import com.main.activity.R;
import com.main.activity.LocationDemo.MyLocationListenner;

/**
 * 此demo用来展示如何进行驾车、步行、公交路线搜索并在地图使用RouteOverlay、TransitOverlay绘制
 * 同时展示如何进行节点浏览并弹出泡泡
 */
public class RoutePlanDemo extends Activity implements OnGetRoutePlanResultListener {
	private static final String TAG = "RoutePlanDemo";
	protected static final int Scal_to_Covert = 100000;
	// 浏览路线节点相关
	Button mBtnPre = null;// 上一个节点
	Button mBtnNext = null;// 下一个节点
	int nodeIndex = -1;// 节点索引,供浏览节点时使用
	RouteLine route = null;
	OverlayManager routeOverlay = null;
	boolean useDefaultIcon = false;
	private TextView popupText = null;// 泡泡view

	// 地图相关，使用继承MapView的MyRouteMapView目的是重写touch事件实现泡泡处理
	// 如果不处理touch事件，则无需继承，直接使用MapView即可
	MapView mMapView = null; // 地图View
	BaiduMap mBaidumap = null;
	// 搜索相关
	RoutePlanSearch mSearch = null; // 搜索模块，也可去掉地图模块独立使用
	private RelativeLayout mMarkerInfoLy;
	private RelativeLayout inner;
	private Button triger, close;
	// 定位相关
	LocationClient mLocClient;
	public MyLocationListenner myListener = new MyLocationListenner();
	private LocationMode mCurrentMode;
	BitmapDescriptor mCurrentMarker;

	// UI相关
	OnCheckedChangeListener radioButtonListener;
	Button requestLocButton;
	boolean isFirstLoc = true;// 是否首次定位
	public LatLng my_BD_APILocation;// 实时更新我的位置经纬度
	private TextView handtv;
	private Handler myHandler;
	private ImageView Car_warn_imv;
	private Button map_clean;
	private Button navi;
	protected LatLng my_mk5_LatLong;// 本车的位置
	protected Car_Data car_info;
	private ImageView warn_level_iv;
	private textThread Genchi_Thread;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_routeplan);
		CharSequence titleLable = "路线规划功能";
		setTitle(titleLable);
		FindViewById();
		Map_Clean();

		// 地图点击事件处理
		// 初始化搜索模块，注册事件监听
		mSearch = RoutePlanSearch.newInstance();
		mSearch.setOnGetRoutePlanResultListener(this);

		// Test_Car();//离线调试加的车，与MK5调时，要屏这句

		Util.IninMapLocation(mBaidumap, 24);// 确定地图开始在哪里，缩放等级，等其它信息
		// setStaticDataToInfo();//把停车场显示到地图上，离线的形式
		// setStaticDataToInfoHashMap();//把停车场显示到地图上，离线的形式
		initMarkerClickEvent();// 点击Marker点
		initMapClickEvent();// 点击地图事件
		UI_handler();
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					if (!Client.Other_Car_map.isEmpty()) {
						 Log.i(TAG, "myHandler01-------------");
						myHandler.sendEmptyMessage(0x001);
					}
					if (Client.Other_Car_map.isEmpty()) {
						 Log.i(TAG, "myHandler02------------------");
						myHandler.sendEmptyMessage(0x002);
					}
					if (MyApplication.Lat_From_MK5 > 0) {
						 Log.i(TAG, "myHandler04----------------------------");
						myHandler.sendEmptyMessage(0x004);// 显示MK5发来的本车图标
					}

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

	private void Test_Car() {
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
					other_car_data1.longi = 10655251;
					other_car_data1.lat = 2955527;
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

	private void Map_Clean() {
		map_clean.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mBaidumap.clear();
				if (Genchi_Thread != null) {
					Genchi_Thread.flag = false;
				}
				navi.setEnabled(true);
			}
		});

	}

	private void UI_handler() {
		myHandler = new Handler() {
			public void handleMessage(Message msg) {
				if (msg.what == 0x001) {
					mBaidumap.clear();// 为了防止MK5发来的GPS有细小改变，每次更新UI时，先把地图清一下
					setStaticDataToInfoHashMap();// 把停车场显示到地图上，离线的形式
				}
				if (msg.what == 0x002) {
					mBaidumap.clear();
				}
				if (msg.what == 0x003) {// 为了与杨允新调加的
					Car_Data car_info = new Car_Data();
					car_info.longi = 10661105;
					car_info.lat = 2954309;
					car_info.Mode = 1;
					car_info.Level = 2;

					mMarkerInfoLy.setVisibility(View.VISIBLE);
					popupInfo_Car_Info(mMarkerInfoLy, car_info);// 把存在marker点中的信息存入布局框架中
					map_clean.setVisibility(View.INVISIBLE);
					navi.setVisibility(View.INVISIBLE);
					IninMapLocation(my_mk5_LatLong,30);
				}
				if (msg.what == 0x004) {// 显示本车

					Display_MyLoc_Marker();
				}
				// if (msg.what == 0x005) {// 跟驰
				// Set_Collision_UI();
				// Car_warn_imv.setImageResource(R.drawable.carfollow);
				// }if (msg.what == 0x006) {// 超车
				// Set_Collision_UI();
				// Car_warn_imv.setImageResource(R.drawable.overpass);
				// }if (msg.what == 0x007) {// 会车
				// Set_Collision_UI();
				//
				// Car_warn_imv.setImageResource(R.drawable.carmeet);
				//
				// }if (msg.what == 0x008) {// 交叉口
				// Set_Collision_UI();
				// Car_warn_imv.setImageResource(R.drawable.crossroad);
				// }if(msg.what == 0x009) {// 防撞预警清显示
				// mMarkerInfoLy.setVisibility(View.GONE);
				// }
			}
		};

	}

	private void Display_MyLoc_Marker() {
		int lat = MyApplication.Lat_From_MK5;
		int longi = MyApplication.Long_From_MK5;
		// LJL 转换成百度地图需要的经纬度
		if((lat!=0)&&(longi!=0)){
			LatLng myAPPlatlong = new LatLng(((double) lat / Scal_to_Covert), ((double) longi / Scal_to_Covert));// 纬度，经度
			my_mk5_LatLong = Util.GPS_Covert(myAPPlatlong);	
		}
	
		BitmapDescriptor mIconMaker = BitmapDescriptorFactory.fromResource(R.drawable.icon_marka);// 把图片转化为BitmapDescriptor格式

		MarkerOptions overlayOptions = new MarkerOptions().position(my_mk5_LatLong).icon(mIconMaker).zIndex(5).draggable(false);// zIndex没什么用
		Marker marker = (Marker) (mBaidumap.addOverlay(overlayOptions));
		marker.setTitle("本车");

	}

	private void Set_Collision_UI() {
		mMarkerInfoLy.setVisibility(View.VISIBLE);
		popupInfo_Car_Info(mMarkerInfoLy, car_info);// 把存在marker点中的信息存入布局框架中
		map_clean.setVisibility(View.INVISIBLE);
		navi.setVisibility(View.INVISIBLE);
		warn_level_iv.setImageResource(R.drawable.warning);
		IninMapLocation(my_mk5_LatLong,30);
	}

	private void setStaticDataToInfoHashMap() {
		if (Client.Other_Car_map.isEmpty()) {
			return;// 如果是空，直接返回
		}

		addInfosOverlayHashMap(Client.Other_Car_map);// 把存储在Info中的信息填充到Marker点中，显示在地图上。

	}

	private void addInfosOverlayHashMap(final HashMap<Integer, Car_Data> other_Car_map) {

		new Thread() {// 一进来就开启新进程，因为网络读取图片不能再主线程中操作
			private BitmapDescriptor mIconMaker;

			public void run() {

				// mBaiduMap.clear();
				OverlayOptions overlayOptions = null;
				Marker marker = null;

				for (Integer key : other_Car_map.keySet()) {
					car_info = other_Car_map.get(key);
					int longi = other_Car_map.get(key).longi;
					int lat = other_Car_map.get(key).lat;
					// Log.i(TAG, "longi-------->" + longi);
					// Log.i(TAG, "lat-------->" + lat);
					// LJL 转换成百度地图需要的经纬度
					LatLng sourceLatLng = new LatLng(((double) lat / Scal_to_Covert),
							((double) longi / Scal_to_Covert));// 纬度，经度

					mIconMaker = BitmapDescriptorFactory.fromResource(R.drawable.maker);// 把图片转化为BitmapDescriptor格式

					overlayOptions = new MarkerOptions().position(Util.GPS_Covert(sourceLatLng)).icon(mIconMaker)
							.zIndex(5);// zIndex没什么用
					marker = (Marker) (mBaidumap.addOverlay(overlayOptions));
					// 额外的信息需要用Bundle存储该marker点的Info数据源
					// 存入的信息放入键值对中，每次都NEW一个Bundle对象
					// 取出时只需要用marker.getExtraInfo().get("info");
					Bundle bundle = new Bundle();// bundel类似于session
					bundle.putSerializable("Car_info", car_info);
					marker.setExtraInfo(bundle);// 把INFO信息存入marker点中
					if (car_info.Mode == 1) {
						myHandler.sendEmptyMessage(0x005);// 跟驰
					}
					if (car_info.Mode == 2) {
						myHandler.sendEmptyMessage(0x006);// 超车
					}
					if (car_info.Mode == 3) {
						myHandler.sendEmptyMessage(0x007);// 会车
					}
					if (car_info.Mode == 4) {
						myHandler.sendEmptyMessage(0x008);// 交叉口
					}
					if (car_info.Mode == 0) {
						myHandler.sendEmptyMessage(0x009);// 清防撞预警显示
					}
				}
				Log.d("TAG", "Finish add Markers");
			}

		}.start();

	}

	@SuppressWarnings("unused")
	private void setStaticDataToInfo() {
		Park_Info.infos.clear();

		// 增加学校内的停车点
		Park_Info.infos.add(new Park_Info("29.562139", "106.603666", "http://images.juheapi.com/park/6202.jpg",
				"http://images.juheapi.com/park/P1004.png", "北京化工大学招待所", "北京市朝阳区北京化工大学西门", "123", "88"));

		addInfosOverlay(Park_Info.infos);// 把存储在Info中的信息填充到Marker点中，显示在地图上。

	}

	private void initMarkerClickEvent() {
		// 对 marker 添加点击相应事件
		mBaidumap.setOnMarkerClickListener(new OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick(Marker marker) {
				Log.i(TAG,"onMarkerClick");
				// Park_Info info = (Park_Info)
				// marker.getExtraInfo().get("info");
				// mMarkerInfoLy.setVisibility(View.VISIBLE);
				// popupInfo(mMarkerInfoLy, info);//把存在marker点中的信息存入布局框架中
				if((Car_Data) marker.getExtraInfo().get("Car_info") instanceof Car_Data){
					Car_Data car_info = (Car_Data) marker.getExtraInfo().get("Car_info");
					mMarkerInfoLy.setVisibility(View.VISIBLE);
					popupInfo_Car_Info(mMarkerInfoLy, car_info);// 把存在marker点中的信息存入布局框架中
					return false;
				}else{
					ToastUtil.makeText(getApplicationContext(), "你点击的本车");
					Log.i(TAG,"你点击的本车");
					return true;
				}

				
			}
		});

	}

	private void popupInfo_Car_Info(RelativeLayout mMarkerLy, Car_Data info) {

		ViewHolder viewHolder = null;

		if (mMarkerLy.getTag() == null) {
			viewHolder = new ViewHolder();
			viewHolder.infoName = (TextView) mMarkerLy.findViewById(R.id.info_name);
			viewHolder.infoAddress = (TextView) mMarkerLy.findViewById(R.id.info_distance);
			viewHolder.car_id = (TextView) mMarkerLy.findViewById(R.id.car_id);
			viewHolder.car_info = (TextView) mMarkerLy.findViewById(R.id.car_info);

			mMarkerLy.setTag(viewHolder);
		}
		viewHolder = (ViewHolder) mMarkerLy.getTag();// info.getPicture()
		viewHolder.infoAddress.setText(String.valueOf(info.lat));
		viewHolder.infoName.setText(String.valueOf(info.longi));
		viewHolder.car_id.setText(String.valueOf(info.id));
		viewHolder.car_info.setText("预警模式为-》" + String.valueOf(info.Mode) + "预警级别为-》" + String.valueOf(info.Level));

		NaviSkipClickEvent_Car_Info(info);

	}

	private void NaviSkipClickEvent_Car_Info(final Car_Data info) {
		navi = (Button) findViewById(R.id.navi);
		navi.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
		
				/*
				 * 		// 开一个新线程跑
				if (Genchi_Thread == null) {
					Genchi_Thread = new textThread(info);
				} else {// 再次点时，要先清他，然后再用现在的info构造这个线程，不然程序会崩
					Genchi_Thread = null;
					Genchi_Thread = new textThread(info);
				}

				Genchi_Thread.flag = true;
				Genchi_Thread.start();
				navi.setEnabled(false);
				 */
				startNavi(info);
				ToastUtil.makeText(getApplicationContext(), "启动导航");

			}
		});

	}

	protected void startNavi(Car_Data info) {
		int longi = info.longi;
		int lat = info.lat;
		LatLng target_car_latlng = new LatLng(((double) lat / Scal_to_Covert),
				((double) longi / Scal_to_Covert));// 纬度，经度
		// 构建 导航参数 有时不导航的原因是自己车与其它车辆距离太近了，百度地图就无法调他的接口
		NaviParaOption para = new NaviParaOption()
					.startPoint(my_mk5_LatLong).endPoint(target_car_latlng)
					.startName("从我自己的地点").endName("目标车辆");

		try {
			BaiduMapNavigation.openBaiduMapNavi(para, RoutePlanDemo.this);
		} catch (BaiduMapAppNotSupportNaviException e) {
			e.printStackTrace();
			ToastUtil.makeText(getApplicationContext(), "请注意没有安装百度地图APP");
		}
		
	}


	protected void Parking_Lot_onMarkerClick(Marker marker) {

		return;

	}

	private class ViewHolder {
		TextView infoName;
		TextView infoAddress;
		TextView car_id;
		TextView car_info;
	}

	protected void popupInfo(RelativeLayout mMarkerLy, Park_Info info) {
		ViewHolder viewHolder = null;

		if (mMarkerLy.getTag() == null) {
			viewHolder = new ViewHolder();
			viewHolder.infoName = (TextView) mMarkerLy.findViewById(R.id.info_name);
			viewHolder.infoAddress = (TextView) mMarkerLy.findViewById(R.id.info_distance);
			viewHolder.car_id = (TextView) mMarkerLy.findViewById(R.id.car_id);
			viewHolder.car_info = (TextView) mMarkerLy.findViewById(R.id.car_info);

			mMarkerLy.setTag(viewHolder);
		}
		viewHolder = (ViewHolder) mMarkerLy.getTag();// info.getPicture()
		viewHolder.infoAddress.setText(info.getAddress());
		viewHolder.infoName.setText(info.getName());
		viewHolder.car_id.setText(info.getKcw());
		viewHolder.car_info.setText(info.getZcw());

		NaviSkipClickEvent(info);

	}

	private void NaviSkipClickEvent(Park_Info info) {
		Button navi = (Button) findViewById(R.id.navi);
		navi.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.i(TAG, "onClick");
			}
		});
	}

	private void initMapClickEvent() {
		mBaidumap.setOnMapClickListener(new OnMapClickListener() {

			@Override
			public boolean onMapPoiClick(MapPoi arg0) {
				return false;
			}

			@Override
			public void onMapClick(LatLng arg0) {
				mMarkerInfoLy.setVisibility(View.GONE);
				mBaidumap.hideInfoWindow();
			}
		});

	}

	private void addInfosOverlay(final List<Park_Info> infos) {
		new Thread() {// 一进来就开启新进程，因为网络读取图片不能再主线程中操作
			private BitmapDescriptor mIconMaker;

			public void run() {

				// mBaiduMap.clear();
				LatLng latLng = null;
				OverlayOptions overlayOptions = null;
				Marker marker = null;

				for (Park_Info info : infos) {
					Double latitude = Double.parseDouble(info.getLatitude());// 转化为Double经度
					Double longitude = Double.parseDouble(info.getLongitude());// 转化为Double纬度

					latLng = new LatLng(latitude, longitude);// 纬度，经度

					mIconMaker = BitmapDescriptorFactory.fromResource(R.drawable.maker);// 把图片转化为BitmapDescriptor格式

					overlayOptions = new MarkerOptions().position(latLng).icon(mIconMaker).zIndex(10);// zIndex没什么用
					marker = (Marker) (mBaidumap.addOverlay(overlayOptions));
					// 初始化marker的时候，只需要填充两个信息：1.经纬度2.显示图片
					// 额外的信息需要用Bundle存储该marker点的Info数据源
					// 存入的信息放入键值对中，每次都NEW一个Bundle对象
					// 取出时只需要用marker.getExtraInfo().get("info");
					Bundle bundle = new Bundle();// bundel类似于session
					bundle.putSerializable("info", info);
					marker.setExtraInfo(bundle);// 把INFO信息存入marker点中
				}
				Log.i(TAG, "Finish add Markers");
			}
		}.start();

	}

	private void IninMapLocation(LatLng mLatLng,int scale) {
		LatLng cqupt_pos = new LatLng(29.541909, 106.615292);
		// 定义地图状态 //myLocation
		MapStatus mMapStatus = new MapStatus.Builder().target(mLatLng).zoom(scale)// 数字越大，地图越放大
				.build();
		// 定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
		MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
		// 改变地图状态
		mBaidumap.setMapStatus(mMapStatusUpdate);
		mBaidumap.setMyLocationEnabled(true);// 使用百度地图定位图层时，要先开启定位

	}

	private void FindViewById() {
		// 初始化地图
		handtv = (TextView) findViewById(R.id.hand_textview_test);
		mMapView = (MapView) findViewById(R.id.id_bmapView);
		mBaidumap = mMapView.getMap();
		mBtnPre = (Button) findViewById(R.id.trans_pre);
		mBtnNext = (Button) findViewById(R.id.trans_next);
		mBtnPre.setVisibility(View.INVISIBLE);
		mBtnNext.setVisibility(View.INVISIBLE);
		mMarkerInfoLy = (RelativeLayout) findViewById(R.id.id_marker_info);
		inner = (RelativeLayout) findViewById(R.id.trans_inner_layout);
		inner.setVisibility(View.GONE);
		navi = (Button) findViewById(R.id.navi);
		map_clean = (Button) findViewById(R.id.map_clean);
		// 初始化隐藏组件
		triger = (Button) findViewById(R.id.chat_display_trigger);
		triger.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				triger.setVisibility(View.GONE);
				inner.setVisibility(View.VISIBLE);
				return true;
			}
		});

		Car_warn_imv = (ImageView) findViewById(R.id.info_img);
		warn_level_iv = (ImageView) findViewById(R.id.warn_level_iv);

		// 关闭隐藏组件按钮
		close = (Button) findViewById(R.id.close_linner);
		close.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				triger.setVisibility(View.VISIBLE);
				inner.setVisibility(View.GONE);
				return true;
			}
		});

		requestLocButton = (Button) findViewById(R.id.btn_location);
		mCurrentMode = LocationMode.NORMAL;
		requestLocButton.setText("普通");
		OnClickListener btnClickListener = new OnClickListener() {
			public void onClick(View v) {
				switch (mCurrentMode) {
				case NORMAL:
					requestLocButton.setText("跟随");
					mCurrentMode = LocationMode.FOLLOWING;
					mBaidumap.setMyLocationConfigeration(
							new MyLocationConfiguration(mCurrentMode, true, mCurrentMarker));
					break;
				case COMPASS:
					requestLocButton.setText("普通");
					mCurrentMode = LocationMode.NORMAL;
					mBaidumap.setMyLocationConfigeration(
							new MyLocationConfiguration(mCurrentMode, true, mCurrentMarker));
					break;
				case FOLLOWING:
					requestLocButton.setText("罗盘");
					mCurrentMode = LocationMode.COMPASS;
					mBaidumap.setMyLocationConfigeration(
							new MyLocationConfiguration(mCurrentMode, true, mCurrentMarker));
					break;
				}
			}
		};
		requestLocButton.setOnClickListener(btnClickListener);

		// 地图初始化
		mMapView = (MapView) findViewById(R.id.id_bmapView);
		mBaidumap = mMapView.getMap();
		// 开启定位图层
		mBaidumap.setMyLocationEnabled(true);
		// 定位初始化
		mLocClient = new LocationClient(this);
		mLocClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		option.setCoorType("bd09ll"); // 设置坐标类型
		option.setScanSpan(100);// 更新时间 单位毫秒
		option.setIsNeedAddress(true);// 设置定位结果包含地址信息
		mLocClient.setLocOption(option);
		mLocClient.start();

	}

	public void MapTypeControlProcess(View v) {
		// 实际使用中请对起点终点城市进行正确的设定
		if (v.getId() == R.id.btn_normalmap) {
			mBaidumap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
			mBaidumap.setBaiduHeatMapEnabled(false);
			mBaidumap.setTrafficEnabled(false);
		} else if (v.getId() == R.id.btn_satellitemap) {
			mBaidumap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
		} else if (v.getId() == R.id.btn_trafficmap) {
			mBaidumap.setTrafficEnabled(true);
		} else if (v.getId() == R.id.btn_hotspotmap) {
			mBaidumap.setBaiduHeatMapEnabled(true);
		}else if (v.getId() == R.id.btn_tocenter) {
			if(my_mk5_LatLong !=null )
				IninMapLocation( my_mk5_LatLong,24);// 确定地图开始在哪里，缩放等级，等其它信息
			ToastUtil.makeText(getApplicationContext(), "my_mk5_LatLong 不为空");
		}
		ToastUtil.makeText(getApplicationContext(), "my_mk5_LatLong 为空");
	}

	/**
	 * 发起路线规划搜索示例
	 * 
	 * @param v
	 */
	public void SearchButtonProcess(View v) {
		// 重置浏览节点的路线数据
		route = null;
		mBtnPre.setVisibility(View.INVISIBLE);
		mBtnNext.setVisibility(View.INVISIBLE);
		mBaidumap.clear();
		// 处理搜索按钮响应
		EditText editSt = (EditText) findViewById(R.id.route_start);
		EditText editEn = (EditText) findViewById(R.id.route_end);
		// 设置起终点信息，对于tranist search 来说，城市名无意义
		PlanNode stNode = PlanNode.withCityNameAndPlaceName("北京", editSt.getText().toString());
		PlanNode enNode = PlanNode.withCityNameAndPlaceName("北京", editEn.getText().toString());

		// 实际使用中请对起点终点城市进行正确的设定
		if (v.getId() == R.id.drive) {
			mSearch.drivingSearch((new DrivingRoutePlanOption()).from(stNode).to(enNode));
		} else if (v.getId() == R.id.transit) {
			mSearch.transitSearch((new TransitRoutePlanOption()).from(stNode).city("北京").to(enNode));
		} else if (v.getId() == R.id.walk) {
			mSearch.walkingSearch((new WalkingRoutePlanOption()).from(stNode).to(enNode));
		}
	}

	/**
	 * 节点浏览示例
	 * 
	 * @param v
	 */
	public void nodeClick(View v) {
		if (route == null || route.getAllStep() == null) {
			return;
		}
		if (nodeIndex == -1 && v.getId() == R.id.pre) {
			return;
		}
		// 设置节点索引
		if (v.getId() == R.id.trans_next) {
			if (nodeIndex < route.getAllStep().size() - 1) {
				nodeIndex++;
			} else {
				return;
			}
		} else if (v.getId() == R.id.trans_pre) {
			if (nodeIndex > 0) {
				nodeIndex--;
			} else {
				return;
			}
		}
		// 获取节结果信息
		LatLng nodeLocation = null;
		String nodeTitle = null;
		Object step = route.getAllStep().get(nodeIndex);
		if (step instanceof DrivingRouteLine.DrivingStep) {
			nodeLocation = ((DrivingRouteLine.DrivingStep) step).getEntrance().getLocation();
			nodeTitle = ((DrivingRouteLine.DrivingStep) step).getInstructions();
		} else if (step instanceof WalkingRouteLine.WalkingStep) {
			nodeLocation = ((WalkingRouteLine.WalkingStep) step).getEntrance().getLocation();
			nodeTitle = ((WalkingRouteLine.WalkingStep) step).getInstructions();
		} else if (step instanceof TransitRouteLine.TransitStep) {
			nodeLocation = ((TransitRouteLine.TransitStep) step).getEntrance().getLocation();
			nodeTitle = ((TransitRouteLine.TransitStep) step).getInstructions();
		}

		if (nodeLocation == null || nodeTitle == null) {
			return;
		}
		// 移动节点至中心
		mBaidumap.setMapStatus(MapStatusUpdateFactory.newLatLng(nodeLocation));
		// show popup
		popupText = new TextView(RoutePlanDemo.this);
		popupText.setBackgroundResource(R.drawable.popup);
		popupText.setTextColor(0xFF000000);
		popupText.setText(nodeTitle);
		mBaidumap.showInfoWindow(new InfoWindow(popupText, nodeLocation, 0));

	}

	/**
	 * 切换路线图标，刷新地图使其生效 注意： 起终点图标使用中心对齐.
	 */
	public void changeRouteIcon(View v) {
		if (routeOverlay == null) {
			return;
		}
		if (useDefaultIcon) {
			((Button) v).setText("自定义起终点图标");
			Toast.makeText(this, "将使用系统起终点图标", Toast.LENGTH_SHORT).show();

		} else {
			((Button) v).setText("系统起终点图标");
			Toast.makeText(this, "将使用自定义起终点图标", Toast.LENGTH_SHORT).show();

		}
		useDefaultIcon = !useDefaultIcon;
		routeOverlay.removeFromMap();
		routeOverlay.addToMap();
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	public void onGetWalkingRouteResult(WalkingRouteResult result) {
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			Toast.makeText(RoutePlanDemo.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
		}
		if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
			// 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
			// result.getSuggestAddrInfo()
			return;
		}
		if (result.error == SearchResult.ERRORNO.NO_ERROR) {
			nodeIndex = -1;
			mBtnPre.setVisibility(View.VISIBLE);
			mBtnNext.setVisibility(View.VISIBLE);
			route = result.getRouteLines().get(0);
			WalkingRouteOverlay overlay = new MyWalkingRouteOverlay(mBaidumap);
			mBaidumap.setOnMarkerClickListener(overlay);
			routeOverlay = overlay;
			overlay.setData(result.getRouteLines().get(0));
			overlay.addToMap();
			overlay.zoomToSpan();
		}

	}

	@Override
	public void onGetTransitRouteResult(TransitRouteResult result) {

		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			Toast.makeText(RoutePlanDemo.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
		}
		if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
			// 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
			// result.getSuggestAddrInfo()
			return;
		}
		if (result.error == SearchResult.ERRORNO.NO_ERROR) {
			nodeIndex = -1;
			mBtnPre.setVisibility(View.VISIBLE);
			mBtnNext.setVisibility(View.VISIBLE);
			route = result.getRouteLines().get(0);
			TransitRouteOverlay overlay = new MyTransitRouteOverlay(mBaidumap);
			mBaidumap.setOnMarkerClickListener(overlay);
			routeOverlay = overlay;
			overlay.setData(result.getRouteLines().get(0));
			overlay.addToMap();
			overlay.zoomToSpan();
		}
	}

	@Override
	public void onGetDrivingRouteResult(DrivingRouteResult result) {
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			Toast.makeText(RoutePlanDemo.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
		}
		if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
			// 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
			// result.getSuggestAddrInfo()
			return;
		}
		if (result.error == SearchResult.ERRORNO.NO_ERROR) {
			nodeIndex = -1;
			mBtnPre.setVisibility(View.VISIBLE);
			mBtnNext.setVisibility(View.VISIBLE);
			route = result.getRouteLines().get(0);
			DrivingRouteOverlay overlay = new MyDrivingRouteOverlay(mBaidumap);
			routeOverlay = overlay;
			mBaidumap.setOnMarkerClickListener(overlay);
			overlay.setData(result.getRouteLines().get(0));
			overlay.addToMap();
			overlay.zoomToSpan();
			overlay.setFocus(true);
		}
	}

	// 定制RouteOverly
	private class MyDrivingRouteOverlay extends DrivingRouteOverlay {

		public MyDrivingRouteOverlay(BaiduMap baiduMap) {
			super(baiduMap);
		}

		@Override
		public BitmapDescriptor getStartMarker() {
			if (useDefaultIcon) {
				return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
			}
			return null;
		}

		@Override
		public BitmapDescriptor getTerminalMarker() {
			if (useDefaultIcon) {
				return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
			}
			return null;
		}
	}

	private class MyWalkingRouteOverlay extends WalkingRouteOverlay {

		public MyWalkingRouteOverlay(BaiduMap baiduMap) {
			super(baiduMap);
		}

		@Override
		public BitmapDescriptor getStartMarker() {
			if (useDefaultIcon) {
				return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
			}
			return null;
		}

		@Override
		public BitmapDescriptor getTerminalMarker() {
			if (useDefaultIcon) {
				return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
			}
			return null;
		}
	}

	private class MyTransitRouteOverlay extends TransitRouteOverlay {

		public MyTransitRouteOverlay(BaiduMap baiduMap) {
			super(baiduMap);
		}

		@Override
		public BitmapDescriptor getStartMarker() {
			if (useDefaultIcon) {
				return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
			}
			return null;
		}

		@Override
		public BitmapDescriptor getTerminalMarker() {
			if (useDefaultIcon) {
				return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
			}
			return null;
		}
	}

	@SuppressLint("InlinedApi")
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

	/**
	 * 定位SDK监听函数
	 */
	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			// map view 销毁后不在处理新接收的位置
			if (location == null || mMapView == null)
				return;
			// Log.i(TAG, "add" + location.getAddrStr());//
			// 这里可以得到我当前的位置如重庆市南岸区崇文路2号，打印出来过,因为是的调好的程序，所以屏了
			Log.i(TAG, "我的位置long" + String.valueOf(location.getLongitude()));
			Log.i(TAG, "我的位置lat" + String.valueOf(location.getLatitude()));
			my_BD_APILocation = new LatLng(location.getLatitude(), location.getLongitude());
			MyLocationData locData = new MyLocationData.Builder().accuracy(location.getRadius())
					// 此处设置开发者获取到的方向信息，顺时针0-360
					.direction(100).latitude(location.getLatitude()).longitude(location.getLongitude()).build();
			mBaidumap.setMyLocationData(locData);
			if (isFirstLoc) {
				isFirstLoc = false;
				LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
				mBaidumap.animateMapStatus(u);
			}
		}

		public void onReceivePoi(BDLocation poiLocation) {
		}
	}

	@Override
	protected void onDestroy() {
		mMapView.onDestroy();
		mSearch.destroy();
	
		super.onDestroy();
	}

	public class textThread extends Thread {
		boolean flag = true;
		int timer = 0;
		private Car_Data mCar_data;

		public textThread(Car_Data data) {
			super();
			this.mCar_data = data;
		}

		@Override
		public void run() {
			super.run();
			while (flag && (my_BD_APILocation != null)) {
				int longi = mCar_data.longi;
				int lat = mCar_data.lat;
				LatLng target_car_latlng = new LatLng(((double) lat / Scal_to_Covert),
						((double) longi / Scal_to_Covert));// 纬度，经度

				PlanNode stNode = PlanNode.withLocation(my_mk5_LatLong);// 用MK5发的的位置导航
				PlanNode entNode = PlanNode.withLocation(Util.GPS_Covert(target_car_latlng));
				mSearch.drivingSearch((new DrivingRoutePlanOption()).from(stNode).to(entNode));
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
	public void onBackPressed() {
		exitDialog(RoutePlanDemo.this, "提示", "亲！您真的要退出吗？");
	}

	private void exitDialog(Context context, String title, String msg) {

		new AlertDialog.Builder(context).setTitle(title).setMessage(msg)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						RoutePlanDemo.this.finish();// 结束当前Activity
						Client.Other_Car_map.clear();
					}
				}).setNegativeButton("取消", null).create().show();

	}

}
