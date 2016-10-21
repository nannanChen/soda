package com.soda.servlet.base;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.dbcp.BasicDataSource;
import org.json.JSONArray;
import org.json.JSONObject;
import com.soda.common.DataSourceUtil;

public class BaseServlet extends HttpServlet {
	
    public static Map<String,Integer> tradingAreaMap=new HashMap<String,Integer>();
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public BasicDataSource dataSource=DataSourceUtil.dataSource;

	public BaseServlet() {
		super();
	}

	public void destroy() {
		super.destroy(); // Just puts "destroy" string in log
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request,response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
	}
	
	
	public int queryTotalByHour(String date,Integer hour,Integer tradingArea,String classType){
		System.out.println(new Date()+" BaseServlet queryTotalByHour date="+date+" hour="+hour+" tradingArea="+tradingArea+" classType="+classType);
		String sql="SELECT t1.DATE,t1.HOUR,t1.FROM_INDEX,SUM(t2.COUNT) AS total FROM grid_from_to_num1 t1 JOIN grid_people_group1 t2 ON t1.grid_people_group_id=t2.grid_people_group_id WHERE t1.DATE=? AND t1.HOUR=? AND t2.type=?  AND t1.COUNT>1000 AND t1.FROM_INDEX=?";
		List<Object> params=new ArrayList<Object>();
		params.add(date);
		params.add(hour);
		params.add(classType);
		params.add(tradingArea);
		return queryCommonTotalByHour(params,sql);
	}
	
	public int queryTotalByHour(String date,Integer hour,Integer tradingArea){
		System.out.println(new Date()+" BaseServlet queryTotalByHour date="+date+" hour="+hour+" tradingArea="+tradingArea);
		String sql="SELECT SUM(COUNT) AS total FROM `grid_from_to_num1` WHERE DATE=? AND HOUR=? AND COUNT>1000 AND FROM_INDEX=?";
		List<Object> params=new ArrayList<Object>();
		params.add(date);
		params.add(hour);
		params.add(tradingArea);
		return queryCommonTotalByHour(params,sql);
	}
	
	private int queryCommonTotalByHour(List<Object> params,String querySql){
		System.out.println(new Date()+" BaseServlet queryCommonTotalByHour querySql="+querySql);
		Connection connection=null;
		PreparedStatement pstmt=null;
		ResultSet resultSet=null;
		try{
			connection=dataSource.getConnection();
	        pstmt = connection.prepareStatement(querySql);
	        for(int i=0;i<params.size();i++){
		        pstmt.setObject(i+1, params.get(i));
	        }
	        resultSet = pstmt.executeQuery();
	        while(resultSet.next()){
	        	int total=resultSet.getInt("total");
	        	return total;
	        }
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			release(connection,pstmt,resultSet);
		}
		return 0;
	}
	
	public JSONObject queryGraph(JSONObject json,String date,Integer fromH,Integer toH){
		System.out.println(new Date()+" BaseServlet queryGraph date="+date+" fromH="+fromH+" toH="+toH);
		List<Object> params=new ArrayList<Object>();
		params.add(date);
		String sql="SELECT SUM(COUNT) AS NUM FROM `grid_from_to_num1` WHERE DATE=? AND COUNT>1000 AND HOUR "+queryWhereInByHour(fromH,toH);
		return this.queryCommonGraph(json, params,sql);
	}
	
	public JSONObject queryGraph(JSONObject json,String date,Integer fromH,Integer toH,Integer classTypeI){
		System.out.println(new Date()+" BaseServlet queryGraph date="+date+" fromH="+fromH+" toH="+toH+" classTypeI="+classTypeI);
		List<Object> params=new ArrayList<Object>();
		params.add(classTypeI);
		params.add(date);
		String sql="SELECT SUM(t2.count) AS NUM FROM grid_from_to_num1 t1 JOIN grid_people_group1 t2 ON t1.grid_people_group_id=t2.grid_people_group_id WHERE t2.type=? AND t1.COUNT>1000 AND t1.DATE=? AND t1.HOUR "+queryWhereInByHour(fromH,toH);
		return this.queryCommonGraph(json, params,sql);
	}
	
	private JSONObject queryCommonGraph(JSONObject json,List<Object> params,String querySql){
		System.out.println(new Date()+" BaseServlet queryCommonGraph params="+params+" querySql="+querySql);
		Connection connection=null;
		PreparedStatement pstmt=null;
		ResultSet resultSet=null;
		try{
			connection=dataSource.getConnection();
	        pstmt = connection.prepareStatement(querySql);
	        for(int i=0;i<params.size();i++){
		        pstmt.setObject(i+1, params.get(i));
	        }
	        resultSet = pstmt.executeQuery();
        	JSONArray graphData=new JSONArray();
	        if(resultSet.next()){
	    		int num=resultSet.getInt("NUM");
	    		JSONObject czc=new JSONObject();
	    		czc.put("name", "出租车");
	    		czc.put("value", (int) (num*0.03));
	    		graphData.put(czc);
	    		
	    		JSONObject gj=new JSONObject();
	    		gj.put("name", "公交");
	    		gj.put("value", (int) (num*0.15));
	    		graphData.put(gj);
	    		
	    		JSONObject dt=new JSONObject();
	    		dt.put("name", "地铁");
	    		dt.put("value", (int) (num*0.69));
	    		graphData.put(dt);
	    		
	    		JSONObject gjToDt=new JSONObject();
	    		gjToDt.put("name", "公交->地铁");
	    		gjToDt.put("value", (int) (num*0.05));
	    		graphData.put(gjToDt);
	    		
	    		JSONObject dtToGj=new JSONObject();
	    		dtToGj.put("name", "地铁->公交");
	    		dtToGj.put("value", (int) (num*0.07));
	    		graphData.put(dtToGj);
	    		
	    		JSONObject other=new JSONObject();
	    		other.put("name", "其他");
	    		other.put("value", (int) (num*0.01));
	    		graphData.put(other);
	        }
	        System.out.println(new Date()+" GridFromToNum2 graphData="+graphData);
	        json.put("graphData",graphData);
		}catch(Exception e){
			e.printStackTrace();
			json.put("GraphStatus", "Graph Error");
			json.put("GraphMessage", "Graph查询失败！");
		}finally{
			release(connection,pstmt,resultSet);
		}
		return json;
	}
	
	//IN(1,2,3)
	public String queryWhereInByHour(Integer fromH,Integer toH){
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
	
	public String queryWhereInByIndex(List<Integer> gridIndexs){
		StringBuffer in=new StringBuffer("IN(");
		for(int i=0;i<gridIndexs.size();i++){
			if(i==gridIndexs.size()-1){
				in.append(gridIndexs.get(i).intValue()+")");
			}else{
				in.append(gridIndexs.get(i).intValue()+",");
			}
		}
		System.out.println(new Date()+" queryWhereInByIndex in="+in.toString());
		return in.toString();
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

	public void init() throws ServletException {
//		 addressMap.put("nanJingDong",indexMap.get("157"));
//	     addressMap.put("xuJiaHui",indexMap.get("180"));
//	     addressMap.put("xinZhuang",indexMap.get("226"));
		tradingAreaMap.put("1",157);
		tradingAreaMap.put("2",180);
		tradingAreaMap.put("3",226);
	}
}
