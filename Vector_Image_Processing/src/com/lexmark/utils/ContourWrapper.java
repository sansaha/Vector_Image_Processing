package com.lexmark.utils;

import org.opencv.core.MatOfPoint;

public class ContourWrapper implements Comparable<ContourWrapper>{
	
	private MatOfPoint matOfPoint;
	private PointData centroied;
	private double area;
	boolean ignore;
	
	public MatOfPoint getMatOfPoint() {
		return matOfPoint;
	}
	public void setMatOfPoint(MatOfPoint matOfPoint) {
		this.matOfPoint = matOfPoint;
	}
	
	public PointData getCentroied() {
		return centroied;
	}
	public void setCentroied(PointData centroied) {
		this.centroied = centroied;
	}
	public double getArea() {
		return area;
	}
	public void setArea(double area) {
		this.area = area;
	}

	public boolean isIgnore() {
		return ignore;
	}
	public void setIgnore(boolean ignore) {
		this.ignore = ignore;
	}
	@Override
	public int compareTo(ContourWrapper o) {
		PointDataComparator pointDataComparator = new PointDataComparator();
		pointDataComparator.setAxis("x");
		return pointDataComparator.compare(this.centroied, o.centroied);
	}
	
	

}
