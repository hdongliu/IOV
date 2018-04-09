package com.liu.Class;

import java.io.Serializable;

public class formation implements Serializable{

	private String formationid;//编队id
	
	private String formationname;//编队名称
	
	private int formationnum;//编队成员数
	
	private int formaitontype;//编队类型 (0 unknown 1 car 2 bus 3   special 4 axle)
	
	private String formationdestination;//编队终点
	
	private String formationstartlocation;//编队起点
	
	private Double vehleaderlat;// 编队头车纬度
	
	private Double vehleaderlon;//编队头车经度
	
	private Double vehleaderspeed;//编队头车速度
	
	public void setFormationid(String formationid) {
		this.formationid = formationid;
	}
	
	public void setFormationname(String formationname) {
		this.formationname = formationname;
	}
	
	public void setFormationnum(int formationnum) {
		this.formationnum = formationnum;
	}
	
	public void setFormaitontype(int formaitontype) {
		this.formaitontype = formaitontype;
	}
	
	public void setFormationdestination(String formationdestination) {
		this.formationdestination = formationdestination;
	}
	
	public void setFormationstartlocation(String formationstartlocation) {
		this.formationstartlocation = formationstartlocation;
	}

	public void setVehleaderlat(Double vehleaderlat) {
		this.vehleaderlat = vehleaderlat;
	}
	
	public void setVehleaderlon(Double vehleaderlon) {
		this.vehleaderlon = vehleaderlon;
	}

	public void setVehleaderspeed(Double vehleaderspeed) {
		this.vehleaderspeed = vehleaderspeed;
	}
	
	public String getFormationid() {
		return formationid;
	}
	
	public String getFormationname() {
		return formationname;
	}
	
	public int getFormationnum() {
		return formationnum;
	}

	public int getFormaitontype() {
		return formaitontype;
	}
	
	public String getFormationdestination() {
		return formationdestination;
	}
	
	public String getFormationstartlocation() {
		return formationstartlocation;
	}
	
	public Double getVehleaderlat() {
		return vehleaderlat;
	}
	
	public Double getVehleaderlon() {
		return vehleaderlon;
	}
	
	public Double getVehleaderspeed() {
		return vehleaderspeed;
	}
}