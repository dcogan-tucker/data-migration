package com.sparta.dominic.model.consumer;

import com.sparta.dominic.model.database.EmployeeDTO;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class EmployeeDTOManager
{
	private final Queue<EmployeeDTO> employeeDTOQueue = new LinkedBlockingQueue<>();
	private final Map<Integer, EmployeeDTO> employees = new HashMap<>();
	private final List<EmployeeDTO> duplicates = new ArrayList<>();

	public void addEmployee(EmployeeDTO employeeDTO)
	{
		EmployeeDTO check = employees.putIfAbsent(employeeDTO.getId(), employeeDTO);
		synchronized (employeeDTOQueue)
		{
			if (check == null)
			{
				employeeDTOQueue.add(employeeDTO);
			} else
			{
				duplicates.add(employeeDTO);
			}
			employeeDTOQueue.notifyAll();
		}
	}

	public boolean employeeQueueIsEmpty()
	{
		return employeeDTOQueue.isEmpty();
	}

	public EmployeeDTO pollEmployeeFromQueue()
	{
		synchronized (employeeDTOQueue)
		{
			while (employeeDTOQueue.isEmpty())
			{
				try
				{
					employeeDTOQueue.wait();
				} catch (InterruptedException e)
				{
					Logger logger = Logger.getLogger(this.getClass().getSimpleName() + "Logger");
					logger.log(Level.SEVERE, e.getMessage(), e);
				}
			}
			employeeDTOQueue.notifyAll();
			return  employeeDTOQueue.poll();
		}
	}

	public List<EmployeeDTO> getDuplicates()
	{
		return duplicates;
	}
}
