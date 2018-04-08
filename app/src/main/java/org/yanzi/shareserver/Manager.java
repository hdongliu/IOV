package org.yanzi.shareserver;

import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;

import com.main.chart.DisplayActivity;

import android.nfc.Tag;
import android.util.Log;

public class Manager {

	private String TAG = "Manager1";

	public static enum command {
		CLOSE
	};

	private Manager() {
	}

	private static final Manager cm = new Manager();

	public static Manager getManager() {
		return cm;
	}

	Vector<Client> vector = new Vector<Client>();

	public void add(Client cs) {
		Log.i(TAG, "add client-----");
		vector.add(cs);
	}

	public void delete(String clientID) {
		// System.out.println("[Manager]: delete~~"+clientID+"~~!!");
		// 迭代器
		Iterator<Client> it = vector.iterator();
		while (it.hasNext()) {
			Client clientTmp = (Client) it.next();
			if (clientTmp.clientID != null
					&& clientTmp.clientID.equals(clientID)) {
				try {

					if (clientTmp != null && clientTmp.socket != null
							&& !clientTmp.socket.isClosed()
							&& !clientTmp.socket.isOutputShutdown()) {
						clientTmp.socket.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				it.remove();
				break;
			}
		}

	}

	public void deleteAll() {
		// 迭代器
		Iterator<Client> it = vector.iterator();
		while (it.hasNext()) {
			Client clientTmp = (Client) it.next();

			Log.i(TAG, "[Manager.java 11] start delete the "
					+ clientTmp.clientID);
			try {
				if (clientTmp != null && clientTmp.socket != null
						&& !clientTmp.socket.isClosed()
						&& !clientTmp.socket.isOutputShutdown()) {
					clientTmp.socket.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			it.remove();

//			client ct = Manager.getManager().getClient(clientTmp.clientID);
//			if (ct == null) {
//				// System.out.println("[Manager.java] delete the "
//				// + clientTmp.clientID + "");
//				// Log.i(TAG, "[Manager.java 22] delete the "+
//				// clientTmp.clientID + "");
//			}
		}
	}

	public int getClientNumber() {
		return vector.size();
	}

	public Client getClient(String clientID) {
		for (int i = 0; i < vector.size(); i++) {
			// System.out.println("[clientID]: TEST 5~~~~~~!!");
			Client csChatSocket = vector.get(i);
			if (csChatSocket.clientID != null
					&& csChatSocket.clientID.equals(clientID)) {
				// System.out.println("delete the old "+clientID+"!");
				return csChatSocket;
			}
		}
		System.out.println("[Manager.java]There is no " + clientID);
		return null;
	}

	public void publish(Client cs, JSONObject out) {
		cs.send(out);

		if (DisplayActivity.flag) {
//			Log.i("Manager2", "已发送出去111111111111111----------------");
			DisplayActivity.flag = false;
		}

	}

	// / wkl 20150728 发送给所有客户端 控制消息， out 内容为 command 和 里面的内容
	public void sendToAll(JSONObject out) {
		for (int i = 0; i < vector.size(); i++) {
			Client csChatSocket = vector.get(i);
			this.publish(csChatSocket, out);
		}
		try {
			System.out.println("Send global message: " + out.getInt("command"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return;
	}
}
