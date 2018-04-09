package com.liu.Class;

import java.io.Serializable;

public class request implements Serializable {

	private String vehid;
	
	private int quadrant;
	
	private Double distance;
	
	public String getVehid() {
		return vehid;
	}
	
	public int getQuadrant() {
		return quadrant;
	}
	
	public Double getDistance() {
		return distance;
	}
	
	public void setVehid(String vehid) {
		this.vehid = vehid;
	}
	
	public void setQuadrant(int quadrant) {
		this.quadrant = quadrant;
	}
	
	public void setDistance(Double distance) {
		this.distance = distance;
	}
}
