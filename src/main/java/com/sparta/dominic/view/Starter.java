package com.sparta.dominic.view;

public class Starter {
    private static final String CSV_PATH = "src/main/resources/employees.csv";
    private static final String LARGE_CSV_PATH = "src/main/resources/EmployeeRecordsLarge.csv";

    public static void starter() {
        DataMigrationApp dataMigrationApp = new DataMigrationApp(LARGE_CSV_PATH, 8, false);
        dataMigrationApp.launch();
    }
}
