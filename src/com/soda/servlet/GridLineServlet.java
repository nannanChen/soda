package com.soda.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import com.soda.common.GridDivide;
import com.soda.common.Point;

/**
 * 获取网格线
 * @author kcao
 *
 */
public class GridLineServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public GridLineServlet() {
		super();
	}

	public void destroy() {
		super.destroy(); // Just puts "destroy" string in log
		// Put your code here
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request,response);
	}
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/json; charset=UTF-8");  
		
		JSONObject grid=new JSONObject();
		
		JSONArray xLine=new JSONArray();
		
	   for(int yy=0;yy<GridDivide.grid.length;yy++){  //y递增 没一行的数据 在变化
			JSONArray line=new JSONArray();
            Point start=GridDivide.grid[yy][0];
            JSONObject startLine=new JSONObject();
            startLine.put("x", start.x);
            startLine.put("y", start.y);
            line.put(startLine);
            
            Point end=GridDivide.grid[yy][GridDivide.grid[0].length-1];
            JSONObject endLine=new JSONObject();
            endLine.put("x", end.x);
            endLine.put("y", end.y);
            line.put(endLine);
            xLine.put(line);
        }
	    grid.put("xLine",xLine);
		
		
		JSONArray yLine=new JSONArray();
	    for(int xx=0;xx<GridDivide.grid[0].length;xx++){   //x递增  每一列数据在变化
			JSONArray line=new JSONArray();
            Point start=GridDivide.grid[0][xx];
            JSONObject startLine=new JSONObject();
            startLine.put("x", start.x);
            startLine.put("y", start.y);
            line.put(startLine);
            
            Point end=GridDivide.grid[GridDivide.grid.length-1][xx];
            JSONObject endLine=new JSONObject();
            endLine.put("x", end.x);
            endLine.put("y", end.y);
            line.put(endLine);
            yLine.put(line);
        }
		grid.put("yLine",yLine);
		
		response.getOutputStream().write(grid.toString().getBytes("UTF-8"));

	}
	public void init() throws ServletException {
	}

}
