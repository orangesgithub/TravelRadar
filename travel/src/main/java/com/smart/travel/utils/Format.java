package com.smart.travel.utils;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Format {
	/**
	 * format util.Date to str date
	 * 
	 * @param date
	 *            format date
	 * @param pattern
	 *            format pattern, default yyyy-MM-dd HH:mm:ss
	 * @return
	 */
	public static String formatDate(Date date, String pattern) {
		if (pattern == null) {
			pattern = "yyyy-MM-dd HH:mm:ss";
		}

		SimpleDateFormat sdf = new SimpleDateFormat(pattern);

		return sdf.format(date);
	}

	public static String formatDate(long timeMillis, String pattern) {
		Date date = new Date(timeMillis);

		return formatDate(date, pattern);
	}

	public static Date parseDate(String strDate, String pattern) {
		if (pattern == null) {
			pattern = "yyyy-MM-dd HH:mm:ss";
		}

		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		Date date = null;
		try {
			date = sdf.parse(strDate);
		} catch (ParseException e) {
			return null;
		}

		return date;
	}

	public static String rightPad(String targetStr, int strLength) {
		int curLength = targetStr.length();
		String newString = "";
		int cutLength = strLength - curLength;
		for (int i = 0; i < cutLength; i++)
			newString += " ";
		return targetStr + newString;
	}

	public static String formatDecimal(double d, String pattern) {
		if (pattern == null) {
			pattern = "0.00";
		}

		if (Double.isNaN(d)) {
			d = 0.0;
			pattern = "0.##";
		}

		DecimalFormat df = new DecimalFormat(pattern);

		return df.format(d);
	}

	public static String formatFileSize(long size) {
		if (size <= 0) {
			return "0B";
		}
		final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
		int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
		return new DecimalFormat("#,##0.#").format(size
				/ Math.pow(1024, digitGroups))
				+ units[digitGroups];
	}

}
