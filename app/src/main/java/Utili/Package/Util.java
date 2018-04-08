package Utili.Package;

import org.json.JSONObject;

import com.Li.serviceThread.ClientManager;
import com.Li.serviceThread.ServiceClient;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.main.activity.MyApplication;
import com.main.activity.R;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class Util {
	/*
	 * 函数说明，传入一个MK5发来的经纬度，转化成可以正确在地图上显示的经纬度
	 */
	public static LatLng GPS_Covert(LatLng sourceLatLng) {
		CoordinateConverter converter = new CoordinateConverter();
		converter.from(CoordinateConverter.CoordType.GPS);
		converter.coord(sourceLatLng);
		LatLng desLatLng = converter.convert();
		return desLatLng;

	}
	
	public static void IninMapLocation(BaiduMap mBaidumap,int zoomScale) {
		LatLng cqupt_pos = new LatLng(29.541909, 106.615292);
		// 定义地图状态
		MapStatus mMapStatus = new MapStatus.Builder().target(cqupt_pos)
				.zoom(zoomScale)// 数字越大，地图越放大
				.build();
		// 定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
		MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory
				.newMapStatus(mMapStatus);
		// 改变地图状态
		mBaidumap.setMapStatus(mMapStatusUpdate);
		mBaidumap.setMyLocationEnabled(true);// 使用百度地图定位图层时，要先开启定位
		
	}
	
	/*
	 * 传入一个JSONObject对象，然后发送到后台
	 */
	public static void send_To_Clound(JSONObject mJsonObject) {
		if (ClientManager.getManager() != null) {
			ServiceClient ct = ClientManager.getManager()
					.getClient();
			// 向后台发送 Jason包
			ClientManager.getManager().servicePublish(ct,
					mJsonObject);
		}
		
	}
	
	/**
	 * 判断网络是否可用
	 */
	public static boolean isNetworkAvailable(Context context) {
		if (context != null){
			ConnectivityManager mgr = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo[] info = mgr.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	/*
	 * 传入一个百度地图，在它的图层上面添加本车位置
	 */
	public static void Display_MyLoc_Marker(BaiduMap mBaidumap) {
		int lat = MyApplication.Lat_From_MK5;
		int longi = MyApplication.Long_From_MK5;
		// LJL 转换成百度地图需要的经纬度
		if ((lat != 0) && (longi != 0)) {
			LatLng myAPPlatlong = new LatLng(((double) lat / MyApplication.Scal_to_Covert),
					((double) longi / MyApplication.Scal_to_Covert));// 纬度，经度
			MyApplication.LatLong_From_MK5 = Util.GPS_Covert(myAPPlatlong);
		}

		BitmapDescriptor mIconMaker = BitmapDescriptorFactory
				.fromResource(R.drawable.icon_marka);// 把图片转化为BitmapDescriptor格式

		MarkerOptions overlayOptions = new MarkerOptions()
				.position(MyApplication.LatLong_From_MK5).icon(mIconMaker).zIndex(5)
				.draggable(false);// zIndex没什么用
		Marker marker = (Marker) (mBaidumap.addOverlay(overlayOptions));
		marker.setTitle("本车");

	}
	
}
