package com.lexmark.utils;

import org.opencv.core.Mat;

public class ImageThinner {
	
	public static void ThinSubiteration1(Mat pSrc, Mat pDst) {
		int rows = pSrc.rows();
		int cols = pSrc.cols();
		pSrc.copyTo(pDst);
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				if (pSrc.get(i, j)[0] == 1.0f) {
					// / get 8 neighbors
					// / calculate C(p)
					int neighbor0 = (int) pSrc.get(i - 1, j - 1)[0];
					int neighbor1 = (int) pSrc.get(i - 1, j)[0];
					int neighbor2 = (int) pSrc.get(i - 1, j + 1)[0];
					int neighbor3 = (int) pSrc.get(i, j + 1)[0];
					int neighbor4 = (int) pSrc.get(i + 1, j + 1)[0];
					int neighbor5 = (int) pSrc.get(i + 1, j)[0];
					int neighbor6 = (int) pSrc.get(i + 1, j - 1)[0];
					int neighbor7 = (int) pSrc.get(i, j - 1)[0];
					int C = (~neighbor1 & (neighbor2 | neighbor3))
							+ (~neighbor3 & (neighbor4 | neighbor5))
							+ (~neighbor5 & (neighbor6 | neighbor7))
							+ (~neighbor7 & (neighbor0 | neighbor1));
					if (C == 1) {
						// / calculate N
						int N1 = (neighbor0 | neighbor1)
								+ (neighbor2 | neighbor3)
								+ (neighbor4 | neighbor5)
								+ (neighbor6 | neighbor7);
						int N2 = (neighbor1 | neighbor2)
								+ (neighbor3 | neighbor4)
								+ (neighbor5 | neighbor6)
								+ (neighbor7 | neighbor0);
						int N = Math.min(N1, N2);
						if ((N == 2) || (N == 3)) {
							// / calculate criteria 3
							int c3 = (neighbor1 | neighbor2 | ~neighbor4)
									& neighbor3;
							if (c3 == 0) {
								pDst.get(i, j)[0] = 0.0f;
							}
						}
					}
				}
			}
		}
	}


	public static void ThinSubiteration2(Mat pSrc, Mat pDst) {
		int rows = pSrc.rows();
		int cols = pSrc.cols();
		pSrc.copyTo(pDst);
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				if (pSrc.get(i, j)[0] == 1.0f) {
					// / get 8 neighbors
					// / calculate C(p)
					int neighbor0 = (int) pSrc.get(i - 1, j - 1)[0];
					int neighbor1 = (int) pSrc.get(i - 1, j)[0];
					int neighbor2 = (int) pSrc.get(i - 1, j + 1)[0];
					int neighbor3 = (int) pSrc.get(i, j + 1)[0];
					int neighbor4 = (int) pSrc.get(i + 1, j + 1)[0];
					int neighbor5 = (int) pSrc.get(i + 1, j)[0];
					int neighbor6 = (int) pSrc.get(i + 1, j - 1)[0];
					int neighbor7 = (int) pSrc.get(i, j - 1)[0];
					int C = (~neighbor1 & (neighbor2 | neighbor3))
							+ (~neighbor3 & (neighbor4 | neighbor5))
							+ (~neighbor5 & (neighbor6 | neighbor7))
							+ (~neighbor7 & (neighbor0 | neighbor1));
					if (C == 1) {
						// / calculate N
						int N1 = (neighbor0 | neighbor1)
								+ (neighbor2 | neighbor3)
								+ (neighbor4 | neighbor5)
								+ (neighbor6 | neighbor7);
						int N2 = (neighbor1 | neighbor2)
								+ (neighbor3 | neighbor4)
								+ (neighbor5 | neighbor6)
								+ (neighbor7 | neighbor0);
						int N = Math.min(N1, N2);
						if ((N == 2) || (N == 3)) {
							int E = (neighbor5 | neighbor6 | ~neighbor0)
									& neighbor7;
							if (E == 0) {
								pDst.get(i, j)[0] = 0.0f;
							}
						}
					}
				}
			}
		}
	}

}
