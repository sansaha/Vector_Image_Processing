package com.lexmark;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import com.lexmark.utils.ContourWrapper;
import com.lexmark.utils.LineData;
import com.lexmark.utils.LineTypeEnum;
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
	
	public static MatOfPoint processPoints(MatOfPoint matOfPoint){
		
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
		
		
		Map<Integer,List<LineData>> lineRegionMap = new LinkedHashMap<Integer,List<LineData>>();
		
		
		int groupNo = 1;
		
		Mat drawContour = new Mat(2000,2000,CvType.CV_8UC1,Scalar.all(255));
		//Mat drawContourHz = new Mat(2000,2000,CvType.CV_8UC1,Scalar.all(255));
		//Mat drawContourVrt = drawContourHz.clone();
		int totalLine = lines.size();
		for(LineData lineData:lines){
			if(!lineRegionMap.containsKey(groupNo)){
				lineRegionMap.put(groupNo, new ArrayList<LineData>());
			}
			
			List<LineData> regionLineList = lineRegionMap.get(groupNo);
			
			if(regionLineList.size() > 0){
				//LineData lineDataLargest = LineUtils.getLargestLine(regionLineList);
				LineData lineDataPrevious = regionLineList.get(regionLineList.size()-1);
				LineData lineDataPreviousToPrevious = null;
				if(regionLineList.size() > 1){
					lineDataPreviousToPrevious = regionLineList.get(regionLineList.size()-2);
				}
				
				LineData lineDataNext = null;
				
				if(lines.indexOf(lineData) != totalLine-1){
					lineDataNext = lines.get(lines.indexOf(lineData)+1);
				}
				
				
				int dX = Math.abs(lineDataPrevious.getEndPointData().getX()-lineData.getEndPointData().getX());
				int dY = Math.abs(lineDataPrevious.getEndPointData().getY()-lineData.getEndPointData().getY());
				
				//System.out.println(dX+","+dY+","+lengthPercentage);
				if((dX <= 3 && dY <=3)
						|| (lineDataNext != null && getLengthPercentageWithLargestLine(lineData, lineDataPrevious,lineDataNext) <= 2 && ((lineDataPrevious.isHorizontal() && lineDataNext.isHorizontal()) || (lineDataPrevious.isVertical() && lineDataNext.isVertical()) || (lineDataPrevious.isIncliend() && lineDataNext.isIncliend())))
						|| (lineDataPreviousToPrevious != null && getLengthPercentageWithLargestLine(lineDataPrevious, lineDataPreviousToPrevious,lineData) <= 2 && ((lineDataPreviousToPrevious.isHorizontal() && lineData.isHorizontal()) || (lineDataPreviousToPrevious.isVertical() && lineData.isVertical()) || (lineDataPreviousToPrevious.isIncliend() && lineData.isIncliend())))
						){
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
			
		}
		
		System.out.println("Total region:: "+lineRegionMap.keySet().size());
		
		List<Point> finalPointList = new ArrayList<Point>();
		
		for(Integer groupNoKey:lineRegionMap.keySet()){
			List<LineData> groupLines = lineRegionMap.get(groupNoKey);
			
			System.out.println("Region#"+groupNoKey+", No of line:"+groupLines.size());
			
			if(groupLines.size() == 1){
				Core.line(drawContour, new Point(groupLines.get(0).getStartPointData().getX(),groupLines.get(0).getStartPointData().getY()), new Point(groupLines.get(0).getEndPointData().getX(),groupLines.get(0).getEndPointData().getY()), Scalar.all(0));
				continue;
			}
			
			int lineTypes = getLineTypeCount(groupLines);
			
			if(lineTypes > 2){
				//TODO further split is required
				/*for(LineData lineDataTmp:groupLines){
					
				}*/
				System.out.println("Further split required in Region#"+groupNoKey);
				List<LineData> subGroupList = new ArrayList<LineData>();
				for(int i = 0; i <groupLines.size(); i++ ){
					LineData groupLineData = groupLines.get(i);
					subGroupList.add(groupLineData);
					if(getLineTypeCount(subGroupList) <=2){
						continue;
					}else{
						subGroupList.remove(groupLineData);
						i--;
						processRegionPoints(subGroupList, finalPointList);
						subGroupList.clear();
					}
					
				}
				processRegionPoints(subGroupList, finalPointList);
				
				
			}else{
				processRegionPoints(groupLines, finalPointList);
			}
			
			/*int hZLineTotalLength = getTotalLengthByLineType(groupLines, LineTypeEnum.HORIZONTAL);
			int vTLineTotalLength = getTotalLengthByLineType(groupLines, LineTypeEnum.VERTICAL);
			int incLineTotalLength = getTotalLengthByLineType(groupLines, LineTypeEnum.INCLIEND);
			
			int maxLength = LineUtils.getMaxVal(hZLineTotalLength,vTLineTotalLength,incLineTotalLength);
			
			PointData groupStartPoint = groupLines.get(0).getStartPointData();
			
			int startSeqNo = groupLines.get(0).getContourSequence();
			PointData groupEndPoint = groupLines.get(groupLines.size()-1).getEndPointData();
			int endSeqNo = groupLines.get(groupLines.size()-1).getContourSequence();
			
			//LineData lineDataLargest = LineUtils.getLargestLine(groupLines);
			LineData lineDataLargest = null;
			
			LineData mergedLargestLine = new LineData();
			LineData startJoinLine = null;
			LineData endJoinLine = null;
			
			//if(lineDataLargest.isHorizontal()){
			if(maxLength == hZLineTotalLength){
				lineDataLargest = LineUtils.getLargestLineByType(groupLines, LineTypeEnum.HORIZONTAL);
				if(lineDataLargest.getContourSequence() == startSeqNo){
					mergedLargestLine.setStartPointData(groupStartPoint.clonePointData());
					mergedLargestLine.setEndPointData(new PointData(groupEndPoint.getX(), groupStartPoint.getY()));
					endJoinLine = new LineData();
					endJoinLine.setStartPointData(mergedLargestLine.getEndPointData().clonePointData());
					endJoinLine.setEndPointData(groupEndPoint.clonePointData());
				}else if(lineDataLargest.getContourSequence() == endSeqNo){
					mergedLargestLine.setStartPointData(new PointData(groupStartPoint.getX(),groupEndPoint.getY()));
					mergedLargestLine.setEndPointData(groupEndPoint.clonePointData());
					startJoinLine = new LineData();
					startJoinLine.setStartPointData(groupStartPoint.clonePointData());
					startJoinLine.setEndPointData(mergedLargestLine.getStartPointData().clonePointData());
				}else{
					mergedLargestLine.setStartPointData(new PointData(groupStartPoint.getX(), lineDataLargest.getStartPointData().getY()));
					mergedLargestLine.setEndPointData(new PointData(groupEndPoint.getX(),lineDataLargest.getStartPointData().getY()));
					startJoinLine = new LineData();
					startJoinLine.setStartPointData(groupStartPoint.clonePointData());
					startJoinLine.setEndPointData(mergedLargestLine.getStartPointData().clonePointData());
					endJoinLine = new LineData();
					endJoinLine.setStartPointData(mergedLargestLine.getEndPointData().clonePointData());
					endJoinLine.setEndPointData(groupEndPoint.clonePointData());
				}
				
			//}else if(lineDataLargest.isVertical()){
			}else if(maxLength == vTLineTotalLength){
				lineDataLargest = LineUtils.getLargestLineByType(groupLines, LineTypeEnum.VERTICAL);
				if(lineDataLargest.getContourSequence() == startSeqNo){
					mergedLargestLine.setStartPointData(groupStartPoint.clonePointData());
					mergedLargestLine.setEndPointData(new PointData(groupStartPoint.getX(), groupEndPoint.getY()));
					endJoinLine = new LineData();
					endJoinLine.setStartPointData(mergedLargestLine.getEndPointData().clonePointData());
					endJoinLine.setEndPointData(groupEndPoint.clonePointData());
				}else if(lineDataLargest.getContourSequence() == endSeqNo){
					mergedLargestLine.setStartPointData(new PointData(groupEndPoint.getX(),groupStartPoint.getY()));
					mergedLargestLine.setEndPointData(groupEndPoint.clonePointData());
					startJoinLine = new LineData();
					startJoinLine.setStartPointData(groupStartPoint.clonePointData());
					startJoinLine.setEndPointData(mergedLargestLine.getStartPointData().clonePointData());
				}else{
					mergedLargestLine.setStartPointData(new PointData(lineDataLargest.getStartPointData().getX(), groupStartPoint.getY()));
					mergedLargestLine.setEndPointData(new PointData(lineDataLargest.getStartPointData().getX(),groupEndPoint.getY()));
					startJoinLine = new LineData();
					startJoinLine.setStartPointData(groupStartPoint.clonePointData());
					startJoinLine.setEndPointData(mergedLargestLine.getStartPointData().clonePointData());
					endJoinLine = new LineData();
					endJoinLine.setStartPointData(mergedLargestLine.getEndPointData().clonePointData());
					endJoinLine.setEndPointData(groupEndPoint.clonePointData());
				}
				
			}else{
				//inclined
				//TODO
				mergedLargestLine.setStartPointData(groupStartPoint);
				mergedLargestLine.setEndPointData(groupEndPoint);
			}
			
			
			if(startJoinLine != null){
				Point startPoint = new Point(startJoinLine.getStartPointData().getX(),startJoinLine.getStartPointData().getY());
				Point endPoint = new Point(startJoinLine.getEndPointData().getX(),startJoinLine.getEndPointData().getY());
				Core.line(drawContour,startPoint,endPoint,Scalar.all(0));
				if(!finalPointList.contains(startPoint)){
					finalPointList.add(startPoint);
				}
				if(!finalPointList.contains(endPoint)){
					finalPointList.add(endPoint);
				}
			}
			
			if(mergedLargestLine != null){
				Point startPoint = new Point(mergedLargestLine.getStartPointData().getX(),mergedLargestLine.getStartPointData().getY());
				Point endPoint = new Point(mergedLargestLine.getEndPointData().getX(),mergedLargestLine.getEndPointData().getY());
				Core.line(drawContour,startPoint,endPoint,Scalar.all(0));
				if(!finalPointList.contains(startPoint)){
					finalPointList.add(startPoint);
				}
				if(!finalPointList.contains(endPoint)){
					finalPointList.add(endPoint);
				}
			}
			
			
			if(endJoinLine != null){
				Point startPoint = new Point(endJoinLine.getStartPointData().getX(),endJoinLine.getStartPointData().getY());
				Point endPoint = new Point(endJoinLine.getEndPointData().getX(),endJoinLine.getEndPointData().getY());
				Core.line(drawContour,startPoint,endPoint,Scalar.all(0));
				if(!finalPointList.contains(startPoint)){
					finalPointList.add(startPoint);
				}
				if(!finalPointList.contains(endPoint)){
					finalPointList.add(endPoint);
				}
			}*/
						
		}
		
		
		MatOfPoint processedMatOfPoint = new MatOfPoint(finalPointList.toArray(new Point[]{}));
		
		//Highgui.imwrite("image-destination1/contours-processed.png", drawContour);
		
		return processedMatOfPoint;	
		
	}
	
	private static int getLineTypeCount(List<LineData> lineData ){
		int count = 0;
		boolean hzLinePresent = false;
		boolean vrtLinePresent = false;
		boolean inclinedLinePresent = false;
		for(LineData lineDataTmp:lineData){
			if(lineDataTmp.isVertical()){
				vrtLinePresent = true;
				continue;
			}
			if(lineDataTmp.isHorizontal()){
				hzLinePresent = true;
				continue;
			}
			if(lineDataTmp.isIncliend()){
				inclinedLinePresent = true;
			}	
		}
		
		if(hzLinePresent){
			count++;
		}
		if(vrtLinePresent){
			count++;
		}
		if(inclinedLinePresent){
			count++;
		}
		return count;
	}
	
	private static void processRegionPoints(List<LineData> regionLines,List<Point> finalPointList){
		
		int hZLineTotalLength = getTotalLengthByLineType(regionLines, LineTypeEnum.HORIZONTAL);
		int vTLineTotalLength = getTotalLengthByLineType(regionLines, LineTypeEnum.VERTICAL);
		int incLineTotalLength = getTotalLengthByLineType(regionLines, LineTypeEnum.INCLIEND);
		
		int maxLength = LineUtils.getMaxVal(hZLineTotalLength,vTLineTotalLength,incLineTotalLength);
		
		PointData groupStartPoint = regionLines.get(0).getStartPointData();
		
		int startSeqNo = regionLines.get(0).getContourSequence();
		PointData groupEndPoint = regionLines.get(regionLines.size()-1).getEndPointData();
		int endSeqNo = regionLines.get(regionLines.size()-1).getContourSequence();
		
		//LineData lineDataLargest = LineUtils.getLargestLine(groupLines);
		LineData lineDataLargest = null;
		
		LineData mergedLargestLine = new LineData();
		LineData startJoinLine = null;
		LineData endJoinLine = null;
		
		//if(lineDataLargest.isHorizontal()){
		if(maxLength == hZLineTotalLength){
			lineDataLargest = LineUtils.getLargestLineByType(regionLines, LineTypeEnum.HORIZONTAL);
			if(lineDataLargest.getContourSequence() == startSeqNo){
				mergedLargestLine.setStartPointData(groupStartPoint.clonePointData());
				mergedLargestLine.setEndPointData(new PointData(groupEndPoint.getX(), groupStartPoint.getY()));
				endJoinLine = new LineData();
				endJoinLine.setStartPointData(mergedLargestLine.getEndPointData().clonePointData());
				endJoinLine.setEndPointData(groupEndPoint.clonePointData());
			}else if(lineDataLargest.getContourSequence() == endSeqNo){
				mergedLargestLine.setStartPointData(new PointData(groupStartPoint.getX(),groupEndPoint.getY()));
				mergedLargestLine.setEndPointData(groupEndPoint.clonePointData());
				startJoinLine = new LineData();
				startJoinLine.setStartPointData(groupStartPoint.clonePointData());
				startJoinLine.setEndPointData(mergedLargestLine.getStartPointData().clonePointData());
			}else{
				mergedLargestLine.setStartPointData(new PointData(groupStartPoint.getX(), lineDataLargest.getStartPointData().getY()));
				mergedLargestLine.setEndPointData(new PointData(groupEndPoint.getX(),lineDataLargest.getStartPointData().getY()));
				startJoinLine = new LineData();
				startJoinLine.setStartPointData(groupStartPoint.clonePointData());
				startJoinLine.setEndPointData(mergedLargestLine.getStartPointData().clonePointData());
				endJoinLine = new LineData();
				endJoinLine.setStartPointData(mergedLargestLine.getEndPointData().clonePointData());
				endJoinLine.setEndPointData(groupEndPoint.clonePointData());
			}
			
		//}else if(lineDataLargest.isVertical()){
		}else if(maxLength == vTLineTotalLength){
			lineDataLargest = LineUtils.getLargestLineByType(regionLines, LineTypeEnum.VERTICAL);
			if(lineDataLargest.getContourSequence() == startSeqNo){
				mergedLargestLine.setStartPointData(groupStartPoint.clonePointData());
				mergedLargestLine.setEndPointData(new PointData(groupStartPoint.getX(), groupEndPoint.getY()));
				endJoinLine = new LineData();
				endJoinLine.setStartPointData(mergedLargestLine.getEndPointData().clonePointData());
				endJoinLine.setEndPointData(groupEndPoint.clonePointData());
			}else if(lineDataLargest.getContourSequence() == endSeqNo){
				mergedLargestLine.setStartPointData(new PointData(groupEndPoint.getX(),groupStartPoint.getY()));
				mergedLargestLine.setEndPointData(groupEndPoint.clonePointData());
				startJoinLine = new LineData();
				startJoinLine.setStartPointData(groupStartPoint.clonePointData());
				startJoinLine.setEndPointData(mergedLargestLine.getStartPointData().clonePointData());
			}else{
				mergedLargestLine.setStartPointData(new PointData(lineDataLargest.getStartPointData().getX(), groupStartPoint.getY()));
				mergedLargestLine.setEndPointData(new PointData(lineDataLargest.getStartPointData().getX(),groupEndPoint.getY()));
				startJoinLine = new LineData();
				startJoinLine.setStartPointData(groupStartPoint.clonePointData());
				startJoinLine.setEndPointData(mergedLargestLine.getStartPointData().clonePointData());
				endJoinLine = new LineData();
				endJoinLine.setStartPointData(mergedLargestLine.getEndPointData().clonePointData());
				endJoinLine.setEndPointData(groupEndPoint.clonePointData());
			}
			
		}else{
			//inclined
			//TODO
			mergedLargestLine.setStartPointData(groupStartPoint);
			mergedLargestLine.setEndPointData(groupEndPoint);
		}
		
		
		if(startJoinLine != null){
			Point startPoint = new Point(startJoinLine.getStartPointData().getX(),startJoinLine.getStartPointData().getY());
			Point endPoint = new Point(startJoinLine.getEndPointData().getX(),startJoinLine.getEndPointData().getY());
			//Core.line(drawContour,startPoint,endPoint,Scalar.all(0));
			if(!finalPointList.contains(startPoint)){
				finalPointList.add(startPoint);
			}
			if(!finalPointList.contains(endPoint)){
				finalPointList.add(endPoint);
			}
		}
		
		if(mergedLargestLine != null){
			Point startPoint = new Point(mergedLargestLine.getStartPointData().getX(),mergedLargestLine.getStartPointData().getY());
			Point endPoint = new Point(mergedLargestLine.getEndPointData().getX(),mergedLargestLine.getEndPointData().getY());
			//Core.line(drawContour,startPoint,endPoint,Scalar.all(0));
			if(!finalPointList.contains(startPoint)){
				finalPointList.add(startPoint);
			}
			if(!finalPointList.contains(endPoint)){
				finalPointList.add(endPoint);
			}
		}
		
		
		if(endJoinLine != null){
			Point startPoint = new Point(endJoinLine.getStartPointData().getX(),endJoinLine.getStartPointData().getY());
			Point endPoint = new Point(endJoinLine.getEndPointData().getX(),endJoinLine.getEndPointData().getY());
			//Core.line(drawContour,startPoint,endPoint,Scalar.all(0));
			if(!finalPointList.contains(startPoint)){
				finalPointList.add(startPoint);
			}
			if(!finalPointList.contains(endPoint)){
				finalPointList.add(endPoint);
			}
		}
	}
	
	private static int getLengthPercentageWithLargestLine(LineData refLine,LineData... lines){
		int percentage = 0;
		LineData largestLine = LineUtils.getLargestLine(Arrays.asList(lines));
		percentage = (refLine.getLength()*100)/largestLine.getLength();
		//System.out.println("percentage::"+percentage);
		return percentage;
	}
	
	/*private static int getLengthPercentageWithSmallestLine(LineData refLine,LineData... lines){
		int percentage = 0;
		LineData smallestLine = LineUtils.getSmallestLine(lines);
		percentage = (refLine.getLength()*100)/smallestLine.getLength();
		//System.out.println("percentage::"+percentage);
		return percentage;
	}*/
	
	private static int getTotalLengthByLineType(List<LineData> lines,LineTypeEnum lineType){
		
		int totLength = 0;
		for(LineData lineData:lines){
			if(LineTypeEnum.HORIZONTAL == lineType && lineData.isHorizontal()){
				totLength = totLength + lineData.getLength();
			}else if(LineTypeEnum.VERTICAL == lineType && lineData.isVertical()){
				totLength = totLength + lineData.getLength();
			}else if(LineTypeEnum.INCLIEND == lineType && lineData.isIncliend()){
				totLength = totLength + lineData.getLength();
			}
		}
		
		return totLength;
	}

}
