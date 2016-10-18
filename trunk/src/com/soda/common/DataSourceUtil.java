package com.soda.common;

import org.apache.commons.dbcp.BasicDataSource;

import java.io.Serializable;

/**
 * Created by kcao on 2016/7/8.
 */
public class DataSourceUtil implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static BasicDataSource dataSource=new BasicDataSource();

    static{
        dataSource.setUrl(ConstantsUtil.DB_URL);
        dataSource.setDriverClassName(ConstantsUtil.DRIVERCLASSNAME);
        dataSource.setUsername(ConstantsUtil.DB_USERNAME);
        dataSource.setPassword(ConstantsUtil.DB_PASSWORD);
        dataSource.setMaxActive(ConstantsUtil.maxActive);
        dataSource.setMaxIdle(ConstantsUtil.maxIdle);
        dataSource.setMaxWait(ConstantsUtil.maxWait);
        dataSource.setRemoveAbandoned(ConstantsUtil.removeAbandoned);
        dataSource.setRemoveAbandonedTimeout(ConstantsUtil.removeAbandonedTimeout);
    }

}
