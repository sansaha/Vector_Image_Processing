package com.lexmark.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;


public class ImageLineExtractionHelper {
	
	private static final Logger logger = Logger.getLogger(ImageLineExtractionHelper.class);
	
	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
	
	private static final int LINE_THICKNESS = 1;
	

	
	private static List<PointData> filterVerticalBorderPoints(List<PointData> verticalBorderPoints){
		PointDataComparator pointDataComparator = new PointDataComparator();
		pointDataComparator.setAxis("x");
		Collections.sort(verticalBorderPoints, pointDataComparator);
		
		Set<PointData> filterPointSet = new HashSet<PointData>();
		
		List<LineData> verticalLines = getVerticalLines(verticalBorderPoints);
		
		for(LineData lineData:verticalLines){
			if(lineData.getLength() >= ImageConstant.LINE_LENGTH_THRESOLD_VERTICAL){
				filterPointSet.addAll(lineData.getAllPoints());
			}
		}
	
		return new ArrayList<PointData>(filterPointSet);
	}
	
	private static List<PointData> filterHorizontalBorderPoints(List<PointData> horizontalBorderPoints){
		PointDataComparator pointDataComparator = new PointDataComparator();
		pointDataComparator.setAxis("y");
		Collections.sort(horizontalBorderPoints, pointDataComparator);
		
		Set<PointData> filterPointSet = new HashSet<PointData>();
		
		List<LineData> horizontalLines = getHorizontalLines(horizontalBorderPoints);
		
		for(LineData lineData:horizontalLines){
			if(lineData.getLength() >= ImageConstant.LINE_LENGTH_THRESOLD_HORIZONTAL){
				filterPointSet.addAll(lineData.getAllPoints());
			}
		}
	
		return new ArrayList<PointData>(filterPointSet);
	}
	
	
	
	public static List<LineData> getHorizontalLines(List<PointData> points){
		
		List<LineData> horizontalLines = new ArrayList<LineData>();
		
		LineData lineData = new LineData();
		for(PointData pointData:points){
			//first time
			if(lineData.getStartPointData() == null && lineData.getEndPointData() == null){
				lineData.setStartPointData(pointData.clonePointData());
				lineData.setEndPointData(pointData.clonePointData());
				horizontalLines.add(lineData);
				continue;
			}
			
			if(lineData.getEndPointData().getY() != pointData.getY()){
				lineData = new LineData();
				lineData.setStartPointData(pointData.clonePointData());
				lineData.setEndPointData(pointData.clonePointData());
				horizontalLines.add(lineData);
				continue;
			}
			
			if((pointData.getX() - lineData.getEndPointData().getX()) <= ImageConstant.HORIZONTAL_MAX_PERMISSIBLE_SINGLE_LINE_GAP){
				lineData.setEndPointData(pointData.clonePointData());
			}else{
				lineData = new LineData();
				lineData.setStartPointData(pointData.clonePointData());
				lineData.setEndPointData(pointData.clonePointData());
				horizontalLines.add(lineData);
			}
			
		}
		
		
		return horizontalLines;
		
	}
	
	public static List<LineData> getVerticalLines(List<PointData> points){
		
		List<LineData> verticalLines = new ArrayList<LineData>();
		
		LineData lineData = new LineData();
		for(PointData pointData:points){
			//first time
			if(lineData.getStartPointData() == null && lineData.getEndPointData() == null){
				lineData.setStartPointData(pointData.clonePointData());
				lineData.setEndPointData(pointData.clonePointData());
				verticalLines.add(lineData);
				continue;
			}
			
			if(lineData.getEndPointData().getX() != pointData.getX()){
				lineData = new LineData();
				lineData.setStartPointData(pointData.clonePointData());
				lineData.setEndPointData(pointData.clonePointData());
				verticalLines.add(lineData);
				continue;
			}
			
			if((pointData.getY() - lineData.getEndPointData().getY()) <= ImageConstant.VERTICAL_MAX_PERMISSIBLE_SINGLE_LINE_GAP){
				lineData.setEndPointData(pointData.clonePointData());
			}else{
				lineData = new LineData();
				lineData.setStartPointData(pointData.clonePointData());
				lineData.setEndPointData(pointData.clonePointData());
				verticalLines.add(lineData);
			}
			
		}
		
		
		return verticalLines;
		
	}
	
