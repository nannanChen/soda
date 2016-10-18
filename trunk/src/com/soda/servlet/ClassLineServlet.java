package com.soda.servlet;

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
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.soda.common.DataSourceUtil;
import com.soda.common.GridDivide;
import com.soda.common.Point;

public class ClassLineServlet extends HttpServlet {
	
    public static Map<String,Integer> tradingAreaMap=new HashMap<String,Integer>();
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private BasicDataSource dataSource=DataSourceUtil.dataSource;

	public ClassLineServlet() {
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
	
	public JSONObject queryDynamic(JSONObject json,String date,Integer currentHour,List<Integer> gridIndexs,Integer classType){
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
	
	
	public JSONObject queryGraph(JSONObject json,String date,Integer fromH,Integer toH,Integer classTypeI){
		System.out.println(new Date()+" ClassLineServlet queryGraph date="+date+" fromH="+fromH+" toH="+toH+" classTypeI="+classTypeI);
		Connection connection=null;
		PreparedStatement pstmt=null;
		ResultSet resultSet=null;
		try{
			connection=dataSource.getConnection();
	        pstmt = connection.prepareStatement("SELECT SUM(t2.count) AS NUM FROM grid_from_to_num1 t1 JOIN grid_people_group1 t2 ON t1.grid_people_group_id=t2.grid_people_group_id WHERE t2.type=? AND t1.DATE=? AND t1.HOUR "+queryWhereInByHour(fromH,toH));
	        pstmt.setInt(1, classTypeI);
	        pstmt.setString(2, date);
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
	    		dt.put("value", (int) (num*0.7));
	    		graphData.put(dt);
	    		
	    		JSONObject gjToDt=new JSONObject();
	    		gjToDt.put("name", "公交->地铁");
	    		gjToDt.put("value", (int) (num*0.05));
	    		graphData.put(gjToDt);
	    		
	    		JSONObject dtToGj=new JSONObject();
	    		dtToGj.put("name", "地铁->公交");
	    		dtToGj.put("value", (int) (num*0.07));
	    		graphData.put(dtToGj);
	        }
	        System.out.println(new Date()+" ClassLineServlet graphData="+graphData);
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
	
	public boolean checkParam(JSONObject json,String date,String fromHour,String toHour,String tradingArea,String classType){
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
		tradingAreaMap.put("1",157);
		tradingAreaMap.put("2",180);
		tradingAreaMap.put("3",226);
	}

}
