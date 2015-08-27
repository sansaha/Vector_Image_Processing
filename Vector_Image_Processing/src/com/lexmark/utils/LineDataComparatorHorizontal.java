package com.lexmark.utils;

import java.util.Comparator;

public class LineDataComparatorHorizontal implements Comparator<LineData> {

	@Override
	public int compare(LineData o1, LineData o2) {

		return o1.getStartPointData().getY()-o2.getStartPointData().getY();
	}

}
