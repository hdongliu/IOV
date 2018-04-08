package com.main.activity;

import org.yanzi.shareserver.Client;
import org.yanzi.shareserver.Car_Data;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.VersionInfo;
import com.main.activity.R;
import com.main.activity.BMapApiDemoMain.SDKReceiver;

public class Dis_Info_Activity extends Activity {
	private static final String TAG = "Dis_Info_Activity";
	private SDKReceiver mReceiver;
	private Handler myHandler;
	// obd textview display wangyonglong
	private TextView tv_vbat, tv_rpm, tv_spd, tv_tp, tv_lod, tv_ect, tv_fli,
			tv_mph;
	private RelativeLayout light;
	private TextView lightText;
	private ImageView green;
	private ImageView yellow;
	private ImageView red;
	private RelativeLayout houtai_ifo_rl_layout;
	private TextView MK5_tv_push_ifo;
	private TextView houtai_tv_ifo;
	private Button groupBtn;
	protected RelativeLayout fragmentRelative;
	private LinearLayout groupInvite;
	private EditText inviteName;
	private EditText inviteUser;
	private Button inviteBack;
	private Button inviteOK;
	private final int MAX_LIMIT_TIME = 6000;// 后台消息界面显示 最大显示时间，超过时间则界面消失

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 设置屏幕旋转
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		
		setContentView(R.layout.dsp_info_layout);
		Find_View_By_Id();// 拿到UI上的控件
		Baidu_Key_Listener();// 百度地图KEY监听器
		UI_Handler();// 这里写更新UI

		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					if (MyApplication.lightState != 0) {// 交通灯显示
						myHandler.sendEmptyMessage(0x001);
					}
					if (MyApplication.OBDFlag_display) {// OBD显示
						myHandler.sendEmptyMessage(0x002);
					}
					if (MyApplication.MK5InfoFlag
							|| (MyApplication.houtai_msg_push != null)) {// 消息推送
						myHandler.sendEmptyMessage(0x003);
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
		
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				while (true) {
//					MyApplication.MK5InfoFlag = true;
//					MyApplication.MK5Info = "hello world";
//
//					try {
//						Thread.sleep(5000);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					
//					MyApplication.houtai_msg_push = "后台消息发来了";
//					
//					try {
//						Thread.sleep(5000);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
//			}
//		}).start();

	}

	private void UI_Handler() {

		myHandler = new Handler() {
			public void handleMessage(Message msg) {
				if (msg.what == 0x001) {
					switch (MyApplication.lightState) {
					case 1:// 绿
						Display_Green();

						break;
					case 2:// yellow
						Display_Yellow();

						break;
					case 3:// red
						Display_Red();

						break;
					case 4:// yellow
						Display_Yellow();
						break;

					default:
						break;
					}
				}
				if (msg.what == 0x002) {
					Display_OBD();

				}
				if (msg.what == 0x003) {
					Display_Msg_Push();

				}
			}
		};

	}

	private void Display_Msg_Push() {
	
		// 显示路测消息内容
		houtai_tv_ifo.setVisibility(View.VISIBLE);
		houtai_tv_ifo.setText(MyApplication.houtai_msg_push);
		MK5_tv_push_ifo.setVisibility(View.VISIBLE);
		MK5_tv_push_ifo.setText(MyApplication.MK5Info);

	}

	private void Display_OBD() {
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

	private void Display_Red() {
		green.setVisibility(View.GONE);
		red.setVisibility(View.VISIBLE);
		yellow.setVisibility(View.GONE);
		// 显示交通秒数
		lightText.setText(String.valueOf(MyApplication.lightRemainTime) + "s");
		lightText.setTextSize(40);
		lightText.setTextColor(Color.RED);

	}

	private void Display_Yellow() {
		green.setVisibility(View.GONE);
		red.setVisibility(View.GONE);
		yellow.setVisibility(View.VISIBLE);
		// 显示交通秒数
		lightText.setText(String.valueOf(MyApplication.lightRemainTime) + "s");
		lightText.setTextSize(40);

		lightText.setTextColor(Color.YELLOW);

	}

	private void Display_Green() {
		green.setVisibility(View.VISIBLE);
		red.setVisibility(View.GONE);
		yellow.setVisibility(View.GONE);
		// 显示交通秒数
		lightText.setText(String.valueOf(MyApplication.lightRemainTime) + "s");
		lightText.setTextColor(Color.GREEN);
		lightText.setTextSize(40);

	}

	private void Baidu_Key_Listener() {
		// 注册 SDK 广播监听者
		IntentFilter iFilter = new IntentFilter();
		iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
		iFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
		mReceiver = new SDKReceiver();
		registerReceiver(mReceiver, iFilter);

	}

	private void Find_View_By_Id() {
		TextView text = (TextView) findViewById(R.id.map_key);
		text.setTextColor(Color.YELLOW);
		text.setText("欢迎使用百度地图Android SDK v" + VersionInfo.getApiVersion());

		// OBD 消息显示界面 wangyonglong
		tv_vbat = (TextView) findViewById(R.id.obd_tv_vbat);// OBD发来的电池电压
		tv_rpm = (TextView) findViewById(R.id.obd_tv_rpm);// OBD发来的发动机转速
		tv_spd = (TextView) findViewById(R.id.obd_tv_spd);// OBD发来的车速
		tv_tp = (TextView) findViewById(R.id.obd_tv_tp);// OBD发来的节气门开度
		tv_lod = (TextView) findViewById(R.id.obd_tv_lod);// OBD发来的发动机负荷
		tv_ect = (TextView) findViewById(R.id.obd_tv_ect);// OBD发来的冷却液水温
		tv_fli = (TextView) findViewById(R.id.obd_tv_fli);// OBD发来的剩余油量
		tv_mph = (TextView) findViewById(R.id.obd_tv_mph);// OBD发来的瞬时油耗

		// 交通灯界面显示
		light = (RelativeLayout) findViewById(R.id.light_layout);
		light.setVisibility(View.VISIBLE);
		lightText = (TextView) findViewById(R.id.traf_lig_info);
		green = (ImageView) findViewById(R.id.green);
		yellow = (ImageView) findViewById(R.id.yellow);
		red = (ImageView) findViewById(R.id.red);

		// 路测后台 消息显示界面初始化
		houtai_ifo_rl_layout = (RelativeLayout) findViewById(R.id.houtai_layout);
		houtai_ifo_rl_layout.setVisibility(View.VISIBLE);
		// 显示路测消息 的消息头：路测消息
		MK5_tv_push_ifo = (TextView) findViewById(R.id.mk5_tv);
		MK5_tv_push_ifo.setVisibility(View.VISIBLE);
		// 显示路测消息内容
		houtai_tv_ifo = (TextView) findViewById(R.id.houtai_tv);
		houtai_tv_ifo.setVisibility(View.GONE);
		// 车群组界面 fragment（碎片组件）
		fragmentRelative = (RelativeLayout) findViewById(R.id.id_fragment_relative);
		fragmentRelative.setVisibility(View.GONE);
		// 创建车群组组件
		groupBtn = (Button) findViewById(R.id.group);
		groupBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//fragmentRelative.setVisibility(View.VISIBLE);
			}
		});

