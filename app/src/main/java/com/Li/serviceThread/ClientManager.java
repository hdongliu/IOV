package com.Li.serviceThread;

import java.util.Iterator;
import java.util.Vector;

import org.json.JSONObject;

import android.util.Log;

public class ClientManager {
	private final static String TAG= "ClientManager";


	public static enum command{
		CLOSE
	};
	
	private ClientManager(){}
	private static final ClientManager cm = new ClientManager();
	
	public static ClientManager getManager() {
		return cm;
	}
	
	Vector<ServiceClient> vector = new Vector<ServiceClient>();
	
	public void add(ServiceClient sc) {
		Log.i(TAG, "[ClientManager]------addClient");
		vector.add(sc);
		Log.i(TAG, "[ClientManager]----"+vector.size());
		
	}
	
//	public void delete(String clientID) {
//		//System.out.println("[Manager]: delete~~"+clientID+"~~!!");
//	      //迭代器   
//      Iterator it = vector.iterator();  
//      while(it.hasNext()){ 
//    	//  System.out.println("[delete]: ~"+clientID+"~ 1 ~~!!");
//    	  client clientTmp = (client)it.next();
//    	  if( clientTmp.clientID != null && clientTmp.clientID.equals(clientID) )
//    	  {
//    	//	  System.out.println("[delete]: ~"+clientID+"~ 2 ~~!!");
//    		  try {
//    			  
//    			    if( clientTmp != null && clientTmp.socket != null &&  !clientTmp.socket.isClosed() && !clientTmp.socket.isOutputShutdown() )
//	        		{	
//	        			clientTmp.socket.close();
//	        		}
//		//		System.out.println("[delete]: ~"+clientID+"~ 3 ~~!!");
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			  it.remove();
//		//	  System.out.println("[delete]: ~"+clientID+"~ 4 ~~!!");
//			  break;
//    	  }
//      }   

		
//	}
	
	public void deleteAll() {
	      //迭代器   
        Iterator it = vector.iterator();   
        while(it.hasNext()){   
        	ServiceClient clientTmp = (ServiceClient)it.next();

//        	System.out.println("[Manager.java] start delete the "+clientTmp.clientID+"");
//        	try {
//	        		if( clientTmp != null && clientTmp.socket != null &&  !clientTmp.socket.isClosed() && !clientTmp.socket.isOutputShutdown() )
//	        		{	
//	        			clientTmp.socket.close();
//	        			
//	        		}
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
        	
        	it.remove();
        	
//        	ServiceClient ct = Manager.getManager().getClient(clientTmp.clientID);
//            if( ct == null ){
//            	System.out.println("[Manager.java] delete the "+clientTmp.clientID+"");
//            }
        }   
	}
	
	public int getClientNumber(){
		return vector.size();		
	}
	
	public ServiceClient getClient() {
		
		for (int i = 0; i < vector.size(); i++) {
			ServiceClient csChatSocket = vector.get(i);
			
			Log.i(TAG, "[ClientManager]---retrun csChatSocket");
					return csChatSocket;
			}
		Log.i(TAG, "[ClientManager]---retrun null");
		return null;
	}
	
	public void servicePublish(ServiceClient cs, JSONObject out) {
		cs.send(out);
	}
	
	/// wkl 20150728   发送给所有客户端 控制消息，   out 内容为   command  和   里面的内容
//	public void sendToAll( JSONObject out  ) {
//		for (int i = 0; i < vector.size(); i++) {
//			client csChatSocket = vector.get(i);
//			this.publish(csChatSocket, out);
//		}
//		try {
//			System.out.println("Send global message: "+out.getInt("command"));
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return ;
//	}

}
