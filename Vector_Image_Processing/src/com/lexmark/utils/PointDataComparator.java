package com.lexmark.utils;

import java.util.Comparator;

public class PointDataComparator implements Comparator<PointData> {
	
	private boolean desending = false;
	private String axis;

	@Override
	public int compare(PointData arg0, PointData arg1) {
		int result = 0;
		
		if(desending){
			if("x".equalsIgnoreCase(axis)){
				result = arg1.getX()-arg0.getX();
				if(result == 0){
					result = arg1.getY()-arg0.getY();
				}
			}else if("y".equalsIgnoreCase(axis)){
				result = arg1.getY()-arg0.getY();
				if(result == 0){
					result = arg1.getX()-arg0.getX();
				}
			}
			
		}else{
			
			if("x".equalsIgnoreCase(axis)){
				result = arg0.getX()-arg1.getX();
				if(result == 0){
					result = arg0.getY()-arg1.getY();
				}
			}else if("y".equalsIgnoreCase(axis)){
				result = arg0.getY()-arg1.getY();
				if(result == 0){
					result = arg0.getX()-arg1.getX();
				}
			}
		}
		return result; 
	}
	
	public void setDesending(boolean desending) {
		this.desending = desending;
	}
	
	public void setAxis(String axis) {
		this.axis = axis;
	}

}
