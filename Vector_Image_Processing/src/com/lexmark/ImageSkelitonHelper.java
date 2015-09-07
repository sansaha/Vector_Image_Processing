package com.lexmark;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import com.lexmark.utils.LineData;
import com.lexmark.utils.LineGroup;
import com.lexmark.utils.PointData;
import com.lexmark.utils.RegressionUtils;

public class ImageSkelitonHelper {

	private static final Logger logger = Logger.getLogger(ImageSkelitonHelper.class);
	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
	
	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		//String fileName = args.length >= 1 ? args[0] : "image-source/image-vessel.png";
		String fileName = args.length >= 1 ? args[0] : "image-source/input.png";
		//String fileName = args.length >= 1 ? args[0] : "image-source/input-skeliton-2.png";
		
		//getMorphologicalSkeleton1(fileName);
		getMorphologicalSkeleton2(fileName);
	}
	/**
	 * @param args
	 */
	public static void getMorphologicalSkeleton1(String fileName) {
		
		String dateTimeComponent = dateFormat.format(new Date());
		
		Mat sourceImage = null;
		
		//sourceImage = Imgcodecs.imread(fileName);
		sourceImage = Highgui.imread(fileName);
		
		//System.out.println(sourceImage);
		
		Mat sourceImageGrey = new Mat(sourceImage.rows(),sourceImage.cols(),CvType.CV_8UC1);
		
		Imgproc.cvtColor(sourceImage, sourceImageGrey,  Imgproc.COLOR_RGB2GRAY);
		//System.out.println(sourceImageGrey);
		
		int scaleFactor = 3;
		
		Size scaledSize = new Size(sourceImageGrey.size().width*scaleFactor, sourceImageGrey.size().height*scaleFactor);
		Mat sourceImageGreyScaled = new Mat(scaledSize,CvType.CV_8UC1);
		
		Imgproc.resize(sourceImageGrey, sourceImageGreyScaled,scaledSize,0,0,Imgproc.INTER_CUBIC);
		
		Imgproc.equalizeHist(sourceImageGreyScaled, sourceImageGreyScaled);
		Highgui.imwrite("image-destination/histeq"+dateTimeComponent+".png", sourceImageGreyScaled);
		Mat sourceImageBinary = new Mat(sourceImageGreyScaled.rows(),sourceImageGreyScaled.cols(),CvType.CV_8UC1,Scalar.all(255.0));
		Mat erodeImageBinary = new Mat(sourceImageGreyScaled.rows(),sourceImageGreyScaled.cols(),CvType.CV_8UC1,Scalar.all(255.0));
		//Mat dilateImageBinary = new Mat(sourceImageGreyScaled.rows(),sourceImageGreyScaled.cols(),CvType.CV_8UC1,Scalar.all(255.0));
		Imgproc.erode(sourceImageGreyScaled, erodeImageBinary, Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(2,2)), new Point(0, 0), 4);
		Highgui.imwrite("image-destination/erode"+dateTimeComponent+".png", erodeImageBinary);
		Imgproc.threshold(erodeImageBinary, sourceImageBinary, 120, 255, Imgproc.THRESH_BINARY);	
		
		
		/*Imgcodecs.imwrite("image-destination/input-thresold-"+dateTimeComponent+".png", sourceImageBinary);
		Imgproc.dilate(sourceImageGreyScaled, dilateImageBinary, Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(2,2)), new Point(0, 0), 1);
		Core.add(erodeImageBinary, dilateImageBinary, sourceImageBinary);
		Imgcodecs.imwrite("image-destination/erode-dilate"+dateTimeComponent+".png", sourceImageBinary);
		
		
		
		
		Imgproc.threshold(sourceImageBinary, sourceImageBinary, 120, 255, Imgproc.THRESH_BINARY);
		Imgcodecs.imwrite("image-destination/erode-dilate-thersold-"+dateTimeComponent+".png", sourceImageBinary);*/
		
		//TODO dilate
		/*Imgproc.GaussianBlur(sourceImageBinary, sourceImageBinary, new Size(5,5),0);
		Imgproc.erode(sourceImageBinary, sourceImageBinary, Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(2,2)), new Point(0, 0), 3);
		//Imgproc.dilate(sourceImageBinary, sourceImageBinary, Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(5,5)), new Point(0, 0), 1);
		Imgcodecs.imwrite("image-destination/erode-dilate-thersold-erode-"+dateTimeComponent+".png", sourceImageBinary);*/
		
		Mat cannyMat = new Mat(sourceImageBinary.rows(),sourceImageBinary.cols(),CvType.CV_8UC1);
		Imgproc.Canny(sourceImageBinary, cannyMat, 50, 150);
		Highgui.imwrite("image-destination/canny-"+dateTimeComponent+".png", cannyMat);
		
		
		
		
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat hirearchy = new Mat();
		Imgproc.findContours(cannyMat, contours, hirearchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		Mat drawContour = new Mat(cannyMat.rows(),cannyMat.cols(),CvType.CV_8UC1,Scalar.all(0));
		
		
		List<MatOfPoint> filteredContours = new ArrayList<MatOfPoint>();
		
		List<MatOfPoint> filteredProcessedContours = new ArrayList<MatOfPoint>();
		
		for(MatOfPoint matOfPoint:contours){
			
			
			
			/*if(){
				
			}*/
			
			MatOfPoint2f contour2f = new MatOfPoint2f();
			matOfPoint.convertTo(contour2f, CvType.CV_32FC2);
			int arcLength = (int) Imgproc.arcLength(contour2f, true);
			if(arcLength > 100){//if(arcLength > 25){
				logger.info(matOfPoint.dump());
				/*System.out.println(matOfPoint.total());
				MatOfPoint2f contour2fAprox = new MatOfPoint2f();
				Imgproc.approxPolyDP(contour2f, contour2fAprox, 0.01*arcLength, true);
				MatOfPoint matOfPointApprox = new MatOfPoint();
				contour2fAprox.convertTo(matOfPointApprox, CvType.CV_32S);
				filteredContours.add(matOfPointApprox);*/
				//break;
				filteredContours.add(matOfPoint);
				
				MatOfPoint processedMatOfPoint = RegressionUtils.processContour(matOfPoint);
				filteredProcessedContours.add(processedMatOfPoint);
				//break;
			}
			
			
			/*logger.info(simpleRegression.getIntercept());
			logger.info(simpleRegression.getSlope());
			logger.info(simpleRegression.getSlopeStdErr());*/
		}
		System.out.println("Total contours:: "+contours.size());
		System.out.println("Total contour hirearchy:: "+hirearchy.size());
		System.out.println("Total filtered contours:: "+filteredContours.size());
		
		//System.out.println("Total filtered contours:: "+filteredContours.size());
		Imgproc.drawContours(drawContour, contours, -1, Scalar.all(255), 1);
		Highgui.imwrite("image-destination/contours-"+dateTimeComponent+".png", drawContour);
		
		Mat drawContourResult = new Mat(cannyMat.rows(),cannyMat.cols(),CvType.CV_8UC1,Scalar.all(0));
		for(MatOfPoint matOfPoint:filteredContours){
			List<Point> pointList = matOfPoint.toList();
			for (int i = 0; i < pointList.size()-1; i++) {
				Point point1 = pointList.get(i);
				Point point2 = pointList.get(i+1);
				if(RegressionUtils.isHorizontal(point1, point2) || RegressionUtils.isVertical(point1, point2)){
					int length = RegressionUtils.getLength(point1, point2);
					if(length < 5){
						continue;
					}
					//System.out.println("Drawing line between "+point1+" and "+point2);
					Core.line(drawContourResult, point1, point2, new Scalar(255));
				}
				
			}
		}
		//Imgproc.drawContours(drawContourResult, filteredContours, -1, Scalar.all(255), 1);
		Highgui.imwrite("image-destination/contours-filtered-"+dateTimeComponent+".png", drawContourResult);
		
		Mat drawContourRegretionResult = new Mat(cannyMat.rows(),cannyMat.cols(),CvType.CV_8UC1,Scalar.all(0));
		Imgproc.drawContours(drawContourRegretionResult, filteredProcessedContours, 0, Scalar.all(255), 1);
		//Imgproc.drawContours(drawContourRegretionResult, filteredProcessedContours, -1, Scalar.all(255),1,Imgproc.LINE_4,hirearchy,0,new Point(0,0));
		Highgui.imwrite("image-destination/contours-filtered-processed-"+dateTimeComponent+".png", drawContourRegretionResult);
		
				
		
		
		
		/*Mat skelitonImage = new Mat(sourceImageGreyScaled.size(),CvType.CV_8UC1,new Scalar(0));
		Mat tempImage = new Mat(sourceImageGreyScaled.size(),CvType.CV_8UC1);
		
		Mat structureElement = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(3,3));
		
		boolean done = false;
		do
		{
		  Imgproc.morphologyEx(sourceImageBinary, tempImage,Imgproc.MORPH_OPEN,structureElement);
		  Core.bitwise_not(tempImage, tempImage);
		  Core.bitwise_and(sourceImageBinary, tempImage, tempImage);
		  Core.bitwise_or(skelitonImage, tempImage, skelitonImage);
		  Imgproc.erode(sourceImageBinary, sourceImageBinary, structureElement);
		 
		  double max = 0;
		  //Core.minMaxLoc(sourceImageBinary, 0, &max);
		  MinMaxLocResult minMaxLocResult = Core.minMaxLoc(sourceImageBinary);
		  max = minMaxLocResult.maxVal;
		  done = (max == 0);
		} while (!done);
		
		Imgcodecs.imwrite("image-destination/skeliton1-"+dateTimeComponent+".png", skelitonImage);*/

	}
	
	public static void getMorphologicalSkeleton2(String fileName) {
		
		String dateTimeComponent = dateFormat.format(new Date());
		
		Mat sourceImage = null;
		
		sourceImage = Highgui.imread(fileName);
		
		System.out.println(sourceImage);
		
		Mat sourceImageGrey = new Mat(sourceImage.rows(),sourceImage.cols(),CvType.CV_8UC1);
		
		Imgproc.cvtColor(sourceImage, sourceImageGrey,  Imgproc.COLOR_RGB2GRAY);
		System.out.println(sourceImageGrey);
		
		int scaleFactor = 5;
		
		Size scaledSize = new Size(sourceImageGrey.size().width*scaleFactor, sourceImageGrey.size().height*scaleFactor);
		Mat sourceImageGreyScaled = new Mat(scaledSize,CvType.CV_8UC1);
		
		Imgproc.resize(sourceImageGrey, sourceImageGreyScaled,scaledSize,0,0,Imgproc.INTER_CUBIC);
		
		Imgproc.erode(sourceImageGreyScaled, sourceImageGreyScaled, Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(2,2)), new Point(0, 0), 1);
		
		Imgproc.equalizeHist(sourceImageGreyScaled, sourceImageGreyScaled);
		
		Imgproc.erode(sourceImageGreyScaled, sourceImageGreyScaled, Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(2,2)), new Point(0, 0), 2);
		
		Imgproc.threshold(sourceImageGreyScaled, sourceImageGreyScaled, 120, 255, Imgproc.THRESH_BINARY);
		Highgui.imwrite("image-destination/thresold-"+dateTimeComponent+".png", sourceImageGreyScaled);
		
		Imgproc.erode(sourceImageGreyScaled, sourceImageGreyScaled, Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(2,2)), new Point(0, 0), 2);
		Highgui.imwrite("image-destination/thresold-erode-"+dateTimeComponent+".png", sourceImageGreyScaled);
		
				
		Mat contourInput = sourceImageGreyScaled.clone();
		Imgproc.threshold(contourInput, contourInput, 240, 255, Imgproc.THRESH_BINARY_INV);
		//Imgproc.dilate(contourInput, contourInput, Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(5,5)), new Point(0, 0), 1);
		Highgui.imwrite("image-destination/contour-input-"+dateTimeComponent+".png", contourInput);
		
		/*Mat contourInputThin = contourInput.clone();
		ImageThinner.ThinSubiteration2(sourceImageGreyScaled, contourInputThin);
		ImageThinner.ThinSubiteration2(contourInputThin, contourInputThin);
		Imgcodecs.imwrite("image-destination/contour-input-thin-"+dateTimeComponent+".png", contourInputThin);*/

		Mat cannyMat = new Mat(sourceImageGreyScaled.rows(),sourceImageGreyScaled.cols(),CvType.CV_8UC1);
		Imgproc.Canny(sourceImageGreyScaled, cannyMat, 50, 150);
		Highgui.imwrite("image-destination/canny-"+dateTimeComponent+".png", cannyMat);
		

		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat hirearchy = new Mat();
		Imgproc.findContours(cannyMat, contours, hirearchy, 0, Imgproc.CHAIN_APPROX_SIMPLE);
		Mat drawContour = new Mat(contourInput.rows(),contourInput.cols(),CvType.CV_8UC1,Scalar.all(0));
		
		List<MatOfPoint> filteredContours = new ArrayList<MatOfPoint>();
		
		for(MatOfPoint matOfPoint:contours){
			
			//Imgproc.ar
			
			MatOfPoint2f contour2f = new MatOfPoint2f();
			matOfPoint.convertTo(contour2f, CvType.CV_32FC2);
			int arcLength = (int) Imgproc.arcLength(contour2f, false);
			if(arcLength > 0){//if(arcLength > 25){
				//System.out.println(matOfPoint.isContinuous());
			   filteredContours.add(matOfPoint);
				
				/*MatOfPoint processedMatOfPoint = RegressionUtils.processContour(matOfPoint);
				filteredProcessedContours.add(processedMatOfPoint);*/
				//break;
			}
		}
		
		
		
		
		Mat drawContourResult = new Mat(cannyMat.rows(),cannyMat.cols(),CvType.CV_8UC1,Scalar.all(0));
		
		Mat drawContourResultProcessed = drawContourResult.clone();
		
		
		for(MatOfPoint matOfPoint:filteredContours){
			
			List<Point> pointList = matOfPoint.toList();
			List<LineData> lines = new ArrayList<LineData>();
			Point openPoint = null;
			for (int i = 0; i < pointList.size()-1; i++) {
				Point point1 = pointList.get(i);
				Point point2 = pointList.get(i+1);
				//TODO
				int length = RegressionUtils.getLength(point1, point2);
				if(length >= 5){
					if(openPoint != null){
						int lengthTmp = RegressionUtils.getLength(openPoint, point1);
						if(lengthTmp >= 5){
							Core.line(drawContourResult, openPoint, point1, new Scalar(255),1);
						}
						openPoint = null;
					}
					
					if(RegressionUtils.isHorizontal(point1, point2) || RegressionUtils.isVertical(point1, point2)){
						LineData lineData = new LineData();
						lineData.setStartPointData(new PointData((int)point1.x, (int)point1.y));
						lineData.setEndPointData(new PointData((int)point2.x, (int)point2.y));
						lines.add(lineData);
						
					}else{
						//Imgproc.line(drawContourResult, point1, point2, new Scalar(255),1);
					}
					
				}else{
					if(openPoint == null){
						openPoint = point1;
					}
				}
				
				/*if(RegressionUtils.isHorizontal(point1, point2) || RegressionUtils.isVertical(point1, point2)){
					int length = RegressionUtils.getLength(point1, point2);
					if(length < 2){
						continue;
					}
					//System.out.println("Drawing line between "+point1+" and "+point2);
					LineData lineData = new LineData();
					lineData.setStartPointData(new PointData((int)point1.x, (int)point1.y));
					lineData.setEndPointData(new PointData((int)point2.x, (int)point2.y));
					lines.add(lineData);
					//TODO
					Imgproc.line(drawContourResult, point1, point2, new Scalar(255),1);
				}*/
				
			}
			
			List<LineData> hzLineData = new ArrayList<LineData>();
			List<LineData> vrtLineData = new ArrayList<LineData>();
			
			for(LineData lineData:lines){
				/*System.out.println(lineData);
				Point point1 = new Point(lineData.getStartPointData().getX(), lineData.getStartPointData().getY());
				Point point2 = new Point(lineData.getEndPointData().getX(), lineData.getEndPointData().getY());
				Imgproc.line(drawContourResultProcessed, point1, point2, new Scalar(255),1);*/
				if(lineData.isHorizontal()){
					if(!hzLineData.contains(lineData) && !hzLineData.contains(lineData.getReverseLine())){
						hzLineData.add(lineData);
					}
					continue;
				}
				
				if(lineData.isVertical()){
					if(!vrtLineData.contains(lineData) && !vrtLineData.contains(lineData.getReverseLine())){
						vrtLineData.add(lineData);
					}
				}
			}
			
			//System.out.println("Vert line count:: "+vrtLineData.size());
			
			List<LineGroup> lineVrtGroupList = new ArrayList<LineGroup>();
			//LineDataComparatorVertical lineDataComparatorVertical = new LineDataComparatorVertical();
			//Collections.sort(vrtLineData, lineDataComparatorVertical);
			for (int i = 0; i < vrtLineData.size(); i++) {
				LineData lineData = vrtLineData.get(i);
				LineGroup lastLineGroup = null;
				if(lineVrtGroupList.size() > 0){
					lastLineGroup = lineVrtGroupList.get(lineVrtGroupList.size()-1);
				}
				
				if(lastLineGroup != null){
					boolean addSuccess = lastLineGroup.addLine(lineData, 5, 5);
					if(!addSuccess){
						int lastGroupNo = lastLineGroup.getGroupNo();
						lastLineGroup = new LineGroup();
						lastLineGroup.setGroupNo(++lastGroupNo);
						lastLineGroup.addLine(lineData, 5, 5);
						lineVrtGroupList.add(lastLineGroup);
					}
					
				}else{
					lastLineGroup = new LineGroup();
					lastLineGroup.setGroupNo(1);
					lastLineGroup.addLine(lineData, 5, 5);
					lineVrtGroupList.add(lastLineGroup);
				}
			}
			
			for(LineGroup lineGroup:lineVrtGroupList){
				lineGroup.doRegression();
				Point point1 = new Point(lineGroup.getStartX(), lineGroup.getStartY());
				Point point2 = new Point(lineGroup.getEndX(), lineGroup.getEndY());
				Core.line(drawContourResult, point1, point2, new Scalar(255),1);
			}
			
			
			List<LineGroup> lineHzGroupList = new ArrayList<LineGroup>();
			//LineDataComparatorHorizontal comparatorHorizontal = new LineDataComparatorHorizontal();
			//Collections.sort(hzLineData, comparatorHorizontal);
			for (int i = 0; i < hzLineData.size(); i++) {
				LineData lineData = hzLineData.get(i);
				LineGroup lastLineGroup = null;
				if(lineHzGroupList.size() > 0){
					lastLineGroup = lineHzGroupList.get(lineHzGroupList.size()-1);
				}
				
				if(lastLineGroup != null){
					boolean addSuccess = lastLineGroup.addLine(lineData, 5, 5);
					if(!addSuccess){
						int lastGroupNo = lastLineGroup.getGroupNo();
						lastLineGroup = new LineGroup();
						lastLineGroup.setGroupNo(++lastGroupNo);
						lastLineGroup.addLine(lineData, 5, 5);
						lineHzGroupList.add(lastLineGroup);
					}
					
				}else{
					lastLineGroup = new LineGroup();
					lastLineGroup.setGroupNo(1);
					lastLineGroup.addLine(lineData, 5, 5);
					lineHzGroupList.add(lastLineGroup);
				}
			}
			
			
			for(LineGroup lineGroup:lineHzGroupList){
				lineGroup.doRegression();
				Point point1 = new Point(lineGroup.getStartX(), lineGroup.getStartY());
				Point point2 = new Point(lineGroup.getEndX(), lineGroup.getEndY());
				Core.line(drawContourResult, point1, point2, new Scalar(255),1);
			}
			
			
			
		}
		
		
		//Imgproc.drawContours(drawContour, contours, -1, Scalar.all(255), 3);
		Imgproc.drawContours(drawContour, filteredContours, -1, Scalar.all(255), 2);
		Highgui.imwrite("image-destination/contours-"+dateTimeComponent+".png", drawContour);
		
		//Imgproc.drawContours(drawContourResult, filteredContours, -1, Scalar.all(255), 3);
		Highgui.imwrite("image-destination/contours-filtered-"+dateTimeComponent+".png", drawContourResult);
		
		Highgui.imwrite("image-destination/contours-filtered-psd-"+dateTimeComponent+".png", drawContourResultProcessed);
		
		/*double newScaleFactor = 0.33;
		
		Size finalScaledSize = new Size(drawContourResult.size().width*newScaleFactor, drawContourResult.size().height*newScaleFactor);
		Mat drawContourResultFinal = new Mat(finalScaledSize,CvType.CV_8UC1);
		
		Imgproc.resize(drawContourResult, drawContourResultFinal,finalScaledSize,0,0,Imgproc.INTER_CUBIC);
		Imgcodecs.imwrite("image-destination/contours-filtered-actual-"+dateTimeComponent+".png", drawContourResultFinal);*/
		
	}
	
	
	/*public static void getSkeleton2(String fileName) {
		
		String dateTimeComponent = dateFormat.format(new Date());
		
		Mat sourceImage = null;
		
		sourceImage = Imgcodecs.imread(fileName);
		
		System.out.println(sourceImage);
		
		Mat sourceImageGrey = new Mat(sourceImage.rows(),sourceImage.cols(),CvType.CV_8UC1);
		
		Imgproc.cvtColor(sourceImage, sourceImageGrey,  Imgproc.COLOR_RGB2GRAY);
		System.out.println(sourceImageGrey);
		
		int scaleFactor = 3;
		
		Size scaledSize = new Size(sourceImageGrey.size().width*scaleFactor, sourceImageGrey.size().height*scaleFactor);
		Mat sourceImageGreyScaled = new Mat(scaledSize,CvType.CV_8UC1);
		
		Imgproc.resize(sourceImageGrey, sourceImageGreyScaled,scaledSize,0,0,Imgproc.INTER_CUBIC);
		
		Imgproc.threshold(sourceImageGreyScaled, sourceImageGreyScaled, 0, 128, Imgproc.THRESH_BINARY_INV);
		Imgcodecs.imwrite("image-destination/thresold-"+dateTimeComponent+".png", sourceImageGreyScaled);
		
		Imgproc.equalizeHist(sourceImageGreyScaled, sourceImageGreyScaled);
		
		
		//Imgproc.equalizeHist(sourceImageGrey, sourceImageGrey);
				
		Mat sourceImageBinary = new Mat(sourceImageGreyScaled.size(),CvType.CV_8UC1);
		//Imgproc.threshold(sourceImageGreyScaled, sourceImageGreyScaled, 120, 255, Imgproc.THRESH_BINARY_INV);
		
		
		Mat skelitonImage = new Mat(sourceImageGreyScaled.size(),CvType.CV_8UC1,new Scalar(0));
		
		Mat temp = new Mat();
        Mat eroded = new Mat();
        
        Mat structureElement = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(3,3));

		boolean done = false;
		
		 do{
	          Imgproc.erode(sourceImageBinary, eroded, structureElement);
	          Imgproc.dilate(eroded, temp, structureElement);
	          Core.subtract(sourceImageBinary, temp, temp);
	          Core.bitwise_or(skelitonImage, temp, skelitonImage);
	          eroded.copyTo(sourceImageBinary);
	          done = (Core.countNonZero(sourceImageBinary) == 0);
	        } while (!done);
	        
		 Imgproc.dilate(skelitonImage,skelitonImage,new Mat(),new Point(-1,-1),2);
		 Imgproc.erode(skelitonImage,skelitonImage,new Mat(),new Point(-1,-1),2);
		 
		 Imgcodecs.imwrite("image-destination/skeliton2-"+dateTimeComponent+".png", skelitonImage);
		 
		 Mat cannyMat = new Mat(skelitonImage.rows(),skelitonImage.cols(),CvType.CV_8UC1);
		 Imgproc.Canny(sourceImageBinary, cannyMat, 200, 250);
		 Imgcodecs.imwrite("image-destination/canny-"+dateTimeComponent+".png", cannyMat);
			
			
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat hirearchy = new Mat();
		Imgproc.findContours(skelitonImage, contours, hirearchy, 0, Imgproc.CHAIN_APPROX_SIMPLE);
		Mat drawContour = new Mat(skelitonImage.rows(),skelitonImage.cols(),CvType.CV_8UC1,Scalar.all(0));
			
			
			//List<MatOfPoint> filteredContours = new ArrayList<MatOfPoint>();
			
			for(MatOfPoint matOfPoint:contours){
				SimpleRegression simpleRegression = new SimpleRegression();
				logger.info(matOfPoint);
				logger.info(matOfPoint.dump());
				List<Point> points = matOfPoint.toList();
				for(Point point:points){
					simpleRegression.addData(point.x, point.y);
				}
				logger.info(simpleRegression.getIntercept());
				logger.info(simpleRegression.getSlope());
				logger.info(simpleRegression.getSlopeStdErr());
			}
			System.out.println("Total contours:: "+contours.size());
			//System.out.println("Total filtered contours:: "+filteredContours.size());
			Imgproc.drawContours(drawContour, contours, -1, Scalar.all(255), 1);
			Imgcodecs.imwrite("image-destination/contours-"+dateTimeComponent+".png", drawContour);
		
	}*/
	
	

}