	private static int getNoOfOpenEnds(LineData lineData,List<LineData> allLines){
		
		int sodeOpen = 0;
				
		sodeOpen = getOpenEndedPoint(lineData, allLines).size();
		
		return sodeOpen;
	}
	
	
	private static List<PointData> getOpenEndedPoint(LineData lineData,List<LineData> allLines){
		
		
		List<PointData> openEndPoints = new ArrayList<PointData>(2);
		
		PointData startPointData = lineData.getStartPointData();
		PointData endPointData = lineData.getEndPointData();
		
		PointData nearesrStartPointData1 = null;
		PointData nearesrStartPointData2 = null;
		PointData nearesrStartPointData3 = null;
		PointData nearesrStartPointData4 = null;
		
		PointData nearesrEndPointData1 = null;
		PointData nearesrEndPointData2 = null;
		PointData nearesrEndPointData3 = null;
		PointData nearesrEndPointData4 = null;
		
		if(lineData.isHorizontal()){
			if(startPointData.getX() < endPointData.getX()){
				nearesrStartPointData1 = new PointData(startPointData.getX()+1, startPointData.getY());
				nearesrStartPointData2 = new PointData(startPointData.getX()+2, startPointData.getY());
				nearesrStartPointData3 = new PointData(startPointData.getX()+3, startPointData.getY());
				nearesrStartPointData4 = new PointData(startPointData.getX()-1, startPointData.getY());
				//nearesrStartPointData5 = new PointData(startPointData.getX()-2, startPointData.getY());
				
				
				nearesrEndPointData1 = new PointData(endPointData.getX()-1, endPointData.getY());
				nearesrEndPointData2 = new PointData(endPointData.getX()-2, endPointData.getY());
				nearesrEndPointData3 = new PointData(endPointData.getX()-3, endPointData.getY());
				nearesrEndPointData4 = new PointData(endPointData.getX()+1, endPointData.getY());
				//nearesrEndPointData5 = new PointData(endPointData.getX()+2, endPointData.getY());
			}else{
				nearesrStartPointData1 = new PointData(startPointData.getX()-1, startPointData.getY());
				nearesrStartPointData2 = new PointData(startPointData.getX()-2, startPointData.getY());
				nearesrStartPointData3 = new PointData(startPointData.getX()-3, startPointData.getY());
				nearesrStartPointData4 = new PointData(startPointData.getX()+1, startPointData.getY());
				
				
				nearesrEndPointData1 = new PointData(endPointData.getX()+1, endPointData.getY());
				nearesrEndPointData2 = new PointData(endPointData.getX()+2, endPointData.getY());
				nearesrEndPointData3 = new PointData(endPointData.getX()+3, endPointData.getY());
				nearesrEndPointData4 = new PointData(endPointData.getX()-1, endPointData.getY());
			}
			
		}else{
			if(startPointData.getY() < endPointData.getY()){
				nearesrStartPointData1 = new PointData(startPointData.getX(), startPointData.getY()+1);
				nearesrStartPointData2 = new PointData(startPointData.getX(), startPointData.getY()+2);
				nearesrStartPointData3 = new PointData(startPointData.getX(), startPointData.getY()+3);
				nearesrStartPointData4 = new PointData(startPointData.getX(), startPointData.getY()-1);
				
				
				nearesrEndPointData1 = new PointData(endPointData.getX(), endPointData.getY()-1);
				nearesrEndPointData2 = new PointData(endPointData.getX(), endPointData.getY()-2);
				nearesrEndPointData3 = new PointData(endPointData.getX(), endPointData.getY()-3);
				nearesrEndPointData4 = new PointData(endPointData.getX(), endPointData.getY()+1);
			}else{
				nearesrStartPointData1 = new PointData(startPointData.getX(), startPointData.getY()-1);
				nearesrStartPointData2 = new PointData(startPointData.getX(), startPointData.getY()-2);
				nearesrStartPointData3 = new PointData(startPointData.getX(), startPointData.getY()-3);
				nearesrStartPointData4 = new PointData(startPointData.getX(), startPointData.getY()+1);
				
				
				nearesrEndPointData1 = new PointData(endPointData.getX(), endPointData.getY()+1);
				nearesrEndPointData2 = new PointData(endPointData.getX(), endPointData.getY()+2);
				nearesrEndPointData3 = new PointData(endPointData.getX(), endPointData.getY()+3);
				nearesrEndPointData4 = new PointData(endPointData.getX(), endPointData.getY()-1);
			}
		}
		
		boolean startPointConnected = false;
		boolean endPointConnected = false;
		
		for(LineData lineDataTmp:allLines){
			if(lineDataTmp.equals(lineData)){
				continue;
			}
			
			List<PointData> internalPoints = lineDataTmp.getAllPoints();
			
			if(!startPointConnected && (internalPoints.contains(startPointData) || internalPoints.contains(nearesrStartPointData1) || internalPoints.contains(nearesrStartPointData2) ||
					internalPoints.contains(nearesrStartPointData3) || internalPoints.contains(nearesrStartPointData4))){
				startPointConnected = true;
			}
			
			if(!endPointConnected && (internalPoints.contains(endPointData) || internalPoints.contains(nearesrEndPointData1) || internalPoints.contains(nearesrEndPointData2) ||
					internalPoints.contains(nearesrEndPointData3) || internalPoints.contains(nearesrEndPointData4))){
				endPointConnected = true;
			}
			
		}
		if(!startPointConnected){
			openEndPoints.add(startPointData);
		}
		if(!endPointConnected){
			openEndPoints.add(endPointData);
		}
		
		
		return openEndPoints;
	}

	
	private static LinePair getBestPossibleJoinLine(LineData refLineData,List<LineData> allLines,int imageRows,int imageColumns){
		
		LinePair possibleJoinLinePairData = null;
		
		List<LinePair> possibleJoinLines = getPossibleJoinLines(refLineData, allLines,imageRows,imageColumns);
		
		if(!possibleJoinLines.isEmpty()){
						
			/*for(LineData lineData:possibleJoinLines){
				setMatchingPixelCount(lineData, allPoints);
			}*/
			
			//Collections.sort(possibleJoinLines, new LineComparatorByMatchingPixelAndLength());
			LinePairComparator linePairComparator = new LinePairComparator();
			if(refLineData.isHorizontal()){
				linePairComparator.setPreferenceHorizontal(true);
				Collections.sort(possibleJoinLines,linePairComparator);
			}else{
				Collections.sort(possibleJoinLines, linePairComparator);	
			}
			
			possibleJoinLinePairData = possibleJoinLines.get(0);
			
			/*if(possibleJoinLines.get(0).getMatchingPixelCount() > 0){
				possibleJoinLineData = possibleJoinLines.get(0);
			}*/
			
			
		}
		
		return possibleJoinLinePairData;
	}
	
	
	private static List<LinePair> getPossibleJoinLines(LineData refLineData,List<LineData> allLines,int imageRows,int imageColumns){

		List<LinePair> linePairs = new ArrayList<LinePair>();
		
		List<LineData> netghbourLines = null;
		
		if(refLineData.isHorizontal()){
			netghbourLines = getNeighbourLines(refLineData, allLines, 15, imageRows, imageColumns);
		}else{
			netghbourLines = getNeighbourLines(refLineData, allLines, 8, imageRows, imageColumns);
		}
		
		
		for(LineData lineDataTmp:netghbourLines){
			
			LineData joinLineData1 = null;
			LineData joinLineData2 = null;
			
			PointData pointData = null;
			
			if(refLineData.isHorizontal()){
				pointData = getIntersectionPoint(refLineData, lineDataTmp);
			}else{
				pointData = getIntersectionPoint(lineDataTmp,refLineData);
			}
			
			
			if(!refLineData.getAllPoints().contains(pointData)){
				LineData joinLineDataTmp1 = new LineData();
				joinLineDataTmp1.setEndPointData(pointData);
				joinLineDataTmp1.setStartPointData(refLineData.getStartPointData().clonePointData());
				
				LineData joinLineDataTmp2 = new LineData();
				joinLineDataTmp2.setEndPointData(pointData);
				joinLineDataTmp2.setStartPointData(refLineData.getEndPointData().clonePointData());
				
				if(joinLineDataTmp1.getLength() > joinLineDataTmp2.getLength()){
					joinLineData1 = joinLineDataTmp2;
				}else{
					joinLineData1 = joinLineDataTmp1;
				}
			}
			
			if(!lineDataTmp.getAllPoints().contains(pointData)){
				LineData joinLineDataTmp1 = new LineData();
				joinLineDataTmp1.setEndPointData(pointData);
				joinLineDataTmp1.setStartPointData(lineDataTmp.getStartPointData().clonePointData());
				
				LineData joinLineDataTmp2 = new LineData();
				joinLineDataTmp2.setEndPointData(pointData);
				joinLineDataTmp2.setStartPointData(lineDataTmp.getEndPointData().clonePointData());
				
				if(joinLineDataTmp1.getLength() > joinLineDataTmp2.getLength()){
					joinLineData2 = joinLineDataTmp2;
				}else{
					joinLineData2 = joinLineDataTmp1;
				}
				
			}
			
			LinePair linePair = new LinePair();
			
			if(joinLineData1 != null){
				linePair.setLineData1(joinLineData1);
				linePair.setRefConnectingLine2(lineDataTmp);
			}
			if(joinLineData2 != null){
				linePair.setLineData2(joinLineData2);
				linePair.setRefConnectingLine2(lineDataTmp);
			}
			
			if(linePair.getTotalLength() > 0 && (refLineData.getLength() > linePair.getTotalLength() && linePair.getTotalLength() < 25)){
				linePairs.add(linePair);
			}
			
		}
		
		
		return linePairs;
	}
	
