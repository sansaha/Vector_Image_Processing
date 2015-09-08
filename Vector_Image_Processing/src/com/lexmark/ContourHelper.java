package com.lexmark;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import com.lexmark.utils.ContourWrapper;
import com.lexmark.utils.LineData;
import com.lexmark.utils.LineUtils;
import com.lexmark.utils.PointData;

public class ContourHelper {

	/**
	 * @param args
	 */
	public static List<MatOfPoint> filterDuplicateContour(List<MatOfPoint> inputContours,int dCentroied,int dAreaPercent) {
		
		if(inputContours.size() <= 1){
			return inputContours;
		}
		
		List<ContourWrapper> contourWrapperList = new ArrayList<ContourWrapper>();
		for(MatOfPoint matOfPoint: inputContours){
			
			Moments moments = Imgproc.moments(matOfPoint);
			
			int x = (int) (moments.get_m10() / moments.get_m00());
	        int y = (int) (moments.get_m01() / moments.get_m00());
	        
	        //System.out.println("X:"+x+",Y:"+y);
	        double contourArea = Imgproc.contourArea(matOfPoint);
			//System.out.println("Area::"+contourArea);
			
			ContourWrapper contourWrapper = new ContourWrapper();
			contourWrapper.setMatOfPoint(matOfPoint);
			contourWrapper.setArea(contourArea);
			contourWrapper.setCentroied(new PointData(x, y));
			contourWrapperList.add(contourWrapper);
		}
		
		Collections.sort(contourWrapperList);
		
		List<MatOfPoint> refineContours = null;
		
		int totContours = contourWrapperList.size();
		if(totContours > 1){
			refineContours = new ArrayList<MatOfPoint>();
			
			for (int i = 0; i < totContours; i++) {
				 ContourWrapper currentContourWrapper = contourWrapperList.get(i);
				 if(currentContourWrapper.isIgnore()){
					 continue;
				 }
				 
				 boolean matchFound = false;
				 for (int j = i+1; j < totContours; j++) {
					 ContourWrapper nextContourWrapper = contourWrapperList.get(j);

					 LineData centroidConnectingLine = new LineData();
					 centroidConnectingLine.setStartPointData(currentContourWrapper.getCentroied());
					 centroidConnectingLine.setEndPointData(nextContourWrapper.getCentroied());
					 
					 int centroidDistance = centroidConnectingLine.getLength();
					 //System.out.println("Centroid distance::"+centroidDistance);
					 
					 if(centroidDistance <= dCentroied){
						 double baseArea = currentContourWrapper.getArea() > nextContourWrapper.getArea()?currentContourWrapper.getArea():nextContourWrapper.getArea();
						 double areaChangePercentage = 0;
						 if(baseArea > 0){
							 areaChangePercentage = (Math.abs(currentContourWrapper.getArea()-nextContourWrapper.getArea())*100)/baseArea;
						 }
						// System.out.println("Area change::"+areaChangePercentage);
						 if(areaChangePercentage <= dAreaPercent){
							 if(currentContourWrapper.getArea() >= nextContourWrapper.getArea()){
								 refineContours.add(currentContourWrapper.getMatOfPoint());
								 nextContourWrapper.setIgnore(true);
							 }else{
								 refineContours.add(nextContourWrapper.getMatOfPoint());
								 currentContourWrapper.setIgnore(true);
							 }
							 matchFound = true;
							 break;
						 }
					 }
				 }
				 
				 if(!matchFound){
					 refineContours.add(currentContourWrapper.getMatOfPoint());
				 }
			}
		}
		
		
		return refineContours;
	}
	
	/*public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		Point[] points = {new Point(2,0),new Point(12,0),new Point(12,10),new Point(2,10)};
		MatOfPoint matOfPoint = new MatOfPoint(points);
		Moments moments = Imgproc.moments(matOfPoint);
		
		int x = (int) (moments.get_m10() / moments.get_m00());
        int y = (int) (moments.get_m01() / moments.get_m00());
        
        System.out.println("X:"+x+",Y:"+y);
		
	}*/
	
