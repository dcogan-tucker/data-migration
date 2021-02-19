package com.sparta.dominic.model.database;

import com.sparta.dominic.model.util.DateFormatter;

import java.sql.Date;

public class EmployeeDTO
{
	private final int id;
	private final String namePrefix;
	private final String firstName;
	private final char middleInitial;
	private final String lastName;
	private final char gender;
	private final String email;
	private final Date dob;
	private final Date joinDate;
	private final int salary;

	public EmployeeDTO(int id, String namePrefix, String firstName, char middleInitial, String lastName, char gender, String email, Date dob, Date joinDate, int salary)
	{
		this.id = id;
		this.namePrefix = namePrefix;
		this.firstName = firstName;
		this.middleInitial = middleInitial;
		this.lastName = lastName;
		this.gender = gender;
		this.email = email;
		this.dob = dob;
		this.joinDate = joinDate;
		this.salary = salary;
	}

	public EmployeeDTO(String... data)
	{
		this(Integer.parseInt(data[0]), data[1], data[2], data[3].charAt(0),
				data[4], data[5].charAt(0), data[6],
				DateFormatter.format(data[7]),
				DateFormatter.format(data[8]), Integer.parseInt(data[9]));
	}

	public int getId()
	{
		return id;
	}

	public String getNamePrefix()
	{
		return namePrefix;
	}

	public String getFirstName()
	{
		return firstName;
	}

	public char getMiddleInitial()
	{
		return middleInitial;
	}

	public String getLastName()
	{
		return lastName;
	}

	public char getGender()
	{
		return gender;
	}

	public String getEmail()
	{
		return email;
	}

	public Date getDob()
	{
		return dob;
	}

	public Date getJoinDate()
	{
		return joinDate;
	}

	public int getSalary()
	{
		return salary;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof EmployeeDTO)
		{
			EmployeeDTO otherEmployeeDTO = (EmployeeDTO) obj;
			return id == otherEmployeeDTO.id;
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return id;
	}

	@Override
	public String toString()
	{
		return "Employee: " +
				"id='" + id + '\'' +
				", name='" + namePrefix + " " +
				firstName + " " + middleInitial + " " +
				lastName + '\'' +
				", gender=" + gender +
				", email='" + email + '\'' +
				", dob=" + dob +
				", joinDate=" + joinDate;
	}
}
