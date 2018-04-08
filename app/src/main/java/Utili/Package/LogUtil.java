package Utili.Package;

import android.util.Log;


public class LogUtil {
	
	public static void i(String tag, Object msg) {
		//����ϲ���ӡ��־
		if (false) {
			return;
		}
		Log.i(tag, String.valueOf(msg));
	}
}
