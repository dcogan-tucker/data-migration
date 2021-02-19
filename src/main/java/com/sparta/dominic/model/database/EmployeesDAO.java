package com.sparta.dominic.model.database;

import com.sparta.dominic.model.consumer.EmployeeDTOManager;
import com.sparta.dominic.view.Printer;

import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

import static java.sql.Statement.SUCCESS_NO_INFO;

public class EmployeesDAO
{
	private static final String URL = "jdbc:mysql://localhost:3306/my_local?allowLoadLocalInfile=true&rewriteBatchedStatements=false";

	private Properties properties;
	private final int BATCH_SIZE = 100;

	{
		try
		{
			properties = new Properties();
			properties.load(new FileReader("src/main/resources/login.properties"));
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

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
		String addEmployees = "INSERT INTO employees " +
						      "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		PreparedStatement addEmployeeStatement = null;
		try (Connection connection = connectToDatabase())
		{
			addEmployeeStatement = connection.prepareStatement(addEmployees);
			int employeesAdded = 0;
			int batchCount = 0;
			while (!employeeDTOManager.employeeQueueIsEmpty())
			{
				EmployeeDTO employeeDTO = employeeDTOManager.pollEmployeeFromQueue();
				addEmployeeStatement.setInt(1, employeeDTO.getId());
				addEmployeeStatement.setString(2, employeeDTO.getNamePrefix());
				addEmployeeStatement.setString(3, employeeDTO.getFirstName());
				addEmployeeStatement.setString(4, String.valueOf(employeeDTO.getMiddleInitial()));
				addEmployeeStatement.setString(5, employeeDTO.getLastName());
				addEmployeeStatement.setString(6, String.valueOf(employeeDTO.getGender()));
				addEmployeeStatement.setString(7, employeeDTO.getEmail());
				addEmployeeStatement.setDate(8, employeeDTO.getDob());
				addEmployeeStatement.setDate(9, employeeDTO.getJoinDate());
				addEmployeeStatement.setInt(10, employeeDTO.getSalary());
				addEmployeeStatement.addBatch();
				batchCount++;
				if (batchCount % BATCH_SIZE == 0)
				{
					int[] updates = addEmployeeStatement.executeBatch();
					for (int i : updates)
					{
						if (i >= 0 || i == SUCCESS_NO_INFO)
						{
							employeesAdded++;
						}
					}
				}
			}
			int[] updates = addEmployeeStatement.executeBatch();
			for (int i : updates)
			{
				if (i >= 0)
				{
					employeesAdded++;
				}
			}
			Printer.printMessage(Thread.currentThread().getName() + " Has Added " + employeesAdded + " Successfully To the Employees Table.");
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			closeStatement(addEmployeeStatement);
		}
	}

	public void transferFromLocalCSV(String path)
	{
		String loadData = "LOAD DATA LOCAL INFILE '" + path + "' INTO TABLE employees";

		PreparedStatement loadDataStatement = null;
		try(Connection connection = connectToDatabase())
		{
			loadDataStatement = connection.prepareStatement(loadData);
			loadDataStatement.executeUpdate();
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			closeStatement(loadDataStatement);
		}
	}

	private void createTable()
	{
		String createTable = "CREATE TABLE employees (" +
							 "employee_id INT(6) PRIMARY KEY," +
							 "prefix VARCHAR(10)," +
							 "first_name VARCHAR(30)," +
							 "middle_initial CHAR(1)," +
							 "last_name VARCHAR(30)," +
							 "gender CHAR(1)," +
							 "email VARCHAR(50)," +
							 "date_of_birth DATE," +
							 "join_date DATE," +
							 "salary INT(6)" +
							 ")";

		PreparedStatement createStatement = null;
		try (Connection connection = connectToDatabase())
		{
			createStatement = connection.prepareStatement(createTable);
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
			closeStatement(createStatement);
		}
	}

	private void dropTable()
	{
		String dropTable = "DROP TABLE employees";

		PreparedStatement dropStatement = null;
		try (Connection connection = connectToDatabase())
		{
			dropStatement = connection.prepareStatement(dropTable);
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
			closeStatement(dropStatement);
		}
	}

	private boolean hasEmployeesTable()
	{
		ResultSet table = null;
		try (Connection connection = connectToDatabase())
		{
			DatabaseMetaData databaseMetaData = connection.getMetaData();
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
		}
		return false;
	}

	private Connection connectToDatabase()
	{
		Connection connection = null;
		try
		{
			connection = DriverManager.getConnection(URL,
					properties.getProperty("username"), properties.getProperty("password"));
		}  catch (SQLException e)
		{
			e.printStackTrace();
		}
		return connection;
	}

	private void closeStatement(Statement statement)
	{
		if (statement !=null)
		{
			try
			{
				statement.close();
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
