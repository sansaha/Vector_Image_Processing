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

import com.lexmark.utils.ImageDetectByBorderColour;



public class ImageProcessor {
	
	private static final Logger logger = Logger.getLogger(ImageProcessor.class);
	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
	
	
	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		String fileName = args.length >= 1 ? args[0] : "image-source/input.png";
		//fileName = "image-source/image-vessel.png";
		//fileName = "image-source/map.png";
		String dateTimeComponent = dateFormat.format(new Date());
		
		Mat sourceImage = null;
		
		//sourceImage = Imgcodecs.imread(fileName);
		sourceImage = Highgui.imread(fileName);
		
		System.out.println(sourceImage);
		
		Mat sourceImageGrey = new Mat(sourceImage.rows(),sourceImage.cols(),CvType.CV_8UC1);
		
		Imgproc.cvtColor(sourceImage, sourceImageGrey,  Imgproc.COLOR_RGB2GRAY);
		System.out.println(sourceImageGrey);
		//Imgcodecs.imwrite("image-destination1/input-grey-"+dateTimeComponent+".png", sourceImageGrey);
		
		int scaleFactor = 2;
		
		Size scaledSize = new Size(sourceImageGrey.size().width*scaleFactor, sourceImageGrey.size().height*scaleFactor);
		Mat sourceImageGreyScaled = new Mat(scaledSize,CvType.CV_8UC1);
		Imgproc.resize(sourceImageGrey, sourceImageGreyScaled,scaledSize,0,0,Imgproc.INTER_CUBIC);
		
		
		Imgproc.equalizeHist(sourceImageGreyScaled, sourceImageGreyScaled);
		
		//TODO
		//Highgui.imwrite("image-destination1/hyst-"+dateTimeComponent+".png", sourceImageGreyScaled);
		
