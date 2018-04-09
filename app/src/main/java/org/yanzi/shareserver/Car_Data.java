package org.yanzi.shareserver;

import java.io.Serializable;

public class Car_Data implements Serializable {
	short msgID;	//1 Byte(0~16)
	short msgCnt;	//1 Byte(0~127)
	public int id;		//4 Byte
	short secMark;	//2 Byte(0~65535)
	public int lat;		//4 Byte(-720000000~720000000)
	public  int longi;		//4 Byte(-1440000000~1440000000)
	public double lat_cloud;		//后台发送的经纬度
	public  double longi_cloud;		//
	public String user_name;  //车辆名
	public int vin;//车架号
	short elev;		//2 Byte
	int accuracy;	//4 Byte
	short speed;	//2 Byte(0~32765)
	short heading;	//2 Byte(0~32767)
	short angle;		//1 Byte(-128~127)
	short breaks;		//2 Byte
	public int Mode;
	public int Level;
	
	String size ;	//width:0~1023;length:0~4095*/
    public Way way;
   public class Way{
	   public int longitude;
		public int latitude;
		short vertical;
		char yaw;
   }
   public Car_Data(){
	   way = new Way();
   }

}
