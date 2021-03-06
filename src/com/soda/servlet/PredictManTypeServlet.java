package com.soda.servlet;
import com.soda.common.CenterData;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
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

import com.soda.common.CenterData;
import com.soda.servlet.base.BaseServlet;

public class PredictManTypeServlet extends BaseServlet {
	
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
		String ty=request.getParameter("type");
		String type = CenterData.centerDataMap.get(ty);

		System.out.println(new Date()+" PredictManTypeServlet date="+date+" fromHour="+fromHour+" toHour="+toHour+" tradingArea="+tradingArea);
		if(checkParam(json,date,fromHour,toHour,tradingArea,ty)){
			Integer fromH=Integer.parseInt(fromHour);
			Integer toH=Integer.parseInt(toHour);
			if(fromH.intValue()==toH.intValue()){
				json=queryStatic(json,date,fromH,toH,tradingAreaMap.get(tradingArea).toString(),ty);
			}else{
				List<Integer> gridIndexs=new ArrayList<Integer>();
				gridIndexs.add(tradingAreaMap.get(tradingArea));
				Integer currentHour=toH;
//				do{
					json=queryDynamic(json,date,fromH,toH,tradingArea,ty);
//					currentHour--;
//					System.out.println("fromH="+fromH+" currentHour="+currentHour+" fromH<=currentHour="+(fromH<=currentHour));
//				}while(fromH<=currentHour&&gridIndexs.size()!=0);
				JSONArray dataList=null;
		        try{
		        	dataList=json.getJSONArray("dataList");
			        System.out.println(new Date()+" PredictManTypeServlet dataList="+dataList.length());
		        }catch(JSONException jsonE){
			        System.out.println(new Date()+" PredictManTypeServlet dataList="+dataList);
		        }
			}
//			json=queryGraph(json,date,fromH,toH);
		}
		response.getOutputStream().write(json.toString().getBytes("UTF-8"));
	}
	

	
	private JSONObject queryDynamic(JSONObject json,String date,Integer fromHour,Integer toHour,String ind,String type ){
		System.out.println(new Date()+" PredictManTypeServlet queryDynamic date="+date+" fromHour="+fromHour+" toHour="+toHour+" ind="+ind);
		Connection connection=null;
		PreparedStatement pstmt=null;
		ResultSet resultSet=null;
		try{
			connection=dataSource.getConnection();
	        pstmt = connection.prepareStatement("SELECT SUM(COUNT) AS sum1 FROM  `predictManTpye` WHERE DATE =? AND ind=?  AND type=? AND HOUR "+queryWhereInByHour(fromHour,toHour));
	        pstmt.setString(1, date);
	        pstmt.setString(2, tradingAreaMap.get(ind).toString());
	        pstmt.setString(3, type);
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
//	        System.out.println(new Date()+" PredictManTypeServlet currentHour="+currentHour+" dataList="+dataList.length());
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
	

	private JSONObject queryStatic(JSONObject json,String date,Integer fromH,Integer toH,String tradingArea,String type){
		Connection connection=null;
		PreparedStatement pstmt=null;
		ResultSet resultSet=null;
		System.out.println("date:"+date+"fromH:"+fromH+"traingArea:"+tradingArea+"type:"+type);
		try{
			connection=dataSource.getConnection();
	        pstmt = connection.prepareStatement("SELECT COUNT,HOUR,DATE,ind FROM `predictManTpye` WHERE DATE =? AND ind=? AND HOUR = ? AND TYPE = ?");
	        pstmt.setString(1, date);
	        pstmt.setString(2, tradingArea);
	        pstmt.setInt(3, fromH);
	        pstmt.setString(4, type);
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
	        	System.out.println("---------"+count);
	        	
//	        	data.put("from_index",from_index);

	        	Map<String,Integer> warnAverage=(Map<String,Integer>)this.getServletContext().getAttribute("warn_Average");
	    		
	        	int ind = resultSet.getInt("ind");
	        	data.put("ind", ind);
	        	dataList.put(data);
	        }
			System.out.println(new Date()+" PredictManTypeServlet dataList="+dataList.length());
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
	
	private boolean checkParam(JSONObject json,String date,String fromHour,String toHour,String tradingArea,String type){
		if(StringUtils.isNotBlank(date)&&StringUtils.isNotBlank(fromHour)&&StringUtils.isNotBlank(toHour)){
			if(date.length()==8){
				try{
					Integer fromH=Integer.parseInt(fromHour);
					Integer toH=Integer.parseInt(toHour);
					if(fromH>=0&&fromH<=23&&toH>=0&&toH<=23){
						if(fromH<=toH){
							if(fromH!=toH){
								if("1".equals(tradingArea)||"2".equals(tradingArea)||"3".equals(tradingArea)){
									if("0".equals(type)||"1".equals(type)||"2".equals(type)||
											"3".equals(type)||"4".equals(type)||"5".equals(type)||
											"6".equals(type)||"7".equals(type)||"8".equals(type)||
											"9".equals(type)
											){
										return true;
									}else{
										json.put("status", "error");
										json.put("message", "取值范围为0-9");
									}
									
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
