package com.sparta.dominic.util;

import java.util.logging.Logger;

public class DataMigrationLogger {
    private static Logger logger = Logger.getLogger(DataMigrationLogger.class.getSimpleName());

    public static Logger getLogger() {
        return logger;
    }
}
