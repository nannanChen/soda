<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <base href="<%=basePath%>">
    
    <title>soda-web</title>
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">    
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="This is my page">
	<!--
	<link rel="stylesheet" type="text/css" href="styles.css">
	-->
  </head>
  
  <body>
    <table border='1' align="center">
    	<tr>
    		<th colspan="5" align="center">soda-web 接口说明</th>
  		</tr>
  		<tr>
    		<td>接口名称</td>
    		<td>功能   </td>
    		<td>入参</td>
    		<td>结果</td>
    		<td>样例</td>
  		</tr>
  		<tr>
    		<td>getGridPoint</td>
    		<td>画网格点</td>
    		<td>无</td>
    		<td>Point(x,y)</td>
    		<td>
    			<a href="getGridPoint">效果</a>
    		</td>
  		</tr>
  		<tr>
    		<td>getGridLine</td>
    		<td>画网格线</td>
    		<td>无</td>
    		<td>
    			xLine(横线):开始点Point(x,y),结束点Point(x,y) <br/>
    			yLine(竖线):开始点Point(x,y),结束点Point(x,y)
    		</td>
    		<td>
    			<a href="getGridLine">效果</a>
    		</td>
  		</tr>
  		<tr>
    		<td>getGridFromToNum2</td>
    		<td>画线（动态）   画点（静态）</td>
    		<td>动态：日期，开始时间，结束时间，商圈  <br/>
    			（开始时间，结束时间不同，动态数据）<br/>
    			静态：日期，开始时间，结束时间 ，商圈 <br/>
    			（开始时间，结束时间相同，静态数据）
    		</td>
    		<td>
    			动态：日期，时间，源经纬度（网格坐标），目标经纬度（网格坐标），人数，分组id<br/>
    			静态：日期，时间，经纬度（网格坐标）, 人数，分组id<br/>
    			通用：图表数据(出行方式，数量)
    		</td>
    		<td>
    			<a href="getGridFromToNum2?date=20160301&fromHour=15&toHour=16&tradingArea=1">效果(动态)</a><br/>
    			<a href="getGridFromToNum2?date=20160301&fromHour=15&toHour=15&tradingArea=1">效果(静态)</a>
    		</td>
  		</tr>
  		<tr>
    		<td>getGridPeopleGroup2</td>
    		<td>查询分组   </td>
    		<td>分组id</td>
    		<td>分组id，人群类别，数量</td>
    		<td>
    			<a href="getGridPeopleGroup2?groupId=25ec6ad57439863068d3329a6b8c982f">效果</a>
    		</td>
  		</tr>
  		<tr>
    		<td>getClassLineServlet</td>
    		<td>获取一类人的行动轨迹 </td>
    		<td>日期，开始时间，结束时间，商圈，类型</td>
    		<td>
    			日期，时间，源经纬度（网格坐标），目标经纬度（网格坐标），人数，类别<br/>
    			通用：图表数据(出行方式，数量)
    		</td>
    		<td>
    			<a href="getClassLineServlet?date=20160301&fromHour=15&toHour=16&tradingArea=1&classType=5">效果</a>
    		</td>
  		</tr>
  		<tr>
  		<td>getPredictServlet</td>
    		<td>商圈预测数据 </td>
    		<td>日期，开始时间，结束时间，商圈</td>
    		<td>
    			数量
    		</td>
    		<td>
    			<a href="getPredictServlet?date=20160401&fromHour=15&toHour=18&tradingArea=1">效果</a>
    		</td>
  		</tr>
  		<tr>
  		<td>getPredictManTypeServlet</td>
    		<td>商圈预测人群 </td>
    		<td>日期，开始时间，结束时间，商圈，类型</td>
    		<td>
    			数量
    		</td>
    		<td>
    			<a href="getPredictManTypeServlet?date=20160401&fromHour=15&toHour=15&tradingArea=1&type=9">效果</a>
    		</td>
  		</tr>
    </table>
  </body>
</html>
