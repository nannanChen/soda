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
import org.json.JSONObject;

import com.soda.common.CenterData;
import com.soda.servlet.base.BaseServlet;

public class GridPeopleGroup2 extends BaseServlet {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/json; charset=UTF-8");  
		JSONObject json=new JSONObject();
		String groupId=request.getParameter("groupId");
		String type=request.getParameter("type");
		System.out.println(new Date()+" GridPeopleGroup2 groupId="+groupId);
		if(checkParam(json,groupId)){
			List<Object> params=new ArrayList<Object>();
			String sql="SELECT SUM(COUNT) AS count,type,GROUP_CONCAT(grid_people_group_id) AS grid_people_group_id FROM `grid_people_group1` WHERE grid_people_group_id "+queryWhereInByGroupId(groupId.split(","))+" GROUP BY TYPE";
			if(StringUtils.isNotBlank(type)){
				sql="SELECT SUM(COUNT) AS count,type,GROUP_CONCAT(grid_people_group_id) AS grid_people_group_id FROM `grid_people_group1` WHERE grid_people_group_id "+queryWhereInByGroupId(groupId.split(","))+" and type=? GROUP BY TYPE";
				params.add(type);
			}
			json=queryPeopleGroup(json,params,sql);
		}
		response.getOutputStream().write(json.toString().getBytes("UTF-8"));
	}
	
	
	private JSONObject queryPeopleGroup(JSONObject json,List<Object> params,String sql){
		Connection connection=null;
		PreparedStatement pstmt=null;
		ResultSet resultSet=null;
		try{
			connection=dataSource.getConnection();
	        pstmt = connection.prepareStatement(sql);
	        for(int i=0;i<params.size();i++){
		        pstmt.setObject(i+1, params.get(i));
	        }
	        resultSet = pstmt.executeQuery();
	        json.put("status", "OK");
        	JSONArray dataList=new JSONArray();
	        while(resultSet.next()){
	    		JSONObject data=new JSONObject();
	    		String groupId=resultSet.getString("grid_people_group_id");
	    		data.put("grid_people_group_id",groupId);
	    		String type=resultSet.getString("type");
	    		data.put("type",CenterData.centerDataMap.get(type));
	        	data.put("count",resultSet.getString("count"));
	        	data.put("IMEIS", this.queryImeis(groupId,type));
	        	dataList.put(data);
	        }
			System.out.println(new Date()+" GridPeopleGroup2 dataList="+dataList.length());
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
	
	private JSONArray queryImeis(String groupId,String type){
		JSONArray imeis=new JSONArray();
		Connection connection=null;
		PreparedStatement pstmt=null;
		ResultSet resultSet=null;
		try{
			connection=dataSource.getConnection();
	        pstmt = connection.prepareStatement("SELECT imei FROM grid_imei_detail WHERE grid_people_group_id=? AND TYPE=? LIMIT 0,10");
	        pstmt.setString(1, groupId);
	        pstmt.setString(2, type);
	        resultSet = pstmt.executeQuery();
	        while(resultSet.next()){
	        	imeis.put(resultSet.getString("imei"));
	        }
			System.out.println(new Date()+" queryImeis imeis="+imeis.length());
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			release(connection,pstmt,resultSet);
		}
		return imeis;
	}
	
	
	private String queryWhereInByGroupId(String[] groupIds){
		StringBuffer in=new StringBuffer("IN(");
		for(int i=0;i<groupIds.length;i++){
			if(i==groupIds.length-1){
				in.append("'"+groupIds[i]+"')");
			}else{
				in.append("'"+groupIds[i]+"',");
			}
		}
		System.out.println(new Date()+" queryWhereInByGroupId in="+in.toString());
		return in.toString();
	}
	
	
	private boolean checkParam(JSONObject json,String groupId){
		if(StringUtils.isNotBlank(groupId)){
			return true;
		}else{
			json.put("status", "error");
			json.put("message", "参数不能为空[groupId]");

		}
		return false;
	}
}
