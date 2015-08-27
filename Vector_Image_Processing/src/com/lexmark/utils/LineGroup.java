package com.lexmark.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math.stat.regression.SimpleRegression;

public class LineGroup {
	
	private int groupNo = -1;
	
	private List<LineData> lines = new ArrayList<LineData>();
	
	private int startX = -1;
	private int startY = -1;
	private int endX = -1;
	private int endY = -1;
	
	
	public int getGroupNo() {
		return groupNo;
	}
	public void setGroupNo(int groupNo) {
		this.groupNo = groupNo;
	}
	public List<LineData> getLines() {
		return lines;
	}
	public void setLines(List<LineData> lines) {
		this.lines = lines;
	}
	
	public boolean addLine(LineData lineData,int dxMax,int dyMax) {
		boolean success = false;
		
		if(lines.size() == 0){
			lines.add(lineData);
			success = true;
		}else if(lineData.isHorizontal() && this.isHorizontal()){
			LineData refLineData = lines.get(lines.size()-1);
			int deltaX = LineUtils.getDeltaXBetweenHorizLines(refLineData, lineData);
			int deltaY = Math.abs(refLineData.getStartPointData().getY() - lineData.getStartPointData().getY());
			
			if(deltaX <= dxMax && deltaY <= dyMax){
				this.lines.add(lineData);
				success = true;
			}
			
		}else if(lineData.isVertical() && this.isVertical()){
			LineData refLineData = lines.get(lines.size()-1);
			int deltaX = Math.abs(refLineData.getStartPointData().getX() - lineData.getStartPointData().getX());
			int deltaY = LineUtils.getDeltaYBetweenVertLines(refLineData, lineData);
			
			if(deltaX <= dxMax && deltaY <= dyMax){
				this.lines.add(lineData);
				success = true;
			}
		}

		return success;
	}
	public int getStartX() {
		return startX;
	}
	public int getStartY() {
		return startY;
	}
	public int getEndX() {
		return endX;
	}
	public int getEndY() {
		return endY;
	}
	
	public boolean isVertical() {
		boolean vertical = false;

		if(lines.size() > 0){
			vertical = lines.get(0).isVertical();
		}
		
		
		return vertical;
	}
	
	public boolean isHorizontal() {
		boolean horiz = false;

		if(lines.size() > 0){
			horiz = lines.get(0).isHorizontal();
		}
		return horiz;

	}
	
	public void doRegression(){
		if(lines.size() > 1){
			List<PointData> refPoints = new ArrayList<PointData>();
			for(LineData lineData:lines){
				if(!refPoints.contains(lineData.getStartPointData())){
					refPoints.add(lineData.getStartPointData());
				}
				if(!refPoints.contains(lineData.getEndPointData())){
					refPoints.add(lineData.getEndPointData());	
				}
				
				
			}
			
			PointDataComparator pointDataComparator = new PointDataComparator();
			if(this.isHorizontal()){
				pointDataComparator.setAxis("x");
			}else if(this.isVertical()){
				pointDataComparator.setAxis("y");
			}
			
			Collections.sort(refPoints,pointDataComparator);
			
			if(refPoints.size() > 2){
				List<PointData> pointList = simpleRegrateMatPoints(refPoints);
				PointData startPoint = pointList.get(0);
				PointData endPoint = pointList.get(1);
				
				startX = startPoint.getX();
				startY = startPoint.getY();
				
				endX = endPoint.getX();
				endY = endPoint.getY();
			}else{
				LineData lineData = lines.get(0);
				PointData startPoint = lineData.getStartPointData();
				PointData endPoint = lineData.getEndPointData();
				
				startX = startPoint.getX();
				startY = startPoint.getY();
				
				endX = endPoint.getX();
				endY = endPoint.getY();
			}
			
		}else if(lines.size() == 1){
			LineData lineData = lines.get(0);
			PointData startPoint = lineData.getStartPointData();
			PointData endPoint = lineData.getEndPointData();
			
			startX = startPoint.getX();
			startY = startPoint.getY();
			
			endX = endPoint.getX();
			endY = endPoint.getY();
		}
		
	}
	
