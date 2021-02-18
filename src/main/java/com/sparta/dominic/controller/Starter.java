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

	public static void starter()
	{
		long start = System.nanoTime();
		setUpDatabase();
		EmployeeDTOManager employeeDTOManager = new EmployeeDTOManager();

		ExecutorService threadPool = Executors.newCachedThreadPool();

		Runnable loadFromBufferedReader = () ->
				CSVReader.transferToEmployeeDTOManager(CSV_PATH, employeeDTOManager);
		createAndStartThreads(loadFromBufferedReader, threadPool, 1);

		Runnable addToDatabase = dtoToDatabase(employeeDTOManager);
		createAndStartThreads(addToDatabase, threadPool, 128);

		Printer.printMessage(Thread.activeCount() + " Threads Created.\n");
		shutdownAndTerminate(threadPool);
		long end = System.nanoTime();

		printDuplicates(employeeDTOManager, false);

		Printer.printMessage("\nMigration Complete. Total Time Taken: " + (double) (end - start) / 1000000000 + " seconds.");
	}

	private static void setUpDatabase()
	{
		new EmployeesDAO().setUp();
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

	private static Runnable dtoToDatabase(EmployeeDTOManager employeeDTOManager)
	{
		return () ->
		{
			EmployeesDAO threadEmployeesDAO = new EmployeesDAO();
			threadEmployeesDAO.transferToDatabase(employeeDTOManager);
		};
	}

	private static void shutdownAndTerminate(ExecutorService threadPool)
	{
		try
		{
			threadPool.shutdown();
			threadPool.shutdownNow();
			threadPool.awaitTermination(2, TimeUnit.MINUTES);
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