		Imgproc.erode(sourceImageGreyScaled, sourceImageGreyScaled, Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(2,2)), new Point(0, 0), 1);
		Imgproc.dilate(sourceImageGreyScaled, sourceImageGreyScaled, Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(2,2)), new Point(0, 0), 1);
		
		//Imgproc.GaussianBlur(sourceImageGreyScaled, sourceImageGreyScaled, new Size(5,5),0);
		Imgproc.medianBlur(sourceImageGreyScaled, sourceImageGreyScaled, 5);
		Highgui.imwrite("image-destination1/hyst-errode-dialate-"+dateTimeComponent+".png", sourceImageGreyScaled);
		
		Imgproc.threshold(sourceImageGreyScaled, sourceImageGreyScaled, 150, 255, Imgproc.THRESH_BINARY);
		
		
		//adaptiveMethod - ADAPTIVE_THRESH_MEAN_C or ADAPTIVE_THRESH_GAUSSIAN_C
		//thresholdType - THRESH_BINARY or THRESH_BINARY_INV
		//blockSize - 3, 5, 7 so on
		//Imgproc.adaptiveThreshold(sourceImageGreyScaled, sourceImageGreyScaled, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV, 15, 4);
		/*Imgproc.adaptiveThreshold(sourceImageGreyScaled, sourceImageGreyScaled, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 15, 4);
		//Imgproc.erode(sourceImageGreyScaled, sourceImageGreyScaled, Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(2,2)), new Point(0, 0), 1);
		Imgproc.equalizeHist(sourceImageGreyScaled, sourceImageGreyScaled);
		Imgproc.threshold(sourceImageGreyScaled, sourceImageGreyScaled, 150, 255, Imgproc.THRESH_BINARY_INV);*/
		
		String fileNameErode = "image-destination1/thresold-"+dateTimeComponent+".png";
		
		Highgui.imwrite(fileNameErode, sourceImageGreyScaled);
		
		/*Mat cannyMat = new Mat(sourceImageGreyScaled.size(),CvType.CV_8UC1);
		Imgproc.Canny(sourceImageGreyScaled, cannyMat, 50, 150);
		Imgcodecs.imwrite("image-destination1/canny-"+dateTimeComponent+".png", cannyMat);*/
		
		String fileOut = ImageDetectByBorderColour.traceSkelitonFromRawInput(fileNameErode,1);
		
		Mat sourceImageProcessed = Highgui.imread(fileOut);
		Mat sourceImageProcessedGray = new Mat(sourceImageProcessed.size(),CvType.CV_8UC1);
		
		Imgproc.cvtColor(sourceImageProcessed, sourceImageProcessedGray,  Imgproc.COLOR_RGB2GRAY);
		
		Imgproc.threshold(sourceImageProcessedGray, sourceImageProcessedGray, 150, 255, Imgproc.THRESH_BINARY_INV);
		
		//TODO
		//Highgui.imwrite("image-destination1/processed-thresold-"+dateTimeComponent+".png", sourceImageProcessedGray);
		
		
		
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat hirearchy = new Mat();
		Imgproc.findContours(sourceImageProcessedGray, contours, hirearchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
		System.out.println("Total contours:: "+contours.size());
		Mat drawContour = new Mat(sourceImageProcessedGray.size(),CvType.CV_8UC1,Scalar.all(255));
		//Mat drawContourRegretionResult = new Mat(cannyMat.rows(),cannyMat.cols(),CvType.CV_8UC1,Scalar.all(0));
		
		List<MatOfPoint> filteredContours = new ArrayList<MatOfPoint>();
		for(MatOfPoint matOfPoint:contours){
			double contourArea = Imgproc.contourArea(matOfPoint);
			System.out.println("Area::"+contourArea);
			
			if(contourArea == 0){
				logger.info(matOfPoint.dump());
				MatOfPoint2f contour2f = new MatOfPoint2f();
				matOfPoint.convertTo(contour2f, CvType.CV_32FC2);
				int arcLength = (int) Imgproc.arcLength(contour2f, matOfPoint.isContinuous());
				System.out.println("Arc Length:"+arcLength);
				if(arcLength > 0){
					filteredContours.add(matOfPoint);
				}
			}else{
				filteredContours.add(matOfPoint);
			}
			//if(contourArea > 499){ //499
				//logger.info(matOfPoint.isContinuous());
				//filteredContours.add(matOfPoint);
				
				//TODO
				//logger.info(matOfPoint.dump());
				
				
			//}
		}
		
		System.out.println("Filtered contours:: "+filteredContours.size());
		
		filteredContours = ContourHelper.filterDuplicateContour(filteredContours,0,1);
		
		System.out.println("Filtered contours:: "+filteredContours.size());
		
		//ContourHelper.processPoints(filteredContours.get(1));
		
		
		Imgproc.drawContours(drawContour, filteredContours, -1, Scalar.all(0), 1);
		Highgui.imwrite("image-destination1/contours-"+dateTimeComponent+".png", drawContour);
	}
	

	/**
	 * @param args
	 */
	public static void main1(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		//String fileName = args.length >= 1 ? args[0] : "image-source/image-vessel.png";
		String fileName = args.length >= 1 ? args[0] : "image-source/input.png";
		//String fileName = args.length >= 1 ? args[0] : "image-source/input-skeliton-1.png";
		
		String dateTimeComponent = dateFormat.format(new Date());
		
		Mat sourceImage = null;
		
		sourceImage = Highgui.imread(fileName);
		
		System.out.println(sourceImage);
		
		Mat sourceImageGrey = new Mat(sourceImage.rows(),sourceImage.cols(),CvType.CV_8UC1);
		
		Imgproc.cvtColor(sourceImage, sourceImageGrey,  Imgproc.COLOR_RGB2GRAY);
		System.out.println(sourceImageGrey);
		Highgui.imwrite("image-destination1/input-grey-"+dateTimeComponent+".png", sourceImageGrey);
		
		int scaleFactor = 3;
		
		Size scaledSize = new Size(sourceImageGrey.size().width*scaleFactor, sourceImageGrey.size().height*scaleFactor);
		Mat sourceImageGreyScaled = new Mat(scaledSize,CvType.CV_8UC1);
		Imgproc.resize(sourceImageGrey, sourceImageGreyScaled,scaledSize,0,0,Imgproc.INTER_CUBIC);
		
		//Mat destination = new Mat(sourceImage.rows(),sourceImage.cols(),sourceImage.type());
		//Mat gaussiabBlurImg = new Mat(sourceImage.rows(),sourceImage.cols(),CvType.CV_8UC1);
		
		//Imgcodecs.imwrite("image-source/input-123.png", sourceImage);
		
		//Imgproc.GaussianBlur(sourceImage, destination, new Size(45,45), 0);
		//Imgproc.GaussianBlur(sourceImage, destination, new Size (5,5), 2.2, 2);
		//Imgproc.GaussianBlur(sourceImageGreyScaled, sourceImageGreyScaled, new Size(5,5),0);
		//Imgcodecs.imwrite("image-destination1/GaussianBlur-"+dateTimeComponent+".png", sourceImageGrey);
		Imgproc.equalizeHist(sourceImageGreyScaled, sourceImageGreyScaled);
		
		//Mat destinationErode = new Mat(sourceImageGreyScaled.rows(),sourceImageGreyScaled.cols(),CvType.CV_8UC1);
		
		//Imgproc.threshold(sourceImageGreyScaled, sourceImageGreyScaled, 120, 255, Imgproc.THRESH_BINARY);
		//Imgcodecs.imwrite("image-destination1/thresold-"+dateTimeComponent+".png", sourceImageGreyScaled);
				
		//Imgproc.erode(sourceImageGreyScaled, destinationErode, Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(2,2)),new Point(0, 0),3);
		//Imgcodecs.imwrite("image-destination1/erode-"+dateTimeComponent+".png", destinationErode);
		
		
		//Mat destinationDilate = new Mat(sourceImage.rows(),sourceImage.cols(),sourceImage.type());
		//Imgproc.threshold(destinationErode, destinationErode, 100, 255, Imgproc.THRESH_BINARY);
		//Imgcodecs.imwrite("image-destination1/erode-thresold-"+dateTimeComponent+".png", destinationErode);
		
		Mat destinationDilate = new Mat(sourceImageGreyScaled.rows(),sourceImageGreyScaled.cols(),CvType.CV_8UC1);
		Imgproc.dilate(sourceImageGreyScaled, destinationDilate, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1,1)),new Point(0, 0),1);
		Highgui.imwrite("image-destination1/dilate-"+dateTimeComponent+".png", destinationDilate);
		
		/*Mat destinationErodeDilate = new Mat(sourceImageGreyScaled.rows(),sourceImageGreyScaled.cols(),CvType.CV_8UC1);
		//Core.addWeighted(destinationErode, 1, destinationDilate, 1, 0, destinationErodeDilate);
		Core.add(destinationErode, destinationDilate, destinationErodeDilate);
		Imgcodecs.imwrite("image-destination1/erode-dilate-"+dateTimeComponent+".png", destinationErodeDilate);*/
		
		
		Mat cannyMat = new Mat(sourceImageGreyScaled.rows(),sourceImageGreyScaled.cols(),CvType.CV_8UC1);
		
		Imgproc.Canny(destinationDilate, cannyMat, 50, 150);
		Highgui.imwrite("image-destination1/canny-"+dateTimeComponent+".png", cannyMat);
		
		List<MatOfPoint> contours = new ArrayList<>();
		Mat hirearchy = new Mat();
		Imgproc.findContours(cannyMat, contours, hirearchy, 0, Imgproc.CHAIN_APPROX_SIMPLE);
		Mat drawContour = new Mat(sourceImageGreyScaled.rows(),sourceImageGreyScaled.cols(),CvType.CV_8UC1,Scalar.all(0));	
