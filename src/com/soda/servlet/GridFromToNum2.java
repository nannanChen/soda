package com.soda.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.soda.common.GridDivide;
import com.soda.common.Point;
import com.soda.servlet.base.BaseServlet;

public class GridFromToNum2 extends BaseServlet {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/json; charset=UTF-8");  
		JSONObject json=new JSONObject();
		String date=request.getParameter("date");
		String fromHour=request.getParameter("fromHour");
		String toHour=request.getParameter("toHour");
		String tradingArea=request.getParameter("tradingArea");

		System.out.println(new Date()+" GridFromToNum2 date="+date+" fromHour="+fromHour+" toHour="+toHour+" tradingArea="+tradingArea);
		if(checkParam(json,date,fromHour,toHour,tradingArea)){
			Integer fromH=Integer.parseInt(fromHour);
			Integer toH=Integer.parseInt(toHour);
			if(fromH.intValue()==toH.intValue()){
				json=queryStatic(json,date,fromH,toH,tradingArea);
				int total=queryTotalByHour(date,fromH,tradingAreaMap.get(tradingArea));
		        json.put("total",total);
			}else{
				List<Integer> gridIndexs=new ArrayList<Integer>();
				gridIndexs.add(tradingAreaMap.get(tradingArea));
				Integer currentHour=toH;
				int total=0;
				do{
					json=queryDynamic(json,date,currentHour,gridIndexs);
					int count=queryTotalByHour(date,currentHour,tradingAreaMap.get(tradingArea));
					total+=count;
					currentHour--;
					System.out.println("fromH="+fromH+" currentHour="+currentHour+" fromH<=currentHour="+(fromH<=currentHour)+" total="+total);
				}while(fromH<=currentHour&&gridIndexs.size()!=0);
		        json.put("total",total);
				JSONArray dataList=null;
		        try{
		        	dataList=json.getJSONArray("dataList");
			        System.out.println(new Date()+" GridFromToNum2 dataList="+dataList.length());
		        }catch(JSONException jsonE){
			        System.out.println(new Date()+" GridFromToNum2 dataList="+dataList);
		        }
			}
			json=queryGraph(json,date,fromH,toH);
		}
		response.getOutputStream().write(json.toString().getBytes("UTF-8"));
	}
	
	private JSONObject queryDynamic(JSONObject json,String date,Integer currentHour,List<Integer> gridIndexs){
		System.out.println(new Date()+" GridFromToNum2 queryDynamic date="+date+" currentHour="+currentHour+" gridIndexs="+gridIndexs);
		Connection connection=null;
		PreparedStatement pstmt=null;
		ResultSet resultSet=null;
		try{
			connection=dataSource.getConnection();
	        pstmt = connection.prepareStatement("SELECT * FROM `grid_from_to_num1` WHERE DATE=? AND HOUR=? AND COUNT>1000 AND to_index!=(-1) AND to_index!=from_index AND to_index "+queryWhereInByIndex(gridIndexs));
	        pstmt.setString(1, date);
	        pstmt.setInt(2, currentHour);
	        resultSet = pstmt.executeQuery();
	        json.put("status", "OK");
	        json.put("ResultType", "dynamic");
	        gridIndexs.clear();
	        JSONArray dataList;
	        try{
	        	dataList=json.getJSONArray("dataList");
	        }catch(JSONException jsonE){
	        	dataList=new JSONArray();
	        }
	        while(resultSet.next()){
	    		JSONObject data=new JSONObject();
	    		data.put("date",resultSet.getString("date"));
	    		data.put("hour",resultSet.getString("hour"));
	    		
	    		Point fromPoint=GridDivide.indexMap.get(resultSet.getString("from_index"));
	        	data.put("from_longitude",fromPoint.x);
	        	data.put("from_latitude",fromPoint.y);
	        	
	        	Point toPoint=GridDivide.indexMap.get(resultSet.getString("to_index"));
	        	data.put("to_longitude",toPoint.x);
	        	data.put("to_latitude",toPoint.y);
	        	
	        	gridIndexs.add(resultSet.getInt("from_index"));

	        	data.put("count",resultSet.getString("count"));
	        	data.put("grid_people_group_id",resultSet.getString("grid_people_group_id"));
	        	dataList.put(data);
	        }
	        System.out.println(new Date()+" GridFromToNum2 currentHour="+currentHour+" dataList="+dataList.length());
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
	
	
	private JSONObject queryStatic(JSONObject json,String date,Integer fromH,Integer toH,String tradingArea){
		Connection connection=null;
		PreparedStatement pstmt=null;
		ResultSet resultSet=null;
		try{
			connection=dataSource.getConnection();
	        pstmt = connection.prepareStatement("SELECT DATE,HOUR,FROM_INDEX,SUM(COUNT) AS COUNT,GROUP_CONCAT(grid_people_group_id) AS grid_people_group_id FROM `grid_from_to_num1` WHERE DATE=? AND HOUR=? AND COUNT>1000 GROUP BY FROM_INDEX");
	        pstmt.setString(1, date);
	        pstmt.setInt(2, fromH);
	        resultSet = pstmt.executeQuery();
	        json.put("status", "OK");
	        json.put("ResultType", "static");
        	JSONArray dataList=new JSONArray();
	        while(resultSet.next()){
	    		JSONObject data=new JSONObject();
	    		data.put("date",resultSet.getString("DATE"));
	    		
	    		int hour=resultSet.getInt("HOUR");
	    		data.put("hour",hour);
	    		
	    		String from_index=resultSet.getString("FROM_INDEX");
	    		Point point=GridDivide.indexMap.get(from_index);
	        	data.put("longitude",point.x);
	        	data.put("latitude",point.y);
	        	
	        	int count=resultSet.getInt("COUNT");
	        	data.put("count",count);
	        	
	        	Map<String,Integer> warnAverage=(Map<String,Integer>)this.getServletContext().getAttribute("warn_Average");
	    		int avg=warnAverage.get(from_index+"_"+hour);
	        	if(avg<10000){
		        	data.put("warn",false);
	        	}else{
	        		if(count>avg*1.1){
			        	data.put("warn",true);
	        		}else{
	        			data.put("warn",false);
	        		}
	        	}
	        	
	        	data.put("grid_people_group_id",resultSet.getString("grid_people_group_id"));
	        	dataList.put(data);
	        }
			System.out.println(new Date()+" GridFromToNum2 dataList="+dataList.length());
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
	
	private boolean checkParam(JSONObject json,String date,String fromHour,String toHour,String tradingArea){
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
}
