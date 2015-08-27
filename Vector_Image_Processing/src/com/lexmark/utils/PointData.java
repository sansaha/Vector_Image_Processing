package com.lexmark.utils;

import java.util.ArrayList;
import java.util.List;

public class PointData {

	private int x;
	private int y;
	
	public PointData(int x,int y){
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}
	
	public PointData clonePointData(){
		PointData pointData = new PointData(x, y);
		return pointData;
	}
	
	
	public List<PointData> getSurroundingPoints(int boundaryRadiusInPixel,int minX,int maxX,int minY,int maxY){
		
		List<PointData> pointList = new ArrayList<PointData>();
		
		int xRef = this.getX();
		int yRef = this.getY();
		
		for(int i = 1; i <=boundaryRadiusInPixel ; i++){
			
			//int noOfSurroundingPixels = (int) Math.pow(2, i+2);
			
			List<Integer> possibleX = new ArrayList<Integer>();
			List<Integer> possibleY = new ArrayList<Integer>();
			
			possibleX.add(xRef);
			possibleY.add(yRef);
			
			for(int j=1;j <= i;j++){
				possibleX.add(xRef+j);
				possibleX.add(xRef-j);
				
				possibleY.add(yRef+j);
				possibleY.add(yRef-j);
			}
			
			for (int k = 0; k < possibleX.size(); k++) {
				int xTmp = possibleX.get(k);
				for (int yTmp:possibleY) {
					if(Math.abs(xTmp - xRef) == i || Math.abs(yTmp - yRef) == i){
						
						if(isValid(xTmp, minX, maxX) && isValid(yTmp, minY, maxY)){
							PointData pointData = new PointData(xTmp, yTmp);
							pointList.add(pointData);
						}
						
					}
					
				}
			}
			
		}
				
		return pointList;
	}
	
	
	private boolean isValid(int value,int minVal,int maxVal){
		return (value <=maxVal && value >=minVal);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
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
		PointData other = (PointData) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "PointData [x=" + x + ", y=" + y + "]";
	}
	
	

}