		// 邀约
		// groupInvite = (LinearLayout) findViewById(R.id.group_invite);
		// inviteName = (EditText) findViewById(R.id.group__invite_name);
		// inviteUser = (EditText) findViewById(R.id.group_invite_user_name);
		// inviteBack = (Button) findViewById(R.id.group_invite_back_btn);
		// // inviteBack.setOnClickListener(this);
		// inviteOK = (Button) findViewById(R.id.group_invite_ok);
		// // inviteOK.setOnClickListener(this);

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		// 取消监听 SDK 广播
		unregisterReceiver(mReceiver);
	}

	/**
	 * 构造广播监听类，监听 SDK key 验证以及网络异常广播
	 */
	public class SDKReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			String s = intent.getAction();
			Log.i(TAG, "action: " + s);
			TextView text = (TextView) findViewById(R.id.text_Info);
			text.setTextColor(Color.RED);
			if (s.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR)) {
				text.setText("key 验证出错! 请在 AndroidManifest.xml 文件中检查 key 设置");
			} else if (s
					.equals(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR)) {
				text.setText("网络出错");
			}
		}
	}
	
	@Override
	public void onBackPressed() {
		exitDialog(Dis_Info_Activity.this, "提示", "亲！您真的要退出吗？");
	}

	private void exitDialog(Context context, String title, String msg) {

		new AlertDialog.Builder(context).setTitle(title).setMessage(msg)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						Dis_Info_Activity.this.finish();// 结束当前Activity
						Client.Other_Car_map.clear();
					}
				}).setNegativeButton("取消", null).create().show();

	}
}