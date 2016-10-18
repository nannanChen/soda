package com.soda.common;


import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kcao on 2016/9/28.
 */
public class GridDivide implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static int xNum=25;
    private static int yNum=20;

//    public static Point leftTop=new Point(121.1075740000,31.3877260000);   //左上角 花桥
    public static Point leftTop=new Point(121.0026520000,31.4595850000);   //左上角 太仓 昆山中间  沁园春

//    public static Point rightDown=new Point(121.9515500000,30.9031160000);  //右下角 滴水湖
    public static Point rightDown=new Point(121.9782840000,30.7645710000);  //右下角  东海大桥中间

    public static double x=rightDown.x-leftTop.x;  //宽
    public static double y=leftTop.y-rightDown.y;  //高

    public static double xStep=x/xNum; //宽的步长
    public static double yStep=y/yNum; //高的步长

    public static Point[][] grid=new Point[yNum][xNum];

    public static Map<String,Point> indexMap=new HashMap<String,Point>();

    public static Map<String,Point> addressMap=new HashMap<String,Point>();

    static{
        //初始化网格点数据
        for(int yy=0;yy<grid.length;yy++){
            for(int xx=0;xx<grid[yy].length;xx++){
                grid[yy][xx]=new Point((xx*xStep+leftTop.x),(leftTop.y-yy*yStep));
            }
        }

        //初始化网格下标与中心点映射
        for(int yy=0;yy<grid.length-1;yy++){
            for(int xx=0;xx<grid[yy].length-1;xx++){
                Point p=grid[yy][xx];
                int index=indexMap.size();
                indexMap.put((index+1)+"",new Point(p.x+xStep/2,p.y-yStep/2));
            }
        }

//        addressMap.put("nanJingDong",new Point(121.4910040000,31.2436160000));
//        addressMap.put("xuJiaHui",new Point(121.4446320000,31.1993870000));
//        addressMap.put("xinZhuang",new Point(121.3914070000,31.1167550000));
        
//        nanJingDong=Point{x=121.490468, y=31.233705450000002}
//        xuJiaHui=Point{x=121.45144272, y=31.19895475}
//        xinZhuang=Point{x=121.37339216, y=31.12945335}
        
        addressMap.put("nanJingDong",indexMap.get("157"));
        addressMap.put("xuJiaHui",indexMap.get("180"));
        addressMap.put("xinZhuang",indexMap.get("226"));
    }

    public static int findIndex(double x,double y) {
        int xIndex=-1;
        for(int xx=0;xx<grid[0].length;xx++){
            Point xp=grid[0][xx];
            if(x<xp.x){
                xIndex=xx-1;
                break;
            }
        }
        int yIndex=-1;
        for(int yy=0;yy<grid.length;yy++){
            Point yp=grid[yy][xIndex];
            if(y>yp.y){
                yIndex=yy-1;
                break;
            }
        }
//        System.out.println("xIndex:"+xIndex+" yIndex:"+yIndex+" "+grid[yIndex][xIndex]);
        return (yIndex*(xNum-1))+(xIndex+1);
    }

    /**
     * 检查点是否在网格内
     * @param x
     * @param y
     * @return 返回true 就是存在
     */
    public static boolean checkPoint(double x,double y) {
        if(x<rightDown.x&&x>leftTop.x&&y<leftTop.y&&y>rightDown.y){
            int index=GridDivide.findIndex(x,y);
            if(indexMap.get(index+"")!=null){
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }
    }

    public static void main(String[] args) throws Exception {
        Point nanJingDong=indexMap.get("157");
        System.out.println("nanJingDong="+nanJingDong);
        
        Point xuJiaHui=indexMap.get("180");
        System.out.println("xuJiaHui="+xuJiaHui);

        Point xinZhuang=indexMap.get("226");
        System.out.println("xinZhuang="+xinZhuang);

        	
    }

}

