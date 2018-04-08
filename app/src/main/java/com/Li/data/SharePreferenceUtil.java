package com.Li.data;

import android.content.Context;
import android.content.SharedPreferences;

/*
 * 轻量级数据库，存储登陆和注册信息
 */
public class SharePreferenceUtil {

	private SharedPreferences sp;
	private SharedPreferences.Editor editor;

	public SharePreferenceUtil(Context context, String file) {// 传入一个
																// file（文件名），context上下文
		sp = context.getSharedPreferences(file, context.MODE_PRIVATE);
		editor = sp.edit();
	}

	public void setAccounts(String account) {
		editor.putString("account", account);
		editor.commit();
	}

	public String getAccounts() {
		return sp.getString("account", "");
	}

	public void setPasswd(String passwd) {
		editor.putString("passwd", passwd);
		editor.commit();
	}

	public String getPasswd() {
		return sp.getString("passwd", "");
	}

	public String getName() {
		return sp.getString("name", "");
	}

	public void setName(String name) {
		editor.putString("name", name);
		editor.commit();
	}

	public String getEmail() {
		return sp.getString("email", "");
	}

	public void setEmail(String email) {
		editor.putString("email", email);
		editor.commit();
	}

	public Integer getImg() {
		return sp.getInt("img", 0);
	}

	public void setImg(int i) {
		editor.putInt("img", i);
		editor.commit();
	}

	// ip
	public void setIp(String ip) {
		editor.putString("ip", ip);
		editor.commit();
	}

	public void setPort(int port) {
		editor.putInt("port", port);
		editor.commit();
	}


	public void setIsStart(boolean isStart) {
		editor.putBoolean("isStart", isStart);
		editor.commit();
	}

	public boolean getIsStart() {
		return sp.getBoolean("isStart", false);
	}

	public void setUname(String string) {
		editor.putString("string", string);
		editor.commit();
	}

	public String getUname(String string) {
		// TODO Auto-generated method stub
		return string;
	}

}
