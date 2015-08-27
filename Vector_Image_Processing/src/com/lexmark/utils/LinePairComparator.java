package com.lexmark.utils;

import java.util.Comparator;

public class LinePairComparator implements Comparator<LinePair> {
	
	private boolean preferenceHorizontal = false;

	@Override
	public int compare(LinePair arg0, LinePair arg1) {
		
		LineData pair1Line1 = arg0.getLineData1();
		LineData pair1Line2 = arg0.getLineData2();
		
		LineData pair2Line1 = arg1.getLineData1();
		LineData pair2Line2 = arg1.getLineData2();
		
		int result = 0;
		
		int horizontalLine1Length = -1;
		int verticalLine1Length = -1;
		if(pair1Line1 != null){
			if(pair1Line1.isHorizontal()){
				horizontalLine1Length = pair1Line1.getLength();
			}else{
				verticalLine1Length = pair1Line1.getLength();
			}
		}
		
		if(pair1Line2 != null){
			if(pair1Line2.isHorizontal()){
				horizontalLine1Length = pair1Line2.getLength();
			}else{
				verticalLine1Length = pair1Line2.getLength();
			}
		}
		
		int horizontalLine2Length = -1;
		int verticalLine2Length = -1;
		
		if(pair2Line1 != null){
			if(pair2Line1.isHorizontal()){
				horizontalLine2Length = pair2Line1.getLength();
			}else{
				verticalLine2Length = pair2Line1.getLength();
			}
		}
		
		if(pair2Line2 != null){
			if(pair2Line2.isHorizontal()){
				horizontalLine2Length = pair2Line2.getLength();
			}else{
				verticalLine2Length = pair2Line2.getLength();
			}
		}
		
		if(preferenceHorizontal){
			
			//both have horizontal line
			if(horizontalLine1Length > -1 && horizontalLine2Length > -1){
				result = horizontalLine1Length - horizontalLine2Length;
				if(result == 0){
					result = arg0.getTotalLength() - arg1.getTotalLength();
				}
			}else{
				result = arg0.getTotalLength() - arg1.getTotalLength();
			}
			
		}else{
			//both have vertical line
			if(verticalLine1Length > -1 && verticalLine2Length > -1){
				result = verticalLine1Length - verticalLine2Length;
				if(result == 0){
					result = arg0.getTotalLength() - arg1.getTotalLength();
				}
			}else{
				result = arg0.getTotalLength() - arg1.getTotalLength();
			}
		}
		
		//int result = arg0.getTotalLength() - arg0.getTotalLength();
		return result;
	}
	
	public void setPreferenceHorizontal(boolean preferenceHorizontal) {
		this.preferenceHorizontal = preferenceHorizontal;
	}

}
