package com.lexmark.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class LineUtils {
	
	public static List<LineData> getHorizontalLines(List<LineData> allLines){
		
		List<LineData> horizontalLines = new ArrayList<LineData>();
		
		for(LineData lineData:allLines){
			if(lineData.isHorizontal()){
				horizontalLines.add(lineData);
			}
		}

		return horizontalLines;
	}
	
	public static List<LineData> getVerticalLines(List<LineData> allLines){
		
		List<LineData> verticalLines = new ArrayList<LineData>();
		
		for(LineData lineData:allLines){
			if(lineData.isVertical()){
				verticalLines.add(lineData);
			}
		}

		return verticalLines;
	}
	
	public static LineData getLargestLine(List<LineData> lines){
		
		LineData lineDataLargest = null;
		int totLines = lines.size();
		if(totLines > 0){
			lineDataLargest = lines.get(0);
			
			for (int i = 1; i < totLines; i++) {
				LineData tmpLineData = lines.get(i);
				if(lineDataLargest.getLength() < tmpLineData.getLength()){
					lineDataLargest = tmpLineData;
				}
			}
			
		}
		
		return lineDataLargest;
	}
	
	public static boolean mergeHorizontalLines(List<LineData> horizLines,int dX,int dY){
		
		boolean mergeSuccess = false;
		for(LineData lineData:horizLines){
			 int length = lineData.getLength();
				if(length < ImageConstant.LINE_LENGTH_THRESOLD_HORIZONTAL){
					continue;
				}
			 if(!lineData.isIgnore()){
				 boolean mergeSuccessTmp = mergeNearestParallelHorizontalLine(lineData, horizLines,dX,dY);
				 if(!mergeSuccess && mergeSuccessTmp){
					 mergeSuccess = mergeSuccessTmp;
				 }
			 } 
		 }
		 
		 return mergeSuccess;
	}
	
	public static boolean mergeVerticalLines(List<LineData> verticalLines,int dX,int dY){
		
		boolean mergeSuccess = false;
		for(LineData lineData:verticalLines){
			 int length = lineData.getLength();
				if(length < ImageConstant.LINE_LENGTH_THRESOLD_VERTICAL){
					continue;
				}
			 if(!lineData.isIgnore()){
				 boolean mergeSuccessTmp = mergeNearestParallelVerticalLine(lineData, verticalLines,dX,dY);
				 if(!mergeSuccess && mergeSuccessTmp){
					 mergeSuccess = mergeSuccessTmp;
				 }
			 } 
		 }
		 
		 return mergeSuccess;
	}
	
	private static boolean mergeNearestParallelHorizontalLine(LineData lineDataRef,List<LineData> horizLines,int dX,int dY){
		
		//logger.info("Going to check merging possibilities for the horizontal line:"+lineDataRef);
		
		boolean mergeSuccess = false;
		
		Map<Integer,List<LineData>> parallelLineMap = new TreeMap<Integer,List<LineData>>();
		
		for(LineData lineDataTmp:horizLines){
			//exclude the same line
			if(lineDataRef.equals(lineDataTmp)){
				continue;
			}
			
			int length = lineDataTmp.getLength();
			if(length < ImageConstant.LINE_LENGTH_THRESOLD_HORIZONTAL){
				continue;
			}
			
			int deltaY = Math.abs(lineDataTmp.getStartPointData().getY() - lineDataRef.getStartPointData().getY());
			
			//if(deltaY <= ImageConstant.MERGE_DISTANCE_Y_THRESOLD_HORIZONTAL){
			if(deltaY <= dY){
				
				if(!parallelLineMap.containsKey(deltaY)){
					parallelLineMap.put(deltaY, new ArrayList<LineData>());
				}
				
				parallelLineMap.get(deltaY).add(lineDataTmp);
				
			}
			
		}
		
		if(!parallelLineMap.isEmpty()){
			
			for(Integer deltaY : parallelLineMap.keySet()){
				
				List<LineData> lines = parallelLineMap.get(deltaY);
				
				for(LineData parallelLine:lines){
					
					if(parallelLine.isIgnore()){
						continue;
					}
					
					int deltaX = getDeltaXBetweenHorizLines(lineDataRef, parallelLine);

					if(deltaX <= dX){
						mergeSuccess = true;
						mergeHorizontalLines(lineDataRef, parallelLine);
						if(lineDataRef.isIgnore()){
							//logger.info("Merging successful, new merged line: "+parallelLine);
							break;
						}else{
							//logger.info("Merging successful, new merged line: "+lineDataRef);
						}
						//parallelLine.setIgnore(true);
						
						//logger.info("Due to merge, horiz line to be ignored: "+parallelLine);
						//break;
					}
				}				
				
			}

		}
		
		return mergeSuccess;
		
	}
	
	private static boolean mergeNearestParallelVerticalLine(LineData lineDataRef,List<LineData> vertLines,int dX,int dY){
		
		boolean mergeSuccess = false;
		
		Map<Integer,List<LineData>> parallelLineMap = new TreeMap<Integer,List<LineData>>();
		
		for(LineData lineDataTmp:vertLines){
			//exclude the same line
			if(lineDataRef.equals(lineDataTmp)){
				continue;
			}
			
			int length = lineDataTmp.getLength();
			if(length < ImageConstant.LINE_LENGTH_THRESOLD_VERTICAL){
				continue;
			}
			
			int deltaX = Math.abs(lineDataTmp.getStartPointData().getX() - lineDataRef.getStartPointData().getX());
			
			//TODO remove 2 by a constant
			//if(deltaX <= ImageConstant.MERGE_DISTANCE_X_THRESOLD_VERTICAL){
			if(deltaX <= dX){
				
				if(!parallelLineMap.containsKey(deltaX)){
					parallelLineMap.put(deltaX, new ArrayList<LineData>());
				}
				
				parallelLineMap.get(deltaX).add(lineDataTmp);
				
			}
			
		}
		
		if(!parallelLineMap.isEmpty()){
			
			for(Integer deltaX : parallelLineMap.keySet()){
				
				List<LineData> lines = parallelLineMap.get(deltaX);
				
				for(LineData parallelLine:lines){
					if(parallelLine.isIgnore()){
						continue;
					}
					
					int deltaY = getDeltaYBetweenVertLines(lineDataRef, parallelLine);	
					
					//if(deltaY <= ImageConstant.MERGE_DISTANCE_Y_THRESOLD_VERTICAL){
					if(deltaY <= dY){
						mergeSuccess = true;
						mergeVerticalLines(lineDataRef, parallelLine);
						if(lineDataRef.isIgnore()){
							break;
						}
						//parallelLine.setIgnore(true);
						//break;
					}
				}				
				
			}

		}
		
		return mergeSuccess;
	}

	
	public static int getDeltaXBetweenHorizLines(LineData lineData1,LineData lineData2){
		
		int deltaX = 0;
		
		int line1Xmax = Math.max(lineData1.getStartPointData().getX(), lineData1.getEndPointData().getX());
		int line1Xmin = Math.min(lineData1.getStartPointData().getX(), lineData1.getEndPointData().getX());
		int line2Xmax = Math.max(lineData2.getStartPointData().getX(), lineData2.getEndPointData().getX());
		int line2Xmin = Math.min(lineData2.getStartPointData().getX(), lineData2.getEndPointData().getX());
		
		if((line2Xmax > line1Xmin && line2Xmax < line1Xmax) || (line2Xmin > line1Xmin && line2Xmin < line1Xmax)){
			return deltaX;
		}
		
		int deltaX_Start1_Start2 = Math.abs(lineData1.getStartPointData().getX()-lineData2.getStartPointData().getX());
		int deltaX_Start1_End2 = Math.abs(lineData1.getStartPointData().getX()-lineData2.getEndPointData().getX());
		int deltaX_End1_Start2 = Math.abs(lineData1.getEndPointData().getX()-lineData2.getStartPointData().getX());
		int deltaX_End1_End2 = Math.abs(lineData1.getEndPointData().getX()-lineData2.getEndPointData().getX());
		
		deltaX = Math.min(deltaX_Start1_Start2, deltaX_Start1_End2);
		deltaX = Math.min(deltaX, deltaX_End1_Start2);
		deltaX = Math.min(deltaX, deltaX_End1_End2);
		
		return deltaX;
	}
	
	public static int getDeltaYBetweenVertLines(LineData lineData1,LineData lineData2){
		
		
		int deltaY = 0;
		
		int line1Ymax = Math.max(lineData1.getStartPointData().getY(), lineData1.getEndPointData().getY());
		int line1Ymin = Math.min(lineData1.getStartPointData().getY(), lineData1.getEndPointData().getY());
		int line2Ymax = Math.max(lineData2.getStartPointData().getY(), lineData2.getEndPointData().getY());
		int line2Ymin = Math.min(lineData2.getStartPointData().getY(), lineData2.getEndPointData().getY());
		
		if((line2Ymax > line1Ymin && line2Ymax < line1Ymax) || (line2Ymin > line1Ymin && line2Ymin < line1Ymax)){
			return deltaY;
		}
		
		int deltaY_Start1_Start2 = Math.abs(lineData1.getStartPointData().getY()-lineData2.getStartPointData().getY());
		int deltaY_Start1_End2 = Math.abs(lineData1.getStartPointData().getY()-lineData2.getEndPointData().getY());
		int deltaY_End1_Start2 = Math.abs(lineData1.getEndPointData().getY()-lineData2.getStartPointData().getY());
		int deltaY_End1_End2 = Math.abs(lineData1.getEndPointData().getY()-lineData2.getEndPointData().getY());
		
		deltaY = Math.min(deltaY_Start1_Start2, deltaY_Start1_End2);
		deltaY = Math.min(deltaY, deltaY_End1_Start2);
		deltaY = Math.min(deltaY, deltaY_End1_End2);
		
		return deltaY;
	}
	
	public static void mergeHorizontalLines(LineData lineDataRef,LineData lineData){
		
		int x1 = getMinVal(lineDataRef.getStartPointData().getX(),lineDataRef.getEndPointData().getX(),lineData.getStartPointData().getX(),lineData.getEndPointData().getX());//minimum x
		int x2 = getMaxVal(lineDataRef.getStartPointData().getX(),lineDataRef.getEndPointData().getX(),lineData.getStartPointData().getX(),lineData.getEndPointData().getX());//maximum x
		
		int y = 0;
		
		//boolean isRefBorderLine = isBorderLine(lineDataRef, horizontalBorderPoints);
		//boolean isBorderLine = isBorderLine(lineData, horizontalBorderPoints);
		
		/*if((isRefBorderLine && isBorderLine) || (!isRefBorderLine && !isBorderLine)){
			if(lineDataRef.getLength() > lineData.getLength()){
				//merge line1 with line2
				y = lineDataRef.getStartPointData().getY();
				lineData.setIgnore(true);
				
			}else if(lineDataRef.getLength() < lineData.getLength()){
				//merge line2 with line1
				y = lineData.getStartPointData().getY();
				lineDataRef.setIgnore(true);
			}else{
				y = Math.max(lineDataRef.getStartPointData().getY(), lineData.getStartPointData().getY());
				if(lineDataRef.getStartPointData().getY() > lineData.getStartPointData().getY()){
					lineData.setIgnore(true);
				}else{
					lineDataRef.setIgnore(true);
				}
			}
		}else{
			if(isRefBorderLine){
				y = lineDataRef.getStartPointData().getY();
				lineData.setIgnore(true);
			}else{
				y = lineData.getStartPointData().getY();
				lineDataRef.setIgnore(true);
			}
		}*/
		
		boolean boarderLine = lineDataRef.isBorderLine() || lineData.isBorderLine();
		
		if(lineDataRef.getLength() > lineData.getLength()){
			//merge line1 with line2
			y = lineDataRef.getStartPointData().getY();
			lineDataRef.setBorderLine(boarderLine);
			lineData.setIgnore(true);
			
		}else if(lineDataRef.getLength() < lineData.getLength()){
			//merge line2 with line1
			y = lineData.getStartPointData().getY();
			lineData.setBorderLine(boarderLine);
			lineDataRef.setIgnore(true);
		}else{
			y = Math.max(lineDataRef.getStartPointData().getY(), lineData.getStartPointData().getY());
			if(lineDataRef.getStartPointData().getY() > lineData.getStartPointData().getY()){
				lineDataRef.setBorderLine(boarderLine);
				lineData.setIgnore(true);
			}else{
				lineData.setBorderLine(boarderLine);
				lineDataRef.setIgnore(true);
			}
		}
		
		
		if(lineData.isIgnore()){
			lineDataRef.getStartPointData().setX(x1);
			lineDataRef.getStartPointData().setY(y);
			
			lineDataRef.getEndPointData().setX(x2);
			lineDataRef.getEndPointData().setY(y);
		}else{
			lineData.getStartPointData().setX(x1);
			lineData.getStartPointData().setY(y);
			
			lineData.getEndPointData().setX(x2);
			lineData.getEndPointData().setY(y);
		}
		
	}
	
	public static void mergeVerticalLines(LineData lineDataRef,LineData lineData){
		
		int y1 = getMinVal(lineDataRef.getStartPointData().getY(),lineDataRef.getEndPointData().getY(),lineData.getStartPointData().getY(),lineData.getEndPointData().getY());//minimum Y
		int y2 = getMaxVal(lineDataRef.getStartPointData().getY(),lineDataRef.getEndPointData().getY(),lineData.getStartPointData().getY(),lineData.getEndPointData().getY());//maximum Y
		
		int x = 0;
		
		/*boolean isRefBorderLine = isBorderLine(lineDataRef, verticalBorderPoints);
		boolean isBorderLine = isBorderLine(lineData, verticalBorderPoints);
		
		if((isRefBorderLine && isBorderLine) || (!isRefBorderLine && !isBorderLine)){
			if(lineDataRef.getLength() > lineData.getLength()){
				//merge line1 with line2
				x = lineDataRef.getStartPointData().getX();
				lineData.setIgnore(true);
				
			}else if(lineDataRef.getLength() < lineData.getLength()){
				//merge line2 with line1
				x = lineData.getStartPointData().getX();
				lineDataRef.setIgnore(true);
			}else{
				x = Math.max(lineDataRef.getStartPointData().getX(), lineData.getStartPointData().getX());
				if(lineDataRef.getStartPointData().getX() > lineData.getStartPointData().getX()){
					lineData.setIgnore(true);
				}else{
					lineDataRef.setIgnore(true);
				}
			}
		}else{
			if(isRefBorderLine){
				x = lineDataRef.getStartPointData().getX();
				lineData.setIgnore(true);
			}else{
				x = lineData.getStartPointData().getX();
				lineDataRef.setIgnore(true);
			}
		}*/
		
		boolean boarderLine = lineDataRef.isBorderLine() || lineData.isBorderLine();
		
		if(lineDataRef.getLength() > lineData.getLength()){
			//merge line1 with line2
			x = lineDataRef.getStartPointData().getX();
			lineDataRef.setBorderLine(boarderLine);
			lineData.setIgnore(true);
			
		}else if(lineDataRef.getLength() < lineData.getLength()){
			//merge line2 with line1
			x = lineData.getStartPointData().getX();
			lineData.setBorderLine(boarderLine);
			lineDataRef.setIgnore(true);
		}else{
			x = Math.max(lineDataRef.getStartPointData().getX(), lineData.getStartPointData().getX());
			if(lineDataRef.getStartPointData().getX() > lineData.getStartPointData().getX()){
				lineDataRef.setBorderLine(boarderLine);
				lineData.setIgnore(true);
			}else{
				lineData.setBorderLine(boarderLine);
				lineDataRef.setIgnore(true);
			}
		}
		
		if(lineData.isIgnore()){
			lineDataRef.getStartPointData().setX(x);
			lineDataRef.getStartPointData().setY(y1);
			
			lineDataRef.getEndPointData().setX(x);
			lineDataRef.getEndPointData().setY(y2);
		}else{
			lineData.getStartPointData().setX(x);
			lineData.getStartPointData().setY(y1);
			
			lineData.getEndPointData().setX(x);
			lineData.getEndPointData().setY(y2);
		}
		
	}
	
	public static List<LineData> getNeighbourLines(LineData refLineData,List<LineData> allLines,int neighbourDistanceInPixels,int imageRows,int imageColumns,String lineType){
		
		List<LineData> neighbourLines = new ArrayList<LineData>();
		
		 List<PointData> refLinePoints = refLineData.getAllPoints();
		 		 
		 for(PointData openEndPoint:refLinePoints){
			 
			 List<PointData> surroundingPoints = openEndPoint.getSurroundingPoints(neighbourDistanceInPixels, 0, imageColumns, 0,imageRows);
			 
			 surroundingPoints.removeAll(refLineData.getAllPoints());
			 
			 for(LineData lineData:allLines){
				 if("HZ".equals(lineType) && lineData.isVertical()){
					 continue;
				 }
				 if("VT".equals(lineType) && lineData.isHorizontal()){
					 continue;
				 }
				if(!lineData.equals(refLineData)){
					List<PointData> linePoints = lineData.getAllPoints();
					int initialSize = linePoints.size();
					linePoints.removeAll(surroundingPoints);
					int finalSize = linePoints.size();
					if(initialSize > finalSize){
						neighbourLines.add(lineData);
					}
				}
				
			 }
		 }
		 
		 
		 
		 return neighbourLines;
	}
	
	public int getHorizontalOverlapPercentage(LineData lineData1,LineData lineData2){
		int percentage = 0;
		
		LineData refLineData = null;
		LineData otherLineData = null;
		int minLength = 0;
		if(lineData1.getLength() >= lineData2.getLength()){
			refLineData = lineData1;
			otherLineData = lineData2;
			minLength = lineData2.getLength();
		}else{
			refLineData = lineData2;
			otherLineData = lineData1;
			minLength = lineData1.getLength();
		}
		
		int xRefStart = getMinVal(refLineData.getStartPointData().getX(),refLineData.getEndPointData().getX());
		int xRefEnd = getMaxVal(refLineData.getStartPointData().getX(),refLineData.getEndPointData().getX());
		
		int xOtherStart = getMinVal(otherLineData.getStartPointData().getX(),otherLineData.getEndPointData().getX());
		int xOtherEnd = getMaxVal(otherLineData.getStartPointData().getX(),otherLineData.getEndPointData().getX());
		
		int overlapLength = 0;
		
		if(xOtherStart >=xRefStart && xOtherStart <= xRefEnd){
			if(xOtherEnd > xRefEnd){
				overlapLength = (xRefEnd-xOtherStart);
			}else{
				overlapLength = otherLineData.getLength();
			}
			
		}else if(xOtherEnd >=xRefStart && xOtherEnd <= xRefEnd){
			if(xOtherEnd > xRefStart){
				overlapLength = (xOtherEnd-xRefStart);
			}else{
				overlapLength = 1;
			}
		}
		
		percentage = (overlapLength/minLength)*100;
		
		return percentage;
	}
	
	public static int getMinVal(int... vals){
		int min = 0;
		if(vals.length > 0){
			Arrays.sort(vals);
			min = vals[0];
		}
		return min;
	}
	
	public static int getMaxVal(int... vals){
		int max = 0;
		if(vals.length > 0){
			Arrays.sort(vals);
			max = vals[vals.length - 1];
		}
		return max;
	}
	
	public static void excludeIgnoreLines(List<LineData> lines){
		List<LineData> ignoreLines = new ArrayList<LineData>();
		for(LineData lineData:lines){
			if(lineData.isIgnore()){
				ignoreLines.add(lineData);
			}
		}
		
		//logger.info("Before Removing ignore line,total count:"+lines.size());
		if(ignoreLines.size() > 0){
			//logger.info("Removing ignore line count:"+ignoreLines.size());
			lines.removeAll(ignoreLines);
		}
		//logger.info("After Removing ignore line, total count:"+lines.size());
	}

}
