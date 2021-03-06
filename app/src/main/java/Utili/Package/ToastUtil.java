package Utili.Package;

import android.content.Context;
import android.widget.Toast;

public class ToastUtil {
	private static Toast toast;
	/**
	 * 能够连续弹的吐司，不会等上个吐司消失
	 * @param context
	 * @param text
	 */
	public static void makeText(Context context,String text){
		if(toast==null){
			toast = Toast.makeText(context, text, 0);
		}
		toast.setText(text);//将text文本设置给吐司
		toast.show();
	}
}
