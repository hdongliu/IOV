package com.Li.register;

import java.net.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.Li.data.SharePreferenceUtil;
import com.main.activity.MyApplication;
import com.main.activity.R;
import com.main.activity.TabMainActivity;

import Utili.Package.ToastUtil;
import Utili.Package.Util;

/**
 * author：Administrator on 2017/3/21 09:13 description:文件说明
 * 注册画面，主要作用把收到注册成功的广播得到，跳转到TAB，同时将车牌号的密码存到SP里，供LoginActivity用 version:版本
 */
public class RegisterActivity extends Activity implements OnClickListener {

	private final static String TAG = "RegisterActivity";

	public static JSONObject mJson_register = new JSONObject();
	private MsgReceiverServiceRegisterLogin msgReceiverServiceRegisterLogin;
	private RegisterLoginSuccess registerLoginSuccess;
	private Button mBtn_Register;
	private Button mBtn_back;
	private EditText user_name, user_password;

	// SP,属性写成默认，是为了LoginActivity，访问
	private Editor editor;
	static SharedPreferences sharedPreferences;
	static final String SHARED_PREFERENCES = "RegisterActivity";
	static final String CAR_No = "CAR_No";
	static final String CAR_PWD = "CAR_PWD";
	
	private JSONObject mJson_register_login = new JSONObject();

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);

		mRegister_Receiver();// 注册后台发来的注册成功的广播，准备接收
		initView();
		mShare_Preference_Init();//
		initPermission();
	}

	private void mRegister_Receiver() {
		/*
		 * set isRegister=true; send reg info to the background in the broadcast
		 * receiver
		 */
		msgReceiverServiceRegisterLogin = new MsgReceiverServiceRegisterLogin();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("com.Li.ServiceClient.sentRegisterLogin");
		registerReceiver(msgReceiverServiceRegisterLogin, intentFilter);
		
		// 注册成功后，发送登录信息等待后台回应的广播
		registerLoginSuccess = new RegisterLoginSuccess();
		IntentFilter intentFilter1 = new IntentFilter();
		intentFilter1.addAction("com.Li.ServiceClient.sentRegisterLoginSuccess");
		registerReceiver(registerLoginSuccess, intentFilter1);

	}

	public void initView() {
		mBtn_Register = (Button) findViewById(R.id.register_btn);
		mBtn_back = (Button) findViewById(R.id.reg_back_btn);

		mBtn_Register.setOnClickListener(this);
		mBtn_back.setOnClickListener(this);

		user_name = (EditText) findViewById(R.id.user_name);
		user_password = (EditText) findViewById(R.id.user_password);

	}

	private void mShare_Preference_Init() {
		// TODO Auto-generated method stub
		if (sharedPreferences == null) { // SharedPreferences是Android平台上一个轻量级的存储类，
											// 主要是保存一些常用的配置比如窗口状态
			sharedPreferences = getSharedPreferences(SHARED_PREFERENCES,
					MODE_PRIVATE);
		}

		editor = sharedPreferences.edit();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();

	}

	/*
	 * 当打开APP 收到 isSuccess 向后台发送登陆成功注册的JSON车辆信息在 registeractivity broadcaster
	 * receiver ‘ MsgReceiverServiceRegisterLogin’ in regester activity get the
	 * bradcast and send register info to houtai set isRegister = true
	 */
	// 在注册活动中收到广播，广播接收器MsgReceiverServiceRegisterLogin，向后台发送注册信息，
	// 并置isRegister= true
	public class MsgReceiverServiceRegisterLogin extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Boolean isRegister = intent.getBooleanExtra("isRegister", false);
			String content = intent.getStringExtra("content");
			if (isRegister) {
				// 收到注册成功，说明这个帐号可以用，这样把它存到SP
				save_To_Sharepreference();
	            // 注册成功后，发送登录信息
				try {
					mJson_register_login.put("datatype", "VEH_LOGIN");// 数据类型 登录
					mJson_register_login.put("fromtype", "veh");// 数据来源
					mJson_register_login.put("owner_id", "4CFC2BD17DF5793CB");// 数据key
					mJson_register_login.put("veh_lpn", user_name.getText().toString());// 数据车牌号
					mJson_register_login.put("veh_pwd", user_password.getText().toString());// 密码
					mJson_register_login.put("veh_id", user_name.getText().toString());// 车的id
					mJson_register_login.put("fromid", user_name.getText().toString());// 从哪里来的数据

					MyApplication.register_login = true;
					Util.send_To_Clound(mJson_register_login);
//					mJson_register_login = null;
				} catch (JSONException e) {
					e.printStackTrace();
				}
				Toast.makeText(RegisterActivity.this,"注册成功！", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(RegisterActivity.this,"该账号已经被注册，请重新注册！", Toast.LENGTH_SHORT).show();
			}


			
		}
	}
	
	public class RegisterLoginSuccess extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Intent intent1 = new Intent(RegisterActivity.this,
					TabMainActivity.class);
			startActivity(intent1);
			intent1 = null;
			//注册框信息清除
			user_name.setText("");
			user_password.setText("");
			RegisterActivity.this.finish();
		}
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.register_btn:
			send_info_to_houtai();
			break;
		case R.id.reg_back_btn:
			// onBackPressed();
			finish();
			break;
		default:
			break;
		}
	}

	public void save_To_Sharepreference() {
		editor.putString(CAR_No, user_name.getText().toString().trim());
		editor.putString(CAR_PWD, user_password.getText().toString().trim());
		editor.commit();
		// ToastUtil.makeText(RegisterActivity.this, "保存成功，准备跳转");

	}

	private void send_info_to_houtai() {

		if (user_name.getText().toString().trim().isEmpty()
				|| user_password.getText().toString().trim().isEmpty()) {
			ToastUtil.makeText(getApplicationContext(), "请输入正确的数据");
			return;
		}
		try {
			mJson_register.put("datatype", "VEH_REGISTER");// 数据类型 注册
			mJson_register.put("fromtype", "veh");// 数据来源
			mJson_register.put("owner_id", "4CFC2BD17DF5793CB");// 数据key
			mJson_register.put("veh_lpn", user_name.getText());// 数据车牌号
			mJson_register.put("veh_pwd", user_password.getText());// 密码
			mJson_register.put("veh_id", user_name.getText());// 车的id
			mJson_register.put("fromid", user_name.getText());// 从哪里来的数据

			MyApplication.user_name = user_name.getText().toString();
			
			Util.send_To_Clound(mJson_register);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		Toast.makeText(RegisterActivity.this, "已发送到后台，请稍等！",
				Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		// 捕获返回键
		toast(RegisterActivity.this);
	}

	/*
	 * 生成 -----您真的不注册了吗----对话框
	 */
	private void toast(Context context) {
		new AlertDialog.Builder(context).setTitle("注册")
				.setMessage("亲！您真的不注册了吗？")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						RegisterActivity.this.finish();
					}
				}).setNegativeButton("取消", null).create().show();
	}

	//权限申请
	private void initPermission() {

		if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
			// 进入到这里代表没有权限.
			ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 123);
		}

	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		switch (requestCode) {
			case 123:
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

				}
		}
	}

}
