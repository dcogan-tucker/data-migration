package com.sparta.dominic.controller;

import com.sparta.dominic.model.database.EmployeesDAO;
import com.sparta.dominic.model.consumer.EmployeeDTOManager;
import com.sparta.dominic.model.producer.CSVReader;
import com.sparta.dominic.view.Printer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Starter
{
	private static final String CSV_PATH = "src/main/resources/employees.csv";
	private static final String LARGE_CSV_PATH = "src/main/resources/EmployeeRecordsLarge.csv";

	public static void starter()
	{
		long start = System.nanoTime();
		EmployeeDTOManager employeeDTOManager = new EmployeeDTOManager();
		EmployeesDAO employeesDAO = new EmployeesDAO(employeeDTOManager);

		employeesDAO.setUp();

		ExecutorService producers = Executors.newCachedThreadPool();
		ExecutorService consumers = Executors.newCachedThreadPool();

		long start1 = System.nanoTime();
		CSVReader.transferToDTOManager(LARGE_CSV_PATH, employeeDTOManager);
		long end1 = System.nanoTime();

		Printer.printMessage("Transfer From CSV to DTO Manager. Time Taken: " + (double) (end1 - start1) / 1000000000 + " seconds.\n");

		Runnable transferToDatabase = () -> employeesDAO.transferToDatabase();
		createAndStartThreads(transferToDatabase, consumers, 16);

		shutdownAndTerminate(producers);
		shutdownAndTerminate(consumers);
		long end = System.nanoTime();

		printDuplicates(employeeDTOManager, false);

		Printer.printMessage("\nMigration Complete. Total Time Taken: " + (double) (end - start) / 1000000000 + " seconds.");
	}


	private static void createAndStartThreads(Runnable runnable, ExecutorService threadPool, int numberOfThreads)
	{
		if (numberOfThreads == 0)
		{
			runnable.run();
		}
		for (int i = 0; i < numberOfThreads; i++)
		{
			threadPool.submit(runnable);
		}
	}

	private static void shutdownAndTerminate(ExecutorService threadPool)
	{
		try
		{
			threadPool.shutdown();
			threadPool.awaitTermination(2, TimeUnit.MINUTES);
			threadPool.shutdownNow();
		} catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	private static void printDuplicates(EmployeeDTOManager dtoManager, boolean print)
	{
		if (print)
		{
			Printer.printMessage("\n" + dtoManager.getDuplicates().size() + " Employees Had Duplicate IDs So Were Not Added.");
			dtoManager.getDuplicates().forEach(System.out::println);
		}
	}
}
