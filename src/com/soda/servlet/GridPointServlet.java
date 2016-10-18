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
 * 获取网格点
 * @author kcao
 *
 */
public class GridPointServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public GridPointServlet() {
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
		
		JSONArray yList=new JSONArray();
		for(int yy=0;yy<GridDivide.grid.length;yy++){
			JSONArray xList=new JSONArray();
            for(int xx=0;xx<GridDivide.grid[yy].length;xx++){
        		JSONObject point=new JSONObject();
        		Point p=GridDivide.grid[yy][xx];
        		point.put("x", p.x);
        		point.put("y", p.y);
            	xList.put(point);
            }
            yList.put(xList);
        }
		
		response.getOutputStream().write(yList.toString().getBytes("UTF-8"));

	}
	public void init() throws ServletException {
	}

}
