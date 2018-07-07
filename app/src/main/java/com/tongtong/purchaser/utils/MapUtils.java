package com.tongtong.purchaser.utils;

public class MapUtils {
	public static int getGeoGashSizeByDistance(float distance) {
		if (distance > 156 * 1000) {
			return 3;
		} else if (distance < 156 * 1000 && distance >= 19.5 * 1000) {
			return 4;
		} else if (distance < 19.5 * 1000 && distance >= 4.9 * 1000) {
			return 5;
		} else {
			return 6;
		}
	}
}
