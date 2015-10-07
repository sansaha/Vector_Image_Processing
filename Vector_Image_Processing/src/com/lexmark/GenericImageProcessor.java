package com.lexmark;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

public class GenericImageProcessor {
	
	private static final Logger logger = Logger.getLogger(GenericImageProcessor.class);
	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
	private static final String IMAGE_DEST_BASE_DIR = "image-destination1";
	
	
	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		String fileName = args.length >= 1 ? args[0] : "image-source/input.png";
		//fileName = "image-source/image-vessel.png";
		//fileName = "image-source/map.png";
		fileName = "image-source/sample1.png";
		//fileName = "image-source/sample2.png";
		//fileName = "image-source/sample3.png";
		
		
		
		String dateTimeComponent = dateFormat.format(new Date());
		
		Mat sourceImage = null;
		
		sourceImage = Highgui.imread(fileName);
		
		Mat sourceImageGrey = new Mat(sourceImage.rows(),sourceImage.cols(),CvType.CV_8UC1);
		
		Imgproc.cvtColor(sourceImage, sourceImageGrey,  Imgproc.COLOR_RGB2GRAY);
		Highgui.imwrite("image-destination1/input-grey-"+dateTimeComponent+".png", sourceImageGrey);
		
		int scaleFactor = 1;
		
		Size scaledSize = new Size(sourceImageGrey.size().width*scaleFactor, sourceImageGrey.size().height*scaleFactor);
		Mat sourceImageGreyScaled = new Mat(scaledSize,CvType.CV_8UC1);
		Imgproc.resize(sourceImageGrey, sourceImageGreyScaled,scaledSize,0,0,Imgproc.INTER_CUBIC);
		
		Mat sourceImageGreyScaledCopy2 = sourceImageGreyScaled.clone();
		Imgproc.equalizeHist(sourceImageGreyScaled, sourceImageGreyScaled);
		
		Mat sourceImageGreyScaledCopy1 = sourceImageGreyScaled.clone();		
		
		Imgproc.dilate(sourceImageGreyScaledCopy2, sourceImageGreyScaledCopy2, Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(2,2)), new Point(0, 0),2);
		Imgproc.erode(sourceImageGreyScaled, sourceImageGreyScaledCopy1, Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(2,2)), new Point(0, 0),2);		
		
		//Imgproc.GaussianBlur(sourceImageGreyScaled, sourceImageGreyScaled, new Size(5,5),0);
		Highgui.imwrite(IMAGE_DEST_BASE_DIR+"/erode-"+dateTimeComponent+".png", sourceImageGreyScaledCopy1);
		Highgui.imwrite(IMAGE_DEST_BASE_DIR+"/dilate-"+dateTimeComponent+".png", sourceImageGreyScaledCopy2);
		//Highgui.imwrite(IMAGE_DEST_BASE_DIR+"/dilate-erode-"+dateTimeComponent+".png", sourceImageGreyScaledAdd);
		
		//Imgproc.GaussianBlur(sourceImageGreyScaledCopy2, sourceImageGreyScaledCopy2, new Size(5,5),0);
		//Imgproc.GaussianBlur(sourceImageGreyScaledCopy1, sourceImageGreyScaledCopy1, new Size(5,5),0);
		//Imgproc.threshold(sourceImageGreyScaledCopy2, sourceImageGreyScaledCopy2, 127, 255, Imgproc.THRESH_BINARY);
		
		//mergeImage(sourceImageGreyScaledCopy1, sourceImageGreyScaledCopy2);
		
		
		Mat adaptiveThresold = mergeImage(sourceImageGreyScaledCopy1, sourceImageGreyScaledCopy2);		
		
		//adaptiveMethod - ADAPTIVE_THRESH_MEAN_C or ADAPTIVE_THRESH_GAUSSIAN_C
		//thresholdType - THRESH_BINARY or THRESH_BINARY_INV
		//blockSize - 3, 5, 7 so on
		Imgproc.adaptiveThreshold(adaptiveThresold, adaptiveThresold, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV, 7, 4);
		String fileNameAdptThresold =IMAGE_DEST_BASE_DIR+"/thresold-adpt-"+dateTimeComponent+".png";
		//Imgproc.equalizeHist(adaptiveThresold, adaptiveThresold);
		Imgproc.threshold(adaptiveThresold, adaptiveThresold, 150, 255, Imgproc.THRESH_BINARY_INV);
		//Imgproc.dilate(adaptiveThresold, adaptiveThresold, Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(2,2)), new Point(0, 0), 1);
		//Imgproc.equalizeHist(adaptiveThresold, adaptiveThresold);
		Highgui.imwrite(fileNameAdptThresold, adaptiveThresold);
		
		//String fileOut1 = ImageDetectByBorderColour.traceSkelitonFromRawInput(fileNameAdptThresold,7);
		//System.out.println("fileOut1::"+fileOut1);

				
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