	public List<PointData> simpleRegrateMatPoints(List<PointData> refPoints){
		
		//List<Point> refPoints = matOfPoint.toList();
		
		int xMin = -1;
		int xMax = -1;
		int yMin = -1;
		int yMax = -1;
		SimpleRegression simpleRegression = new SimpleRegression();
		for(PointData point:refPoints){
			simpleRegression.addData(point.getX(), point.getY());
			int tmpX = (int) point.getX();
			int tmpY = (int) point.getY();
			
			if(xMin != -1){
				if(xMin > tmpX){
					xMin = tmpX;
				}
			}else{
				xMin = tmpX;
			}
			if(xMax != -1){
				if(xMax < tmpX){
					xMax = tmpX;
				}
			}else{
				xMax = tmpX;
			}
			
			if(yMin != -1){
				if(yMin > tmpY){
					yMin = tmpY;
				}
			}else{
				yMin = tmpY;
			}
			if(yMax != -1){
				if(yMax < tmpY){
					yMax = tmpY;
				}
			}else{
				yMax = tmpY;
			}
		}
		int dX = xMax - xMin;
		int dY = yMax - yMin;
		
		PointData startPoint = new PointData(0,0);
		PointData endPoint = new PointData(0,0);
		
		List<PointData> resultPoints = new ArrayList<PointData>(2);
		
		//int index = 0;
		/*if(dX >= dY){
			for(Point refPoint: refPoints){
				int xTmp = (int) refPoint.x;
				int yTmp = (int) ((simpleRegression.getSlope()*refPoint.x)+simpleRegression.getIntercept());
				resultPoints.add(new Point(xTmp,yTmp));
				
			}
		}else{
			for(Point refPoint: refPoints){
				int xTmp = (int) ((refPoint.y -simpleRegression.getIntercept())/simpleRegression.getSlope()) ;;
				int yTmp = (int) refPoint.y;
				resultPoints.add(new Point(xTmp,yTmp));
				
			}
		}*/
		
		if(dX >= dY){
			int yCal = (int) ((simpleRegression.getSlope()*xMin)+simpleRegression.getIntercept());
			//System.out.println("x: "+xMin+", yCal:"+yCal);
			startPoint.setX(xMin);
			startPoint.setY(yCal);
			
			yCal = (int) ((simpleRegression.getSlope()*xMax)+simpleRegression.getIntercept());
			//System.out.println("x: "+xMax+", yCal:"+yCal);
			endPoint.setX(xMax);
			endPoint.setY(yCal);
			
		}else{
			int xCal = (int) ((yMin -simpleRegression.getIntercept())/simpleRegression.getSlope());
			//System.out.println("x: "+xCal+", y:"+yMin);
			if(xCal == 0){
				xCal = xMin;
			}
			startPoint.setX(xCal);
			startPoint.setY(yMin);
			
			xCal = (int) ((yMax -simpleRegression.getIntercept())/simpleRegression.getSlope()) ;
			if(xCal == 0){
				xCal = xMax;
			}
			//System.out.println("x: "+xCal+", y:"+yMax);
			endPoint.setX(xCal);
			endPoint.setY(yMax);
		}
		
		if(this.isHorizontal()){
			int yAvg = (startPoint.getY() + endPoint.getY())/2;
			startPoint.setY(yAvg);
			endPoint.setY(yAvg);
		}
		
		if(this.isVertical()){
			int xAvg = (startPoint.getX() + endPoint.getX())/2;
			startPoint.setX(xAvg);
			endPoint.setX(xAvg);
		}
		
		resultPoints.add(startPoint);
		resultPoints.add(endPoint);

		return resultPoints;
	}

}
