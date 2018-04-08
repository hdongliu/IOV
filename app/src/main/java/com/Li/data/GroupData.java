package com.Li.data;

public class GroupData {

	public String datatype;// 详细类型 值可以为：TEAM_REGISTER
	public String veh_id;//车辆唯一标识   与fromid是一样的
	public int team_id;// 车队ID
	public String team_name;// 车队名称
	public double team_startlongitude;// 起点经度
	public double team_startlatitude;// 起点纬度
	public double team_stoplongitude;// 终点经度
	public double team_stoplatitude;// 终点纬度
	public int team_veh_maxnumber; // 车队最大车辆数
	public int team_veh_number; // 车辆数
	public String team_description; // 车队描述
	public String team_logo; // 车队标志
	public double team_regtime; // 车队注册时间
	public double team_driveroute;// 车队行驶路线
	
}
