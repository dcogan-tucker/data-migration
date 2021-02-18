package com.sparta.dominic.model.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.sql.Date;

public class DateFormatter
{
	private static final String DATE_FORMAT_STRING = "dd/MM/yyyy";
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_STRING);

	public static Date format(String date)
	{
		Date date1 = null;
		try
		{
			date1 = new Date(DATE_FORMAT.parse(date).getTime());
		} catch (ParseException e)
		{
			e.printStackTrace();
		}
		return date1;
	}
}
