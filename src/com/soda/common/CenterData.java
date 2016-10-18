package com.soda.common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kcao on 2016/10/12.
 */
public class CenterData implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static Map<String,String> centerDataMap=new HashMap<String,String>();

	 static {
	        centerDataMap.put("0","����");
	        centerDataMap.put("1","�������");
	        centerDataMap.put("2","���ʴ���");
	        centerDataMap.put("3","��Ϸ����");
	        centerDataMap.put("4","ʱ������");
	        centerDataMap.put("5","�������");
	        centerDataMap.put("6","IT����");
	        centerDataMap.put("7","�������");
	        centerDataMap.put("8","���δ���");
	        centerDataMap.put("9","���ڴ���");
	    }


    public static void main(String[] args) throws Exception {
        for(Map.Entry<String,String> entry:centerDataMap.entrySet()){
            System.out.println(entry.getKey()+":"+entry.getValue());
        }
    }

}
