package com.soda.common;

import java.io.Serializable;

/**
 * Created by kcao on 2016/9/28.
 */
public class Point implements Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public double x;
    public double y;
    public Point(double x,double y){
        this.x=x;
        this.y=y;
    }

    @Override
    public String toString() {
        return "Point{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
