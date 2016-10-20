package com.soda.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
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
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.soda.common.DataSourceUtil;
import com.soda.common.GridDivide;
import com.soda.common.Point;

public class PredictDataServlet extends HttpServlet {
	
    public static Map<String,Integer> tradingAreaMap=new HashMap<String,Integer>();
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private BasicDataSource dataSource=DataSourceUtil.dataSource;

	public PredictDataServlet() {
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
		response.setContentType("text/json; charset=UTF-8");  
		JSONObject json=new JSONObject();
		String date=request.getParameter("date");
		String fromHour=request.getParameter("fromHour");
		String toHour=request.getParameter("toHour");
		String tradingArea=request.getParameter("tradingArea");

		System.out.println(new Date()+" PredictData date="+date+" fromHour="+fromHour+" toHour="+toHour+" tradingArea="+tradingArea);
		if(checkParam(json,date,fromHour,toHour,tradingArea)){
			Integer fromH=Integer.parseInt(fromHour);
			Integer toH=Integer.parseInt(toHour);
			if(fromH.intValue()==toH.intValue()){
				json=queryStatic(json,date,fromH,toH,tradingAreaMap.get(tradingArea).toString());
			}else{
				List<Integer> gridIndexs=new ArrayList<Integer>();
				gridIndexs.add(tradingAreaMap.get(tradingArea));
				Integer currentHour=toH;
//				do{
					json=queryDynamic(json,date,fromH,toH,tradingArea);
//					currentHour--;
//					System.out.println("fromH="+fromH+" currentHour="+currentHour+" fromH<=currentHour="+(fromH<=currentHour));
//				}while(fromH<=currentHour&&gridIndexs.size()!=0);
				JSONArray dataList=null;
		        try{
		        	dataList=json.getJSONArray("dataList");
			        System.out.println(new Date()+" PredictData dataList="+dataList.length());
		        }catch(JSONException jsonE){
			        System.out.println(new Date()+" PredictData dataList="+dataList);
		        }
			}
//			json=queryGraph(json,date,fromH,toH);
		}
		response.getOutputStream().write(json.toString().getBytes("UTF-8"));
	}
	

	
	public JSONObject queryDynamic(JSONObject json,String date,Integer fromHour,Integer toHour,String ind ){
		System.out.println(new Date()+" PredictData queryDynamic date="+date+" fromHour="+fromHour+" toHour="+toHour+" ind="+ind);
		Connection connection=null;
		PreparedStatement pstmt=null;
		ResultSet resultSet=null;
		try{
			connection=dataSource.getConnection();
	        pstmt = connection.prepareStatement("SELECT SUM(COUNT) AS sum1 FROM  `predictData` WHERE DATE =? AND ind=?  AND HOUR "+queryWhereInByHour(fromHour,toHour));
	        pstmt.setString(1, date);
	        pstmt.setString(2, tradingAreaMap.get(ind).toString());
	        resultSet = pstmt.executeQuery();
	        json.put("status", "OK");
	        json.put("ResultType", "dynamic");
	        JSONArray dataList;
	        try{
	        	dataList=json.getJSONArray("dataList");
	        }catch(JSONException jsonE){
	        	dataList=new JSONArray();
	        }
	        
	        ResultSetMetaData meta=resultSet.getMetaData();
	        while(resultSet.next()){
	    		JSONObject data=new JSONObject();
	    		data.put("count",resultSet.getString("sum1"));
	        	dataList.put(data);
	        }
//	        System.out.println(new Date()+" PredictData currentHour="+currentHour+" dataList="+dataList.length());
	        json.put("dataList",dataList);
		}catch(Exception e){
			e.printStackTrace();
			json.put("status", "error");
			json.put("message", "查询失败！");
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
	
	
	

	public JSONObject queryStatic(JSONObject json,String date,Integer fromH,Integer toH,String tradingArea){
		Connection connection=null;
		PreparedStatement pstmt=null;
		ResultSet resultSet=null;
		try{
			connection=dataSource.getConnection();
	        pstmt = connection.prepareStatement("SELECT COUNT,HOUR,DATE,ind FROM `predictData` WHERE DATE =? AND ind=? AND HOUR = ?");
	        pstmt.setString(1, date);
	        pstmt.setString(2, tradingArea);
	        pstmt.setInt(3, fromH);
	        resultSet = pstmt.executeQuery();
	        json.put("status", "OK");
	        json.put("ResultType", "static");
        	JSONArray dataList=new JSONArray();
	        while(resultSet.next()){
	    		JSONObject data=new JSONObject();
	    		data.put("date",resultSet.getString("DATE"));
	    		
	    		int hour=resultSet.getInt("HOUR");
	    		data.put("hour",hour);
	 
	        	int count=resultSet.getInt("COUNT");
	        	data.put("count",count);
	        	
//	        	data.put("from_index",from_index);

	        	Map<String,Integer> warnAverage=(Map<String,Integer>)this.getServletContext().getAttribute("warn_Average");
	    		
	        	int ind = resultSet.getInt("ind");
	        	data.put("ind", ind);
	        	dataList.put(data);
	        }
			System.out.println(new Date()+" PredictData dataList="+dataList.length());
	        json.put("dataList",dataList);
		}catch(Exception e){
			e.printStackTrace();
			json.put("status", "error");
			json.put("message", "查询失败！");
		}finally{
			release(connection,pstmt,resultSet);
		}
		return json;
	}
	
	public boolean checkParam(JSONObject json,String date,String fromHour,String toHour,String tradingArea){
		if(StringUtils.isNotBlank(date)&&StringUtils.isNotBlank(fromHour)&&StringUtils.isNotBlank(toHour)){
			if(date.length()==8){
				try{
					Integer fromH=Integer.parseInt(fromHour);
					Integer toH=Integer.parseInt(toHour);
					if(fromH>=0&&fromH<=23&&toH>=0&&toH<=23){
						if(fromH<=toH){
							if(fromH!=toH){
								if("1".equals(tradingArea)||"2".equals(tradingArea)||"3".equals(tradingArea)){
									return true;
								}else{
									json.put("status", "error");
									json.put("message", "tradingArea，商圈取值范围【1(南京东路)，2(徐家汇)，3(莘庄)】");
								}
							}else{
								return true;
							}
						}else{
							json.put("status", "error");
							json.put("message", "fromHour必须小于toHour");
						}
					}else{
						json.put("status", "error");
						json.put("message", "hour取值范围[0-23]");
					}
				}catch(Exception e){
					json.put("status", "error");
					json.put("message", "hour必须为整型数字");
				}
			}else{
				json.put("status", "error");
				json.put("message", "date参数格式为8位字符：20160927");
			}
		}else{
			json.put("status", "error");
			json.put("message", "参数不能为空[date,fromHour,toHour]");

		}
		return false;
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