	private static List<LineData> getNeighbourLines(LineData refLineData,List<LineData> allLines,int neighbourDistanceInPixels,int imageRows,int imageColumns){
		
		List<LineData> neighbourLines = new ArrayList<LineData>();
		
		 List<PointData> openPpoints = getOpenEndedPoint(refLineData, allLines);
		 		 
		 for(PointData openEndPoint:openPpoints){
			 
			 List<PointData> surroundingPoints = openEndPoint.getSurroundingPoints(neighbourDistanceInPixels, 0, imageColumns, 0,imageRows);
			 
			 surroundingPoints.removeAll(refLineData.getAllPoints());
			 
			 for(LineData lineData:allLines){
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
	
	
	private static PointData getIntersectionPoint(LineData horixLineData,LineData vertLineData){
		PointData pointData = new PointData(vertLineData.getStartPointData().getX(), horixLineData.getStartPointData().getY());
		return pointData;
	}

	private static List<PointData> populateVerticleBounderis(List<PointData> allPoints){
		
		List<PointData> verticalBounderyPoints = new ArrayList<PointData>();
		
		int yPrev = -1;
		int xMin = -1;
		int xMax = -1;
		for(PointData pointData:allPoints){
			if(yPrev== -1){
				//for the first time
				xMin = pointData.getX();
				xMax = pointData.getX();
				yPrev = pointData.getY();
				continue;
			}
			
			if(yPrev != pointData.getY()){
				PointData boundaryPointData1 = new PointData(xMin, yPrev);
				verticalBounderyPoints.add(boundaryPointData1);
				if(xMin != xMax){
					PointData boundaryPointData2 = new PointData(xMax, yPrev);
					verticalBounderyPoints.add(boundaryPointData2);
				}
				xMin = pointData.getX();
				xMax = pointData.getX();
				yPrev = pointData.getY();
				continue;
			}else{
				xMin = Math.min(xMin, pointData.getX());
				xMax = Math.max(xMax, pointData.getX());
			}
		}
		
		PointData boundaryPointData1 = new PointData(xMin, yPrev);
		verticalBounderyPoints.add(boundaryPointData1);
		if(xMin != xMax){
			PointData boundaryPointData2 = new PointData(xMax, yPrev);
			verticalBounderyPoints.add(boundaryPointData2);
		}		

		return verticalBounderyPoints;
	}
	
	private static List<PointData> populateHorizontalBounderis(List<PointData> allPoints){
		List<PointData> horizontalBounderyPoints = new ArrayList<PointData>();
		
		int xPrev = -1;
		int yMin = -1;
		int yMax = -1;
		for(PointData pointData:allPoints){
			if(xPrev== -1){
				//for the first time
				yMin = pointData.getY();
				yMax = pointData.getY();
				xPrev = pointData.getX();
				continue;
			}
			
			if(xPrev != pointData.getX()){
				PointData boundaryPointData1 = new PointData(xPrev, yMin);
				horizontalBounderyPoints.add(boundaryPointData1);
				if(yMin != yMax){
					PointData boundaryPointData2 = new PointData(xPrev, yMax);
					horizontalBounderyPoints.add(boundaryPointData2);
				}
				yMin = pointData.getY();
				yMax = pointData.getY();
				xPrev = pointData.getX();
				continue;
			}else{
				yMin = Math.min(yMin, pointData.getY());
				yMax = Math.max(yMax, pointData.getY());
			}
		}
		
		PointData boundaryPointData1 = new PointData(xPrev, yMin);
		horizontalBounderyPoints.add(boundaryPointData1);
		if(yMin != yMax){
			PointData boundaryPointData2 = new PointData(xPrev, yMax);
			horizontalBounderyPoints.add(boundaryPointData2);
		}
		
		
		return horizontalBounderyPoints;
	}
	
	
	private static boolean isBorderLine(LineData lineData,List<PointData> borderPoints){
		boolean borderLine = false;
		List<PointData> linePoints = lineData.getAllPoints();
		/*List<PointData> linePoints = new ArrayList<PointData>(2);
		linePoints.add(lineData.getStartPointData());
		linePoints.add(lineData.getEndPointData());*/

		for(PointData pointData:linePoints){
			if(borderPoints.contains(pointData)){
				borderLine = true;
				break;
			}
		}
		
		/*if(!borderLine){
			List<PointData> nearestBoarderPointList = new ArrayList<PointData>();
			PointData lineStartPointData = lineData.getStartPointData();
			for(PointData pointData:borderPoints){
				if(pointData.getX() == lineData.getStartPointData().getX() || pointData.getY() == lineData.getStartPointData().getY()){
					nearestBoarderPointList.add(pointData);
				}
			}
		}*/
		
		return borderLine;
	}
	

}
