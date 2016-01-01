package com.lexmark.img;

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
import com.lexmark.utils.PointData;
import com.lexmark.utils.ZhangSuenThinning;

public class LexmarkImageProcessor {
	
	private static final Logger logger = Logger.getLogger(LexmarkImageProcessor.class);
	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
	private static final String IMAGE_DEST_BASE_DIR = "image-destination-1.0";
	
	
	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		String fileName = args.length >= 1 ? args[0] : "image-source/input.png";
		//fileName = "image-source/image-vessel.png";
		//fileName = "image-source/map.png";
		//fileName = "image-source/sample1.png";
		//fileName = "image-source/sample2.png";
		fileName = "image-source/sample3.png";
		
		
		
		String dateTimeComponent = dateFormat.format(new Date());
		
		Mat sourceImage = null;
		
		sourceImage = Highgui.imread(fileName);
		
		Mat sourceImageGrey = new Mat(sourceImage.rows(),sourceImage.cols(),CvType.CV_8UC1);
		
		Imgproc.cvtColor(sourceImage, sourceImageGrey,  Imgproc.COLOR_RGB2GRAY);
		Highgui.imwrite(IMAGE_DEST_BASE_DIR+"/input-grey.png", sourceImageGrey);
		
		int scaleFactor = 1;
		
		Size scaledSize = new Size(sourceImageGrey.size().width*scaleFactor, sourceImageGrey.size().height*scaleFactor);
		Mat sourceImageGreyScaled = new Mat(scaledSize,CvType.CV_8UC1);
		Imgproc.resize(sourceImageGrey, sourceImageGreyScaled,scaledSize,0,0,Imgproc.INTER_CUBIC);
		
		Mat blankImageGreyScaled = new Mat(scaledSize,CvType.CV_8UC1,new Scalar(255));
		int rowMax = blankImageGreyScaled.rows();
		int colMax = blankImageGreyScaled.cols();
		
				 
		for(int rowStart = 0;rowStart < rowMax;rowStart++){
			
			for(int colStart = 0;colStart < colMax;colStart++){
				double[] valueArray = sourceImageGrey.get(rowStart, colStart);
				
				double value = valueArray[0];
				
				if(value == 0){
					blankImageGreyScaled.put(rowStart, colStart, valueArray);
				}				
			}
		}
		
		Imgproc.threshold(blankImageGreyScaled, blankImageGreyScaled, 1, 255, Imgproc.THRESH_BINARY_INV);
		
