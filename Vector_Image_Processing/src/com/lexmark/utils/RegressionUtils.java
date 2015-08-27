package com.lexmark.utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math.stat.regression.SimpleRegression;
import org.apache.log4j.Logger;
import org.opencv.core.Core;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;

public class RegressionUtils {
	
	private static final Logger logger = Logger.getLogger(RegressionUtils.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		MatOfPoint2f matOfPoint2f = new MatOfPoint2f(new Point(1,1),new Point(2,2),new Point(3,3),new Point(3,6));
		
		int dY = -1;
		int dX = -1;
		
		int xPrev = -1;
		int yPrev = -1;
		
		
		Map<Integer,List<Point>> contoruGroupMap = new LinkedHashMap<>();
		
		int index = 1;
		for(Point point:matOfPoint2f.toList()){
			if(xPrev > -1){
				dX = (int) point.x - xPrev;
				dY = (int) point.y - yPrev;
				
				if(Math.abs(dX) > 1 || Math.abs(dY) > 1){
					index++;
					contoruGroupMap.put(index, new ArrayList<Point>());
					contoruGroupMap.get(index).add(new Point(xPrev,yPrev));
				}
				xPrev = (int) point.x;
				yPrev = (int) point.y;
				contoruGroupMap.get(index).add(point.clone());
				
			}else{
				xPrev = (int) point.x;
				yPrev = (int) point.y;
				contoruGroupMap.put(index, new ArrayList<Point>());
				contoruGroupMap.get(index).add(point.clone());
			}
		}
		
		/*for(Integer key:contoruGroupMap.keySet()){
			List<Point> refPointlist = contoruGroupMap.get(key);
			System.out.println("group::"+key);
			System.out.println("members:: "+refPointlist.size());
			for(Point point:refPointlist){
				System.out.println(point.x+" , "+point.y);
			}			
		}*/
		
		//System.out.println("After");
		Set<Integer> groupKeySet = contoruGroupMap.keySet();
		
		for(Integer key:groupKeySet){
			List<Point> refPointlist = contoruGroupMap.get(key);
			/*System.out.println("Ref points");
			for(Point point:refPointlist){
				System.out.println(point.x+" , "+point.y);
			}*/
			if(refPointlist.size() > 2){
				List<Point> regressionPoints = simpleRegrateMatPoints(refPointlist);
				refPointlist.clear();
				refPointlist.addAll(regressionPoints);
				//contoruGroupMap.put(key, regressionPoints);
			}
			
		}
		
		//Point[] resultPoints = new Point[groupKeySet.size()];
		for(Integer key:groupKeySet){
			List<Point> refPointlist = contoruGroupMap.get(key);
			System.out.println("group::"+key);
			System.out.println("members:: "+refPointlist.size());
			for(Point point:refPointlist){
				System.out.println(point.x+" , "+point.y);
			}
			
		}
		
		/*final RealMatrix factors=new Array2DRowRealMatrix(new double[][]{{1.0,0.0},{0.0,1.0}},false);
		  LeastSquaresConverter ls=new LeastSquaresConverter(new MultivariateVectorialFunction(){
		    public double[] value(    double[] variables){
		      return factors.operate(variables);
		    }
		  }
		,new double[]{2.0,-3.0});
		  NelderMead optimizer=new NelderMead();
		  optimizer.setConvergenceChecker(new SimpleScalarValueChecker(-1.0,1.0e-6));
		  optimizer.setMaxIterations(200);
		  try {
			RealPointValuePair optimum=optimizer.optimize(ls,GoalType.MINIMIZE,new double[]{10.0,10.0});
			
			optimum.getPointRef()[0];
			
		} catch (OptimizationException | FunctionEvaluationException
				| IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		//LeastSquaresConverter leastSquaresConverter = new LeastSquaresConverter(function, observations, weights)
		
		/*SimpleRegression simpleRegression = new SimpleRegression();
		
		List<Point> points = new ArrayList<Point>();
		
		
		points.add(new Point(536, 1100));
		points.add(new Point(534, 1102));
		points.add(new Point(529, 1102));
		points.add(new Point(528, 1103));
		points.add(new Point(526, 1103));
		points.add(new Point(525, 1102));
		points.add(new Point(519, 1102));
		points.add(new Point(525, 1102));
		points.add(new Point(526, 1103));
		points.add(new Point(528, 1103));
		points.add(new Point(529, 1102));
		points.add(new Point(535, 1102));
		points.add(new Point(537, 1100));
		
		int xMin = -1;
		int xMax = -1;
		int yMin = -1;
		int yMax = -1;
		
		for(Point point:points){
			simpleRegression.addData(point.x, point.y);
			int tmpX = (int) point.x;
			int tmpY = (int) point.y;
			
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
		
		System.out.println("xMin:"+xMin+" ,xMax:"+xMax+",dX:"+dX);
		System.out.println("yMin:"+yMin+" ,yMax:"+yMax+",dY:"+dY);
		
		System.out.println(simpleRegression.getIntercept());
		System.out.println(simpleRegression.getSlope());
		System.out.println(simpleRegression.getSlopeStdErr());
		
		
		Point startPoint = new Point();
		Point endPoint = new Point();
		
		if(dX >= dY){
			int yCal = (int) ((simpleRegression.getSlope()*xMin)+simpleRegression.getIntercept());
			System.out.println("x: "+xMin+", yCal:"+yCal);
			startPoint.x = xMin;
			startPoint.y = yCal;
			
			yCal = (int) ((simpleRegression.getSlope()*xMax)+simpleRegression.getIntercept());
			System.out.println("x: "+xMax+", yCal:"+yCal);
			endPoint.x = xMax;
			endPoint.y = yCal;
		}else{
			int xCal = (int) ((yMin -simpleRegression.getIntercept())/simpleRegression.getSlope()) ;
			System.out.println("x: "+xCal+", y:"+yMin);
			startPoint.x = xCal;
			startPoint.y = yMin;
			
			xCal = (int) ((yMax -simpleRegression.getIntercept())/simpleRegression.getSlope()) ;
			System.out.println("x: "+xCal+", y:"+yMax);
			endPoint.x = xCal;
			endPoint.y = yMax;
		}*/

	}
	
