package com.lexmark.utils;

import java.util.ArrayList;
import java.util.List;

public class LineData {

	private PointData startPointData;
	private PointData endPointData;
	private boolean ignore = false;
	private int matchingPixelCount = 0;
	private boolean borderLine = false;
	private int contourSequence;
	
	public int getContourSequence() {
		return contourSequence;
	}
	public void setContourSequence(int contourSequence) {
		this.contourSequence = contourSequence;
	}
	@Override
	public String toString() {
		return "LineData [start=" + startPointData + ", end="
				+ endPointData + "]";
	}
	public PointData getStartPointData() {
		return startPointData;
	}
	public void setStartPointData(PointData startPointData) {
		this.startPointData = startPointData;
	}
	public PointData getEndPointData() {
		return endPointData;
	}
	public void setEndPointData(PointData endPointData) {
		this.endPointData = endPointData;
	}

	public boolean isIgnore() {
		return ignore;
	}
	public void setIgnore(boolean ignore) {
		this.ignore = ignore;
	}

	public int getMatchingPixelCount() {
		return matchingPixelCount;
	}
	public void setMatchingPixelCount(int matchingPixelCount) {
		this.matchingPixelCount = matchingPixelCount;
	}
	
	
	public boolean isBorderLine() {
		return borderLine;
	}
	public void setBorderLine(boolean borderLine) {
		this.borderLine = borderLine;
	}
	public int getLength(){
		
		int length = 0;
		
		 int dx = startPointData.getX()-endPointData.getX();
         int dy = startPointData.getY()-endPointData.getY();
         
         length = (int) Math.sqrt((dx*dx)+(dy*dy));
		
		return length;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((endPointData == null) ? 0 : endPointData.hashCode());
		result = prime * result
				+ ((startPointData == null) ? 0 : startPointData.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LineData other = (LineData) obj;
		if (endPointData == null) {
			if (other.endPointData != null)
				return false;
		} else if (!endPointData.equals(other.endPointData))
			return false;
		if (startPointData == null) {
			if (other.startPointData != null)
				return false;
		} else if (!startPointData.equals(other.startPointData))
			return false;
		return true;
	}
	
	public LineData getReverseLine(){
		LineData revLineData = new LineData();
		revLineData.setStartPointData(this.getEndPointData().clonePointData());
		revLineData.setEndPointData(this.getStartPointData().clonePointData());
		
		return revLineData;
	}
	
	
	public List<PointData> getAllPoints(){
		List<PointData> pointList = new ArrayList<PointData>();
		
		if(startPointData != null){
			pointList.add(startPointData.clonePointData());
		}
		
		//y = mx+c
		if(startPointData != null && endPointData != null){
			
			int dy = startPointData.getY() - endPointData.getY();
			int dx = startPointData.getX() - endPointData.getX();
			
			//vertical line
			if(dx == 0){
				//equation: x = constant
				int x = startPointData.getX();
				int y_start = 0;
				int y_end = 0;
				if(dy > 0){
					y_start = endPointData.getY() + 1;
					y_end = startPointData.getY() -1;
				}else{
					y_start = startPointData.getY() + 1;
					y_end = endPointData.getY() -1;
				}
				
				for(;y_start <= y_end; y_start++){
					PointData pointData = new PointData(x, y_start);
					pointList.add(pointData);
				}
				
				
			}else if(dy == 0){
				//equation: y = constant
				
				int y = startPointData.getY();
				int x_start = 0;
				int x_end = 0;
				if(dx > 0){
					x_start = endPointData.getX() + 1;
					x_end = startPointData.getX() -1;
				}else{
					x_start = startPointData.getX() + 1;
					x_end = endPointData.getX() -1;
				}
				
				for(;x_start <= x_end; x_start++){
					PointData pointData = new PointData(x_start, y);
					pointList.add(pointData);
				}
			}else{
				float slope = dy/dx;
				
				float c = startPointData.getY() - (slope * startPointData.getX());
				
				int x_start = 0;
				int x_end = 0;
				
				if(dx > 0){
					x_start = endPointData.getX() + 1;
					x_end = startPointData.getX() -1;
				}else{
					x_start = startPointData.getX() + 1;
					x_end = endPointData.getX() -1;
				}
				
				for(;x_start <= x_end; x_start++){
					int y = (int) ((slope*x_start) + c);
					PointData pointData = new PointData(x_start, y);
					pointList.add(pointData);
				}

			}
			
		}
		
		
		if(endPointData != null){
			pointList.add(endPointData.clonePointData());
		}
		
		return pointList;
	}
	
	
	public List<PointData> getSurroundingPoints(int surroundingRadius,boolean fromStartPoint,boolean fromEndPoint,int minX,int maxX,int minY,int maxY){
		
		List<PointData> surroundingPoints = new ArrayList<PointData>();
		
		if(fromStartPoint){
			List<PointData> surroundingFromStartPoint = this.getStartPointData().getSurroundingPoints(surroundingRadius, minX, maxX, minY, maxY);
			if(surroundingFromStartPoint.size() > 0){
				surroundingPoints.addAll(surroundingFromStartPoint);
			}
		}
		
		if(fromEndPoint){
			List<PointData> surroundingFromEndPoint = this.getEndPointData().getSurroundingPoints(surroundingRadius, minX, maxX, minY, maxY);
			if(surroundingFromEndPoint.size() > 0){
				surroundingPoints.addAll(surroundingFromEndPoint);
			}
		}
		
		if(surroundingPoints.size() > 0){
			surroundingPoints.removeAll(this.getAllPoints());
		}
		
		
		return surroundingPoints;
	}
	
	public boolean isHorizontal(){
		int y1 = this.getStartPointData().getY();
		int y2 = this.getEndPointData().getY();
		
		return (y1 == y2);
	}
	
	public boolean isVertical(){
		int x1 = this.getStartPointData().getX();
		int x2 = this.getEndPointData().getX();
		
		return (x1 == x2);
	}
	
	public boolean isIncliend(){
		return (!isVertical() && !isHorizontal());
	}
	

}
