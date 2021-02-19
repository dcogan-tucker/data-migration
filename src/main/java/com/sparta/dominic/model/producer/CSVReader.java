package com.sparta.dominic.model.producer;

import com.sparta.dominic.model.database.EmployeeDTO;
import com.sparta.dominic.model.consumer.EmployeeDTOManager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class CSVReader
{
	public static void transferToDTOManager(String path, EmployeeDTOManager dtoManager)
	{
		try(BufferedReader bufferedReader = loadToBufferedReader(path))
		{
			bufferedReader.lines()
					.filter(line -> Character.isDigit(line.charAt(0)))
					.map(line -> line.split(","))
					.map(EmployeeDTO::new)
					.forEach(dtoManager::addEmployee);
			
		} catch (IOException e)
		{
			Logger logger = Logger.getLogger(CSVReader.class.getSimpleName() + "Logger");
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	private static BufferedReader loadToBufferedReader(String path) throws IOException
	{
		return new BufferedReader(new FileReader(path));
	}
}
