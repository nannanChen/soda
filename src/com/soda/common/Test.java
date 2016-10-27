package com.soda.common;

import java.util.Date;

public class Test {

	public static String queryWhereInByHour(Integer fromH,Integer toH){
		StringBuffer in=new StringBuffer("IN(");
		for(int i=fromH;i<=toH;i++){
			if(i==toH){
				in.append(i+")");
			}else{
				in.append(i+",");
			}
		}
		System.out.println(new Date()+" queryWhereInByHour in="+in.toString());
		return in.toString();
	}
	public static void main(String[] args) {
		queryWhereInByHour(1, 4);
	}
}
