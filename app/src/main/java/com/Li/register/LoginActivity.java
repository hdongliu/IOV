package com.Li.register;

import org.json.JSONException;
import org.json.JSONObject;

import com.Li.register.RegisterActivity.MsgReceiverServiceRegisterLogin;
import com.Li.serviceThread.ClientManager;
import com.Li.serviceThread.ServiceClient;
import com.main.activity.MyApplication;
import com.main.activity.R;
import com.main.activity.TabMainActivity;

import Utili.Package.LogUtil;
import Utili.Package.Util;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * author：Administrator on 2017/3/21 09:13 description:文件说明 version:版本
 */
public class LoginActivity extends FragmentActivity implements
		View.OnClickListener {
	private static final String TAG = "LoginActivity";
	private ImageView logo;
	private ScrollView scrollView;
	private EditText user_name;
	private EditText user_password;
	private ImageView iv_clean_phone;
	private ImageView clean_password;
	private ImageView iv_show_pwd;
	private CheckBox rememberPassword;
	private CheckBox autoLogin;
	private Button btn_login;
	private TextView regist;
	private TextView forget_password;
	private int screenHeight = 0;// 屏幕高度
	private int keyHeight = 0; // 软件盘弹起后所占高度
	private float scale = 0.6f; // logo缩放比例
	private View service;
	private int height = 0;

	private JSONObject Account = new JSONObject();

	private SharedPreferences account = null;// 账号数据库
	private String accoountDatabase = "login";
	private SharedPreferences.Editor editor = null;
	private int isCheck = 0; // 是否记住密码 0默认 不记住
	private int isAuto = 0;
	private String str_user;
	private String str_pass;

	private MsgReceiverServiceLogin msgReceiverServiceLogin;
	private JSONObject mJson_login = new JSONObject();
	private TextView toTab;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
				
		// 设置屏幕旋转
//		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);	
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		// 设置输入法不弹起
		setContentView(R.layout.activity_login);
//		mShare_Preference_Init();// 拿到RegisterActivity的SP

		intiView();
		initListener();
		register_Broadcaster();

		account = getApplicationContext().getSharedPreferences(accoountDatabase, Context.MODE_PRIVATE);
		editor = account.edit();
		isAuto = account.getInt("auto", 0);
		isCheck = account.getInt("check", 0); // 刚进入界面获取 是否记住密码的状态
		
		// 记住密码
		if (isCheck == 1) {
			// 记住密码，从SharedPreference中就获取账号密码
			str_user = account.getString("user", "");
			str_pass = account.getString("pass", "");
			// 设置给控件
			user_name.setText(str_user);
			user_password.setText(str_pass);
			// 设置控件为选中状态
			rememberPassword.setChecked(true);
		} else {
			// 不记住密码
			rememberPassword.setChecked(false);
		}
		// 自动登录
		if (isAuto == 1) {
			autoLogin.setChecked(true);
			if (MyApplication.isServerConnect) {
				try {
					mJson_login.put("datatype", "VEH_LOGIN");// 数据类型 登录
					mJson_login.put("fromtype", "veh");// 数据来源
					mJson_login.put("owner_id", "4CFC2BD17DF5793CB");// 数据key
					mJson_login.put("veh_lpn", user_name.getText().toString());// 数据车牌号
					mJson_login.put("veh_pwd", user_password.getText().toString());// 密码
					mJson_login.put("veh_id", user_name.getText().toString());// 车的id
					mJson_login.put("fromid", user_name.getText().toString());// 从哪里来的数据

					Util.send_To_Clound(mJson_login);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				Toast.makeText(LoginActivity.this, "与服务器未连接", Toast.LENGTH_SHORT).show();
			}
		}
		// 记住密码监听器
		rememberPassword
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton arg0,
							boolean bool) {
						// TODO Auto-generated method stub
						if (!bool) {
							autoLogin.setChecked(false);
							// 防止 在正常登录后，在次进入登录界面时，只做了取消记住密码操作，而没有登录的情况。
							editor.putInt("check", 0);
							editor.putInt("auto", 0);
							editor.commit();
						}
					}
				});
		// 自动登录监听器
		autoLogin.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean bool) {
				// TODO Auto-generated method stub
				if (bool) {// 自动登录选中，记住密码也选中。
					rememberPassword.setChecked(true);
				}
			}
		});

		initPermission();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
