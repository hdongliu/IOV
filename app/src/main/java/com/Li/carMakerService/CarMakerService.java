package com.Li.carMakerService;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.main.activity.MyApplication;

public class CarMakerService extends Service {
	// public String m_serverIP = "172.22.139.150";
	public String m_serverIP = "192.168.123.184";
	// 定义每个数据报的最大大小 为4kb
	private static final int DATA_LEN = 4096;
	public static final String TAG = "CarMakerService";
	public String m_text;
	// private int TIMEOUT = 3000;
	private int servPort = 9090;// 9091
	// 发送初始化
	private DatagramPacket outPacket;
	private double lat,loge;//算是白杰文那边的经度与纬度
	/*
	 * 接收初始化
	 */

	// 定义接收网络数据的字节数组
	byte[] intBuff = new byte[DATA_LEN];
	// 指定字节数组 创建准备数据的 DatagramPacket 对象
	private DatagramPacket receivePacket = new DatagramPacket(intBuff, DATA_LEN);
	private DatagramSocket sendsocket, receiveSocket;
	InetAddress serverAddress;
	private UDPThread udpThread;
	private byte[] data;
	private byte lightState, lightRemainTime;
	private int nLength, lat1, lat2, lat3,lat4,lat5, loge1, loge2,
	loge3, loge4, loge5;

	// byte数组转换成十进制
	private String getHexString(byte[] data, int nLength) {
		String strTemString = "";
		for (int i = 0; i < nLength; i++) {
			strTemString += String.format("%d", data[i]);
		}
		return strTemString;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		// byte[] bytesToSend = m_text.getBytes();// "test_client".getBytes();

		/*
		 * 网络连接线程，判断是否有网络
		 */
		if (udpThread == null) {
			udpThread = new UDPThread();
			udpThread.start();
		}

	}

	public class UDPThread extends Thread {
		public void run() {
			// 建立UDP连接
			try {
				sendsocket = new DatagramSocket();
				receiveSocket = new DatagramSocket(7070);
				serverAddress = InetAddress.getByName(m_serverIP);
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			catch (UnknownHostException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			while (true) {

				// 发送消息
				byte[] sentData = { '3', '2', '1', '2', '1', '2', '1', '2',
						'1', '2', '1', '2', '1', '2', '1', '2', '1', '2' };
				// double[] sentData = "3".get;
				// BufferedWriter br=new BufferedWriter(new
				// OutputStreamWriter(socket));
				try {
					// DatagramSocket socket1 = new DatagramSocket();
					// socket.setSoTimeout(TIMEOUT);
					// DataOutputStream ds=new DataOutputStream();
					DatagramPacket sendPacket = new DatagramPacket(sentData,
							sentData.length, serverAddress, servPort);
					sendsocket.send(sendPacket);
					// socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

				// 接收消息
				try {
					receiveSocket.receive(receivePacket);
					data = receivePacket.getData();
					nLength = receivePacket.getLength();
					lightState = data[0];
					lightRemainTime = data[1];
					lat1 = (int)data[2];
					lat2 = (int)data[3];
					lat3 = (int)data[4];
					lat4 = (int)data[5];
					lat5 = (int)data[6];

					loge1 = (int)data[7];
					loge2 = (int)data[8];
					loge3 = (int)data[9];
					loge4 = (int)data[10];
					loge5 = (int)data[11];
					
					MyApplication.lat = ((double)lat1/100 + (double)lat2/10000 + (double)lat3/1000000+ (double)lat4/100000000+ (double)lat5/(100000*100000))/Math.PI*180 + 0.0027;
					MyApplication.loge = ((double)loge1/100 + (double)loge2/10000 + (double)loge3/1000000+ (double)loge4/100000000+ (double)loge5/(100000*100000))/Math.PI*1800 + 0.0103;

					MyApplication.lightState = (int) lightState;
					MyApplication.lightRemainTime = (int) lightRemainTime;

					Log.i(TAG, "---lightState---" + (int) lightState
							+ "----lightRemainTime=="
							+ MyApplication.lightRemainTime);
//					
//					Log.i(TAG, "---lat1--->" + (int) data[2]+ "----lat2==>"+ (int) data[3]+ "----lat3==>"+ (int) data[4]+ "----lat4==>"+ (int) data[5]+ "----lat5==>"+ (int) data[6]);
//					Log.i(TAG, "---loge1--->" + (int) data[7]+ "----loge2==>"+ data[8]+ "----loge3==>"+ data[9]+ "----loge4==>"+ data[10]+ "----loge5==>"+ data[11]);
                    Log.i(TAG, "-------lat------->"+MyApplication.lat);
                    Log.i(TAG, "-------loge------->"+MyApplication.loge);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
