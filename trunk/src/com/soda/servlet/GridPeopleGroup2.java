package com.soda.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
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
		System.out.println(new Date()+" GridPeopleGroup2 groupId="+groupId);
		if(checkParam(json,groupId)){
			json=queryPeopleGroup(json,groupId);
		}
		response.getOutputStream().write(json.toString().getBytes("UTF-8"));
	}
	
	
	private JSONObject queryPeopleGroup(JSONObject json,String groupId){
		Connection connection=null;
		PreparedStatement pstmt=null;
		ResultSet resultSet=null;
		try{
			connection=dataSource.getConnection();
	        pstmt = connection.prepareStatement("SELECT SUM(COUNT) AS count,type,GROUP_CONCAT(grid_people_group_id) AS grid_people_group_id FROM `grid_people_group1` WHERE grid_people_group_id "+queryWhereInByGroupId(groupId.split(","))+" GROUP BY TYPE");
	        resultSet = pstmt.executeQuery();
	        json.put("status", "OK");
        	JSONArray dataList=new JSONArray();
	        while(resultSet.next()){
	    		JSONObject data=new JSONObject();
	    		data.put("grid_people_group_id",groupId);
	    		data.put("type",CenterData.centerDataMap.get(resultSet.getString("type")));
	        	data.put("count",resultSet.getString("count"));
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
