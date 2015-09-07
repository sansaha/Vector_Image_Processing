package com.lexmark;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

public class ExtractImageStructure {
	
	private static final Logger logger = Logger.getLogger(ExtractImageStructure.class);
	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
	private static final String outputFolderBase = "image-destination2/";

	/**
	 * @param args
	 */
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
		
		
		//Imgproc.equalizeHist(sourceImageGreyScaled, sourceImageGreyScaled);
		//Imgcodecs.imwrite(outputFolderBase+"hyst-"+dateTimeComponent+".png", sourceImageGreyScaled);
		
		//Imgproc.erode(sourceImageGreyScaled, sourceImageGreyScaled, Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(2,2)), new Point(0, 0), 1);
		//Imgproc.dilate(sourceImageGreyScaled, sourceImageGreyScaled, Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(2,2)), new Point(0, 0), 1);
		
		Highgui.imwrite(outputFolderBase+"hyst-errode-dialate-"+dateTimeComponent+".png", sourceImageGreyScaled);
		
		Mat sourceImgGrayBackup = sourceImageGreyScaled.clone();
		
		//Imgproc.threshold(sourceImageGreyScaled, sourceImageGreyScaled, 150, 255, Imgproc.THRESH_BINARY);
		
		
		//adaptiveMethod - ADAPTIVE_THRESH_MEAN_C or ADAPTIVE_THRESH_GAUSSIAN_C
		//thresholdType - THRESH_BINARY or THRESH_BINARY_INV
		//blockSize - 3, 5, 7 so on
		//Imgproc.adaptiveThreshold(sourceImageGreyScaled, sourceImageGreyScaled, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV, 15, 4);
		Imgproc.adaptiveThreshold(sourceImageGreyScaled, sourceImageGreyScaled, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 15, 4);
		//Imgproc.erode(sourceImageGreyScaled, sourceImageGreyScaled, Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(2,2)), new Point(0, 0), 1);
		//Imgproc.equalizeHist(sourceImageGreyScaled, sourceImageGreyScaled);
		
		//Imgproc.threshold(sourceImageGreyScaled, sourceImageGreyScaled, 150, 255, Imgproc.THRESH_BINARY);
		
		
		String fileNameErode = outputFolderBase+"thresold-"+dateTimeComponent+".png";
		Highgui.imwrite(fileNameErode, sourceImageGreyScaled);
		
		Mat matDest = new Mat(sourceImageGreyScaled.size(), CvType.CV_8UC1);
		
		//Core.add(sourceImgGrayBackup, sourceImageGreyScaled, matDest);
		//destinationErode, 1, destinationDilate, 1, 0, destinationErodeDilate
		Core.addWeighted(sourceImageGreyScaled,1, sourceImgGrayBackup, 1, 0, matDest);
		Highgui.imwrite(outputFolderBase+"added-"+dateTimeComponent+".png", matDest);
		
		/*Imgproc.dilate(sourceImageGreyScaled, sourceImageGreyScaled, Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(2,2)), new Point(0, 0), 4);
		//Imgproc.equalizeHist(sourceImageGreyScaled, sourceImageGreyScaled);
		Imgproc.threshold(sourceImageGreyScaled, sourceImageGreyScaled, 150, 255, Imgproc.THRESH_BINARY_INV);
		Imgcodecs.imwrite(outputFolderBase+"thresold-dialate-"+dateTimeComponent+".png", sourceImageGreyScaled);
		
		Mat sourceImage1 = Imgcodecs.imread("image-destination1/thresold-2015-09-03-20-50-05.png");
		
		Imgproc.cvtColor(sourceImage1, sourceImage1,  Imgproc.COLOR_RGB2GRAY);
		
		System.out.println("sourceImage1::"+sourceImage1);
		System.out.println("sourceImageGreyScaled::"+sourceImageGreyScaled);
		
		Mat matDest = new Mat(sourceImageGreyScaled.size(), CvType.CV_8UC1);
		
		Core.add(sourceImage1, sourceImageGreyScaled, matDest);
		Imgcodecs.imwrite(outputFolderBase+"added-"+dateTimeComponent+".png", matDest);*/
		
		/*Mat cannyMat = new Mat(sourceImageGreyScaled.size(),CvType.CV_8UC1);
		Imgproc.Canny(sourceImageGreyScaled, cannyMat, 50, 150);
		Imgcodecs.imwrite(outputFolderBase+"canny-"+dateTimeComponent+".png", cannyMat);*/
		/*
		String fileOut = ImageDetectByBorderColour.traceSkelitonFromRawInput(fileNameErode,1);
		
		Mat sourceImageProcessed = Imgcodecs.imread(fileOut);
		Mat sourceImageProcessedGray = new Mat(sourceImageProcessed.size(),CvType.CV_8UC1);
		
		Imgproc.cvtColor(sourceImageProcessed, sourceImageProcessedGray,  Imgproc.COLOR_RGB2GRAY);
		
		Imgproc.threshold(sourceImageProcessedGray, sourceImageProcessedGray, 150, 255, Imgproc.THRESH_BINARY_INV);
		
		Imgcodecs.imwrite(outputFolderBase+"processed-thresold-"+dateTimeComponent+".png", sourceImageProcessedGray);
		
		
		
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat hirearchy = new Mat();
		Imgproc.findContours(sourceImageProcessedGray, contours, hirearchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		System.out.println("Total contours:: "+contours.size());
		Mat drawContour = new Mat(cannyMat.size(),CvType.CV_8UC1,Scalar.all(0));*/
		
		//Mat drawContourRegretionResult = new Mat(cannyMat.rows(),cannyMat.cols(),CvType.CV_8UC1,Scalar.all(0));
		
		//hirearchy.get(row, col)
		
		//int i = 0;
		
		
		/*List<MatOfPoint> filteredContours = new ArrayList<MatOfPoint>();
		for(MatOfPoint matOfPoint:contours){
			double contourArea = Imgproc.contourArea(matOfPoint);
			System.out.println("Area::"+contourArea);
			if(contourArea > 499){ //499
				//i++;
				logger.info(matOfPoint.isContinuous());
				filteredContours.add(matOfPoint);
				
				//TODO
				logger.info(matOfPoint.dump());
				if(i > 2){
					break;
				}
				
			}
		}
		
		System.out.println("Filtered contours:: "+filteredContours.size());
		
		
		Imgproc.drawContours(drawContour, filteredContours, -1, Scalar.all(255), 1);
		Imgcodecs.imwrite(outputFolderBase+"contours-"+dateTimeComponent+".png", drawContour);*/

	}

}
