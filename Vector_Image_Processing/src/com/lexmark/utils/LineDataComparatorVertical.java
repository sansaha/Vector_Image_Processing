package com.lexmark.utils;

import java.util.Comparator;

public class LineDataComparatorVertical implements Comparator<LineData> {

	@Override
	public int compare(LineData o1, LineData o2) {
		// TODO Auto-generated method stub
		return o1.getStartPointData().getX()-o2.getStartPointData().getX();
	}

}
