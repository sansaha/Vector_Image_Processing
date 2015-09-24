package com.lexmark;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
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
	
	private static final Logger logger = Logger.getLogger(ContourHelper.class);

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
	
	
	public static MatOfPoint processPoints(MatOfPoint matOfPoint){
		
		logger.info("Source"+matOfPoint.dump());
		
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
		
		boolean continuousContour = matOfPoint.isContinuous();
		
		if(continuousContour){
			//add the last line
			LineData lineData = new LineData();
			Point endPoint = points.get(0);
			Point startPoint = points.get(points.size()-1);
			lineData.setStartPointData(new PointData((int)startPoint.x, (int)startPoint.y));
			lineData.setEndPointData(new PointData((int)endPoint.x, (int)endPoint.y));
			lineData.setContourSequence(pointCount-1);
			lines.add(lineData);
		}
		
		System.out.println("Total lines:: "+lines.size());
		
		
		Map<Integer,List<LineData>> lineRegionMap = createGroup(lines);
		
		Mat drawContour = new Mat(2000,2000,CvType.CV_8UC1,Scalar.all(255));
		
		System.out.println("Total region:: "+lineRegionMap.keySet().size());
		
		List<Point> finalPointList = new ArrayList<Point>();
		
		List<LineData> initialProcessedLines = new ArrayList<>();
		
		for(Integer groupNoKey:lineRegionMap.keySet()){
			List<LineData> groupLines = lineRegionMap.get(groupNoKey);
			
			System.out.println("Region#"+groupNoKey+", No of line:"+groupLines.size());
			
			if(groupLines.size() == 1){
				Core.line(drawContour, new Point(groupLines.get(0).getStartPointData().getX(),groupLines.get(0).getStartPointData().getY()), new Point(groupLines.get(0).getEndPointData().getX(),groupLines.get(0).getEndPointData().getY()), Scalar.all(0));
				//finalPointList.add(new Point(groupLines.get(0).getStartPointData().getX(),groupLines.get(0).getStartPointData().getY()));
				//finalPointList.add(new Point(groupLines.get(0).getEndPointData().getX(),groupLines.get(0).getEndPointData().getY()));
				initialProcessedLines.add(groupLines.get(0));
				continue;
			}
			
			int lineTypes = getLineTypeCount(groupLines);
			
			if(lineTypes > 2){
				//further split is required
				
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
						List<LineData> tmpProcessedLines = processRegionPoints(subGroupList);
						initialProcessedLines.addAll(tmpProcessedLines);
						subGroupList.clear();
					}
					
				}
				List<LineData> tmpProcessedLines = processRegionPoints(subGroupList);
				initialProcessedLines.addAll(tmpProcessedLines);
				
				
			}else{
				List<LineData> tmpProcessedLines = processRegionPoints(groupLines);
				initialProcessedLines.addAll(tmpProcessedLines);
			}
									
		}
		
		int initialLineCount = initialProcessedLines.size();
		List<LineData> mergedLines = mergeLinesForward(initialProcessedLines, continuousContour);
		int mergeLineCount = mergedLines.size();
		System.out.println("Before call merge line in loop");
		while(mergeLineCount < initialLineCount){
			System.out.println("In call merge line loop");
			initialLineCount = mergeLineCount;
			mergedLines = mergeLinesForward(mergedLines, continuousContour);
			mergeLineCount = mergedLines.size();
		}
		
		populatePoints(mergedLines, finalPointList);
		
		
		MatOfPoint processedMatOfPoint = new MatOfPoint(finalPointList.toArray(new Point[]{}));
		
		
		/*List<LineData> newLines = new ArrayList<LineData>(finalPointList.size());
		
		int newPointCount = finalPointList.size();
		
		for (int i = 0; i < newPointCount-1; i++) {
			LineData lineData = new LineData();
			Point startPoint = finalPointList.get(i);
			Point endPoint = finalPointList.get(i+1);
			
			lineData.setStartPointData(new PointData((int)startPoint.x, (int)startPoint.y));
			lineData.setEndPointData(new PointData((int)endPoint.x, (int)endPoint.y));
			lineData.setContourSequence(i);
			newLines.add(lineData);
		}
		
		if(continuousContour){
			//add the last line
			LineData lineData = new LineData();
			Point endPoint = finalPointList.get(0);
			Point startPoint = finalPointList.get(finalPointList.size()-1);
			lineData.setStartPointData(new PointData((int)startPoint.x, (int)startPoint.y));
			lineData.setEndPointData(new PointData((int)endPoint.x, (int)endPoint.y));
			lineData.setContourSequence(pointCount-1);
			newLines.add(lineData);
		}*/
		
		//Map<Integer,List<LineData>> lineRegionMapFinal = createGroup(newLines);
		
		/*for(Integer groupNoKey:lineRegionMapFinal.keySet()){
			List<LineData> groupLines = lineRegionMapFinal.get(groupNoKey);
			
			System.out.println("Region#"+groupNoKey+", No of line:"+groupLines.size());
			
			if(groupLines.size() == 1){
				//Core.line(drawContour, new Point(groupLines.get(0).getStartPointData().getX(),groupLines.get(0).getStartPointData().getY()), new Point(groupLines.get(0).getEndPointData().getX(),groupLines.get(0).getEndPointData().getY()), Scalar.all(0));
				continue;
			}
			
			int lineTypes = getLineTypeCount(groupLines);
			
			if(lineTypes > 2){
				//further split is required
				
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
									
		}*/
		
		//Highgui.imwrite("image-destination1/contours-processed.png", drawContour);
		
		logger.info("Result"+processedMatOfPoint.dump());
		
		return processedMatOfPoint;	
		
	}
	
	private static void populatePoints(List<LineData> lines,List<Point> allPoints){
		for(LineData lineData:lines){
			Point startPoint = new Point(lineData.getStartPointData().getX(),lineData.getStartPointData().getY());
			Point endPoint = new Point(lineData.getEndPointData().getX(),lineData.getEndPointData().getY());
			
			if(!allPoints.contains(startPoint)){
				allPoints.add(startPoint);
			}
			if(!allPoints.contains(endPoint)){
				allPoints.add(endPoint);
			}
		}
	}
	
	
	private static List<LineData> mergeLinesForward(List<LineData> lines,boolean continuous){
		
		LineData currentLine = null;
		LineData nextLine = null;
		LineData nextToNextLine = null;
		
		List<LineData> mergedLines = new ArrayList<>();
		
		int totalLines = lines.size();
		
		for (int i = 0; i < totalLines-2; i++) {
			
			currentLine = lines.get(i);
			
			if(currentLine.isIgnore()){
				continue;
			}
			
			if(totalLines > (i+1)){
				nextLine = lines.get(i+1);
			}
			
			if(totalLines > (i+2)){
				nextToNextLine = lines.get(i+2);
			}
			
			if(currentLine != null && nextLine != null && nextToNextLine != null){
				if((currentLine.isVertical() && nextToNextLine.isVertical()) || 
						(currentLine.isHorizontal() && nextToNextLine.isHorizontal()) ||
						(currentLine.isIncliend() && nextToNextLine.isIncliend())){
					int percentage = getLengthPercentageWithLargestLine(nextLine, currentLine,nextToNextLine);
					if(percentage < 8){
						//merge
						LineData largestLine = null;
						LineData joinLineStart = null;
						LineData joinLineEnd = null;
						if(currentLine.getLength() >= nextToNextLine.getLength()){
							largestLine = currentLine;
							joinLineEnd = new LineData();
						}else{
							joinLineStart = new LineData();
							largestLine = nextToNextLine;
						}
						
						PointData startPoint = currentLine.getStartPointData();
						PointData endPoint = nextToNextLine.getEndPointData();
						
						//LineData joinLine = null;
						LineData mergedLine = new LineData();
						
						if(largestLine.isHorizontal()){
							
							int y = largestLine.getStartPointData().getY();
							int x1 = startPoint.getX();
							int x2 = endPoint.getX();
							mergedLine.setStartPointData(new PointData(x1, y));
							mergedLine.setEndPointData(new PointData(x2, y));
							if(joinLineStart != null){
								joinLineStart.setStartPointData(startPoint.clonePointData());
								joinLineStart.setEndPointData(mergedLine.getStartPointData().clonePointData());
							}else if(joinLineEnd != null){
								joinLineEnd.setStartPointData(mergedLine.getEndPointData().clonePointData());
								joinLineEnd.setEndPointData(endPoint.clonePointData());
							}
							
						}else if(largestLine.isVertical()){
							
							int x = largestLine.getStartPointData().getX();
							int y1 = startPoint.getY();
							int y2 = endPoint.getY();
							mergedLine.setStartPointData(new PointData(x, y1));
							mergedLine.setEndPointData(new PointData(x, y2));
							if(joinLineStart != null){
								joinLineStart.setStartPointData(startPoint.clonePointData());
								joinLineStart.setEndPointData(mergedLine.getStartPointData().clonePointData());
							}else if(joinLineEnd != null){
								joinLineEnd.setStartPointData(mergedLine.getEndPointData().clonePointData());
								joinLineEnd.setEndPointData(endPoint.clonePointData());
							}
							
							
						}else if(largestLine.isIncliend()){
							mergedLine.setStartPointData(startPoint);
							mergedLine.setEndPointData(endPoint);
						}
						
						nextLine.setIgnore(true);
						
						if(joinLineStart != null){
							currentLine.setStartPointData(joinLineStart.getStartPointData().clonePointData());
							currentLine.setEndPointData(joinLineStart.getEndPointData().clonePointData());
														
							nextToNextLine.setStartPointData(mergedLine.getStartPointData().clonePointData());
							nextToNextLine.setEndPointData(mergedLine.getEndPointData().clonePointData());

						}else if(joinLineEnd != null){
							
							currentLine.setStartPointData(mergedLine.getStartPointData().clonePointData());
							currentLine.setEndPointData(mergedLine.getEndPointData().clonePointData());
														
							nextToNextLine.setStartPointData(joinLineEnd.getStartPointData().clonePointData());
							nextToNextLine.setEndPointData(joinLineEnd.getEndPointData().clonePointData());
						} else if(joinLineStart == null && joinLineEnd == null){
							currentLine.setIgnore(true);
							nextToNextLine.setStartPointData(mergedLine.getStartPointData().clonePointData());
							nextToNextLine.setEndPointData(mergedLine.getEndPointData().clonePointData());
						}
						
						
					}
					
				}
			}
			
		}
		
		for(LineData lineData:lines){
			if(lineData.isIgnore()){
				continue;
			}
			mergedLines.add(lineData);
		}
		
		return mergedLines;
	}
	
	
	private static Map<Integer,List<LineData>> createGroup(List<LineData> lines){
		
		Map<Integer,List<LineData>> lineRegionMap = new LinkedHashMap<Integer,List<LineData>>();
		
		int groupNo = 1;
		
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
		
		return lineRegionMap;
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
	
	private static List<LineData> processRegionPoints(List<LineData> regionLines){
		
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
				endJoinLine.setImaginary(true);
				endJoinLine.setStartPointData(mergedLargestLine.getEndPointData().clonePointData());
				endJoinLine.setEndPointData(groupEndPoint.clonePointData());
			}else if(lineDataLargest.getContourSequence() == endSeqNo){
				mergedLargestLine.setStartPointData(new PointData(groupStartPoint.getX(),groupEndPoint.getY()));
				mergedLargestLine.setEndPointData(groupEndPoint.clonePointData());
				startJoinLine = new LineData();
				startJoinLine.setImaginary(true);
				startJoinLine.setStartPointData(groupStartPoint.clonePointData());
				startJoinLine.setEndPointData(mergedLargestLine.getStartPointData().clonePointData());
			}else{
				mergedLargestLine.setStartPointData(new PointData(groupStartPoint.getX(), lineDataLargest.getStartPointData().getY()));
				mergedLargestLine.setEndPointData(new PointData(groupEndPoint.getX(),lineDataLargest.getStartPointData().getY()));
				startJoinLine = new LineData();
				startJoinLine.setImaginary(true);
				startJoinLine.setStartPointData(groupStartPoint.clonePointData());
				startJoinLine.setEndPointData(mergedLargestLine.getStartPointData().clonePointData());
				endJoinLine = new LineData();
				endJoinLine.setImaginary(true);
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
				endJoinLine.setImaginary(true);
				endJoinLine.setStartPointData(mergedLargestLine.getEndPointData().clonePointData());
				endJoinLine.setEndPointData(groupEndPoint.clonePointData());
			}else if(lineDataLargest.getContourSequence() == endSeqNo){
				mergedLargestLine.setStartPointData(new PointData(groupEndPoint.getX(),groupStartPoint.getY()));
				mergedLargestLine.setEndPointData(groupEndPoint.clonePointData());
				startJoinLine = new LineData();
				startJoinLine.setImaginary(true);
				startJoinLine.setStartPointData(groupStartPoint.clonePointData());
				startJoinLine.setEndPointData(mergedLargestLine.getStartPointData().clonePointData());
			}else{
				mergedLargestLine.setStartPointData(new PointData(lineDataLargest.getStartPointData().getX(), groupStartPoint.getY()));
				mergedLargestLine.setEndPointData(new PointData(lineDataLargest.getStartPointData().getX(),groupEndPoint.getY()));
				startJoinLine = new LineData();
				startJoinLine.setImaginary(true);
				startJoinLine.setStartPointData(groupStartPoint.clonePointData());
				startJoinLine.setEndPointData(mergedLargestLine.getStartPointData().clonePointData());
				endJoinLine = new LineData();
				endJoinLine.setImaginary(true);
				endJoinLine.setStartPointData(mergedLargestLine.getEndPointData().clonePointData());
				endJoinLine.setEndPointData(groupEndPoint.clonePointData());
			}
			
		}else{
			//inclined
			mergedLargestLine.setStartPointData(groupStartPoint);
			mergedLargestLine.setEndPointData(groupEndPoint);
		}
		
		List<LineData> processedLines = new ArrayList<>(3);
		
		if(startJoinLine != null){
			processedLines.add(startJoinLine);
		}
		
		if(mergedLargestLine != null){
			processedLines.add(mergedLargestLine);
		}
		
		
		if(endJoinLine != null){
			processedLines.add(endJoinLine);
		}
		
		return processedLines;
	}
	
	private static int getLengthPercentageWithLargestLine(LineData refLine,LineData... lines){
		int percentage = 0;
		LineData largestLine = LineUtils.getLargestLine(Arrays.asList(lines));
		int largestLinLength = largestLine.getLength();
		if(largestLinLength == 0){
			largestLinLength = 1;
		}
		percentage = (refLine.getLength()*100)/largestLinLength;
		//System.out.println("percentage::"+percentage);
		return percentage;
	}
	
	/*private static int getLengthPercentageWithSmallestLine(LineData refLine,LineData... lines){
		int percentage = 0;
		LineData smallestLine = LineUtils.getSmallestLine(lines);
		int smallestLinLength = smallestLine.getLength();
		if(smallestLinLength == 0){
			smallestLinLength = 1;
		}
		percentage = (refLine.getLength()*100)/smallestLinLength;
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
