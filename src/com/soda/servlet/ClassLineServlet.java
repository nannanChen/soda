package com.soda.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

public class ClassLineServlet extends BaseServlet {
	
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
		String classType=request.getParameter("classType");
		System.out.println(new Date()+" ClassLineServlet date="+date+" fromHour="+fromHour+" toHour="+toHour+" tradingArea="+tradingArea+" classType="+classType);
		if(checkParam(json,date,fromHour,toHour,tradingArea,classType)){
			Integer fromH=Integer.parseInt(fromHour);
			Integer toH=Integer.parseInt(toHour);
			Integer classTypeI=Integer.parseInt(classType);
			if(fromH.intValue()==toH.intValue()){
		        System.out.println(new Date()+" ClassLineServlet 时间一样，静态参数，无轨迹");
		        json.put("status", "OK");
		        json.put("message", "静态参数，无轨迹");
			}else{
				List<Integer> gridIndexs=new ArrayList<Integer>();
				gridIndexs.add(tradingAreaMap.get(tradingArea));
				Integer currentHour=toH;
				do{
					json=queryDynamic(json,date,currentHour,gridIndexs,classTypeI);
					currentHour--;
					System.out.println("fromH="+fromH+" currentHour="+currentHour+" fromH<=currentHour="+(fromH<=currentHour));
				}while(fromH<=currentHour&&gridIndexs.size()!=0);
				JSONArray dataList=null;
		        try{
		        	dataList=json.getJSONArray("dataList");
			        System.out.println(new Date()+" ClassLineServlet dataList="+dataList.length());
		        }catch(JSONException jsonE){
			        System.out.println(new Date()+" ClassLineServlet dataList="+dataList);
		        }
			}
			json=queryGraph(json,date,fromH,toH,classTypeI);
		}
		response.getOutputStream().write(json.toString().getBytes("UTF-8"));
	}
	
	private JSONObject queryDynamic(JSONObject json,String date,Integer currentHour,List<Integer> gridIndexs,Integer classType){
		System.out.println(new Date()+" ClassLineServlet queryDynamic date="+date+" currentHour="+currentHour+" gridIndexs="+gridIndexs);
		Connection connection=null;
		PreparedStatement pstmt=null;
		ResultSet resultSet=null;
		try{
			connection=dataSource.getConnection();
	        pstmt = connection.prepareStatement("SELECT t1.date,t1.hour,t1.from_index,t1.to_index,t2.type,t2.count FROM grid_from_to_num1 t1 JOIN grid_people_group1 t2 ON t1.grid_people_group_id=t2.grid_people_group_id WHERE t2.type=? AND t1.DATE=? AND t1.HOUR=? AND t1.COUNT>1000 AND t1.to_index!=(-1) AND t1.to_index!=t1.from_index AND t1.to_index "+queryWhereInByIndex(gridIndexs));
	        pstmt.setInt(1, classType);
	        pstmt.setString(2, date);
	        pstmt.setInt(3, currentHour);
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
	        	data.put("type",resultSet.getString("type"));
	        	dataList.put(data);
	        }
	        System.out.println(new Date()+" ClassLineServlet currentHour="+currentHour+" dataList="+dataList.length());
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
	
	private boolean checkParam(JSONObject json,String date,String fromHour,String toHour,String tradingArea,String classType){
		if(StringUtils.isNotBlank(date)&&StringUtils.isNotBlank(fromHour)&&StringUtils.isNotBlank(toHour)&&StringUtils.isNotBlank(classType)){
			if(date.length()==8){
				try{
					Integer fromH=Integer.parseInt(fromHour);
					Integer toH=Integer.parseInt(toHour);
					Integer classTypeI=Integer.parseInt(classType);
					if(fromH>=0&&fromH<=23&&toH>=0&&toH<=23){
						if(fromH<=toH){
							if(classTypeI>=0&&classTypeI<=9){
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
								json.put("message", "classType取值范围[0-9]");
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
					json.put("message", "classType或hour必须为整型数字");
				}
			}else{
				json.put("status", "error");
				json.put("message", "date参数格式为8位字符：20160927");
			}
		}else{
			json.put("status", "error");
			json.put("message", "参数不能为空[date,fromHour,toHour,classType]");

		}
		return false;
	}
}
