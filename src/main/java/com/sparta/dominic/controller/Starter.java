package com.sparta.dominic.controller;

public class Starter {
    private static final String CSV_PATH = "src/main/resources/employees.csv";
    private static final String LARGE_CSV_PATH = "src/main/resources/EmployeeRecordsLarge.csv";

    public static void starter() {
        DataMigrationApp dataMigrationApp = new DataMigrationApp(LARGE_CSV_PATH, 12, false);
        dataMigrationApp.launch();
    }
}
