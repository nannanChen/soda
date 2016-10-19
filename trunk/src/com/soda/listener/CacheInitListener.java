package com.soda.listener;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.commons.dbcp.BasicDataSource;
import com.soda.common.DataSourceUtil;

/**
 * Created by kcao on 2016/10/19.
 */
public class CacheInitListener implements ServletContextListener {
	
	private BasicDataSource dataSource=DataSourceUtil.dataSource;
	
	@Override
	public void contextInitialized(ServletContextEvent event) {
		System.out.println("=============soda web启动=============");  
		Connection connection=null;
		PreparedStatement pstmt=null;
		ResultSet resultSet=null;
		Map<String,Integer> warnAverage=new HashMap<String,Integer>();
		try{
			connection=dataSource.getConnection();
	        pstmt = connection.prepareStatement("SELECT * FROM warn_average");
	        resultSet = pstmt.executeQuery();
	        while(resultSet.next()){
	        	int grid_index=resultSet.getInt("grid_index");
	        	int hour=resultSet.getInt("hour");
	        	int avg=resultSet.getInt("avg");
	        	warnAverage.put(grid_index+"_"+hour, avg);
	        }
	        System.out.println("============= CacheInitListener 缓存"+warnAverage.size()+"个平均值数据！=============");
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			release(connection,pstmt,resultSet);
		}
		event.getServletContext().setAttribute("warn_Average", warnAverage);
	}

	public void release(Connection connection, PreparedStatement pstmt, ResultSet resultSet) {
        if(resultSet!=null){
            try {
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if(pstmt!=null){
            try {
                pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if(connection!=null){
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
	@Override
	public void contextDestroyed(ServletContextEvent event) {
		System.out.println("=============soda web停止=================");  
	}

}
