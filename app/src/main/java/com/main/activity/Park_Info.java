package com.main.activity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Park_Info implements Serializable
{
	private static final long serialVersionUID = -758459502806858414L;
	
	//8������
	private String latitude;//����
	private String longitude;//γ��
	private String picture;//ͣ����ͼƬ
	private String state;//��λ״̬ͼ
	private String name;//ͣ��������
	private String address;//ͣ������ַ
	private String zcw;//�ܳ�λ
	private String kcw;//�ճ�λ

	public static List<Park_Info> infos = new ArrayList<Park_Info>();//��Ա�������洢����marker�����Ϣ����ͬActivity��Ҳ���Ե��ã�
														  ///��һ�����ǣ���infos����䷽��������MainActivity��setDatatoInfo�����У�������static��


	//����ʱִ�еĳ�ʼ������
	public Park_Info(){}

	//����Info�Ĵ��η���
	public Park_Info(String latitude, String longitude, String picture, String state, String name,
			String address, String zcw, String kcw)
	{
		super();
		this.latitude = latitude;
		this.longitude = longitude;
		this.picture = picture;
		this.state = state;
		this.name = name;
		this.address = address;
		this.zcw = zcw;
		this.kcw = kcw;
	}
	
	//����
	public String getLatitude()
	{
		return latitude;
	}
	public void setLatitude(String latitude)
	{
		this.latitude = latitude;
	}

	//ά��
	public String getLongitude()
	{
		return longitude;
	}
	public void setLongitude(String longitude)
	{
		this.longitude = longitude;
	}

	//ͣ��������
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}

	//ͣ����ͼƬ
	public String getPicture()
	{
		return picture;
	}
	public void setPicture(String picture)
	{
		this.picture = picture;
	}

	//��λ״̬ͼ
	public String getState()
	{
		return state;
	}
	public void setState(String state)
	{
		this.state = state;
	}

	//ͣ������λ��
	public String getAddress()
	{
		return address;
	}
	public void setAddress(String address)
	{
		this.address = address;
	}

	//�ܳ�λ
	public String getZcw()
	{
		return zcw;
	}
	public void setZcw(String zcw)
	{
		this.zcw = zcw;
	}
	
	//�ճ�λ
	public String getKcw()
	{
		return kcw;
	}
	public void setKcw(String kcw)
	{
		this.kcw = kcw;
	}

}