	public static MatOfPoint regretContours(MatOfPoint refMatOfPoint){
		int dY = -1;
		int dX = -1;
		
		int xPrev = -1;
		int yPrev = -1;
		
		
		Map<Integer,List<Point>> contoruGroupMap = new LinkedHashMap<>();
		
		int index = 1;
		for(Point point:refMatOfPoint.toList()){
			if(xPrev > -1){
				dX = (int) point.x - xPrev;
				dY = (int) point.y - yPrev;
				
				if(Math.abs(dX) > 1 || Math.abs(dY) > 1){
					index++;
					contoruGroupMap.put(index, new ArrayList<Point>());
					contoruGroupMap.get(index).add(new Point(xPrev,yPrev));
				}
				xPrev = (int) point.x;
				yPrev = (int) point.y;
				contoruGroupMap.get(index).add(point.clone());
				
			}else{
				xPrev = (int) point.x;
				yPrev = (int) point.y;
				contoruGroupMap.put(index, new ArrayList<Point>());
				contoruGroupMap.get(index).add(point.clone());
			}
		}
		
		Set<Integer> groupKeySet = contoruGroupMap.keySet();
		
		for(Integer key:groupKeySet){
			List<Point> refPointlist = contoruGroupMap.get(key);
			System.out.println("group::"+key);
			System.out.println("members:: "+refPointlist.size());
			for(Point point:refPointlist){
				System.out.println(point.x+" , "+point.y);
			}
			
		}
		
		System.out.println("After processing...");
		
		for(Integer key:groupKeySet){
			List<Point> refPointlist = contoruGroupMap.get(key);
			if(refPointlist.size() > 2){
				List<Point> regressionPoints = simpleRegrateMatPoints(refPointlist);
				refPointlist.clear();
				refPointlist.addAll(regressionPoints);
			}
			
		}
		
		for(Integer key:groupKeySet){
			List<Point> refPointlist = contoruGroupMap.get(key);
			System.out.println("group::"+key);
			System.out.println("members:: "+refPointlist.size());
			for(Point point:refPointlist){
				System.out.println(point.x+" , "+point.y);
			}
			
		}
		
		
		Set<Point> resultPointSet = new LinkedHashSet<Point>();
		for(Integer key:groupKeySet){
			List<Point> refPointlist = contoruGroupMap.get(key);
			
			if(refPointlist.size() == 2){
				resultPointSet.add(refPointlist.get(0));
				resultPointSet.add(refPointlist.get(1));
			}
			
			
		}
		
		for(Point point:resultPointSet){
			System.out.println(point.x+" , "+point.y);
		}
		
		
		return new MatOfPoint(resultPointSet.toArray(new Point[0]));
		
	}
	
