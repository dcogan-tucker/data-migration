package com.sparta.dominic.model.consumer;

import com.sparta.dominic.model.database.EmployeeDTO;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class EmployeeDTOManager
{
	public final Queue<EmployeeDTO> employeeDTOQueue = new LinkedBlockingQueue<>();
	private final Map<Integer, EmployeeDTO> employees = new HashMap<>();
	private final List<EmployeeDTO> duplicates = new ArrayList<>();

	private final Object lock = new Object();

	public void addEmployee(EmployeeDTO employeeDTO)
	{
		EmployeeDTO check = employees.putIfAbsent(employeeDTO.getId(), employeeDTO);
		synchronized (lock)
		{
			if (check == null)
			{
				employeeDTOQueue.add(employeeDTO);
			} else
			{
				duplicates.add(employeeDTO);
			}
			lock.notifyAll();
		}
	}

	public boolean employeeQueueIsEmpty()
	{
		synchronized (lock)
		{
			return employeeDTOQueue.isEmpty();
		}
	}

	public EmployeeDTO pollEmployeeFromQueue()
	{
		synchronized (lock)
		{
			EmployeeDTO employeeDTO =  employeeDTOQueue.poll();
			if (employeeDTO == null)
			{
				try
				{
					lock.wait();
				} catch (InterruptedException e)
				{
					Logger logger = Logger.getLogger(this.getClass().getSimpleName() + "Logger");
					logger.log(Level.SEVERE, e.getMessage(), e);
				}
				employeeDTO = pollEmployeeFromQueue();
			}
			return employeeDTO;
		}
	}

	public Collection<EmployeeDTO> getDuplicates()
	{
		return duplicates;
	}
}
