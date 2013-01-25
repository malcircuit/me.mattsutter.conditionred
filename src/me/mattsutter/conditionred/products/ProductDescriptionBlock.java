package me.mattsutter.conditionred.products;

import static me.mattsutter.conditionred.util.NWSFile.readHalfWords;

import java.io.DataInputStream;

import me.mattsutter.conditionred.util.LatLng;

/**
 * Class that holds all of the information contained in the product block
 * of the radar file.
 * @author Matt
 *
 */
public class ProductDescriptionBlock {
	// Operation modes.
	public static final int MAINTAINENCE = 0;
	public static final int CLEAR_AIR = 1;
	public static final int PRECIP = 2;
	// In bytes
	public static final int LENGTH = 50;
	
	public final double latitude;
	public final double longitude;
	public final LatLng radar_center;
	public final int altitude;
	public final int type;
	public final int mode;
	public final int vcp;
	public final int seq_num;
	public final int vol_scan_num;
	public final int vol_scan_date;
	public final int vol_scan_time;
	public final int prod_gen_date;
	public final int prod_gen_time;
	public final int[] p = new int[10];
	public final int elevation_num;
	public final int[] thresh = new int[16];
	public final int version;
	public final int symb_offset;
	public final int graphic_offset;
	public final int tabular_offset;

	public ProductDescriptionBlock(DataInputStream product){
		int[] buffer = readHalfWords(product, 1);	// Skipping the -1 divider
		
		buffer = readHalfWords(product, LENGTH);
		
		latitude = ((buffer[0] << 16) + buffer[1]) / 1000d;
		longitude = ((buffer[2] << 16) + buffer[3]) / 1000d;
		radar_center = new LatLng(latitude, longitude);
		altitude = buffer[4];
		type = buffer[5];
		mode = buffer[6];
		vcp = buffer[7];
		seq_num = buffer[8];
		vol_scan_num = buffer[9];
		vol_scan_date = buffer[10];
		vol_scan_time = (buffer[11] << 16) + buffer[12];
		prod_gen_date = buffer[13];
		prod_gen_time = (buffer[14] << 16) + buffer[15];
		p[0] = buffer[16];
		p[1] = buffer[17];
		elevation_num = buffer[18];
		p[2] = buffer[19];
		
		for (int i=0; i<16; i++)
			thresh[i] = buffer[20 + i];
		
		for (int i=0; i<7; i++)
			p[3 + i] = buffer[36 + i];
		
		version = (buffer[43] >> 8);
		
		symb_offset = (buffer[44] << 16) + buffer[45];
		graphic_offset = (buffer[46] << 16) + buffer[47];
		tabular_offset = (buffer[48] << 16) + buffer[49];
	}
	
	/**
	 * Converts seconds since midnight (UTC) to hours, minutes, seconds.
	 * @param time - The time in the form of seconds since midnight.
	 * @return Integer array containing the time -- hours, minutes, and seconds in that order.
	 */
	public static int[] secToHour( int time ){
		int hours = 0;
		int minutes = 0;
		int seconds = 0;
		hours = time/3600;
		minutes = (time - hours * 3600)/60;
		seconds = time - hours * 3600 - minutes * 60;
		int[] real_time = {hours, minutes, seconds};
		
		return real_time;
	}
	
	/**
	 * Turns a Modified Julian Date into a plain old Gregorian calendar date.
	 * @param mjdate - The Modified Julian Date you want to convert.
	 * @return A integer array that contains the month, day, and year in that order.
	 */
	public static int[] MJDtoCal(int mjdate){	
		// Turns MJD into a plain old Gregorian calendar. 

		// This shit was hard, BTW.

		// Number of days in each month
		final int[] months = {31,28,31,30,31,30,31,31,30,31,30,31};
		//int years;
		// Jan 1, 1970 is day 0 in MJD
		final int EPOCH = 1970;
		int year = EPOCH;
		int day = 0;
		int month = 1;
		int leapYears = 0;
		// Let's make a rough estimate of years
		final int years_since_epoch = mjdate/365;
		
		// find the number of leap years 

		// It's a leap year if:
		//	1. it's divisible by 4, but not 100... OR
		//	2. it's divisible by 400
		for (int i = 0; i < years_since_epoch; i++){
			if (year % 4 == 0)
				if (year % 100 == 0){
					if (year % 400 == 0)
						leapYears += 1;
				}
				else
					leapYears += 1;

			year++;
		}

		if (year % 4 == 0)
			if (year % 100 == 0){
				if (year % 400 == 0)
					months[1] = 29;
			}
			else
				months[1] = 29;

		day = mjdate - 366 * leapYears - 365 * (years_since_epoch - 10);
				
		while (month <= 12){
			if (day - months[month - 1] <= 0)
				break;
			else
				day -= months[month - 1];
			month++;
		}
		
		int[] real_date = {month, day, year};
		return real_date;
	}
}