	public static void processPoints(MatOfPoint matOfPoint){
		
		List<Point> points = matOfPoint.toList();
		
		List<LineData> lines = new ArrayList<LineData>(points.size());
		
		int pointCount = points.size();
		
		for (int i = 0; i < pointCount-1; i++) {
			LineData lineData = new LineData();
			Point startPoint = points.get(i);
			Point endPoint = points.get(i+1);
			
			lineData.setStartPointData(new PointData((int)startPoint.x, (int)startPoint.y));
			lineData.setEndPointData(new PointData((int)endPoint.x, (int)endPoint.y));
			lineData.setContourSequence(i);
			lines.add(lineData);
		}
		
		if(matOfPoint.isContinuous()){
			LineData lineData = new LineData();
			Point endPoint = points.get(0);
			Point startPoint = points.get(points.size()-1);
			lineData.setStartPointData(new PointData((int)startPoint.x, (int)startPoint.y));
			lineData.setEndPointData(new PointData((int)endPoint.x, (int)endPoint.y));
			lineData.setContourSequence(pointCount-1);
			lines.add(lineData);
		}
		
		System.out.println("Total lines:: "+lines.size());
		int lengthTot = 0;
		for(LineData lineData:lines){
			//System.out.println(lineData.getLength());
			lengthTot = lengthTot + lineData.getLength();
		}
		
		System.out.println("Total length::"+lengthTot);
		
		MatOfPoint2f contour2f = new MatOfPoint2f();
		matOfPoint.convertTo(contour2f, CvType.CV_32FC2);
		int arcLength = (int) Imgproc.arcLength(contour2f, true);
		
		System.out.println("arcLength::"+arcLength);
		
		LineData lineDataLargest = LineUtils.getLargestLine(lines);
		
		//Map<Integer,List<LineData>> lineRegionMap = new LinkedHashMap<Integer,List<LineData>>();
		
		List<LineData> hzLines = LineUtils.getHorizontalLines(lines);
		
		List<LineData> vertLines = LineUtils.getVerticalLines(lines);
		
		/*int maxDxPercent = 49;
		int maxDyPercent = 49;
		
		int groupNo = 1;*/
		Mat drawContourHz = new Mat(2000,2000,CvType.CV_8UC1,Scalar.all(255));
		Mat drawContourVrt = drawContourHz.clone();
		/*for(LineData lineData:lines){
			if(!lineRegionMap.containsKey(groupNo)){
				lineRegionMap.put(groupNo, new ArrayList<LineData>());
			}
			
			List<LineData> regionLineList = lineRegionMap.get(groupNo);
			
			if(regionLineList.size() > 0){
				//LineData lineDataLargest = LineUtils.getLargestLine(regionLineList);
				LineData linedataPrevious = regionLineList.get(regionLineList.size()-1);
				int dX = Math.abs(linedataPrevious.getEndPointData().getX()-lineData.getEndPointData().getX());
				int dY = Math.abs(linedataPrevious.getEndPointData().getY()-lineData.getEndPointData().getY());
				//int lengthPercentage = (lineData.getLength()*100)/lineDataLargest.getLength();
				//System.out.println(dX+","+dY+","+lengthPercentage);
				if(dX <= 1 && dY <=1){
					regionLineList.add(lineData);
				}else{
					groupNo++;
					lineRegionMap.put(groupNo, new ArrayList<LineData>());
					lineRegionMap.get(groupNo).add(lineData);
				}
			}else{
				regionLineList.add(lineData);
				continue;
			}
			if(lineData.getLength() < 1){
				continue;
			}
			
			//Core.line(drawContour, new Point(lineData.getStartPointData().getX(),lineData.getStartPointData().getY()), new Point(lineData.getEndPointData().getX(),lineData.getEndPointData().getY()), Scalar.all(0));
			
		}*/
		
		/*System.out.println("Total region:: "+lineRegionMap.keySet().size());
		
		for(Integer groupNoKey:lineRegionMap.keySet()){
			System.out.println("Group#"+groupNoKey);
			List<LineData> groupLines = lineRegionMap.get(groupNoKey);
			PointData groupStartPoint = groupLines.get(0).getStartPointData();
			
			int startSeqNo = groupLines.get(0).getContourSequence();
			PointData groupEndPoint = groupLines.get(groupLines.size()-1).getEndPointData();
			int endSeqNo = groupLines.get(groupLines.size()-1).getContourSequence();
			
			LineData groupLine = new LineData();
			groupLine.setStartPointData(groupStartPoint);
			groupLine.setEndPointData(groupEndPoint);
			//int totalLength = groupLine.getLength();
			//System.out.println("Group Total Length:: "+totalLength);
			
			Core.line(drawContour, new Point(groupLine.getStartPointData().getX(),groupLine.getStartPointData().getY()), new Point(groupLine.getEndPointData().getX(),groupLine.getEndPointData().getY()), Scalar.all(0));
			
			for(LineData lineData:groupLines){
				System.out.println("Seq:"+lineData.getContourSequence()+", start:"+lineData.getStartPointData()+", end"+lineData.getEndPointData()+", Length:"+lineData.getLength());
			}
			
		}*/
		
		for(LineData tmpLine:hzLines){
			Core.line(drawContourHz, new Point(tmpLine.getStartPointData().getX(),tmpLine.getStartPointData().getY()), new Point(tmpLine.getEndPointData().getX(),tmpLine.getEndPointData().getY()), Scalar.all(0));
		}
		
		for(LineData tmpLine:vertLines){
			Core.line(drawContourVrt, new Point(tmpLine.getStartPointData().getX(),tmpLine.getStartPointData().getY()), new Point(tmpLine.getEndPointData().getX(),tmpLine.getEndPointData().getY()), Scalar.all(0));
		}
		
		Highgui.imwrite("image-destination1/contours-sub-hz.png", drawContourHz);
		Highgui.imwrite("image-destination1/contours-sub-vt.png", drawContourVrt);
		
		//Imgproc.cont
		
		
		
	}

}
