package com.sparta.dominic.model.database;

import com.sparta.dominic.model.consumer.EmployeeDTOManager;
import com.sparta.dominic.view.Printer;

import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

public class EmployeesDAO
{
	private static final String URL = "jdbc:mysql://localhost:3306/my_local";

	private Connection connection;
	private final Properties properties = new Properties();

	public void setUp()
	{
		if (hasEmployeesTable())
		{
			dropTable();
		}
		createTable();
	}

	public void transferToDatabase(EmployeeDTOManager employeeDTOManager)
	{
		String addEmployees = "INSERT INTO employees (employee_id, prefix, first_name, " +
													 "middle_initial, last_name, gender, " +
													 "email, date_of_birth, join_date) " +
													 "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

		try (PreparedStatement addEmployeeStatement = connectToDatabase().prepareStatement(addEmployees))
		{
			int employeesAdded = 0;
			while (!employeeDTOManager.employeeQueueIsEmpty())
			{
				EmployeeDTO employeeDTO = employeeDTOManager.pollEmployeeFromQueue();
				addEmployeeStatement.setString(1, employeeDTO.getId());
				addEmployeeStatement.setString(2, employeeDTO.getNamePrefix());
				addEmployeeStatement.setString(3, employeeDTO.getFirstName());
				addEmployeeStatement.setString(4, String.valueOf(employeeDTO.getMiddleInitial()));
				addEmployeeStatement.setString(5, employeeDTO.getLastName());
				addEmployeeStatement.setString(6, String.valueOf(employeeDTO.getGender()));
				addEmployeeStatement.setString(7, employeeDTO.getEmail());
				addEmployeeStatement.setDate(8, employeeDTO.getDob());
				addEmployeeStatement.setDate(9, employeeDTO.getJoinDate());
				int hasRun = addEmployeeStatement.executeUpdate();
				if (hasRun == 1)
				{
					employeesAdded++;
					//Printer.printMessage("Employee with id " + dto.getId() + " added to the table.");
				}
			}
			Printer.printMessage(Thread.currentThread().getName() + " Has Added " + employeesAdded + " Successfully To the Employees Table.");
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			closeDatabaseConnection();
		}
	}

	private void createTable()
	{
		String createTable = "CREATE TABLE employees (" +
							 "employee_id CHAR(6) PRIMARY KEY," +
							 "prefix VARCHAR(10)," +
							 "first_name VARCHAR(30)," +
							 "middle_initial CHAR(1)," +
							 "last_name VARCHAR(30)," +
							 "gender CHAR(1)," +
							 "email VARCHAR(50)," +
							 "date_of_birth DATE," +
							 "join_date DATE" +
							 ")";

		try (PreparedStatement createStatement = connectToDatabase().prepareStatement(createTable))
		{
			int hasRun = createStatement.executeUpdate();
			if (hasRun == 0)
			{
				Printer.printMessage("Employees Table Created.\n");
			}
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			closeDatabaseConnection();
		}
	}

	private void dropTable()
	{
		String dropTable = "DROP TABLE employees";

		try (PreparedStatement dropStatement = connectToDatabase().prepareStatement(dropTable))
		{
			int hasRun = dropStatement.executeUpdate();
			if (hasRun == 0)
			{
				Printer.printMessage("Employees Table Dropped.\n");
			}
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			closeDatabaseConnection();
		}
	}

	private boolean hasEmployeesTable()
	{
		ResultSet table = null;
		try
		{
			DatabaseMetaData databaseMetaData = connectToDatabase().getMetaData();
			table = databaseMetaData.getTables(null, null, "employees", null);
			if (table.next())
			{
				return true;
			}
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			closeResultSet(table);
			closeDatabaseConnection();
		}
		return false;
	}

	private Connection connectToDatabase()
	{
		try
		{
			properties.load(new FileReader("src/main/resources/login.properties"));
			connection = DriverManager.getConnection(URL,
					properties.getProperty("username"), properties.getProperty("password"));
		}  catch (IOException | SQLException e)
		{
			e.printStackTrace();
		}
		return connection;
	}

	private void closeDatabaseConnection()
	{
		if (connection != null)
		{
			try
			{
				connection.close();
			} catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
	}

	private void closeResultSet(ResultSet resultSet)
	{
		if (resultSet != null)
		{
			try
			{
				resultSet.close();
			} catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
	}
}
