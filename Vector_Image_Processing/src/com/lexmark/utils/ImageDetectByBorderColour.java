package com.lexmark.utils;



import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
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

public class ImageDetectByBorderColour {
	
	private static final Logger logger = Logger.getLogger(ImageDetectByBorderColour.class);
	
	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		//traceSkelitonFromRawInput("image-destination/test.png");
		traceSkelitonFromRawInput("image-source/input.png",1);
		//traceSkelitonFromRawInput("image-source/image-vessel.png");
		
		
		//traceSkeliton("image-source/input-skeliton-1.png");
	}
	
	public static String traceSkelitonFromRawInput(String fileName,int lineWidth){
		String dateTimeComponent = dateFormat.format(new Date());
		Mat sourceImage = null;
		//sourceImage = Imgcodecs.imread(fileName);
		sourceImage = Highgui.imread(fileName);
		Mat sourceImageGrey = new Mat(sourceImage.rows(),sourceImage.cols(),CvType.CV_8UC1);
		
		Imgproc.cvtColor(sourceImage, sourceImageGrey,  Imgproc.COLOR_RGB2GRAY);
		
		Imgproc.equalizeHist(sourceImageGrey, sourceImageGrey);

		logger.info("Image row:: "+sourceImageGrey.rows());
		logger.info("Image columns:: "+sourceImageGrey.cols());
		logger.info("Image channel:: "+sourceImageGrey.channels());
		
		
		int rowMax = sourceImageGrey.rows();
		int colMax = sourceImageGrey.cols();
		
		
		List<PointData> initialPointList = new ArrayList<PointData>();
		 
		for(int rowStart = 0;rowStart < rowMax;rowStart++){
			//double prevColourValue = 120;
			double prevColourValue = 0;
			int previousMatchCol = -1;
			//int colStart = 0;
			for(int colStart = 0;colStart < colMax;colStart++){
				double[] valueArray = sourceImageGrey.get(rowStart, colStart);
				
				double value = valueArray[0];
				
				if(Math.abs(value - prevColourValue) > 50){
					if((previousMatchCol >= 0 && (colStart - previousMatchCol) > lineWidth) || (previousMatchCol == -1)){
						PointData pointData = new PointData(colStart,rowStart);
						initialPointList.add(pointData);
						prevColourValue = value;
						previousMatchCol = colStart;
					}
					
				}
				
			}
		}
		
		
		PointDataComparator pointDataComparator = new PointDataComparator();
		pointDataComparator.setAxis("x");
				
		Collections.sort(initialPointList, pointDataComparator);
		
		List<LineData> vertLines = ImageLineExtractionHelper.getVerticalLines(initialPointList);
		
		logger.info("Tot vertical lines:: "+vertLines.size());
		
		initialPointList.clear();
		
		for(int colStart = 0;colStart < colMax;colStart++){
			//double prevColourValue = 120;
			double prevColourValue = 0;
			int previousMatchRow = -1;
			for(int rowStart = 0;rowStart < rowMax;rowStart++){
				double[] valueArray = sourceImageGrey.get(rowStart, colStart);
				
				double value = valueArray[0];
				
				if(Math.abs(value - prevColourValue) > 50){
					if((previousMatchRow >= 0 && (rowStart - previousMatchRow) > lineWidth) || (previousMatchRow == -1)){
						PointData pointData = new PointData(colStart,rowStart);
						initialPointList.add(pointData);
						prevColourValue = value;
						previousMatchRow = rowStart;
					}
					
				}
				
			}
		}
		
		
		pointDataComparator.setAxis("y");
				
		Collections.sort(initialPointList, pointDataComparator);
		
		
		List<LineData> horizLines = ImageLineExtractionHelper.getHorizontalLines(initialPointList);
		
		logger.info("Tot horiz lines:: "+horizLines.size());
		
		Mat colorDst = new Mat(sourceImageGrey.size(), CvType.CV_8UC1,new Scalar(255));
		
		
		List<LineData> filteredVerticalLines = new ArrayList<LineData>();
		
		
		for(LineData lineData:vertLines){
			Point pt1 = new Point(lineData.getStartPointData().getX(), lineData.getStartPointData().getY());
			Point pt2 = new Point(lineData.getEndPointData().getX(), lineData.getEndPointData().getY());
						
			
			
			filteredVerticalLines.add(lineData);
			//Imgproc.line(colorDst,pt1, pt2, new Scalar(0),1);
			Core.line(colorDst,pt1, pt2, new Scalar(0),1);
		}
		
		
		
		
		
		List<LineData> filteredHorizontalLines = new ArrayList<LineData>();
		
		for(LineData lineData:horizLines){
			Point pt1 = new Point(lineData.getStartPointData().getX(), lineData.getStartPointData().getY());
			Point pt2 = new Point(lineData.getEndPointData().getX(), lineData.getEndPointData().getY());
			
			//int length = lineData.getLength();
			/*if(length < ImageConstant.LINE_LENGTH_THRESOLD_HORIZONTAL){
				continue;
			}*/
			
			filteredHorizontalLines.add(lineData);
			
			//Imgproc.line(colorDst,pt1, pt2, new Scalar(0),1);
			Core.line(colorDst,pt1, pt2, new Scalar(0),1);
		}

		String fileOut = "image-destination1/input-skeliton-"+dateTimeComponent+".png";
		//Imgcodecs.imwrite(fileOut, colorDst);
		Highgui.imwrite(fileOut, colorDst);
		
		return fileOut;
				 
	}
	
	
	public static void traceSkeliton(String fileName){
		String dateTimeComponent = dateFormat.format(new Date());
		//Mat sourceImageGrey = Imgcodecs.imread(fileName);
		Mat sourceImageGrey = Highgui.imread(fileName);
		
		logger.info("Image row:: "+sourceImageGrey.rows());
		logger.info("Image columns:: "+sourceImageGrey.cols());
		logger.info("Image channel:: "+sourceImageGrey.channels());
		
		int scaleFactor = 3;
		
		Size scaledSize = new Size(sourceImageGrey.size().width*scaleFactor, sourceImageGrey.size().height*scaleFactor);
		Mat sourceImageGreyScaled = new Mat(scaledSize,CvType.CV_8UC1);
		Imgproc.resize(sourceImageGrey, sourceImageGreyScaled,scaledSize,0,0,Imgproc.INTER_CUBIC);
		
		int rowMax = sourceImageGreyScaled.rows();
		int colMax = sourceImageGreyScaled.cols();
		
		Mat sourceImageIrrode = new Mat(sourceImageGreyScaled.size(), CvType.CV_8UC1);
		
		Imgproc.GaussianBlur(sourceImageGreyScaled, sourceImageGreyScaled, new Size (5,5), 2, 2);
		
		Imgproc.erode(sourceImageGreyScaled, sourceImageIrrode, Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(2,2)),new Point(0, 0),1);
		Highgui.imwrite("image-destination1/erode-"+dateTimeComponent+".png", sourceImageIrrode);
		
		Imgproc.threshold(sourceImageIrrode, sourceImageIrrode, 200, 255, Imgproc.THRESH_BINARY);
		Highgui.imwrite("image-destination1/erode-thresold-"+dateTimeComponent+".png", sourceImageIrrode);
		
		Imgproc.dilate(sourceImageIrrode, sourceImageIrrode, Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(2,2)),new Point(0, 0),3);
		Highgui.imwrite("image-destination1/dilate-"+dateTimeComponent+".png", sourceImageIrrode);
		
		List<PointData> initialPointList = new ArrayList<PointData>();
		 
		for(int rowStart = 0;rowStart < rowMax;rowStart++){
			double prevColourValue = 255;
			//int colStart = 0;
			for(int colStart = 0;colStart < colMax;colStart++){
				double[] valueArray = sourceImageIrrode.get(rowStart, colStart);
				
				double value = valueArray[0];
				
				if(Math.abs(value - prevColourValue) > 50){
					PointData pointData = new PointData(colStart,rowStart);
					initialPointList.add(pointData);
					prevColourValue = value;
				}
				
			}
		}
		
		
		PointDataComparator pointDataComparator = new PointDataComparator();
		pointDataComparator.setAxis("x");
				
		Collections.sort(initialPointList, pointDataComparator);
		
		List<LineData> vertLines = ImageLineExtractionHelper.getVerticalLines(initialPointList);
		
		logger.info("Tot vertical lines:: "+vertLines.size());
		
		initialPointList.clear();
		
		for(int colStart = 0;colStart < colMax;colStart++){
			double prevColourValue = 255;
			for(int rowStart = 0;rowStart < rowMax;rowStart++){
				double[] valueArray = sourceImageIrrode.get(rowStart, colStart);
				
				double value = valueArray[0];
				
				if(Math.abs(value - prevColourValue) > 50){
					PointData pointData = new PointData(colStart,rowStart);
					initialPointList.add(pointData);
					prevColourValue = value;
				}
				
			}
		}
		
		
		pointDataComparator.setAxis("y");
				
		Collections.sort(initialPointList, pointDataComparator);
		
		
		List<LineData> horizLines = ImageLineExtractionHelper.getHorizontalLines(initialPointList);
		
		logger.info("Tot horiz lines:: "+horizLines.size());
		
		Mat colorDst = new Mat(sourceImageGreyScaled.size(), CvType.CV_8UC1,new Scalar(255));
		
		
		List<LineData> filteredVerticalLines = new ArrayList<LineData>();
		
		
		for(LineData lineData:vertLines){
			Point pt1 = new Point(lineData.getStartPointData().getX(), lineData.getStartPointData().getY());
			Point pt2 = new Point(lineData.getEndPointData().getX(), lineData.getEndPointData().getY());
						
			
			
			filteredVerticalLines.add(lineData);
			//Imgproc.line(colorDst,pt1, pt2, new Scalar(0),1);
			Core.line(colorDst,pt1, pt2, new Scalar(0),1);
		}
		
		
		
		
		
		List<LineData> filteredHorizontalLines = new ArrayList<LineData>();
		
		for(LineData lineData:horizLines){
			Point pt1 = new Point(lineData.getStartPointData().getX(), lineData.getStartPointData().getY());
			Point pt2 = new Point(lineData.getEndPointData().getX(), lineData.getEndPointData().getY());
			
			int length = lineData.getLength();
			if(length < ImageConstant.LINE_LENGTH_THRESOLD_HORIZONTAL){
				continue;
			}
			
			filteredHorizontalLines.add(lineData);
			
			//Imgproc.line(colorDst,pt1, pt2, new Scalar(0),1);
			Core.line(colorDst,pt1, pt2, new Scalar(0),1);
		}

		Highgui.imwrite("image-destination1/input-skeliton1-"+dateTimeComponent+".png", colorDst);
		
		Mat cannyMat = new Mat(colorDst.rows(),colorDst.cols(),CvType.CV_8UC1);
		
		Imgproc.Canny(colorDst, cannyMat, 50, 150);
		Highgui.imwrite("image-destination1/canny-"+dateTimeComponent+".png", cannyMat);
		
		List<MatOfPoint> contours = new ArrayList<>();
		Mat hirearchy = new Mat();
		Imgproc.findContours(cannyMat, contours, hirearchy, 0, Imgproc.CHAIN_APPROX_SIMPLE);
		Mat drawContour = new Mat(sourceImageGreyScaled.rows(),sourceImageGreyScaled.cols(),CvType.CV_8UC1,Scalar.all(0));
		
		List<MatOfPoint> filteredContours = new ArrayList<MatOfPoint>();
		
		for(MatOfPoint matOfPoint:contours){
			MatOfPoint2f contour2f = new MatOfPoint2f();
			matOfPoint.convertTo(contour2f, CvType.CV_32FC2);
			int arcLength = (int) Imgproc.arcLength(contour2f, true);
			if(arcLength > 25){
				MatOfPoint2f contour2fAprox = new MatOfPoint2f();
				Imgproc.approxPolyDP(contour2f, contour2fAprox, 0.01, true);
				MatOfPoint matOfPointApprox = new MatOfPoint();
				contour2fAprox.convertTo(matOfPointApprox, CvType.CV_32S);
				filteredContours.add(matOfPointApprox);
				//break;
			}
			
			/*logger.info(simpleRegression.getIntercept());
			logger.info(simpleRegression.getSlope());
			logger.info(simpleRegression.getSlopeStdErr());*/
		}
		
		
		Imgproc.drawContours(drawContour, filteredContours, -1, Scalar.all(255), 1);
		Highgui.imwrite("image-destination1/contours-"+dateTimeComponent+".png", drawContour);
				 
	}
	
	
	
	

}