//		List<MatOfPoint> resultPolys = new ArrayList<>();
//		for (int i = 0; i < contours.size(); i++) {
//			
//			MatOfPoint2f resultPoly = new MatOfPoint2f();
//			MatOfPoint contour = contours.get(i);
//			int rows = contour.rows();
//			int cols = contour.cols();
//			double[] points = new double[rows * cols];
//			for (int j = 0; j < rows; j++){
//				for(int k = 0; k < cols; k++){
//					points[(j*k) + k] = contour.get(row, col)
//				}
//			}
//			MatOfPoint2f m2f = new MatOfPoint2f(m)
//			Imgproc.approxPolyDP(m2f, resultPoly, 0.0, true);
//			resultPolys.add(new MatOfPoint(m2f));
//			
//			MatOfPoint contour = contours.get(i);
//			MatOfPoint2f contour2f = new MatOfPoint2f();
//			contour.convertTo(contour2f, CvType.CV_32FC2);
//			MatOfPoint2f contour2fApprox = new MatOfPoint2f();
//			Imgproc.approxPolyDP(contour2f, contour2fApprox, 0.0, true);
//			
//			MatOfPoint contourApprox = new MatOfPoint();
//			contour2fApprox.convertTo(contourApprox, CvType.CV_32S);
//			resultPolys.add(contourApprox);
//			
//		}
//		
//		Imgproc.fillPoly(drawContour, resultPolys, Scalar.all(0),8,0,new Point(0,0));
		
		Imgproc.drawContours(drawContour, contours, -1, Scalar.all(255), 1);
		Highgui.imwrite("image-destination1/contours-"+dateTimeComponent+".png", drawContour);
		
		//cvShowImage("Image New Merged", sourceImage);
	       
	     
		//ImageProcessor.cvWaitKey();
		
		
		
		
		//Imgcodecs.imwrite("image-source/input-erode12.png", destinationErode);
		//Imgcodecs.imwrite("image-source/input-dilate1.png", destinationErode);
		//Imgcodecs.imwrite("image-source/input-canny1.png", cannyMat);

	}
	
	
	/*Mat thresholdBW = new Mat(original.rows(),original.cols(), CvType.CV_8UC1);
	Core.inRange(hsv, new Scalar(0, 0, 0), new Scalar(360, 70, 70), thresholdBW);

	Mat thresholdBLUE = new Mat(original.rows(),original.cols(), CvType.CV_8UC1);
	Core.inRange(hsv, new Scalar(90, 90, 90), new Scalar(130, 255, 255), thresholdBLUE);

	Mat threshold = new Mat(original.rows(),original.cols(), CvType.CV_8UC1);
	Core.add(thresholdBW, thresholdBLUE, threshold);*/

}
