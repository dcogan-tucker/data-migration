package com.sparta.dominic.model.consumer;

import com.sparta.dominic.model.database.EmployeeDTO;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

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

	public boolean queueNotEmpty()
	{
		return !employeeDTOQueue.isEmpty();
	}

	public EmployeeDTO pollEmployeeFromQueue()
	{
		synchronized (employeeDTOQueue)
		{
			if (queueNotEmpty())
			{
				employeeDTOQueue.notifyAll();
				return employeeDTOQueue.poll();
			}
			return null;
		}
	}

	public List<EmployeeDTO> getDuplicates()
	{
		return duplicates;
	}
}
