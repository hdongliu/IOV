package org.yanzi.shareserver;

import java.util.ArrayList;
import java.util.List;

public class UnregisteredId {
   int count;
   int[] arrayid;
   public static List<Integer> list = new ArrayList<Integer>();
   public UnregisteredId(int count) {
	// TODO Auto-generated constructor stub
	   this.count = count;
	   arrayid = new int[count];
}
   public boolean CreatLink(){
	if(count != 0){
    
    for(int i = 0; i < count;i++){
    getList().add(arrayid[i]);
       }
	return true;
      }else return false;
}
public static List<Integer> getList() {
	return list;
}
public static void setList(List<Integer> list) {
	UnregisteredId.list = list;
}
}
