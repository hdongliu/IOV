package com.liu.Class;

import java.io.Serializable;

public class joinadjust implements Serializable{
	
	private Double advspeed;
	
	private Double advdistance;
	
	private Double curdistance;
	
	private int joincarnum;
	
	private int positiontype;
	
	public Double getAdvspeed() {
		return advspeed;
	}
	
	public Double getAdvdistance() {
		return advdistance;
	}
	
	public Double getCurdistance() {
		return curdistance;
	}
	
	public int getJoincarnum() {
		return joincarnum;
	}
	
	public int getPositiontype() {
		return positiontype;
	}
	
	public void setAdvspeed(Double advspeed) {
		this.advspeed = advspeed;
	}
	
	public void setAdvdistance(Double advdistance) {
		this.advdistance = advdistance;
	}
	
	public void setCurdistance(Double curdistance) {
		this.curdistance = curdistance;
	}
	
	public void setJoincarnum(int joincarnum) {
		this.joincarnum = joincarnum;
	}

	public void setPositiontype(int positiontype) {
		this.positiontype = positiontype;
	}

}
