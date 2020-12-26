package com.pa.xpath.data;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;

/**
 *
 * @author paul.anderson
 */
public class Asd {

	public static void main(String[] args) {
		String date = "2017-10-14T18:02:14.472";
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS").withZone(ZoneId.of("UTC"));
		TemporalAccessor ta = dateTimeFormatter.parse(date);
		System.out.println(Date.from(Instant.from(ta)));
		System.out.println(dateTimeFormatter.format(ta));
	}
}
