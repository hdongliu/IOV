package com.main.utilTools;

import java.sql.Date;
import java.text.SimpleDateFormat;


public class MyDate {
	
public static SimpleDateFormat format1,format;
	public static String getDateCN() {
		format = new SimpleDateFormat("yyyy年MM月dd日  HH:mm:ss");
		String date = format.format(new Date(System.currentTimeMillis()));
		return date;// 2012��10��03�� 23:41:31
	}

	public static String getDateEN() {
		format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String date1 = format1.format(new Date(System.currentTimeMillis()));
		return date1;// 2012-10-03 23:41:31
	}
}