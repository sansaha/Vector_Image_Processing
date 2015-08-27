package com.lexmark.utils;

public class LinePair {
	
	private LineData lineData1;
	private LineData lineData2;
	
	private LineData refConnectingLine1;
	private LineData refConnectingLine2;
	
	
	public LineData getLineData1() {
		return lineData1;
	}
	public void setLineData1(LineData lineData1) {
		this.lineData1 = lineData1;
	}
	public LineData getLineData2() {
		return lineData2;
	}
	public void setLineData2(LineData lineData2) {
		this.lineData2 = lineData2;
	}
	
	public LineData getRefConnectingLine1() {
		return refConnectingLine1;
	}
	public void setRefConnectingLine1(LineData refConnectingLine1) {
		this.refConnectingLine1 = refConnectingLine1;
	}
	public LineData getRefConnectingLine2() {
		return refConnectingLine2;
	}
	public void setRefConnectingLine2(LineData refConnectingLine2) {
		this.refConnectingLine2 = refConnectingLine2;
	}
	public int getTotalLength(){
		int length = 0;
		if(lineData1 != null){
			length = length+lineData1.getLength();
		}
		if(lineData2 != null){
			length = length+lineData2.getLength();
		}
		
		return length;
	}
	
	
	
	

}
