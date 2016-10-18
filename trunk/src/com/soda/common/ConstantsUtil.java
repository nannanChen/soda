package com.soda.common;

import java.io.Serializable;

/**
 * Created by kcao on 2016/3/11.
 */
public final class ConstantsUtil implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	   //ZKµÿ÷∑
    public static final String ZOOKEEPER_ADDRESS="zk1:2181,zk2:2181,zk3:2181";
    //hdfsµÿ÷∑
    public static final String HDFS_ADDRESS="hdfs://masters";
    
    public static final String POINT_DETAIL="point_detail_yarn";

    public static final String POINT_MAP_INFO="point_map_info";
	
	public static final String DRIVERCLASSNAME="com.mysql.jdbc.Driver";
    public static final String DB_URL="jdbc:mysql://192.168.20.92:3306/soda?useUnicode=true&characterEncoding=utf-8";
    public static final String DB_USERNAME="root";
    public static final String DB_PASSWORD="admin123!!";
    public static final int maxActive=30;
    public static final int maxIdle=1;
    public static final int maxWait=1000;
    public static final boolean removeAbandoned=true;
    public static final int removeAbandonedTimeout=180;


    private ConstantsUtil() {} // prevent instantiation

}