//		user_name.setText(RegisterActivity.sharedPreferences.getString(
//				RegisterActivity.CAR_No, ""));
//		user_password.setText(RegisterActivity.sharedPreferences.getString(
//				RegisterActivity.CAR_PWD, ""));
		
	}

	private void mShare_Preference_Init() {
		if (RegisterActivity.sharedPreferences == null) {
			RegisterActivity.sharedPreferences = this.getSharedPreferences(
					RegisterActivity.SHARED_PREFERENCES, Context.MODE_PRIVATE);
		}

	}

	private void register_Broadcaster() {
		// TODO Auto-generated method stub
		msgReceiverServiceLogin = new MsgReceiverServiceLogin();
		IntentFilter intentFilter5 = new IntentFilter();
		intentFilter5.addAction("com.Li.ServiceClient.sentLogin");
		registerReceiver(msgReceiverServiceLogin, intentFilter5);
	}

	public class MsgReceiverServiceLogin extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String loginContent = intent.getStringExtra("loginContent");
			if (loginContent.equals("User id or password is wrong")) {
				Toast.makeText(LoginActivity.this, "User id or password is wrong", Toast.LENGTH_SHORT).show();
			} 
			if (loginContent.equals("The vehicle was logged in another device")) {
				Toast.makeText(LoginActivity.this, "The vehicle was logged in another device", Toast.LENGTH_SHORT).show();
			} 
			if (loginContent.equals("Login failed,the vehicle is not registered")) {
				Toast.makeText(LoginActivity.this, "Login failed, the vehicle is not registered", Toast.LENGTH_SHORT).show();
			} 
			if (loginContent.equals("Login success")){
				Toast.makeText(LoginActivity.this, "Login success", Toast.LENGTH_SHORT).show();
				Intent intent1 = new Intent(LoginActivity.this,
						TabMainActivity.class);
				startActivity(intent1);
//				intent1 = null;
				
				// 在登录时，判断控件是否记住密码，只有正确登录才能保存密码
				 if (autoLogin.isChecked()) {
				 editor.putInt("check",1);
				 editor.putInt("auto", 1);
				 editor.putString("user", user_name.getText().toString());
				 editor.putString("pass", user_password.getText().toString());
				 }else if(rememberPassword.isChecked()){
				 editor.putInt("check",1);
				 editor.putString("user", user_name.getText().toString());
				 editor.putString("pass", user_password.getText().toString());
				 }else {
				 editor.putInt("check",0);
				 editor.putInt("auto", 0);
				 }
				 editor.commit();
				MyApplication.user_name = user_name.getText().toString();
				LoginActivity.this.finish();
			}
			
		}
	}

	private void intiView() {
		logo = (ImageView) findViewById(R.id.logo);
		scrollView = (ScrollView) findViewById(R.id.scrollView);
		
		user_name = (EditText) findViewById(R.id.et_mobile);
		user_password = (EditText) findViewById(R.id.et_password);
		
		iv_clean_phone = (ImageView) findViewById(R.id.iv_clean_phone);
		clean_password = (ImageView) findViewById(R.id.clean_password);
		iv_show_pwd = (ImageView) findViewById(R.id.iv_show_pwd);
		rememberPassword = (CheckBox) findViewById(R.id.checkpass);
		autoLogin = (CheckBox) findViewById(R.id.auto_login);
		btn_login = (Button) findViewById(R.id.btn_login);
		regist = (TextView) findViewById(R.id.regist);
		forget_password = (TextView) findViewById(R.id.forget_password);
		toTab = (TextView) findViewById(R.id.offline_to_tabe);
		
		screenHeight = this.getResources().getDisplayMetrics().heightPixels; // 获取屏幕高度
		keyHeight = screenHeight / 3;// 弹起高度为屏幕高度的1/3

	}

	private void initListener() {
		iv_clean_phone.setOnClickListener(this);
		clean_password.setOnClickListener(this);
		iv_show_pwd.setOnClickListener(this);
		btn_login.setOnClickListener(this);
		regist.setOnClickListener(this);
		toTab.setOnClickListener(this);
		user_name.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				if (!TextUtils.isEmpty(s)
						&& iv_clean_phone.getVisibility() == View.GONE) {
					iv_clean_phone.setVisibility(View.VISIBLE);
				} else if (TextUtils.isEmpty(s)) {
					iv_clean_phone.setVisibility(View.GONE);
				}
			}
		});
		user_password.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				if (!TextUtils.isEmpty(s)
						&& clean_password.getVisibility() == View.GONE) {
					clean_password.setVisibility(View.VISIBLE);
				} else if (TextUtils.isEmpty(s)) {
					clean_password.setVisibility(View.GONE);
				}
				if (s.toString().isEmpty())
					return;
				if (!s.toString().matches("[A-Za-z0-9]+")) {
					String temp = s.toString();
					Toast.makeText(LoginActivity.this,
							R.string.please_input_limit_pwd, Toast.LENGTH_SHORT)
							.show();
					s.delete(temp.length() - 1, temp.length());
					user_password.setSelection(s.length());
				}
			}
		});
		scrollView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});
		findViewById(R.id.root).addOnLayoutChangeListener(
				new ViewGroup.OnLayoutChangeListener() {
					@Override
					public void onLayoutChange(View v, int left, int top,
							int right, int bottom, int oldLeft, int oldTop,
							int oldRight, int oldBottom) {
						/*
						 * old是改变前的左上右下坐标点值，没有old的是改变后的左上右下坐标点值
						 * 现在认为只要控件将Activity向上推的高度超过了1/3屏幕高，就认为软键盘弹起
						 */
						if (oldBottom != 0 && bottom != 0
								&& (oldBottom - bottom > keyHeight)) {
							Log.e("wenzhihao", "up------>"
									+ (oldBottom - bottom));
							new Handler().postDelayed(new Runnable() {
								@Override
								public void run() {
									scrollView.smoothScrollTo(0,
											scrollView.getHeight());
								}
							}, 0);
							zoomIn(logo, (oldBottom - bottom) - keyHeight);
							service.setVisibility(View.INVISIBLE);
						} else if (oldBottom != 0 && bottom != 0
								&& (bottom - oldBottom > keyHeight)) {
							Log.e("wenzhihao", "down------>"
									+ (bottom - oldBottom));
							// 键盘收回后，logo恢复原来大小，位置同样回到初始位置
							new Handler().postDelayed(new Runnable() {
								@Override
								public void run() {
									scrollView.smoothScrollTo(0,
											scrollView.getHeight());
								}
							}, 0);
							zoomOut(logo, (bottom - oldBottom) - keyHeight);
							service.setVisibility(View.VISIBLE);
						}
					}
				});
	}

	/**
	 * 缩小
	 * 
	 * @param view
	 */
	public void zoomIn(final View view, float dist) {
		view.setPivotY(view.getHeight());
		view.setPivotX(view.getWidth() / 2);
		AnimatorSet mAnimatorSet = new AnimatorSet();
		ObjectAnimator mAnimatorScaleX = ObjectAnimator.ofFloat(view, "scaleX",
				1.0f, scale);
		ObjectAnimator mAnimatorScaleY = ObjectAnimator.ofFloat(view, "scaleY",
				1.0f, scale);
		ObjectAnimator mAnimatorTranslateY = ObjectAnimator.ofFloat(view,
				"translationY", 0.0f, -dist);

		mAnimatorSet.play(mAnimatorTranslateY).with(mAnimatorScaleX);
		mAnimatorSet.play(mAnimatorScaleX).with(mAnimatorScaleY);
		mAnimatorSet.setDuration(200);
		mAnimatorSet.start();
	}

	/**
	 * f放大
	 * 
	 * @param view
	 */
	public void zoomOut(final View view, float dist) {
		view.setPivotY(view.getHeight());
		view.setPivotX(view.getWidth() / 2);
		AnimatorSet mAnimatorSet = new AnimatorSet();

		ObjectAnimator mAnimatorScaleX = ObjectAnimator.ofFloat(view, "scaleX",
				scale, 1.0f);
		ObjectAnimator mAnimatorScaleY = ObjectAnimator.ofFloat(view, "scaleY",
				scale, 1.0f);
		ObjectAnimator mAnimatorTranslateY = ObjectAnimator.ofFloat(view,
				"translationY", view.getTranslationY(), 0);

		mAnimatorSet.play(mAnimatorTranslateY).with(mAnimatorScaleX);
		mAnimatorSet.play(mAnimatorScaleX).with(mAnimatorScaleY);
		mAnimatorSet.setDuration(200);
		mAnimatorSet.start();
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.iv_clean_phone:
			user_name.setText("");
			break;
		case R.id.offline_to_tabe:
			LogUtil.i(TAG, "offline_to_tabe-------");

			if (rememberPassword.isChecked()) {
				editor.putInt("check",1);
				editor.putString("user", user_name.getText().toString());
				editor.putString("pass", user_password.getText().toString());
				editor.commit();
			}

			Intent mintentTab = new Intent(LoginActivity.this,
					TabMainActivity.class);
			startActivity(mintentTab);
			LoginActivity.this.finish();
			break;
		case R.id.clean_password:
			user_password.setText("");
			break;
		case R.id.iv_show_pwd:
			if (user_password.getInputType() != InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
				user_password
						.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
				iv_show_pwd.setImageResource(R.drawable.pass_visuable);
			} else {
				user_password.setInputType(InputType.TYPE_CLASS_TEXT
						| InputType.TYPE_TEXT_VARIATION_PASSWORD);
				iv_show_pwd.setImageResource(R.drawable.pass_gone);
			}
			String pwd = user_password.getText().toString();
			if (!TextUtils.isEmpty(pwd))
				user_password.setSelection(pwd.length());
			break;
		case R.id.regist:
			Intent intentRegister = new Intent(LoginActivity.this,
					RegisterActivity.class);
			startActivity(intentRegister);
			finish();
			break;
		case R.id.btn_login:
			
			if (MyApplication.isServerConnect) {
				try {
					mJson_login.put("datatype", "VEH_LOGIN");// 数据类型 登录
					mJson_login.put("fromtype", "veh");// 数据来源
					mJson_login.put("owner_id", "4CFC2BD17DF5793CB");// 数据key
					mJson_login.put("veh_lpn", user_name.getText().toString());// 数据车牌号
					mJson_login.put("veh_pwd", user_password.getText().toString());// 密码
					mJson_login.put("veh_id", user_name.getText().toString());// 车的id
					mJson_login.put("fromid", user_name.getText().toString());// 从哪里来的数据

					Util.send_To_Clound(mJson_login);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				Toast.makeText(LoginActivity.this, "与服务器未连接", Toast.LENGTH_SHORT).show();
			}
			
			break;
		}
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