	public static List<Point> simpleRegrateMatPoints(List<Point> refPoints){
		
		//List<Point> refPoints = matOfPoint.toList();
		
		int xMin = -1;
		int xMax = -1;
		int yMin = -1;
		int yMax = -1;
		SimpleRegression simpleRegression = new SimpleRegression();
		for(Point point:refPoints){
			simpleRegression.addData(point.x, point.y);
			int tmpX = (int) point.x;
			int tmpY = (int) point.y;
			
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
		
		Point startPoint = new Point();
		Point endPoint = new Point();
		
		List<Point> resultPoints = new ArrayList<Point>(2);
		
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
			startPoint.x = xMin;
			startPoint.y = yCal;
			
			yCal = (int) ((simpleRegression.getSlope()*xMax)+simpleRegression.getIntercept());
			//System.out.println("x: "+xMax+", yCal:"+yCal);
			endPoint.x = xMax;
			endPoint.y = yCal;
		}else{
			int xCal = (int) ((yMin -simpleRegression.getIntercept())/simpleRegression.getSlope()) ;
			//System.out.println("x: "+xCal+", y:"+yMin);
			startPoint.x = xCal;
			startPoint.y = yMin;
			
			xCal = (int) ((yMax -simpleRegression.getIntercept())/simpleRegression.getSlope()) ;
			//System.out.println("x: "+xCal+", y:"+yMax);
			endPoint.x = xCal;
			endPoint.y = yMax;
		}
		
		resultPoints.add(startPoint);
		resultPoints.add(endPoint);

		return resultPoints;
	}
	
	public static MatOfPoint processContour(MatOfPoint refMatOfPoint){
		List<Point> refPoints = refMatOfPoint.toList();
		
		List<Point> resultPoints = new ArrayList<Point>();
		
		Point initialPoint = refPoints.get(0);
		Point lastPoint = refPoints.get(refPoints.size()-1);
		
		resultPoints.add(initialPoint);
		
		Point previousPoint = initialPoint;
		
		for (int i = 1; i < refPoints.size(); i++) {
			Point tmpPoint = refPoints.get(i);
			
			if(isHorizontal(previousPoint, tmpPoint) || isVertical(previousPoint, tmpPoint)){
				int tmpLength = getLength(previousPoint, tmpPoint);
				if(tmpLength >= 5){
					if(!resultPoints.contains(previousPoint)){
						resultPoints.add(previousPoint);
					}
					resultPoints.add(tmpPoint);
				}
			}

			previousPoint = tmpPoint;

		}
		
		if(!resultPoints.contains(lastPoint)){
			resultPoints.add(lastPoint);
		}
		
		//resultPoints.add(lastPoint);
		
		/*for(Point point:refPoints){
			logger.info(point.x+" , "+point.y);
		}*/
		
		/*logger.info("After processing..");
		
		for(Point point:resultPoints){
			logger.info(point.x+" , "+point.y);
		}*/
		
		//System.out.println(resultPoints.size());		
				
				//TODO
		return new MatOfPoint(resultPoints.toArray(new Point[0]));
		//return new MatOfPoint(refPoints.toArray(new Point[0]));
	}
	
	public static boolean isHorizontal(Point point1,Point point2){
		int y1 = (int) point1.y;
		int y2 = (int) point2.y;
		
		return (y1 - y2 == 0);
	}
	
	public static boolean isVertical(Point point1,Point point2){
		int x1 = (int) point1.x;
		int x2 = (int) point2.x;
		
		return (x1 - x2 == 0);
	}
	
	public static int getLength(Point point1,Point point2){
		
		int length = 0;
		
		 int dx = (int) (point1.x-point2.x);
         int dy = (int) (point1.y-point2.y);
         
         length = (int) Math.sqrt((dx*dx)+(dy*dy));
		
		return length;
	}
	
	

}
