package com.sparta.dominic.model.producer;

import com.sparta.dominic.model.database.EmployeeDTO;
import com.sparta.dominic.model.consumer.EmployeeDTOManager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public final class CSVReader
{
	public static void transferToEmployeeDTOManager(String path, EmployeeDTOManager employeeDTOManager)
	{
		try(BufferedReader bufferedReader = loadToBufferedReader(path))
		{
			bufferedReader.lines()
					.filter(line -> Character.isDigit(line.charAt(0)))
					.map(line -> line.split(","))
					.map(EmployeeDTO::new)
					.forEach(employeeDTOManager::addEmployee);
			
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private static BufferedReader loadToBufferedReader(String path) throws IOException
	{
		return new BufferedReader(new FileReader(path));
	}
}
