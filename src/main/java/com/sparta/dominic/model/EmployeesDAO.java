package com.sparta.dominic.model;

import com.sparta.dominic.controller.EmployeeDTOManager;
import com.sparta.dominic.util.DataMigrationLogger;
import com.sparta.dominic.util.Printer;

import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;
import java.util.logging.Level;

import static java.sql.Statement.SUCCESS_NO_INFO;

public class EmployeesDAO {
    private final EmployeeDTOManager employeeDTOManager;
    private Properties properties;
    private final int BATCH_SIZE = 100;

    {
        try {
            properties = new Properties();
            properties.load(new FileReader("src/main/resources/login.properties"));
        } catch (IOException e) {
            DataMigrationLogger.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public EmployeesDAO(EmployeeDTOManager employeeDTOManager) {
        this.employeeDTOManager = employeeDTOManager;
    }

    public void transferToDatabase() {
        String addEmployees = "INSERT INTO employees " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = connectToDatabase();
             PreparedStatement addEmployeeStatement = connection.prepareStatement(addEmployees);) {
            int employeesAdded = 0;
            int batchCount = 0;
            while (employeeDTOManager.queueNotEmpty()) {
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
                if (batchCount % BATCH_SIZE == 0) {
                    int[] updates = addEmployeeStatement.executeBatch();
                    for (int i : updates) {
                        if (i >= 0 || i == SUCCESS_NO_INFO) {
                            employeesAdded++;
                        }
                    }
                }
            }
            int[] updates = addEmployeeStatement.executeBatch();
            for (int i : updates) {
                if (i >= 0 || i == SUCCESS_NO_INFO) {
                    employeesAdded++;
                }
            }
            Printer.printMessage(employeesAdded + " Employees Have Been Successfully Added to the Employees Table.");
        } catch (SQLException e) {
            DataMigrationLogger.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void transferFromLocalCSV(String path) {
        String loadData = "LOAD DATA LOCAL INFILE '" + path + "' INTO TABLE employees";

        try (Connection connection = connectToDatabase();
             PreparedStatement loadDataStatement = connection.prepareStatement(loadData)) {
            loadDataStatement.executeUpdate();
        } catch (SQLException e) {
            DataMigrationLogger.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private Connection connectToDatabase() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection((String) properties.get("url"),
                    properties.getProperty("username"), properties.getProperty("password"));
        } catch (SQLException e) {
            DataMigrationLogger.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
        return connection;
    }

    public void setUp() {
        if (hasEmployeesTable()) {
            dropTable();
        }
        createTable();
    }

    private void createTable() {
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

        try (Connection connection = connectToDatabase();
             PreparedStatement createStatement = connection.prepareStatement(createTable);) {
            int hasRun = createStatement.executeUpdate();
            if (hasRun == 0) {
                Printer.printMessage("Employees Table Created.\n");
            }
        } catch (SQLException e) {
            DataMigrationLogger.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private void dropTable() {
        String dropTable = "DROP TABLE employees";

        try (Connection connection = connectToDatabase();
             PreparedStatement dropStatement = connection.prepareStatement(dropTable);) {
            int hasRun = dropStatement.executeUpdate();
            if (hasRun == 0) {
                Printer.printMessage("Employees Table Dropped.\n");
            }
        } catch (SQLException e) {
            DataMigrationLogger.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private boolean hasEmployeesTable() {
        try (Connection connection = connectToDatabase();
             ResultSet table = connection.getMetaData().getTables(null, null, "employees", null)) {
            if (table.next()) {
                return true;
            }
        } catch (SQLException e) {
            DataMigrationLogger.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
        return false;
    }
}