		Highgui.imwrite(IMAGE_DEST_BASE_DIR+"/new-blank-img-"+dateTimeComponent+".png", blankImageGreyScaled);
		
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat hirearchy = new Mat();
		Imgproc.findContours(blankImageGreyScaled, contours, hirearchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
		System.out.println("Total contours:: "+contours.size());
		
		List<MatOfPoint> filterContours = new ArrayList<>();
		for(MatOfPoint matOfPoint:contours){
			double contourArea = Imgproc.contourArea(matOfPoint);
			MatOfPoint2f contour2f = new MatOfPoint2f();
			matOfPoint.convertTo(contour2f, CvType.CV_32FC2);
			int arcLength = (int) Imgproc.arcLength(contour2f, matOfPoint.isContinuous());
			System.out.println("Area::"+contourArea+", Arc Length::"+arcLength);
			if(contourArea > 0 && arcLength >= 5){
				filterContours.add(matOfPoint);
			}
		}
		
		Mat drawContour = new Mat(blankImageGreyScaled.size(),CvType.CV_8UC1,Scalar.all(255));
		Imgproc.drawContours(drawContour, filterContours, -1, Scalar.all(0), 1);
		Highgui.imwrite(IMAGE_DEST_BASE_DIR+"/contours-"+dateTimeComponent+".png", drawContour);
		
		/*Mat sourceImageGreyScaledCopy2 = sourceImageGreyScaled.clone();
		Imgproc.equalizeHist(sourceImageGreyScaled, sourceImageGreyScaled);
		Highgui.imwrite(IMAGE_DEST_BASE_DIR+"/input-hyst-"+dateTimeComponent+".png", sourceImageGreyScaled);
		
		Mat sourceImageGreyScaledCopy1 = sourceImageGreyScaled.clone();		
		
		Imgproc.dilate(sourceImageGreyScaledCopy2, sourceImageGreyScaledCopy2, Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(2,2)), new Point(0, 0),2);
		Imgproc.erode(sourceImageGreyScaled, sourceImageGreyScaledCopy1, Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(2,2)), new Point(0, 0),1);	
		
		//Imgproc.GaussianBlur(sourceImageGreyScaled, sourceImageGreyScaled, new Size(5,5),0);
		Highgui.imwrite(IMAGE_DEST_BASE_DIR+"/erode-"+dateTimeComponent+".png", sourceImageGreyScaledCopy1);
		Highgui.imwrite(IMAGE_DEST_BASE_DIR+"/dilate-"+dateTimeComponent+".png", sourceImageGreyScaledCopy2);*/	
		/*		
		Mat adaptiveThresold = mergeImage(sourceImageGreyScaledCopy1, sourceImageGreyScaledCopy2);	
		
		//adaptiveMethod - ADAPTIVE_THRESH_MEAN_C or ADAPTIVE_THRESH_GAUSSIAN_C
		//thresholdType - THRESH_BINARY or THRESH_BINARY_INV
		//blockSize - 3, 5, 7 so on
		Imgproc.adaptiveThreshold(adaptiveThresold, adaptiveThresold, 175, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV, 9, 5);
		String fileNameAdptThresold = IMAGE_DEST_BASE_DIR+"/thresold-adpt-"+dateTimeComponent+".png";
		Imgproc.threshold(adaptiveThresold, adaptiveThresold, 150, 255, Imgproc.THRESH_BINARY);
		Highgui.imwrite(fileNameAdptThresold, adaptiveThresold);
		
		Mat thinImage = ZhangSuenThinning.thinning(adaptiveThresold);
		Highgui.imwrite(IMAGE_DEST_BASE_DIR+"/thined-adaptive-"+dateTimeComponent+".png", thinImage);
		
		//String fileOut1 = ImageDetectByBorderColour.traceSkelitonFromRawInput(fileNameAdptThresold,12);
		//System.out.println("fileOut1::"+fileOut1);
		
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat hirearchy = new Mat();
		Imgproc.findContours(thinImage, contours, hirearchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
		System.out.println("Total contours:: "+contours.size());
		
		Mat drawContour = new Mat(adaptiveThresold.size(),CvType.CV_8UC1,Scalar.all(255));
		Imgproc.drawContours(drawContour, contours, -1, Scalar.all(0), 1);
		Highgui.imwrite(IMAGE_DEST_BASE_DIR+"/contours-"+dateTimeComponent+".png", drawContour);*/
		
		/*List<MatOfPoint> filteredContours = new ArrayList<MatOfPoint>();
		for(MatOfPoint matOfPoint:contours){
			double contourArea = Imgproc.contourArea(matOfPoint);
			
			if(contourArea == 0){
				logger.info(matOfPoint.dump());
				MatOfPoint2f contour2f = new MatOfPoint2f();
				matOfPoint.convertTo(contour2f, CvType.CV_32FC2);
				int arcLength = (int) Imgproc.arcLength(contour2f, matOfPoint.isContinuous());
				//System.out.println("Arc Length:"+arcLength);
				if(arcLength > 0){
					filteredContours.add(matOfPoint);
				}
			}else{
				filteredContours.add(matOfPoint);
			}
			
		}
		
		System.out.println("Filtered contours Initial:: "+filteredContours.size());
		
		filteredContours = ContourHelper.filterDuplicateContour(filteredContours,0,1);
		
		System.out.println("Filtered contours Final:: "+filteredContours.size());
		
		List<MatOfPoint> processedContours = new ArrayList<MatOfPoint>(filteredContours.size());
		for(MatOfPoint matOfPoint:filteredContours){
			MatOfPoint matOfPointProcessed = ContourHelper.processPoints(matOfPoint);
			processedContours.add(matOfPointProcessed);
		}
		
		Mat drawProcessedContour = new Mat(adaptiveThresold.size(),CvType.CV_8UC1,Scalar.all(255));
		Imgproc.drawContours(drawProcessedContour, processedContours, -1, Scalar.all(0), 1);
		Highgui.imwrite(IMAGE_DEST_BASE_DIR+"/contours-processed-"+dateTimeComponent+".png", drawContour);*/
		
		

				
	}
	
	private static Mat mergeImage(Mat erodeImage,Mat dilateImage){
		
		int rowMax = erodeImage.rows();
		int colMax = erodeImage.cols();
		Mat result = new Mat(erodeImage.size(), erodeImage.type());
		
		for(int rowStart = 0;rowStart < rowMax;rowStart++){
			for(int colStart = 0;colStart < colMax;colStart++){
				double[] valueArrayDilate = dilateImage.get(rowStart, colStart);
				double valueDilate = valueArrayDilate[0];
				
				double[] valueArrayErode = erodeImage.get(rowStart, colStart);
				double valueErode = valueArrayErode[0];
				
				if(valueErode == 0.0){
					result.put(rowStart, colStart, valueErode);
				}else{
					result.put(rowStart, colStart, valueDilate);	
				}
			}
		}
		String dateTimeComponent = dateFormat.format(new Date());
		String fileName = IMAGE_DEST_BASE_DIR+"/merged-"+dateTimeComponent+".png";
		Highgui.imwrite(fileName, result);
		
		return result;
		
	}

}
