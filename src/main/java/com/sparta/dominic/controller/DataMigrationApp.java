package com.sparta.dominic.controller;

import com.sparta.dominic.model.consumer.EmployeeDTOManager;
import com.sparta.dominic.model.database.EmployeesDAO;
import com.sparta.dominic.model.producer.CSVReader;
import com.sparta.dominic.view.Printer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DataMigrationApp
{
	private final String path;
	private final int numberOfThreads;
	private final boolean printDuplicates;
	private final ExecutorService threadPool;
	private final EmployeeDTOManager employeeDTOManager;
	private final EmployeesDAO employeesDAO;

	public DataMigrationApp(String path, int numberOfThreads, boolean printDuplicates)
	{
		this.path = path;
		this.numberOfThreads = numberOfThreads;
		this.printDuplicates = printDuplicates;
		threadPool = Executors.newCachedThreadPool();
		employeeDTOManager = new EmployeeDTOManager();
		employeesDAO = new EmployeesDAO(employeeDTOManager);
	}

	public void launch()
	{
		long start = System.nanoTime();
		employeesDAO.setUp();

		long start1 = System.nanoTime();
		CSVReader.transferToDTOManager(path, employeeDTOManager);
		long end1 = System.nanoTime();

		Printer.printMessage("Transfer From CSV to EmployeeDTOManager. Time Taken: " + (double) (end1 - start1) / 1000000000 + " seconds.\n");

		createAndStartThreads();
		shutdownAndTerminateThreads();
		long end = System.nanoTime();

		printDuplicates(employeeDTOManager);

		Printer.printMessage("\nMigration Complete. Total Time Taken: " + (double) (end - start) / 1000000000 + " seconds.");
	}

	private void createAndStartThreads()
	{
		Runnable transferToDatabase = () -> employeesDAO.transferToDatabase();

		if (numberOfThreads == 0)
		{
			transferToDatabase.run();
		}
		for (int i = 0; i < numberOfThreads; i++)
		{
			threadPool.submit(transferToDatabase);
		}
	}

	private void shutdownAndTerminateThreads()
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

	private void printDuplicates(EmployeeDTOManager dtoManager)
	{
		if (printDuplicates)
		{
			Printer.printMessage("\n" + dtoManager.getDuplicates().size() + " Employees Had Duplicate IDs So Were Not Added.");
			dtoManager.getDuplicates().forEach(System.out::println);
		}
	}
}
