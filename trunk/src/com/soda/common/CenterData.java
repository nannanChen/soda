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
	        centerDataMap.put("0","其它");
	        centerDataMap.put("1","购物达人");
	        centerDataMap.put("2","交际达人");
	        centerDataMap.put("3","游戏达人");
	        centerDataMap.put("4","时政达人");
	        centerDataMap.put("5","文娱达人");
	        centerDataMap.put("6","IT达人");
	        centerDataMap.put("7","健身达人");
	        centerDataMap.put("8","旅游达人");
	        centerDataMap.put("9","金融达人");
	    }


    public static void main(String[] args) throws Exception {
        for(Map.Entry<String,String> entry:centerDataMap.entrySet()){
            System.out.println(entry.getKey()+":"+entry.getValue());
        }
    }

}
